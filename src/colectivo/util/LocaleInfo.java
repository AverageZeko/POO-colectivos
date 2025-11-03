package colectivo.util;

/**
 * Un record para almacenar la información de una localización descubierta.
 *
 * @param codigoCompleto Ej: "es_ARG", "en_UK", "medsea_PIR"
 * @param lang           Ej: "es", "en", "medsea"
 * @param country        Ej: "ARG", "UK", "PIR"
 */
public record LocaleInfo(String codigoCompleto, String lang, String country) {
    /**
     * Devuelve la ruta al archivo de la bandera correspondiente.
     * Ej: "/localizacion/es_ARG.png"
     */
    public String getRutaBandera() {
        return "/localizacion/" + codigoCompleto + ".png";
    }

    /**
     * Devuelve el nombre base para el ResourceBundle.
     * Ej: "localizacion.label_es_ARG"
     */
    public String getNombreBaseBundle() {
        return "localizacion.label_" + codigoCompleto;
    }

}