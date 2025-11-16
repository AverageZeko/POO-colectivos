package colectivo.interfaz.javafx.tareas;

import javafx.scene.control.RadioButton;

import java.time.LocalTime;
import java.util.ResourceBundle;

/**
 * Encapsula los datos de una solicitud de consulta realizada por el usuario.
 * Usa IDs de paradas para no exponer objetos de dominio en la UI.
 */
public class ConsultaRequest {

    private final int idParadaOrigen;
    private final int idParadaDestino;
    private final RadioButton diaRadio;
    private final LocalTime hora;

    public ConsultaRequest(int idParadaOrigen, int idParadaDestino, RadioButton diaRadio, LocalTime hora) {
        this.idParadaOrigen = idParadaOrigen;
        this.idParadaDestino = idParadaDestino;
        this.diaRadio = diaRadio;
        this.hora = hora;
    }

    public int getIdParadaOrigen() {
        return idParadaOrigen;
    }

    public int getIdParadaDestino() {
        return idParadaDestino;
    }

    public LocalTime getHora() {
        return hora;
    }

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