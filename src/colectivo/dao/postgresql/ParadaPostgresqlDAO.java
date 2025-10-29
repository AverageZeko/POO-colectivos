package colectivo.dao.postgresql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import colectivo.conexion.BDConexion;
import colectivo.dao.ParadaDAO;
import colectivo.modelo.Parada;

public class ParadaPostgresqlDAO implements ParadaDAO {
    private Map<Integer, Parada> paradas;
    private boolean cambiarCiudad;
    
    public ParadaPostgresqlDAO() {
        paradas = new HashMap<>();
        cambiarCiudad = false;
    }

    @Override
    public Map<Integer, Parada> buscarTodos() {
        Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
        String schema = SchemaPostgresqlDAO.getSchema();
        try {
            con = BDConexion.getConnection();
			String sql = "SELECT codigo, nombre FROM public.lineas ";
            pstm = con.prepareStatement(sql);
            rs = pstm.executeQuery();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }
    
}
