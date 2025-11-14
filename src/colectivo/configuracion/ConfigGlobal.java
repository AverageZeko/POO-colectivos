package colectivo.configuracion;

import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import colectivo.servicio.SchemaServicio;
import colectivo.servicio.SchemaServicioImplementacion;
import colectivo.util.LocaleInfo;
import colectivo.util.LocalizacionUtil;

public class ConfigGlobal {
    private static final Logger QUERY_LOG = LoggerFactory.getLogger("Consulta");
    private static ConfigGlobal configuracion;
    private SchemaServicio schemaServicio;
    private LocaleInfo localeActual; // Guardar el LocaleInfo actual
    private ResourceBundle bundle;

    public static ConfigGlobal getConfiguracion() {
        if (configuracion == null) {
            configuracion = new ConfigGlobal();
        }   
        return configuracion;
    }

    private ConfigGlobal() {
        schemaServicio = new SchemaServicioImplementacion();
		localeActual = new LocaleInfo("es_ARG", "es", "ARG");
        setLocalizacion(localeActual);
    }

    /**
     * Establece el servicio de esquema a utilizar para cambiar entre ciudades.
     * @param schema el servicio de esquema.
     */
    public void setSchemaServicio(SchemaServicio schema) {
        this.schemaServicio = schema;
    }

    /**
     * Configura la localización (idioma y país) de la aplicación, cargando el
     * ResourceBundle correspondiente para la internacionalización.
     * @param localeInfo la información de localización a establecer.
     */
    public void setLocalizacion(LocaleInfo localeInfo) {
        if (localeInfo == null) {
            QUERY_LOG.warn("setLocalizacion fue llamado con un valor nulo.");
            return;
        }
        
        this.localeActual = localeInfo; // Guardar la referencia
        try {
            String nombreBase = localeInfo.getNombreBaseBundle();
            this.bundle = ResourceBundle.getBundle(nombreBase);
            QUERY_LOG.info("Localización seleccionada: {}. ResourceBundle '{}' cargado con éxito.", localeInfo.codigoCompleto(), nombreBase);
        } catch (MissingResourceException e) {
            QUERY_LOG.error("No se pudo cargar el ResourceBundle para '{}'. Verifica que el archivo .properties exista.", localeInfo.getNombreBaseBundle(), e);
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

    /**
     * Cambia el esquema de la base de datos activo.
     * @param nuevoSchema el nombre del nuevo esquema a utilizar.
     */
    public void cambiarSchema(String nuevoSchema) {
        schemaServicio.cambiarSchema(nuevoSchema);
    }

}
