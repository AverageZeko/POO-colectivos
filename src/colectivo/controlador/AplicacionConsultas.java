package colectivo.controlador;

import colectivo.logica.Calculo;
import colectivo.servicio.SchemaServicio;
import colectivo.servicio.SchemaServicioImplementacion;
import colectivo.dao.postgresql.SchemaPostgresqlDAO;
import colectivo.interfaz.Interfaz;
import colectivo.interfaz.VentanaConsultas;
import colectivo.interfaz.VentanaInicial;
import colectivo.interfaz.VentanaInicio;
import colectivo.util.LocaleInfo;

/**
 * Punto de entrada principal. Se ajustó para inicializar estáticamente el coordinador
 * y asegurar que esté disponible para la vista desde el arranque.
 */
public class AplicacionConsultas {
    private Coordinador coordinador;
    private VentanaInicial ventanaInicio;
    private VentanaConsultas ventanaConsultas;
    private SchemaServicio schema;
    private Calculo calculo;
    private LocaleInfo defaultLocale;

    public static void main(String[] args) {
        AplicacionConsultas aplicacion = new AplicacionConsultas();
        aplicacion.iniciar(args);
    }

    public void iniciar(String[] args) {
		coordinador = new Coordinador();
        ventanaInicio = new VentanaInicio();
		ventanaConsultas = new Interfaz();
		calculo = new Calculo();
		schema = new SchemaServicioImplementacion();
		defaultLocale = new LocaleInfo("es_ARG", "es", "ARG");

        coordinador.setSchemaServicio(schema);
		coordinador.setCalculo(calculo);
		coordinador.setLocalizacion(defaultLocale);

        coordinador.setVentanaInicio(ventanaInicio);
        coordinador.setVentanaConsultas(ventanaConsultas);
        ventanaInicio.setCoordinador(coordinador);
		ventanaConsultas.setCoordinador(coordinador);
        coordinador.iniciarAplicacion(args);

    }
}