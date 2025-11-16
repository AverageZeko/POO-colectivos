package colectivo.interfaz.javafx;

import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import colectivo.interfaz.Mostrable;
import colectivo.interfaz.javafx.paneles.PanelDerecho;
import colectivo.interfaz.javafx.paneles.PanelIzquierdo;
import colectivo.interfaz.javafx.tareas.ConsultaRequest;
import colectivo.interfaz.javafx.tareas.ConsultaTask;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import colectivo.modelo.Parada;


/**
 * Ventana principal para la consulta de recorridos.
 * Ensambla y coordina los paneles de consulta (izquierda) y resultados (derecha).
 * Implementa {@link Mostrable}.
 *
 * NOTA: Ahora la UI únicamente recibe textos ya formateados para mostrar.
 */
public class Interfaz extends Application{

    private GestorDeVentanas gestor;
    private Stage escenarioPrincipal;
    private BorderPane raiz;

    private PanelIzquierdo panelIzquierdo;
    private PanelDerecho panelDerecho;
    private ImageView loadingView;
    private Button botonAumentarFuente, botonDisminuirFuente;
    private double tamanoFuenteActual = 12;
    private static final double TAMANO_FUENTE_BASE = 12;
    private static final int MAX_INCREMENTOS = 5;


    @Override
    public void start(Stage escenarioPrincipal) {
        this.escenarioPrincipal = escenarioPrincipal;
        raiz = new BorderPane();
        raiz.setPadding(new Insets(30));

        try {
            Image loadingImage = new Image(getClass().getResourceAsStream("/loading.gif"));
            loadingView = new ImageView(loadingImage);
            loadingView.setFitWidth(100);
            loadingView.setFitHeight(100);
            loadingView.setVisible(false);
        } catch (Exception e) {
            System.err.println("Error al cargar el GIF de carga: /loading.gif no encontrado.");
            loadingView = new ImageView();
        }

        // Crear paneles
        panelIzquierdo = new PanelIzquierdo(this::manejarCalculo, this::manejarVolver);
        panelDerecho = new PanelDerecho(this::mostrarMapa, this::getBundle);

        raiz.setLeft(panelIzquierdo.getLayout());
        raiz.setCenter(panelDerecho.getLayout());
        BorderPane.setMargin(panelIzquierdo.getLayout(), new Insets(0, 20, 0, 0));

        // Controles de fuente
        botonAumentarFuente = new Button();
        botonAumentarFuente.setOnAction(e -> cambiarFuente(1));
        botonDisminuirFuente = new Button();
        botonDisminuirFuente.setOnAction(e -> cambiarFuente(-1));

        HBox cajaControlFuente = new HBox(10, botonDisminuirFuente, botonAumentarFuente);
        cajaControlFuente.setAlignment(Pos.CENTER_RIGHT);

        // Panel inferior que combina navegación y controles de fuente
        BorderPane panelInferior = new BorderPane();
        panelInferior.setCenter(panelDerecho.getNavegacionLayout());
        panelInferior.setRight(cajaControlFuente);

        raiz.setBottom(panelInferior);

        Scene escena = new Scene(raiz);
        escenarioPrincipal.setScene(escena);
        escenarioPrincipal.setMaximized(true);

        actualizarEstiloFuente();
        inicializarComponentes();

        escenarioPrincipal.show();
    }

    private void inicializarComponentes() {
        if (gestor != null) {
            actualizarEstilosYTextos();
            Map<Integer, Parada> paradasMap = gestor.getMapaParadas();
            panelIzquierdo.cargarParadas(paradasMap);
        }
    }

    private void manejarCalculo(ConsultaRequest request) {
        panelDerecho.mostrarCargando(loadingView);
        panelIzquierdo.setBotonCalcularDeshabilitado(true);

        ConsultaTask task = new ConsultaTask(gestor, request);

        task.setOnSucceeded(event -> {
            List<List<String>> recorridosFormateados = task.getValue();
            Platform.runLater(() -> {
                panelDerecho.mostrarResultados(recorridosFormateados);
                panelIzquierdo.setBotonCalcularDeshabilitado(false);
            });
        });

        task.setOnFailed(event -> {
            Platform.runLater(() -> {
                panelDerecho.mostrarError("Error durante el cálculo.");
                panelIzquierdo.setBotonCalcularDeshabilitado(false);
            });
        });

        new Thread(task).start();
    }

    private void manejarVolver() {
        gestor.solicitarVolverAInicio(escenarioPrincipal);
    }

    public void setGestor(GestorDeVentanas gest) {
        this.gestor = gest;
    }

    private void actualizarEstilosYTextos() {
        ResourceBundle bundle = getBundle();
        if (bundle == null) return;

        escenarioPrincipal.setTitle(bundle.getString("Query_WindowName"));
        panelIzquierdo.actualizarTextos(bundle);
        panelDerecho.actualizarTextos(bundle);

        botonAumentarFuente.setText(bundle.getString("Query_FontPlus"));
        botonDisminuirFuente.setText(bundle.getString("Query_FontMinus"));

    }


    public void mostrar(Stage stage) {
        start(stage);
    }


    public void cerrar(Stage stage) {
        stage.close();
    }

    private void cambiarFuente(double delta) {
        tamanoFuenteActual += delta;
        tamanoFuenteActual = Math.max(TAMANO_FUENTE_BASE - MAX_INCREMENTOS, tamanoFuenteActual);
        tamanoFuenteActual = Math.min(TAMANO_FUENTE_BASE + MAX_INCREMENTOS, tamanoFuenteActual);
        actualizarEstiloFuente();
    }

    private void actualizarEstiloFuente() {
        if (raiz != null) {
            raiz.setStyle("-fx-font-size: " + tamanoFuenteActual + "pt;");
        }
    }

    /**
     * La UI ya no recibe objetos de negocio. El botón de "Mapa" recibe
     * una representación textual (multilínea) y la muestra en una ventana simple.
     */
    private void mostrarMapa(String recorridoTexto) {
        if (recorridoTexto == null || recorridoTexto.isEmpty()) return;

        Stage s = new Stage();
        BorderPane p = new BorderPane();
        javafx.scene.control.TextArea ta = new javafx.scene.control.TextArea(recorridoTexto);
        ta.setEditable(false);
        ta.setWrapText(true);
        p.setCenter(ta);
        Scene sc = new Scene(p, 600, 400);
        s.setScene(sc);
        s.setTitle(getBundle() != null ? getBundle().getString("Query_WindowName") : "Mapa");
        s.show();
    }

    ResourceBundle getBundle() {
        return gestor.getBundle();
    }
}