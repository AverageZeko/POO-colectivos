package colectivo.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import colectivo.modelo.Parada;
import colectivo.logica.Recorrido;

/**
 * Clase de lógica que maneja el estado del mapa y construye la URL
 * de la API de Google Static Maps.
 *
 * Mantiene el estado interno (zoom, centro) y lo actualiza a través
 * de su ÚNICO método público: generarMapa().
 */
public class ArmadorLinkMapa {

    // --- Estado Interno del Mapa ---
    private double currentCenterLat = -42.7745;
    private double currentCenterLng = -65.0446;
    private int currentZoom = 13;
    private boolean isFirstLoad = true;

    // --- Datos del Recorrido ---
    private List<List<Recorrido>> todasLasRutas;
    private List<Recorrido> recorridoParaMostrar;
    private Map<String, String> leyendaColores = new HashMap<>();

    // --- Constantes ---
    private final String apiKey;
    private static final int MAP_WIDTH = 640;
    private static final int MAP_HEIGHT = 640;
    private static final String[] COLORES_LINEA = {
        "0x0000FF", "0xFF0000", "0xFFA500", "0x800080", "0xA52A2A", "0x008000"
    };
    private static final String COLOR_CAMINANDO = "0x404040";

    /**
     * DTO (Data Transfer Object) público para devolver la URL y la leyenda juntas.
     */
    public static class ResultadoMapa {
        private final String url;
        private final Map<String, String> leyenda;

        public ResultadoMapa(String url, Map<String, String> leyenda) {
            this.url = url;
            this.leyenda = new HashMap<>(leyenda); 
        }
        
        public String getUrl() { return url; }
        public Map<String, String> getLeyenda() { return leyenda; }
    }

    /**
     * Constructor.
     * @param apiKey Tu clave de API de Google Maps.
     * @param recorrido El recorrido inicial a mostrar.
     */
    public ArmadorLinkMapa(String apiKey) {
        this.apiKey = apiKey;
        
    }
    
    public void setRutas(List<List<Recorrido>> todasLasRutas) {
    	this.todasLasRutas = todasLasRutas;
    }

    // =========================================================================
    // --- ÚNICO MÉTODO PÚBLICO ---
    // =========================================================================

    /**
     * Genera un nuevo resultado de mapa aplicando los cambios de
     * zoom y localización (deltas) al estado actual.
     *
     * @param zoomDelta Cambio en el zoom (+1, -1, o 0)
     * @param latDelta  Cambio en la latitud (ej. 0.005, -0.005, o 0)
     * @param lngDelta  Cambio en la longitud (ej. 0.005, -0.005, o 0)
     * @return Un nuevo ResultadoMapa con la URL y la leyenda actualizadas.
     */
    public ResultadoMapa generarMapa(int zoomDelta, double latDelta, double lngDelta, int recorrido) {
        recorridoParaMostrar = todasLasRutas.get(recorrido);
        // 1. Actualizar el estado interno
        currentZoom += zoomDelta;
        if (currentZoom < 1) currentZoom = 1;
        if (currentZoom > 20) currentZoom = 20;

        currentCenterLat += latDelta;
        currentCenterLng += lngDelta;
        
        // 2. Generar y devolver el resultado
        String url = construirURL();
        return new ResultadoMapa(url, this.leyendaColores);
    }


    // =========================================================================
    // --- MÉTODOS PRIVADOS ---
    // =========================================================================

    /**
     * Construye la URL para la solicitud a la API de Google Maps Static.
     * Utiliza el estado interno (currentZoom, etc.) para generar la URL.
     *
     * @return La URL completa (o una URL de placeholder en caso de error).
     */
    private String construirURL() {
        leyendaColores.clear();
        leyendaColores.put("Parada Origen", "0x008000");
        leyendaColores.put("Parada Destino", "0xFF0000");

        // --- Verificaciones de Errores ---
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("ERROR: Falta la clave de API de Google Maps.");
            return "https://via.placeholder.com/" + MAP_WIDTH + "x" + MAP_HEIGHT + ".png?text=ERROR:+FALTA+API+KEY";
        }
        
        if (recorridoParaMostrar == null || recorridoParaMostrar.isEmpty()) {
            return "https://via.placeholder.com/" + MAP_WIDTH + "x" + MAP_HEIGHT + ".png?text=No+se+paso+un+recorrido";
        }
        
        // --- Base de la URL ---
        String baseURL = "https://maps.googleapis.com/maps/api/staticmap";
        StringBuilder urlParams = new StringBuilder();
        urlParams.append("?").append(String.format("size=%dx%d", MAP_WIDTH, MAP_HEIGHT));
        urlParams.append("&maptype=roadmap");
        urlParams.append("&format=png");
        urlParams.append("&key=").append(apiKey);

        // --- Lógica de Paradas y Centrado ---
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
        
        // --- Marcadores (Markers) ---
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

        // --- Trazado de Rutas (Paths) ---
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
        
        // --- Ensamblaje Final y Verificación de Longitud ---
        String urlFinal = baseURL + urlParams.toString() + pathParams.toString() + markerParams.toString();
        
        if (urlFinal.length() > 8192) {
             System.err.println("ERROR: La URL del mapa es demasiado larga (" + urlFinal.length() + " caracteres).");
             return "https://via.placeholder.com/" + MAP_WIDTH + "x" + MAP_HEIGHT + ".png?text=ERROR:+RECORRIDO+MUY+LARGO";
        }
        
        return urlFinal;
    }
}