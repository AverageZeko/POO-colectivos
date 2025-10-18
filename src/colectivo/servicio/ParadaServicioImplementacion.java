package colectivo.servicio;

import java.util.Map;
import colectivo.dao.ParadaDAO;
import colectivo.dao.secuencial.ParadaSecuencialDAO;
import colectivo.modelo.Parada;

public class ParadaServicioImplementacion implements ParadaServicio{
    private ParadaDAO paradaDAO;

    public ParadaServicioImplementacion() {
        paradaDAO = new ParadaSecuencialDAO();
    }

    @Override
    public Map<Integer, Parada> buscarParadas() {
        return paradaDAO.buscarParadas();
    }
}
