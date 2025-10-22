package colectivo.controlador;

import java.io.IOException;

import colectivo.logica.EmpresaColectivos;

/**
 * Punto de entrada principal para la aplicación de consultas de recorridos de colectivos.
 *
 * <p>Esta clase inicializa la empresa y el coordinador, y gestiona el ciclo de vida
 * de la consulta de recorridos a través de la interfaz de usuario.</p>
 *
 */
public class AplicacionConsultas {
	private EmpresaColectivos empresa;
	private Coordinador controlador;

	public static void main(String[] args) throws IOException {
		AplicacionConsultas aplicacion = new AplicacionConsultas();
		aplicacion.iniciar();
		aplicacion.consulta();
	}

	/**
	 * Inicializa la empresa y el coordinador, y vincula ambos para la consulta.
	 */
	public void iniciar() {
		empresa = EmpresaColectivos.getEmpresa();
		controlador = new Coordinador();
		controlador.setEmpresa(empresa);
	}
	
	/**
	 * Ejecuta la consulta de recorridos a través del coordinador.
	 */
	public void consulta() {
		controlador.consulta();
	}
}
