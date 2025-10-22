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
    private Interfaz interfaz;

    public Coordinador() {

    }

    public EmpresaColectivos getEmpresa() {
        return empresa;
    }

    public void setEmpresa(EmpresaColectivos empresa) {
        this.empresa = empresa;
    }

    public void setInterfaz(Interfaz interfaz) {
        this.interfaz = interfaz;
    }

    public void consulta(Parada origen,Parada  destino, int diaSemana, LocalTime horaLlegaParada) {

		List<List<Recorrido>> recorridos = Calculo.calcularRecorrido(origen, destino, diaSemana, horaLlegaParada, empresa.getTramos());

		interfaz.resultado(recorridos, origen, destino, horaLlegaParada);		
    }

    public void iniciar(String[] args) {
        interfaz.lanzarAplicacion(args);
    }

}