package colectivo.interfaz.tareas;

import colectivo.controlador.Coordinador;
import colectivo.interfaz.GestorDeVentanas;
import colectivo.modelo.Recorrido;
import javafx.concurrent.Task;

import java.util.List;

/**
 * Tarea en segundo plano para realizar la consulta al coordinador
 * sin bloquear la interfaz de usuario.
 */
public class ConsultaTask extends Task<List<List<Recorrido>>> {

    private final GestorDeVentanas gestor;
    private final ConsultaRequest request;

    public ConsultaTask(GestorDeVentanas gestor, ConsultaRequest request) {
        this.gestor = gestor;
        this.request = request;
    }

    @Override
    protected List<List<Recorrido>> call() throws Exception {
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