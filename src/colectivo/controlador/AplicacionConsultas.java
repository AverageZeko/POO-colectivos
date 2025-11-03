package colectivo.controlador;

import colectivo.logica.Calculo;
import colectivo.servicio.SchemaServicio;
import colectivo.servicio.SchemaServicioImplementacion;
import colectivo.dao.postgresql.SchemaPostgresqlDAO;
import colectivo.configuracion.Localizacion;
import colectivo.interfaz.Interfaz;
import colectivo.interfaz.Mostrable;

/**
 * Punto de entrada principal para la aplicación de consultas de recorridos de colectivos.
 *
 * <p>Esta clase inicializa la empresa y el coordinador, y gestiona el ciclo de vida
 * de la consulta de recorridos a través de la interfaz de usuario.</p>
 *
 */
public class AplicacionConsultas {
	private Coordinador controlador;
	private Mostrable interfaz;
	private Calculo calculo;
	private SchemaServicio schema;
	private Localizacion localizacion;

	public static void main(String[] args) {
		AplicacionConsultas aplicacion = new AplicacionConsultas();
		aplicacion.iniciar(args);
	}

	/**
	 * Inicializa el modelo, la vista y el controlador e inicia el programa
	 */
	public void iniciar(String[] args) {
		controlador = new Coordinador();
		interfaz = new Interfaz();
		calculo = new Calculo();
		schema = new SchemaServicioImplementacion();
		localizacion = new Localizacion();

		controlador.setSchemaServicio(schema);
		controlador.setInterfaz(interfaz);
		controlador.setCalculo(calculo);
		controlador.setLocalizacion(localizacion);
		interfaz.setCoordinador(controlador);
		controlador.setCiudadActual(SchemaPostgresqlDAO.getSchema());	// ELIMINAR DESPUES

		controlador.iniciar(args);
		
	}

}