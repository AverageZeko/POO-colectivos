package colectivo.logica;

import java.time.LocalTime;
import java.util.*;

import colectivo.modelo.Linea;

public class CacheLineas {

    private final Map<String, List<LocalTime>> cacheFrecuencias = new HashMap<>();
    private final Map<String, int[]> cachePrefixSegundos = new HashMap<>();

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