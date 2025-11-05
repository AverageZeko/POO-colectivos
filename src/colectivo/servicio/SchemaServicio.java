package colectivo.servicio;

/**
 * Interfaz que define el contrato para los servicios que gestionan el esquema de la base de datos.
 * <p>
 * Esta capa de servicio permite a la aplicación cambiar dinámicamente el esquema de la base de datos
 * que se está utilizando, lo que es útil para trabajar con datos de diferentes ciudades.
 * </p>
 */
public interface SchemaServicio {
    
    /**
     * Cambia el esquema activo que utilizarán los DAOs de base de datos.
     *
     * @param nuevoSchema El nombre del nuevo esquema a establecer. No puede ser nulo ni vacío.
     */
    void cambiarSchema(String nuevoSchema);
    
    /**
     * Obtiene el nombre del esquema que está actualmente en uso.
     *
     * @return El nombre del esquema activo.
     */
    String obtenerSchema();
}