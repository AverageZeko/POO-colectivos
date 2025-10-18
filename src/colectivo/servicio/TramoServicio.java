package colectivo.servicio;

import java.util.Map;
import colectivo.modelo.Parada;
import colectivo.modelo.Tramo;

public interface TramoServicio {
    Map<String, Tramo> buscarTramos(Map<Integer, Parada> paradas);
}
