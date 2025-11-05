package colectivo.logica;

import java.time.LocalTime;
import java.util.*;

import colectivo.modelo.Linea;

/**
 * Gestiona una caché en memoria para optimizar los cálculos de recorridos.
 * <p>
 * Almacena dos tipos de datos:
 * <ul>
 *     <li><b>Sumas de prefijos de tiempos:</b> Para cada línea, guarda un arreglo con el tiempo acumulado en segundos desde el inicio de la ruta hasta cada parada. Esto permite calcular la duración de un tramo entre dos paradas con una simple resta.</li>
 *     <li><b>Frecuencias ordenadas:</b> Para cada línea y día de la semana, guarda una lista ordenada de los horarios de salida, lo que permite búsquedas binarias eficientes.</li>
 * </ul>
 * Esta caché es interna a la capa de lógica y ayuda a evitar recálculos costosos.
 * </p>
 */
public class CacheLineas {

    private final Map<String, List<LocalTime>> cacheFrecuencias = new HashMap<>();
    private final Map<String, int[]> cachePrefixSegundos = new HashMap<>();

    /**
     * Obtiene (o calcula y cachea) el arreglo de sumas de prefijos de tiempos para una línea.
     * El arreglo contiene el tiempo acumulado en segundos desde el inicio de la línea hasta cada parada.
     *
     * @param linea La línea para la cual se calcula el prefijo.
     * @param paradas La lista ordenada de paradas de la línea.
     * @param tramos Un mapa con todos los tramos disponibles para buscar los tiempos.
     * @return Un arreglo de enteros con los tiempos acumulados, o {@code null} si un tramo no se encuentra.
     */
    public int[] obtenerPrefix(Linea linea, List<colectivo.modelo.Parada> paradas, Map<String, colectivo.modelo.Tramo> tramos) {
        if (linea == null || paradas == null) return null;
        String key = linea.getCodigo();
        int[] prefix = cachePrefixSegundos.get(key);
        if (prefix != null) return prefix;

        int n = paradas.size();
        int[] p = new int[n];
        int acum = 0;
        p[0] = 0;
        for (int i = 0; i < n - 1; i++) {
            String clave = paradas.get(i).getCodigo() + "->" + paradas.get(i + 1).getCodigo();
            colectivo.modelo.Tramo tramo = tramos.get(clave);
            if (tramo == null) return null;
            acum += tramo.getTiempo();
            p[i + 1] = acum;
        }
        cachePrefixSegundos.put(key, p);
        return p;
    }

    /**
     * Obtiene (o calcula y cachea) la lista ordenada de frecuencias (horarios de salida) para una línea y día de la semana específicos.
     *
     * @param linea La línea de la cual obtener las frecuencias.
     * @param diaSemana El día de la semana para filtrar las frecuencias.
     * @return Una lista ordenada e inmutable de {@link LocalTime} con los horarios. Retorna una lista vacía si no hay frecuencias.
     */
    public List<LocalTime> obtenerFrecuenciasOrdenadas(Linea linea, int diaSemana) {
        if (linea == null) return List.of();
        String key = linea.getCodigo() + "#" + diaSemana;
        List<LocalTime> cached = cacheFrecuencias.get(key);
        if (cached != null) return cached;

        List<LocalTime> frecuencias = linea.getHorasFrecuenciaPorDia(diaSemana);
        if (frecuencias == null || frecuencias.isEmpty()) {
            cacheFrecuencias.put(key, List.of());
            return List.of();
        }
        List<LocalTime> copia = new ArrayList<>(frecuencias);
        copia.sort(Comparator.naturalOrder());
        cacheFrecuencias.put(key, copia);
        return copia;
    }
}