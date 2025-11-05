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

// ... (resto de la clase sin cambios)
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
    
    public Map<Integer, Parada> getParadas() {
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

	/**
	 * [MODIFICADO] Realiza la consulta y DEVUELVE los recorridos encontrados.
	 * Ya no interactúa directamente con la ventana de consultas.
	 *
	 * @param origen
	 * @param destino
	 * @param diaSemana
	 * @param horaLlegaParada
	 * @return La lista de posibles rutas.
	 */
    public List<List<Recorrido>> consulta(Parada origen, Parada destino, int diaSemana, LocalTime horaLlegaParada) {
        QUERY_LOG.info("Usuario realiza consulta desde {} hasta {}, dia de la semana {} a las {}", origen.getDireccion(), destino.getDireccion(), diaSemana, horaLlegaParada);
        List<List<Recorrido>> recorridos = calculo.calcularRecorrido(
                origen, destino, diaSemana, horaLlegaParada, ciudadActual.getTramos()
        );
        // Ya no llama a la ventana, simplemente devuelve el resultado.
        return recorridos;
    }
    

    public void iniciarConsulta(Stage ventana) {
		ventanaConsultas.start(new Stage());
        ventanaInicio.close(ventana);
    }

    public void volverAInicio(Stage ventanaActual) {
        ventanaConsultas.close(ventanaActual);
        try {
			ventanaInicio.getClass().getMethod("start", Stage.class).invoke(ventanaInicio, new Stage());
		} catch (Exception e) {
			QUERY_LOG.error("No se pudo volver a la ventana de inicio.", e);
		}
    }
}