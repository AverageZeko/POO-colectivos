package colectivo.dao.secuencial;

import java.io.InputStream;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import colectivo.dao.LineaDAO;
import colectivo.modelo.Linea;
import colectivo.modelo.Parada;

public class LineaSecuencialDAO implements LineaDAO {
    private String archivoLinea;
    private String archivoFrecuencia;
    private Map<String, Linea> lineas;

    public LineaSecuencialDAO() {
        Properties prop = ArchivoSecuencialDAO.leerArchivo();
        archivoLinea = prop.getProperty("linea");
        archivoFrecuencia = prop.getProperty("frecuencia");
		if (archivoLinea == null) {
			throw new IllegalStateException("Error al cargar archivo de lineas en src/resource.");
		}
        if (archivoFrecuencia == null) {
			throw new IllegalStateException("Error al cargar archivo de frecuencias en src/resource.");
		}
    }


    public Map<String, Linea> buscarLineas(Map<Integer, Parada> paradas) {
        if (lineas == null) {
            lineas = new HashMap<>();
            Map<String, List<String[]>> frecuencias = buscarFrecuencias();
        
            InputStream lineaInputStream = LineaSecuencialDAO.class.getClassLoader().getResourceAsStream("resources/" + archivoLinea);
            if (lineaInputStream == null) {
                throw new IllegalStateException("No fue posible encontrar " + archivoLinea + " en la carpeta resources del classpath.");
            }

            try (Scanner contenidoLineas = new Scanner(lineaInputStream)) {
                while (contenidoLineas.hasNextLine()) {
                    String lineaArchivo = contenidoLineas.nextLine().trim();
                    if (lineaArchivo.isEmpty() || lineaArchivo.startsWith("#")) {
                        continue;
                    }

                    String[] partesLinea = lineaArchivo.split(";");
                    if (partesLinea.length < 3) {
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
                            }
                        }
                    }	catch (NumberFormatException e) {
                        throw new IllegalStateException("Codigo de parada mal formateado: " + lineaArchivo);
                    }

                    if (frecuencias.containsKey(codigoLinea)) {
                        for (String[] detallesFrecuencia : frecuencias.get(codigoLinea)) {
                            try {
                                int diaSemana = Integer.parseInt(detallesFrecuencia[0]);
                                LocalTime inicioRecorrido = LocalTime.parse(detallesFrecuencia[1]);
                                lineaActual.agregarFrecuencia(diaSemana, inicioRecorrido);
                            }	catch (IllegalArgumentException e) {
                                throw new IllegalStateException("Frecuencia invalida para linea: " + frecuencias.get(codigoLinea));
                            }
                        }
                    }	else {
                        throw new IllegalStateException("AVISO: No se encontro ninguna frecuencia para linea " + codigoLinea);
                    }
                    lineas.put(codigoLinea, lineaActual);
                }
            }
        }
    
        return lineas;
    }


    private Map<String, List<String[]>> buscarFrecuencias() {
        Map<String, List<String[]>> frecuencias = new HashMap<>();
		InputStream frecuenciaInputStream = LineaSecuencialDAO.class.getClassLoader().getResourceAsStream("resources/" + archivoFrecuencia);
		if (frecuenciaInputStream == null) {
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
        return frecuencias;
    }


    public String getArchivoLinea() {
        return archivoLinea;
    }

    public String getArchivoFrecuencia() {
        return archivoFrecuencia;
    }

}


