package colectivo.interfaz;

import colectivo.controlador.Coordinador;
import javafx.stage.Stage;

/**
 * Define el contrato común para todas las ventanas principales de la aplicación.
 * Esto permite al Coordinador y al GestorDeVentanas interactuar con las ventanas
 * de una manera agnóstica a su implementación concreta.
 */
public interface Mostrable {

 
    /**
     * Muestra la ventana.
     * @param stage El {@link Stage} sobre el cual se construirá la escena.
     */
    void mostrar(Stage stage);

    /**
     * Cierra la ventana.
     * @param stage El {@link Stage} que debe ser cerrado.
     */
    void cerrar(Stage stage);
}