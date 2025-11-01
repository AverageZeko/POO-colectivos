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
 * Singleton que representa la empresa de colectivos y centraliza el acceso a los datos principales.
 *
 * <p>Esta clase gestiona las colecciones de paradas, líneas y tramos, obtenidas a través de los servicios
 * correspondientes. Proporciona métodos para consultar y acceder a estos datos de manera segura y eficiente.</p>
 *
 * <p>Utiliza el patrón Singleton para asegurar que solo exista una instancia de la empresa en toda la aplicación.
 * Los datos se cargan al inicializar la instancia y se exponen mediante métodos de consulta y acceso.</p>
 *
 */
public class EmpresaColectivos {
	private	Map<Integer, Parada> paradas;
	private	Map<String, Linea> lineas;
	private	Map<String, Tramo> tramos;
    private LineaServicio lineaServicio;
    private ParadaServicio paradaServicio;
    private TramoServicio tramoServicio;

    /**
     * Constructor privado que inicializa los servicios y carga los datos de paradas, líneas y tramos.
     * <p>Este método se invoca solo una vez al crear la instancia Singleton.</p>
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

    public Parada getParada(int paradaId) {
        return paradas.get(paradaId);
    }

    public Linea getLinea(String lineaId) {
        return lineas.get(lineaId);
    }

    public Map<String, Linea> getLineas() {
        return Collections.unmodifiableMap(lineas);
    }

    public Map<Integer, Parada> getParadas() {
        return Collections.unmodifiableMap(paradas);
    }

    public Map<String, Tramo> getTramos() {
        return Collections.unmodifiableMap(tramos);
    }

}
