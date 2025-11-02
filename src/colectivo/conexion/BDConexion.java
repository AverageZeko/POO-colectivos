package colectivo.conexion;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BDConexion {
    private static final Logger BD_LOG = LoggerFactory.getLogger(BDConexion.class);
    private static final String RUTA_BD = "resources/postgresql.properties";
    private static Connection conexion = null;

    public static Connection getConnection() {
            try {
                if (conexion == null) {
                    Properties prop = new Properties();
                    InputStream entrada = BDConexion.class.getClassLoader().getResourceAsStream(RUTA_BD);

                    if (entrada == null) {
                        BD_LOG.error("No fue posible encontrar archivo postgresql.properties en el class path.");
                        throw new IllegalStateException("No fue posible encontrar archivo postgresql.properties en el class path.");
                    }
                    try {
                        prop.load(entrada);
                    } catch (IOException e) {
                        BD_LOG.error("Error al cargar " + RUTA_BD, e);
                        throw new IllegalStateException("Error al cargar postgresql.properties", e);
                    }
                    
                    // con esto determinamos cuando finalize el programa
                    Runtime.getRuntime().addShutdownHook(new MiShDwnHook());
                    String driver = prop.getProperty("driver");
                    String url = prop.getProperty("url");
                    String usr = prop.getProperty("usr");
                    String pwd = prop.getProperty("pwd");
                    Class.forName(driver);
                    conexion = DriverManager.getConnection(url, usr, pwd);
                    if (conexion != null) {
                        BD_LOG.info("Conexion establecida a {} (user={})", url, usr);
                    }
                    
                }
                return conexion;
            } catch (Exception e) {
                BD_LOG.error("Error al crear la conexión a la base de datos", e);
                throw new RuntimeException("Error al crear la conexion a la base de datos", e);
            }
        }

    public static class MiShDwnHook extends Thread {
        // justo antes de finalizar el programa la JVM invocara
        // a este metodo donde podemos cerrar la conexion
        public void run() {
            Connection con = conexion;
            if (con != null) {
                try {
                    con.close();
                    BD_LOG.info("Conexion a la base de datos cerrada");
                }   catch (Exception e) {
                    BD_LOG.error("Error al cerrar la conexion utilizando shutdown hook", e);
                }
            }   else {
                BD_LOG.warn("No hay conexion para cerrar");
            }
        }
    }
}
