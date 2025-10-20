package colectivo.servicio;

import java.util.Map;
import colectivo.conexion.Factory;
import colectivo.dao.TramoDAO;
import colectivo.modelo.Parada;
import colectivo.modelo.Tramo;

public class TramoServicioImplementacion implements TramoServicio{
    private TramoDAO tramoDAO;

    public TramoServicioImplementacion() {
        tramoDAO = (TramoDAO) Factory.getInstancia("tramo");
    }
    
    @Override
    public Map<String, Tramo> buscarTramos(Map<Integer, Parada> paradas) {
        return tramoDAO.buscarTramos(paradas);
    }
    
}
