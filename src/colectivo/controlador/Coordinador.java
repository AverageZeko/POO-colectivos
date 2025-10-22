package colectivo.controlador;

import java.time.LocalTime;
import java.util.List;

import colectivo.interfaz.Interfaz;
import colectivo.logica.Calculo;
import colectivo.logica.EmpresaColectivos;
import colectivo.modelo.Parada;
import colectivo.modelo.Recorrido;

/**
 * Controlador principal que coordina la interacción entre la interfaz de usuario y la lógica de negocio.
 *
 * <p>Esta clase utiliza el patron de diseño MVC para gestionar los datos de la consulta de recorridos, incluyendo la empresa, paradas de origen y destino,
 * día de la semana y hora de llegada. Permite realizar consultas de recorridos y mostrar los resultados
 * utilizando la interfaz definida.</p>
 *
 */
public class Coordinador {
    private EmpresaColectivos empresa;
    private Parada origen;
    private Parada destino;
    private int diaSemana;
    private LocalTime horaLlegaParada;

    public Coordinador() {

    }

    public EmpresaColectivos getEmpresa() {
        return empresa;
    }

    public void setEmpresa(EmpresaColectivos empresa) {
        this.empresa = empresa;
    }

    public void setParadaOrigen(Parada parada) {
        if (parada == null) {
            return;
        }
        origen = parada;
    }

    public void setParadaDestino(Parada parada) {
        if (parada == null) {
            return;
        }
        destino = parada;
    }

    public void setDiaSemana(int dia) {
        diaSemana = dia;
    }

    public void setHora(LocalTime hora) {
        if (hora == null) {
            return;
        }
        horaLlegaParada = hora;
    }

    /**
     * Realiza la consulta de recorridos entre las paradas configuradas, en el día y hora indicados.
     *
     * <p>Solicita los datos necesarios a la interfaz, realiza el cálculo de recorridos y muestra el resultado
     * al usuario.</p>
     */
    public void consulta() {
    	origen = Interfaz.ingresarParadaOrigen(empresa.getParadas());
		destino = Interfaz.ingresarParadaDestino(empresa.getParadas());
		diaSemana = Interfaz.ingresarDiaSemana();
		horaLlegaParada = Interfaz.ingresarHoraLlegaParada();

        // Realizar c�lculo
		List<List<Recorrido>> recorridos = Calculo.calcularRecorrido(origen, destino, diaSemana, horaLlegaParada, empresa.getTramos());

		// Mostrar resultado
		Interfaz.resultado(recorridos, origen, destino, horaLlegaParada);		
    }


}
