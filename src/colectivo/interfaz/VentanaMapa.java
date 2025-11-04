package colectivo.interfaz; // O el paquete que estés usando

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
import colectivo.modelo.Recorrido;


/**
 * Aplicación que muestra un mapa ESTÁTICO de Google (una imagen)
 * con un recorrido específico, controles de navegación y leyenda.
 *
 * VM Arguments necesarios:
 * --add-modules javafx.controls
 */
public class VentanaMapa extends Application {

    // --- Componentes de UI ---
    private ImageView imageView;
    private VBox panelLeyenda;

    // --- Estado actual del mapa ---
    private double currentCenterLat = -42.7745;
    private double currentCenterLng = -65.0446;
    private int currentZoom = 13; // Zoom por defecto después del auto-ajuste
    
    // --- (NUEVO) Flag para auto-ajuste inicial ---
    private boolean isFirstLoad = true;
    
    // --- DATOS DEL RECORRIDO ---
    private List<Recorrido> recorridoParaMostrar;
    
    // --- Paleta de colores para las líneas ---
    private static final String[] COLORES_LINEA = {
        "0x0000FF", "0xFF0000", "0xFFA500", "0x800080", "0xA52A2A", "0x008000"
    };
    private static final String COLOR_CAMINANDO = "0x404040";
    
    // --- Mapa para construir la leyenda dinámicamente ---
    private Map<String, String> leyendaColores = new HashMap<>();


    /**
     * ¡¡¡IMPORTANTE!!!
     * Debes generar tu propia clave de API en Google Cloud Console
     * y activar la "Maps Static API".
     * Pega tu clave aquí abajo.
     */
    private static final String TU_CLAVE_DE_API_AQUI = "AIzaSyCiWk2rBTihKSwummyYVv6mTzc-lFQspQ0"; // <--- ¡PON TU CLAVE AQUÍ!
    
    // --- Parámetros del mapa estático ---
    private static final int MAP_WIDTH = 640;
    private static final int MAP_HEIGHT = 640;

    /**
     * Método para que la Interfaz principal nos pase los datos
     * ANTES de mostrar la ventana.
     * (Modificado para resetear el flag de carga)
     * @param recorrido La lista de tramos (Recorrido) a dibujar.
     */
    public void setRecorrido(List<Recorrido> recorrido) {
        this.recorridoParaMostrar = recorrido;
        
        // (NUEVO) Resetear el estado de carga cada vez que se pasa un nuevo recorrido
        this.isFirstLoad = true;
        
        // Establecer un centro/zoom por defecto MUY genérico
        // El cálculo real se hará en construirURL
        this.currentCenterLat = -42.7745;
        this.currentCenterLng = -65.0446;
        this.currentZoom = 13;
    }

    @Override
    public void start(Stage stage) {

        imageView = new ImageView();
        imageView.setPreserveRatio(true); // Mantener la proporción

        // --- Layout Principal (BorderPane) ---
        BorderPane root = new BorderPane();
        root.setCenter(imageView);
        
        // --- Panel Izquierdo: Leyenda ---
        panelLeyenda = new VBox(5);
        panelLeyenda.setPadding(new Insets(10));
        panelLeyenda.setAlignment(Pos.TOP_LEFT);
        root.setLeft(panelLeyenda);

        // --- Panel Derecho: Controles de Navegación ---
        Button zoomInButton = new Button("+");
        Button zoomOutButton = new Button("-");
        Button upButton = new Button("▲");
        Button downButton = new Button("▼");
        Button leftButton = new Button("◀");
        Button rightButton = new Button("▶");

        // Acciones de los botones
        zoomInButton.setOnAction(e -> changeZoom(1));
        zoomOutButton.setOnAction(e -> changeZoom(-1));
        upButton.setOnAction(e -> moveMap(0.005, 0));
        downButton.setOnAction(e -> moveMap(-0.005, 0));
        leftButton.setOnAction(e -> moveMap(0, -0.005));
        rightButton.setOnAction(e -> moveMap(0, 0.005));

        // Layout de los controles
        HBox zoomControls = new HBox(5, zoomInButton, zoomOutButton);
        zoomControls.setAlignment(Pos.CENTER);
        VBox navButtons = new VBox(5, upButton, new HBox(5, leftButton, rightButton), downButton);
        navButtons.setAlignment(Pos.CENTER);
        
        VBox allControls = new VBox(20, zoomControls, navButtons);
        allControls.setAlignment(Pos.CENTER);
        allControls.setPadding(new Insets(10));
        
        root.setRight(allControls); // Controles a la derecha

        // --- Cargar el mapa inicial ---
        updateMapImage();

        // --- Configuración del Stage ---
        Scene scene = new Scene(root, 900, 680); 
        stage.setTitle("Visor de Mapa del Recorrido");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Actualiza el panel de la leyenda (panelLeyenda)
     */
    private void actualizarLeyenda() {
        panelLeyenda.getChildren().clear();

        Label titulo = new Label("Referencias:");
        titulo.setStyle("-fx-font-weight: bold;");
        panelLeyenda.getChildren().add(titulo);

        for (Map.Entry<String, String> entry : leyendaColores.entrySet()) {
            String nombre = entry.getKey();
            // Asegurarse que el color tenga el formato #RRGGBB
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
     * Actualiza la imagen del mapa Y la leyenda.
     */
    private void updateMapImage() {
        // 1. construirURL() genera la URL y RELLENA 'leyendaColores'
        String url = construirURL();
        System.out.println("Cargando URL de Mapa: " + url); // Para depuración

        Image mapImage;
        if (TU_CLAVE_DE_API_AQUI.equals("AQUI_VA_TU_CLAVE_DE_API")) {
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
        
        // 2. Ahora que 'leyendaColores' está lleno, actualizamos el panel visual
        actualizarLeyenda();
    }
    
    // --- Métodos de navegación ---
    /**
     * Cambia el nivel de zoom y recarga el mapa.
     */
    private void changeZoom(int delta) {
        currentZoom += delta;
        if (currentZoom < 1) currentZoom = 1;
        if (currentZoom > 20) currentZoom = 20;
        updateMapImage(); // Recargar mapa y leyenda
    }

    /**
     * Mueve el centro del mapa y recarga la imagen.
     */
    private void moveMap(double latDelta, double lngDelta) {
        currentCenterLat += latDelta;
        currentCenterLng += lngDelta;
        updateMapImage(); // Recargar mapa y leyenda
    }


    /**
     * (MÉTODO REESCRITO)
     * Construye la URL. La primera vez, omite center/zoom para auto-ajustar.
     * Las veces siguientes, usa los valores manuales para pan/zoom.
     * TAMBIÉN RELLENA el mapa 'leyendaColores'.
     */
    private String construirURL() {
        // 1. Limpiar y rellenar leyenda con paradas
        leyendaColores.clear();
        // (NUEVO) Añadir referencias de paradas
        leyendaColores.put("Parada Origen", "0x008000"); // Verde (color:green)
        leyendaColores.put("Parada Destino", "0xFF0000"); // Rojo (color:red)

        String baseURL = "https://maps.googleapis.com/maps/api/staticmap";
        
        // Parámetros base
        StringBuilder urlParams = new StringBuilder();
        urlParams.append("?").append(String.format("size=%dx%d", MAP_WIDTH, MAP_HEIGHT));
        urlParams.append("&maptype=roadmap");
        urlParams.append("&format=png");
        urlParams.append("&key=").append(TU_CLAVE_DE_API_AQUI);

        if (recorridoParaMostrar == null || recorridoParaMostrar.isEmpty()) {
            return "https://via.placeholder.com/" + MAP_WIDTH + "x" + MAP_HEIGHT + ".png?text=No+se+paso+un+recorrido";
        }

        // 3. Recopilar todas las paradas
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

        // 4. (NUEVO) Lógica de Centro y Zoom
        if (isFirstLoad) {
            isFirstLoad = false; // Ya no es la primera carga
            
            // Calcular el punto medio para futuros paneos
            Parada origen = todasLasParadas.get(0);
            Parada destino = todasLasParadas.get(todasLasParadas.size() - 1);
            this.currentCenterLat = (origen.getLatitud() + destino.getLatitud()) / 2.0;
            this.currentCenterLng = (origen.getLongitud() + destino.getLongitud()) / 2.0;
            // No establecemos zoom, dejamos que Google lo haga
            
            // Omitir center y zoom en la URL para que Google Maps auto-ajuste
        } else {
            // Usar los valores manuales para pan/zoom
            urlParams.append("&").append(String.format("center=%f,%f", currentCenterLat, currentCenterLng));
            urlParams.append("&").append("zoom=").append(currentZoom);
        }
        
        // 5. Construir los Marcadores (markers=)
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

        // 6. Construir los Paths y rellenar leyenda
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

            if (r.getLinea() != null) { // Es un tramo en colectivo
                String codigoLinea = r.getLinea().getCodigo();
                nombreLeyenda = "Línea " + codigoLinea;
                
                if (!coloresDeLineaAsignados.containsKey(codigoLinea)) {
                    coloresDeLineaAsignados.put(codigoLinea, COLORES_LINEA[colorIndex % COLORES_LINEA.length]);
                    colorIndex++;
                }
                colorHex = coloresDeLineaAsignados.get(codigoLinea);
                
            } else { // Es un tramo caminando
                nombreLeyenda = "Caminando 🚶";
                colorHex = COLOR_CAMINANDO;
            }

            // Rellenar el mapa que usará la leyenda
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
        
        // 7. Unimos todo
        return baseURL + urlParams.toString() + pathParams.toString() + markerParams.toString();
    }


    public static void main(String[] args) {
        launch(args);
    }
}