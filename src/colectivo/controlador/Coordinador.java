package colectivo.controlador;

import java.time.LocalTime;
import java.util.List;

import colectivo.configuracion.Localizacion;
import colectivo.interfaz.Mostrable;
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
    private Mostrable interfaz;
    private Calculo calculo;
    private Localizacion localizacion;
    
    public Coordinador() {

    }

    public EmpresaColectivos getEmpresa() {
        return empresa;
    }

    public void setEmpresa(EmpresaColectivos empresa) {
        this.empresa = empresa;
    }

    public void setInterfaz(Mostrable interfaz) {
        this.interfaz = interfaz;
    }

    public void setCalculo(Calculo calculo) {
    	this.calculo = calculo;
    }

    public void setLocalizacion(Localizacion localizacion) {
        this.localizacion = localizacion;
    }

    public Parada getParada(int paradaId) {
        return empresa.getParada(paradaId);
    }
    
    public void setIdioma(String idioma) {
        localizacion.setIdioma(idioma);
    }

    public String getPalabra(String llave) {
        return localizacion.getPalabra(llave);
    }

    public String getRutaFoto() {
        return localizacion.getRutaFoto();
    }

    public void consulta(Parada origen, Parada destino, int diaSemana, LocalTime horaLlegaParada) {
        // ahora usamos la instancia de Calculo
        List<List<Recorrido>> recorridos = calculo.calcularRecorrido(
                origen, destino, diaSemana, horaLlegaParada, empresa.getTramos()
        );
        interfaz.resultado(recorridos, origen, destino, horaLlegaParada);
    }
    
    public void iniciar(String[] args) {
		interfaz.lanzarAplicacion(args);
    }
    


}