package colectivo.interfaz.tareas;

import colectivo.interfaz.GestorDeVentanas;
import javafx.concurrent.Task;

import java.util.List;

/**
 * Tarea en segundo plano que solicita a través del Gestor de Ventanas
 * la consulta ya formateada (List<List<String>>) para que la UI solo
 * renderice texto listo.
 */
public class ConsultaTask extends Task<List<List<String>>> {

    private final GestorDeVentanas gestor;
    private final ConsultaRequest request;

    public ConsultaTask(GestorDeVentanas gestor, ConsultaRequest request) {
        this.gestor = gestor;
        this.request = request;
    }

    @Override
    protected List<List<String>> call() throws Exception {
        // Simula una espera para que el GIF de carga sea visible
        Thread.sleep(2000);

        int diaInt = request.getDiaSemana(gestor.getBundle());
        return gestor.solicitarConsulta(
                request.getParadaOrigen(),
                request.getParadaDestino(),
                diaInt,
                request.getHora()
        );
    }
}