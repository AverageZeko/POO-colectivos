package colectivo.logica;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import colectivo.modelo.Parada;
import colectivo.modelo.Recorrido;
import colectivo.modelo.Tramo;

/**
 * Clase de utilidad con métodos estáticos auxiliares para los cálculos de recorridos.
 * Contiene lógica para validar índices, extraer sub-recorridos, calcular horarios de salida
 * y obtener tramos de caminata.
 */
public class RecorridoUtils {

    /**
     * Comprueba si los índices de origen y destino son válidos para un sub-recorrido.
     * Un recorrido es válido si el origen existe (>=0) y el destino está después del origen.
     *
     * @param idxOrigen Índice de la parada de origen.
     * @param idxDestino Índice de la parada de destino.
     * @return {@code true} si los índices son válidos, {@code false} en caso contrario.
     */
    public static boolean indicesValidos(int idxOrigen, int idxDestino) {
        return idxOrigen >= 0 && idxDestino > idxOrigen;
    }

    /**
     * Extrae una sublista de paradas que representan un segmento de un recorrido.
     *
     * @param paradas La lista completa de paradas de una línea.
     * @param idxOrigen El índice de inicio del sub-recorrido.
     * @param idxDestino El índice de fin del sub-recorrido.
     * @return Una nueva lista con las paradas del segmento.
     */
    public static List<Parada> subRecorrido(List<Parada> paradas, int idxOrigen, int idxDestino) {
        List<Parada> recorrido = new ArrayList<>(Math.max(2, idxDestino - idxOrigen + 1));
        for (int i = idxOrigen; i <= idxDestino; i++) {
            recorrido.add(paradas.get(i));
        }
        return recorrido;
    }

    /**
     * Calcula la próxima hora de salida de un colectivo desde una parada de origen,
     * basándose en la hora de llegada del pasajero. Utiliza búsqueda binaria para eficiencia.
     *
     * @param frecuenciasBase Lista ordenada de horarios de salida desde el inicio de la línea.
     * @param offsetOrigenSeg Tiempo en segundos desde el inicio de la línea hasta la parada de origen.
     * @param horaLlegaParada Hora en que el pasajero llega a la parada de origen.
     * @return La {@link LocalTime} de la próxima salida del colectivo desde esa parada, o {@code null} si no hay más servicios.
     */
    public static LocalTime calcularHoraSalidaBinaria(List<LocalTime> frecuenciasBase, int offsetOrigenSeg, LocalTime horaLlegaParada) {
        LocalTime resultado = null;
        if (frecuenciasBase != null && !frecuenciasBase.isEmpty() && horaLlegaParada != null) {
            LocalTime objetivo = horaLlegaParada.minusSeconds(offsetOrigenSeg);
            int lo = 0;
            int hi = frecuenciasBase.size() - 1;
            int foundIndex = -1;
            while (lo <= hi) {
                int mid = (lo + hi) >>> 1;
                LocalTime midTime = frecuenciasBase.get(mid);
                if (midTime.compareTo(objetivo) < 0) {
                    lo = mid + 1;
                } else {
                    foundIndex = mid;
                    hi = mid - 1;
                }
            }
            if (foundIndex >= 0) {
                LocalTime base = frecuenciasBase.get(foundIndex);
                resultado = base.plusSeconds(offsetOrigenSeg);
            }
        }
        return resultado;
    }

    /**
     * Calcula el tiempo total de una ruta de conexión, sumando la duración de todos sus
     * segmentos y los tiempos de espera en los transbordos.
     *
     * @param conexion Una lista de {@link Recorrido} que componen la ruta completa.
     * @return El tiempo total en segundos, o {@link Integer#MAX_VALUE} si la conexión es inválida.
     */
    public static int calcularTiempoTotalConexion(List<Recorrido> conexion) {
        int tiempoTotalSeg = 0;
        if (conexion == null || conexion.isEmpty()) {
            return Integer.MAX_VALUE;
        }
        for (Recorrido r : conexion) {
            tiempoTotalSeg += r.getDuracion();
        }
        if (conexion.size() > 1) {
            for (int i = 1; i < conexion.size(); i++) {
                Recorrido anterior = conexion.get(i - 1);
                Recorrido actual = conexion.get(i);
                if (actual.getLinea() != null) {
                    LocalTime finAnterior = anterior.getHoraSalida().plusSeconds(anterior.getDuracion());
                    LocalTime inicioActual = actual.getHoraSalida();
                    long esperaSeg = inicioActual.toSecondOfDay() - finAnterior.toSecondOfDay();
                    if (esperaSeg < 0) {
                        esperaSeg += 24 * 60 * 60;
                    }
                    tiempoTotalSeg += esperaSeg;
                }
            }
        }
        return tiempoTotalSeg;
    }
    
    /**
     * Obtiene una lista de todos los tramos que se pueden recorrer caminando desde una parada de origen.
     *
     * @param origen La parada de inicio.
     * @param tramos Un mapa con todos los tramos del sistema.
     * @return Una lista de {@link Tramo} de tipo caminata que parten del origen.
     */
    public static List<Tramo> getTramosCaminandoDesde(Parada origen, Map<String, Tramo> tramos) {
        return tramos.values().stream()
            .filter(t -> t.getTipo() == 2 && t.getInicio().equals(origen))
            .collect(Collectors.toList());
    }
}