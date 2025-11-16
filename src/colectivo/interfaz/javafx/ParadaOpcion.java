package colectivo.interfaz.javafx;

/**
 * DTO ligero para la UI: representa una parada por id y nombre visible.
 * Evita exponer objetos de dominio (Parada) en la interfaz.
 */
public class ParadaOpcion {
    private final int id;
    private final String nombre;

    public ParadaOpcion(int id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    @Override
    public String toString() {
        // Lo que verá el usuario en el ComboBox
        return nombre;
    }
}