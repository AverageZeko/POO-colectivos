package colectivo.servicio;

import java.util.Map;
import colectivo.modelo.Linea;

/**
 * Interfaz que define el contrato para los servicios relacionados con las líneas de colectivo.
 * <p>
 * Esta capa de servicio actúa como intermediario entre la lógica de la aplicación
 * (por ejemplo, el coordinador) y la capa de acceso a datos (DAO).
 * </p>
 */
public interface LineaServicio {
    /**
     * Obtiene todas las líneas de colectivo disponibles en el sistema.
     *
     * @return un {@link Map} que contiene todas las líneas, donde la clave es el código
     *         de la línea y el valor es el objeto {@link Linea} correspondiente.
     */
    Map<String, Linea> buscarLineas();
}