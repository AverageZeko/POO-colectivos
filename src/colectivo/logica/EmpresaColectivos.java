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

public class EmpresaColectivos {
    private static EmpresaColectivos empresa = null;
	private	Map<Integer, Parada> paradas;
	private	Map<String, Linea> lineas;
	private	Map<String, Tramo> tramos;
    private LineaServicio lineaServicio;
    private ParadaServicio paradaServicio;
    private TramoServicio tramoServicio;

    public static EmpresaColectivos getEmpresa() {
        if (empresa == null) {
            empresa = new EmpresaColectivos();
        }

        return empresa;
    }


    private EmpresaColectivos() {
        super();
        paradaServicio = new ParadaServicioImplementacion();
        lineaServicio = new LineaServicioImplementacion();
        tramoServicio = new TramoServicioImplementacion();
        paradas = paradaServicio.buscarParadas();
        lineas = lineaServicio.buscarLineas(paradas);
        tramos = tramoServicio.buscarTramos(paradas);
    }

    public void agregarParada(Parada parada) {

    }

    public void agregarLinea(Linea linea) {

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
