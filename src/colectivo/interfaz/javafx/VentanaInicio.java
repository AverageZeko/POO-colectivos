package colectivo.interfaz.javafx;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import colectivo.util.LocaleInfo;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


/**
 * Implementación de {@link IVentana} que muestra la pantalla de bienvenida.
 * Permite al usuario seleccionar una ciudad y un idioma antes de iniciar
 * la aplicación de consultas.
 */
public class VentanaInicio extends Application{

    private static GestorDeVentanas gestor;
    
    private ComboBox<String> comboCiudad;
    private Label etiquetaAdvertencia;
    private ToggleGroup grupoBanderas;

    private static final Logger LOG = LoggerFactory.getLogger("Consulta");
    
    private Label lblCiudad, lblIdioma;
    private Button btnIniciar;
    private Stage primaryStage;
    private FilteredList<String> ciudadesFiltradas;

    
    public void setGestor(GestorDeVentanas gest) {
        VentanaInicio.gestor = gest;
    }


    public void lanzarAplicacion(String[] args) {
        LOG.info("Lanzando VentanaInicio (Application.launch)");
        Application.launch(VentanaInicio.class, args);
    }

    /**
     * Actualiza los textos de la interfaz según el idioma seleccionado.
     */
    private void actualizarTextos() {
        if (gestor == null) {
            LOG.error("El gestor es nulo. No se pueden actualizar los textos.");
            return;
        }
        ResourceBundle bundle = gestor.getBundle();
        if (bundle == null) {
            LOG.warn("ResourceBundle es nulo, no se pueden actualizar los textos. Verifica que el idioma por defecto esté bien configurado.");
            return;
        }
        primaryStage.setTitle(bundle.getString("Welcome_WindowTitle"));
        lblCiudad.setText(bundle.getString("Welcome_CityLabel"));
        lblIdioma.setText(bundle.getString("Welcome_LanguageLabel"));
        btnIniciar.setText(bundle.getString("Welcome_StartButton"));
        comboCiudad.setPromptText(bundle.getString("Welcome_CityPrompt"));
    }

    
    public void mostrar(Stage stage) {
        start(stage);
    }
    
    
    public void cerrar(Stage stage) {
        stage.close();
    }
    
    /**
     * Inicializa y muestra la ventana de inicio.
     * @param primaryStage El escenario principal de la aplicación.
     */
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        LOG.info("Mostrando ventana de inicio dinámica y adaptable.");

        lblCiudad = new Label();
        lblIdioma = new Label();
        btnIniciar = new Button();
        etiquetaAdvertencia = new Label();
        etiquetaAdvertencia.setStyle("-fx-text-fill: red; -fx-font-size: 14pt;");
        etiquetaAdvertencia.setVisible(false);

        ObservableList<String> todasLasCiudades =
                FXCollections.observableArrayList(gestor.getCiudades());

        ciudadesFiltradas = new FilteredList<>(todasLasCiudades, p -> true);
        comboCiudad = new ComboBox<>(ciudadesFiltradas);
        
        FlowPane panelBanderas = new FlowPane(30, 30);
        grupoBanderas = new ToggleGroup();
        
        comboCiudad.setEditable(true);
        comboCiudad.setMaxWidth(Double.MAX_VALUE);
        
        comboCiudad.setOnKeyReleased(event -> {
            if (event.getCode().isLetterKey() || event.getCode().isDigitKey() || event.getCode() == KeyCode.BACK_SPACE) {
                String filtro = comboCiudad.getEditor().getText();
                ciudadesFiltradas.setPredicate(item -> {
                    if (filtro == null || filtro.isEmpty()) {
                        return true;
                    }
                    return item.toLowerCase().contains(filtro.toLowerCase());
                });
                comboCiudad.show(); 
            }
        });

        comboCiudad.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                ciudadesFiltradas.setPredicate(item -> true);
                comboCiudad.getEditor().setText(newVal);
                comboCiudad.getEditor().end();
            }
        });
        
        panelBanderas.setPadding(new Insets(20, 0, 20, 0));
        panelBanderas.setAlignment(Pos.CENTER);
        
        List<LocaleInfo> localizaciones = gestor.descubrirLocalizaciones();
        if (localizaciones.isEmpty()) {
            LOG.warn("No se encontraron localizaciones. La selección de idioma estará vacía.");
        }
        for (LocaleInfo locale : localizaciones) {
            panelBanderas.getChildren().add(crearBotonBandera(locale));
        }
        
        btnIniciar.setMaxWidth(Double.MAX_VALUE);
        btnIniciar.setOnAction(e -> manejarInicio(primaryStage));
        
        LocaleInfo defaultLocale = gestor.getLocale();
        if (defaultLocale != null) {
            for (Node node : panelBanderas.getChildren()) {
                if (node instanceof ToggleButton) {
                    ToggleButton botonBandera = (ToggleButton) node;
                    LocaleInfo localeBoton = (LocaleInfo) botonBandera.getUserData();
                    if (defaultLocale.equals(localeBoton)) {
                        botonBandera.setSelected(true);
                        break; 
                    }
                }
            }
        }
        
        VBox root = new VBox(15, lblCiudad, comboCiudad, lblIdioma, panelBanderas, btnIniciar, etiquetaAdvertencia);
        root.setPadding(new Insets(40));
        root.setStyle("-fx-font-size: 18pt;");
        VBox.setVgrow(panelBanderas, Priority.ALWAYS);

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        
        actualizarTextos(); 
        primaryStage.show();
    }
    
    /**
     * Crea un {@link ToggleButton} con la imagen de una bandera para una localización.
     * @param locale La información de la localización.
     * @return Un ToggleButton configurado con la bandera y estilos.
     */
    private ToggleButton crearBotonBandera(LocaleInfo locale) {
        ToggleButton boton = new ToggleButton();
        boton.setUserData(locale);
        
        try (InputStream stream = getClass().getResourceAsStream(locale.getRutaBandera())) {
            if (stream == null) throw new IOException("Recurso no encontrado: " + locale.getRutaBandera());
            Image imagen = new Image(stream, 180, 120, true, true);
            boton.setGraphic(new ImageView(imagen));
        } catch (Exception e) {
            LOG.error("No se pudo cargar la bandera para '{}'", locale.codigoCompleto(), e);
            boton.setText(locale.codigoCompleto());
        }
        
        boton.setToggleGroup(grupoBanderas);
        String styleUnselected = "-fx-background-color: transparent; -fx-padding: 8; -fx-border-color: lightgray; -fx-border-radius: 12;";
        String styleSelected = "-fx-background-color: #cce7ff; -fx-padding: 8; -fx-border-color: #007bff; -fx-border-width: 4; -fx-border-radius: 12;";
        
        boton.setStyle(styleUnselected);
        
        boton.selectedProperty().addListener((obs, was, is) -> {
            boton.setStyle(is ? styleSelected : styleUnselected);
            if (is) {
                gestor.setLocalizacion((LocaleInfo) boton.getUserData());
                actualizarTextos();
            }
        });
        
        return boton;
    }

    /**
     * Maneja el evento del botón "Iniciar". Recoge los datos y los pasa al gestor.
     * @param ventanaActual La ventana actual que se cerrará.
     */
    private void manejarInicio(Stage ventanaActual) {
        etiquetaAdvertencia.setVisible(false);
        ToggleButton seleccionBandera = (ToggleButton) grupoBanderas.getSelectedToggle();
        String seleccionCiudad = comboCiudad.getValue();
        LocaleInfo localeSeleccionado = (seleccionBandera != null) ? (LocaleInfo) seleccionBandera.getUserData() : null;

        gestor.procesarInicio(localeSeleccionado, seleccionCiudad, ventanaActual);
    }
    
    /**
     * Muestra un mensaje de advertencia en la interfaz.
     * @param mensaje El texto a mostrar.
     */
    public void mostrarAdvertencia(String mensaje) {
        etiquetaAdvertencia.setText(mensaje);
        etiquetaAdvertencia.setVisible(true);
    }
}