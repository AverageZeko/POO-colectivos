package colectivo.servicio;

import java.util.Map;
import colectivo.modelo.Tramo;

/**
 * Interfaz que define el contrato para los servicios relacionados con los tramos.
 * <p>
 * Un tramo es un segmento de ruta entre dos paradas. Esta capa de servicio
 * actúa como intermediario entre la lógica de la aplicación y la capa de acceso
 * a datos (DAO) para los tramos.
 * </p>
 */
public interface TramoServicio {
    /**
     * Obtiene todos los tramos disponibles en el sistema.
     *
     * @return un {@link Map} que contiene todos los tramos, donde la clave es un
     *         identificador único del tramo (ej. "inicio-&gt;fin") y el valor es el
     *         objeto {@link Tramo} correspondiente.
     */
    Map<String, Tramo> buscarTramos();
}