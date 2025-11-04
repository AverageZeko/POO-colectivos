package colectivo.controlador;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import colectivo.interfaz.VentanaConsultas;
import colectivo.interfaz.VentanaInicial;
import colectivo.logica.Calculo;
import colectivo.logica.EmpresaColectivos;
import colectivo.modelo.Parada;
import colectivo.modelo.Recorrido;
import colectivo.servicio.SchemaServicio;
import colectivo.util.Factory;
import colectivo.util.LocaleInfo;
import colectivo.util.LocalizacionUtil;
import javafx.stage.Stage;

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
    private VentanaInicial ventanaInicio;
    private VentanaConsultas ventanaConsultas;
    private Calculo calculo;
    private LocaleInfo localeActual; // Guardar el LocaleInfo actual
    private ResourceBundle bundle;
    
    public Coordinador() {
        ciudades = new HashMap<>();
    }

    public void setSchemaServicio(SchemaServicio schema) {
        this.schemaServicio = schema;
    }

    public void setCalculo(Calculo calculo) {
    	this.calculo = calculo;
    }

    public void setVentanaInicio(VentanaInicial ventanaInicio) {
        this.ventanaInicio = ventanaInicio;
    }

    public void setVentanaConsultas(VentanaConsultas ventanaConsultas) {
        this.ventanaConsultas = ventanaConsultas;
    }

    
    public void setLocalizacion(LocaleInfo localeInfo) {
        if (localeInfo == null) {
            QUERY_LOG.warn("setLocalizacion fue llamado con un valor nulo.");
            return;
        }
        
        this.localeActual = localeInfo; // Guardar la referencia
        try {
            String nombreBase = localeInfo.getNombreBaseBundle();
            this.bundle = ResourceBundle.getBundle(nombreBase);
            QUERY_LOG.info("Localización seleccionada: {}. ResourceBundle '{}' cargado con éxito.", localeInfo.codigoCompleto(), nombreBase);
        } catch (MissingResourceException e) {
            QUERY_LOG.error("No se pudo cargar el ResourceBundle para '{}'. Verifica que el archivo .properties exista.", localeInfo.getNombreBaseBundle(), e);
            this.bundle = null;
        }
    }

    public List<LocaleInfo> descubrirLocalizaciones() {
        return LocalizacionUtil.descubrirLocalizaciones();
    }
    
    /**
     * Devuelve el LocaleInfo actualmente configurado.
     * @return El objeto LocaleInfo actual.
     */
    public LocaleInfo getLocale() {
        return localeActual;
    }

    public ResourceBundle getBundle() {
        return bundle;
    }

    public Parada getParada(int paradaId) {
        if (ciudadActual == null) {
            QUERY_LOG.error("Intento de getParada({}) cuando ciudadActual es nula.", paradaId);
            return null;
        }
        return ciudadActual.getParada(paradaId);
    }
    
    public Map<Integer, Parada> getMapaParadas() {
        if (ciudadActual == null) {
            QUERY_LOG.error("Intento de getParadas() cuando ciudadActual es nula.");
            return java.util.Collections.emptyMap(); 
        }
        return ciudadActual.getParadas(); 
    }

    public void cambiarSchema(String nuevoSchema) {
        schemaServicio.cambiarSchema(nuevoSchema);
    }

    public void setCiudadActual(String nuevaCiudad) {
        cambiarSchema(nuevaCiudad);
        EmpresaColectivos ciudad = ciudades.get(nuevaCiudad);
        if (ciudad == null) {
            ciudad = new EmpresaColectivos();
            ciudades.put(nuevaCiudad, ciudad);
        }
        ciudadActual = ciudad;

        Factory.clearInstancia(Constantes.TRAMO);
        Factory.clearInstancia(Constantes.LINEA);
        Factory.clearInstancia(Constantes.PARADA);

        QUERY_LOG.info("Usuario cambia de ciudad a {}", nuevaCiudad);
    }
    
    public void iniciarAplicacion(String[] args) {
        QUERY_LOG.info("Usuario inicia aplicacion");
        ventanaInicio.lanzarAplicacion(args);
    }

    public void consulta(Parada origen, Parada destino, int diaSemana, LocalTime horaLlegaParada) {
        List<List<Recorrido>> recorridos = calculo.calcularRecorrido(
                origen, destino, diaSemana, horaLlegaParada, ciudadActual.getTramos()
        );
        ventanaConsultas.resultado(recorridos, origen, destino, horaLlegaParada);
        QUERY_LOG.info("Usuario realiza consulta desde {} hasta {}, dia de la semana {} a las {}", origen.getDireccion(), destino.getDireccion(), diaSemana, horaLlegaParada);
    }
    

    public void iniciarConsulta(Stage ventana) {
		ventanaConsultas.start(new Stage());
        ventanaInicio.close(ventana);
    }

    /**
     * Cierra la ventana de consulta actual y vuelve a mostrar la ventana de inicio.
     * @param ventanaActual La ventana (Stage) de la Interfaz que se debe cerrar.
     */
    public void volverAInicio(Stage ventanaActual) {
        // Cierra la ventana de la interfaz de consulta
        ventanaConsultas.close(ventanaActual);
        
        // Vuelve a lanzar la ventana de inicio
        // La implementación de VentanaInicio es una Application, por lo que se puede
        // llamar a su método start() para volver a crearla.
        try {
			ventanaInicio.getClass().getMethod("start", Stage.class).invoke(ventanaInicio, new Stage());
		} catch (Exception e) {
			QUERY_LOG.error("No se pudo volver a la ventana de inicio.", e);
		}
    }
}