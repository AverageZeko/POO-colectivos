package colectivo.datos;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.time.LocalTime;

import colectivo.modelo.Linea;
import colectivo.modelo.Parada;
import colectivo.modelo.Tramo;

/**
 * Clase para cargar datos del sistema de colectivos desde archivos de texto.
 * <p>
 * Esta clase proporciona métodos estáticos para leer archivos de configuración
 * que contienen información sobre paradas, tramos y líneas de colectivos.
 * Los archivos deben estar ubicados en la carpeta resources del classpath.
 * <p>
 * Formatos de archivo esperados:
 * <ul>
 * <li>Paradas: codigo;direccion;latitud;longitud</li>
 * <li>Tramos: paradaInicio;paradaFinal;tiempo;tipoRecorrido</li>
 * <li>Líneas: codigoLinea;nombreLinea;parada1;parada2;...</li>
 * <li>Frecuencias: codigoLinea;diaSemana;horaInicio</li>
 * </ul>
 * <p>
 * Los archivos pueden contener líneas de comentarios que inicien con '#' y
 * líneas vacías, las cuales serán ignoradas durante el procesamiento.
 */
public class CargarDatos {

	/**
	 * Carga las paradas de colectivos desde un archivo de texto.
	 * <p>
	 * El archivo debe contener líneas con el formato:
	 * codigo;direccion;latitud;longitud
	 * <p>
	 * Las líneas que inicien con '#' o estén vacías serán ignoradas.
	 * Si una línea está mal formateada o contiene datos inválidos,
	 * se mostrará un mensaje de error y se continuará con la siguiente línea.
	 * 
	 * @param nombreArchivo el nombre del archivo a cargar desde la carpeta resources
	 * @return un Map donde la clave es el código de la parada (Integer) y el valor
	 *         es el objeto Parada correspondiente
	 * @throws FileNotFoundException si ocurre un error al leer el archivo
	 * @throws IOException si ocurre un error de formateo en las divisiones de la linea
	 * @throws NumberFormatException si el codigo, latitud o longitud estan mal formateados
	 * 
	 * @see colectivo.modelo.Parada
	 */
	public static Map<Integer, Parada> cargarParadas(String nombreArchivo) throws IOException {
		Map<Integer, Parada> paradas = new HashMap<>();
		InputStream inputStream = CargarDatos.class.getClassLoader().getResourceAsStream("resources/" + nombreArchivo);
		if (inputStream == null) {
			throw new FileNotFoundException("No fue posible encontrar " + nombreArchivo + " en la carpeta resources del classpath.");
		}

		try (Scanner contenidoArchivo = new Scanner(inputStream)) {
            while (contenidoArchivo.hasNextLine()) {
                String lineaActual = contenidoArchivo.nextLine().trim();
                if (lineaActual.isEmpty() || lineaActual.startsWith("#")) {
                    continue;
                }

                String[] partesLinea = lineaActual.split(";");
                if (partesLinea.length < 4) {
					throw new IOException("Linea mal formateada:  " + lineaActual);
                }

                try {
                    int codigoParada = Integer.parseInt(partesLinea[0].trim());
                    String direccionParada = partesLinea[1].trim();
					double latitudParada = Double.parseDouble(partesLinea[2].trim());
					double longitudParada = Double.parseDouble(partesLinea[3].trim());

					Parada paradaActual = new Parada(codigoParada, direccionParada, latitudParada, longitudParada);
                    paradas.put(codigoParada, paradaActual);

                } catch (NumberFormatException e) {
                    System.err.println("Codigo, latitud o longitud invalidas en la linea: " + lineaActual);
                }
            }
        }

		return paradas;
	}

	/**
	 * Carga los tramos entre paradas desde un archivo de texto.
	 * <p>
	 * El archivo debe contener líneas con el formato:
	 * paradaInicio;paradaFinal;tiempo;tipoRecorrido
	 * <p>
	 * Las líneas que inicien con '#' o estén vacías serán ignoradas.
	 * Si se referencia una parada que no existe en el mapa de paradas,
	 * se mostrará un aviso pero se continuará el procesamiento.
	 * 
	 * @param nombreArchivo el nombre del archivo a cargar desde la carpeta resources
	 * @param paradas mapa de paradas previamente cargadas, usado para validar
	 *                las referencias de paradas en los tramos
	 * @return un Map donde la clave es una String con formato "codigoOrigen->codigoDestino"
	 *         y el valor es el objeto Tramo correspondiente
	 * @throws FileNotFoundException si ocurre un error al leer el archivo
	 * @throws IOException si ocurre un error de formateo en el archivo
	 * @throws NumberFormatException si el indice de alguna parada, el tiempo o tipo de recorrido fueron mal formateados
	 * 
	 * @see colectivo.modelo.Tramo
	 */
	public static Map<String, Tramo> cargarTramos(String nombreArchivo, Map<Integer, Parada> paradas)
			throws IOException {

		Map<String, Tramo> tramos = new HashMap<>();
		InputStream inputStream = CargarDatos.class.getClassLoader().getResourceAsStream("resources/" + nombreArchivo);
		if (inputStream == null) {
			throw new FileNotFoundException("No fue posible encontrar " + nombreArchivo + " en la carpeta resources del classpath.");
		}

		try (Scanner contenidoArchivo = new Scanner(inputStream)) {
			while (contenidoArchivo.hasNextLine()) {
				String lineaActual = contenidoArchivo.nextLine().trim();
				if (lineaActual.isEmpty() || lineaActual.startsWith("#")) {
					continue;
				}

				String[] partesLinea = lineaActual.split(";");
				if (partesLinea.length < 4) {
					throw new IOException("Linea mal formateada:  " + lineaActual);
				}

				try {
					int paradaInicio = Integer.parseInt(partesLinea[0].trim());
					int paradaFinal = Integer.parseInt(partesLinea[1].trim());

					if (!paradas.containsKey(paradaInicio)) {
						System.err.println("AVISO: Parada inicial no fue encontrada " + paradaInicio);
					}

					if (!paradas.containsKey(paradaFinal)) {
						System.err.println("AVISO: Parada final no fue encontrada " + paradaFinal);
					}


					Parada inicio = paradas.get(paradaInicio);
					Parada fin = paradas.get(paradaFinal);

					int tiempo = Integer.parseInt(partesLinea[2].trim());
					int tipoRecorrido = Integer.parseInt(partesLinea[3].trim());

					Tramo tramoActual = new Tramo(inicio, fin, tiempo, tipoRecorrido);

					String tramoKey = String.format("%d->%d", inicio.getCodigo(), fin.getCodigo());
					tramos.put(tramoKey, tramoActual);

				} catch (NumberFormatException e) {
					System.err.println("index de parada inicial/final o tiempo invalidos en la linea: " + lineaActual);
				}
			}
		}
		return tramos;
	}

	/**
	 * Carga las líneas de colectivos y sus frecuencias desde archivos de texto.
	 * <p>
	 * El archivo de líneas debe contener líneas con el formato:
	 * codigoLinea;nombreLinea;parada1;parada2;...;paradaN
	 * <p>
	 * El archivo de frecuencias debe contener líneas con el formato:
	 * codigoLinea;diaSemana;horaInicio
	 * <p>
	 * Se procesan primero las frecuencias y luego las líneas. Para cada línea
	 * se agregan las paradas que existan en el mapa de paradas y se asocian
	 * las frecuencias correspondientes. Las líneas que inicien con '#' o estén
	 * vacías serán ignoradas en ambos archivos.
	 * 
	 * @param nombreArchivo el nombre del archivo de líneas en la carpeta resources
	 * @param nombreArchivoFrecuencia el nombre del archivo de frecuencias en resources
	 * @param paradas mapa de paradas previamente cargadas, usado para validar
	 *                las referencias de paradas en las líneas
	 * @return un Map donde la clave es el código de la línea (String) y el valor
	 *         es el objeto Linea correspondiente con sus paradas y frecuencias
	 * @throws FileNotFoundException si ocurre un error al leer el archivo
	 * @throws IOException si ocurre un error de formateo en el archivo
	 * @throws NumberFormatException si el codigo de una parada esta mal formateado
	 * @throws IllegalArgumentException si el tiempo o tipo de recorrido estan mal formateados en la frecuencia
	 * 
	 * @see colectivo.modelo.Linea
	 * @see colectivo.modelo.Linea#agregarParada(Parada)
	 * @see colectivo.modelo.Linea#agregarFrecuencia(int, LocalTime)
	 */
	public static Map<String, Linea> cargarLineas(String nombreArchivo, String nombreArchivoFrecuencia,
			Map<Integer, Parada> paradas) throws IOException {

		Map<String, Linea> lineas = new HashMap<>();
		Map<String, List<String[]>> frecuencias = new HashMap<>();
		InputStream frecuenciaInputStream = CargarDatos.class.getClassLoader().getResourceAsStream("resources/" + nombreArchivoFrecuencia);
		if (frecuenciaInputStream == null) {
			throw new FileNotFoundException("No fue posible encontrar " + nombreArchivoFrecuencia + " en la carpeta resources del classpath.");
		}

		try (Scanner contenidoFrecuencias = new Scanner(frecuenciaInputStream)) {
			while (contenidoFrecuencias.hasNextLine()) {
				String lineaFrecuencia = contenidoFrecuencias.nextLine().trim();
				if (lineaFrecuencia.isEmpty() || lineaFrecuencia.startsWith("#")) {
					continue;
				}
				String[] partesLinea = lineaFrecuencia.split(";");
				if (partesLinea.length < 3) {
					throw new IOException("Linea mal formateada:  " + lineaFrecuencia);
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

		InputStream lineaInputStream = CargarDatos.class.getClassLoader().getResourceAsStream("resources/" + nombreArchivo);
		if (lineaInputStream == null) {
			throw new FileNotFoundException("No fue posible encontrar " + nombreArchivo + " en la carpeta resources del classpath.");
		}

        try (Scanner contenidoLineas = new Scanner(lineaInputStream)) {
            while (contenidoLineas.hasNextLine()) {
                String lineaArchivo = contenidoLineas.nextLine().trim();
                if (lineaArchivo.isEmpty() || lineaArchivo.startsWith("#")) {
                    continue;
                }

                String[] partesLinea = lineaArchivo.split(";");
                if (partesLinea.length < 3) {
                    throw new IOException("Linea mal formateada:  " + lineaArchivo);
                }

                String codigoLinea = partesLinea[0].trim();
				String nombreLinea = partesLinea[1].trim();

				Linea lineaActual = new Linea(codigoLinea, nombreLinea); 

				try {
					for (int i = 2; i < partesLinea.length; i++) {
						int codigoParada = Integer.parseInt(partesLinea[i].trim());
						if (paradas.containsKey(codigoParada)) {
							lineaActual.agregarParada(paradas.get(codigoParada));
						}
					}
				}	catch (NumberFormatException e) {
					System.err.println("Codigo de parada mal formateado: " + lineaArchivo);
				}

				if (frecuencias.containsKey(codigoLinea)) {
					for (String[] detallesFrecuencia : frecuencias.get(codigoLinea)) {
						try {
							int diaSemana = Integer.parseInt(detallesFrecuencia[0]);
							LocalTime inicioRecorrido = LocalTime.parse(detallesFrecuencia[1]);
							lineaActual.agregarFrecuencia(diaSemana, inicioRecorrido);
						}	catch (IllegalArgumentException e) {
							System.err.println("Frecuencia invalida para linea: " + frecuencias.get(codigoLinea));
						}
					}
				}	else {
					System.err.println("AVISO: No se encontro ninguna frecuencia para linea " + codigoLinea);
				}
				lineas.put(codigoLinea, lineaActual);
            }
        }

		return lineas;
	}

}