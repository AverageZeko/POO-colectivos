/**
 * GestorDeVentanas es el punto central de la capa de interfaz gráfica.
 * Se encarga de la comunicación con el Coordinador y de gestionar las ventanas principales de la aplicación.
 * La interfaz solo recibe y renderiza strings o metadatos simples (IDs/nombres).
 */
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

public class GestorDeVentanas implements Mostrable {

    /** Coordinador principal de la aplicación. */
    private Coordinador coordinador;

    /** Ventana de inicio de la aplicación. */
    private VentanaInicio ventanaInicio;

    /** Ventana principal de consultas. */
    private Interfaz ventanaConsultas;

    /** Ventana de visualización de mapas. */
    private VentanaMapa ventanaMapa;

    /** Logger para mensajes de consulta. */
    private static final Logger LOG = LoggerFactory.getLogger("Consulta");

    /**
     * Crea el gestor de ventanas e inicializa las ventanas principales.
     */
    public GestorDeVentanas() {
        this.ventanaInicio = new VentanaInicio();
        this.ventanaConsultas = new Interfaz();
        this.ventanaMapa = new VentanaMapa();
    }

    /**
     * Asocia el coordinador y lo inyecta en las ventanas para permitir la comunicación.
     * @param coordinador Coordinador principal de la aplicación.
     */
    public void setCoordinador(Coordinador coordinador) {
        this.coordinador = coordinador;
        this.ventanaInicio.setGestor(this);
        this.ventanaConsultas.setGestor(this);
        this.ventanaMapa.setGestor(this);
    }

    /**
     * Muestra la ventana de inicio de la aplicación.
     * @param args Argumentos de la línea de comandos.
     */
    public void mostrar(String[] args) {
        ventanaInicio.lanzarAplicacion(args);
    }

    /**
     * Procesa la selección de localización y ciudad en la ventana de inicio.
     * @param localeSeleccionado Localización seleccionada.
     * @param seleccionCiudad Ciudad seleccionada.
     * @param ventanaActual Ventana actual de la UI.
     */
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

    /**
     * Muestra la ventana de inicio y cierra la ventana de consultas.
     * @param ventanaActual Ventana actual de la UI.
     */
    public void mostrarVentanaInicio(Stage ventanaActual) {
        ventanaConsultas.cerrar(ventanaActual);
        ventanaInicio.mostrar(new Stage());
    }

    /**
     * Muestra la ventana de consultas y cierra la ventana de inicio.
     * @param ventanaActual Ventana actual de la UI.
     */
    public void mostrarVentanaConsultas(Stage ventanaActual) {
        ventanaInicio.cerrar(ventanaActual);
        ventanaConsultas.start(new Stage());
    }
    
    /**
     * Muestra la ventana de mapa para el recorrido especificado.
     * @param recorrido Índice del recorrido a mostrar en el mapa.
     */
    public void mostrarVentanaMapa(int recorrido) {
        try {
            ventanaMapa.setRecorrido(recorrido);
            ventanaMapa.start(new Stage());
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("Error al lanzar VentanaMapa", e);
        }
    }

    /**
     * Solicita volver a la ventana de inicio y limpia la caché de ciudades.
     * @param ventanaActual Ventana actual de la UI.
     */
    public void solicitarVolverAInicio(Stage ventanaActual) {
        coordinador.limpiarCacheCiudades();
        mostrarVentanaInicio(ventanaActual);
    }
    
    
    
    

    /**
     * Solicita una consulta usando IDs de paradas, ocultando objetos de dominio a la UI.
     * @param idOrigen ID de la parada de origen.
     * @param idDestino ID de la parada de destino.
     * @param diaSemana Día de la semana (1=Lunes, ..., 7=Domingo).
     * @param hora Hora de la consulta.
     * @return Lista de páginas de resultados formateados.
     */
    public java.util.List<java.util.List<String>> solicitarConsulta(int idOrigen, int idDestino, int diaSemana, LocalTime hora) {
        Parada origen = coordinador.getParada(idOrigen);
        Parada destino = coordinador.getParada(idDestino);
        return coordinador.consulta(origen, destino, diaSemana, hora);
    }

    /**
     * Devuelve un mapa id->nombre(dirección) para poblar la UI.
     * @return Mapa de IDs de parada a nombres/direcciones.
     */
    public Map<Integer, String> getMapaParadasNombres() {
        Map<Integer, String> nombres = coordinador.getMapaParadasNombres();
        if (nombres == null || nombres.isEmpty()) {
            throw new IllegalStateException("No hay nombres de paradas disponibles desde el Coordinador");
        }
        return nombres;
    }

    /**
     * Solicita la URL del mapa desde VentanaMapa al Coordinador.
     * @param zoomDelta Delta de zoom.
     * @param latDelta Delta de latitud.
     * @param lngDelta Delta de longitud.
     * @param ruta Índice de la ruta.
     * @return Mapa con el link y la leyenda del mapa.
     */
    public Map<String, Object> solicitarMapa(int zoomDelta, double latDelta, double lngDelta, int ruta) {
        if (coordinador == null) {
            LOG.error("Gestor no tiene coordinador, no se puede pedir mapa.");
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("link", "https://via.placeholder.com/640x640.png?text=Error:+ArmadorLink+nulo");
            errorMap.put("leyenda", new HashMap<String, String>());
            return errorMap;
        }
        return coordinador.obtenerLink(zoomDelta, latDelta, lngDelta, ruta);
    }

    /**
     * Devuelve el ResourceBundle actual para internacionalización.
     * @return ResourceBundle actual.
     */
    public ResourceBundle getBundle() {
        return coordinador.getBundle();
    }

    /**
     * Descubre las localizaciones disponibles en la aplicación.
     * @return Lista de objetos LocaleInfo.
     */
    public java.util.List<colectivo.util.LocaleInfo> descubrirLocalizaciones() {
        return coordinador.descubrirLocalizaciones();
    }

    /**
     * Devuelve la localización actual.
     * @return LocaleInfo actual.
     */
    public LocaleInfo getLocale() {
        return coordinador.getLocale();
    }

    /**
     * Establece la localización actual.
     * @param localeInfo Localización a establecer.
     */
    public void setLocalizacion(LocaleInfo localeInfo) {
        coordinador.setLocalizacion(localeInfo);
    }

    /**
     * Devuelve la lista de ciudades disponibles.
     * @return Lista de nombres de ciudades.
     */
    public java.util.List<String> getCiudades() {
        return coordinador.getCiudades();
    }
}