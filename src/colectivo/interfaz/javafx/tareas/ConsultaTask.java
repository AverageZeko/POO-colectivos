package colectivo.interfaz.javafx.tareas;

import javafx.concurrent.Task;
import java.util.List;
import colectivo.interfaz.javafx.GestorDeVentanas;

/**
 * Tarea en segundo plano que solicita a través del Gestor de Ventanas
 * la consulta ya formateada (List<List<String>>) para que la UI solo
 * renderice texto listo.
 */
public class ConsultaTask extends Task<List<List<String>>> {

    /** Gestor de ventanas encargado de procesar la consulta. */
    private final GestorDeVentanas gestor;

    /** Solicitud de consulta con los datos necesarios. */
    private final ConsultaRequest request;

    /**
     * Crea una nueva tarea de consulta con el gestor y la solicitud especificados.
     * @param gestor Gestor de ventanas encargado de la consulta.
     * @param request Solicitud de consulta con los datos del usuario.
     */
    public ConsultaTask(GestorDeVentanas gestor, ConsultaRequest request) {
        this.gestor = gestor;
        this.request = request;
    }

    /**
     * Ejecuta la consulta en segundo plano y devuelve los resultados formateados.
     * @return Lista de páginas de resultados (List<List<String>>).
     * @throws Exception si ocurre un error durante la consulta.
     */
    @Override
    protected List<List<String>> call() throws Exception {
        Thread.sleep(2000); // Espera para mostrar el GIF de carga

        int diaInt = request.getDiaSemana(gestor.getBundle());
        return gestor.solicitarConsulta(
                request.getIdParadaOrigen(),
                request.getIdParadaDestino(),
                diaInt,
                request.getHora()
        );
    }
}