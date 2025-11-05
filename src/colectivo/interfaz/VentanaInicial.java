package colectivo.interfaz;

import colectivo.controlador.Coordinador;
import javafx.stage.Stage;

/**
 * Define el contrato para la ventana de bienvenida o inicio de la aplicación.
 * Esta ventana es la primera que ve el usuario y es responsable de recoger
 * la configuración inicial, como la ciudad y el idioma.
 */
public interface VentanaInicial {

    /**
     * Inyecta una instancia del {@link Coordinador} para permitir la comunicación
     * entre la vista y la lógica de negocio.
     * @param coord El coordinador a establecer.
     */
    void setCoordinador(Coordinador coord);

    /**
     * Lanza la aplicación JavaFX. Este método típicamente llama a {@code Application.launch()}.
     * @param args Argumentos de la línea de comandos pasados a la aplicación.
     */
    void lanzarAplicacion(String[] args);

    /**
     * Cierra la ventana inicial.
     * @param stage El {@link Stage} que debe ser cerrado.
     */
    void close(Stage stage);
}