package colectivo.servicio;

import java.util.Map;
import colectivo.modelo.Linea;

public interface LineaServicio {
    Map<String, Linea> buscarLineas();
}
