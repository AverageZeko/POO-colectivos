package colectivo.conexion;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class BDConexion {
    private static final String RUTA_BD = "resources/postgresql.properties";
    private static Connection conexion = null;

    public static Connection getConnection() {
            try {
                if (conexion == null) {
                    Properties prop = new Properties();
                    InputStream entrada = BDConexion.class.getClassLoader().getResourceAsStream(RUTA_BD);

                    if (entrada == null) {
                        //  TODO: LOGGER
                        throw new IllegalStateException("No fue posible encontrar archivo postgresql.properties en el class path.");
                    }
                    try {
                        prop.load(entrada);
                    } catch (IOException e) {
                        //  TODO: LOGGER
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
                    
                }
                return conexion;
            } catch (Exception ex) {
                //  TODO: LOGGER
                ex.printStackTrace();
                throw new RuntimeException("Error al crear la conexion a la base de datos", ex);
            }
        }

        public static class MiShDwnHook extends Thread {
            // justo antes de finalizar el programa la JVM invocara
            // a este metodo donde podemos cerrar la conexion
            public void run() {
                try {
                    Connection conexion = BDConexion.getConnection();
                    conexion.close();
                } catch (Exception ex) {
                    //  TODO: LOGGER
                    ex.printStackTrace();
                    throw new RuntimeException(ex);
                }
            }
        }

}
