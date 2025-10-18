package colectivo.dao.secuencial;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import colectivo.dao.ParadaDAO;
import colectivo.modelo.Parada;

public class ParadaSecuencialDAO implements ParadaDAO {
    private String archivo;
    private Map<Integer, Parada> paradas;

    public ParadaSecuencialDAO() {
        Properties prop = ArchivoSecuencialDAO.leerArchivo();
        archivo = prop.getProperty("parada");
		if (archivo == null) {
			throw new IllegalStateException("Error al cargar archivo de paradas en src/resource.");
		}
    }

    @Override
    public Map<Integer, Parada> buscarParadas() {
        if (paradas == null) {
            paradas = new HashMap<>();
            InputStream inputStream = ParadaSecuencialDAO.class.getClassLoader().getResourceAsStream("resources/" + archivo);
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
        }
    
		return paradas;
    }

    public String getArchivo() {
        return archivo;
    }

}
