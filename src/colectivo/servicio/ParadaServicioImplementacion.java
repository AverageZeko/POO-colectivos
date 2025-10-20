package colectivo.servicio;

import java.util.Map;
import colectivo.conexion.Factory;
import colectivo.dao.ParadaDAO;
import colectivo.modelo.Parada;

public class ParadaServicioImplementacion implements ParadaServicio{
    private ParadaDAO paradaDAO;

    public ParadaServicioImplementacion() {
        paradaDAO = (ParadaDAO) Factory.getInstancia("parada");
    }

    @Override
    public Map<Integer, Parada> buscarParadas() {
        return paradaDAO.buscarParadas();
    }
}
