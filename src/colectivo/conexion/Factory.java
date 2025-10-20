package colectivo.conexion;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class Factory {
    private static ConcurrentHashMap <String, Object> instancias = new ConcurrentHashMap<>();
    private static final String RUTA_FACTORY = "resources/factory.properties";

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
