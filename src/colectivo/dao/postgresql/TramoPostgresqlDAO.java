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

import colectivo.app.Constantes;
import colectivo.conexion.BDConexion;
import colectivo.controlador.Coordinador;
import colectivo.dao.ParadaDAO;
import colectivo.dao.TramoDAO;
import colectivo.modelo.Parada;
import colectivo.modelo.Tramo;
import colectivo.util.Factory;


/**
 * Implementación de {@link TramoDAO} que obtiene los tramos desde una base de datos PostgreSQL.
 *
 * <p>Esta clase se encarga de leer los datos de tramos desde una base
 * de datos PostgreSQL utilizando el esquema configurado en {@link SchemaPostgresqlDAO}. Los tramos
 * se cargan una sola vez y se almacenan en memoria para optimizar el acceso en
 * consultas posteriores.</p>
 *
 * <p>La conexión a la base de datos se obtiene mediante {@link BDConexion} y el esquema activo
 * se establece dinámicamente usando {@code SET search_path TO 'esquema'} antes de ejecutar
 * las consultas. Antes de cargar los tramos, se obtienen todas las paradas mediante
 * {@link ParadaDAO} para validar que las paradas de inicio y fin de cada tramo existan.</p>
 * @see TramoDAO
 * @see Tramo
 * @see ParadaDAO
 * @see BDConexion
 * @see SchemaPostgresqlDAO
 * @see Factory
 */
public class TramoPostgresqlDAO implements TramoDAO{
    private static final Logger TRAMO_DAO_LOG = LoggerFactory.getLogger("TramoDAO");
    private Map<String, Tramo> tramos;

    public TramoPostgresqlDAO() {
    }


    /**
     * Carga y devuelve todos los tramos desde la base de datos PostgreSQL.
     *
     * <p>Si los tramos ya fueron cargados previamente, devuelve el mapa almacenado en memoria.
     * En caso contrario, realiza las siguientes operaciones:</p>
     * <ol>
     *   <li>Obtiene todas las paradas mediante {@link ParadaDAO} para validación de integridad.</li>
     *   <li>Obtiene una conexión a la base de datos mediante {@link BDConexion#getConnection()}.</li>
     *   <li>Establece el esquema activo usando {@code SET search_path TO 'esquema'}.</li>
     *   <li>Ejecuta una consulta SELECT para obtener todos los tramos (inicio, fin, tiempo, tipo).</li>
     *   <li>Para cada registro:
     *     <ul>
     *       <li>Valida que las paradas de inicio y fin existan en el conjunto de paradas cargadas.</li>
     *       <li>Construye un objeto {@link Tramo} con las paradas correspondientes.</li>
     *       <li>Almacena el tramo en el mapa con la clave {@code "codigoInicio->codigoFin"}.</li>
     *     </ul>
     *   </li>
     *   <li>Cierra los recursos utilizados en la consulta en el bloque finally.</li>
     * </ol>
     *
     * @return un {@link Map} con los tramos indexados por clave {@code "codigoInicio->codigoFin"}.
     *         Nunca devuelve {@code null}.
     * @throws IllegalStateException si un tramo referencia una parada de inicio o fin que no existe
     *                               en el conjunto de paradas cargadas. La parada faltante se
     *                               incluye en el mensaje de error.
     * @throws RuntimeException      si ocurre un error SQL durante la consulta o al cerrar los recursos.
     */
    @Override
    public Map<String, Tramo> buscarTodos() {
        if (tramos == null) {
            tramos = new HashMap<>();
            Connection con = null;
            Statement schemaStatement = null;
            PreparedStatement selectStatement = null;
            ResultSet rs = null;
            String schema = Coordinador.getSchema();
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
                TRAMO_DAO_LOG.error(schemaStatement.toString());
                TRAMO_DAO_LOG.error(selectStatement.toString());
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
