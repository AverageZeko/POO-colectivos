package colectivo.dao.secuencial;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import colectivo.dao.TramoDAO;
import colectivo.modelo.Parada;
import colectivo.modelo.Tramo;

public class TramoSecuencialDAO implements TramoDAO {
    private String archivo;
    private Map<String, Tramo> tramos;

    public TramoSecuencialDAO() {
        Properties prop = ArchivoSecuencialDAO.leerArchivo();
        archivo = prop.getProperty("tramo");
		if (archivo == null) {
			throw new IllegalStateException("Error al cargar archivo de tramos en src/resource.");
		}
    }

    @Override
    public Map<String, Tramo> buscarTramos(Map<Integer, Parada> paradas) {
        if (tramos == null) {
            tramos = new HashMap<>();
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
                        throw new IllegalStateException("index de parada inicial/final o tiempo invalidos en la linea: " + lineaActual);
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
