package colectivo.datos;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import java.time.LocalTime;

import colectivo.modelo.Linea;
import colectivo.modelo.Parada;
import colectivo.modelo.Tramo;

public class CargarDatos {

	public static Map<Integer, Parada> cargarParadas(String nombreArchivo) throws IOException {
		Map<Integer, Parada> paradas = new TreeMap<>();
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
                    System.err.println("Linea mal formateada: " + lineaActual);
                    continue;
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

	public static Map<String, Tramo> cargarTramos(String nombreArchivo, Map<Integer, Parada> paradas)
			throws FileNotFoundException {
				
		Map<String, Tramo> tramos = new TreeMap<>();
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
					System.err.println("Linea mal formateada: " + lineaActual);
					continue;
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
					//////////////////////////////////////////////////////////////////////////////////
					String tramoKey = String.format("%d", tramoActual.hashCode());
					tramos.put(tramoKey, tramoActual);
					////////////////////////////////////////////////////////////////////////////////// Key???


				} catch (NumberFormatException e) {
					System.err.println("index de parada inicial/final o tiempo invalidos en la linea: " + lineaActual);
				}
			}
		}
		return tramos;
	}

	public static Map<String, Linea> cargarLineas(String nombreArchivo, String nombreArchivoFrecuencia,
			Map<Integer, Parada> paradas) throws FileNotFoundException {

		Map<String, Linea> lineas = new TreeMap<>();
		Map<String, List<String[]>> frecuencias = new TreeMap<>();
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
					System.err.println("Frecuencia mal formateada en linea " + lineaFrecuencia);
					continue;
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
                    System.err.println("Malformed line: " + lineaArchivo);
                    continue;
                }

                String codigoLinea = partesLinea[0].trim();
				String nombreLinea = partesLinea[1].trim();

				Linea lineaActual = new Linea(codigoLinea, nombreLinea); 

                for (int i = 2; i < partesLinea.length; i++) {
                    int codigoParada = Integer.parseInt(partesLinea[i].trim());
                    if (paradas.containsKey(codigoParada)) {
						lineaActual.agregarParada(paradas.get(codigoParada));
                    }
                }
				if (frecuencias.containsKey(codigoLinea)) {
					for (String[] detallesFrecuencia : frecuencias.get(codigoLinea)) {
						try {
							int diaSemana = Integer.parseInt(detallesFrecuencia[0]);
							LocalTime inicioRecorrido = LocalTime.parse(detallesFrecuencia[1]);
							lineaActual.agregarFrecuencia(diaSemana, inicioRecorrido);
						}	catch (Exception e) {
							e.printStackTrace();
							System.err.println("Frecuencia invalida para linea " + codigoLinea);
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