package colectivo.interfaz;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;
import java.util.Map;

import colectivo.modelo.Parada;
import colectivo.modelo.Recorrido;
import colectivo.util.Tiempo;

public class Interfaz {

    private static Scanner scanner = new Scanner(System.in);
    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

    // Usuario ingresa parada origen
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

    // Usuario ingresa parada destino
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

    // Usuario ingresa día de la semana
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

    // Usuario ingresa hora de llegada a la parada
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

    // Mostrar los resultados
    public static void resultado(List<List<Recorrido>> listaRecorridos, Parada paradaOrigen,
            Parada paradaDestino, LocalTime horaLlegaParada) {

        if (listaRecorridos.isEmpty()) {
            System.out.println("No se encontraron recorridos desde " + paradaOrigen.getDireccion() + " hasta "
                    + paradaDestino.getDireccion());
            return;
        }

        System.out.println("Recorridos disponibles desde " + paradaOrigen.getDireccion() + " hasta "
                + paradaDestino.getDireccion() + ":");

        int i = 1;
        for (List<Recorrido> recorridoCompleto : listaRecorridos) {
            System.out.println("\nRecorrido " + i + ":");
            for (Recorrido r : recorridoCompleto) {

                // Solo mostramos la información, sin calcular la hora del colectivo
                System.out.println("  - Linea: " + r.getLinea().getCodigo());
                System.out.println("    Recorrido desde " + r.getParadas().get(0).getDireccion()
                        + " hasta " + r.getParadas().get(r.getParadas().size() - 1).getDireccion());
                System.out.println("    Hora usuario en origen: " + horaLlegaParada);
                System.out.println("    Hora colectivo en origen: " + "N/A");
                System.out.println("    Tiempo total: " + Tiempo.segundosATiempo(r.getDuracion()) + " min");

                // Mostrar paradas sin flecha extra al final
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

