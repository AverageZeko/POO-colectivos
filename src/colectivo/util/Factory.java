package colectivo.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementación del patrón de diseño Factory para la creación de instancias de DAO.
 * <p>Esta clase decide la implementación concreta de DAO a utilizar en base al archivo de configuración
 * {@code resources/factory.properties}.</p>
 * <p>
 * Las instancias se almacenan en caché en un {@link java.util.concurrent.ConcurrentHashMap} para asegurar
 * que solo exista una instancia de cada DAO (patrón Singleton gestionado por la Factory).
 * La creación es segura en entornos concurrentes gracias a {@code computeIfAbsent}.
 * </p>
 */
public class Factory {
    private static final Logger FACTORY_LOG = LoggerFactory.getLogger(Factory.class);
    private static ConcurrentHashMap <String, Object> instancias = new ConcurrentHashMap<>();
    private static final String RUTA_FACTORY = "resources/factory.properties";

    /**
     * Devuelve una instancia única del objeto DAO solicitado, creándola si no existe.
     * <p>Notas de implementación:
     *  Este método encapsula las excepciones originales utilizando {@link IllegalStateException} y devuelve un mensaje detallando el error junto con la traza de la excepcion real.
     * </p>
     * <p>Flujo del metodo:
     * <ol>
     *   <li>Se lee el fichero de propiedades definido por {@link #RUTA_FACTORY} desde el classpath.</li>
     *   <li>Se busca la propiedad cuyo nombre es exactamente {@code nombreObj} y se obtiene
     *       el nombre completo de la clase a instanciar.</li>
     *   <li>Se carga la clase con {@link Class#forName(String)} y se crea una instancia
     *       invocando su constructor sin parametros.</li>
     *   <li>La instancia se almacena en caché en memoria y se devuelve.</li>
     * </ol>
     * </p>
     *
     * @param nombreObj clave usada para buscar la clase en {@code resources/factory.properties}.
     *                  Debe coincidir exactamente con la clave definida (ej. "TRAMO", "LINEA").
     * @return la instancia asociada a la clave; nunca devuelve {@code null} si la clave está bien
     *         configurada y la clase tiene un constructor sin argumentos accesible.
     * @throws IllegalStateException si:
     *         <ul>
     *           <li>no se encuentra el archivo de propiedades en el classpath;</li>
     *           <li>no existe una entrada para {@code nombreObj} en el fichero de propiedades;</li>
     *           <li>la carga de la clase o la invocación del constructor falla.</li>
     *         </ul>
     */
    public static Object getInstancia(String nombreObj) {
        Object obj = instancias.computeIfAbsent(nombreObj, funcion -> {
            try (InputStream entrada = Factory.class.getClassLoader().getResourceAsStream(RUTA_FACTORY)) {
                Properties prop = new Properties();
                if (entrada == null) {
                    FACTORY_LOG.error("No fue posible encontrar el archivo de propiedades para Factory");
                    throw new IllegalStateException("No fue posible encontrar el archivo de propiedades para Factory");
                }
                    prop.load(entrada);
                    String direccionClase = prop.getProperty(nombreObj);
                    if (direccionClase == null) {
                        FACTORY_LOG.error("No fue posible encontrar la propiedad para {}", nombreObj);
                        throw new IllegalStateException("No fue posible encontrar la propiedad para " + nombreObj);
                    }
                    Class<?> cls = Class.forName(direccionClase);
                    Object objeto = cls.getDeclaredConstructor().newInstance();
                    FACTORY_LOG.info("Creada instancia de {}", nombreObj);
                    return objeto;
            } catch (IOException e) {
                FACTORY_LOG.error("No fue posible leer {}", RUTA_FACTORY, e);
                throw new IllegalStateException("No fue posible leer" + RUTA_FACTORY, e);
            } catch (ReflectiveOperationException e) {
                FACTORY_LOG.error("No fue posible instanciar la clase para la clave {}", nombreObj, e);
                throw new IllegalStateException("No fue posible instanciar la clase para clave " + nombreObj, e);
            }
        });
        return obj;
    }

    /**
     * Elimina una instancia de DAO de la caché.
     * La próxima vez que se solicite con {@code getInstancia}, se creará de nuevo.
     * Útil para cambiar de ciudad y forzar la recarga de datos.
     *
     * @param nombreObj La clave de la instancia a eliminar.
     */
    public static void clearInstancia(String nombreObj) {
        instancias.remove(nombreObj);
    }

}