package colectivo.dao.secuencial;

import java.io.InputStream;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import colectivo.conexion.Factory;
import colectivo.controlador.Constantes;
import colectivo.dao.LineaDAO;
import colectivo.dao.ParadaDAO;
import colectivo.modelo.Linea;
import colectivo.modelo.Parada;

/**
 * DAO secuencial para la gestión y carga de líneas de colectivos desde archivos planos.
 *
 * <p>Esta clase se encarga de leer los datos de líneas y sus frecuencias desde archivos
 * configurados en el archivo de propiedades {@code secuencial.properties}, ubicados en el classpath.
 * Utiliza la información de paradas obtenida a través de {@link ParadaDAO} para construir
 * las relaciones entre líneas y paradas.</p>
 *
 * <p>Las líneas y frecuencias se cargan una sola vez y se almacenan en memoria para
 * optimizar el acceso. Si los archivos no se encuentran o contienen errores de formato,
 * se lanzan excepciones {@link IllegalStateException} descriptivas.</p>
 */
public class LineaSecuencialDAO implements LineaDAO {
    private static final Logger LINEA_DAO_LOG = LoggerFactory.getLogger("LineaDAO");
    private String archivoLinea;
    private String archivoFrecuencia;
    private Map<String, Linea> lineas;

    /**
     * Constructor que inicializa las rutas de los archivos de líneas y frecuencias
     * a partir del archivo de propiedades secuencial.
     *
     * @throws IllegalStateException si no se encuentran las rutas de los archivos requeridos.
     */
    public LineaSecuencialDAO() {
        Properties prop = ArchivoSecuencialDAO.leerArchivo();
        archivoLinea = prop.getProperty(Constantes.LINEA);
        archivoFrecuencia = prop.getProperty(Constantes.FRECUENCIA);
		if (archivoLinea == null) {
            LINEA_DAO_LOG.error("Error al cargar archivo de lineas en src/resource");
			throw new IllegalStateException("Error al cargar archivo de lineas en src/resource");
		}
        if (archivoFrecuencia == null) {
			LINEA_DAO_LOG.error("Error al cargar archivo de frecuencias en src/resource");
            throw new IllegalStateException("Error al cargar archivo de frecuencias en src/resource");
		}
    }

    /**
     * Carga y devuelve todas las líneas de colectivos junto con sus paradas y frecuencias.
     *
     * <p>Lee los datos desde los archivos configurados, construye los objetos {@link Linea}
     * y los relaciona con las paradas y frecuencias correspondientes.</p>
     *
     * @return un {@link Map} con las líneas, indexadas por su código.
     * @throws IllegalStateException si ocurre algún error de lectura o formato en los archivos, 
     *          junto con la traza original.
     */
    public Map<String, Linea> buscarTodos() {
        if (lineas == null) {
            ParadaDAO paradaDAO = (ParadaDAO) Factory.getInstancia(Constantes.PARADA);
            Map<Integer, Parada> paradas = paradaDAO.buscarTodos();
            lineas = new HashMap<>();
            Map<String, List<String[]>> frecuencias = buscarFrecuencias();
        
            InputStream lineaInputStream = LineaSecuencialDAO.class.getClassLoader().getResourceAsStream("resources/" + archivoLinea);
            if (lineaInputStream == null) {
                LINEA_DAO_LOG.error("No fue posible encontrar {} en la carpeta resources del classpath", archivoLinea);
                throw new IllegalStateException("No fue posible encontrar " + archivoLinea + " en la carpeta resources del classpath");
            }
            LINEA_DAO_LOG.debug("Archivo de lineas cargado");

            try (Scanner contenidoLineas = new Scanner(lineaInputStream)) {
                while (contenidoLineas.hasNextLine()) {
                    String lineaArchivo = contenidoLineas.nextLine().trim();
                    if (lineaArchivo.isEmpty() || lineaArchivo.startsWith("#")) {
                        continue;
                    }

                    String[] partesLinea = lineaArchivo.split(";");
                    if (partesLinea.length < 3) {
                        LINEA_DAO_LOG.error("Linea mal formateada: {}", lineaArchivo);
                        throw new IllegalStateException("Linea mal formateada:  " + lineaArchivo);
                    }

                    String codigoLinea = partesLinea[0].trim();
                    String nombreLinea = partesLinea[1].trim();

                    Linea lineaActual = new Linea(codigoLinea, nombreLinea); 

                    try {
                        for (int i = 2; i < partesLinea.length; i++) {
                            int codigoParada = Integer.parseInt(partesLinea[i].trim());
                            if (paradas.containsKey(codigoParada)) {
                                lineaActual.agregarParada(paradas.get(codigoParada));
                            }  else {
                                LINEA_DAO_LOG.error("Parada {} no encontrada en el conjunto de paradas", codigoParada);
                            }
                        }
                    }	catch (NumberFormatException e) {
                        LINEA_DAO_LOG.error("Codigo de parada mal formateado: {}", lineaArchivo, e);
                        throw new IllegalStateException("Codigo de parada mal formateado: " + lineaArchivo, e);
                    }

                    if (frecuencias.containsKey(codigoLinea)) {
                        for (String[] detallesFrecuencia : frecuencias.get(codigoLinea)) {
                            try {
                                int diaSemana = Integer.parseInt(detallesFrecuencia[0]);
                                LocalTime inicioRecorrido = LocalTime.parse(detallesFrecuencia[1]);
                                lineaActual.agregarFrecuencia(diaSemana, inicioRecorrido);
                            }	catch (IllegalArgumentException e) {
                                LINEA_DAO_LOG.error("Frecuencia invalida para linea: {}", frecuencias.get(codigoLinea), e);
                                throw new IllegalStateException("Frecuencia invalida para linea: " + frecuencias.get(codigoLinea), e);
                            }
                        }
                    }	else {
                        LINEA_DAO_LOG.error("No se encontro ninguna frecuencia para linea {}", codigoLinea);
                        throw new IllegalStateException("No se encontro ninguna frecuencia para linea " + codigoLinea);
                    }
                    lineas.put(codigoLinea, lineaActual);
                }
            }
        }
        LINEA_DAO_LOG.info("Lineas cargadas");
        return lineas;
    }


    /**
     * Carga las frecuencias de las líneas desde el archivo configurado.
     *
     * <p>Devuelve un mapa donde la clave es el código de la línea y el valor es una lista
     * de arreglos de cadenas con los detalles de frecuencia.</p>
     *
     * @return un {@link Map} con las frecuencias por línea.
     * @throws IllegalStateException si el archivo no se encuentra o contiene errores de formato.
     */
    private Map<String, List<String[]>> buscarFrecuencias() {
        Map<String, List<String[]>> frecuencias = new HashMap<>();
		InputStream frecuenciaInputStream = LineaSecuencialDAO.class.getClassLoader().getResourceAsStream("resources/" + archivoFrecuencia);
		if (frecuenciaInputStream == null) {
            LINEA_DAO_LOG.error("No fue posible encontrar {} en la carpeta resources del classpath", archivoFrecuencia);
			throw new IllegalStateException("No fue posible encontrar " + archivoFrecuencia + " en la carpeta resources del classpath.");
		}

		try (Scanner contenidoFrecuencias = new Scanner(frecuenciaInputStream)) {
			while (contenidoFrecuencias.hasNextLine()) {
				String lineaFrecuencia = contenidoFrecuencias.nextLine().trim();
				if (lineaFrecuencia.isEmpty() || lineaFrecuencia.startsWith("#")) {
					continue;
				}
				String[] partesLinea = lineaFrecuencia.split(";");
				if (partesLinea.length < 3) {
                    LINEA_DAO_LOG.error("Linea mal formateada: {}", lineaFrecuencia);
					throw new IllegalStateException("Linea mal formateada:  " + lineaFrecuencia);
				}

				String codigoLinea = partesLinea[0].trim();
				String detallesFrecuencia[] = {partesLinea[1].trim(), partesLinea[2].trim()};

				if (!frecuencias.containsKey(codigoLinea)) {
					ArrayList<String[]> lista = new ArrayList<>();
					lista.add(detallesFrecuencia);
					frecuencias.put(codigoLinea, lista);
				}	else {
					List<String[]> lista = frecuencias.get(codigoLinea);
					lista.add(detallesFrecuencia);
				}
			}
		}
        LINEA_DAO_LOG.info("Frecuencias cargadas");
        return frecuencias;
    }


    public String getArchivoLinea() {
        return archivoLinea;
    }

    public String getArchivoFrecuencia() {
        return archivoFrecuencia;
    }

}


