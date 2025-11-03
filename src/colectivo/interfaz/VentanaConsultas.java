package colectivo.interfaz;

import java.time.LocalTime;
import java.util.List;

import colectivo.controlador.Coordinador;
import colectivo.modelo.Parada;
import colectivo.modelo.Recorrido;
import javafx.stage.Stage;

public interface VentanaConsultas {
    void start(Stage stage);
    void close(Stage stage);
    void setCoordinador(Coordinador coord);
    void resultado(List<List<Recorrido>> listaRecorridos,
            Parada paradaOrigen,
            Parada paradaDestino,
            LocalTime horaLlegaParada);

}
