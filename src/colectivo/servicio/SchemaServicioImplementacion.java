package colectivo.servicio;

import colectivo.dao.postgresql.SchemaPostgresqlDAO;

/**
 * Implementación del servicio {@link SchemaServicio} para gestionar el esquema activo de PostgreSQL.
 *
 * <p>Esta clase proporciona métodos para cambiar y buscar el esquema en la base de datos.
 * Delega las operaciones a {@link SchemaPostgresqlDAO} y añade validación de entrada
 * para garantizar que el nombre del esquema no sea nulo o vacío.</p>
 *
 * <p>Los nombres de los esquemas provienen de una lista proporcionada por la interfaz de usuario.</p>
 *
 * @see SchemaServicio
 * @see SchemaPostgresqlDAO
 */
public class SchemaServicioImplementacion implements SchemaServicio {

    /**
     * Cambia el esquema activo de la base de datos.
     *
     * @param nuevoSchema El nombre del nuevo esquema a establecer.
     * @throws IllegalArgumentException si {@code nuevoSchema} es nulo o una cadena vacía.
     */
    @Override
    public void cambiarSchema(String nuevoSchema) {
        if (nuevoSchema == null || nuevoSchema.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del esquema no puede ser nulo o vacío.");
        }
        SchemaPostgresqlDAO.setSchema(nuevoSchema);
    }

    /**
     * Obtiene el nombre del esquema actualmente activo.
     *
     * @return El nombre del esquema actual.
     */
    @Override
    public String obtenerSchema() {
        return SchemaPostgresqlDAO.getSchema();
    }
    
}