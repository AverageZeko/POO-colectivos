package colectivo.dao.postgresql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
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
            Statement schemaStatement = null;
            PreparedStatement selectStatement = null;
            ResultSet rs = null;
            String schema = SchemaPostgresqlDAO.getSchema();
            try {
                con = BDConexion.getConnection();

                String sql = String.format("SET search_path TO '%s'", schema);
                schemaStatement = con.createStatement();
                schemaStatement.execute(sql);

                sql = "SELECT codigo, direccion, latitud, longitud FROM parada";
                selectStatement = con.prepareStatement(sql);
                rs = selectStatement.executeQuery();

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
                    if (selectStatement != null) {
                        selectStatement.close();
                    }
                    if (schemaStatement != null) {
                        schemaStatement.close();
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
