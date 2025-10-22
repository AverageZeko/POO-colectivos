package colectivo.dao;

import java.util.Map;
import colectivo.modelo.Parada;

public interface ParadaDAO {
    Map<Integer, Parada> buscarTodos();
}
