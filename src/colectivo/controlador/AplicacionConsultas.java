package colectivo.controlador;

import java.io.IOException;

import colectivo.logica.EmpresaColectivos;

public class AplicacionConsultas {
	private EmpresaColectivos empresa;
	private Coordinador controlador;

	public static void main(String[] args) throws IOException {
		AplicacionConsultas aplicacion = new AplicacionConsultas();
		aplicacion.iniciar();
		aplicacion.consulta();
	}

	public void iniciar() {
		empresa = EmpresaColectivos.getEmpresa();
		controlador = new Coordinador();
		controlador.setEmpresa(empresa);
	}

	public void consulta() {
		controlador.consulta();
	}
}
