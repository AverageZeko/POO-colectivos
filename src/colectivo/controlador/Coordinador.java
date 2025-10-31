package colectivo.controlador;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import colectivo.interfaz.Mostrable;
import colectivo.logica.Calculo;
import colectivo.logica.EmpresaColectivos;
import colectivo.modelo.Parada;
import colectivo.modelo.Recorrido;
import colectivo.servicio.SchemaServicio;

/**
 * Controlador principal que coordina la interacción entre la interfaz de usuario y la lógica de negocio.
 *
 * <p>Esta clase utiliza el patron de diseño MVC para gestionar los datos de la consulta de recorridos, incluyendo la empresa, paradas de origen y destino,
 * día de la semana y hora de llegada. Permite realizar consultas de recorridos y mostrar los resultados
 * utilizando la interfaz definida.</p>
 *
 */
public class Coordinador {
    //private Map<String, EmpresaColectivos> ciudades;
    private EmpresaColectivos empresa;
    private SchemaServicio schemaServicio;
    private Mostrable interfaz;
    private Calculo calculo;
    
    public Coordinador() {
        //ciudades = new HashMap<>();
    }

    public EmpresaColectivos getEmpresa() {
        return empresa;
    }

    public void setEmpresa(EmpresaColectivos empresa) {
        this.empresa = empresa;
    }

    public void cambiarCiudad(String nuevaCiudad) {
        
    }

/*     public void setCiudadActual(String ciudad) {
        if (ciudades.containsKey(ciudad)) {
            empresa = ciudades.get(ciudad);
        }   else {
            //ciudades.put(ciudad, new EmpresaColectivos());
        }
    } */

    public void setSchemaServicio(SchemaServicio schema) {
        this.schemaServicio = schema;
    }

    public void cambiarSchema(String nuevoSchema) {
        schemaServicio.cambiarSchema(nuevoSchema);
    }

    public void setInterfaz(Mostrable interfaz) {
        this.interfaz = interfaz;
    }

    public void setCalculo(Calculo calculo) {
    	this.calculo = calculo;
    }

    public Parada getParada(int paradaId) {
        return empresa.getParada(paradaId);
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