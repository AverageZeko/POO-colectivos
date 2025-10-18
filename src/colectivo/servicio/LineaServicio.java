package colectivo.servicio;

import java.util.Map;
import colectivo.modelo.Linea;
import colectivo.modelo.Parada;

public interface LineaServicio {
    Map<String, Linea> buscarLineas(Map<Integer, Parada> paradas);
}
