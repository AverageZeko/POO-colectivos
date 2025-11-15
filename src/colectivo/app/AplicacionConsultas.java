package colectivo.app;

import colectivo.controlador.Coordinador;

/**
 * Punto de entrada principal de la aplicación de consultas de colectivos.
 * Se encarga de inicializar y configurar los componentes clave como el
 * coordinador, las ventanas de la interfaz de usuario, y los servicios de lógica de negocio.
 */
public class AplicacionConsultas {
    private static Coordinador coordinador;

    public static void main(String[] args) {
        coordinador = new Coordinador();
        coordinador.mostrar(args);
    }
}