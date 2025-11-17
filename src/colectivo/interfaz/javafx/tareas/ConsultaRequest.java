package colectivo.interfaz.javafx.tareas;

import javafx.scene.control.RadioButton;
import java.time.LocalTime;
import java.util.ResourceBundle;

/**
 * ConsultaRequest encapsula los datos de una solicitud de consulta realizada por el usuario.
 * Utiliza IDs de paradas y un RadioButton para el día de la semana, junto con la hora seleccionada.
 */
public class ConsultaRequest {

    /** ID de la parada de origen seleccionada por el usuario. */
    private final int idParadaOrigen;

    /** ID de la parada de destino seleccionada por el usuario. */
    private final int idParadaDestino;

    /** RadioButton que representa el día de la semana seleccionado. */
    private final RadioButton diaRadio;

    /** Hora seleccionada para la consulta. */
    private final LocalTime hora;

    /**
     * Crea una nueva solicitud de consulta con los datos proporcionados.
     * @param idParadaOrigen ID de la parada de origen.
     * @param idParadaDestino ID de la parada de destino.
     * @param diaRadio RadioButton del día de la semana.
     * @param hora Hora seleccionada.
     */
    public ConsultaRequest(int idParadaOrigen, int idParadaDestino, RadioButton diaRadio, LocalTime hora) {
        this.idParadaOrigen = idParadaOrigen;
        this.idParadaDestino = idParadaDestino;
        this.diaRadio = diaRadio;
        this.hora = hora;
    }

    /**
     * Devuelve el ID de la parada de origen.
     * @return ID de la parada de origen.
     */
    public int getIdParadaOrigen() {
        return idParadaOrigen;
    }

    /**
     * Devuelve el ID de la parada de destino.
     * @return ID de la parada de destino.
     */
    public int getIdParadaDestino() {
        return idParadaDestino;
    }

    /**
     * Devuelve la hora seleccionada para la consulta.
     * @return Hora seleccionada.
     */
    public LocalTime getHora() {
        return hora;
    }

    /**
     * Devuelve el número correspondiente al día de la semana según el texto del RadioButton y el ResourceBundle.
     * @param bundle ResourceBundle con los textos de los días.
     * @return Número del día de la semana (1=Lunes, ..., 7=Domingo), 0 si no coincide.
     */
    public int getDiaSemana(ResourceBundle bundle) {
        String diaTexto = diaRadio.getText();
        if (diaTexto.equals(bundle.getString("Query_Monday"))) return 1;
        if (diaTexto.equals(bundle.getString("Query_Tuesday"))) return 2;
        if (diaTexto.equals(bundle.getString("Query_Wednesday"))) return 3;
        if (diaTexto.equals(bundle.getString("Query_Thursday"))) return 4;
        if (diaTexto.equals(bundle.getString("Query_Friday"))) return 5;
        if (diaTexto.equals(bundle.getString("Query_Saturday"))) return 6;
        if (diaTexto.equals(bundle.getString("Query_Sunday"))) return 7;
        return 0;
    }
}