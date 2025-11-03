package colectivo.servicio;

import java.util.Map;

import colectivo.controlador.Constantes;
import colectivo.dao.TramoDAO;
import colectivo.modelo.Tramo;
import colectivo.util.Factory;

/**
 * Implementación del servicio {@link TramoServicio} para los tramos de colectivos.
 * <p>Esta clase actúa como intermediario entre la lógica de negocio y el acceso a los datos
 * de tramos, utilizando el patrón DAO y la factoría para obtener la instancia correspondiente.</p>
 *
 * <p>Proporciona métodos para consultar los tramos disponibles en el sistema.</p>
 */
public class TramoServicioImplementacion implements TramoServicio {
    private TramoDAO tramoDAO;

    /**
     * Constructor que inicializa la instancia de TramoDAO con una referencia al objeto que es devuelto por
     * {@link colectivo.util.Factory#getInstancia(String)}
     *
     * <p>Obtiene la instancia de {@link TramoDAO} configurada en el sistema.</p>
     */
    public TramoServicioImplementacion() {
        tramoDAO = (TramoDAO) Factory.getInstancia(Constantes.TRAMO);
    }
    

    /**
     * Devuelve todos los tramos de colectivos disponibles en el sistema.
     *
     * <p>Consulta el DAO correspondiente y retorna el mapa de tramos indexados por su identificador.</p>
     *
     * @return {@link Map} de tramos disponibles.
     */
    @Override
    public Map<String, Tramo> buscarTramos() {
        return tramoDAO.buscarTodos();
    }
    
}
