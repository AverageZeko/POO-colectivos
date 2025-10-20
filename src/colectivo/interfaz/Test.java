package colectivo.interfaz;


import java.time.LocalTime;
import java.time.format.DateTimeParseException;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Test extends Application {

    @Override
    public void start(Stage primaryStage) {
        // --- 1. Main Layout ---
        // An HBox divides the screen horizontally.
        // The number 20 is the spacing between the left and right panels.
        HBox root = new HBox(20);
        root.setPadding(new Insets(20)); // 20px margin around the whole window
        root.setAlignment(Pos.CENTER);

        // --- 2. Left Panel (for user input) ---
        // A VBox arranges elements vertically.
        // The number 10 is the spacing between each element (label, text field, etc.).
        VBox leftPanel = new VBox(10);
        leftPanel.setAlignment(Pos.CENTER_LEFT);

        // Create the input fields and their labels
        Label locationLabel = new Label("Where are you? (int)");
        TextField locationField = new TextField();
        locationField.setPromptText("e.g., 101"); // Placeholder text

        Label destinationLabel = new Label("Where are you going? (int)");
        TextField destinationField = new TextField();
        destinationField.setPromptText("e.g., 202");

        Label timeLabel = new Label("What time is it? (LocalTime)");
        TextField timeField = new TextField();
        timeField.setPromptText("HH:mm, e.g., 14:30");

        // Create the warning label, initially hidden
        Label warningLabel = new Label();
        warningLabel.setTextFill(Color.RED); // Set the text color to red
        warningLabel.setVisible(false);     // Make it invisible by default

        // Create the button
        Button calculateButton = new Button("Calculate");

        // Add all the controls to the left panel in order
        leftPanel.getChildren().addAll(
            locationLabel, locationField,
            destinationLabel, destinationField,
            timeLabel, timeField,
            calculateButton,
            warningLabel
        );

        // --- 3. Right Panel (for displaying the result) ---
        VBox rightPanel = new VBox(10);
        rightPanel.setAlignment(Pos.CENTER_LEFT);
        rightPanel.setPadding(new Insets(10));
        // Add a simple border to make it look distinct
        rightPanel.setStyle("-fx-border-color: lightgray; -fx-border-width: 1; -fx-border-radius: 5;");
        // Set a minimum width so it doesn't look too small when empty
        rightPanel.setMinWidth(200);


        // --- 4. Button Logic (what happens when you click "Calculate") ---
        calculateButton.setOnAction(event -> {
            // First, reset everything from the previous click
            warningLabel.setVisible(false);
            rightPanel.getChildren().clear(); // Clear old results

            try {
                // Get the text from the input fields
                String locationText = locationField.getText();
                String destinationText = destinationField.getText();
                String timeText = timeField.getText();

                // Check if any field is empty. If so, throw an exception to be caught below.
                if (locationText.isEmpty() || destinationText.isEmpty() || timeText.isEmpty()) {
                    throw new Exception("A field is empty.");
                }

                // Try to convert the text into the correct data types
                int currentLocation = Integer.parseInt(locationText);
                int destination = Integer.parseInt(destinationText);
                LocalTime time = LocalTime.parse(timeText); // JavaFX can parse "HH:mm" format

                // If all conversions are successful, display the data on the right panel
                Label resultTitle = new Label("Calculation Data:");
                resultTitle.setStyle("-fx-font-weight: bold;");

                Label resultLocation = new Label("Current Location: " + currentLocation);
                Label resultDestination = new Label("Destination: " + destination);
                Label resultTime = new Label("Time: " + time.toString());

                rightPanel.getChildren().addAll(resultTitle, resultLocation, resultDestination, resultTime);

            } catch (Exception e) {
                // If any error occurs (empty field, wrong number, wrong time format),
                // this 'catch' block will execute.
                warningLabel.setText("One or many of the fields has been incorrectly filled.");
                warningLabel.setVisible(true);
            }
        });


        // --- 5. Final Scene and Stage Setup ---
        // Add both panels to the main HBox layout
        root.getChildren().addAll(leftPanel, rightPanel);

        // Create the scene and set the window size
        Scene scene = new Scene(root, 600, 350);
        primaryStage.setTitle("Data Calculator");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
    
    
    private int dia, parada,paradaf;
    private LocalTime time;
    

    
    
}
