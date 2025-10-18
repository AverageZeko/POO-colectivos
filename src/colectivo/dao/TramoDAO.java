package colectivo.dao;

import java.util.Map;
import colectivo.modelo.Parada;
import colectivo.modelo.Tramo;

public interface TramoDAO {
    Map<String, Tramo> buscarTramos(Map<Integer, Parada> paradas);
}
