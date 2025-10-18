package colectivo.servicio;

import java.util.Map;
import colectivo.dao.LineaDAO;
import colectivo.dao.secuencial.LineaSecuencialDAO;
import colectivo.modelo.Linea;
import colectivo.modelo.Parada;

public class LineaServicioImplementacion implements LineaServicio{
    private LineaDAO lineaDAO;

    public LineaServicioImplementacion() {
        lineaDAO = new LineaSecuencialDAO();
    }

    @Override
    public Map<String, Linea> buscarLineas(Map<Integer, Parada> paradas) {
        return lineaDAO.buscarLineas(paradas);
    }
    
}
