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
