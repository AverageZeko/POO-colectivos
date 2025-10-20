package colectivo.servicio;

import java.util.Map;
import colectivo.conexion.Factory;
import colectivo.dao.LineaDAO;
import colectivo.modelo.Linea;
import colectivo.modelo.Parada;

public class LineaServicioImplementacion implements LineaServicio{
    private LineaDAO lineaDAO;

    public LineaServicioImplementacion() {
        lineaDAO = (LineaDAO) Factory.getInstancia("linea");
    }

    @Override
    public Map<String, Linea> buscarLineas(Map<Integer, Parada> paradas) {
        return lineaDAO.buscarLineas(paradas);
    }
    
}
