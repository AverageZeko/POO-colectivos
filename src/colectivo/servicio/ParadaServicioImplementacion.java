package colectivo.servicio;

import java.util.Map;

import colectivo.controlador.Constantes;
import colectivo.dao.ParadaDAO;
import colectivo.modelo.Parada;
import colectivo.util.Factory;

/**
 * Implementación del servicio {@link ParadaServicio} para las paradas de colectivos.
 * <p>Esta clase actúa como intermediario entre la lógica de negocio y el acceso a los datos
 * de paradas, utilizando el patrón DAO y la factoría para obtener la instancia correspondiente.</p>
 *
 * <p>Proporciona métodos para consultar las paradas disponibles en el sistema.</p>
 *
 */
public class ParadaServicioImplementacion implements ParadaServicio {
    private ParadaDAO paradaDAO;

    /**
     * Constructor que inicializa la instancia de ParadaDAO con una referencia al objeto que es devuelto por
     * {@link colectivo.util.Factory#getInstancia(String)}
     *
     * <p>Obtiene la instancia de {@link ParadaDAO} configurada en el sistema.</p>
     */
    public ParadaServicioImplementacion() {
        paradaDAO = (ParadaDAO) Factory.getInstancia(Constantes.PARADA);
    }

    /**
     * Devuelve todas las paradas de colectivos disponibles en el sistema.
     *
     * <p>Consulta el DAO correspondiente y retorna el mapa de paradas indexadas por su identificador.</p>
     *
     * @return {@link Map} de paradas disponibles.
     */
    @Override
    public Map<Integer, Parada> buscarParadas() {
        return paradaDAO.buscarTodos();
    }
}