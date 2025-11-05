package colectivo.interfaz;


import java.time.Duration;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import colectivo.controlador.Coordinador;
import colectivo.modelo.Parada;
import colectivo.modelo.Recorrido;
import colectivo.util.Tiempo;
import javafx.scene.paint.Color;

/**
 * Clase principal de la interfaz de usuario para la aplicación de consulta de colectivos.
 * Gestiona la ventana, la entrada del usuario y la visualización de resultados.
 */
public class Interfaz extends Application implements VentanaConsultas {
    
    /** El coordinador que maneja la lógica de negocio. */
    private static Coordinador coordinador;
    /** Almacena la lista completa de rutas encontradas en la última consulta. */
    private static List<List<Recorrido>> rutasCompletas;
    /** Índice de la página de resultados que se está mostrando actualmente. */
    private static int indicePaginaActual;
    
    /** Almacena la parada de origen de la última consulta para paginación. */
    private static Parada ultimaConsultaParadaOrigen;
    /** Almacena la parada de destino de la última consulta para paginación. */
    private static Parada ultimaConsultaParadaDestino;
    /** Almacena la hora de llegada de la última consulta para paginación. */
    private static LocalTime ultimaConsultaHoraLlegada;

    private static ComboBox<Parada> comboOrigen;
    private static ComboBox<Parada> comboDestino;
    private static ComboBox<String> comboHora;
    private static ComboBox<String> comboMinuto;

    private static Label etiquetaAdvertencia;
    private static VBox panelDerechoContenido;
    private static ToggleGroup grupoDiasSemana;
    private static Button botonAnterior;
    private static Button botonSiguiente;
    private static Label etiquetaPagina;
    private static Button botonMapa; 
    private static ImageView loadingView;

    private static BorderPane raiz;
    private static double tamanoFuenteActual = 12;
    private static final double TAMANO_FUENTE_BASE = 12;
    private static final int MAX_INCREMENTOS = 5;

    // Campos para componentes de la UI que necesitan actualizar su texto
    private Label etiquetaOrigen;
    private Label etiquetaDestino;
    private Label etiquetaHora;
    private Label etiquetaDia;
    private RadioButton lun, mar, mie, jue, vie, sab, dom;
    private Button botonCalcular;
    private Button botonAumentarFuente, botonDisminuirFuente;
    private Button botonVolver;
    private Stage escenarioPrincipal;

    static class ParadaListCell extends ListCell<Parada> {
        @Override
        protected void updateItem(Parada item, boolean empty) {
            super.updateItem(item, empty);
            setText( (empty || item == null) ? null : item.getDireccion());
        }
    }

    @Override
    public void setCoordinador(Coordinador coord) {
        setCoordinadorFinal(coord);
    }

    private static void setCoordinadorFinal(Coordinador coord) {
        coordinador = coord;
    }
    
    private static void resetState() {
        rutasCompletas = null;
        indicePaginaActual = 0;
        ultimaConsultaParadaOrigen = null;
        ultimaConsultaParadaDestino = null;
        ultimaConsultaHoraLlegada = null;

        if (comboOrigen != null) comboOrigen.setValue(null);
        if (comboDestino != null) comboDestino.setValue(null);
        if (comboHora != null) comboHora.setValue(null);
        if (comboMinuto != null) comboMinuto.setValue(null);
        if (grupoDiasSemana != null && grupoDiasSemana.getSelectedToggle() != null) {
            grupoDiasSemana.getSelectedToggle().setSelected(false);
        }
        if (panelDerechoContenido != null) {
            panelDerechoContenido.getChildren().clear();
        }
        if (etiquetaAdvertencia != null) {
            etiquetaAdvertencia.setVisible(false);
        }
    }


    private void actualizarTextos() {
        ResourceBundle bundle = coordinador.getBundle();
        if (bundle == null) return; 

        escenarioPrincipal.setTitle(bundle.getString("Query_WindowName"));
        etiquetaOrigen.setText(bundle.getString("Query_InitialStopQuestion"));
        etiquetaDestino.setText(bundle.getString("Query_FinalStopQuestion"));
        etiquetaHora.setText(bundle.getString("Query_TimeQuestion"));
        etiquetaDia.setText(bundle.getString("Query_DayOfWeekQuestion"));
        
        lun.setText(bundle.getString("Query_Monday"));
        mar.setText(bundle.getString("Query_Tuesday"));
        mie.setText(bundle.getString("Query_Wednesday"));
        jue.setText(bundle.getString("Query_Thursday"));
        vie.setText(bundle.getString("Query_Friday"));
        sab.setText(bundle.getString("Query_Saturday"));
        dom.setText(bundle.getString("Query_Sunday"));

        botonCalcular.setText(bundle.getString("Query_QueryButton"));
        botonVolver.setText(bundle.getString("Query_BackButton"));
        botonAnterior.setText(bundle.getString("Result_PreviousButton"));
        botonSiguiente.setText(bundle.getString("Result_NextButton"));
        botonMapa.setText(bundle.getString("Query_MapButton"));
        botonAumentarFuente.setText(bundle.getString("Query_FontPlus"));
        botonDisminuirFuente.setText(bundle.getString("Query_FontMinus"));
        
        comboOrigen.setPromptText(bundle.getString("Query_OriginPrompt"));
        comboDestino.setPromptText(bundle.getString("Query_DestinationPrompt"));

        if (rutasCompletas != null) {
            mostrarPaginaActual(ultimaConsultaParadaOrigen, ultimaConsultaParadaDestino, ultimaConsultaHoraLlegada);
        } else {
            actualizarControlesNavegacion();
        }
    }

    @Override
    public void start(Stage escenarioPrincipal) {
        resetState();
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
            System.err.println("Error al cargar el GIF de carga: /loading.gif no encontrado. Asegúrate de que el archivo exista en la carpeta 'resources'.");
            loadingView = new ImageView();
        }

        VBox panelIzquierdo = new VBox(10);
        panelIzquierdo.setAlignment(Pos.CENTER_LEFT);

        comboOrigen = new ComboBox<>();
        comboDestino = new ComboBox<>();
        comboHora = new ComboBox<>();
        comboMinuto = new ComboBox<>();

        ObservableList<String> horas = FXCollections.observableArrayList();
        for (int i = 0; i < 24; i++) horas.add(String.format("%02d", i));
        comboHora.setItems(horas);

        ObservableList<String> minutos = FXCollections.observableArrayList();
        for (int i = 0; i < 60; i++) minutos.add(String.format("%02d", i));
        comboMinuto.setItems(minutos);
        
        HBox cajaHora = new HBox(5, comboHora, new Label(":"), comboMinuto);
        cajaHora.setAlignment(Pos.CENTER_LEFT);
        
        if (coordinador != null) {
            Map<Integer, Parada> paradasMap = coordinador.getParadas();
            ObservableList<Parada> paradasLista = FXCollections.observableArrayList(paradasMap.values());
            
            comboOrigen.setItems(paradasLista);
            comboDestino.setItems(paradasLista);

            comboOrigen.setCellFactory(param -> new ParadaListCell());
            comboOrigen.setButtonCell(new ParadaListCell());
            comboDestino.setCellFactory(param -> new ParadaListCell());
            comboDestino.setButtonCell(new ParadaListCell());
        }

        grupoDiasSemana = new ToggleGroup();
        etiquetaAdvertencia = new Label();
        etiquetaAdvertencia.setTextFill(Color.RED);
        etiquetaAdvertencia.setVisible(false);
        
        panelDerechoContenido = new VBox(10);
        panelDerechoContenido.setPadding(new Insets(10));
        panelDerechoContenido.setAlignment(Pos.TOP_CENTER);

        etiquetaOrigen = new Label();
        etiquetaDestino = new Label();
        etiquetaHora = new Label();
        etiquetaDia = new Label();
        
        lun = new RadioButton(); lun.setToggleGroup(grupoDiasSemana);
        mar = new RadioButton(); mar.setToggleGroup(grupoDiasSemana);
        mie = new RadioButton(); mie.setToggleGroup(grupoDiasSemana);
        jue = new RadioButton(); jue.setToggleGroup(grupoDiasSemana);
        vie = new RadioButton(); vie.setToggleGroup(grupoDiasSemana);
        sab = new RadioButton(); sab.setToggleGroup(grupoDiasSemana);
        dom = new RadioButton(); dom.setToggleGroup(grupoDiasSemana);
        VBox cajaDias = new VBox(10, lun, mar, mie, jue, vie, sab, dom);

        botonCalcular = new Button();
        botonCalcular.setOnAction(event -> manejarCalculo());
        
        botonVolver = new Button();
        botonVolver.setOnAction(e -> coordinador.volverAInicio(escenarioPrincipal));
        
        panelIzquierdo.getChildren().addAll(
            etiquetaOrigen, comboOrigen,
            etiquetaDestino, comboDestino,
            etiquetaHora, cajaHora,
            etiquetaDia, cajaDias,
            botonCalcular,
            botonVolver,
            etiquetaAdvertencia
        );

        BorderPane panelDerechoLayout = new BorderPane();
        panelDerechoLayout.setStyle("-fx-border-color: lightgray; -fx-border-width: 1; -fx-border-radius: 5;");
        
        ScrollPane panelScroll = new ScrollPane(panelDerechoContenido);
        panelScroll.setFitToWidth(true);
        panelScroll.setStyle("-fx-background-color: transparent;");
        panelDerechoLayout.setCenter(panelScroll);
        
        botonAnterior = new Button();
        botonAnterior.setOnAction(e -> cambiarPagina(-1));
        botonSiguiente = new Button();
        botonSiguiente.setOnAction(e -> cambiarPagina(1));
        etiquetaPagina = new Label();
        botonMapa = new Button();
        botonMapa.setOnAction(e -> mostrarMapa());

        HBox cajaNavegacion = new HBox(10, botonAnterior, etiquetaPagina, botonSiguiente, botonMapa);
        cajaNavegacion.setAlignment(Pos.CENTER);
        cajaNavegacion.setPadding(new Insets(10));
        panelDerechoLayout.setBottom(cajaNavegacion);

        botonAumentarFuente = new Button();
        botonAumentarFuente.setOnAction(e -> cambiarFuente(1));
        botonDisminuirFuente = new Button();
        botonDisminuirFuente.setOnAction(e -> cambiarFuente(-1));
        
        HBox cajaControlFuente = new HBox(10, botonDisminuirFuente, botonAumentarFuente);
        cajaControlFuente.setAlignment(Pos.CENTER_RIGHT);
        
        VBox panelInferior = new VBox(10, panelDerechoLayout.getBottom(), cajaControlFuente);
        raiz.setBottom(panelInferior);

        raiz.setLeft(panelIzquierdo);
        raiz.setCenter(panelDerechoLayout);
        BorderPane.setMargin(panelIzquierdo, new Insets(0, 20, 0, 0));

        Scene escena = new Scene(raiz);
        escenarioPrincipal.setScene(escena);
        
        // --- INICIO DEL CAMBIO ---
        // Se establece la ventana en modo maximizado
        escenarioPrincipal.setMaximized(true);
        // --- FIN DEL CAMBIO ---
        
        actualizarEstiloFuente();
        actualizarTextos();
        actualizarControlesNavegacion();
        
        escenarioPrincipal.show();
    }
    
    private void manejarCalculo() {
        ResourceBundle bundle = coordinador.getBundle();
        etiquetaAdvertencia.setVisible(false);
        
        Parada paradaOrigen = comboOrigen.getValue();
        Parada paradaDestino = comboDestino.getValue();
        String horaSel = comboHora.getValue();
        String minSel = comboMinuto.getValue();
        RadioButton diaRadio = (RadioButton) grupoDiasSemana.getSelectedToggle();

        if (paradaOrigen == null || paradaDestino == null || horaSel == null || minSel == null || diaRadio == null) {
            etiquetaAdvertencia.setText(bundle.getString("Query_MissingInputError"));
            etiquetaAdvertencia.setVisible(true);
            return;
        }
        
        panelDerechoContenido.getChildren().clear();
        loadingView.setVisible(true);
        panelDerechoContenido.getChildren().add(loadingView);
        botonCalcular.setDisable(true);

        Task<List<List<Recorrido>>> task = new Task<>() {
            @Override
            protected List<List<Recorrido>> call() throws Exception {
                Thread.sleep(6000);

                String diaTexto = diaRadio.getText();
                int diaInt = 0;
                if (diaTexto.equals(bundle.getString("Query_Monday"))) diaInt = 1;
                else if (diaTexto.equals(bundle.getString("Query_Tuesday"))) diaInt = 2;
                else if (diaTexto.equals(bundle.getString("Query_Wednesday"))) diaInt = 3;
                else if (diaTexto.equals(bundle.getString("Query_Thursday"))) diaInt = 4;
                else if (diaTexto.equals(bundle.getString("Query_Friday"))) diaInt = 5;
                else if (diaTexto.equals(bundle.getString("Query_Saturday"))) diaInt = 6;
                else if (diaTexto.equals(bundle.getString("Query_Sunday"))) diaInt = 7;

                LocalTime hora = LocalTime.parse(horaSel + ":" + minSel);
                
                return coordinador.consulta(paradaOrigen, paradaDestino, diaInt, hora);
            }
        };

        task.setOnSucceeded(event -> {
            loadingView.setVisible(false);
            List<List<Recorrido>> recorridosEncontrados = task.getValue();
            ultimaConsultaParadaOrigen = paradaOrigen;
            ultimaConsultaParadaDestino = paradaDestino;
            ultimaConsultaHoraLlegada = LocalTime.parse(horaSel + ":" + minSel);
            resultado(recorridosEncontrados, paradaOrigen, paradaDestino, ultimaConsultaHoraLlegada);
            botonCalcular.setDisable(false);
        });

        task.setOnFailed(event -> {
            loadingView.setVisible(false);
            etiquetaAdvertencia.setText("Error durante el cálculo.");
            etiquetaAdvertencia.setVisible(true);
            botonCalcular.setDisable(false);
        });
        
        new Thread(task).start();
    }


    @Override
    public void resultado(List<List<Recorrido>> listaRecorridos, Parada pOrigen, Parada pDestino, LocalTime hLlegada) {
        rutasCompletas = listaRecorridos;
        ResourceBundle bundle = coordinador.getBundle();

        if (rutasCompletas != null && !rutasCompletas.isEmpty()) {
            indicePaginaActual = 0;
            mostrarPaginaActual(pOrigen, pDestino, hLlegada);
        } else {
            panelDerechoContenido.getChildren().clear();
            panelDerechoContenido.getChildren().add(new Label(bundle.getString("Result_ZeroRoutes")));
            actualizarControlesNavegacion();
        }
    }

    private static void mostrarPaginaActual(Parada paradaOrigen, Parada paradaDestino, LocalTime horaLlegaParada) {
        panelDerechoContenido.getChildren().clear();
        ResourceBundle bundle = coordinador.getBundle();
        
        List<Recorrido> recorridoCompleto = rutasCompletas.get(indicePaginaActual);
        
        Label titulo = new Label(bundle.getString("Result_RouteX") + " " + (indicePaginaActual + 1) + ":");
        titulo.setStyle("-fx-font-weight: bold;");
        panelDerechoContenido.getChildren().add(titulo);
        
        if (recorridoCompleto.size() > 1) {
            Label avisoTrasbordo = new Label(bundle.getString("Result_TransferWarning"));
            avisoTrasbordo.setTextFill(Color.DARKRED);
            avisoTrasbordo.setStyle("-fx-font-weight: bold;");
            panelDerechoContenido.getChildren().add(avisoTrasbordo);
        }
        
        LocalTime horaLlegaActual = horaLlegaParada;

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
            panelDerechoContenido.getChildren().add(tramoBox);
            horaLlegaActual = horaLlegadaTramo;
        }
        actualizarControlesNavegacion();
    }

    private static void actualizarControlesNavegacion() {
        ResourceBundle bundle = coordinador.getBundle();
        if (bundle == null) return;
        String de = bundle.getString("Result_Of");
        
        boolean hayRutas = rutasCompletas != null && !rutasCompletas.isEmpty();
        
        if (hayRutas) {
            etiquetaPagina.setText(bundle.getString("Result_RoutePages") + " " + (indicePaginaActual + 1) + " " + de + " " + rutasCompletas.size());
            botonAnterior.setDisable(indicePaginaActual <= 0);
            botonSiguiente.setDisable(indicePaginaActual >= rutasCompletas.size() - 1);
        } else {
            etiquetaPagina.setText(bundle.getString("Result_RoutePages") + " 0 " + de + " 0");
            botonAnterior.setDisable(true);
            botonSiguiente.setDisable(true);
        }
        botonMapa.setDisable(!hayRutas);
    }

    private static void cambiarPagina(int direccion) {
        if (rutasCompletas != null) {
            indicePaginaActual += direccion;
            mostrarPaginaActual(ultimaConsultaParadaOrigen, ultimaConsultaParadaDestino, ultimaConsultaHoraLlegada);
        }
    }
    
    private static void cambiarFuente(double delta) {
        tamanoFuenteActual += delta;
        tamanoFuenteActual = Math.max(TAMANO_FUENTE_BASE - MAX_INCREMENTOS, tamanoFuenteActual);
        tamanoFuenteActual = Math.min(TAMANO_FUENTE_BASE + MAX_INCREMENTOS, tamanoFuenteActual);
        actualizarEstiloFuente();
    }

    private static void actualizarEstiloFuente() {
        if (raiz != null) {
            raiz.setStyle("-fx-font-size: " + tamanoFuenteActual + "pt;");
        }
    }
    
    private static void mostrarMapa() {
        if (rutasCompletas == null || rutasCompletas.isEmpty()) return;
        
        VentanaMapa ventanaMapa = new VentanaMapa();
        ventanaMapa.setRecorrido(rutasCompletas.get(indicePaginaActual));
        
        try {
            ventanaMapa.start(new Stage());
        } catch (Exception e) {
            e.printStackTrace();
            etiquetaAdvertencia.setText("Error al abrir el mapa.");
            etiquetaAdvertencia.setVisible(true);
        }
    }

    @Override
    public void close(Stage ventana) {
        resetState();
        ventana.close();
    }
}