package colectivo.dao.postgresql;

public class SchemaPostgresqlDAO {
    private static String schema = "colectivo_PM";

    public static void setSchema(String nuevoSchema) {
        schema = nuevoSchema;
    }

    public static String getSchema() {
        return schema;
    }
}
