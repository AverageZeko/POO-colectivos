package colectivo.dao.postgresql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import colectivo.conexion.BDConexion;
import colectivo.dao.ParadaDAO;
import colectivo.modelo.Parada;

public class ParadaPostgresqlDAO implements ParadaDAO {
    private Map<Integer, Parada> paradas;
    
    public ParadaPostgresqlDAO() {
    }

    @Override
    public Map<Integer, Parada> buscarTodos() {
        if (paradas == null) {
            paradas = new HashMap<>();
            Connection con = null;
            PreparedStatement pstm = null;
            ResultSet rs = null;
            String schema = SchemaPostgresqlDAO.getSchema();
            try {
                con = BDConexion.getConnection();

                String sql = "SELECT codigo, direccion, latitud, longitud FROM " + schema + ".parada";
                pstm = con.prepareStatement(sql);
                rs = pstm.executeQuery();

                while (rs.next()) {
                    int codigo = rs.getInt("codigo");
                    String direccion = rs.getString("direccion");
                    double latitud = rs.getDouble("latitud");
                    double longitud = rs.getDouble("longitud");
                    Parada parada = new Parada(codigo, direccion, latitud, longitud);
                    paradas.put(codigo, parada);
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
        
        return paradas;
    }
    
}
