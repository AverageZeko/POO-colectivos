package colectivo.servicio;

import java.util.Map;
import colectivo.modelo.Parada;

/**
 * Interfaz que define el contrato para los servicios relacionados con las paradas de colectivo.
 * <p>
 * Esta capa de servicio actúa como intermediario entre la lógica de la aplicación
 * y la capa de acceso a datos (DAO) para las paradas.
 * </p>
 */
public interface ParadaServicio {
    /**
     * Obtiene todas las paradas de colectivo disponibles en el sistema.
     *
     * @return un {@link Map} que contiene todas las paradas, donde la clave es el código
     *         de la parada y el valor es el objeto {@link Parada} correspondiente.
     */
    Map<Integer, Parada> buscarParadas();
}