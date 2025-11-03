package colectivo.dao.postgresql;

/**
 * Clase de utilidad para gestionar el esquema activo de PostgreSQL en tiempo de ejecución.
 *
 * <p>Esta clase mantiene el nombre del esquema de base de datos que será utilizado por los DAOs
 * de PostgreSQL. Permite cambiar dinámicamente entre diferentes esquemas (por ejemplo, diferentes
 * ciudades) sin necesidad de cambiar la configuración de conexión.</p>
 *
 * <p>Los DAOs de PostgreSQL deben consultar
 * el esquema actual mediante {@link #getSchema()} antes de ejecutar consultas SQL, típicamente
 * usando {@code SET search_path TO 'esquema'} en PostgreSQL.</p>
 *
 * @see colectivo.dao.postgresql.LineaPostgresqlDAO
 * @see colectivo.dao.postgresql.ParadaPostgresqlDAO
 * @see colectivo.dao.postgresql.TramoPostgresqlDAO
 */
public class SchemaPostgresqlDAO {
    private static String schema = "colectivo_PM";

    public static void setSchema(String nuevoSchema) {
        schema = nuevoSchema;
    }

    public static String getSchema() {
        return schema;
    }
}
