package colectivo.dao;

import java.util.Map;
import colectivo.modelo.Tramo;

/**
 * Interfaz que define el contrato para el acceso a datos de los tramos.
 * Un tramo representa un segmento entre dos paradas. Las implementaciones de esta
 * interfaz proporcionan los mecanismos para obtener los datos de los tramos desde
 * diferentes fuentes (por ejemplo, base de datos, archivos, etc.).
 */
public interface TramoDAO {
    
    /**
     * Busca y devuelve todos los tramos disponibles en la fuente de datos.
     *
     * @return un {@link Map} que contiene todos los tramos, donde la clave es una
     *         representación única del tramo (por ejemplo, "codigoInicio->codigoFin")
     *         y el valor es el objeto {@link Tramo} correspondiente.
     */
    Map<String, Tramo> buscarTodos();
}