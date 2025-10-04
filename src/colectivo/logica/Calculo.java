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

public class Calculo {

    public static List<List<Recorrido>> calcularRecorrido(
            Parada paradaOrigen,
            Parada paradaDestino,
            int diaSemana,
            LocalTime horaLlegaParada,
            Map<String, Tramo> tramos) {

        List<List<Recorrido>> resultado = new ArrayList<>();
        if (paradaOrigen == null || paradaDestino == null) return resultado;

        List<Linea> lineas = paradaOrigen.getLineas();
        if (lineas == null || lineas.isEmpty()) return resultado;

        for (Linea linea : lineas) {
            if (linea == null) continue;
            List<Parada> paradasLinea = linea.getParadas();
            if (paradasLinea == null || paradasLinea.isEmpty()) continue;
            if (!paradasLinea.contains(paradaDestino)) continue;

            int idxOrigen  = paradasLinea.indexOf(paradaOrigen);
            int idxDestino = paradasLinea.indexOf(paradaDestino);
            if (idxOrigen < 0 || idxDestino < 0 || idxDestino <= idxOrigen) continue;

            // 1. Calcular offset desde primera parada de la línea hasta paradaOrigen
            int offsetOrigenSeg = 0;
            boolean faltanteOffset = false;
            for (int i = 0; i < idxOrigen; i++) {
                Parada a = paradasLinea.get(i);
                Parada b = paradasLinea.get(i + 1);
                Tramo tramo = tramos.get(claveTramo(a, b));
                if (tramo == null) {
                    faltanteOffset = true;
                    break;
                }
                offsetOrigenSeg += tramo.getTiempo();
            }
            if (faltanteOffset) continue;

            // 2. Calcular duración viaje origen->destino
            int viajeSeg = 0;
            boolean faltanteViaje = false;
            List<Parada> paradasRecorrido = new ArrayList<>();
            for (int i = idxOrigen; i <= idxDestino; i++) {
                paradasRecorrido.add(paradasLinea.get(i));
            }
            for (int i = 0; i < paradasRecorrido.size() - 1; i++) {
                Parada a = paradasRecorrido.get(i);
                Parada b = paradasRecorrido.get(i + 1);
                Tramo tramo = tramos.get(claveTramo(a, b));
                if (tramo == null) {
                    faltanteViaje = true;
                    break;
                }
                viajeSeg += tramo.getTiempo();
            }
            if (faltanteViaje) continue;

            // 3. Obtener frecuencias base (salida primera parada línea) y ordenarlas
            List<LocalTime> frecuenciasBase = linea.getHorasFrecuenciaPorDia(diaSemana);
            if (frecuenciasBase == null || frecuenciasBase.isEmpty()) continue;
            frecuenciasBase.sort(Comparator.naturalOrder());

            // 4. Encontrar la primera frecuencia cuya llegada a la paradaOrigen sea >= horaLlegaParada
            LocalTime horaSalidaEnOrigen = null;
            for (LocalTime base : frecuenciasBase) {
                LocalTime llegadaOrigen = base.plusSeconds(offsetOrigenSeg);
                if (!llegadaOrigen.isBefore(horaLlegaParada)) {
                    horaSalidaEnOrigen = llegadaOrigen;
                    break;
                }
            }
            if (horaSalidaEnOrigen == null) {
                // No hay más servicios que lleguen después de la hora del usuario
                continue;
            }

            // 5. Crear recorrido
            Recorrido recorrido = new Recorrido(linea, paradasRecorrido, horaSalidaEnOrigen, viajeSeg);

            List<Recorrido> alternativa = new ArrayList<>();
            alternativa.add(recorrido);
            resultado.add(alternativa);
        }

        return resultado;
    }

    private static String claveTramo(Parada a, Parada b) {
        return a.getCodigo() + "->" + b.getCodigo();
    }

}