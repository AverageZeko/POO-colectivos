package colectivo.interfaz;


import java.time.Duration;
import java.time.LocalTime;
import java.util.List;
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
public class Interfaz extends Application implements Mostrable {
    
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
     * Método principal de JavaFX, construye y muestra la interfaz gráfica.
     * @param escenarioPrincipal El escenario principal proporcionado por JavaFX.
     */
    @Override
    public void start(Stage escenarioPrincipal) {
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

        Label etiquetaOrigen = new Label("¿Dónde estás? (int)");
        Label etiquetaDestino = new Label("¿A dónde vas? (int)");
        Label etiquetaHora = new Label("¿Qué hora es? (LocalTime)");
        Label etiquetaDia = new Label("Día de la semana:");
        
        RadioButton lun = new RadioButton("Lunes");
        lun.setToggleGroup(grupoDiasSemana);
        RadioButton mar = new RadioButton("Martes");
        mar.setToggleGroup(grupoDiasSemana);
        RadioButton mie = new RadioButton("Miércoles");
        mie.setToggleGroup(grupoDiasSemana);
        RadioButton jue = new RadioButton("Jueves");
        jue.setToggleGroup(grupoDiasSemana);
        RadioButton vie = new RadioButton("Viernes");
        vie.setToggleGroup(grupoDiasSemana);
        RadioButton sab = new RadioButton("Sábado");
        sab.setToggleGroup(grupoDiasSemana);
        RadioButton dom = new RadioButton("Domingo");
        dom.setToggleGroup(grupoDiasSemana);
        VBox cajaDias = new VBox(10, lun, mar, mie, jue, vie, sab, dom);

        Button botonCalcular = new Button("Calcular");
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
        
        botonAnterior = new Button("« Anterior");
        botonAnterior.setOnAction(e -> cambiarPagina(-1));
        botonSiguiente = new Button("Siguiente »");
        botonSiguiente.setOnAction(e -> cambiarPagina(1));
        etiquetaPagina = new Label("Ruta 0 de 0");
        
        HBox cajaNavegacion = new HBox(10, botonAnterior, etiquetaPagina, botonSiguiente);
        cajaNavegacion.setAlignment(Pos.CENTER);
        cajaNavegacion.setPadding(new Insets(10));
        panelDerechoLayout.setBottom(cajaNavegacion);
        actualizarControlesNavegacion();

        Button botonAumentarFuente = new Button("Fuente +");
        botonAumentarFuente.setOnAction(e -> cambiarFuente(1));
        Button botonDisminuirFuente = new Button("Fuente -");
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
        escenarioPrincipal.setTitle("Calculadora de Rutas");
        escenarioPrincipal.setScene(escena);
        
        actualizarEstiloFuente();
        
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
                throw new Exception("Un campo está vacío o no se ha seleccionado un día.");
            }
            
            String textoDiaSeleccionado = diaSeleccionadoRadio.getText();
            int diaSeleccionadoInt;
            switch (textoDiaSeleccionado) {
                case "Lunes": diaSeleccionadoInt = 1; break;
                case "Martes": diaSeleccionadoInt = 2; break;
                case "Miércoles": diaSeleccionadoInt = 3; break;
                case "Jueves": diaSeleccionadoInt = 4; break;
                case "Viernes": diaSeleccionadoInt = 5; break;
                case "Sábado": diaSeleccionadoInt = 6; break;
                case "Domingo": diaSeleccionadoInt = 7; break;
                default: throw new IllegalStateException("Día de la semana inesperado: " + textoDiaSeleccionado);
            }

            int idOrigen = Integer.parseInt(textoOrigen);
            Parada paradaOrigen = coordinador.getParada(idOrigen);
            if (paradaOrigen == null) {
            	throw new IllegalStateException("Parada de origen no encontrada: " + idOrigen);
            } 
            
            int idDestino = Integer.parseInt(textoDestino);
            Parada paradaDestino = coordinador.getParada(idDestino);
            if (paradaDestino == null) {
            	throw new IllegalStateException("Parada de destino no encontrada: " + idDestino);
            } 
            LocalTime hora = LocalTime.parse(textoHora);
            
            ultimaConsultaParadaOrigen = paradaOrigen;
            ultimaConsultaParadaDestino = paradaDestino;
            ultimaConsultaHoraLlegada = hora;
            
            coordinador.consulta(paradaOrigen, paradaDestino, diaSeleccionadoInt, hora);

        } catch (Exception e) {
            etiquetaAdvertencia.setText("Ocurrió un error: " + e.getMessage());
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

        if (rutasCompletas != null && !rutasCompletas.isEmpty()) {
            indicePaginaActual = 0;
            mostrarPaginaActual(paradaOrigen, paradaDestino, horaLlegaParada);
        } else {
            panelDerechoContenido.getChildren().add(new Label("No se encontraron recorridos."));
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
        
        List<Recorrido> recorridoCompleto = rutasCompletas.get(indicePaginaActual);
        
        // Título del recorrido
        Label titulo = new Label("Recorrido " + (indicePaginaActual + 1) + ":");
        titulo.setStyle("-fx-font-weight: bold;");
        panelDerechoContenido.getChildren().add(titulo);
        
        // Aviso de trasbordos si hay más de un tramo
        if (recorridoCompleto.size() > 1) {
            Label avisoTrasbordo = new Label("⚠ Este recorrido incluye trasbordos: " + (recorridoCompleto.size() - 1) + " transbordo(s).");
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

            tramoBox.getChildren().add(new Label("Tramo " + (t + 1) + " - Línea: " + r.getLinea().getCodigo()));
            tramoBox.getChildren().add(new Label("  Origen tramo: " + tramoOrigen.getDireccion()));
            tramoBox.getChildren().add(new Label("  Destino tramo: " + tramoDestino.getDireccion()));
            tramoBox.getChildren().add(new Label("  Hora llegada usuario a origen: " + horaLlegaActual));
            tramoBox.getChildren().add(new Label("  Hora salida colectivo: " + horaSalida));
            tramoBox.getChildren().add(new Label("  Tiempo de espera: " + Tiempo.segundosATiempo((int) esperaSeg)));
            tramoBox.getChildren().add(new Label("  Tiempo de viaje: " + Tiempo.segundosATiempo(viajeSeg)));
            tramoBox.getChildren().add(new Label("  Duración total: " + Tiempo.segundosATiempo((int) totalSeg)));
            tramoBox.getChildren().add(new Label("  Hora de llegada destino: " + horaLlegadaTramo));

            // Paradas intermedias
            tramoBox.getChildren().add(new Label("  Paradas:"));
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
        if (rutasCompletas == null || rutasCompletas.isEmpty()) {
            etiquetaPagina.setText("Ruta 0 de 0");
            botonAnterior.setDisable(true);
            botonSiguiente.setDisable(true);
        } else {
            etiquetaPagina.setText("Ruta " + (indicePaginaActual + 1) + " de " + rutasCompletas.size());
            botonAnterior.setDisable(indicePaginaActual <= 0);
            botonSiguiente.setDisable(indicePaginaActual >= rutasCompletas.size() - 1);
        }
    }

    /**
     * Punto de entrada estático para lanzar la aplicación desde una clase externa.
     * @param coord El coordinador a inyectar.
     * @param args Argumentos de la línea de comandos.
     */
    @Override
    public void lanzarAplicacion(String[] args) {
        Application.launch(Interfaz.class, args);
    }
}