package colectivo.interfaz.tareas;

import colectivo.modelo.Parada;
import javafx.scene.control.RadioButton;

import java.time.LocalTime;
import java.util.ResourceBundle;

/**
 * Encapsula los datos de una solicitud de consulta realizada por el usuario.
 */
public class ConsultaRequest {

    private final Parada paradaOrigen;
    private final Parada paradaDestino;
    private final RadioButton diaRadio;
    private final LocalTime hora;

    public ConsultaRequest(Parada paradaOrigen, Parada paradaDestino, RadioButton diaRadio, LocalTime hora) {
        this.paradaOrigen = paradaOrigen;
        this.paradaDestino = paradaDestino;
        this.diaRadio = diaRadio;
        this.hora = hora;
    }

    public Parada getParadaOrigen() {
        return paradaOrigen;
    }

    public Parada getParadaDestino() {
        return paradaDestino;
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