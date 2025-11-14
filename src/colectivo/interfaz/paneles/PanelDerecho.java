package colectivo.interfaz.paneles;

import colectivo.modelo.Parada;
import colectivo.modelo.Recorrido;
import colectivo.util.Tiempo;
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

import java.time.Duration;
import java.time.LocalTime;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Gestiona el panel derecho de la interfaz, que muestra los
 * resultados de la consulta de rutas, incluyendo paginación y detalles del viaje.
 */
public class PanelDerecho {

    private BorderPane layout;
    private VBox contenido;
    private Button botonAnterior, botonSiguiente, botonMapa;
    private Label etiquetaPagina, etiquetaAdvertencia;
    private HBox cajaNavegacion; // <-- Hacerlo un campo de clase

    private List<List<Recorrido>> rutasCompletas;
    private int indicePaginaActual;
    private LocalTime ultimaConsultaHoraLlegada;

    private Consumer<List<Recorrido>> onMostrarMapa;
    private Supplier<ResourceBundle> bundleSupplier;

    public PanelDerecho(Consumer<List<Recorrido>> onMostrarMapa, Supplier<ResourceBundle> bundleSupplier) {
        this.onMostrarMapa = onMostrarMapa;
        this.bundleSupplier = bundleSupplier;
        crearLayout();
    }

    private void crearLayout() {
        layout = new BorderPane();
        layout.setStyle("-fx-border-color: lightgray; -fx-border-width: 1; -fx-border-radius: 5;");

        contenido = new VBox(10);
        contenido.setPadding(new Insets(10));
        contenido.setAlignment(Pos.TOP_CENTER);
        
        etiquetaAdvertencia = new Label();
        etiquetaAdvertencia.setTextFill(Color.RED);
        etiquetaAdvertencia.setVisible(false);
        contenido.getChildren().add(etiquetaAdvertencia);

        ScrollPane panelScroll = new ScrollPane(contenido);
        panelScroll.setFitToWidth(true);
        panelScroll.setStyle("-fx-background-color: transparent;");
        layout.setCenter(panelScroll);

        botonAnterior = new Button();
        botonAnterior.setOnAction(e -> cambiarPagina(-1));
        botonSiguiente = new Button();
        botonSiguiente.setOnAction(e -> cambiarPagina(1));
        etiquetaPagina = new Label();
        botonMapa = new Button();
        botonMapa.setOnAction(e -> {
            if (rutasCompletas != null && !rutasCompletas.isEmpty()) {
                onMostrarMapa.accept(rutasCompletas.get(indicePaginaActual));
            }
        });

        // --- INICIO DEL CAMBIO ---
        cajaNavegacion = new HBox(10, botonAnterior, etiquetaPagina, botonSiguiente, botonMapa);
        cajaNavegacion.setAlignment(Pos.CENTER);
        cajaNavegacion.setPadding(new Insets(10));
        
        // Ya no se añade al 'bottom' del layout aquí. La clase Interfaz lo hará.
        // --- FIN DEL CAMBIO ---
        
        actualizarControlesNavegacion();
    }
    
    // --- INICIO DEL CAMBIO ---
    /**
     * Devuelve el layout que contiene los controles de navegación de resultados.
     * @return Un HBox con los botones de página y mapa.
     */
    public HBox getNavegacionLayout() {
        return cajaNavegacion;
    }
    // --- FIN DEL CAMBIO ---
    
    public void mostrarCargando(Node loadingView) {
        contenido.getChildren().clear();
        loadingView.setVisible(true);
        contenido.getChildren().add(loadingView);
    }
    
    public void mostrarError(String mensaje) {
        contenido.getChildren().clear();
        etiquetaAdvertencia.setText(mensaje);
        etiquetaAdvertencia.setVisible(true);
        contenido.getChildren().add(etiquetaAdvertencia);
    }

    public void mostrarResultados(List<List<Recorrido>> listaRecorridos, Parada pOrigen, LocalTime hLlegada) {
        this.rutasCompletas = listaRecorridos;
        this.ultimaConsultaHoraLlegada = hLlegada;
        this.indicePaginaActual = 0;
        
        contenido.getChildren().clear();
        
        if (rutasCompletas != null && !rutasCompletas.isEmpty()) {
            mostrarPaginaActual();
        } else {
            contenido.getChildren().add(new Label(bundleSupplier.get().getString("Result_ZeroRoutes")));
        }
        actualizarControlesNavegacion();
    }
    
    private void mostrarPaginaActual() {
        contenido.getChildren().clear();
        ResourceBundle bundle = bundleSupplier.get();
        List<Recorrido> recorridoCompleto = rutasCompletas.get(indicePaginaActual);
        
        Label titulo = new Label(bundle.getString("Result_RouteX") + " " + (indicePaginaActual + 1) + ":");
        titulo.setStyle("-fx-font-weight: bold;");
        contenido.getChildren().add(titulo);
        
        if (recorridoCompleto.size() > 1) {
            Label avisoTrasbordo = new Label(bundle.getString("Result_TransferWarning"));
            avisoTrasbordo.setTextFill(Color.DARKRED);
            avisoTrasbordo.setStyle("-fx-font-weight: bold;");
            contenido.getChildren().add(avisoTrasbordo);
        }
        
        LocalTime horaLlegaActual = ultimaConsultaHoraLlegada;

        for (int t = 0; t < recorridoCompleto.size(); t++) {
            Recorrido r = recorridoCompleto.get(t);
            VBox tramoBox = new VBox(5);
            tramoBox.setPadding(new Insets(10));
            tramoBox.setSpacing(5);

            List<Parada> paradasTramo = r.getParadas();
            Parada tramoOrigen = paradasTramo.get(0);
            Parada tramoDestino = paradasTramo.get(paradasTramo.size() - 1);
            int viajeSeg = r.getDuracion();
            LocalTime horaSalida = r.getHoraSalida();
            LocalTime horaLlegadaTramo = horaSalida.plusSeconds(viajeSeg);
            
            String tituloTramo;
            if (r.getLinea() != null) {
                tramoBox.setStyle("-fx-border-color: lightblue; -fx-border-width: 1; -fx-background-color: #f0f8ff; -fx-border-radius: 5;");
                long esperaSeg = Duration.between(horaLlegaActual, horaSalida).toSeconds();
                if (esperaSeg < 0) esperaSeg = 0;

                tituloTramo = bundle.getString("Result_SegmentX") + " " + (t + 1) + " - " + bundle.getString("Result_LineX") + " " + r.getLinea().getCodigo();
                tramoBox.getChildren().add(new Label(tituloTramo));
                tramoBox.getChildren().add(new Label("  " + bundle.getString("Result_InitialStop") + " " + tramoOrigen.getDireccion()));
                tramoBox.getChildren().add(new Label("  " + bundle.getString("Result_FinalStop") + " " + tramoDestino.getDireccion()));
                tramoBox.getChildren().add(new Label("  " + bundle.getString("Result_UserTimeOfArrival") + " " + horaLlegaActual));
                tramoBox.getChildren().add(new Label("  " + bundle.getString("Result_TimeOfDeparture") + " " + horaSalida));
                tramoBox.getChildren().add(new Label("  " + bundle.getString("Result_WaitTime") + " " + Tiempo.segundosATiempo((int) esperaSeg)));
                tramoBox.getChildren().add(new Label("  " + bundle.getString("Result_TravelTime") + " " + Tiempo.segundosATiempo(viajeSeg)));
                
                if (paradasTramo.size() > 1) {
                    Label tituloParadas = new Label("  " + bundle.getString("Result_Stops"));
                    tituloParadas.setStyle("-fx-font-weight: bold;");
                    tramoBox.getChildren().add(tituloParadas);
                    
                    VBox cajaTramosParadas = new VBox(2);
                    cajaTramosParadas.setPadding(new Insets(0, 0, 0, 35));
                    
                    for (int i = 0; i < paradasTramo.size() - 1; i++) {
                        Parada paradaActual = paradasTramo.get(i);
                        Parada paradaSiguiente = paradasTramo.get(i + 1);
                        Label tramoDeParada = new Label(paradaActual.getDireccion() + " -> " + paradaSiguiente.getDireccion());
                        cajaTramosParadas.getChildren().add(tramoDeParada);
                    }
                    tramoBox.getChildren().add(cajaTramosParadas);
                }

            } else {
                tramoBox.setStyle("-fx-border-color: lightgreen; -fx-border-width: 1; -fx-background-color: #f0fff0; -fx-border-radius: 5;");
                tituloTramo = bundle.getString("Result_SegmentX") + " " + (t + 1) + " - " + bundle.getString("Result_Walking");
                tramoBox.getChildren().add(new Label(tituloTramo));
                tramoBox.getChildren().add(new Label("  " + bundle.getString("Result_WalkFrom") + " " + tramoOrigen.getDireccion()));
                tramoBox.getChildren().add(new Label("  " + bundle.getString("Result_WalkTo") + " " + tramoDestino.getDireccion()));
                tramoBox.getChildren().add(new Label("  " + bundle.getString("Result_WalkStart") + " " + horaSalida));
                tramoBox.getChildren().add(new Label("  " + bundle.getString("Result_WalkDuration") + " " + Tiempo.segundosATiempo(viajeSeg)));
            }
            
            tramoBox.getChildren().add(new Label("  " + bundle.getString("Result_ArrivalTime") + " " + horaLlegadaTramo));
            contenido.getChildren().add(tramoBox);
            horaLlegaActual = horaLlegadaTramo;
        }

        actualizarControlesNavegacion();
    }

    public void actualizarTextos(ResourceBundle bundle) {
        botonAnterior.setText(bundle.getString("Result_PreviousButton"));
        botonSiguiente.setText(bundle.getString("Result_NextButton"));
        botonMapa.setText(bundle.getString("Query_MapButton"));
        if (rutasCompletas != null && !rutasCompletas.isEmpty()) {
            mostrarPaginaActual();
        }
        actualizarControlesNavegacion();
    }
    
    private void cambiarPagina(int direccion) {
        if (rutasCompletas != null && !rutasCompletas.isEmpty()) {
            int nuevaPagina = indicePaginaActual + direccion;
            if (nuevaPagina >= 0 && nuevaPagina < rutasCompletas.size()) {
                indicePaginaActual = nuevaPagina;
                mostrarPaginaActual();
            }
        }
    }
    
    private void actualizarControlesNavegacion() {
        ResourceBundle bundle = bundleSupplier.get();
        if (bundle == null) return;
        
        boolean hayRutas = rutasCompletas != null && !rutasCompletas.isEmpty();
        
        if (hayRutas) {
            etiquetaPagina.setText(bundle.getString("Result_RoutePages") + " " + (indicePaginaActual + 1) + " " + bundle.getString("Result_Of") + " " + rutasCompletas.size());
            botonAnterior.setDisable(indicePaginaActual <= 0);
            botonSiguiente.setDisable(indicePaginaActual >= rutasCompletas.size() - 1);
        } else {
            etiquetaPagina.setText(bundle.getString("Result_RoutePages") + " 0 " + bundle.getString("Result_Of") + " 0");
            botonAnterior.setDisable(true);
            botonSiguiente.setDisable(true);
        }
        botonMapa.setDisable(!hayRutas);
    }
    
    public BorderPane getLayout() {
        return layout;
    }
}