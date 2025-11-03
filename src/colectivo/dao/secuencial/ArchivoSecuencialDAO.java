package colectivo.dao.secuencial;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Clase utilitaria para la carga de parámetros de configuración desde un archivo de propiedades secuencial.
 *
 * <p>Esta clase proporciona un método estático para leer el archivo de propiedades
 * {@code resources/secuencial.properties} desde el classpath y devolverlo como un objeto {@link Properties}.
 * Se utiliza para obtener rutas y nombres de archivos de datos necesarios por los DAOs secuenciales.</p>
 *
 * <p>Si el archivo no se encuentra o ocurre un error de lectura, se lanza una excepción
 * {@link IllegalStateException} con un mensaje descriptivo y la causa original.</p>
 * </p>
 */
class ArchivoSecuencialDAO{
    private static final String RUTA_SECUENCIAL = "resources/secuencial.properties";
    

    /**
     * Lee el archivo de propiedades secuencial desde el classpath y lo devuelve como un objeto {@link Properties}.
     * <p>El archivo debe estar ubicado en {@code resources/secuencial.properties} dentro del classpath.</p>
     *
     * @return objeto {@link Properties} con las claves y valores definidos en el archivo de configuración.
     * @throws IllegalStateException si el archivo no se encuentra o ocurre un error de lectura.
     */
    public static Properties leerArchivo() {
        Properties prop = new Properties();
		
        InputStream entrada = ArchivoSecuencialDAO.class.getClassLoader().getResourceAsStream(RUTA_SECUENCIAL);
        if (entrada == null) {
            //  TODO: LOGGER ERROR
            throw new IllegalStateException("No fue posible encontrar archivo secuencial.properties en el class path.");
        }
        try {
            prop.load(entrada);
        } catch (IOException e) {
            //  TODO: LOGGER ERROR
            throw new IllegalStateException("Error al cargar secuencial.properties", e);
        }

        return prop;
    }

}
