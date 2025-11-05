package colectivo.modelo;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa una parada de colectivo, identificada por un código único
 * y ubicada en una dirección y coordenadas geográficas.
 */
public class Parada {

	private int codigo;
	private String direccion;
	private List<Linea> lineas;
	private List<Parada> paradaCaminando;
	private double latitud;
	private double longitud;

	/**
	 * Constructor por defecto. Inicializa las listas internas.
	 */
	public Parada() {
		this.lineas = new ArrayList<Linea>();
		this.paradaCaminando = new ArrayList<Parada>();
	}

	/**
	 * Constructor con todos los atributos de la parada.
	 *
	 * @param codigo el código único de la parada.
	 * @param direccion la dirección de la parada.
	 * @param latitud la coordenada de latitud.
	 * @param longitud la coordenada de longitud.
	 */
	public Parada(int codigo, String direccion, double latitud, double longitud) {
		super();
		this.codigo = codigo;
		this.direccion = direccion;
		this.latitud = latitud;
		this.longitud = longitud;
		this.lineas = new ArrayList<Linea>();
		this.paradaCaminando = new ArrayList<Parada>();
	}

	/**
	 * Agrega una línea a la lista de líneas que pasan por esta parada.
	 *
	 * @param linea la línea a agregar.
	 */
	public void agregarLinea(Linea linea) {
		this.lineas.add(linea);
	}

	/**
	 * Agrega una parada cercana a la que se puede llegar caminando.
	 *
	 * @param parada la parada cercana.
	 */
	public void agregarParadaCaminado(Parada parada) {
		this.paradaCaminando.add(parada);
	}

	/**
	 * Obtiene el código de la parada.
	 *
	 * @return el código.
	 */
	public int getCodigo() {
		return codigo;
	}

	/**
	 * Establece el código de la parada.
	 *
	 * @param codigo el nuevo código.
	 */
	public void setCodigo(int codigo) {
		this.codigo = codigo;
	}

	/**
	 * Obtiene la dirección de la parada.
	 *
	 * @return la dirección.
	 */
	public String getDireccion() {
		return direccion;
	}

	/**
	 * Establece la dirección de la parada.
	 *
	 * @param direccion la nueva dirección.
	 */
	public void setDireccion(String direccion) {
		this.direccion = direccion;
	}

	/**
	 * Obtiene la latitud de la parada.
	 *
	 * @return la latitud.
	 */
	public double getLatitud() {
		return latitud;
	}

	/**
	 * Establece la latitud de la parada.
	 *
	 * @param latitud la nueva latitud.
	 */
	public void setLatitud(double latitud) {
		this.latitud = latitud;
	}

	/**
	 * Obtiene la longitud de la parada.
	 *
	 * @return la longitud.
	 */
	public double getLongitud() {
		return longitud;
	}

	/**
	 * Establece la longitud de la parada.
	 *
	 * @param longitud la nueva longitud.
	 */
	public void setLongitud(double longitud) {
		this.longitud = longitud;
	}

	/**
	 * Obtiene la lista de líneas que pasan por esta parada.
	 *
	 * @return la lista de líneas.
	 */
	public List<Linea> getLineas() {
		return lineas;
	}
		
	/**
	 * Obtiene la lista de paradas a las que se puede llegar caminando desde esta.
	 *
	 * @return la lista de paradas cercanas.
	 */
	public List<Parada> getParadaCaminando() {
		return paradaCaminando;
	}

	@Override
	public String toString() {
		return "Parada [codigo=" + codigo + ", direccion=" + direccion + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + codigo;
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
		Parada other = (Parada) obj;
		if (codigo != other.codigo)
			return false;
		return true;
	}

}