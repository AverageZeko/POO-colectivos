package colectivo.interfaz.javafx; 

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

/**
 * Ventana que muestra un mapa estático de Google con una ruta específica.
 * Esta clase se encarga ÚNICAMENTE de la presentación (Vista).
 * Llama al GestorDeVentanas para todas las operaciones de lógica.
 */
public class VentanaMapa extends Application {

	private int recorrido;
    // --- Componentes de la Interfaz ---
    private ImageView imageView;
    private VBox panelLeyenda;

    // --- Lógica y Datos ---
    private GestorDeVentanas gestor;
   
    public void setRecorrido(int recorrido) {
    	this.recorrido = recorrido;
    }
    /**
     * Método para inyectar el gestor.
     */
    public void setGestor(GestorDeVentanas gestor) {
        this.gestor = gestor;
    }
  
    @Override
    public void start(Stage stage) {

        if (gestor == null) {
            System.err.println("FATAL: VentanaMapa iniciada sin GestorDeVentanas.");
            return;
        }
      
        //Creación de UI, botones, etc.
        imageView = new ImageView();
        imageView.setPreserveRatio(true);
        BorderPane root = new BorderPane();
        root.setCenter(imageView);
        panelLeyenda = new VBox(5);
        panelLeyenda.setPadding(new Insets(10));
        panelLeyenda.setAlignment(Pos.TOP_LEFT);
        root.setLeft(panelLeyenda);
        Button zoomInButton = new Button("+");
        Button zoomOutButton = new Button("-");
        Button upButton = new Button("▲");
        Button downButton = new Button("▼");
        Button leftButton = new Button("◀");
        Button rightButton = new Button("▶");

        zoomInButton.setOnAction(e -> actualizarUI(gestor.solicitarMapa(1, 0, 0,recorrido)));
        zoomOutButton.setOnAction(e -> actualizarUI(gestor.solicitarMapa(-1, 0, 0,recorrido)));
        upButton.setOnAction(e -> actualizarUI(gestor.solicitarMapa(0, 0.005, 0,recorrido)));
        downButton.setOnAction(e -> actualizarUI(gestor.solicitarMapa(0, -0.005, 0,recorrido)));
        leftButton.setOnAction(e -> actualizarUI(gestor.solicitarMapa(0, 0, -0.005,recorrido)));
        rightButton.setOnAction(e -> actualizarUI(gestor.solicitarMapa(0, 0, 0.005,recorrido)));

        //Layout de controles
        HBox zoomControls = new HBox(5, zoomInButton, zoomOutButton);
        zoomControls.setAlignment(Pos.CENTER);
        VBox navButtons = new VBox(5, upButton, new HBox(5, leftButton, rightButton), downButton);
        navButtons.setAlignment(Pos.CENTER);
        VBox allControls = new VBox(20, zoomControls, navButtons);
        allControls.setAlignment(Pos.CENTER);
        allControls.setPadding(new Insets(10));
        root.setRight(allControls);

        Map<String, Object> primerResultado = gestor.solicitarMapa(0, 0, 0,recorrido);
        actualizarUI(primerResultado);

        Scene scene = new Scene(root, 900, 680); 
        stage.setTitle("Visor de Mapa del Recorrido");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Método centralizado para actualizar la UI basado en un nuevo resultado.
     */
    private void actualizarUI(Map<String, Object> resultado) {
        if (resultado == null) {
            System.err.println("actualizarUI recibió un resultado nulo.");
            return;
        }

        // --- CAMBIO: Extraer datos del Map con casting ---
        String url = (String) resultado.get("link");

        // Casteo seguro para la leyenda
        @SuppressWarnings("unchecked")
        Map<String, String> leyenda = (Map<String, String>) resultado.get("leyenda");

        // Manejo de nulos
        if (url == null) {
            url = "https://via.placeholder.com/640x640.png?text=Error:+URL+nula";
        }
        if (leyenda == null) {
            leyenda = new HashMap<>(); // Evita NullPointerException
        }

        mostrarImagen(url);
        actualizarLeyenda(leyenda);
    }

    /**
     * Recibe la URL (ya sea real o de placeholder) y la muestra.
     */
    private void mostrarImagen(String url) {
        System.out.println("Cargando URL de Mapa: " + url);
        Image mapImage = new Image(url);
        imageView.setImage(mapImage);
    }
    
    /**
     * Actualiza el panel de la leyenda con los colores y nombres
     * recibidos desde el armador de strings.
     */
    private void actualizarLeyenda(Map<String, String> leyendaColores) {
        panelLeyenda.getChildren().clear();

        Label titulo = new Label("Referencias:");
        titulo.setStyle("-fx-font-weight: bold;");
        panelLeyenda.getChildren().add(titulo);

        if (leyendaColores == null) return;

        for (Map.Entry<String, String> entry : leyendaColores.entrySet()) {
            String nombre = entry.getKey();
            String hexColor = entry.getValue().startsWith("0x") ? 
                              entry.getValue().replace("0x", "#") : 
                              "#" + entry.getValue();

            Rectangle colorRect = new Rectangle(20, 20);
            colorRect.setFill(Color.web(hexColor));
            colorRect.setStroke(Color.BLACK);
            colorRect.setStrokeWidth(1);

            Label label = new Label(nombre);
            HBox filaLeyenda = new HBox(5, colorRect, label);
            filaLeyenda.setAlignment(Pos.CENTER_LEFT);
            
            panelLeyenda.getChildren().add(filaLeyenda);
        }
    }
}