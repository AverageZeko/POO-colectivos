package colectivo.dao.postgresql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import colectivo.conexion.BDConexion;
import colectivo.conexion.Factory;
import colectivo.controlador.Constantes;
import colectivo.dao.ParadaDAO;
import colectivo.dao.TramoDAO;
import colectivo.modelo.Parada;
import colectivo.modelo.Tramo;

public class TramoPostgresqlDAO implements TramoDAO{
    private Map<String, Tramo> tramos;

    public TramoPostgresqlDAO() {
    }

    @Override
    public Map<String, Tramo> buscarTodos() {
        if (tramos == null) {
            tramos = new HashMap<>();
            Connection con = null;
            PreparedStatement pstm = null;
            ResultSet rs = null;
            String schema = SchemaPostgresqlDAO.getSchema();
            try {
                ParadaDAO paradaDAO = (ParadaDAO) Factory.getInstancia(Constantes.PARADA);
                Map<Integer, Parada> paradas = paradaDAO.buscarTodos();
                con = BDConexion.getConnection();

                String sql = "SELECT inicio, fin, tiempo, tipo FROM " + schema + ".tramo";
                pstm = con.prepareStatement(sql);
                rs = pstm.executeQuery();

                while (rs.next()) {
                    int inicio = rs.getInt("inicio");
                    int fin = rs.getInt("fin");

                    if (!paradas.containsKey(inicio)) {
                        //  TODO: LOGGER
                        throw new IllegalStateException("AVISO: Parada inicial no fue encontrada " + inicio);
                    }

                    if (!paradas.containsKey(fin)) {
                        //  TODO: LOGGER
                        throw new IllegalStateException("AVISO: Parada final no fue encontrada " + fin);
                    }

                    Parada paradaInicial = paradas.get(inicio);
                    Parada paradaFinal = paradas.get(fin);
                    int tiempo = rs.getInt("tiempo");
                    int tipo = rs.getInt("tipo");
                    Tramo tramoActual = new Tramo(paradaInicial, paradaFinal, tiempo, tipo);
                    
                    String tramoKey = String.format("%d->%d", paradaInicial.getCodigo(), paradaFinal.getCodigo());
                    tramos.put(tramoKey, tramoActual);
                }

            } catch (SQLException e) {
                //  TODO: LOGGER
                e.printStackTrace();
                throw new RuntimeException(e);
            }   finally {
                try {
                    if (rs != null) {
                        rs.close();
                    }
                    if (pstm != null) {
                        pstm.close();
                    }
                } catch (SQLException e) {
                    //  TODO: LOGGER
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }
        }
        
        return tramos;
    }
    
}
