package colectivo.interfaz.javafx;

/**
 * ParadaOpcion es un DTO ligero para la UI que representa una parada por su id y nombre visible.
 * Evita exponer objetos de dominio (Parada) en la interfaz.
 */
public class ParadaOpcion {

    /** ID de la parada. */
    private final int id;

    /** Nombre visible de la parada. */
    private final String nombre;

    /**
     * Crea una nueva opción de parada con el id y nombre especificados.
     * @param id ID de la parada.
     * @param nombre Nombre visible de la parada.
     */
    public ParadaOpcion(int id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    /**
     * Devuelve el ID de la parada.
     * @return ID de la parada.
     */
    public int getId() {
        return id;
    }

    /**
     * Devuelve el nombre visible de la parada.
     * @return Nombre de la parada.
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Devuelve el nombre de la parada como representación textual.
     * @return Nombre de la parada.
     */
    @Override
    public String toString() {
        return nombre;
    }
}