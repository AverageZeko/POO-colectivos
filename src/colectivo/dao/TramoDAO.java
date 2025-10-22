package colectivo.dao;

import java.util.Map;
import colectivo.modelo.Tramo;

public interface TramoDAO {
    Map<String, Tramo> buscarTodos();
}
