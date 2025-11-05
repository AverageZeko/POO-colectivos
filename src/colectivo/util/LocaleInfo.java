package colectivo.util;

/**
 * Representa la información de una localización (idioma/país) descubierta en la aplicación.
 * Es un {@code record} para un almacenamiento de datos inmutable y conciso.
 *
 * @param codigoCompleto El código completo de la localización, ej: "es_ARG", "en_UK".
 * @param lang           El código del idioma, ej: "es", "en".
 * @param country        El código del país o región, ej: "ARG", "UK".
 */
public record LocaleInfo(String codigoCompleto, String lang, String country) {
    /**
     * Construye la ruta relativa al archivo de la bandera correspondiente en la carpeta de recursos.
     *
     * @return La ruta al archivo de la bandera, ej: "/localizacion/es_ARG.png".
     */
    public String getRutaBandera() {
        return "/localizacion/" + codigoCompleto + ".png";
    }

    /**
     * Construye el nombre base para cargar el {@link java.util.ResourceBundle} de esta localización.
     *
     * @return El nombre base del bundle, ej: "localizacion.label_es_ARG".
     */
    public String getNombreBaseBundle() {
        return "localizacion.label_" + codigoCompleto;
    }

}