package colectivo.dao.secuencial;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;

import colectivo.conexion.Factory;
import colectivo.controlador.Constantes;
import colectivo.dao.ParadaDAO;
import colectivo.dao.TramoDAO;
import colectivo.modelo.Parada;
import colectivo.modelo.Tramo;

/**
 * DAO secuencial para la gestión y carga de tramos de colectivos desde archivos planos.
 *
 * <p>Esta clase se encarga de leer los datos de tramos desde el archivo configurado
 * en el archivo de propiedades {@code secuencial.properties}, ubicado en el classpath.
 * Cada tramo se representa como un objeto {@link Tramo} y se almacena en un mapa indexado por la clave formada por las paradas de inicio y fin.</p>
 *
 * <p>Los tramos se cargan una sola vez y se almacenan en memoria para optimizar el acceso.
 * Si el archivo no se encuentra o contiene errores de formato, se lanza una excepción
 * {@link IllegalStateException} descriptiva.</p>
 * 
 */
public class TramoSecuencialDAO implements TramoDAO {
    private String archivo;
    private Map<String, Tramo> tramos;

    /**
     * Constructor que inicializa la ruta del archivo de tramos
     * a partir del archivo de propiedades secuencial.
     *
     * @throws IllegalStateException si no se encuentra la ruta del archivo requerido,
     *          junto con la traza original.
     */
    public TramoSecuencialDAO() {
        Properties prop = ArchivoSecuencialDAO.leerArchivo();
        archivo = prop.getProperty(Constantes.TRAMO);
		if (archivo == null) {
			throw new IllegalStateException("Error al cargar archivo de tramos en src/resource.");
		}
    }


    /**
     * Carga y devuelve todos los tramos de colectivos desde el archivo configurado.
     *
     * <p>Lee los datos desde el archivo, construye los objetos {@link Tramo}
     * y los indexa por la clave formada por las paradas de inicio y fin en un mapa.</p>
     *
     * @return un {@link Map} con los tramos, indexados por la clave "inicio->fin".
     * @throws IllegalStateException si ocurre algún error de lectura o formato en el archivo.
     */
    @Override
    public Map<String, Tramo> buscarTodos() {
        if (tramos == null) {
            tramos = new HashMap<>();
            ParadaDAO paradaDAO = (ParadaDAO) Factory.getInstancia(Constantes.PARADA);
            Map<Integer, Parada> paradas = paradaDAO.buscarTodos();
            InputStream inputStream = TramoSecuencialDAO.class.getClassLoader().getResourceAsStream("resources/" + archivo);
            if (inputStream == null) {
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
                        throw new IllegalStateException("Linea mal formateada:  " + lineaActual);
                    }

                    try {
                        int paradaInicio = Integer.parseInt(partesLinea[0].trim());
                        int paradaFinal = Integer.parseInt(partesLinea[1].trim());

                        if (!paradas.containsKey(paradaInicio)) {
                            throw new IllegalStateException("AVISO: Parada inicial no fue encontrada " + paradaInicio);
                        }

                        if (!paradas.containsKey(paradaFinal)) {
                            throw new IllegalStateException("AVISO: Parada final no fue encontrada " + paradaFinal);
                        }


                        Parada inicio = paradas.get(paradaInicio);
                        Parada fin = paradas.get(paradaFinal);

                        int tiempo = Integer.parseInt(partesLinea[2].trim());
                        int tipoRecorrido = Integer.parseInt(partesLinea[3].trim());

                        Tramo tramoActual = new Tramo(inicio, fin, tiempo, tipoRecorrido);

                        String tramoKey = String.format("%d->%d", inicio.getCodigo(), fin.getCodigo());
                        tramos.put(tramoKey, tramoActual);

                    } catch (NumberFormatException e) {
                        throw new IllegalStateException("index de parada inicial/final o tiempo invalidos en la linea: " + lineaActual, e);
                    }
                }
            }
        }
        return tramos;
    }

    public String getArchivo() {
        return archivo;
    }

}
