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
import colectivo.controlador.Coordinador;
import colectivo.dao.ParadaDAO;
import colectivo.modelo.Parada;

/**
 * Implementación de {@link ParadaDAO} que obtiene las paradas desde una base de datos PostgreSQL.
 *
 * <p>Esta clase se encarga de leer los datos de paradas desde una base de datos PostgreSQL
 * utilizando el esquema configurado en {@link SchemaPostgresqlDAO}. Las paradas se cargan
 * una sola vez y se almacenan en memoria para optimizar el acceso en consultas
 * posteriores.</p>
 *
 * <p>La conexión a la base de datos se obtiene mediante {@link BDConexion} y el esquema activo
 * se establece dinámicamente usando {@code SET search_path TO 'esquema'} antes de ejecutar
 * las consultas.</p>
 *
 * @see ParadaDAO
 * @see Parada
 * @see BDConexion
 * @see SchemaPostgresqlDAO
 */
public class ParadaPostgresqlDAO implements ParadaDAO {
    private static final Logger PARADA_DAO_LOG = LoggerFactory.getLogger("ParadaDAO");
    private Map<Integer, Parada> paradas;
    
    public ParadaPostgresqlDAO() {
    }


    /**
     * Carga y devuelve todas las paradas desde la base de datos PostgreSQL.
     *
     * <p>Si las paradas ya fueron cargadas previamente, devuelve el mapa almacenado en memoria.
     * En caso contrario, realiza las siguientes operaciones:</p>
     * <ol>
     *   <li>Obtiene una conexión a la base de datos mediante {@link BDConexion#getConnection()}.</li>
     *   <li>Establece el esquema activo usando {@code SET search_path TO 'esquema'}.</li>
     *   <li>Ejecuta una consulta SELECT para obtener todas las paradas (código, dirección,
     *       latitud, longitud).</li>
     *   <li>Construye objetos {@link Parada} y los almacena en un mapa indexado por código.</li>
     *   <li>Cierra los recursos utilizados para la consulta en el bloque finally.</li>
     * </ol>
     *
     * @return un {@link Map} con las paradas indexadas por código. Nunca devuelve {@code null}.
     * @throws RuntimeException si ocurre un error SQL durante la consulta o al cerrar los recursos.
     *                          La excepción original ({@link SQLException}) se incluye como causa.
     */
    @Override
    public Map<Integer, Parada> buscarTodos() {
        if (paradas == null) {
            paradas = new HashMap<>();
            Connection con = null;
            Statement schemaStatement = null;
            PreparedStatement selectStatement = null;
            ResultSet rs = null;
            String schema = Coordinador.getSchema();
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
                PARADA_DAO_LOG.error(schemaStatement.toString());
                PARADA_DAO_LOG.error(selectStatement.toString());
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
