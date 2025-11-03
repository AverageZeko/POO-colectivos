package colectivo.controlador;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import colectivo.configuracion.Localizacion;
import colectivo.interfaz.Mostrable;
import colectivo.logica.Calculo;
import colectivo.logica.EmpresaColectivos;
import colectivo.modelo.Parada;
import colectivo.modelo.Recorrido;
import colectivo.servicio.SchemaServicio;

/**
 * Controlador principal que coordina la interacción entre la interfaz de usuario y la lógica de negocio.
 *
 * <p>Esta clase utiliza el patron de diseño MVC para gestionar los datos de la consulta de recorridos,
 *  incluyendo las ciudades y sus datos, día de la semana y hora de llegada. 
 *  Permite realizar consultas de recorridos y mostrar los resultados.
 *  Asi como la posibilidad de cambiar de ciudad y de idioma de la interfaz</p>
 *
 */
public class Coordinador {
    private static final Logger QUERY_LOG = LoggerFactory.getLogger("Consulta");
    private Map<String, EmpresaColectivos> ciudades;
    private EmpresaColectivos ciudadActual;
    private SchemaServicio schemaServicio;
    private Mostrable interfaz;
    private Calculo calculo;
    private Localizacion localizacion;
    
    public Coordinador() {
        ciudades = new HashMap<>();
    }

    public void setSchemaServicio(SchemaServicio schema) {
        this.schemaServicio = schema;
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
        return ciudadActual.getParada(paradaId);
    }
    
    public void cambiarSchema(String nuevoSchema) {
        schemaServicio.cambiarSchema(nuevoSchema);
    }

    public void setCiudadActual(String nuevaCiudad) {
        EmpresaColectivos ciudad = ciudades.get(nuevaCiudad);
        if (ciudad == null) {
            cambiarSchema(nuevaCiudad);
            ciudad = new EmpresaColectivos();
            ciudades.put(nuevaCiudad, ciudad);
        }
        ciudadActual = ciudad;
        QUERY_LOG.info("Usuario cambia de ciudad a {}", nuevaCiudad);
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
        List<List<Recorrido>> recorridos = calculo.calcularRecorrido(
                origen, destino, diaSemana, horaLlegaParada, ciudadActual.getTramos()
        );
        interfaz.resultado(recorridos, origen, destino, horaLlegaParada);
        QUERY_LOG.info("Usuario realiza consulta desde {} hasta {}, dia de la semana {} a las {}", origen.getDireccion(), destino.getDireccion(), diaSemana, horaLlegaParada);
    }
    
    public void iniciar(String[] args) {
        QUERY_LOG.info("Usuario inicia aplicacion");
		interfaz.lanzarAplicacion(args);
    }
}