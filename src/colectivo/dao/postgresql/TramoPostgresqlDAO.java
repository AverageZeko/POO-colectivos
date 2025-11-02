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
import colectivo.conexion.Factory;
import colectivo.controlador.Constantes;
import colectivo.dao.ParadaDAO;
import colectivo.dao.TramoDAO;
import colectivo.modelo.Parada;
import colectivo.modelo.Tramo;

public class TramoPostgresqlDAO implements TramoDAO{
    private static final Logger TRAMO_DAO_LOG = LoggerFactory.getLogger("TramoDAO");
    private Map<String, Tramo> tramos;

    public TramoPostgresqlDAO() {
    }

    @Override
    public Map<String, Tramo> buscarTodos() {
        if (tramos == null) {
            tramos = new HashMap<>();
            Connection con = null;
            Statement schemaStatement = null;
            PreparedStatement selectStatement = null;
            ResultSet rs = null;
            String schema = SchemaPostgresqlDAO.getSchema();
            try {
                ParadaDAO paradaDAO = (ParadaDAO) Factory.getInstancia(Constantes.PARADA);
                Map<Integer, Parada> paradas = paradaDAO.buscarTodos();
                con = BDConexion.getConnection();
                
                String sql = String.format("SET search_path TO '%s'", schema);
                schemaStatement = con.createStatement();
                schemaStatement.execute(sql);
                TRAMO_DAO_LOG.debug("Schema elegido");

                sql = "SELECT inicio, fin, tiempo, tipo FROM tramo";
                selectStatement = con.prepareStatement(sql);
                rs = selectStatement.executeQuery();
                TRAMO_DAO_LOG.debug("Consulta realizada para tramos");

                while (rs.next()) {
                    int inicio = rs.getInt("inicio");
                    int fin = rs.getInt("fin");

                    if (!paradas.containsKey(inicio)) {
                        TRAMO_DAO_LOG.error("La parada inicial {} no se encuentra en el conjunto de paradas", inicio);
                        throw new IllegalStateException(String.format("La parada inicial %s no se encuentra en el conjunto de paradas ", inicio));
                    }

                    if (!paradas.containsKey(fin)) {
                        TRAMO_DAO_LOG.error("La parada final {} no se encuentra en el conjunto de paradas", inicio);
                        throw new IllegalStateException(String.format("La parada final %s no se encuentra en el conjunto de paradas ", fin));
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
                TRAMO_DAO_LOG.error("No se pudo realizar la consulta de tramos", e);
                throw new RuntimeException("No se pudo realizar la consulta de tramos", e);
            }   finally {
                try {
                    if (rs != null) {
                        rs.close();
                        TRAMO_DAO_LOG.debug("ResultSet de tramos cerrado");
                    }
                    if (selectStatement != null) {
                        selectStatement.close();
                        TRAMO_DAO_LOG.debug("PreparedStatement de tramos cerrado");
                    }
                    if (schemaStatement != null) {
                        schemaStatement.close();
                        TRAMO_DAO_LOG.debug("Statement de tramos cerrado");
                    }
                } catch (SQLException e) {
                    TRAMO_DAO_LOG.error("Hubo un error al cerrar los objetos de la consulta de tramos", e);
                    throw new RuntimeException("Hubo un error al cerrar los objetos de la consulta de tramos", e);
                }
            }
        }

        TRAMO_DAO_LOG.info("Tramos cargados");
        return tramos;
    }
    
}
