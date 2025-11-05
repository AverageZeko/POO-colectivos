package colectivo.logica;

import java.time.LocalTime;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import colectivo.modelo.Linea;
import colectivo.modelo.Parada;
import colectivo.modelo.Recorrido;
import colectivo.modelo.Tramo;

/**
 * Clase principal encargada de la lógica de cálculo de rutas entre dos paradas.
 * Utiliza una caché interna ({@link CacheLineas}) para optimizar los cálculos repetitivos.
 * La lógica se divide en buscar recorridos directos y, si no se encuentran,
 * buscar conexiones con transbordo o caminando.
 */
public class Calculo {
    private static final Logger LOGICA_LOG = LoggerFactory.getLogger("Logica");

    private final CacheLineas cache = new CacheLineas();

    public Calculo() {
    }

    /**
     * Método principal para calcular las posibles rutas entre un origen y un destino.
     * Primero intenta encontrar recorridos directos. Si no hay, busca conexiones
     * que pueden implicar un transbordo o un tramo a pie.
     *
     * @param origen La parada de inicio del viaje.
     * @param destino La parada final del viaje.
     * @param diaSemana El día de la semana para el cálculo de frecuencias.
     * @param horaLlegaParada La hora a la que el usuario llega a la parada de origen.
     * @param tramos Un mapa con todos los tramos del sistema de transporte.
     * @return Una lista de rutas. Cada ruta es una lista de segmentos ({@link Recorrido}).
     *         Puede estar vacía si no se encuentra ninguna ruta.
     */
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
        LOGICA_LOG.debug("Recorridos calculados");
        return resultado;
    }

    /**
     * Busca rutas directas (un solo colectivo) entre el origen y el destino.
     *
     * @param origen Parada de inicio.
     * @param destino Parada final.
     * @param diaSemana Día de la semana.
     * @param horaLlegaParada Hora de llegada a la parada de origen.
     * @param tramos Mapa de todos los tramos.
     * @return Una lista de rutas, donde cada ruta contiene un único segmento de recorrido directo.
     */
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

    /**
     * Busca rutas indirectas que requieren uno o más transbordos o tramos a pie.
     *
     * @param origen Parada de inicio.
     * @param destino Parada final.
     * @param diaSemana Día de la semana.
     * @param horaLlegaParada Hora de llegada a la parada de origen.
     * @param tramos Mapa de todos los tramos.
     * @return Una lista de las 2 mejores rutas de conexión encontradas, ordenadas por tiempo total.
     */
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
                                
                                List<Recorrido> tramosIntermedios = new ArrayList<>();
                                tramosIntermedios.add(new Recorrido(null, List.of(paradaIntermedia1), horaLlegadaIntermedia1, 0));
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

    /**
     * Calcula un único segmento de viaje en colectivo (un {@link Recorrido}).
     *
     * @param origenTramo La parada donde se inicia este segmento.
     * @param destinoTramo La parada donde finaliza este segmento.
     * @param linea La línea de colectivo a utilizar.
     * @param diaSemana El día de la semana.
     * @param horaLlegadaOrigen La hora de llegada a la parada de origen de este tramo.
     * @param tramos Mapa de todos los tramos.
     * @return Un objeto {@link Recorrido} si se encuentra un viaje válido, o {@code null} en caso contrario.
     */
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