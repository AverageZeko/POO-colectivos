package colectivo.controlador;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import colectivo.app.Constantes;
import colectivo.configuracion.ConfigGlobal;
import colectivo.interfaz.GestorDeVentanas;
import colectivo.interfaz.Mostrable;
import colectivo.logica.Calculo;
import colectivo.logica.EmpresaColectivos;
import colectivo.modelo.Parada;
import colectivo.modelo.Recorrido;
import colectivo.servicio.InterfazServicio;
import colectivo.servicio.InterfazServicioImplementacion;
import colectivo.util.Factory;
import colectivo.util.LocaleInfo;
import javafx.stage.Stage;

/**
 * Clase central que actúa como mediador entre la interfaz de usuario,
 * la lógica de negocio y los servicios de datos. Gestiona el estado de
 * la aplicación, como la ciudad actual y la localización, y coordina las
 * acciones del usuario.
 */
public class Coordinador {
    private static final Logger QUERY_LOG = LoggerFactory.getLogger("Consulta");
    private ConfigGlobal config;
    private Map<String, EmpresaColectivos> ciudades;
    private EmpresaColectivos ciudadActual;
    private GestorDeVentanas gestorInterfaz;
    private Mostrable interfaz;
    private Calculo calculo;

    /**
     * Construye un nuevo coordinador, inicializando el mapa de ciudades.
     */
    public Coordinador() {
        ciudades = new HashMap<>();
        config = ConfigGlobal.getConfiguracion();
		calculo = new Calculo();
		this.setCalculo(calculo);

        InterfazServicio guiService = new InterfazServicioImplementacion();
        interfaz = guiService.buscarInterfaz();
        interfaz.setCoordinador(this);
        
        gestorInterfaz = new GestorDeVentanas();
        this.inicializarInterfaz(gestorInterfaz);
    }


    public void inicializarInterfaz(GestorDeVentanas gestor) {
        this.setGestorDeVentanas(gestor);
        gestor.setCoordinador(this);
    }
    public void setConfiguracion(ConfigGlobal config) {
        this.config = config;
    }

    /**
     * Establece el objeto de cálculo para la lógica de búsqueda de recorridos.
     * @param calculo el objeto de cálculo.
     */
    public void setCalculo(Calculo calculo) {
    	this.calculo = calculo;
    }

    /**
     * Establece el gestor de ventanas de la aplicación.
     * @param gestorDeVentanas el gestor de ventanas.
     */
    public void setGestorDeVentanas(GestorDeVentanas gestorDeVentanas) {
        this.gestorInterfaz = gestorDeVentanas;
    }

    /**
     * Configura la localización (idioma y país) de la aplicación, cargando el
     * ResourceBundle correspondiente para la internacionalización.
     * @param localeInfo la información de localización a establecer.
     */
    public void setLocalizacion(LocaleInfo localeInfo) {
        config.setLocalizacion(localeInfo);
    }

    /**
     * Descubre las localizaciones disponibles en la aplicación.
     * @return una lista de objetos LocaleInfo que representan las localizaciones encontradas.
     */
    public List<LocaleInfo> descubrirLocalizaciones() {
        return config.descubrirLocalizaciones();
    }
    
    /**
     * Devuelve el LocaleInfo actualmente configurado.
     * @return El objeto LocaleInfo actual.
     */
    public LocaleInfo getLocale() {
        return config.getLocale();
    }

    /**
     * Devuelve el ResourceBundle cargado para la localización actual, que contiene
     * las cadenas de texto internacionalizadas.
     * @return el ResourceBundle actual.
     */
    public ResourceBundle getBundle() {
        return config.getBundle();
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
        config.cambiarSchema(nuevoSchema);
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
        gestorInterfaz.lanzarAplicacion(args);
    }
/////////////////////////////////////////////////////////////////////////////////////////////////////
    public void iniciar(String[] args) {
        interfaz.start(args);
    }
/////////////////////////////////////////////////////////////////////////////////////////////////////
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
     * Cierra la ventana de consultas y vuelve a mostrar la ventana de inicio,
     * limpiando las cachés de datos.
     * @param ventanaActual la ventana de consultas que se debe cerrar.
     */
    public void volverAInicio(Stage ventanaActual) {
        Factory.clearInstancia(Constantes.TRAMO);
        Factory.clearInstancia(Constantes.LINEA);
        Factory.clearInstancia(Constantes.PARADA);
        gestorInterfaz.mostrarVentanaInicio(ventanaActual);
    }
}