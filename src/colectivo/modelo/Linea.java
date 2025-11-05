package colectivo.modelo;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Representa una línea de colectivo, con su código, nombre,
 * un listado de paradas por las que pasa y las frecuencias de paso.
 */
public class Linea {

	private String codigo;
	private String nombre;
	private List<Parada> paradas;
	private List<Frecuencia> frecuencias;

	/**
	 * Constructor por defecto. Inicializa las listas de paradas y frecuencias.
	 */
	public Linea() {
		this.paradas = new ArrayList<Parada>();
		this.frecuencias = new ArrayList<Frecuencia>();
	}

	/**
	 * Constructor con código y nombre.
	 *
	 * @param codigo el código único de la línea (ej: "101").
	 * @param nombre el nombre descriptivo de la línea (ej: "Ramal A").
	 */
	public Linea(String codigo, String nombre) {
		super();
		this.codigo = codigo;
		this.nombre = nombre;
		this.paradas = new ArrayList<Parada>();
		this.frecuencias = new ArrayList<Frecuencia>();
	}
	
	
	/**
	 * Obtiene una lista de las horas de frecuencia para un día específico de la semana.
	 *
	 * @param diaSemana el día de la semana (según la convención utilizada, ej: 1 para Lunes).
	 * @return una lista de {@link LocalTime} con las horas de paso para ese día.
	 */
	public List<LocalTime> getHorasFrecuenciaPorDia(int diaSemana) {
	    List<LocalTime> horas = new ArrayList<>();
	    for (Frecuencia f : frecuencias) {
	        if (f.diaSemana == diaSemana) {
	            horas.add(f.hora);
	        }
	    }
	    return horas;
	}

	/**
	 * Agrega una parada a la lista de paradas de esta línea y actualiza la parada
	 * para que sepa que esta línea pasa por ella.
	 *
	 * @param parada la parada a agregar.
	 */
	public void agregarParada(Parada parada) {
		paradas.add(parada);
		parada.agregarLinea(this);
	}

	/**
	 * Agrega un horario de frecuencia para un día de la semana específico.
	 *
	 * @param diaSemana el día de la semana.
	 * @param hora la hora de paso.
	 */
	public void agregarFrecuencia(int diaSemana, LocalTime hora) {
		frecuencias.add(new Frecuencia(diaSemana, hora));
	}

	/**
	 * Devuelve una lista no modificable de las frecuencias de la línea.
	 *
	 * @return la lista de frecuencias.
	 */
	public List<Frecuencia> getFrecuencias() {
		return Collections.unmodifiableList(frecuencias);
	}

	/**
	 * Obtiene el código de la línea.
	 *
	 * @return el código.
	 */
	public String getCodigo() {
		return codigo;
	}

	/**
	 * Establece el código de la línea.
	 *
	 * @param codigo el nuevo código.
	 */
	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	/**
	 * Obtiene el nombre de la línea.
	 *
	 * @return el nombre.
	 */
	public String getNombre() {
		return nombre;
	}

	/**
	 * Establece el nombre de la línea.
	 *
	 * @param nombre el nuevo nombre.
	 */
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	/**
	 * Obtiene la lista de paradas por las que pasa esta línea.
	 *
	 * @return la lista de paradas.
	 */
	public List<Parada> getParadas() {
		return paradas;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((codigo == null) ? 0 : codigo.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Linea other = (Linea) obj;
		if (codigo == null) {
			if (other.codigo != null)
				return false;
		} else if (!codigo.equals(other.codigo))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Linea [codigo=" + codigo + ", nombre=" + nombre + "]";
	}

	/**
	 * Clase interna que representa la frecuencia de paso de una línea en un
	 * día y hora específicos.
	 */
	private class Frecuencia {

		private int diaSemana;
		private LocalTime hora;

		/**
		 * Constructor para una Frecuencia.
		 *
		 * @param diaSemana el día de la semana.
		 * @param hora la hora de la frecuencia.
		 */
		public Frecuencia(int diaSemana, LocalTime hora) {
			super();
			this.diaSemana = diaSemana;
			this.hora = hora;
		}

		/**
		 * Obtiene el día de la semana.
		 * @return el día de la semana.
		 */
		public int getDiaSemana() {
			return diaSemana;
		}

		/**
		 * Establece el día de la semana.
		 * @param diaSemana el nuevo día de la semana.
		 */
		public void setDiaSemana(int diaSemana) {
			this.diaSemana = diaSemana;
		}

		/**
		 * Obtiene la hora de la frecuencia.
		 * @return la hora.
		 */
		public LocalTime getHora() {
			return hora;
		}

		/**
		 * Establece la hora de la frecuencia.
		 * @param hora la nueva hora.
		 */
		public void setHora(LocalTime hora) {
			this.hora = hora;
		}

		@Override
		public String toString() {
			return "Frecuencia [diaSemana=" + diaSemana + ", hora=" + hora + "]";
		}

		
	}
}