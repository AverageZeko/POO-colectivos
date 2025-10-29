package colectivo.logica;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import colectivo.modelo.Parada;
import colectivo.modelo.Recorrido;
import colectivo.modelo.Tramo;

public class RecorridoUtils {

    public static boolean indicesValidos(int idxOrigen, int idxDestino) {
        return idxOrigen >= 0 && idxDestino > idxOrigen;
    }

    public static List<Parada> subRecorrido(List<Parada> paradas, int idxOrigen, int idxDestino) {
        List<Parada> recorrido = new ArrayList<>(Math.max(2, idxDestino - idxOrigen + 1));
        for (int i = idxOrigen; i <= idxDestino; i++) {
            recorrido.add(paradas.get(i));
        }
        return recorrido;
    }

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
                // Para tramos caminando, la hora de salida es la de llegada del anterior, no hay espera.
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
    
    public static List<Tramo> getTramosCaminandoDesde(Parada origen, Map<String, Tramo> tramos) {
        return tramos.values().stream()
            .filter(t -> t.getTipo() == 2 && t.getInicio().equals(origen))
            .collect(Collectors.toList());
    }
}