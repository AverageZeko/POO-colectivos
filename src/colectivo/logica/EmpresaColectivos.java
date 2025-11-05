package colectivo.logica;

import java.util.Collections;
import java.util.Map;
import colectivo.modelo.Linea;
import colectivo.modelo.Parada;
import colectivo.modelo.Tramo;
import colectivo.servicio.LineaServicio;
import colectivo.servicio.LineaServicioImplementacion;
import colectivo.servicio.ParadaServicio;
import colectivo.servicio.ParadaServicioImplementacion;
import colectivo.servicio.TramoServicio;
import colectivo.servicio.TramoServicioImplementacion;

/**
 * Representa el sistema de transporte de una ciudad.
 * <p>
 * Esta clase actúa como un contenedor para todos los datos maestros de una ciudad específica,
 * incluyendo todas sus paradas, líneas y tramos. Carga estos datos al ser instanciada
 * a través de la capa de servicios y los expone a través de métodos de acceso.
 * </p>
 */
public class EmpresaColectivos {
	private	Map<Integer, Parada> paradas;
	private	Map<String, Linea> lineas;
	private	Map<String, Tramo> tramos;
    private LineaServicio lineaServicio;
    private ParadaServicio paradaServicio;
    private TramoServicio tramoServicio;

    /**
     * Construye una nueva instancia de EmpresaColectivos, inicializando los servicios
     * y cargando todos los datos de paradas, líneas y tramos para la ciudad activa.
     */
    public EmpresaColectivos() {
        super();
        paradaServicio = new ParadaServicioImplementacion();
        lineaServicio = new LineaServicioImplementacion();
        tramoServicio = new TramoServicioImplementacion();
        paradas = paradaServicio.buscarParadas();
        lineas = lineaServicio.buscarLineas();
        tramos = tramoServicio.buscarTramos();
    }

    /**
     * Obtiene una parada específica por su ID.
     * @param paradaId El ID de la parada a buscar.
     * @return El objeto {@link Parada}, o {@code null} si no se encuentra.
     */
    public Parada getParada(int paradaId) {
        return paradas.get(paradaId);
    }

    /**
     * Obtiene una línea específica por su código.
     * @param lineaId El código de la línea a buscar.
     * @return El objeto {@link Linea}, o {@code null} si no se encuentra.
     */
    public Linea getLinea(String lineaId) {
        return lineas.get(lineaId);
    }

    /**
     * Devuelve un mapa inmodificable de todas las líneas.
     * @return Un mapa con todas las líneas de la empresa.
     */
    public Map<String, Linea> getLineas() {
        return Collections.unmodifiableMap(lineas);
    }

    /**
     * Devuelve un mapa inmodificable de todas las paradas.
     * @return Un mapa con todas las paradas de la empresa.
     */
    public Map<Integer, Parada> getParadas() {
        return Collections.unmodifiableMap(paradas);
    }

    /**
     * Devuelve un mapa inmodificable de todos los tramos.
     * @return Un mapa con todos los tramos de la empresa.
     */
    public Map<String, Tramo> getTramos() {
        return Collections.unmodifiableMap(tramos);
    }

}