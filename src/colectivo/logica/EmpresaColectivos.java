package colectivo.logica;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import colectivo.datos.CargarDatos;
import colectivo.datos.CargarParametros;
import colectivo.modelo.Linea;
import colectivo.modelo.Parada;
import colectivo.modelo.Tramo;

public class EmpresaColectivos {
    private static EmpresaColectivos empresa = null;
	private	Map<Integer, Parada> paradas;
	private	Map<String, Linea> lineas;
	private	Map<String, Tramo> tramos;


    public static EmpresaColectivos getEmpresa() {
        if (empresa == null) {
            empresa = new EmpresaColectivos();
        }

        return empresa;
    }


    private EmpresaColectivos() {
        super();
        try {
            CargarParametros.parametros();
            paradas = CargarDatos.cargarParadas(CargarParametros.getArchivoParada());
            lineas = CargarDatos.cargarLineas(CargarParametros.getArchivoLinea(),
                        CargarParametros.getArchivoFrecuencia(), paradas);
            tramos = CargarDatos.cargarTramos(CargarParametros.getArchivoTramo(), paradas);
        } catch (IOException e) {
            throw new IllegalStateException("No se puedo inicializar objeto EmpresaColectivos", e);
        }

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
