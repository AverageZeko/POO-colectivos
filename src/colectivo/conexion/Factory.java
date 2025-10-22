package colectivo.conexion;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>Esta clase decide la implementacion concreta de DAO a utilizar en base al archivo modificable
 * {@code resources/factory.properties} utilizando el patron de diseño Factory </p>
 *
 * <p>
 * Las instancias se almacenan en un {@link java.util.concurrent.ConcurrentHashMap} y se crean
 * mediante el metodo {@code computeIfAbsent}, el cual se asegura que
 * solo exista una instancia del objeto en el mapa
 * </p>
 *
 */
public class Factory {
    private static ConcurrentHashMap <String, Object> instancias = new ConcurrentHashMap<>();
    private static final String RUTA_FACTORY = "resources/factory.properties";


    /**
     * Devuelve (y si es necesario crea) la instancia asociada a la clave dada. Mediante el uso de {@code computeIfAbsent} y una expresion Lambda
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
     *                  Debe coincidir exactamente con la clave definida.
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
                    throw new IllegalStateException("No fue posible encontrar el archivo de propiedades");
                }
                    prop.load(entrada);
                    String direccionClase = prop.getProperty(nombreObj);
                    if (direccionClase == null) {
                        throw new IllegalStateException("No fue posible encontrar la propiedad para " + nombreObj);
                    }
                    Class<?> cls = Class.forName(direccionClase);
                    return cls.getDeclaredConstructor().newInstance();
            } catch (IOException e) {
                    e.printStackTrace();
                    throw new IllegalStateException("No fue posible leer " + RUTA_FACTORY, e);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("No fue posible instanciar la clase para clave " + nombreObj, e);
            }
        });
        return obj;
    }
}
