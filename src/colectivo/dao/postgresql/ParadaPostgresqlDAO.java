package colectivo.dao.postgresql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import colectivo.conexion.BDConexion;
import colectivo.dao.ParadaDAO;
import colectivo.modelo.Parada;

public class ParadaPostgresqlDAO implements ParadaDAO {
    private static final Logger PARADA_DAO_LOG = LoggerFactory.getLogger("ParadaDAO");
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
                PARADA_DAO_LOG.debug("Schema elegido");

                sql = "SELECT codigo, direccion, latitud, longitud FROM parada";
                selectStatement = con.prepareStatement(sql);
                rs = selectStatement.executeQuery();
                PARADA_DAO_LOG.debug("Consulta realizada para paradas");

                while (rs.next()) {
                    int codigo = rs.getInt("codigo");
                    String direccion = rs.getString("direccion");
                    double latitud = rs.getDouble("latitud");
                    double longitud = rs.getDouble("longitud");
                    Parada parada = new Parada(codigo, direccion, latitud, longitud);
                    paradas.put(codigo, parada);
                }

            } catch (SQLException e) {
                PARADA_DAO_LOG.debug("No se pudo realizar la consulta de paradas", e);
                throw new RuntimeException("No se pudo realizar la consulta de paradas", e);
            }   finally {
                try {
                    if (rs != null) {
                        rs.close();
                        PARADA_DAO_LOG.debug("ResultSet de paradas cerrado");
                    }
                    if (selectStatement != null) {
                        selectStatement.close();
                        PARADA_DAO_LOG.debug("PreparedStatement de paradas cerrado");
                    }
                    if (schemaStatement != null) {
                        schemaStatement.close();
                        PARADA_DAO_LOG.debug("Statement de schema cerrado");
                    }
                } catch (SQLException e) {
                    PARADA_DAO_LOG.error("Hubo un error al cerrar los objetos de la consulta de paradas", e);
                    throw new RuntimeException("Hubo un error al cerrar los objetos de la consulta de paradas", e);
                }
            }
        }

        PARADA_DAO_LOG.info("Paradas cargadas");
        return paradas;
    }
    
}
