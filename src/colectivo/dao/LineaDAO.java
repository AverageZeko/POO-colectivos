package colectivo.dao;

import java.util.Map;
import colectivo.modelo.Linea;

public interface LineaDAO {
    Map<String, Linea> buscarTodos();
}