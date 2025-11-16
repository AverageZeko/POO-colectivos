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


import java.util.Map;
import colectivo.logica.Recorrido;
// Importa las clases nuevas
import colectivo.controlador.Coordinador;
import colectivo.util.ArmadorString;

/**
 * Ventana que muestra un mapa estático de Google con una ruta específica.
 * Esta clase se encarga ÚNICAMENTE de la presentación (Vista).
 * Llama al Coordinador para todas las operaciones de lógica.
 */
public class VentanaMapa extends Application {

	private int recorrido;
    // --- Componentes de la Interfaz ---
    private ImageView imageView;
    private VBox panelLeyenda;

    // --- Lógica y Datos ---
    private Coordinador coordinador;
   
    public void setRecorrido(int recorrido) {
    	this.recorrido = recorrido;
    }
    /**
     * Método para inyectar el coordinador desde la interfaz principal.
     */
    public void setCoordinador(Coordinador coordinador) {
        this.coordinador = coordinador;
    }

    /**
     * Establece el recorrido que se va a mostrar en el mapa.
     * Este método debe ser llamado antes de que se muestre la ventana.
     *
     * @param recorrido La lista de tramos ({@link Recorrido}) a dibujar.
     */
  
    @Override
    public void start(Stage stage) {

        // --- Verificación de dependencias ---
        if (coordinador == null) {
            System.err.println("FATAL: VentanaMapa iniciada sin Coordinador.");
            return;
        }
      

        // --- Configuración de la Interfaz ---
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

        // --- Conexión de Controles al Coordinador ---
        // Llama a la función pública única del coordinador
        zoomInButton.setOnAction(e -> actualizarUI(coordinador.obtenerLink(1, 0, 0,recorrido)));
        zoomOutButton.setOnAction(e -> actualizarUI(coordinador.obtenerLink(-1, 0, 0,recorrido)));
        upButton.setOnAction(e -> actualizarUI(coordinador.obtenerLink(0, 0.005, 0,recorrido)));
        downButton.setOnAction(e -> actualizarUI(coordinador.obtenerLink(0, -0.005, 0,recorrido)));
        leftButton.setOnAction(e -> actualizarUI(coordinador.obtenerLink(0, 0, -0.005,recorrido)));
        rightButton.setOnAction(e -> actualizarUI(coordinador.obtenerLink(0, 0, 0.005,recorrido)));

        HBox zoomControls = new HBox(5, zoomInButton, zoomOutButton);
        zoomControls.setAlignment(Pos.CENTER);
        VBox navButtons = new VBox(5, upButton, new HBox(5, leftButton, rightButton), downButton);
        navButtons.setAlignment(Pos.CENTER);
        
        VBox allControls = new VBox(20, zoomControls, navButtons);
        allControls.setAlignment(Pos.CENTER);
        allControls.setPadding(new Insets(10));
        
        root.setRight(allControls);

        // --- Carga Inicial ---
        
        
        // 2. Obtener el primer mapa (con deltas cero)
        ArmadorString.ResultadoMapa primerResultado = coordinador.obtenerLink(0, 0, 0,recorrido);
        actualizarUI(primerResultado);

        Scene scene = new Scene(root, 900, 680); 
        stage.setTitle("Visor de Mapa del Recorrido");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Método centralizado para actualizar la UI basado en un nuevo resultado.
     * @param resultado El objeto que contiene la nueva URL y la leyenda.
     */
    private void actualizarUI(ArmadorString.ResultadoMapa resultado) {
        mostrarImagen(resultado.getUrl());
        actualizarLeyenda(resultado.getLeyenda());
    }

    /**
     * Recibe la URL (ya sea real o de placeholder) y la muestra.
     * @param url La URL de la imagen a cargar.
     */
    private void mostrarImagen(String url) {
        System.out.println("Cargando URL de Mapa: " + url);
        Image mapImage = new Image(url);
        imageView.setImage(mapImage);
    }
    
    /**
     * Actualiza el panel de la leyenda con los colores y nombres
     * recibidos desde el armador de strings.
     * @param leyendaColores El mapa de [Nombre, ColorHex] para mostrar.
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
    
    // El método main() se elimina, ya que esta ventana ahora
    // debe ser instanciada y lanzada por otra clase (como tu 'Interfaz' principal).
}