package colectivo.dao.secuencial;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;

import colectivo.controlador.Constantes;
import colectivo.dao.ParadaDAO;
import colectivo.modelo.Parada;

/**
 * DAO secuencial para la gestión y carga de paradas de colectivos desde archivos planos.
 *
 * <p>Esta clase se encarga de leer los datos de paradas desde el archivo configurado
 * en el archivo de propiedades {@code secuencial.properties}, ubicado en el classpath.
 * Cada parada se representa como un objeto {@link Parada} y se almacena en un mapa indexado por su código.</p>
 *
 * <p>Las paradas se cargan una sola vez y se almacenan en memoria para optimizar el acceso.
 * Si el archivo no se encuentra o contiene errores de formato, se lanza una excepción
 * {@link IllegalStateException} descriptiva.</p>
 *
 */
public class ParadaSecuencialDAO implements ParadaDAO {
    private String archivo;
    private Map<Integer, Parada> paradas;

    /**
     * Constructor que inicializa la ruta del archivo de paradas
     * a partir del archivo de propiedades secuencial.
     *
     * @throws IllegalStateException si no se encuentra la ruta del archivo requerido.
     */
    public ParadaSecuencialDAO() {
        Properties prop = ArchivoSecuencialDAO.leerArchivo();
        archivo = prop.getProperty(Constantes.PARADA);
		if (archivo == null) {
            //  TODO: LOGGER
			throw new IllegalStateException("Error al cargar archivo de paradas en src/resource.");
		}
    }


    /**
     * Carga y devuelve todas las paradas de colectivos desde el archivo configurado.
     *
     * <p>Lee los datos desde el archivo, construye los objetos {@link Parada}
     * y los indexa por su código en un mapa.</p>
     *
     * @return un {@link Map} con las paradas, indexadas por su código.
     * @throws IllegalStateException si ocurre algún error de lectura o formato en el archivo,
     *          junto con su traza original.
     */
    @Override
    public Map<Integer, Parada> buscarTodos() {
        if (paradas == null) {
            paradas = new HashMap<>();
            InputStream inputStream = ParadaSecuencialDAO.class.getClassLoader().getResourceAsStream("resources/" + archivo);
            if (inputStream == null) {
                //  TODO: LOGGER
                throw new IllegalStateException("No fue posible encontrar " + archivo + " en la carpeta resources del classpath.");
            }
            
            try (Scanner contenidoArchivo = new Scanner(inputStream)) {
                while (contenidoArchivo.hasNextLine()) {
                    String lineaActual = contenidoArchivo.nextLine().trim();
                    if (lineaActual.isEmpty() || lineaActual.startsWith("#")) {
                        continue;
                    }

                    String[] partesLinea = lineaActual.split(";");
                    if (partesLinea.length < 4) {
                        //  TODO: LOGGER
                        throw new IllegalStateException("Linea mal formateada:  " + lineaActual);
                    }

                    try {
                        int codigoParada = Integer.parseInt(partesLinea[0].trim());
                        String direccionParada = partesLinea[1].trim();
                        double latitudParada = Double.parseDouble(partesLinea[2].trim());
                        double longitudParada = Double.parseDouble(partesLinea[3].trim());

                        Parada paradaActual = new Parada(codigoParada, direccionParada, latitudParada, longitudParada);
                        paradas.put(codigoParada, paradaActual);

                    } catch (NumberFormatException e) {
                        //  TODO: LOGGER
                        throw new IllegalStateException("Codigo, latitud o longitud invalidas en la linea: " + lineaActual, e);
                    }
                }
            }
        }
    
		return paradas;
    }

    public String getArchivo() {
        return archivo;
    }

}
