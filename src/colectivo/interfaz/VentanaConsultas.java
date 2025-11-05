package colectivo.interfaz;

import java.time.LocalTime;
import java.util.List;

import colectivo.controlador.Coordinador;
import colectivo.modelo.Parada;
import colectivo.modelo.Recorrido;
import javafx.stage.Stage;

/**
 * Define el contrato para la ventana de consultas de la aplicación.
 * Cualquier clase que implemente esta interfaz será responsable de
 * mostrar los controles para realizar una consulta y visualizar sus resultados.
 */
public interface VentanaConsultas {

    /**
     * Inicia y muestra la ventana de consultas.
     * @param stage El {@link Stage} sobre el cual se construirá la escena.
     */
    void start(Stage stage);

    /**
     * Cierra la ventana de consultas.
     * @param stage El {@link Stage} que debe ser cerrado.
     */
    void close(Stage stage);

    /**
     * Inyecta una instancia del {@link Coordinador} para permitir la comunicación
     * entre la vista y la lógica de negocio.
     * @param coord El coordinador a establecer.
     */
    void setCoordinador(Coordinador coord);

    /**
     * Método llamado por el coordinador para entregar los resultados de una consulta.
     * La implementación debe procesar esta lista y actualizar la interfaz de usuario
     * para mostrar las rutas encontradas.
     *
     * @param listaRecorridos La lista de rutas encontradas. Cada ruta es a su vez una lista de tramos ({@link Recorrido}).
     * @param paradaOrigen La parada de origen utilizada en la consulta.
     * @param paradaDestino La parada de destino utilizada en la consulta.
     * @param horaLlegaParada La hora de llegada a la parada de origen especificada.
     */
    void resultado(List<List<Recorrido>> listaRecorridos,
            Parada paradaOrigen,
            Parada paradaDestino,
            LocalTime horaLlegaParada);

}