package colectivo.dao.postgresql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import colectivo.app.Constantes;
import colectivo.conexion.BDConexion;
import colectivo.controlador.Coordinador;
import colectivo.dao.LineaDAO;
import colectivo.dao.ParadaDAO;
import colectivo.modelo.Linea;
import colectivo.modelo.Parada;
import colectivo.util.Factory;


    /**
     * Implementación de {@link LineaDAO} que obtiene las líneas de colectivos desde una base de datos PostgreSQL.
     *
     * <p>Esta clase se encarga de leer los datos de líneas desde una base de datos
     * PostgreSQL utilizando el esquema configurado en {@link SchemaPostgresqlDAO}. Las líneas se cargan
     * una sola vez y se almacenan en memoria para optimizar el acceso en consultas posteriores.</p>
     *
     * <p>Cada línea se construye a partir de tres consultas SQL independientes:</p>
     * <ol>
     *   <li><b>Líneas principales:</b> consulta {@code linea} para obtener código y nombre.</li>
     *   <li><b>Secuencias de paradas:</b> consulta {@code linea_parada} para obtener la secuencia ordenada
     *       de paradas que componen la ruta de cada línea.</li>
     *   <li><b>Frecuencias:</b> consulta {@code linea_frecuencia} para obtener los horarios de salida de cada línea.</li>
     * </ol>
     *
     * <p>La conexión a la base de datos se obtiene mediante {@link BDConexion} y se reutiliza entre
     * las diferentes consultas (líneas, frecuencias y secuencias). El esquema activo se establece
     * dinámicamente usando {@code SET search_path TO 'esquema'} antes de ejecutar las consultas.</p>
     *
     * @see LineaDAO
     * @see Linea
     * @see ParadaDAO
     * @see BDConexion
     * @see SchemaPostgresqlDAO
     * @see Factory
     */
public class LineaPostgresqlDAO implements LineaDAO{
    private static final Logger LINEA_DAO_LOG = LoggerFactory.getLogger("LineaDAO");
    private Map<String, Linea> lineas;
    private Connection con;

    public LineaPostgresqlDAO() {
    }


    /**
     * Carga y devuelve todas las líneas de colectivos desde la base de datos PostgreSQL.
     *
     * <p>Si las líneas ya fueron cargadas previamente, devuelve el mapa almacenado en memoria.
     * En caso contrario, realiza las siguientes operaciones:</p>
     * <ol>
     *   <li>Obtiene todas las paradas mediante {@link ParadaDAO}.</li>
     *   <li>Obtiene una conexión a la base de datos mediante {@link BDConexion#getConnection()}.</li>
     *   <li>Establece el esquema activo usando {@code SET search_path TO 'esquema'}.</li>
     *   <li>Llama {@link #buscarFrecuencias()} para cargar todas las frecuencias de las líneas.</li>
     *   <li>Llama {@link #buscarSecuencias()} para cargar las secuencias de paradas de cada línea.</li>
     *   <li>Ejecuta una consulta SELECT para obtener todas las líneas (código y nombre).</li>
     *   <li>Para cada línea:
     *     <ul>
     *       <li>Vincula la secuencia de paradas obtenida previamente.</li>
     *       <li>Valida y vincula las frecuencias. Si no hay frecuencias o tienen formato inválido,
     *           lanza {@link IllegalStateException}.</li>
     *       <li>Almacena la línea completa en el mapa indexado por código.</li>
     *     </ul>
     *   </li>
     *   <li>Cierra los recursos usados en la consulta en el bloque finally.</li>
     * </ol>
     *
     * @return un {@link Map} con las líneas indexadas por código de línea. Nunca devuelve {@code null}.
     * @throws IllegalStateException si una línea no tiene frecuencias configuradas o si una frecuencia
     *                               tiene formato inválido (día de semana o hora mal formateados).
     * @throws RuntimeException      si ocurre un error SQL durante la consulta o al cerrar los recursos.
     */
    @Override
    public Map<String, Linea> buscarTodos() {
        if (lineas == null) {
            lineas = new HashMap<>();
            Map<String, List<String[]>> frecuencias;
            Map<String, List<Integer>> secuencias;
            con = null;
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
                LINEA_DAO_LOG.debug("Schema elegido");

                frecuencias = buscarFrecuencias();
                secuencias = buscarSecuencias();

                sql = "SELECT codigo, nombre FROM linea";
                selectStatement = con.prepareStatement(sql);
                rs = selectStatement.executeQuery();
                LINEA_DAO_LOG.debug("Consulta realizada para lineas");

                while (rs.next()) {
                    String codigoLinea = rs.getString("codigo");
                    String nombreLinea = rs.getString("nombre");
                    Linea lineaActual = new Linea(codigoLinea, nombreLinea);

                    //Vincular secuencia de paradas a la linea
                    List<Integer> paradasId = secuencias.get(codigoLinea);
                    for (Integer ent : paradasId) {
                        lineaActual.agregarParada(paradas.get(ent));
                    }

                    //Vincular frecuencias a la linea
                    if (frecuencias.containsKey(codigoLinea)) {
                        for (String[] detallesFrecuencia : frecuencias.get(codigoLinea)) {
                            int diaSemana = 0;
                            LocalTime inicioRecorrido = null;
                            try {
                                diaSemana = Integer.parseInt(detallesFrecuencia[0]);
                                inicioRecorrido = LocalTime.parse(detallesFrecuencia[1]);
                                lineaActual.agregarFrecuencia(diaSemana, inicioRecorrido);
                            }	catch (IllegalArgumentException e) {
                                LINEA_DAO_LOG.error("Frecuencia [{}, {}] invalida para linea {}", diaSemana, inicioRecorrido, codigoLinea, e);
                                throw new IllegalStateException("Frecuencia invalida para linea: " + frecuencias.get(codigoLinea), e);
                            }
                        }
                    }	else {
                        LINEA_DAO_LOG.error("No se encontro ninguna frecuencia para linea {}", codigoLinea);
                        throw new IllegalStateException("No se encontro ninguna frecuencia para linea " + codigoLinea);
                    }

                    lineas.put(codigoLinea, lineaActual);
                }

            } catch (SQLException e) {
                LINEA_DAO_LOG.error("No se pudo realizar la consulta de lineas", e);
                LINEA_DAO_LOG.error(schemaStatement.toString());
                LINEA_DAO_LOG.error(selectStatement.toString());
                throw new RuntimeException("No se pudo realizar la consulta de lineas", e);
            }   finally {
                try {
                    if (rs != null) {
                        rs.close();
                        LINEA_DAO_LOG.debug("ResultSet de lineas cerrado");
                    }
                    if (selectStatement != null) {
                        selectStatement.close();
                        LINEA_DAO_LOG.debug("PreparedStatement de lineas cerrado");
                    }
                    if (schemaStatement != null) {
                        schemaStatement.close();
                        LINEA_DAO_LOG.debug("Statement de schema cerrado");
                    }
                } catch (SQLException e) {
                    LINEA_DAO_LOG.error("Hubo un error al cerrar los objetos de la consulta de lineas", e);
                    throw new RuntimeException("Hubo un error al cerrar los objetos de la consulta de lineas", e);
                }
            }
        }
        LINEA_DAO_LOG.info("Lineas cargadas");
        return lineas;
    }


    /**
     * Carga las frecuencias de todas las líneas desde la tabla {@code linea_frecuencia}.
     *
     * <p>Este método auxiliar es invocado por {@link #buscarTodos()} y utiliza la conexión
     * almacenada en {@link #con}. Devuelve un mapa donde la clave es el código de línea y el
     * valor es una lista de arreglos de cadenas con los detalles de frecuencia (día de la semana
     * y hora de salida).</p>
     *
     * @return un {@link Map} con las frecuencias agrupadas por código de línea. Cada entrada contiene
     *         una lista de arreglos donde el primer elemento es el día de la semana y
     *         el siguiente elemento es la hora en formato {@code HH:mm}.
     * @throws RuntimeException si ocurre un error SQL durante la consulta o al cerrar los recursos.
     */
    private Map<String, List<String[]>> buscarFrecuencias() {
        Map<String, List<String[]>> frecuencias = new HashMap<>();

        PreparedStatement selectStatement = null;
        ResultSet rs = null;
        try {
            String sql = "SELECT linea, diasemana, hora FROM linea_frecuencia";
            selectStatement = con.prepareStatement(sql);
            rs = selectStatement.executeQuery();

            LINEA_DAO_LOG.debug("Consulta realizada para frecuencias de cada linea");

            while (rs.next()) {
                String codigoLinea = rs.getString("linea");

                String diaSemana = rs.getString("diasemana");
                String hora = rs.getString("hora");
                String detallesFrecuencia[] = {diaSemana, hora};

                if (!frecuencias.containsKey(codigoLinea)) {
                    ArrayList<String[]> lista = new ArrayList<>();
                    lista.add(detallesFrecuencia);
                    frecuencias.put(codigoLinea, lista);
                }   else {
                    List<String[]> lista = frecuencias.get(codigoLinea);
					lista.add(detallesFrecuencia);
                }
            }  

        } catch (SQLException e) {
            LINEA_DAO_LOG.error("No se pudo ejecutar la consulta para frecuencias", e);
            throw new RuntimeException("No se pudo ejecutar la consulta para frecuencias", e);
        }   finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (selectStatement != null) {
                    selectStatement.close();
                }
            } catch (SQLException e) {
                LINEA_DAO_LOG.error("No se pudieron cerrar los objetos de la consulta de frecuencias", e);
                throw new RuntimeException("No se pudieron cerrar los objetos de la consulta de frecuencias", e);
            }
        }

        LINEA_DAO_LOG.info("Frecuencias de las lineas cargadas");
        return frecuencias;
    }


    /**
     * Carga las secuencias de paradas de todas las líneas desde la tabla {@code linea_parada}.
     *
     * <p>Este método auxiliar es invocado por {@link #buscarTodos()} y utiliza la conexión
     * almacenada en {@link #con}. Devuelve un mapa donde la clave es el código de línea y el
     * valor es una lista ordenada de códigos de paradas que componen la ruta de esa línea.</p>
     *
     * <p>La consulta ordena los resultados por {@code linea} y {@code secuencia} para asegurar
     * que las paradas se cargan en el orden correcto.</p>
     *
     * @return un {@link Map} con las secuencias de paradas agrupadas por código de línea. Cada entrada
     *         contiene una lista ordenada de códigos de parada ({@code Integer}) que representan la
     *         ruta de la línea.
     * @throws RuntimeException si ocurre un error SQL durante la consulta o al cerrar los recursos.
     */
    private Map<String, List<Integer>> buscarSecuencias() {
        Map<String, List<Integer>> secuencias = new HashMap<>();

        PreparedStatement selectStatement = null;
        ResultSet rs = null;
        try {
            String sql = "SELECT linea, parada FROM linea_parada ORDER BY linea, secuencia";
            selectStatement = con.prepareStatement(sql);
            rs = selectStatement.executeQuery();

            LINEA_DAO_LOG.debug("Consulta realizada para secuencia de cada linea");
            while (rs.next()) {
                String codigoLinea = rs.getString("linea");
                int parada = rs.getInt("parada");

                if(!secuencias.containsKey(codigoLinea)) {
                    ArrayList<Integer> lista = new ArrayList<>();
                    lista.add(parada);
                    secuencias.put(codigoLinea, lista);
                }   else {
                    List<Integer> lista = secuencias.get(codigoLinea);
                    lista.add(parada);
                }
            }  

        } catch (SQLException e) {
            LINEA_DAO_LOG.error("No se pudo realizar la consulta de secuencias", e);
            throw new RuntimeException("No se pudo realizar la consulta de secuencias", e);
        }   finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (selectStatement != null) {
                    selectStatement.close();
                }
            } catch (SQLException e) {
                LINEA_DAO_LOG.error("No se pudieron cerrar los recursos de la consulta de secuencias", e);
                throw new RuntimeException("No se pudieron cerrar los recursos de la consulta de secuencias", e);
            }
        }
        LINEA_DAO_LOG.info("Secuencias de cada linea cargadas");
        return secuencias;
    }
}
