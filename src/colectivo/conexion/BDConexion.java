package colectivo.conexion;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Clase singleton para la gestión de conexiones a la base de datos PostgreSQL.
 *
 * <p>Esta clase proporciona una única conexión compartida a la base de datos durante
 * la ejecución de la aplicación. La conexión se inicializa unicamente cuando
 * se solicita por primera vez y se cierra automáticamente al finalizar el programa
 * mediante un shutdown hook registrado en la JVM.</p>
 *
 * <p>La configuración de la conexión (driver, URL, usuario y contraseña) se lee desde
 * el archivo {@code postgresql.properties} ubicado en el classpath bajo la carpeta
 * {@code resources}. Si el archivo no se encuentra o hay errores de configuración,
 * se lanzan excepciones {@link IllegalStateException} o {@link RuntimeException}.</p>
 */
public class BDConexion {
    private static final Logger BD_LOG = LoggerFactory.getLogger("BDConexion");
    private static final String RUTA_BD = "resources/postgresql.properties";
    private static Connection conexion = null;

    /**
     * Obtiene la conexión única a la base de datos PostgreSQL.
     *
     * <p>Si la conexión no existe, este método la crea leyendo la configuración desde
     * {@code postgresql.properties} (driver, URL, usuario y contraseña), registra un
     * shutdown hook para cerrarla al finalizar el programa, y establece la conexión
     * mediante {@link DriverManager}.</p>
     *
     * <p>Si la conexión ya existe, devuelve la instancia previamente creada.</p>
     *
     * @return la conexión activa a la base de datos.
     * @throws IllegalStateException si el archivo {@code postgresql.properties} no se encuentra
     *                               en el classpath o si ocurre un error al cargarlo.
     * @throws RuntimeException      si ocurre cualquier error al crear la conexión (por ejemplo,
     *                               driver no encontrado, credenciales inválidas, URL incorrecta).
     */
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


    /**
     * Thread que se ejecuta automáticamente al finalizar la JVM.
     *
     * <p>Esta clase interna se registra como shutdown hook en el método {@link #getConnection()}
     * para garantizar que la conexión a la base de datos se cierre adecuadamente antes de que
     * la aplicación termine, liberando recursos y evitando conexiones indeseadas.</p>
     *
     * @see Runtime#addShutdownHook(Thread)
     */
    public static class MiShDwnHook extends Thread {

        /**
         * Ejecuta el cierre de la conexión a la base de datos.
         *
         * <p>Este método se invoca automáticamente por la JVM justo antes de finalizar el programa.
         * Intenta cerrar la conexión si existe y registra el resultado.</p>
         */
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
