package colectivo.interfaz.javafx.paneles;

import colectivo.util.FormateadorRecorridos;
import colectivo.util.FormateadorRecorridos.LineaSimple;
import colectivo.util.FormateadorRecorridos.PaginaEstructurada;
import colectivo.util.FormateadorRecorridos.SegmentoFormateado;
import colectivo.util.FormateadorRecorridos.TipoTramo;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * PanelDerecho es el panel lateral derecho de la interfaz de usuario.
 * Se encarga de mostrar los resultados de las consultas, mensajes de error,
 * y controles de navegación entre páginas de resultados.
 * El formateo y parsing de las líneas se delega a FormateadorRecorridos.
 */
public class PanelDerecho {

    /**
     * Layout principal del panel derecho.
     */
    private BorderPane layout;

    /**
     * Contenedor de los elementos visuales del panel.
     */
    private VBox contenido;

    /**
     * Botón para navegar a la página anterior.
     */
    private Button botonAnterior;

    /**
     * Botón para navegar a la página siguiente.
     */
    private Button botonSiguiente;

    /**
     * Botón para mostrar el mapa de la ruta actual.
     */
    private Button botonMapa;

    /**
     * Etiqueta que muestra la página actual.
     */
    private Label etiquetaPagina;

    /**
     * Etiqueta para mostrar mensajes de advertencia o error.
     */
    private Label etiquetaAdvertencia;

    /**
     * Caja de navegación que contiene los botones y la etiqueta de página.
     */
    private HBox cajaNavegacion;

    /**
     * Lista de páginas con los resultados formateados.
     */
    private List<List<String>> paginas;

    /**
     * Índice de la página actualmente mostrada.
     */
    private int indicePaginaActual;

    /**
     * Acción a ejecutar al mostrar el mapa.
     */
    private Consumer<Integer> onMostrarMapa;

    /**
     * Proveedor de ResourceBundle para internacionalización.
     */
    private Supplier<ResourceBundle> bundleSupplier;

    /**
     * Constructor del PanelDerecho.
     * @param onMostrarMapa Acción a ejecutar al mostrar el mapa.
     * @param bundleSupplier Proveedor de ResourceBundle para textos.
     */
    public PanelDerecho(Consumer<Integer> onMostrarMapa, Supplier<ResourceBundle> bundleSupplier) {
        this.onMostrarMapa = onMostrarMapa;
        this.bundleSupplier = bundleSupplier;
        crearLayout();
    }

    /**
     * Inicializa el layout y los controles del panel derecho.
     */
    private void crearLayout() {
        layout = new BorderPane();
        layout.setStyle("-fx-border-color: lightgray; -fx-border-width: 1; -fx-border-radius: 5;");

        contenido = new VBox(10);
        contenido.setPadding(new Insets(10));
        contenido.setAlignment(Pos.TOP_LEFT);

        etiquetaAdvertencia = new Label();
        etiquetaAdvertencia.setTextFill(Color.RED);
        etiquetaAdvertencia.setVisible(false);
        contenido.getChildren().add(etiquetaAdvertencia);

        ScrollPane panelScroll = new ScrollPane(contenido);
        panelScroll.setFitToWidth(true);
        panelScroll.setFitToHeight(true);
        panelScroll.setStyle("-fx-background-color: transparent;");
        layout.setCenter(panelScroll);

        botonAnterior = new Button();
        botonAnterior.setOnAction(e -> cambiarPagina(-1));
        botonSiguiente = new Button();
        botonSiguiente.setOnAction(e -> cambiarPagina(1));
        etiquetaPagina = new Label();
        botonMapa = new Button();
        botonMapa.setOnAction(e -> {
            if (paginas != null && !paginas.isEmpty()) {
                onMostrarMapa.accept(indicePaginaActual); 
            }
        });
        cajaNavegacion = new HBox(10, botonAnterior, etiquetaPagina, botonSiguiente, botonMapa);
        cajaNavegacion.setAlignment(Pos.CENTER);
        cajaNavegacion.setPadding(new Insets(10));

        actualizarControlesNavegacion();
    }

    /**
     * Devuelve el layout de navegación para ser colocado por la clase superior.
     * @return HBox con los controles de navegación.
     */
    public HBox getNavegacionLayout() {
        return cajaNavegacion;
    }

    /**
     * Muestra una vista de carga en el panel derecho.
     * @param loadingView Nodo visual de carga (por ejemplo, un GIF).
     */
    public void mostrarCargando(Node loadingView) {
        contenido.getChildren().clear();
        contenido.setAlignment(Pos.CENTER);
        loadingView.setVisible(true);
        contenido.getChildren().add(loadingView);
    }

    /**
     * Muestra un mensaje de error en el panel derecho.
     * @param mensaje Texto del mensaje de error.
     */
    public void mostrarError(String mensaje) {
        contenido.getChildren().clear();
        contenido.setAlignment(Pos.TOP_LEFT);
        etiquetaAdvertencia.setText(mensaje);
        etiquetaAdvertencia.setVisible(true);
        contenido.getChildren().add(etiquetaAdvertencia);
    }

    /**
     * Muestra los resultados formateados en el panel derecho.
     * @param paginasFormateadas Lista de páginas con resultados formateados.
     */
    public void mostrarResultados(List<List<String>> paginasFormateadas) {
        this.paginas = paginasFormateadas;
        this.indicePaginaActual = 0;
        contenido.getChildren().clear();
        contenido.setAlignment(Pos.TOP_LEFT);

        if (paginas != null && !paginas.isEmpty()) {
            mostrarPaginaActual();
        } else {
            ResourceBundle bundle = bundleSupplier.get();
            contenido.getChildren().add(new Label(bundle.getString("Result_ZeroRoutes")));
        }
        actualizarControlesNavegacion();
    }

    /**
     * Muestra la página actual de resultados en el panel derecho.
     */
    private void mostrarPaginaActual() {
        contenido.getChildren().clear();
        contenido.setAlignment(Pos.TOP_LEFT);
        ResourceBundle bundle = bundleSupplier.get();
        List<String> lineas = paginas.get(indicePaginaActual);

        PaginaEstructurada parsed = FormateadorRecorridos.parsearPagina(lineas, bundle);

        for (FormateadorRecorridos.ItemPagina item : parsed.items) {
            if (item instanceof LineaSimple) {
                LineaSimple ls = (LineaSimple) item;
                Label l = new Label(ls.texto);
                if (ls.advertencia) {
                    l.setTextFill(Color.RED);
                }
                contenido.getChildren().add(l);
            } else if (item instanceof SegmentoFormateado) {
                SegmentoFormateado seg = (SegmentoFormateado) item;

                VBox segmentoBox = new VBox(4);
                segmentoBox.setPadding(new Insets(8));

                if (seg.tipo == TipoTramo.CAMINANDO) {
                    segmentoBox.setStyle(
                        "-fx-background-color: #E8F5E9; " +
                        "-fx-background-radius: 6; " +
                        "-fx-border-color: #43A047; " +
                        "-fx-border-width: 2; " +
                        "-fx-border-radius: 6;"
                    );
                } else if (seg.tipo == TipoTramo.COLECTIVO) {
                    segmentoBox.setStyle(
                        "-fx-background-color: #E6F0FF; " +
                        "-fx-background-radius: 6; " +
                        "-fx-border-color: #1E88E5; " +
                        "-fx-border-width: 2; " +
                        "-fx-border-radius: 6;"
                    );
                } else {
                    segmentoBox.setStyle(
                        "-fx-background-color: #F5F5F5; " +
                        "-fx-background-radius: 6; " +
                        "-fx-border-color: #BDBDBD; " +
                        "-fx-border-width: 2; " +
                        "-fx-border-radius: 6;"
                    );
                }

                segmentoBox.getChildren().add(new Label(seg.encabezado));
                for (String ln : seg.lineas) {
                    segmentoBox.getChildren().add(new Label(ln));
                }

                contenido.getChildren().add(segmentoBox);
                VBox.setMargin(segmentoBox, new Insets(6, 0, 6, 0));
            }
        }

        actualizarControlesNavegacion();
    }

    /**
     * Actualiza los textos de los controles de navegación según el ResourceBundle.
     * @param bundle ResourceBundle con los textos traducidos.
     */
    public void actualizarTextos(ResourceBundle bundle) {
        botonAnterior.setText(bundle.getString("Result_PreviousButton"));
        botonSiguiente.setText(bundle.getString("Result_NextButton"));
        botonMapa.setText(bundle.getString("Query_MapButton"));
        if (paginas != null && !paginas.isEmpty()) {
            mostrarPaginaActual();
        }
        actualizarControlesNavegacion();
    }

    /**
     * Cambia la página actual según la dirección indicada.
     * @param direccion Valor entero: -1 para anterior, 1 para siguiente.
     */
    private void cambiarPagina(int direccion) {
        if (paginas != null && !paginas.isEmpty()) {
            int nuevaPagina = indicePaginaActual + direccion;
            if (nuevaPagina >= 0 && nuevaPagina < paginas.size()) {
                indicePaginaActual = nuevaPagina;
                mostrarPaginaActual();
            }
        }
    }

    /**
     * Actualiza el estado de los controles de navegación según la página actual.
     */
    private void actualizarControlesNavegacion() {
        ResourceBundle bundle = bundleSupplier.get();
        if (bundle == null) return;

        boolean hayRutas = paginas != null && !paginas.isEmpty();

        if (hayRutas) {
            etiquetaPagina.setText(bundle.getString("Result_RoutePages") + " " + (indicePaginaActual + 1) + " " + bundle.getString("Result_Of") + " " + paginas.size());
            botonAnterior.setDisable(indicePaginaActual <= 0);
            botonSiguiente.setDisable(indicePaginaActual >= paginas.size() - 1);
        } else {
            etiquetaPagina.setText(bundle.getString("Result_RoutePages") + " 0 " + bundle.getString("Result_Of") + " 0");
            botonAnterior.setDisable(true);
            botonSiguiente.setDisable(true);
        }
        botonMapa.setDisable(!hayRutas);
    }

    /**
     * Devuelve el layout principal del panel derecho.
     * @return BorderPane del panel derecho.
     */
    public BorderPane getLayout() {
        return layout;
    }
}