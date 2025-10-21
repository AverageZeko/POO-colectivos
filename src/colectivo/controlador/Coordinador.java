package colectivo.controlador;

import java.time.LocalTime;
import java.util.List;

import colectivo.interfaz.Interfaz;
import colectivo.logica.Calculo;
import colectivo.logica.EmpresaColectivos;
import colectivo.modelo.Parada;
import colectivo.modelo.Recorrido;

public class Coordinador {
    private EmpresaColectivos empresa;



    public Coordinador() {

    }

    public EmpresaColectivos getEmpresa() {
        return empresa;
    }

    public void setEmpresa(EmpresaColectivos empresa) {
        this.empresa = empresa;
    }

    public void consulta(Parada origen,Parada  destino, int diaSemana, LocalTime horaLlegaParada) {


        // Realizar c�lculo
		List<List<Recorrido>> recorridos = Calculo.calcularRecorrido(origen, destino, diaSemana, horaLlegaParada, empresa.getTramos());

		// Mostrar resultado
		Interfaz.resultado(recorridos, origen, destino, horaLlegaParada);		
    }


}
