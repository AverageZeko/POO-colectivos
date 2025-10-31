package colectivo.servicio;

import colectivo.dao.postgresql.SchemaPostgresqlDAO;

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
