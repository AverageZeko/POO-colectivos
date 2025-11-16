package colectivo.interfaz;

import colectivo.controlador.Coordinador;

/**
 * Define el contrato común para todas las ventanas principales de la aplicación.
 * Esto permite al Coordinador y al GestorDeVentanas interactuar con las ventanas
 * de una manera agnóstica a su implementación concreta.
 */
public interface Mostrable {

    void setCoordinador(Coordinador coord);
    void mostrar(String[] arg);

   
}