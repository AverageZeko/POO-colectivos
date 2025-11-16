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

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import colectivo.modelo.Parada;
import colectivo.logica.Recorrido;


/**
 * Ventana que muestra un mapa estático de Google con una ruta específica.
 * La ruta se dibuja sobre una imagen obtenida de la API de Google Maps Static.
 * Incluye controles para hacer zoom y moverse por el mapa, y una leyenda
 * que describe los colores de las líneas y los tramos a pie.
 */
public class VentanaMapa extends Application {

    private ImageView imageView;
    private VBox panelLeyenda;

    private double currentCenterLat = -42.7745;
    private double currentCenterLng = -65.0446;
    private int currentZoom = 13;
    
    private boolean isFirstLoad = true;
    
    private List<Recorrido> recorridoParaMostrar;
    
    private static final String[] COLORES_LINEA = {
        "0x0000FF", "0xFF0000", "0xFFA500", "0x800080", "0xA52A2A", "0x008000"
    };
    private static final String COLOR_CAMINANDO = "0x404040";
    
    private Map<String, String> leyendaColores = new HashMap<>();

    /**
     * Clave de API para Google Maps Static API.
     * ¡¡¡IMPORTANTE!!! Debes generar tu propia clave en Google Cloud Console.
     */
    private static final String TU_CLAVE_DE_API_AQUI = "AIzaSyCiWk2rBTihKSwummyYVv6mTzc-lFQspQ0";
    
    private static final int MAP_WIDTH = 640;
    private static final int MAP_HEIGHT = 640;

    /**
     * Establece el recorrido que se va a mostrar en el mapa.
     * Este método debe ser llamado antes de que se muestre la ventana.
     *
     * @param recorrido La lista de tramos ({@link Recorrido}) a dibujar.
     */
    public void setRecorrido(List<Recorrido> recorrido) {
        this.recorridoParaMostrar = recorrido;
        this.isFirstLoad = true;
        this.currentCenterLat = -42.7745;
        this.currentCenterLng = -65.0446;
        this.currentZoom = 13;
    }

    @Override
    public void start(Stage stage) {

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

        zoomInButton.setOnAction(e -> changeZoom(1));
        zoomOutButton.setOnAction(e -> changeZoom(-1));
        upButton.setOnAction(e -> moveMap(0.005, 0));
        downButton.setOnAction(e -> moveMap(-0.005, 0));
        leftButton.setOnAction(e -> moveMap(0, -0.005));
        rightButton.setOnAction(e -> moveMap(0, 0.005));

        HBox zoomControls = new HBox(5, zoomInButton, zoomOutButton);
        zoomControls.setAlignment(Pos.CENTER);
        VBox navButtons = new VBox(5, upButton, new HBox(5, leftButton, rightButton), downButton);
        navButtons.setAlignment(Pos.CENTER);
        
        VBox allControls = new VBox(20, zoomControls, navButtons);
        allControls.setAlignment(Pos.CENTER);
        allControls.setPadding(new Insets(10));
        
        root.setRight(allControls);

        updateMapImage();

        Scene scene = new Scene(root, 900, 680); 
        stage.setTitle("Visor de Mapa del Recorrido");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Actualiza el panel de la leyenda con los colores y nombres de las líneas
     * y tramos del recorrido actual.
     */
    private void actualizarLeyenda() {
        panelLeyenda.getChildren().clear();

        Label titulo = new Label("Referencias:");
        titulo.setStyle("-fx-font-weight: bold;");
        panelLeyenda.getChildren().add(titulo);

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


    /**
     * Solicita una nueva imagen del mapa a la API y actualiza la leyenda.
     */
    private void updateMapImage() {
        String url = construirURL();
        System.out.println("Cargando URL de Mapa: " + url);

        Image mapImage;
        if (TU_CLAVE_DE_API_AQUI.equals("AIzaSyCiWk2rBTihKSwummyYVv6mTzc-lFQspQ0")) {
            System.err.println("ERROR: Falta la clave de API de Google Maps.");
            mapImage = new Image("https://via.placeholder.com/" + MAP_WIDTH + "x" + MAP_HEIGHT + ".png?text=ERROR:+FALTA+API+KEY");
        } else if (url.startsWith("https://via.placeholder.com")) {
            mapImage = new Image(url);
        } else if (url.length() > 8192) {
             System.err.println("ERROR: La URL del mapa es demasiado larga (" + url.length() + " caracteres).");
             mapImage = new Image("https://via.placeholder.com/" + MAP_WIDTH + "x" + MAP_HEIGHT + ".png?text=ERROR:+RECORRIDO+MUY+LARGO");
        } else {
            mapImage = new Image(url);
        }
        
        imageView.setImage(mapImage);
        actualizarLeyenda();
    }
    
    /**
     * Cambia el nivel de zoom del mapa.
     * @param delta Incremento o decremento del zoom.
     */
    private void changeZoom(int delta) {
        currentZoom += delta;
        if (currentZoom < 1) currentZoom = 1;
        if (currentZoom > 20) currentZoom = 20;
        updateMapImage();
    }

    /**
     * Desplaza el centro del mapa.
     * @param latDelta Cambio en la latitud.
     * @param lngDelta Cambio en la longitud.
     */
    private void moveMap(double latDelta, double lngDelta) {
        currentCenterLat += latDelta;
        currentCenterLng += lngDelta;
        updateMapImage();
    }


    /**
     * Construye la URL para la solicitud a la API de Google Maps Static.
     * Esta URL incluye el tamaño, tipo de mapa, clave de API, marcadores para
     * origen/destino y las rutas (paths) para cada tramo del recorrido.
     * También se encarga de rellenar el mapa `leyendaColores` para la leyenda.
     *
     * @return La URL completa para la imagen del mapa.
     */
    private String construirURL() {
        leyendaColores.clear();
        leyendaColores.put("Parada Origen", "0x008000");
        leyendaColores.put("Parada Destino", "0xFF0000");

        String baseURL = "https://maps.googleapis.com/maps/api/staticmap";
        
        StringBuilder urlParams = new StringBuilder();
        urlParams.append("?").append(String.format("size=%dx%d", MAP_WIDTH, MAP_HEIGHT));
        urlParams.append("&maptype=roadmap");
        urlParams.append("&format=png");
        urlParams.append("&key=").append(TU_CLAVE_DE_API_AQUI);

        if (recorridoParaMostrar == null || recorridoParaMostrar.isEmpty()) {
            return "https://via.placeholder.com/" + MAP_WIDTH + "x" + MAP_HEIGHT + ".png?text=No+se+paso+un+recorrido";
        }

        List<Parada> todasLasParadas = new ArrayList<>();
        if (!recorridoParaMostrar.isEmpty()) {
            todasLasParadas.addAll(recorridoParaMostrar.get(0).getParadas());
            for (int i = 1; i < recorridoParaMostrar.size(); i++) {
                List<Parada> paradasTramo = recorridoParaMostrar.get(i).getParadas();
                if (paradasTramo != null && paradasTramo.size() > 1) {
                    todasLasParadas.addAll(paradasTramo.subList(1, paradasTramo.size()));
                }
            }
        }

        if (todasLasParadas.isEmpty()) {
             return "https://via.placeholder.com/" + MAP_WIDTH + "x" + MAP_HEIGHT + ".png?text=Recorrido+vacio";
        }

        if (isFirstLoad) {
            isFirstLoad = false;
            Parada origen = todasLasParadas.get(0);
            Parada destino = todasLasParadas.get(todasLasParadas.size() - 1);
            this.currentCenterLat = (origen.getLatitud() + destino.getLatitud()) / 2.0;
            this.currentCenterLng = (origen.getLongitud() + destino.getLongitud()) / 2.0;
        } else {
            urlParams.append("&").append(String.format("center=%f,%f", currentCenterLat, currentCenterLng));
            urlParams.append("&").append("zoom=").append(currentZoom);
        }
        
        StringBuilder markerParams = new StringBuilder();
        Parada origen = todasLasParadas.get(0);
        markerParams.append("&markers=color:green|label:O|")
                    .append(String.format("%f,%f", origen.getLatitud(), origen.getLongitud()));

        Parada destino = todasLasParadas.get(todasLasParadas.size() - 1);
        markerParams.append("&markers=color:red|label:D|")
                    .append(String.format("%f,%f", destino.getLatitud(), destino.getLongitud()));

        if (todasLasParadas.size() > 2) {
            String intermedios = todasLasParadas.stream()
                .skip(1)
                .limit(todasLasParadas.size() - 2)
                .map(p -> String.format("%f,%f", p.getLatitud(), p.getLongitud()))
                .collect(Collectors.joining("|"));
            
            markerParams.append("&markers=color:blue|size:tiny|").append(intermedios);
        }

        StringBuilder pathParams = new StringBuilder();
        int colorIndex = 0;
        Map<String, String> coloresDeLineaAsignados = new HashMap<>();
        List<Parada> paradasTramoAnterior = null;

        for (int t = 0; t < recorridoParaMostrar.size(); t++) {
            Recorrido r = recorridoParaMostrar.get(t);
            List<Parada> paradasTramoActual = r.getParadas();

            if (paradasTramoActual == null || paradasTramoActual.isEmpty()) {
                continue;
            }

            String colorHex;
            String nombreLeyenda;

            if (r.getLinea() != null) {
                String codigoLinea = r.getLinea().getCodigo();
                nombreLeyenda = "Línea " + codigoLinea;
                
                if (!coloresDeLineaAsignados.containsKey(codigoLinea)) {
                    coloresDeLineaAsignados.put(codigoLinea, COLORES_LINEA[colorIndex % COLORES_LINEA.length]);
                    colorIndex++;
                }
                colorHex = coloresDeLineaAsignados.get(codigoLinea);
                
            } else {
                nombreLeyenda = "Caminando 🚶";
                colorHex = COLOR_CAMINANDO;
            }

            leyendaColores.put(nombreLeyenda, colorHex);

            List<Parada> paradasParaPath = new ArrayList<>();
            if (t > 0 && paradasTramoAnterior != null && !paradasTramoAnterior.isEmpty()) {
                 paradasParaPath.add(paradasTramoAnterior.get(paradasTramoAnterior.size() - 1));
            }
            paradasParaPath.addAll(paradasTramoActual);

            if (paradasParaPath.size() >= 2) {
                String segmentPath = paradasParaPath.stream()
                    .map(p -> String.format("%f,%f", p.getLatitud(), p.getLongitud()))
                    .collect(Collectors.joining("|"));
                
                pathParams.append("&path=color:").append(colorHex).append("ff|weight:5|").append(segmentPath);
            }
            
            paradasTramoAnterior = paradasTramoActual;
        }
        
        return baseURL + urlParams.toString() + pathParams.toString() + markerParams.toString();
    }


    public static void main(String[] args) {
        launch(args);
    }
}