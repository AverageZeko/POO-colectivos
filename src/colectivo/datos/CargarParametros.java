package colectivo.datos;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class CargarParametros {

	private static String archivoLinea;
	private static String archivoParada;
	private static String archivoTramo;
	private static String archivoFrecuencia;
	

	/**
	 * Carga los parametros del archivo "config.properties"
	 * 
	 * @throws IOException
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
