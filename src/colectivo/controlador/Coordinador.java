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
 * Clase central que actúa como mediador entre la interfaz de usuario,
 * la lógica de negocio y los servicios de datos. Gestiona el estado de
 * la aplicación, como la ciudad actual y la localización, y coordina las
 * acciones del usuario.
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
    
    /**
     * Construye un nuevo coordinador, inicializando el mapa de ciudades.
     */
    public Coordinador() {
        ciudades = new HashMap<>();
    }

    /**
     * Establece el servicio de esquema a utilizar para cambiar entre ciudades.
     * @param schema el servicio de esquema.
     */
    public void setSchemaServicio(SchemaServicio schema) {
        this.schemaServicio = schema;
    }

    /**
     * Establece el objeto de cálculo para la lógica de búsqueda de recorridos.
     * @param calculo el objeto de cálculo.
     */
    public void setCalculo(Calculo calculo) {
    	this.calculo = calculo;
    }

    /**
     * Establece la ventana de inicio de la aplicación.
     * @param ventanaInicio la ventana de inicio.
     */
    public void setVentanaInicio(VentanaInicial ventanaInicio) {
        this.ventanaInicio = ventanaInicio;
    }

    /**
     * Establece la ventana de consultas de la aplicación.
     * @param ventanaConsultas la ventana de consultas.
     */
    public void setVentanaConsultas(VentanaConsultas ventanaConsultas) {
        this.ventanaConsultas = ventanaConsultas;
    }

    /**
     * Configura la localización (idioma y país) de la aplicación, cargando el
     * ResourceBundle correspondiente para la internacionalización.
     * @param localeInfo la información de localización a establecer.
     */
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

    /**
     * Descubre las localizaciones disponibles en la aplicación.
     * @return una lista de objetos LocaleInfo que representan las localizaciones encontradas.
     */
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

    /**
     * Devuelve el ResourceBundle cargado para la localización actual, que contiene
     * las cadenas de texto internacionalizadas.
     * @return el ResourceBundle actual.
     */
    public ResourceBundle getBundle() {
        return bundle;
    }

    /**
     * Obtiene un objeto Parada a partir de su identificador.
     * @param paradaId el ID de la parada.
     * @return el objeto Parada correspondiente o null si no se encuentra.
     */
    public Parada getParada(int paradaId) {
        if (ciudadActual == null) {
            QUERY_LOG.error("Intento de getParada({}) cuando ciudadActual es nula.", paradaId);
            return null;
        }
        return ciudadActual.getParada(paradaId);
    }
    
    /**
     * Obtiene todas las paradas de la ciudad actualmente seleccionada.
     * @return un mapa de paradas, con su ID como clave.
     */
    public Map<Integer, Parada> getMapaParadas() {
        if (ciudadActual == null) {
            QUERY_LOG.error("Intento de getParadas() cuando ciudadActual es nula.");
            return java.util.Collections.emptyMap(); 
        }
        return ciudadActual.getParadas(); 
    }

    /**
     * Cambia el esquema de la base de datos activo.
     * @param nuevoSchema el nombre del nuevo esquema a utilizar.
     */
    public void cambiarSchema(String nuevoSchema) {
        schemaServicio.cambiarSchema(nuevoSchema);
    }

    /**
     * Establece la ciudad actual para las consultas. Si la ciudad no ha sido
     * cargada previamente, la inicializa. Limpia las cachés de la Factory.
     * @param nuevaCiudad el nombre de la ciudad a establecer como actual.
     */
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
    
    /**
     * Inicia la aplicación lanzando la ventana de inicio.
     * @param args argumentos de la línea de comandos.
     */
    public void iniciarAplicacion(String[] args) {
        QUERY_LOG.info("Usuario inicia aplicacion");
        ventanaInicio.lanzarAplicacion(args);
    }

	/**
	 * Realiza la consulta de recorridos entre un origen y un destino.
	 * Este método delega el cálculo a la capa de lógica y devuelve los resultados
	 * sin interactuar directamente con la vista.
	 *
	 * @param origen la parada de origen.
	 * @param destino la parada de destino.
	 * @param diaSemana el día de la semana para la consulta.
	 * @param horaLlegaParada la hora de llegada a la parada de origen.
	 * @return una lista de posibles rutas, donde cada ruta es una lista de recorridos.
	 */
    public List<List<Recorrido>> consulta(Parada origen, Parada destino, int diaSemana, LocalTime horaLlegaParada) {
        QUERY_LOG.info("Usuario realiza consulta desde {} hasta {}, dia de la semana {} a las {}", origen.getDireccion(), destino.getDireccion(), diaSemana, horaLlegaParada);
        List<List<Recorrido>> recorridos = calculo.calcularRecorrido(
                origen, destino, diaSemana, horaLlegaParada, ciudadActual.getTramos()
        );
        return recorridos;
    }
    
    /**
     * Cierra la ventana de inicio y abre la ventana de consultas.
     * @param ventana la ventana actual (de inicio) que se debe cerrar.
     */
    public void iniciarConsulta(Stage ventana) {
		ventanaConsultas.start(new Stage());
        ventanaInicio.close(ventana);
    }

    /**
     * Cierra la ventana de consultas y vuelve a mostrar la ventana de inicio,
     * limpiando las cachés de datos.
     * @param ventanaActual la ventana de consultas que se debe cerrar.
     */
    public void volverAInicio(Stage ventanaActual) {
        ventanaConsultas.close(ventanaActual);
        Factory.clearInstancia(Constantes.TRAMO);
        Factory.clearInstancia(Constantes.LINEA);
        Factory.clearInstancia(Constantes.PARADA);
        try {
			ventanaInicio.getClass().getMethod("start", Stage.class).invoke(ventanaInicio, new Stage());
		} catch (Exception e) {
			QUERY_LOG.error("No se pudo volver a la ventana de inicio.", e);
		}
    }
}