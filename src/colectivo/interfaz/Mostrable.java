package colectivo.interfaz;

import java.time.LocalTime;
import java.util.List;

import colectivo.controlador.Coordinador;
import colectivo.modelo.Parada;
import colectivo.modelo.Recorrido;

public interface Mostrable {
    void lanzarAplicacion(String[] args);
    void setCoordinador(Coordinador coord);
    void resultado(List<List<Recorrido>> listaRecorridos,
            Parada paradaOrigen,
            Parada paradaDestino,
            LocalTime horaLlegaParada);

}
