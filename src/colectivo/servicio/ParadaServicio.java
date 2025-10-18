package colectivo.servicio;

import java.util.Map;
import colectivo.modelo.Parada;

public interface ParadaServicio {
    Map<Integer, Parada> buscarParadas();
}
