package colectivo.dao.secuencial;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ArchivoSecuencialDAO{
    private static final String RUTA_SECUENCIAL = "resources/secuencial.properties";
    
    public static Properties leerArchivo() {
        Properties prop = new Properties();
		
        InputStream entrada = ArchivoSecuencialDAO.class.getClassLoader().getResourceAsStream(RUTA_SECUENCIAL);
        if (entrada == null) {
            throw new IllegalStateException("No fue posible encontrar archivo secuencial.properties en el class path.");
        }
        try {
            prop.load(entrada);
        } catch (IOException e) {
            throw new IllegalStateException("Error al cargar secuencial.properties", e);
        }

        return prop;
    }

}
