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

import colectivo.conexion.BDConexion;
import colectivo.conexion.Factory;
import colectivo.controlador.Constantes;
import colectivo.dao.LineaDAO;
import colectivo.dao.ParadaDAO;
import colectivo.modelo.Linea;
import colectivo.modelo.Parada;

public class LineaPostgresqlDAO implements LineaDAO{
    private static final Logger LINEA_DAO_LOG = LoggerFactory.getLogger("LineaDAO");
    private Map<String, Linea> lineas;
    private Connection con;

    public LineaPostgresqlDAO() {
    }

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
            String schema = SchemaPostgresqlDAO.getSchema();
            
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
