package colectivo.servicio;

import colectivo.dao.postgresql.SchemaPostgresqlDAO;

/**
 * Implementación del servicio {@link SchemaServicio} para gestionar el esquema activo de PostgreSQL.
 *
 * <p>Esta clase proporciona metodos para cambiar y buscar el esquema en la base de datos.
 * El cual es utilizado por los DAOs de PostgreSQL. Delega las operaciones a {@link SchemaPostgresqlDAO} y añade
 * validación de entrada para garantizar que el esquema no sea nulo o vacío.</p>
 *
 * <p>Los esquemas recibidos por esta clase provienen de una lista proporcionada por la interfaz.</p>
 *
 * @see SchemaServicio
 * @see SchemaPostgresqlDAO
 */
public class SchemaServicioImplementacion implements SchemaServicio {

    @Override
    public void cambiarSchema(String nuevoSchema) {
        if (nuevoSchema == null || nuevoSchema.trim().isEmpty()) {
            throw new IllegalArgumentException("Schema vacío");
        }
        SchemaPostgresqlDAO.setSchema(nuevoSchema);
    }

    @Override
    public String obtenerSchema() {
        return SchemaPostgresqlDAO.getSchema();
    }
    
}
