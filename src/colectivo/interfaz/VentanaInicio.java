package colectivo.interfaz;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import colectivo.controlador.Coordinador;
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

public class VentanaInicio extends Application implements VentanaInicial {

    private static Coordinador coordinador;
    private ComboBox<String> comboCiudad;
    private Label etiquetaAdvertencia;
    private ToggleGroup grupoBanderas;

    private static final Logger LOG = LoggerFactory.getLogger("Consulta");
    
    private Label lblCiudad, lblIdioma;
    private Button btnIniciar;
    private Stage primaryStage;
    private FilteredList<String> ciudadesFiltradas;


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

    @Override
    public void lanzarAplicacion(String[] args) {
        LOG.info("Lanzando VentanaInicio (Application.launch)");
        Application.launch(VentanaInicio.class, args);
    }

    private void actualizarTextos() {
        if (coordinador == null) {
            LOG.error("El coordinador es nulo. No se pueden actualizar los textos.");
            return;
        }
        ResourceBundle bundle = coordinador.getBundle();
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

        ObservableList<String> todasLasCiudades = FXCollections.observableArrayList(
            "colectivo_PM", "colectivo_AZL","colectivo_CO","colectivo_HL"
        );
        ciudadesFiltradas = new FilteredList<>(todasLasCiudades, p -> true);
        comboCiudad = new ComboBox<>(ciudadesFiltradas);
        
        FlowPane panelBanderas = new FlowPane(30, 30);
        grupoBanderas = new ToggleGroup();
        
        comboCiudad.setEditable(true);
        comboCiudad.setMaxWidth(Double.MAX_VALUE);
        
        // --- LÓGICA DE FILTRADO Y SELECCIÓN MEJORADA ---
        comboCiudad.setOnKeyReleased(event -> {
            // Solo filtra cuando el usuario está escribiendo, no con teclas como Enter o flechas.
            if (event.getCode().isLetterKey() || event.getCode().isDigitKey() || event.getCode() == KeyCode.BACK_SPACE) {
                String filtro = comboCiudad.getEditor().getText();
                ciudadesFiltradas.setPredicate(item -> {
                    if (filtro == null || filtro.isEmpty()) {
                        return true;
                    }
                    return item.toLowerCase().contains(filtro.toLowerCase());
                });
                comboCiudad.show(); // Muestra la lista filtrada
            }
        });

        // Cuando se selecciona un valor, nos aseguramos de que el filtro no lo oculte
        comboCiudad.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                // Restablecer el predicado para asegurar que el elemento seleccionado sea visible
                ciudadesFiltradas.setPredicate(item -> true);
                // Actualizar el texto del editor para que coincida con la selección
                comboCiudad.getEditor().setText(newVal);
                // Opcional: mover el cursor al final
                comboCiudad.getEditor().end();
            }
        });
        
        panelBanderas.setPadding(new Insets(20, 0, 20, 0));
        panelBanderas.setAlignment(Pos.CENTER);
        
        List<LocaleInfo> localizaciones = coordinador.descubrirLocalizaciones();
        if (localizaciones.isEmpty()) {
            LOG.warn("No se encontraron localizaciones. La selección de idioma estará vacía.");
        }
        for (LocaleInfo locale : localizaciones) {
            panelBanderas.getChildren().add(crearBotonBandera(locale));
        }
        
        btnIniciar.setMaxWidth(Double.MAX_VALUE);
        btnIniciar.setOnAction(e -> manejarInicio(primaryStage));
        
        LocaleInfo defaultLocale = coordinador.getLocale();
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
                coordinador.setLocalizacion((LocaleInfo) boton.getUserData());
                actualizarTextos();
            }
        });
        
        return boton;
    }

    private void manejarInicio(Stage ventanaActual) {
        etiquetaAdvertencia.setVisible(false);
        ToggleButton seleccionBandera = (ToggleButton) grupoBanderas.getSelectedToggle();
        String seleccionCiudad = comboCiudad.getValue();

        if (seleccionBandera == null || seleccionCiudad == null || seleccionCiudad.isEmpty()) {
            ResourceBundle bundle = coordinador.getBundle();
            etiquetaAdvertencia.setText(bundle.getString("Welcome_Warning"));
            etiquetaAdvertencia.setVisible(true);
            return;
        }

        LocaleInfo localeSeleccionado = (LocaleInfo) seleccionBandera.getUserData();
        LOG.info("Usuario seleccionó localización='{}' y ciudad='{}'", localeSeleccionado.codigoCompleto(), seleccionCiudad);

        coordinador.setLocalizacion(localeSeleccionado);
        coordinador.setCiudadActual(seleccionCiudad);

        coordinador.iniciarConsulta(ventanaActual);
    }

    @Override
    public void close(Stage ventanaActual) {
        ventanaActual.close();
    }
}