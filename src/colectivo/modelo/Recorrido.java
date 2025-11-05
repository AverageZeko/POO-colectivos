package colectivo.modelo;

import java.time.LocalTime;
import java.util.List;

/**
 * Representa un segmento de un viaje, que puede ser en colectivo o caminando.
 * Incluye la línea utilizada (si aplica), las paradas involucradas, la hora de
 * salida y la duración del segmento.
 */
public class Recorrido {

	private Linea linea;
	private List<Parada> paradas;
	private LocalTime horaSalida;
	private int duracion;

	/**
	 * Constructor para un recorrido.
	 *
	 * @param linea la línea de colectivo utilizada (puede ser null si es un recorrido a pie).
	 * @param paradas la lista de paradas que componen el recorrido.
	 * @param horaSalida la hora de inicio del recorrido.
	 * @param duracion la duración total del recorrido en minutos.
	 */
	public Recorrido(Linea linea, List<Parada> paradas, LocalTime horaSalida, int duracion) {
		super();
		this.linea = linea;
		this.paradas = paradas;
		this.horaSalida = horaSalida;
		this.duracion = duracion;
	}

	/**
	 * Obtiene la línea asociada a este recorrido.
	 *
	 * @return la línea, o null si es un tramo a pie.
	 */
	public Linea getLinea() {
		return linea;
	}

	/**
	 * Establece la línea de este recorrido.
	 *
	 * @param linea la nueva línea.
	 */
	public void setLinea(Linea linea) {
		this.linea = linea;
	}

	/**
	 * Obtiene la lista de paradas del recorrido.
	 *
	 * @return la lista de paradas.
	 */
	public List<Parada> getParadas() {
		return paradas;
	}

	/**
	 * Establece la lista de paradas del recorrido.
	 *
	 * @param paradas la nueva lista de paradas.
	 */
	public void setParadas(List<Parada> paradas) {
		this.paradas = paradas;
	}

	/**
	 * Obtiene la hora de salida del recorrido.
	 *
	 * @return la hora de salida.
	 */
	public LocalTime getHoraSalida() {
		return horaSalida;
	}

	/**
	 * Establece la hora de salida del recorrido.
	 *
	 * @param horaSalida la nueva hora de salida.
	 */
	public void setHoraSalida(LocalTime horaSalida) {
		this.horaSalida = horaSalida;
	}

	/**
	 * Obtiene la duración del recorrido en minutos.
	 *
	 * @return la duración en minutos.
	 */
	public int getDuracion() {
		return duracion;
	}

	/**
	 * Establece la duración del recorrido en minutos.
	 *
	 * @param duracion la nueva duración.
	 */
	public void setDuracion(int duracion) {
		this.duracion = duracion;
	}

	
}