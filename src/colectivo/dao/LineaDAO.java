package colectivo.dao;

import java.util.Map;
import colectivo.modelo.Linea;
import colectivo.modelo.Parada;

public interface LineaDAO {
    Map<String, Linea> buscarLineas(Map<Integer, Parada> paradas);
}