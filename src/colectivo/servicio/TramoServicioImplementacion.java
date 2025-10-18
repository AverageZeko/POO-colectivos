package colectivo.servicio;

import java.util.Map;
import colectivo.dao.TramoDAO;
import colectivo.dao.secuencial.TramoSecuencialDAO;
import colectivo.modelo.Parada;
import colectivo.modelo.Tramo;

public class TramoServicioImplementacion implements TramoServicio{
    private TramoDAO tramoDAO;

    public TramoServicioImplementacion() {
        tramoDAO = new TramoSecuencialDAO();
    }
    
    @Override
    public Map<String, Tramo> buscarTramos(Map<Integer, Parada> paradas) {
        return tramoDAO.buscarTramos(paradas);
    }
    
}
