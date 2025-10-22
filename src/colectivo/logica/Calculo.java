package colectivo.logica;

import java.time.LocalTime;
import java.util.*;

import colectivo.modelo.Linea;
import colectivo.modelo.Parada;
import colectivo.modelo.Recorrido;
import colectivo.modelo.Tramo;
import colectivo.util.CacheLineas;
import colectivo.util.RecorridoUtils;

/**
 * Clase encargada de calcular recorridos directos e indirectos entre paradas.
 * Refactorizada para usar CacheLineas y RecorridoUtils.
 */
public class Calculo {

    private final CacheLineas cache = new CacheLineas();

    public Calculo() {
    }

    public List<List<Recorrido>> calcularRecorrido(
            Parada origen,
            Parada destino,
            int diaSemana,
            LocalTime horaLlegaParada,
            Map<String, Tramo> tramos) {

        List<List<Recorrido>> resultado = new ArrayList<>();
        if (origen == null || destino == null || horaLlegaParada == null || tramos == null) return resultado;

        // ---- RECORRIDOS DIRECTOS ----
        for (Linea linea : origen.getLineas()) {
            if (linea == null) continue;
            List<Parada> paradas = linea.getParadas();
            if (paradas == null) continue;

            int idxOrigen = paradas.indexOf(origen);
            int idxDestino = paradas.indexOf(destino);
            if (!RecorridoUtils.indicesValidos(idxOrigen, idxDestino)) continue;

            int[] prefix = cache.obtenerPrefix(linea, paradas, tramos);
            if (prefix == null) continue;

            int offset = prefix[idxOrigen];
            int duracion = prefix[idxDestino] - prefix[idxOrigen];

            List<LocalTime> frecuencias = cache.obtenerFrecuenciasOrdenadas(linea, diaSemana);
            LocalTime horaSalida = RecorridoUtils.calcularHoraSalidaBinaria(frecuencias, offset, horaLlegaParada);
            if (horaSalida != null) {
                Recorrido r = new Recorrido(linea, RecorridoUtils.subRecorrido(paradas, idxOrigen, idxDestino), horaSalida, duracion);
                resultado.add(List.of(r));
            }
        }

        // ---- CONEXIONES (1 transbordo) ----
        if (resultado.isEmpty()) {
            resultado.addAll(conexion(origen, destino, diaSemana, horaLlegaParada, tramos));
        }

        return resultado;
    }

    private List<List<Recorrido>> conexion(
            Parada origen,
            Parada destino,
            int diaSemana,
            LocalTime horaLlegaParada,
            Map<String, Tramo> tramos) {

        List<List<Recorrido>> resultado = new ArrayList<>();
        if (origen == null || destino == null) return resultado;

        Set<String> conexionesYaEncontradas = new HashSet<>();

        for (Linea lineaPrimerTramo : origen.getLineas()) {
            if (lineaPrimerTramo == null) continue;
            List<Parada> paradasPrimer = lineaPrimerTramo.getParadas();
            if (paradasPrimer == null) continue;

            int idxOrigen = paradasPrimer.indexOf(origen);
            if (idxOrigen < 0) continue;

            int[] prefixPrimer = cache.obtenerPrefix(lineaPrimerTramo, paradasPrimer, tramos);
            if (prefixPrimer == null) continue;

            for (int i = idxOrigen + 1; i < paradasPrimer.size(); i++) {
                Parada paradaIntermedia = paradasPrimer.get(i);
                if (paradaIntermedia == null) continue;

                for (Linea lineaSegundoTramo : paradaIntermedia.getLineas()) {
                    if (lineaSegundoTramo == null || lineaSegundoTramo.equals(lineaPrimerTramo)) continue;

                    String claveConexion = lineaPrimerTramo.getCodigo() + "-" +
                                            paradaIntermedia.getCodigo() + "-" +
                                            lineaSegundoTramo.getCodigo();
                    if (!conexionesYaEncontradas.add(claveConexion)) continue;

                    List<Parada> paradasSegundo = lineaSegundoTramo.getParadas();
                    if (paradasSegundo == null) continue;

                    int idxIntermedia = paradasSegundo.indexOf(paradaIntermedia);
                    int idxDestino = paradasSegundo.indexOf(destino);
                    if (!RecorridoUtils.indicesValidos(idxIntermedia, idxDestino)) continue;

                    int[] prefixSegundo = cache.obtenerPrefix(lineaSegundoTramo, paradasSegundo, tramos);
                    if (prefixSegundo == null) continue;

                    // Primer tramo
                    int offsetOrigen = prefixPrimer[idxOrigen];
                    int duracionPrimer = prefixPrimer[i] - prefixPrimer[idxOrigen];
                    List<LocalTime> frecuenciasPrimer = cache.obtenerFrecuenciasOrdenadas(lineaPrimerTramo, diaSemana);
                    LocalTime horaSalidaPrimer = RecorridoUtils.calcularHoraSalidaBinaria(frecuenciasPrimer, offsetOrigen, horaLlegaParada);
                    if (horaSalidaPrimer == null) continue;
                    LocalTime horaLlegadaIntermedia = horaSalidaPrimer.plusSeconds(duracionPrimer);

                    // Segundo tramo
                    int offsetIntermedia = prefixSegundo[idxIntermedia];
                    int duracionSegundo = prefixSegundo[idxDestino] - prefixSegundo[idxIntermedia];
                    List<LocalTime> frecuenciasSegundo = cache.obtenerFrecuenciasOrdenadas(lineaSegundoTramo, diaSemana);
                    LocalTime horaSalidaSegundo = RecorridoUtils.calcularHoraSalidaBinaria(frecuenciasSegundo, offsetIntermedia, horaLlegadaIntermedia);
                    if (horaSalidaSegundo == null) continue;

                    Recorrido r1 = new Recorrido(lineaPrimerTramo, RecorridoUtils.subRecorrido(paradasPrimer, idxOrigen, i), horaSalidaPrimer, duracionPrimer);
                    Recorrido r2 = new Recorrido(lineaSegundoTramo, RecorridoUtils.subRecorrido(paradasSegundo, idxIntermedia, idxDestino), horaSalidaSegundo, duracionSegundo);

                    resultado.add(List.of(r1, r2));
                }
            }
        }

        // Ordenar por tiempo total y limitar a 2 mejores
        resultado.sort(Comparator.comparingInt(RecorridoUtils::calcularTiempoTotalConexion));
        if (resultado.size() > 2) return new ArrayList<>(resultado.subList(0, 2));

        return resultado;
    }
}

