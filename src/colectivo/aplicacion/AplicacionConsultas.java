package colectivo.aplicacion;

import java.io.IOException;
import java.time.LocalTime;
import java.util.List;

import colectivo.logica.EmpresaColectivos;
import colectivo.interfaz.Interfaz;
import colectivo.logica.Calculo;
import colectivo.modelo.Parada;
import colectivo.modelo.Recorrido;

public class AplicacionConsultas {
	private EmpresaColectivos empresa;

	public static void main(String[] args) throws IOException {
		AplicacionConsultas aplicacion = new AplicacionConsultas();
		aplicacion.iniciar();
		aplicacion.consulta();
	}

	public void iniciar() {
		empresa = EmpresaColectivos.getEmpresa();
	}

	public void consulta() {
		// Ingreso datos usuario
		Parada paradaOrigen = Interfaz.ingresarParadaOrigen(empresa.getParadas());
		Parada paradaDestino = Interfaz.ingresarParadaDestino(empresa.getParadas());
		int diaSemana = Interfaz.ingresarDiaSemana();
		LocalTime horaLlegaParada = Interfaz.ingresarHoraLlegaParada();

		// Realizar c�lculo
		List<List<Recorrido>> recorridos = Calculo.calcularRecorrido(paradaOrigen, paradaDestino, diaSemana, horaLlegaParada, empresa.getTramos());

		// Mostrar resultado
		Interfaz.resultado(recorridos, paradaOrigen, paradaDestino, horaLlegaParada);		
	}
}
