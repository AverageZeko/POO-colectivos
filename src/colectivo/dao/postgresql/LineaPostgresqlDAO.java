package colectivo.dao.postgresql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import colectivo.conexion.BDConexion;
import colectivo.dao.LineaDAO;
import colectivo.modelo.Linea;

public class LineaPostgresqlDAO implements LineaDAO{
    private Map<String, Linea> lineas;

    public LineaPostgresqlDAO() {
    }

    @Override
    public Map<String, Linea> buscarTodos() {
        if (lineas == null) {
            lineas = new HashMap<>();
            Map<String, List<String[]>> frecuencias = buscarFrecuencias();
            Connection con = null;
            PreparedStatement pstm = null;
            ResultSet rs = null;
            String schema = SchemaPostgresqlDAO.getSchema();
            try {
                con = BDConexion.getConnection();

                String sql = "SELECT codigo, nombre FROM " + schema + ".linea";
                pstm = con.prepareStatement(sql);
                rs = pstm.executeQuery();

                while (rs.next()) {
                    String codigoLinea = rs.getString("codigo");
                    String nombreLinea = rs.getString("nombre");
                    Linea lineaActual = new Linea(codigoLinea, nombreLinea);

                    // TODO: Buscar secuencia de parada para la linea a traves de linea_parada

                    //Vincular frecuencias a la linea
                    if (frecuencias.containsKey(codigoLinea)) {
                        for (String[] detallesFrecuencia : frecuencias.get(codigoLinea)) {
                            try {
                                int diaSemana = Integer.parseInt(detallesFrecuencia[0]);
                                LocalTime inicioRecorrido = LocalTime.parse(detallesFrecuencia[1]);
                                lineaActual.agregarFrecuencia(diaSemana, inicioRecorrido);
                            }	catch (IllegalArgumentException e) {
                                //  TODO: LOGGER
                                throw new IllegalStateException("Frecuencia invalida para linea: " + frecuencias.get(codigoLinea), e);
                            }
                        }
                    }	else {
                        //  TODO: LOGGER
                        throw new IllegalStateException("AVISO: No se encontro ninguna frecuencia para linea " + codigoLinea);
                    }

                    lineas.put(codigoLinea, lineaActual);
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
        
        return lineas;
    }

    private Map<String, List<String[]>> buscarFrecuencias() {
        Map<String, List<String[]>> frecuencias = new HashMap<>();

        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;
        String schema = SchemaPostgresqlDAO.getSchema();

        try {
            con = BDConexion.getConnection();

            String sql = "SELECT linea, diasemana, hora FROM " + schema + ".linea_frecuencia";
            pstm = con.prepareStatement(sql);
            rs = pstm.executeQuery();

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
            //  TODO: LOGGER
            e.printStackTrace();
            throw new RuntimeException("No se pudieron ejecutar las sentencias SQL", e);
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
                throw new RuntimeException("No se pudieron cerrar los recursos de las sentencias SQL", e);
            }
        }

        return frecuencias;
    
    }
}
