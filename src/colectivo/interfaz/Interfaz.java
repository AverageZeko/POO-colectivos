package colectivo.interfaz;


import java.time.Duration;
import java.time.LocalTime;
import java.util.List;
import java.util.ResourceBundle;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
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

    private static TextField campoOrigen;
    private static TextField campoDestino;
    private static TextField campoHora;
    private static Label etiquetaAdvertencia;
    private static VBox panelDerechoContenido;
    private static ToggleGroup grupoDiasSemana;
    private static Button botonAnterior;
    private static Button botonSiguiente;
    private static Label etiquetaPagina;

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
    private Stage escenarioPrincipal;

    @Override
    public void setCoordinador(Coordinador coord) {
        setCoordinadorFinal(coord);
    }

    /**
     * Establece el coordinador para ser utilizado por la interfaz.
     * @param coord El coordinador a inyectar.
     */
    private static void setCoordinadorFinal(Coordinador coord) {
        coordinador = coord;
    }

    /**
     * Carga o recarga los textos de la interfaz desde el ResourceBundle del coordinador.
     */
    private void actualizarTextos() {
        ResourceBundle bundle = coordinador.getBundle();
        if (bundle == null) return; // Salir si no hay bundle

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
        botonAnterior.setText(bundle.getString("Result_PreviousButton"));
        botonSiguiente.setText(bundle.getString("Result_NextButton"));
        botonAumentarFuente.setText(bundle.getString("Query_FontPlus"));
        botonDisminuirFuente.setText(bundle.getString("Query_FontMinus"));

        // Actualizar la vista de resultados si ya existe
        if (rutasCompletas != null) {
            mostrarPaginaActual(ultimaConsultaParadaOrigen, ultimaConsultaParadaDestino, ultimaConsultaHoraLlegada);
        } else {
            actualizarControlesNavegacion();
        }
    }

    /**
     * Método principal de JavaFX, construye y muestra la interfaz gráfica.
     * @param escenarioPrincipal El escenario principal proporcionado por JavaFX.
     */
    @Override
    public void start(Stage escenarioPrincipal) {
        this.escenarioPrincipal = escenarioPrincipal;
        raiz = new BorderPane();
        raiz.setPadding(new Insets(30));

        VBox panelIzquierdo = new VBox(10);
        panelIzquierdo.setAlignment(Pos.CENTER_LEFT);

        campoOrigen = new TextField();
        campoOrigen.setPromptText("Ej: 101");
        campoDestino = new TextField();
        campoDestino.setPromptText("Ej: 202");
        campoHora = new TextField();
        campoHora.setPromptText("HH:mm, Ej: 14:30");
        grupoDiasSemana = new ToggleGroup();
        etiquetaAdvertencia = new Label();
        etiquetaAdvertencia.setTextFill(Color.RED);
        etiquetaAdvertencia.setVisible(false);
        panelDerechoContenido = new VBox(10);
        panelDerechoContenido.setPadding(new Insets(10));

        etiquetaOrigen = new Label();
        etiquetaDestino = new Label();
        etiquetaHora = new Label();
        etiquetaDia = new Label();
        
        lun = new RadioButton();
        lun.setToggleGroup(grupoDiasSemana);
        mar = new RadioButton();
        mar.setToggleGroup(grupoDiasSemana);
        mie = new RadioButton();
        mie.setToggleGroup(grupoDiasSemana);
        jue = new RadioButton();
        jue.setToggleGroup(grupoDiasSemana);
        vie = new RadioButton();
        vie.setToggleGroup(grupoDiasSemana);
        sab = new RadioButton();
        sab.setToggleGroup(grupoDiasSemana);
        dom = new RadioButton();
        dom.setToggleGroup(grupoDiasSemana);
        VBox cajaDias = new VBox(10, lun, mar, mie, jue, vie, sab, dom);

        botonCalcular = new Button();
        botonCalcular.setOnAction(event -> manejarCalculo());

        panelIzquierdo.getChildren().addAll(
            etiquetaOrigen, campoOrigen,
            etiquetaDestino, campoDestino,
            etiquetaHora, campoHora,
            etiquetaDia, cajaDias,
            botonCalcular,
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
        
        HBox cajaNavegacion = new HBox(10, botonAnterior, etiquetaPagina, botonSiguiente);
        cajaNavegacion.setAlignment(Pos.CENTER);
        cajaNavegacion.setPadding(new Insets(10));
        panelDerechoLayout.setBottom(cajaNavegacion);
        actualizarControlesNavegacion();

        botonAumentarFuente = new Button();
        botonAumentarFuente.setOnAction(e -> cambiarFuente(1));
        botonDisminuirFuente = new Button();
        botonDisminuirFuente.setOnAction(e -> cambiarFuente(-1));
        
        HBox cajaControlFuente = new HBox(10, botonDisminuirFuente, botonAumentarFuente);
        cajaControlFuente.setAlignment(Pos.CENTER_RIGHT);
        cajaControlFuente.setPadding(new Insets(10, 0, 0, 0));

        raiz.setLeft(panelIzquierdo);
        raiz.setCenter(panelDerechoLayout);
        raiz.setBottom(cajaControlFuente);
        BorderPane.setAlignment(cajaControlFuente, Pos.BOTTOM_RIGHT);
        BorderPane.setMargin(panelIzquierdo, new Insets(0, 20, 0, 0));

        Rectangle2D limitesPantalla = Screen.getPrimary().getVisualBounds();
        double anchoEscena = limitesPantalla.getWidth() * 0.6;
        double altoEscena = limitesPantalla.getHeight() * 0.75;
        
        panelIzquierdo.setPrefWidth(anchoEscena * 0.4);
        panelDerechoLayout.setPrefWidth(anchoEscena * 0.5);
        VBox.setVgrow(panelDerechoLayout, Priority.ALWAYS);

        Scene escena = new Scene(raiz, anchoEscena, altoEscena);
        escenarioPrincipal.setScene(escena);
        
        actualizarEstiloFuente();
        actualizarTextos(); // Cargar textos al iniciar
        
        escenarioPrincipal.show();
    }
    
    /**
     * Cambia el tamaño de la fuente base de la aplicación.
     * @param delta El cambio a aplicar al tamaño de la fuente (positivo o negativo).
     */
    private static void cambiarFuente(double delta) {
        tamanoFuenteActual += delta;
        if (tamanoFuenteActual < (TAMANO_FUENTE_BASE - MAX_INCREMENTOS)) {
            tamanoFuenteActual = TAMANO_FUENTE_BASE - MAX_INCREMENTOS;
        }
        if (tamanoFuenteActual > (TAMANO_FUENTE_BASE + MAX_INCREMENTOS)) {
            tamanoFuenteActual = TAMANO_FUENTE_BASE + MAX_INCREMENTOS;
        }
        actualizarEstiloFuente();
    }

    /**
     * Aplica el tamaño de fuente actual al nodo raíz de la escena.
     */
    private static void actualizarEstiloFuente() {
        if (raiz != null) {
            raiz.setStyle("-fx-font-size: " + tamanoFuenteActual + "pt;");
        }
    }

    /**
     * Maneja el evento de clic del botón "Calcular".
     * Recoge las entradas, las valida y llama al coordinador.
     */
    private void manejarCalculo() {
        ResourceBundle bundle = coordinador.getBundle();
        etiquetaAdvertencia.setVisible(false);
        panelDerechoContenido.getChildren().clear();
        rutasCompletas = null;
        actualizarControlesNavegacion();

        try {
            String textoOrigen = campoOrigen.getText();
            String textoDestino = campoDestino.getText();
            String textoHora = campoHora.getText();
            RadioButton diaSeleccionadoRadio = (RadioButton) grupoDiasSemana.getSelectedToggle();

            if (textoOrigen.isEmpty() || textoDestino.isEmpty() || textoHora.isEmpty() || diaSeleccionadoRadio == null) {
                throw new Exception(bundle.getString("Query_MissingInputError"));
            }
            
            String textoDiaSeleccionado = diaSeleccionadoRadio.getText();
            int diaSeleccionadoInt;
            // Usamos las claves del bundle para la comparación para que funcione con cualquier idioma
            if (textoDiaSeleccionado.equals(bundle.getString("Query_Monday"))) diaSeleccionadoInt = 1;
            else if (textoDiaSeleccionado.equals(bundle.getString("Query_Tuesday"))) diaSeleccionadoInt = 2;
            else if (textoDiaSeleccionado.equals(bundle.getString("Query_Wednesday"))) diaSeleccionadoInt = 3;
            else if (textoDiaSeleccionado.equals(bundle.getString("Query_Thursday"))) diaSeleccionadoInt = 4;
            else if (textoDiaSeleccionado.equals(bundle.getString("Query_Friday"))) diaSeleccionadoInt = 5;
            else if (textoDiaSeleccionado.equals(bundle.getString("Query_Saturday"))) diaSeleccionadoInt = 6;
            else if (textoDiaSeleccionado.equals(bundle.getString("Query_Sunday"))) diaSeleccionadoInt = 7;
            else throw new IllegalStateException(bundle.getString("Query_UnexpectedDayError") + textoDiaSeleccionado);


            int idOrigen = Integer.parseInt(textoOrigen);
            Parada paradaOrigen = coordinador.getParada(idOrigen);
            if (paradaOrigen == null) {
            	throw new IllegalStateException(bundle.getString("Query_StopNotFoundError") + idOrigen);
            } 
            
            int idDestino = Integer.parseInt(textoDestino);
            Parada paradaDestino = coordinador.getParada(idDestino);
            if (paradaDestino == null) {
            	throw new IllegalStateException(bundle.getString("Query_StopNotFoundError") + idDestino);
            } 
            LocalTime hora = LocalTime.parse(textoHora);
            
            ultimaConsultaParadaOrigen = paradaOrigen;
            ultimaConsultaParadaDestino = paradaDestino;
            ultimaConsultaHoraLlegada = hora;
            
            coordinador.consulta(paradaOrigen, paradaDestino, diaSeleccionadoInt, hora);

        } catch (java.time.format.DateTimeParseException e) {
            etiquetaAdvertencia.setText(bundle.getString("Query_WrongFormatError"));
            etiquetaAdvertencia.setVisible(true);
        }
        catch (Exception e) {
            etiquetaAdvertencia.setText(e.getMessage());
            etiquetaAdvertencia.setVisible(true);
            rutasCompletas = null;
            actualizarControlesNavegacion();
        }
    }

    /**
     * Método estático llamado por el Coordinador para mostrar los resultados de la consulta.
     * @param listaRecorridos La lista de rutas encontradas.
     * @param paradaOrigen La parada de origen de la consulta.
     * @param paradaDestino La parada de destino de la consulta.
     * @param horaLlegaParada La hora de llegada del usuario a la parada de origen.
     */
    @Override
    public void resultado(List<List<Recorrido>> listaRecorridos,
            Parada paradaOrigen,
            Parada paradaDestino,
            LocalTime horaLlegaParada) {
        
        rutasCompletas = listaRecorridos;
        panelDerechoContenido.getChildren().clear();
        ResourceBundle bundle = coordinador.getBundle();

        if (rutasCompletas != null && !rutasCompletas.isEmpty()) {
            indicePaginaActual = 0;
            mostrarPaginaActual(paradaOrigen, paradaDestino, horaLlegaParada);
        } else {
            panelDerechoContenido.getChildren().add(new Label(bundle.getString("Result_ZeroRoutes")));
            actualizarControlesNavegacion();
        }
    }

    /**
     * Cambia la página de resultados que se está mostrando.
     * @param direccion El cambio de página (-1 para anterior, +1 para siguiente).
     */
    private static void cambiarPagina(int direccion) {
        if (rutasCompletas != null && !rutasCompletas.isEmpty()) {
            indicePaginaActual += direccion;
            mostrarPaginaActual(ultimaConsultaParadaOrigen, ultimaConsultaParadaDestino, ultimaConsultaHoraLlegada);
        }
    }

    /**
     * Muestra la página de resultados actual en el panel derecho.
     * Utiliza la traducción exacta solicitada.
     * * @param paradaOrigen La parada de origen de la consulta.
     * @param paradaDestino La parada de destino de la consulta.
     * @param horaLlegaParada La hora de llegada del usuario a la parada de origen.
     */
    private static void mostrarPaginaActual(Parada paradaOrigen, Parada paradaDestino, LocalTime horaLlegaParada) {
        panelDerechoContenido.getChildren().clear();
        ResourceBundle bundle = coordinador.getBundle();
        
        List<Recorrido> recorridoCompleto = rutasCompletas.get(indicePaginaActual);
        
        // Título del recorrido
        Label titulo = new Label(bundle.getString("Result_RouteX") + " " + (indicePaginaActual + 1) + ":");
        titulo.setStyle("-fx-font-weight: bold;");
        panelDerechoContenido.getChildren().add(titulo);
        
        // Aviso de trasbordos si hay más de un tramo
        if (recorridoCompleto.size() > 1) {
            Label avisoTrasbordo = new Label(bundle.getString("Result_TransferWarning") + " " + (recorridoCompleto.size() - 1) + " " + bundle.getString("Result_Transfer"));
            avisoTrasbordo.setTextFill(Color.DARKRED);
            avisoTrasbordo.setStyle("-fx-font-weight: bold;");
            panelDerechoContenido.getChildren().add(avisoTrasbordo);
        }
        
        // Mostrar cada tramo
        LocalTime horaLlegaActual = horaLlegaParada; // hora de llegada inicial al primer tramo

        for (int t = 0; t < recorridoCompleto.size(); t++) {
            Recorrido r = recorridoCompleto.get(t);

            LocalTime horaSalida = r.getHoraSalida();
            long esperaSeg = 0;
            if (horaSalida.isAfter(horaLlegaActual)) {
                esperaSeg = Duration.between(horaLlegaActual, horaSalida).getSeconds();
            }
            int viajeSeg = r.getDuracion();
            long totalSeg = esperaSeg + viajeSeg;
            LocalTime horaLlegadaTramo = horaSalida.plusSeconds(viajeSeg);

            // Origen y destino del tramo
            List<Parada> paradasTramo = r.getParadas();
            Parada tramoOrigen = paradasTramo.get(0);
            Parada tramoDestino = paradasTramo.get(paradasTramo.size() - 1);

            VBox tramoBox = new VBox(5);
            tramoBox.setPadding(new Insets(5, 0, 15, 10));
            tramoBox.setStyle("-fx-border-color: lightblue; -fx-border-width: 0 0 1 0;");

            tramoBox.getChildren().add(new Label(bundle.getString("Result_SegmentX") + " " + (t + 1) + " - " + bundle.getString("Result_LineX") + " " + r.getLinea().getCodigo()));
            tramoBox.getChildren().add(new Label("  " + bundle.getString("Result_InitialStop") + " " + tramoOrigen.getDireccion()));
            tramoBox.getChildren().add(new Label("  " + bundle.getString("Result_FinalStop") + " " + tramoDestino.getDireccion()));
            tramoBox.getChildren().add(new Label("  " + bundle.getString("Result_UserTimeOfArrival") + " " + horaLlegaActual));
            tramoBox.getChildren().add(new Label("  " + bundle.getString("Result_TimeOfDeparture") + " " + horaSalida));
            tramoBox.getChildren().add(new Label("  " + bundle.getString("Result_WaitTime") + " " + Tiempo.segundosATiempo((int) esperaSeg)));
            tramoBox.getChildren().add(new Label("  " + bundle.getString("Result_TravelTime") + " " + Tiempo.segundosATiempo(viajeSeg)));
            tramoBox.getChildren().add(new Label("  " + bundle.getString("Result_TotalTime") + " " + Tiempo.segundosATiempo((int) totalSeg)));
            tramoBox.getChildren().add(new Label("  " + bundle.getString("Result_ArrivalTime") + " " + horaLlegadaTramo));

            // Paradas intermedias
            tramoBox.getChildren().add(new Label("  " + bundle.getString("Result_Stops")));
            VBox stopsBox = new VBox(2);
            stopsBox.setPadding(new Insets(0, 0, 0, 35));
            for (int j = 0; j < paradasTramo.size() - 1; j++) {
                stopsBox.getChildren().add(new Label(paradasTramo.get(j).getDireccion() + " -> " + paradasTramo.get(j + 1).getDireccion()));
            }
            tramoBox.getChildren().add(stopsBox);
            panelDerechoContenido.getChildren().add(tramoBox);

            // Actualizar hora de llegada para el próximo tramo
            horaLlegaActual = horaLlegadaTramo;
        }

        actualizarControlesNavegacion();
    }

    
    /**
     * Actualiza el estado (etiqueta y habilitación) de los botones de navegación.
     */
    private static void actualizarControlesNavegacion() {
        ResourceBundle bundle = coordinador.getBundle();
        String de = " de "; // "of"
        if (bundle != null && bundle.getLocale().getLanguage().equals("en")) {
            de = " of ";
        }
        
        if (rutasCompletas == null || rutasCompletas.isEmpty()) {
            etiquetaPagina.setText(bundle != null ? bundle.getString("Result_RoutePages") + " 0" + de + "0" : "Ruta 0 de 0");
            botonAnterior.setDisable(true);
            botonSiguiente.setDisable(true);
        } else {
            etiquetaPagina.setText(bundle.getString("Result_RoutePages") + " " + (indicePaginaActual + 1) + de + rutasCompletas.size());
            botonAnterior.setDisable(indicePaginaActual <= 0);
            botonSiguiente.setDisable(indicePaginaActual >= rutasCompletas.size() - 1);
        }
    }

    @Override
    public void close(Stage ventana) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'close'");
    }

}