package colectivo.interfaz.javafx;

import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

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

/**
 * Interfaz es la ventana principal para la consulta de recorridos.
 * Ensambla y coordina los paneles de consulta (izquierda) y resultados (derecha).
 * La UI únicamente recibe textos ya formateados y metadatos simples (ids, nombres).
 */
public class Interfaz extends Application {

    /** Gestor de ventanas para la comunicación con la lógica de negocio. */
    private GestorDeVentanas gestor;

    /** Escenario principal de la aplicación. */
    private Stage escenarioPrincipal;

    /** Layout principal de la ventana. */
    private BorderPane raiz;

    /** Panel izquierdo para la consulta de datos. */
    private PanelIzquierdo panelIzquierdo;

    /** Panel derecho para mostrar resultados. */
    private PanelDerecho panelDerecho;

    /** Vista de carga (GIF). */
    private ImageView loadingView;

    /** Botón para aumentar el tamaño de fuente. */
    private Button botonAumentarFuente;

    /** Botón para disminuir el tamaño de fuente. */
    private Button botonDisminuirFuente;

    /** Tamaño actual de la fuente. */
    private double tamanoFuenteActual = 12;

    /** Tamaño base de la fuente. */
    private static final double TAMANO_FUENTE_BASE = 12;

    /** Máximo de incrementos permitidos para el tamaño de fuente. */
    private static final int MAX_INCREMENTOS = 5;

    /**
     * Inicializa y muestra la ventana principal de la aplicación.
     * @param escenarioPrincipal Escenario principal de JavaFX.
     */
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

    /**
     * Inicializa los componentes de la interfaz y carga las paradas.
     */
    private void inicializarComponentes() {
        if (gestor != null) {
            actualizarEstilosYTextos();
            Map<Integer, String> paradasMap = gestor.getMapaParadasNombres();
            panelIzquierdo.cargarParadas(paradasMap);
        }
    }

    /**
     * Maneja la solicitud de cálculo de recorridos.
     * @param request Solicitud de consulta realizada por el usuario.
     */
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

    /**
     * Maneja la acción de volver a la ventana de inicio.
     */
    private void manejarVolver() {
        gestor.solicitarVolverAInicio(escenarioPrincipal);
    }

    /**
     * Establece el gestor de ventanas para la interfaz.
     * @param gest Gestor de ventanas.
     */
    public void setGestor(GestorDeVentanas gest) {
        this.gestor = gest;
    }

    /**
     * Actualiza los estilos y textos de la interfaz según el ResourceBundle.
     */
    private void actualizarEstilosYTextos() {
        ResourceBundle bundle = getBundle();
        if (bundle == null) return;

        escenarioPrincipal.setTitle(bundle.getString("Query_WindowName"));
        panelIzquierdo.actualizarTextos(bundle);
        panelDerecho.actualizarTextos(bundle);

        botonAumentarFuente.setText(bundle.getString("Query_FontPlus"));
        botonDisminuirFuente.setText(bundle.getString("Query_FontMinus"));

    }

    /**
     * Muestra la ventana principal en el escenario dado.
     * @param stage Escenario de JavaFX.
     */
    public void mostrar(Stage stage) {
        start(stage);
    }

    /**
     * Cierra la ventana principal.
     * @param stage Escenario de JavaFX a cerrar.
     */
    public void cerrar(Stage stage) {
        stage.close();
    }

    /**
     * Cambia el tamaño de la fuente de la interfaz.
     * @param delta Incremento o decremento del tamaño de fuente.
     */
    private void cambiarFuente(double delta) {
        tamanoFuenteActual += delta;
        tamanoFuenteActual = Math.max(TAMANO_FUENTE_BASE - MAX_INCREMENTOS, tamanoFuenteActual);
        tamanoFuenteActual = Math.min(TAMANO_FUENTE_BASE + MAX_INCREMENTOS, tamanoFuenteActual);
        actualizarEstiloFuente();
    }

    /**
     * Actualiza el estilo de fuente en el layout principal.
     */
    private void actualizarEstiloFuente() {
        if (raiz != null) {
            raiz.setStyle("-fx-font-size: " + tamanoFuenteActual + "pt;");
        }
    }

    /**
     * Muestra la ventana de mapa para el recorrido especificado.
     * @param recorrido Índice del recorrido a mostrar en el mapa.
     */
    private void mostrarMapa(int recorrido) {
        if (gestor != null) {
            gestor.mostrarVentanaMapa(recorrido);
        } else {
            panelDerecho.mostrarError("Error: Gestor no disponible para abrir mapa.");
        }
    }

    /**
     * Devuelve el ResourceBundle actual para internacionalización.
     * @return ResourceBundle actual.
     */
    ResourceBundle getBundle() {
        return gestor.getBundle();
    }
}