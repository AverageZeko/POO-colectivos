package colectivo.modelo;

import colectivo.controlador.Constantes;

/**
 * Representa un segmento o tramo entre dos paradas.
 * Puede ser un tramo recorrido por un colectivo o un tramo que se puede
 * hacer caminando.
 */
public class Tramo {

	private Parada inicio;
	private Parada fin;
	private int tiempo;
	private int tipo;

	/**
	 * Constructor por defecto.
	 */
	public Tramo() {
	}

	/**
	 * Constructor para crear un tramo con sus atributos.
	 * Si el tipo es CAMINANDO, actualiza las paradas de inicio y fin para
	 * registrar que son accesibles a pie entre sí.
	 *
	 * @param inicio la parada de inicio del tramo.
	 * @param fin la parada de fin del tramo.
	 * @param tiempo el tiempo en minutos que toma recorrer el tramo.
	 * @param tipo el tipo de tramo (ej. {@link Constantes#COLECTIVO} o {@link Constantes#CAMINANDO}).
	 */
	public Tramo(Parada inicio, Parada fin, int tiempo, int tipo) {
		super();
		this.inicio = inicio;
		this.fin = fin;
		this.tiempo = tiempo;
		this.tipo = tipo;		
		if (tipo == Constantes.CAMINANDO) {			
			inicio.agregarParadaCaminado(fin);
			fin.agregarParadaCaminado(inicio);
		}
	}

	/**
	 * Obtiene la parada de inicio del tramo.
	 * @return la parada de inicio.
	 */
	public Parada getInicio() {
		return inicio;
	}

	/**
	 * Establece la parada de inicio del tramo.
	 * @param inicio la nueva parada de inicio.
	 */
	public void setInicio(Parada inicio) {
		this.inicio = inicio;
	}

	/**
	 * Obtiene la parada de fin del tramo.
	 * @return la parada de fin.
	 */
	public Parada getFin() {
		return fin;
	}

	/**
	 * Establece la parada de fin del tramo.
	 * @param fin la nueva parada de fin.
	 */
	public void setFin(Parada fin) {
		this.fin = fin;
	}

	/**
	 * Obtiene el tiempo en minutos para recorrer el tramo.
	 * @return el tiempo en minutos.
	 */
	public int getTiempo() {
		return tiempo;
	}

	/**
	 * Establece el tiempo en minutos para recorrer el tramo.
	 * @param tiempo el nuevo tiempo.
	 */
	public void setTiempo(int tiempo) {
		this.tiempo = tiempo;
	}

	/**
	 * Obtiene el tipo de tramo (COLECTIVO o CAMINANDO).
	 * @return el tipo de tramo.
	 */
	public int getTipo() {
		return tipo;
	}

	/**
	 * Establece el tipo de tramo.
	 * @param tipo el nuevo tipo de tramo.
	 */
	public void setTipo(int tipo) {
		this.tipo = tipo;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fin == null) ? 0 : fin.hashCode());
		result = prime * result + ((inicio == null) ? 0 : inicio.hashCode());
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
		Tramo other = (Tramo) obj;
		if (fin == null) {
			if (other.fin != null)
				return false;
		} else if (!fin.equals(other.fin))
			return false;
		if (inicio == null) {
			if (other.inicio != null)
				return false;
		} else if (!inicio.equals(other.inicio))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Tramo [inicio=" + inicio + ", fin=" + fin + ", tiempo=" + tiempo + ", tipo=" + tipo + "]";
	}

}