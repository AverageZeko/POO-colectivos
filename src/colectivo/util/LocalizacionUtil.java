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
 * Utilidad para descubrir localizaciones disponibles escaneando recursos.
 */
public class LocalizacionUtil {

    private static final Logger LOG = LoggerFactory.getLogger(LocalizacionUtil.class);
    private static final String RUTA_LOCALIZACION = "localizacion";
    private static final String PREFIJO_LABEL = "label_";
    private static final String SUFIJO_PROPERTIES = ".properties";

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
    
    private static List<LocaleInfo> scanDirectory(File directory) {
        List<LocaleInfo> localizaciones = new ArrayList<>();
        File[] files = directory.listFiles();
        if (files == null) return localizaciones;

        for (File file : files) {
            parseAndAddLocale(file.getName(), localizaciones);
        }
        return localizaciones;
    }

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

    private static void parseAndAddLocale(String fileName, List<LocaleInfo> list) {
        if (fileName.startsWith(PREFIJO_LABEL) && fileName.endsWith(SUFIJO_PROPERTIES)) {
            String localeCode = fileName.substring(PREFIJO_LABEL.length(), fileName.length() - SUFIJO_PROPERTIES.length());
            
            String[] parts = localeCode.split("_", 2);
            if (parts.length > 0) {
                String lang = parts[0];
                String country = (parts.length > 1) ? parts[1] : "";
                
                // Corrección para que coincida con el nombre de archivo de la bandera (que puede no ser un código ISO válido)
                // Usamos el código tal como viene del nombre de archivo para las banderas.
                String codigoCompletoParaBandera = localeCode;

               

                list.add(new LocaleInfo(codigoCompletoParaBandera, lang, country));
            }
        }
    }
}