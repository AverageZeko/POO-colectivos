package colectivo.dao.postgresql;

public class SchemaPostgresqlDAO {
    private static String schema = "colectivo_AZL";

    public static void setSchema(String nuevoSchema) {
        schema = nuevoSchema;
    }

    public static String getSchema(/* boolean schemaIncorrecto */) {
/*         if (schemaIncorrecto) {
            setSchema("colectivo_AZL");
        }
 */
        return schema;
    }
}
