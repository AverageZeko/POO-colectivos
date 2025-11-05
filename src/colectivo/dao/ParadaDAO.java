package colectivo.dao;

import java.util.Map;
import colectivo.modelo.Parada;

/**
 * Interfaz que define el contrato para el acceso a datos de las paradas de colectivo.
 * Las implementaciones de esta interfaz proporcionan los mecanismos para obtener los datos
 * de las paradas desde diferentes fuentes (por ejemplo, base de datos, archivos, etc.).
 */
public interface ParadaDAO {
    
    /**
     * Busca y devuelve todas las paradas de colectivo disponibles en la fuente de datos.
     *
     * @return un {@link Map} que contiene todas las paradas, donde la clave es el código
     *         de la parada (Integer) y el valor es el objeto {@link Parada} correspondiente.
     */
    Map<Integer, Parada> buscarTodos();
}