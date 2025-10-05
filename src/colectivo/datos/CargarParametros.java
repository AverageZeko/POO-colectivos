package colectivo.datos;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


/**
 * Clase para leer y cargar parámetros de configuración para la simulación del sistema de colectivos.
 * <p>
 * Esta clase lee los nombres de archivos de configuración desde un archivo de propiedades
 * (config.properties) y proporciona acceso a estos parámetros a través de métodos getter.
 * config.properties debe estar ubicado en la carpeta resources del classpath
 * Los parámetros incluyen los nombres de archivos para líneas, paradas, tramos y frecuencias.
 * Es obligatorio utilizar el metodo {@link #parametros()} para poder acceder a los archivos de texto
 * <p>
 */
public class CargarParametros {

	/** Nombre del archivo de líneas cargado desde config.properties */
	private static String archivoLinea;

	/** Nombre del archivo de paradas cargado desde config.properties */
	private static String archivoParada;

	/** Nombre del archivo de tramos cargado desde config.properties */
	private static String archivoTramo;

	/** Nombre del archivo de frecuencias cargado desde config.properties */
	private static String archivoFrecuencia;
	

	/**
	 * Carga los parámetros de configuración desde el archivo "config.properties".
	 * <p>
	 * Este método debe ser llamado antes de usar cualquiera de los métodos getter.
	 * Lee el archivo config.properties desde la carpeta resources del classpath
	 * e inicializa las variables estáticas con los nombres de archivo correspondientes.
	 * <p>
	 * 
	 * @throws IOException si el archivo config.properties no se encuentra en el classpath
	 *                     o si no se pueden leer todas las propiedades requeridas
	 */
    public static void parametros() throws IOException {
        Properties prop = new Properties();
		
		InputStream entrada = CargarParametros.class.getClassLoader().getResourceAsStream("resources/config.properties");
		if (entrada == null) {
			throw new IOException("No fue posible encontrar archivo config.properties en el class path.");
		}
		prop.load(entrada);

		archivoLinea = prop.getProperty("linea");
		archivoParada = prop.getProperty("parada");
		archivoFrecuencia = prop.getProperty("frecuencia");
		archivoTramo = prop.getProperty("tramo");

		if (archivoLinea == null || archivoParada == null || archivoFrecuencia == null|| archivoTramo == null) {
			throw new IOException("No todos los parametros fueron encontrados en config.properties.");
		}
    }

    public static String getArchivoLinea() {
		if (archivoLinea == null) {
			throw new IllegalStateException("Los archivos deben ser inicializados primero.");
		}
		return archivoLinea;
	}

    public static String getArchivoParada() {
		if (archivoParada == null) {
			throw new IllegalStateException("Los archivos deben ser inicializados primero.");
		}
		return archivoParada;
	}

    public static String getArchivoFrecuencia() {
		if (archivoFrecuencia == null) {
			throw new IllegalStateException("Los archivos deben ser inicializados primero.");
		}
		return archivoFrecuencia;
	}

    public static String getArchivoTramo() {
		if (archivoTramo == null) {
			throw new IllegalStateException("Los archivos deben ser inicializados primero.");
		}
		return archivoTramo;
	}
	
}
