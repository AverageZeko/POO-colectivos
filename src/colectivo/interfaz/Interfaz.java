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
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;

import colectivo.controlador.Coordinador;
import colectivo.modelo.Parada;
import colectivo.modelo.Recorrido;
import colectivo.util.Tiempo;

public class Interfaz extends Application {
    
    // --- Campos estáticos para manejar el estado y la UI ---
    private static Coordinador coordinador;
    private static List<List<Recorrido>> allRoutes;
    private static int currentPageIndex;
    
    private static Parada lastQueryParadaOrigen;
    private static Parada lastQueryParadaDestino;
    private static LocalTime lastQueryHoraLlegaParada;

    private static TextField locationField;
    private static TextField destinationField;
    private static TextField timeField;
    private static Label warningLabel;
    private static VBox rightPanelContent;
    private static ToggleGroup dayOfWeekGroup;
    private static Button prevButton;
    private static Button nextButton;
    private static Label pageLabel;

    public static void setCoordinador(Coordinador coord) {
        coordinador = coord;
    }

    @Override
    public void start(Stage primaryStage) {
        // --- 1. Diseño Principal ---
        HBox root = new HBox(20);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.TOP_LEFT);

        // --- 2. Panel Izquierdo (Entrada de usuario) ---
        VBox leftPanel = new VBox(10);
        leftPanel.setAlignment(Pos.CENTER_LEFT);

        // --- Asignación a campos estáticos ---
        locationField = new TextField();
        locationField.setPromptText("e.g., 101");
        destinationField = new TextField();
        destinationField.setPromptText("e.g., 202");
        timeField = new TextField();
        timeField.setPromptText("HH:mm, e.g., 14:30");
        dayOfWeekGroup = new ToggleGroup();
        warningLabel = new Label();
        warningLabel.setTextFill(Color.RED);
        warningLabel.setVisible(false);
        rightPanelContent = new VBox(10);
        rightPanelContent.setPadding(new Insets(10));

        Label locationLabel = new Label("Where are you? (int)");
        Label destinationLabel = new Label("Where are you going? (int)");
        Label timeLabel = new Label("What time is it? (LocalTime)");
        Label dayLabel = new Label("Day of the week:");
        
        RadioButton mon = new RadioButton("Monday");
        mon.setToggleGroup(dayOfWeekGroup);
        RadioButton tue = new RadioButton("Tuesday");
        tue.setToggleGroup(dayOfWeekGroup);
        RadioButton wed = new RadioButton("Wednesday");
        wed.setToggleGroup(dayOfWeekGroup);
        RadioButton thu = new RadioButton("Thursday");
        thu.setToggleGroup(dayOfWeekGroup);
        RadioButton fri = new RadioButton("Friday");
        fri.setToggleGroup(dayOfWeekGroup);
        RadioButton sat = new RadioButton("Saturday");
        sat.setToggleGroup(dayOfWeekGroup);
        RadioButton sun = new RadioButton("Sunday");
        sun.setToggleGroup(dayOfWeekGroup);
        VBox dayBox = new VBox(10, mon, tue, wed, thu, fri, sat, sun);

        Button calculateButton = new Button("Calculate");
        calculateButton.setOnAction(event -> handleCalculation());

        leftPanel.getChildren().addAll(
            locationLabel, locationField,
            destinationLabel, destinationField,
            timeLabel, timeField,
            dayLabel, dayBox,
            calculateButton,
            warningLabel
        );

        // --- 3. Panel Derecho (Resultados con paginación) ---
        BorderPane rightPanelLayout = new BorderPane();
        rightPanelLayout.setStyle("-fx-border-color: lightgray; -fx-border-width: 1; -fx-border-radius: 5;");
        
        ScrollPane scrollPane = new ScrollPane(rightPanelContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        rightPanelLayout.setCenter(scrollPane);
        
        prevButton = new Button("<< Previous");
        prevButton.setOnAction(e -> changePage(-1));
        nextButton = new Button("Next >>");
        nextButton.setOnAction(e -> changePage(1));
        pageLabel = new Label("Route 0 of 0");
        
        HBox navigationBox = new HBox(10, prevButton, pageLabel, nextButton);
        navigationBox.setAlignment(Pos.CENTER);
        navigationBox.setPadding(new Insets(10));
        rightPanelLayout.setBottom(navigationBox);
        updateNavigationControls();

        // --- 5. Configuración Final ---
        root.getChildren().addAll(leftPanel, rightPanelLayout);

        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
        double sceneWidth = primaryScreenBounds.getWidth() * 0.6;
        double sceneHeight = primaryScreenBounds.getHeight() * 0.75;
        
        leftPanel.setPrefWidth(sceneWidth * 0.4);
        rightPanelLayout.setPrefWidth(sceneWidth * 0.5);
        VBox.setVgrow(rightPanelLayout, Priority.ALWAYS);

        Scene scene = new Scene(root, sceneWidth, sceneHeight);
        primaryStage.setTitle("Data Calculator");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    private void handleCalculation() {
        warningLabel.setVisible(false);
        rightPanelContent.getChildren().clear();
        allRoutes = null;
        updateNavigationControls();

        try {
            String locationText = locationField.getText();
            String destinationText = destinationField.getText();
            String timeText = timeField.getText();
            RadioButton selectedDayRadio = (RadioButton) dayOfWeekGroup.getSelectedToggle();

            if (locationText.isEmpty() || destinationText.isEmpty() || timeText.isEmpty() || selectedDayRadio == null) {
                throw new Exception("A field is empty or a day is not selected.");
            }
            
            String selectedDayText = selectedDayRadio.getText();
            int selectedDayInt;
            switch (selectedDayText) {
                case "Monday": selectedDayInt = 1; break;
                case "Tuesday": selectedDayInt = 2; break;
                case "Wednesday": selectedDayInt = 3; break;
                case "Thursday": selectedDayInt = 4; break;
                case "Friday": selectedDayInt = 5; break;
                case "Saturday": selectedDayInt = 6; break;
                case "Sunday": selectedDayInt = 7; break;
                default: throw new IllegalStateException("Unexpected day of week: " + selectedDayText);
            }

            int currentLocationId = Integer.parseInt(locationText);
            Parada startParada = coordinador.getEmpresa().getParada(currentLocationId);
            if (startParada == null) {
            	throw new IllegalStateException("Starting stop not found: " + currentLocationId);
            } 
            
            int destinationId = Integer.parseInt(destinationText);
            Parada destinationParada = coordinador.getEmpresa().getParada(destinationId);
            if (destinationParada == null) {
            	throw new IllegalStateException("Destination stop not found: " + destinationId);
            } 
            LocalTime time = LocalTime.parse(timeText);
            
            // Guardar parámetros para la paginación
            lastQueryParadaOrigen = startParada;
            lastQueryParadaDestino = destinationParada;
            lastQueryHoraLlegaParada = time;
            
            // Llamar al coordinador, que a su vez llamará a Test.resultado(...)
            coordinador.consulta(startParada, destinationParada, selectedDayInt, time);

        } catch (Exception e) {
            warningLabel.setText("An error occurred: " + e.getMessage());
            warningLabel.setVisible(true);
            allRoutes = null;
            updateNavigationControls();
        }
    }

    public static void resultado(List<List<Recorrido>> listaRecorridos,
            Parada paradaOrigen,
            Parada paradaDestino,
            LocalTime horaLlegaParada) {

        allRoutes = listaRecorridos;
        rightPanelContent.getChildren().clear();

        if (allRoutes != null && !allRoutes.isEmpty()) {
            currentPageIndex = 0;
            displayCurrentPage(paradaOrigen, paradaDestino, horaLlegaParada);
        } else {
            rightPanelContent.getChildren().add(new Label("No routes found."));
            updateNavigationControls();
        }
    }

    private static void changePage(int direction) {
        if (allRoutes != null && !allRoutes.isEmpty()) {
            currentPageIndex += direction;
            displayCurrentPage(lastQueryParadaOrigen, lastQueryParadaDestino, lastQueryHoraLlegaParada);
        }
    }

    private static void displayCurrentPage(Parada paradaOrigen, Parada paradaDestino, LocalTime horaLlegaParada) {
        rightPanelContent.getChildren().clear();
        
        List<Recorrido> recorridoCompleto = allRoutes.get(currentPageIndex);
        
        Label title = new Label("Route Option " + (currentPageIndex + 1) + ":");
        title.setStyle("-fx-font-weight: bold;");
        rightPanelContent.getChildren().add(title);
        
        for (Recorrido r : recorridoCompleto) {
            LocalTime horaSalida = r.getHoraSalida();
            long esperaSeg = 0;
            if (horaSalida.isAfter(horaLlegaParada)) {
                esperaSeg = Duration.between(horaLlegaParada, horaSalida).getSeconds();
            }
            int viajeSeg = r.getDuracion();
            long totalSeg = esperaSeg + viajeSeg;
            LocalTime horaLlegada = horaSalida.plusSeconds(viajeSeg);

            VBox tramoBox = new VBox(5);
            tramoBox.setPadding(new Insets(5, 0, 15, 10));
            tramoBox.setStyle("-fx-border-color: lightblue; -fx-border-width: 0 0 1 0;");
            
            tramoBox.getChildren().add(new Label("  - Line: " + r.getLinea().getCodigo()));
            tramoBox.getChildren().add(new Label("    User Origin: " + paradaOrigen.getDireccion()));
            tramoBox.getChildren().add(new Label("    Destination: " + paradaDestino.getDireccion()));
            tramoBox.getChildren().add(new Label("    User arrival time at origin: " + horaLlegaParada));
            tramoBox.getChildren().add(new Label("    Bus departure time: " + horaSalida));
            tramoBox.getChildren().add(new Label("    Wait time: " + Tiempo.segundosATiempo((int) esperaSeg)));
            tramoBox.getChildren().add(new Label("    Travel time: " + Tiempo.segundosATiempo(viajeSeg)));
            tramoBox.getChildren().add(new Label("    Total duration: " + Tiempo.segundosATiempo((int) totalSeg)));
            tramoBox.getChildren().add(new Label("    Arrival time at destination: " + horaLlegada));
            
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < r.getParadas().size(); j++) {
                sb.append(r.getParadas().get(j).getDireccion());
                if (j < r.getParadas().size() - 1) sb.append(" -> ");
            }
            tramoBox.getChildren().add(new Label("    Stops: " + sb.toString()));
            
            rightPanelContent.getChildren().add(tramoBox);
        }
        updateNavigationControls();
    }
    
    private static void updateNavigationControls() {
        if (allRoutes == null || allRoutes.isEmpty()) {
            pageLabel.setText("Route 0 of 0");
            prevButton.setDisable(true);
            nextButton.setDisable(true);
        } else {
            pageLabel.setText("Route " + (currentPageIndex + 1) + " of " + allRoutes.size());
            prevButton.setDisable(currentPageIndex <= 0);
            nextButton.setDisable(currentPageIndex >= allRoutes.size() - 1);
        }
    }

    public static void launchApp(Coordinador coord, String[] args) {
        setCoordinador(coord);
        Application.launch(Interfaz.class, args);
    }
}

