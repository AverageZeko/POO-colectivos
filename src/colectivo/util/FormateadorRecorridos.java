package colectivo.util;

import colectivo.modelo.Parada;
import colectivo.modelo.Recorrido;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Formatea List<List<Recorrido>> -> List<List<String>>.
 * La UI solo recibirá la salida de este formateador.
 */
public class FormateadorRecorridos {

    /**
     * Convierte las rutas (cada ruta = lista de tramos Recorrido) a páginas
     * (cada página = lista de líneas ya formateadas).
     *
     * @param rutasCompletas lista de rutas
     * @param horaConsulta hora de llegada indicada por el usuario
     * @param bundle resource bundle para textos localizados
     * @return List<List<String>> lista de páginas listas para renderizar por la UI
     */
    public static List<List<String>> formatear(List<List<Recorrido>> rutasCompletas, LocalTime horaConsulta, ResourceBundle bundle) {
        List<List<String>> paginas = new ArrayList<>();
        if (rutasCompletas == null || rutasCompletas.isEmpty()) return paginas;

        for (int idx = 0; idx < rutasCompletas.size(); idx++) {
            List<Recorrido> recorridoCompleto = rutasCompletas.get(idx);
            List<String> lineas = new ArrayList<>();
            lineas.add(bundle.getString("Result_RouteX") + " " + (idx + 1) + ":");

            if (recorridoCompleto.size() > 1) {
                lineas.add(bundle.getString("Result_TransferWarning"));
            }

            LocalTime horaLlegaActual = horaConsulta;

            for (int t = 0; t < recorridoCompleto.size(); t++) {
                Recorrido r = recorridoCompleto.get(t);

                LocalTime horaSalida = r.getHoraSalida();
                int viajeSeg = r.getDuracion();
                LocalTime horaLlegadaTramo = horaSalida.plusSeconds(viajeSeg);

                if (r.getLinea() != null) {
                    long esperaSeg = Duration.between(horaLlegaActual, horaSalida).toSeconds();
                    if (esperaSeg < 0) esperaSeg = 0;

                    lineas.add(""); // separador visual
                    lineas.add(bundle.getString("Result_SegmentX") + " " + (t + 1) + " - " + bundle.getString("Result_LineX") + " " + r.getLinea().getCodigo());
                    List<Parada> paradasTramo = r.getParadas();
                    Parada tramoOrigen = paradasTramo.get(0);
                    Parada tramoDestino = paradasTramo.get(paradasTramo.size() - 1);

                    lineas.add("  " + bundle.getString("Result_InitialStop") + " " + tramoOrigen.getDireccion());
                    lineas.add("  " + bundle.getString("Result_FinalStop") + " " + tramoDestino.getDireccion());
                    lineas.add("  " + bundle.getString("Result_UserTimeOfArrival") + " " + horaLlegaActual);
                    lineas.add("  " + bundle.getString("Result_TimeOfDeparture") + " " + horaSalida);
                    lineas.add("  " + bundle.getString("Result_WaitTime") + " " + Tiempo.segundosATiempo((int) esperaSeg));
                    lineas.add("  " + bundle.getString("Result_TravelTime") + " " + Tiempo.segundosATiempo(viajeSeg));

                    if (paradasTramo.size() > 1) {
                        lineas.add("    " + bundle.getString("Result_Stops") + ":");
                        for (int i = 0; i < paradasTramo.size() - 1; i++) {
                            Parada paradaActual = paradasTramo.get(i);
                            Parada paradaSiguiente = paradasTramo.get(i + 1);
                            lineas.add("      - " + paradaActual.getDireccion() + " -> " + paradaSiguiente.getDireccion());
                        }
                    }
                } else {
                    // Tramo a pie
                    List<Parada> paradasTramo = r.getParadas();
                    Parada tramoOrigen = paradasTramo.get(0);
                    Parada tramoDestino = paradasTramo.get(paradasTramo.size() - 1);

                    lineas.add("");
                    lineas.add(bundle.getString("Result_SegmentX") + " " + (t + 1) + " - " + bundle.getString("Result_Walking"));
                    lineas.add("  " + bundle.getString("Result_WalkFrom") + " " + tramoOrigen.getDireccion());
                    lineas.add("  " + bundle.getString("Result_WalkTo") + " " + tramoDestino.getDireccion());
                    lineas.add("  " + bundle.getString("Result_WalkStart") + " " + horaSalida);
                    lineas.add("  " + bundle.getString("Result_WalkDuration") + " " + Tiempo.segundosATiempo(viajeSeg));
                }

                lineas.add("  " + bundle.getString("Result_ArrivalTime") + " " + horaLlegadaTramo);
                horaLlegaActual = horaLlegadaTramo;
            }

            paginas.add(lineas);
        }

        return paginas;
    }
}