package colectivo.dao;

import java.util.Map;
import colectivo.modelo.Linea;

/**
 * Interfaz que define el contrato para el acceso a datos de las líneas de colectivo.
 * Las implementaciones de esta interfaz proporcionan los mecanismos para obtener los datos
 * de las líneas desde diferentes fuentes (por ejemplo, base de datos, archivos, etc.).
 */
public interface LineaDAO {
    
    /**
     * Busca y devuelve todas las líneas de colectivo disponibles en la fuente de datos.
     *
     * @return un {@link Map} que contiene todas las líneas, donde la clave es el código
     *         de la línea (String) y el valor es el objeto {@link Linea} correspondiente.
     */
    Map<String, Linea> buscarTodos();
}