package colectivo.logica;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import colectivo.modelo.Linea;
import colectivo.modelo.Parada;
import colectivo.modelo.Recorrido;
import colectivo.modelo.Tramo;

/**
 * Clase utilitaria encargada de calcular los recorridos posibles entre dos paradas
 * de colectivos, considerando líneas, frecuencias y tiempos de viaje entre tramos.
 *
 * <p>El cálculo contempla únicamente recorridos directos (sin trasbordos) y busca
 * la primera frecuencia disponible según la hora en que el pasajero llega a la parada
 * de origen.</p>
 *
 * <p>No utiliza sentencias {@code break} ni {@code continue} para mantener un flujo
 * de control claro y estructurado.</p>
 *
 * @author 
 */
public class Calculo {

    /**
     * Calcula todos los posibles recorridos directos entre una parada de origen y una de destino,
     * para todas las líneas que pasan por la parada de origen.
     *
     * @param paradaOrigen      parada donde el pasajero inicia su viaje
     * @param paradaDestino     parada donde el pasajero finaliza su viaje
     * @param diaSemana         día de la semana (por ejemplo, 1=Lunes, 7=Domingo)
     * @param horaLlegaParada   hora a la que el pasajero llega a la parada de origen
     * @param tramos            mapa de tramos existentes (clave: "A->B") con sus tiempos de viaje
     * @return una lista de alternativas de recorrido; cada alternativa es una lista de {@link Recorrido}
     */
    public static List<List<Recorrido>> calcularRecorrido(
            Parada paradaOrigen,
            Parada paradaDestino,
            int diaSemana,
            LocalTime horaLlegaParada,
            Map<String, Tramo> tramos) {

        List<List<Recorrido>> resultado = new ArrayList<>();

        // Validaciones básicas de entrada
        if (paradaOrigen == null || paradaDestino == null) return resultado;

        List<Linea> lineas = paradaOrigen.getLineas();
        if (lineas == null || lineas.isEmpty()) return resultado;

        // Iterar por todas las líneas que pasan por la parada de origen
        for (Linea linea : lineas) {
            if (linea == null) continue;

            List<Parada> paradasLinea = linea.getParadas();
            int idxOrigen  = paradasLinea.indexOf(paradaOrigen);
            int idxDestino = paradasLinea.indexOf(paradaDestino);

            if (!indicesValidos(idxOrigen, idxDestino)) continue;

            // 1️⃣ Calcular el tiempo desde el inicio de la línea hasta la parada de origen
            int offsetOrigenSeg = calcularOffsetHastaOrigen(paradasLinea, idxOrigen, tramos);

            // 2️⃣ Calcular las paradas y duración del viaje entre origen y destino
            List<Parada> paradasRecorrido = calcularParadasRecorrido(paradasLinea, idxOrigen, idxDestino, tramos);
            int viajeSeg = calcularDuracionViaje(paradasLinea, idxOrigen, idxDestino, tramos);

            // 3️⃣ Obtener y ordenar las frecuencias base de la línea para el día indicado
            List<LocalTime> frecuenciasBase = obtenerFrecuenciasOrdenadas(linea, diaSemana);
            if (frecuenciasBase.isEmpty()) continue;

            // 4️⃣ Calcular la hora de salida desde la parada de origen
            LocalTime horaSalidaEnOrigen = calcularHoraSalida(frecuenciasBase, offsetOrigenSeg, horaLlegaParada);

            // 5️⃣ Crear el recorrido y agregarlo al resultado final
            if (horaSalidaEnOrigen != null) {
                Recorrido recorrido = new Recorrido(linea, paradasRecorrido, horaSalidaEnOrigen, viajeSeg);
                resultado.add(List.of(recorrido));
            }
        }

        return resultado;
    }

    /**
     * Verifica que los índices de las paradas de origen y destino sean válidos.
     *
     * @param idxOrigen  índice de la parada de origen
     * @param idxDestino índice de la parada de destino
     * @return {@code true} si los índices son válidos; {@code false} en caso contrario
     */
    private static boolean indicesValidos(int idxOrigen, int idxDestino) {
        return idxOrigen >= 0 && idxDestino > idxOrigen;
    }

    /**
     * Calcula el tiempo acumulado (en segundos) desde la primera parada de la línea
     * hasta la parada de origen.
     *
     * @param paradas lista completa de paradas de la línea
     * @param idxOrigen índice de la parada de origen
     * @param tramos mapa de tramos existentes entre paradas
     * @return tiempo total en segundos hasta la parada de origen
     */
    private static int calcularOffsetHastaOrigen(List<Parada> paradas, int idxOrigen, Map<String, Tramo> tramos) {
        int offset = 0;
        for (int i = 0; i < idxOrigen; i++) {
            Tramo tramo = tramos.get(claveTramo(paradas.get(i), paradas.get(i + 1)));
            if (tramo == null) return offset; // Si falta un tramo, se detiene el cálculo
            offset += tramo.getTiempo();
        }
        return offset;
    }

    /**
     * Calcula la duración total (en segundos) del viaje entre las paradas de origen y destino.
     *
     * @param paradas lista completa de paradas de la línea
     * @param idxOrigen índice de la parada de origen
     * @param idxDestino índice de la parada de destino
     * @param tramos mapa de tramos con los tiempos de viaje
     * @return duración total del viaje en segundos
     */
    private static int calcularDuracionViaje(List<Parada> paradas, int idxOrigen, int idxDestino, Map<String, Tramo> tramos) {
        int duracion = 0;
        for (int i = idxOrigen; i < idxDestino; i++) {
            Tramo tramo = tramos.get(claveTramo(paradas.get(i), paradas.get(i + 1)));
            if (tramo == null) return duracion;
            duracion += tramo.getTiempo();
        }
        return duracion;
    }

    /**
     * Genera la lista de paradas intermedias (incluyendo origen y destino)
     * que conforman el recorrido.
     *
     * @param paradas lista completa de paradas de la línea
     * @param idxOrigen índice de la parada de origen
     * @param idxDestino índice de la parada de destino
     * @param tramos mapa de tramos con tiempos de viaje
     * @return lista ordenada de paradas que forman el recorrido
     */
    private static List<Parada> calcularParadasRecorrido(List<Parada> paradas, int idxOrigen, int idxDestino, Map<String, Tramo> tramos) {
        List<Parada> recorrido = new ArrayList<>();
        for (int i = idxOrigen; i < idxDestino; i++) {
            Tramo tramo = tramos.get(claveTramo(paradas.get(i), paradas.get(i + 1)));
            if (tramo == null) return recorrido;
            recorrido.add(paradas.get(i));
        }
        recorrido.add(paradas.get(idxDestino)); // se incluye la parada final
        return recorrido;
    }

    /**
     * Obtiene las frecuencias base de salida de una línea según el día de la semana,
     * ordenadas cronológicamente.
     *
     * @param linea línea de colectivo
     * @param diaSemana día de la semana (1 a 7)
     * @return lista ordenada de horas de salida; vacía si no existen datos
     */
    private static List<LocalTime> obtenerFrecuenciasOrdenadas(Linea linea, int diaSemana) {
        List<LocalTime> frecuencias = linea.getHorasFrecuenciaPorDia(diaSemana);
        if (frecuencias == null) return new ArrayList<>();
        frecuencias.sort(Comparator.naturalOrder());
        return frecuencias;
    }

    /**
     * Determina la primera hora de salida desde la parada de origen, considerando
     * el tiempo que tarda el colectivo en llegar desde la primera parada de la línea.
     *
     * @param frecuenciasBase lista ordenada de horas base (salidas desde el inicio de la línea)
     * @param offsetOrigenSeg tiempo en segundos desde el inicio de la línea hasta la parada de origen
     * @param horaLlegaParada hora en que el pasajero llega a la parada
     * @return la hora de salida más próxima; {@code null} si ninguna frecuencia es válida
     */
    private static LocalTime calcularHoraSalida(List<LocalTime> frecuenciasBase, int offsetOrigenSeg, LocalTime horaLlegaParada) {
        LocalTime llegadaOrigen = LocalTime.of(0, 0);
        int i = 0;

        // Buscar la primera frecuencia que llegue a la parada de origen después o al mismo tiempo
        // que la hora en la que el pasajero llega a dicha parada
        while (i < frecuenciasBase.size() && llegadaOrigen.isBefore(horaLlegaParada)) {
            LocalTime base = frecuenciasBase.get(i++);
            llegadaOrigen = base.plusSeconds(offsetOrigenSeg);
        }

        return llegadaOrigen;
    }

    /**
     * Recrea la clave única para identificar un tramo entre dos paradas consecutivas.
     *
     * @param a parada de inicio
     * @param b parada de fin
     * @return cadena con el formato "codigoA->codigoB"
     */
    private static String claveTramo(Parada a, Parada b) {
        return a.getCodigo() + "->" + b.getCodigo();
    }
}

