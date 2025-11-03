package colectivo.interfaz;

import colectivo.controlador.Coordinador;
import javafx.stage.Stage;

public interface VentanaInicial {
    void setCoordinador(Coordinador coord);
    void lanzarAplicacion(String[] args);
    void close(Stage stage);
}
