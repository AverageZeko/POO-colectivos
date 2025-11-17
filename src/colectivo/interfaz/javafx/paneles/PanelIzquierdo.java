package colectivo.interfaz.javafx.paneles;

import colectivo.interfaz.javafx.ParadaOpcion;
import colectivo.interfaz.javafx.tareas.ConsultaRequest;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.time.LocalTime;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Consumer;

/**
 * Gestiona el panel izquierdo de la interfaz de consulta,
 * que contiene el formulario para seleccionar origen, destino, hora y día.
 * Ya no usa objetos de dominio Parada; trabaja con IDs y nombres.
 */
public class PanelIzquierdo {

    /**
     * Layout principal del panel izquierdo.
     */
    private VBox layout;

    /**
     * ComboBox para seleccionar la parada de origen.
     */
    private ComboBox<ParadaOpcion> comboOrigen;

    /**
     * ComboBox para seleccionar la parada de destino.
     */
    private ComboBox<ParadaOpcion> comboDestino;

    /**
     * ComboBox para seleccionar la hora.
     */
    private ComboBox<String> comboHora;

    /**
     * ComboBox para seleccionar los minutos.
     */
    private ComboBox<String> comboMinuto;

    /**
     * Grupo de botones para seleccionar el día de la semana.
     */
    private ToggleGroup grupoDiasSemana;

    /**
     * Etiqueta para mostrar mensajes de advertencia o error.
     */
    private Label etiquetaAdvertencia;

    /**
     * Botón para ejecutar la consulta.
     */
    private Button botonCalcular;

    /**
     * Botón para volver a la pantalla anterior.
     */
    private Button botonVolver;

    /**
     * Etiqueta para la parada de origen.
     */
    private Label etiquetaOrigen;

    /**
     * Etiqueta para la parada de destino.
     */
    private Label etiquetaDestino;

    /**
     * Etiqueta para la hora.
     */
    private Label etiquetaHora;

    /**
     * Etiqueta para el día de la semana.
     */
    private Label etiquetaDia;

    /**
     * Botones de selección para cada día de la semana.
     */
    private RadioButton lun, mar, mie, jue, vie, sab, dom;

    /**
     * Acción a ejecutar al calcular la consulta.
     */
    private Consumer<ConsultaRequest> onCalcular;

    /**
     * Constructor del PanelIzquierdo.
     * @param onCalcular Acción a ejecutar al calcular la consulta.
     * @param onVolver Acción a ejecutar al volver a la pantalla anterior.
     */
    public PanelIzquierdo(Consumer<ConsultaRequest> onCalcular, Runnable onVolver) {
        this.onCalcular = onCalcular;
        crearLayout();
        botonVolver.setOnAction(e -> onVolver.run());
    }

    /**
     * Inicializa el layout y los controles del panel izquierdo.
     */
    private void crearLayout() {
        layout = new VBox(10);
        layout.setAlignment(Pos.CENTER_LEFT);

        etiquetaOrigen = new Label();
        comboOrigen = new ComboBox<>();

        etiquetaDestino = new Label();
        comboDestino = new ComboBox<>();

        etiquetaHora = new Label();
        comboHora = new ComboBox<>(generarNumeros(24));
        comboMinuto = new ComboBox<>(generarNumeros(60));
        HBox cajaHora = new HBox(5, comboHora, new Label(":"), comboMinuto);
        cajaHora.setAlignment(Pos.CENTER_LEFT);

        etiquetaDia = new Label();
        grupoDiasSemana = new ToggleGroup();
        lun = crearRadioButton(grupoDiasSemana);
        mar = crearRadioButton(grupoDiasSemana);
        mie = crearRadioButton(grupoDiasSemana);
        jue = crearRadioButton(grupoDiasSemana);
        vie = crearRadioButton(grupoDiasSemana);
        sab = crearRadioButton(grupoDiasSemana);
        dom = crearRadioButton(grupoDiasSemana);
        VBox cajaDias = new VBox(10, lun, mar, mie, jue, vie, sab, dom);

        botonCalcular = new Button();
        botonCalcular.setOnAction(event -> handleCalcularClick());

        botonVolver = new Button();

        etiquetaAdvertencia = new Label();
        etiquetaAdvertencia.setTextFill(Color.RED);
        etiquetaAdvertencia.setVisible(false);

        layout.getChildren().addAll(
            etiquetaOrigen, comboOrigen,
            etiquetaDestino, comboDestino,
            etiquetaHora, cajaHora,
            etiquetaDia, cajaDias,
            botonCalcular,
            botonVolver,
            etiquetaAdvertencia
        );
    }

    /**
     * Maneja el evento de clic en el botón calcular.
     */
    private void handleCalcularClick() {
        etiquetaAdvertencia.setVisible(false);

        ParadaOpcion paradaOrigen = comboOrigen.getValue();
        ParadaOpcion paradaDestino = comboDestino.getValue();
        String horaSel = comboHora.getValue();
        String minSel = comboMinuto.getValue();
        RadioButton diaRadio = (RadioButton) grupoDiasSemana.getSelectedToggle();

        if (paradaOrigen == null || paradaDestino == null || horaSel == null || minSel == null || diaRadio == null) {
            etiquetaAdvertencia.setText("Faltan datos para la consulta.");
            etiquetaAdvertencia.setVisible(true);
            return;
        }

        LocalTime hora = LocalTime.parse(horaSel + ":" + minSel);
        ConsultaRequest request = new ConsultaRequest(
            paradaOrigen.getId(),
            paradaDestino.getId(),
            diaRadio,
            hora
        );
        onCalcular.accept(request);
    }

    /**
     * Actualiza los textos de los controles del panel izquierdo según el ResourceBundle.
     * @param bundle ResourceBundle con los textos traducidos.
     */
    public void actualizarTextos(ResourceBundle bundle) {
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
        comboOrigen.setPromptText(bundle.getString("Query_OriginPrompt"));
        comboDestino.setPromptText(bundle.getString("Query_DestinationPrompt"));
        if (etiquetaAdvertencia.isVisible()) {
            etiquetaAdvertencia.setText(bundle.getString("Query_MissingInputError"));
        }
    }

    /**
     * Carga paradas como mapa id -> nombre (dirección) y arma opciones para los ComboBox.
     * @param paradasMap Mapa de IDs de parada a nombres.
     */
    public void cargarParadas(Map<Integer, String> paradasMap) {
        ObservableList<ParadaOpcion> paradasLista = FXCollections.observableArrayList();
        paradasMap.forEach((id, nombre) -> paradasLista.add(new ParadaOpcion(id, nombre)));
        comboOrigen.setItems(paradasLista);
        comboDestino.setItems(paradasLista);
    }

    /**
     * Habilita o deshabilita el botón calcular.
     * @param deshabilitado true para deshabilitar, false para habilitar.
     */
    public void setBotonCalcularDeshabilitado(boolean deshabilitado) {
        botonCalcular.setDisable(deshabilitado);
    }

    /**
     * Devuelve el layout principal del panel izquierdo.
     * @return VBox del panel izquierdo.
     */
    public VBox getLayout() {
        return layout;
    }

    /**
     * Genera una lista de números formateados con dos dígitos.
     * @param limite Límite superior (exclusivo).
     * @return ObservableList de números como String.
     */
    private ObservableList<String> generarNumeros(int limite) {
        ObservableList<String> numeros = FXCollections.observableArrayList();
        for (int i = 0; i < limite; i++) {
            numeros.add(String.format("%02d", i));
        }
        return numeros;
    }

    /**
     * Crea un RadioButton y lo agrega al grupo especificado.
     * @param grupo ToggleGroup al que se agrega el RadioButton.
     * @return RadioButton creado.
     */
    private RadioButton crearRadioButton(ToggleGroup grupo) {
        RadioButton rb = new RadioButton();
        rb.setToggleGroup(grupo);
        return rb;
    }
}