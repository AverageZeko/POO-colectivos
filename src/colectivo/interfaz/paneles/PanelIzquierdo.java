package colectivo.interfaz.paneles;


import colectivo.interfaz.celdas.ParadasComboBox;
import colectivo.interfaz.tareas.ConsultaRequest;
import colectivo.modelo.Parada;
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
 */
public class PanelIzquierdo {

    private VBox layout;
    private ComboBox<Parada> comboOrigen;
    private ComboBox<Parada> comboDestino;
    private ComboBox<String> comboHora;
    private ComboBox<String> comboMinuto;
    private ToggleGroup grupoDiasSemana;
    private Label etiquetaAdvertencia;
    private Button botonCalcular;
    private Button botonVolver;

    private Label etiquetaOrigen;
    private Label etiquetaDestino;
    private Label etiquetaHora;
    private Label etiquetaDia;
    private RadioButton lun, mar, mie, jue, vie, sab, dom;

    private Consumer<ConsultaRequest> onCalcular;

    public PanelIzquierdo(Consumer<ConsultaRequest> onCalcular, Runnable onVolver) {
        this.onCalcular = onCalcular;
        crearLayout();
        botonVolver.setOnAction(e -> onVolver.run());
    }

    private void crearLayout() {
        layout = new VBox(10);
        layout.setAlignment(Pos.CENTER_LEFT);

        etiquetaOrigen = new Label();
        comboOrigen = new ComboBox<>();
        configurarComboBoxParada(comboOrigen);

        etiquetaDestino = new Label();
        comboDestino = new ComboBox<>();
        configurarComboBoxParada(comboDestino);

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
    
    private void handleCalcularClick() {
        etiquetaAdvertencia.setVisible(false);
        
        Parada paradaOrigen = comboOrigen.getValue();
        Parada paradaDestino = comboDestino.getValue();
        String horaSel = comboHora.getValue();
        String minSel = comboMinuto.getValue();
        RadioButton diaRadio = (RadioButton) grupoDiasSemana.getSelectedToggle();

        if (paradaOrigen == null || paradaDestino == null || horaSel == null || minSel == null || diaRadio == null) {
            etiquetaAdvertencia.setText("Faltan datos para la consulta."); // Se actualizará en actualizarTextos
            etiquetaAdvertencia.setVisible(true);
            return;
        }

        LocalTime hora = LocalTime.parse(horaSel + ":" + minSel);
        ConsultaRequest request = new ConsultaRequest(paradaOrigen, paradaDestino, diaRadio, hora);
        onCalcular.accept(request);
    }

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
    
    public void cargarParadas(Map<Integer, Parada> paradasMap) {
        ObservableList<Parada> paradasLista = FXCollections.observableArrayList(paradasMap.values());
        comboOrigen.setItems(paradasLista);
        comboDestino.setItems(paradasLista);
    }
    
    public void setBotonCalcularDeshabilitado(boolean deshabilitado) {
        botonCalcular.setDisable(deshabilitado);
    }

    public VBox getLayout() {
        return layout;
    }

    private void configurarComboBoxParada(ComboBox<Parada> comboBox) {
        comboBox.setCellFactory(param -> new ParadasComboBox());
        comboBox.setButtonCell(new ParadasComboBox());
    }

    private ObservableList<String> generarNumeros(int limite) {
        ObservableList<String> numeros = FXCollections.observableArrayList();
        for (int i = 0; i < limite; i++) {
            numeros.add(String.format("%02d", i));
        }
        return numeros;
    }

    private RadioButton crearRadioButton(ToggleGroup grupo) {
        RadioButton rb = new RadioButton();
        rb.setToggleGroup(grupo);
        return rb;
    }
}