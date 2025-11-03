package colectivo.logica;

import java.time.LocalTime;
import java.util.*;

import colectivo.modelo.Linea;
import colectivo.modelo.Parada;
import colectivo.modelo.Recorrido;
import colectivo.modelo.Tramo;

/**
 * Clase encargada de calcular recorridos directos e indirectos entre paradas.
 * Refactorizada para unificar la lógica de conexiones y reducir la duplicación de código.
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
        if (origen != null && destino != null && horaLlegaParada != null && tramos != null) {
            
            // ---- RECORRIDOS DIRECTOS ----
            resultado.addAll(recorridosDirectos(origen, destino, diaSemana, horaLlegaParada, tramos));

            // ---- CONEXIONES (Transbordo o Caminando) ----
            if (resultado.isEmpty()) {
                resultado.addAll(buscarConexiones(origen, destino, diaSemana, horaLlegaParada, tramos));
            }
        }
        return resultado;
    }

    private List<List<Recorrido>> recorridosDirectos(Parada origen, Parada destino, int diaSemana, LocalTime horaLlegaParada, Map<String, Tramo> tramos) {
        List<List<Recorrido>> resultado = new ArrayList<>();
        for (Linea linea : origen.getLineas()) {
            if (linea != null) {
                Recorrido recorrido = calcularTramoBus(origen, destino, linea, diaSemana, horaLlegaParada, tramos);
                if (recorrido != null) {
                    resultado.add(List.of(recorrido));
                }
            }
        }
        return resultado;
    }

    private List<List<Recorrido>> buscarConexiones(Parada origen, Parada destino, int diaSemana, LocalTime horaLlegaParada, Map<String, Tramo> tramos) {
        List<List<Recorrido>> resultado = new ArrayList<>();
        Set<String> conexionesYaEncontradas = new HashSet<>();

        for (Linea lineaPrimerTramo : origen.getLineas()) {
            if (lineaPrimerTramo != null) {
                List<Parada> paradasPrimer = lineaPrimerTramo.getParadas();
                if (paradasPrimer != null) {
                    int idxOrigen = paradasPrimer.indexOf(origen);
                    if (idxOrigen >= 0) {
                        for (int i = idxOrigen + 1; i < paradasPrimer.size(); i++) {
                            Parada paradaIntermedia1 = paradasPrimer.get(i);
                            Recorrido primerTramo = calcularTramoBus(origen, paradaIntermedia1, lineaPrimerTramo, diaSemana, horaLlegaParada, tramos);
                            
                            if (primerTramo != null) {
                                LocalTime horaLlegadaIntermedia1 = primerTramo.getHoraSalida().plusSeconds(primerTramo.getDuracion());
                                
                                // Generar posibles conexiones intermedias (transbordo o caminata)
                                List<Recorrido> tramosIntermedios = new ArrayList<>();
                                // 1. Conexión por transbordo (duración 0)
                                tramosIntermedios.add(new Recorrido(null, List.of(paradaIntermedia1), horaLlegadaIntermedia1, 0));
                                // 2. Conexiones por caminata
                                for (Tramo tramoCaminando : RecorridoUtils.getTramosCaminandoDesde(paradaIntermedia1, tramos)) {
                                    tramosIntermedios.add(new Recorrido(null, List.of(tramoCaminando.getInicio(), tramoCaminando.getFin()), horaLlegadaIntermedia1, tramoCaminando.getTiempo()));
                                }

                                for (Recorrido tramoIntermedio : tramosIntermedios) {
                                    Parada paradaIntermedia2 = tramoIntermedio.getParadas().get(tramoIntermedio.getParadas().size() - 1);
                                    LocalTime horaLlegadaIntermedia2 = tramoIntermedio.getHoraSalida().plusSeconds(tramoIntermedio.getDuracion());

                                    for (Linea lineaFinal : paradaIntermedia2.getLineas()) {
                                        if (lineaFinal != null && !lineaFinal.equals(lineaPrimerTramo)) {
                                            String claveConexion = lineaPrimerTramo.getCodigo() + "->" + paradaIntermedia1.getCodigo() + "->" + paradaIntermedia2.getCodigo() + "->" + lineaFinal.getCodigo();
                                            if (conexionesYaEncontradas.add(claveConexion)) {
                                                Recorrido tramoFinal = calcularTramoBus(paradaIntermedia2, destino, lineaFinal, diaSemana, horaLlegadaIntermedia2, tramos);
                                                if (tramoFinal != null) {
                                                    List<Recorrido> recorridoCompleto = new ArrayList<>();
                                                    recorridoCompleto.add(primerTramo);
                                                    if (tramoIntermedio.getDuracion() > 0) { // Añadir solo si es caminata
                                                        recorridoCompleto.add(tramoIntermedio);
                                                    }
                                                    recorridoCompleto.add(tramoFinal);
                                                    resultado.add(recorridoCompleto);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        resultado.sort(Comparator.comparingInt(RecorridoUtils::calcularTiempoTotalConexion));
        return resultado.size() > 2 ? new ArrayList<>(resultado.subList(0, 2)) : resultado;
    }

    private Recorrido calcularTramoBus(Parada origenTramo, Parada destinoTramo, Linea linea, int diaSemana, LocalTime horaLlegadaOrigen, Map<String, Tramo> tramos) {
        List<Parada> paradas = linea.getParadas();
        if (paradas != null) {
            int idxOrigen = paradas.indexOf(origenTramo);
            int idxDestino = paradas.indexOf(destinoTramo);
            if (RecorridoUtils.indicesValidos(idxOrigen, idxDestino)) {
                int[] prefix = cache.obtenerPrefix(linea, paradas, tramos);
                if (prefix != null) {
                    int offsetOrigen = prefix[idxOrigen];
                    int duracion = prefix[idxDestino] - offsetOrigen;
                    List<LocalTime> frecuencias = cache.obtenerFrecuenciasOrdenadas(linea, diaSemana);
                    LocalTime horaSalida = RecorridoUtils.calcularHoraSalidaBinaria(frecuencias, offsetOrigen, horaLlegadaOrigen);
                    if (horaSalida != null) {
                        return new Recorrido(linea, RecorridoUtils.subRecorrido(paradas, idxOrigen, idxDestino), horaSalida, duracion);
                    }
                }
            }
        }
        return null;
    }
}