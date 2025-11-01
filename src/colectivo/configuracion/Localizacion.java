package colectivo.configuracion;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class Localizacion {
    private static final String RUTA_LOCALIZACION = "resources/localizacion/";
    private String foto;
    private Properties prop;

    public Localizacion() {
        prop = new Properties();
        cargarArchivo("label_es_ARG.properties");
        setRutaFoto("es_ARG");
    }

    private void cargarArchivo(String nombreArchivo) {
        String ruta = RUTA_LOCALIZACION + nombreArchivo;
        try (InputStream entrada = Localizacion.class.getClassLoader().getResourceAsStream(ruta)) {
            if (entrada == null) {
                // TODO: LOGGER
                throw new IllegalStateException("No fue posible encontrar archivo label_es_ARG en el class path.");
            }
            prop.clear();
            prop.load(new InputStreamReader(entrada, StandardCharsets.UTF_8));

        }   catch (IOException e) {
            // TODO: LOGGER
            throw new IllegalStateException("Error al cargar label_es_ARG", e);
        }
    }

    //Variable idioma tiene un formato como "en_US"
    public void setIdioma(String idioma) {
        String nuevoIdioma = String.format("label_%s.properties",idioma);
        cargarArchivo(nuevoIdioma);
        setRutaFoto(idioma);
    }

    private void setRutaFoto(String idioma) {
        String archivo = String.format("%s.png",idioma);
        foto = RUTA_LOCALIZACION + archivo;
    }

    public String getPalabra(String llave) {
        return prop.getProperty(llave);
    }

    public String getRutaFoto() {
        return foto;
    }

}
