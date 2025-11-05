package colectivo.util;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Clase de utilidad para descubrir dinámicamente las localizaciones disponibles
 * en la aplicación. Escanea el classpath en busca de archivos de propiedades de
 * localización y crea objetos {@link LocaleInfo} para cada uno.
 */
public class LocalizacionUtil {

    private static final Logger LOG = LoggerFactory.getLogger(LocalizacionUtil.class);
    private static final String RUTA_LOCALIZACION = "localizacion";
    private static final String PREFIJO_LABEL = "label_";
    private static final String SUFIJO_PROPERTIES = ".properties";

    /**
     * Escanea el classpath para encontrar todos los archivos de propiedades de localización.
     * Funciona tanto si la aplicación se ejecuta desde un sistema de archivos como desde un JAR.
     *
     * @return una lista de objetos {@link LocaleInfo} representando cada localización encontrada.
     */
    public static List<LocaleInfo> descubrirLocalizaciones() {
        List<LocaleInfo> localizaciones = new ArrayList<>();
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            URL resourceUrl = classLoader.getResource(RUTA_LOCALIZACION);

            if (resourceUrl == null) {
                LOG.error("El directorio de recursos '{}' no fue encontrado en el classpath.", RUTA_LOCALIZACION);
                return localizaciones;
            }

            if ("jar".equals(resourceUrl.getProtocol())) {
                localizaciones.addAll(scanJar(resourceUrl));
            } else {
                localizaciones.addAll(scanDirectory(new File(resourceUrl.toURI())));
            }

        } catch (URISyntaxException | IOException e) {
            LOG.error("Error al escanear las localizaciones disponibles.", e);
        }

        LOG.info("Descubiertas {} localizaciones: {}", localizaciones.size(), localizaciones);
        return localizaciones;
    }
    
    /**
     * Escanea un directorio del sistema de archivos en busca de archivos de localización.
     * @param directory El directorio a escanear.
     * @return La lista de localizaciones encontradas.
     */
    private static List<LocaleInfo> scanDirectory(File directory) {
        List<LocaleInfo> localizaciones = new ArrayList<>();
        File[] files = directory.listFiles();
        if (files == null) return localizaciones;

        for (File file : files) {
            parseAndAddLocale(file.getName(), localizaciones);
        }
        return localizaciones;
    }

    /**
     * Escanea un archivo JAR en busca de entradas que correspondan a archivos de localización.
     * @param jarUrl La URL del recurso dentro del JAR.
     * @return La lista de localizaciones encontradas.
     * @throws IOException Si ocurre un error al leer el archivo JAR.
     */
    private static List<LocaleInfo> scanJar(URL jarUrl) throws IOException {
        List<LocaleInfo> localizaciones = new ArrayList<>();
        String jarPath = jarUrl.getPath().substring(5, jarUrl.getPath().indexOf("!"));
        try (JarFile jar = new JarFile(jarPath)) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                if (name.startsWith(RUTA_LOCALIZACION + "/") && !entry.isDirectory()) {
                    String fileName = name.substring(RUTA_LOCALIZACION.length() + 1);
                    parseAndAddLocale(fileName, localizaciones);
                }
            }
        }
        return localizaciones;
    }

    /**
     * Parsea un nombre de archivo para extraer la información de localización
     * y, si es válido, la añade a la lista.
     * @param fileName El nombre del archivo a parsear (ej. "label_es_ARG.properties").
     * @param list La lista donde se añadirá el nuevo {@link LocaleInfo}.
     */
    private static void parseAndAddLocale(String fileName, List<LocaleInfo> list) {
        if (fileName.startsWith(PREFIJO_LABEL) && fileName.endsWith(SUFIJO_PROPERTIES)) {
            String localeCode = fileName.substring(PREFIJO_LABEL.length(), fileName.length() - SUFIJO_PROPERTIES.length());
            
            String[] parts = localeCode.split("_", 2);
            if (parts.length > 0) {
                String lang = parts[0];
                String country = (parts.length > 1) ? parts[1] : "";
                
                String codigoCompletoParaBandera = localeCode;

                list.add(new LocaleInfo(codigoCompletoParaBandera, lang, country));
            }
        }
    }
}