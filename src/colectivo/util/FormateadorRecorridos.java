package colectivo.util;

import colectivo.modelo.Parada;

import colectivo.logica.Recorrido;
import colectivo.util.ArmadorString;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Formatea List<List<Recorrido>> -> List<List<String>>.
 * La UI solo recibirá la salida de este formateador.
 *
 * Además, expone utilidades de parsing para delegar al formateador
 * la detección de tramos y advertencias desde las líneas ya formateadas.
 */
public class FormateadorRecorridos {

    // ------------------------------
    // Modelo simple para la UI (render)
    // ------------------------------

    /** Marcador de tipo de tramo para ayudar a la UI a aplicar estilos. */
    public enum TipoTramo {
        COLECTIVO, CAMINANDO, DESCONOCIDO
    }

    /** Marcador común de ítem en una página parseada. */
    public interface ItemPagina { }

    /** Línea simple (fuera de un tramo), con flag de advertencia. */
    public static final class LineaSimple implements ItemPagina {
        public final String texto;
        public final boolean advertencia;
        public LineaSimple(String texto, boolean advertencia) {
            this.texto = texto;
            this.advertencia = advertencia;
        }
    }

    /** Segmento (tramo) con encabezado y líneas internas. */
    public static final class SegmentoFormateado implements ItemPagina {
        public final TipoTramo tipo;
        public final String encabezado;
        public final List<String> lineas; // líneas que pertenecen al segmento

        public SegmentoFormateado(TipoTramo tipo, String encabezado) {
            this.tipo = tipo;
            this.encabezado = encabezado;
            this.lineas = new ArrayList<>();
        }
    }

    /** Página parseada lista para renderizar (lista en orden de líneas y segmentos). */
    public static final class PaginaEstructurada {
        public final List<ItemPagina> items = new ArrayList<>();
    }

    /**
     * Parsea una página de líneas ya formateadas y construye una estructura de ítems
     * (líneas simples y segmentos) para que la UI solo itere y aplique estilos.
     *
     * No crea nodos JavaFX: mantiene el desac acoplamiento con la capa UI.
     */
    public static PaginaEstructurada parsearPagina(List<String> lineas, ResourceBundle bundle) {
        PaginaEstructurada pagina = new PaginaEstructurada();
        if (lineas == null || lineas.isEmpty() || bundle == null) {
            return pagina;
        }

        final String segPrefix = bundle.getString("Result_SegmentX");
        final String walkingText = bundle.getString("Result_Walking");
        final String lineText = bundle.getString("Result_LineX");
        final String transferWarning = bundle.getString("Result_TransferWarning");

        SegmentoFormateado segmentoActual = null;

        for (String linea : lineas) {
            if (linea == null || linea.trim().isEmpty()) {
                // ignorar separadores vacíos
                continue;
            }

            // Inicio de segmento
            if (linea.startsWith(segPrefix)) {
                // Cerrar el segmento anterior si estaba abierto
                if (segmentoActual != null) {
                    pagina.items.add(segmentoActual);
                }
                // Determinar tipo
                TipoTramo tipo = TipoTramo.DESCONOCIDO;
                if (linea.contains(walkingText)) {
                    tipo = TipoTramo.CAMINANDO;
                } else if (linea.contains(lineText)) {
                    tipo = TipoTramo.COLECTIVO;
                }
                segmentoActual = new SegmentoFormateado(tipo, linea);
                continue;
            }

            // Advertencia de trasbordo
            if (linea.trim().equals(transferWarning)) {
                // Cerrar segmento si corresponde antes de agregar línea suelta
                if (segmentoActual != null) {
                    pagina.items.add(segmentoActual);
                    segmentoActual = null;
                }
                pagina.items.add(new LineaSimple(linea, true));
                continue;
            }

            // Resto de líneas
            if (segmentoActual != null) {
                segmentoActual.lineas.add(linea);
            } else {
                pagina.items.add(new LineaSimple(linea, false));
            }
        }

        // Agregar último segmento si quedó abierto
        if (segmentoActual != null) {
            pagina.items.add(segmentoActual);
        }

        return pagina;
    }

    // ------------------------------
    // Formateador original de negocio -> texto
    // ------------------------------

    /**
     * Convierte las rutas (cada ruta = lista de tramos Recorrido) a páginas
     * (cada página = lista de líneas ya formateadas).
     *
     * @param rutasCompletas lista de rutas
     * @param horaConsulta hora de llegada indicada por el usuario
     * @param bundle resource bundle para textos localizados
     * @return List<List<String>> lista de páginas listas para renderizar por la UI
     */
    public static List<List<String>> formatear(List<List<Recorrido>> rutasCompletas, LocalTime horaConsulta, ResourceBundle bundle, ArmadorString armador) {
        armador.setRutas(rutasCompletas);
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
                    lineas.add("  " + bundle.getString("Result_WaitTime") + " " + LocalTime.ofSecondOfDay(esperaSeg));
                    lineas.add("  " + bundle.getString("Result_TravelTime") + " " + LocalTime.ofSecondOfDay(viajeSeg));

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
                    lineas.add("  " + bundle.getString("Result_WalkDuration") + " " + LocalTime.ofSecondOfDay(viajeSeg));
                }

                lineas.add("  " + bundle.getString("Result_ArrivalTime") + " " + horaLlegadaTramo);
                horaLlegaActual = horaLlegadaTramo;
            }

            paginas.add(lineas);
        }
        

        return paginas;
    }
}