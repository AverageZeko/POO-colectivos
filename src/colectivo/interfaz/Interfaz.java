package colectivo.interfaz;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;
import java.util.Map;

import colectivo.modelo.Parada;
import colectivo.modelo.Recorrido;
import colectivo.util.Tiempo;


/**
 * Clase para la interfaz de usuario del sistema de consultas de colectivos.
 * <p>
 * Esta clase proporciona métodos estáticos para interactuar con el usuario
 * a través de la consola, permitiendo el ingreso de datos para consultas
 * de recorridos.
 * <p>
 * La interfaz permite al usuario:
 * <ul>
 * <li>Ingresar paradas de origen y destino</li>
 * <li>Seleccionar día de la semana</li>
 * <li>Especificar hora de llegada</li>
 * </ul>
 * <p>
 * Con los datos proporcionados se le presentan todos los recorridos disponibles al usuario
 * 
 */
public class Interfaz {

    /** Scanner para leer entrada del usuario desde la consola */
    private static Scanner scanner = new Scanner(System.in);

    /** Formateador para parsing y display de horarios en formato HH:mm */
    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Solicita al usuario el código de la parada de origen.
     * <p>
     * El método valida que el código ingresado sea un número entero válido
     * y que corresponda a una parada existente en el mapa proporcionado.
     * En caso de error, muestra un mensaje apropiado y solicita nuevamente
     * el ingreso hasta obtener una parada válida.
     * 
     * @param paradas mapa de paradas disponibles donde la clave es el código
     *                de la parada y el valor es el objeto Parada
     * @return la Parada seleccionada como origen del recorrido
     * @see colectivo.modelo.Parada
     */
    public static Parada ingresarParadaOrigen(Map<Integer, Parada> paradas) {
        while (true) {
            System.out.print("Ingrese código de parada origen: ");
            String entrada = scanner.nextLine().trim();
            try {
                int codigo = Integer.parseInt(entrada);
                if (paradas.get(codigo) != null) {
                    return paradas.get(codigo);
                } else {
                    System.out.println("Parada no encontrada. Intente nuevamente.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Ingrese un número válido.");
            }
        }
    }

    /**
     * Solicita al usuario el código de la parada de destino.
     * <p>
     * El método valida que el código ingresado sea un número entero válido
     * y que corresponda a una parada existente en el mapa proporcionado.
     * En caso de error, muestra un mensaje apropiado y solicita nuevamente
     * el ingreso hasta obtener una parada válida.
     * 
     * @param paradas mapa de paradas disponibles donde la clave es el código
     *                de la parada y el valor es el objeto Parada
     * @return la Parada seleccionada como destino del recorrido
     * @see colectivo.modelo.Parada
     */
    public static Parada ingresarParadaDestino(Map<Integer, Parada> paradas) {
        while (true) {
            System.out.print("Ingrese código de parada destino: ");
            String entrada = scanner.nextLine().trim();
            try {
                int codigo = Integer.parseInt(entrada);
                if (paradas.get(codigo) != null) {
                    return paradas.get(codigo);
                } else {
                    System.out.println("Parada no encontrada. Intente nuevamente.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Ingrese un número válido.");
            }
        }
    }

    /**
     * Solicita al usuario el día de la semana para la consulta.
     * <p>
     * El método valida que el valor ingresado sea un número entero
     * entre 1 y 7, donde:
     * <ul>
     * <li>1 = Lunes</li>
     * <li>2 = Martes</li>
     * <li>3 = Miércoles</li>
     * <li>4 = Jueves</li>
     * <li>5 = Viernes</li>
     * <li>6 = Sábado</li>
     * <li>7 = Domingo</li>
     * </ul>
     * En caso de valor inválido, solicita nuevamente el ingreso.
     * @return número entero representando el día de la semana (1-7)
     */
    public static int ingresarDiaSemana() {
        while (true) {
            System.out.print("Ingrese día de la semana (1=Lunes, ..., 7=Domingo): ");
            String entrada = scanner.nextLine().trim();
            try {
                int dia = Integer.parseInt(entrada);
                if (dia >= 1 && dia <= 7) {
                    return dia;
                } else {
                    System.out.println("Día inválido. Intente nuevamente.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Ingrese un número válido.");
            }
        }
    }

    /**
     * Solicita al usuario la hora de llegada a la parada de origen.
     * <p>
     * El método valida que la hora ingresada tenga el formato HH:mm
     * (por ejemplo: "10:35"). En caso de formato incorrecto,
     * muestra un mensaje de error y solicita nuevamente el ingreso.
     * @return objeto LocalTime representando la hora de llegada
     */
    public static LocalTime ingresarHoraLlegaParada() {
        while (true) {
            System.out.print("Ingrese hora de llegada (HH:mm): ");
            String entrada = scanner.nextLine().trim();
            try {
                return LocalTime.parse(entrada, formatter);
            } catch (Exception e) {
                System.out.println("Formato incorrecto. Ejemplo válido: 10:35");
            }
        }
    }

    /**
     * Muestra los resultados de la consulta de recorridos de forma detallada.
     * <p>
     * Para cada recorrido encontrado, se muestra:
     * <ul>
     * <li>Información de la línea</li>
     * <li>Paradas de origen y destino</li>
     * <li>Horarios de salida y llegada</li>
     * <li>Tiempo de espera en la parada</li>
     * <li>Duración del viaje</li>
     * <li>Tiempo total del recorrido</li>
     * <li>Secuencia completa de paradas</li>
     * </ul>
     * <p>
     * Si no se encuentran recorridos, muestra un mensaje para informar al usuario.
     * Los tiempos se formatean usando la clase utilitaria Tiempo.
     * @see colectivo.util.Tiempo#segundosATiempo(int)
     * 
     * @param listaRecorridos lista de listas de recorridos encontrados,
     *                        donde cada lista interna representa un recorrido completo
     *                        (que puede incluir transbordos)
     * @param paradaOrigen parada de origen seleccionada por el usuario
     * @param paradaDestino parada de destino seleccionada por el usuario
     * @param horaLlegaParada hora en que el usuario llega a la parada de origen
     * 
     * @see colectivo.modelo.Recorrido
     * @see colectivo.modelo.Parada
     */
    public static void resultado(List<List<Recorrido>> listaRecorridos,
            Parada paradaOrigen,
            Parada paradaDestino,
            LocalTime horaLlegaParada) {

        if (listaRecorridos.isEmpty()) {
        System.out.println("No se encontraron recorridos desde " + paradaOrigen.getDireccion()
        + " hasta " + paradaDestino.getDireccion());
        return;
        }

        System.out.println("Recorridos disponibles desde "
        + paradaOrigen.getDireccion() + " hasta " + paradaDestino.getDireccion() + ":");

        int i = 1;
        for (List<Recorrido> recorridoCompleto : listaRecorridos) {
            System.out.println("\nRecorrido " + i + ":");

            for (Recorrido r : recorridoCompleto) {
                LocalTime horaSalida = r.getHoraSalida();

                // Espera (en segundos)
                long esperaSeg = 0;
                if (horaSalida.isAfter(horaLlegaParada)) {
                esperaSeg = Duration.between(horaLlegaParada, horaSalida).getSeconds();
                }

                int viajeSeg = r.getDuracion();
                long totalSeg = esperaSeg + viajeSeg;

                LocalTime horaLlegada = horaSalida.plusSeconds(viajeSeg);

                System.out.println("  - Línea: " + r.getLinea().getCodigo());
                System.out.println("    Origen usuario: " + paradaOrigen.getDireccion());
                System.out.println("    Destino: " + paradaDestino.getDireccion());
                System.out.println("    Hora llegada usuario a origen: " + horaLlegaParada);
                System.out.println("    Hora salida colectivo: " + horaSalida);
                System.out.println("    Tiempo de espera: " + Tiempo.segundosATiempo((int) esperaSeg));
                System.out.println("    Tiempo de viaje: " + Tiempo.segundosATiempo(viajeSeg));
                System.out.println("    Duración total: " + Tiempo.segundosATiempo((int) totalSeg));
                System.out.println("    Hora de llegada destino: " + horaLlegada);

                // Mostrar paradas
                StringBuilder sb = new StringBuilder();
                for (int j = 0; j < r.getParadas().size(); j++) {
                    sb.append(r.getParadas().get(j).getDireccion());
                    if (j < r.getParadas().size() - 1) sb.append(" -> ");
                }
                System.out.println("    Paradas: " + sb);
            }
            i++;
        }
    }

}
