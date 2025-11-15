package colectivo.configuracion;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import colectivo.util.LocaleInfo;
import colectivo.util.LocalizacionUtil;

public class ConfigGlobal {
    private static final String RUTA_CIUDADES = "resources/ciudades.properties";
    private static final Logger CONFIG_LOGGER = LoggerFactory.getLogger("Configuracion");
    private static ConfigGlobal configuracion;
    private LocaleInfo localeActual;
    private ResourceBundle bundle;
    private Map<String, String> nombresCiudades;
    private static String schemaActual;

    public static ConfigGlobal getConfiguracion() {
        if (configuracion == null) {
            configuracion = new ConfigGlobal();
        }   
        return configuracion;
    }

    private ConfigGlobal() {
		localeActual = new LocaleInfo("es_ARG", "es", "ARG");
        setLocalizacion(localeActual);
        nombresCiudades = new HashMap<>();
        buscarCiudades();
    }

    public List<String> getCiudades() {
        ArrayList<String> listaCiudades = new ArrayList<>();
        for (String ciudad : nombresCiudades.keySet()) {
            listaCiudades.add(ciudad);
        }
        return listaCiudades;
    }

    private void buscarCiudades() {
        Properties prop = new Properties();
		
        InputStream entrada = ConfigGlobal.class.getClassLoader().getResourceAsStream(RUTA_CIUDADES);
        if (entrada == null) {
            CONFIG_LOGGER.error("No fue posible encontrar archivo ciudades.properties en el class path");
            throw new IllegalStateException("No fue posible encontrar archivo ciudades.properties en el class path.");
        }
        try {
            prop.load(entrada);
            for (String clave : prop.stringPropertyNames()) {
                String valor = prop.getProperty(clave);
                if (valor != null) {
                    String claveNormalizada = clave.replace(';', ' ');
                    nombresCiudades.put(claveNormalizada, valor);
                }
            }
        } catch (IOException e) {
            CONFIG_LOGGER.error("Error al cargar ciudades.properties", e);
            throw new IllegalStateException("Error al cargar ciudades.properties", e);
        }
        
    }

    public void cambiarCiudad(String ciudad) {
    	schemaActual = nombresCiudades.get(ciudad);
    }

    public static String getSchema() {
        return schemaActual;
    }

    /**
     * Configura la localización (idioma y país) de la aplicación, cargando el
     * ResourceBundle correspondiente para la internacionalización.
     * @param localeInfo la información de localización a establecer.
     */
    public void setLocalizacion(LocaleInfo localeInfo) {
        if (localeInfo == null) {
            CONFIG_LOGGER.warn("setLocalizacion fue llamado con un valor nulo.");
            return;
        }
        
        this.localeActual = localeInfo; // Guardar la referencia
        try {
            String nombreBase = localeInfo.getNombreBaseBundle();
            this.bundle = ResourceBundle.getBundle(nombreBase);
            CONFIG_LOGGER.info("Localización seleccionada: {}. ResourceBundle '{}' cargado con éxito.", localeInfo.codigoCompleto(), nombreBase);
        } catch (MissingResourceException e) {
            CONFIG_LOGGER.error("No se pudo cargar el ResourceBundle para '{}'. Verifica que el archivo .properties exista.", localeInfo.getNombreBaseBundle(), e);
            this.bundle = null;
        }
    }

    /**
     * Descubre las localizaciones disponibles en la aplicación.
     * @return una lista de objetos LocaleInfo que representan las localizaciones encontradas.
     */
    public List<LocaleInfo> descubrirLocalizaciones() {
        return LocalizacionUtil.descubrirLocalizaciones();
    }

    /**
     * Devuelve el LocaleInfo actualmente configurado.
     * @return El objeto LocaleInfo actual.
     */
    public LocaleInfo getLocale() {
        return localeActual;
    }

    /**
     * Devuelve el ResourceBundle cargado para la localización actual, que contiene
     * las cadenas de texto internacionalizadas.
     * @return el ResourceBundle actual.
     */
    public ResourceBundle getBundle() {
        return bundle;
    }

}
