package colectivo.controlador;

import java.io.IOException;

import colectivo.logica.EmpresaColectivos;
import colectivo.interfaz.Interfaz;

public class AplicacionConsultas {
	private EmpresaColectivos empresa;
	private Coordinador controlador;
	private Interfaz interfaz;

	public static void main(String[] args) throws IOException {
		AplicacionConsultas aplicacion = new AplicacionConsultas();
		aplicacion.iniciar(args);
		
	}

	public void iniciar(String[] args) {
		empresa = EmpresaColectivos.getEmpresa();
		controlador = new Coordinador();
		controlador.setEmpresa(empresa);
		interfaz = new Interfaz();
		interfaz.launchApp(controlador, args);;
	}

	
}