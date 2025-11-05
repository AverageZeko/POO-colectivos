package colectivo.servicio;

import java.util.Map;

import colectivo.controlador.Constantes;
import colectivo.dao.LineaDAO;
import colectivo.modelo.Linea;
import colectivo.util.Factory;

/**
 * Implementación del servicio {@link LineaServicio} para las líneas de colectivos.
 *
 * <p>Esta clase actúa como intermediario entre la lógica de negocio y el acceso a los datos
 * de líneas, utilizando el patrón DAO y Factory para obtener la instancia correspondiente.</p>
 *
 * <p>Proporciona métodos para consultar las líneas disponibles en el sistema.</p>
 */
public class LineaServicioImplementacion implements LineaServicio {
    private LineaDAO lineaDAO;

    /**
     * Constructor que inicializa la instancia de LineaDAO con una referencia al objeto que es devuelto por
     * {@link colectivo.util.Factory#getInstancia(String)}
     *
     * <p>Obtiene la instancia de {@link LineaDAO} configurada en el sistema.</p>
     */
    public LineaServicioImplementacion() {
        lineaDAO = (LineaDAO) Factory.getInstancia(Constantes.LINEA);
    }

    /**
     * Devuelve todas las líneas de colectivos disponibles en el sistema.
     * <p>Consulta el DAO correspondiente y retorna el mapa de líneas indexadas por su identificador.</p>
     *
     * @return {@link Map} de líneas disponibles.
     */
    @Override
    public Map<String, Linea> buscarLineas() {
        return lineaDAO.buscarTodos();
    }
    
}