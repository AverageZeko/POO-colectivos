package colectivo.controlador;

import colectivo.logica.Calculo;
import colectivo.servicio.SchemaServicio;
import colectivo.servicio.SchemaServicioImplementacion;
import colectivo.dao.postgresql.SchemaPostgresqlDAO;
import colectivo.interfaz.GestorDeVentanas;
import colectivo.interfaz.Interfaz;
import colectivo.interfaz.VentanaInicio;
import colectivo.util.LocaleInfo;

/**
 * Punto de entrada principal de la aplicación de consultas de colectivos.
 * Se encarga de inicializar y configurar los componentes clave como el
 * coordinador, las ventanas de la interfaz de usuario, y los servicios de lógica de negocio.
 */
public class AplicacionConsultas {
    private Coordinador coordinador;
    private GestorDeVentanas gestorDeVentanas;
    private SchemaServicio schema;
    private Calculo calculo;
    private LocaleInfo defaultLocale;

    /**
     * Método principal que inicia la aplicación.
     *
     * @param args los argumentos de la línea de comandos (no se utilizan).
     */
    public static void main(String[] args) {
        AplicacionConsultas aplicacion = new AplicacionConsultas();
        aplicacion.iniciar(args);
    }

    /**
     * Inicializa todos los componentes de la aplicación y establece las dependencias entre ellos.
     * Configura el coordinador con los servicios necesarios, las ventanas y la localización
     * por defecto, y luego lanza la ventana inicial.
     *
     * @param args los argumentos pasados al método main.
     */
    public void iniciar(String[] args) {
		coordinador = new Coordinador();
        gestorDeVentanas = new GestorDeVentanas();
		calculo = new Calculo();
		schema = new SchemaServicioImplementacion();
		defaultLocale = new LocaleInfo("es_ARG", "es", "ARG");

        // Configurar el coordinador con sus dependencias
        coordinador.setSchemaServicio(schema);
		coordinador.setCalculo(calculo);
		coordinador.setLocalizacion(defaultLocale);
        coordinador.setGestorDeVentanas(gestorDeVentanas);

        // Configurar el gestor de ventanas con su dependencia
        gestorDeVentanas.setCoordinador(coordinador);
        
        // Iniciar la aplicación a través del coordinador
        coordinador.iniciarAplicacion(args);

    }
}