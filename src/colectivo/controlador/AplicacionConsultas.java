package colectivo.controlador;

import colectivo.logica.Calculo;
import colectivo.logica.EmpresaColectivos;
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
	//Sacar esto?
	private EmpresaColectivos empresa;
	//Sacar esto?
	private Coordinador controlador;
	private Mostrable interfaz;
	private Calculo calculo;

	public static void main(String[] args) {
		AplicacionConsultas aplicacion = new AplicacionConsultas();
		aplicacion.iniciar(args);
	}

	/**
	 * Inicializa el modelo, la vista y el controlador e inicia el programa
	 */
	public void iniciar(String[] args) {
		empresa = EmpresaColectivos.getEmpresa();
		controlador = new Coordinador();
		interfaz = new Interfaz();
		calculo = new Calculo();

		//Sacar esto?
		controlador.setEmpresa(empresa);
		//Sacar esto?
		
		controlador.setInterfaz(interfaz);
		controlador.setCalculo(calculo);
		interfaz.setCoordinador(controlador);
		controlador.iniciar(args);


	}

}