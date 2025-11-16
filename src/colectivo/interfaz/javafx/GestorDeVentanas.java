package colectivo.interfaz.javafx;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import colectivo.controlador.Coordinador;
import colectivo.interfaz.Mostrable;
import colectivo.modelo.Parada;
import colectivo.util.LocaleInfo;

import javafx.stage.Stage;

/**
 * GestorDeVentanas: Es la única clase de la capa de interfaz que comunica con el Coordinador.
 * La interfaz recibe y renderiza solamente strings o metadatos simples (IDs/nombres).
 */
public class GestorDeVentanas implements Mostrable{

    private Coordinador coordinador;
    private VentanaInicio ventanaInicio;
    private Interfaz ventanaConsultas;
    
    private VentanaMapa ventanaMapa;

    private static final Logger LOG = LoggerFactory.getLogger("Consulta");

    public GestorDeVentanas() {
        this.ventanaInicio = new VentanaInicio();
        this.ventanaConsultas = new Interfaz();
        this.ventanaMapa = new VentanaMapa();
    }

    public void setCoordinador(Coordinador coordinador) {
        this.coordinador = coordinador;
        // Inyecta el gestor en las ventanas para que puedan comunicarse hacia arriba.
        this.ventanaInicio.setGestor(this);
        this.ventanaConsultas.setGestor(this);
        this.ventanaMapa.setGestor(this);
    }

    public void mostrar(String[] args){
        ventanaInicio.lanzarAplicacion(args);
    }

    public void procesarInicio(LocaleInfo localeSeleccionado, String seleccionCiudad, Stage ventanaActual) {
        if (localeSeleccionado == null || seleccionCiudad == null || seleccionCiudad.isEmpty()) {
            ResourceBundle bundle = coordinador.getBundle();
            ventanaInicio.mostrarAdvertencia(bundle.getString("Welcome_Warning"));
            return;
        }

        LOG.info("Usuario seleccionó localización='{}' y ciudad='{}'", localeSeleccionado.codigoCompleto(), seleccionCiudad);

        coordinador.setLocalizacion(localeSeleccionado);
        coordinador.setCiudadActual(seleccionCiudad);

        mostrarVentanaConsultas(ventanaActual);
    }

    public void mostrarVentanaInicio(Stage ventanaActual) {
        ventanaConsultas.cerrar(ventanaActual);
        ventanaInicio.mostrar(new Stage());
    }

    public void mostrarVentanaConsultas(Stage ventanaActual) {
        ventanaInicio.cerrar(ventanaActual);
        ventanaConsultas.start(new Stage());
    }
    
    public void mostrarVentanaMapa(int recorrido) {
        try {
            // Preparamos la ventana con el índice del recorrido
            ventanaMapa.setRecorrido(recorrido);
            // Lanzamos la ventana (VentanaMapa.start() será llamada)
            ventanaMapa.start(new Stage());
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("Error al lanzar VentanaMapa", e);
        }
    }

    public void solicitarVolverAInicio(Stage ventanaActual) {
        coordinador.limpiarCacheCiudades();
        mostrarVentanaInicio(ventanaActual);
    }
    
    
    
    

    // MÉTODOS DE FACHADA

    /**
     * Solicita una consulta usando IDs de paradas, ocultando objetos de dominio a la UI.
     */
    public java.util.List<java.util.List<String>> solicitarConsulta(int idOrigen, int idDestino, int diaSemana, LocalTime hora) {
        Parada origen = coordinador.getParada(idOrigen);
        Parada destino = coordinador.getParada(idDestino);
        return coordinador.consulta(origen, destino, diaSemana, hora);
    }

    /**
     * Devuelve un mapa id->nombre(dirección) para poblar la UI.
     */
    public Map<Integer, String> getMapaParadasNombres() {
        Map<Integer, String> nombres = coordinador.getMapaParadasNombres();
        if (nombres == null || nombres.isEmpty()) {
            throw new IllegalStateException("No hay nombres de paradas disponibles desde el Coordinador");
        }
        return nombres;
    }
    
    /**
     * Pasa la solicitud de URL del mapa desde VentanaMapa al Coordinador.
     */
    public Map<String, Object> solicitarMapa(int zoomDelta, double latDelta, double lngDelta, int ruta) {
        if (coordinador == null) {
             System.err.println("Gestor no tiene coordinador, no se puede pedir mapa.");
          // --- CAMBIO 3: Crear un mapa de error válido ---
             Map<String, Object> errorMap = new HashMap<>();
             errorMap.put("link", "https://via.placeholder.com/640x640.png?text=Error:+ArmadorLink+nulo");
             errorMap.put("leyenda", new HashMap<String, String>());
             
             return errorMap;
             
        }
        return coordinador.obtenerLink(zoomDelta, latDelta, lngDelta, ruta);
    }
    

    public ResourceBundle getBundle() {
        return coordinador.getBundle();
    }

    public java.util.List<colectivo.util.LocaleInfo> descubrirLocalizaciones() {
        return coordinador.descubrirLocalizaciones();
    }

    public LocaleInfo getLocale() {
        return coordinador.getLocale();
    }

    public void setLocalizacion(LocaleInfo localeInfo) {
        coordinador.setLocalizacion(localeInfo);
    }

    public java.util.List<String> getCiudades() {
        return coordinador.getCiudades();
    }
}