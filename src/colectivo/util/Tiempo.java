package colectivo.util;


import java.time.LocalTime;

/**
 * Clase de utilidad para operaciones relacionadas con el tiempo.
 */
public class Tiempo {

	/**
	 * Convierte una cantidad total de segundos a un objeto {@link LocalTime}.
	 * Este método es útil para representar duraciones o intervalos.
	 *
	 * @param totalSegundos El número total de segundos a convertir.
	 * @return Un objeto {@link LocalTime} que representa las horas, minutos y segundos.
	 */
	public static LocalTime segundosATiempo(int totalSegundos) {

		// Calcular horas
		int horas = totalSegundos / 3600;
		int segundosRestantes = totalSegundos % 3600;

		// Calcular minutos
		int minutos = segundosRestantes / 60;
		int segundos = segundosRestantes % 60;

		return LocalTime.of(horas, minutos, segundos);
	}
}