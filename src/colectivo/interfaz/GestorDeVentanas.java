package colectivo.interfaz;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import colectivo.controlador.Coordinador;
import colectivo.modelo.Parada;
import colectivo.modelo.Recorrido;
import colectivo.util.LocaleInfo;
import javafx.stage.Stage;

/**
 * Gestiona la creación y transición entre las diferentes ventanas de la aplicación.
 * Actúa como un punto central para la navegación de la UI, desacoplando al Coordinador
 * de las implementaciones concretas de las ventanas.
 */
public class GestorDeVentanas {

    private Coordinador coordinador;
    private VentanaInicio ventanaInicio;
    private Interfaz ventanaConsultas; 
    
    private static final Logger LOG = LoggerFactory.getLogger("Consulta");

    public GestorDeVentanas() {
        this.ventanaInicio = new VentanaInicio();
        this.ventanaConsultas = new Interfaz();
    }

    public void setCoordinador(Coordinador coordinador) {
        this.coordinador = coordinador;
        // Inyecta el gestor en las ventanas para que puedan comunicarse hacia arriba.
        this.ventanaInicio.setGestor(this); 
        this.ventanaConsultas.setGestor(this);
    }

    /**
     * Lanza la aplicación mostrando la ventana inicial.
     * @param args Argumentos de la línea de comandos.
     */
    public void lanzarAplicacion(String[] args) {
        ventanaInicio.lanzarAplicacion(args);
    }
    
    /**
     * Procesa la solicitud para iniciar la consulta desde la ventana de bienvenida.
     * @param localeSeleccionado La localización elegida por el usuario.
     * @param seleccionCiudad La ciudad elegida por el usuario.
     * @param ventanaActual La ventana de inicio que se debe cerrar.
     */
    public void procesarInicio(LocaleInfo localeSeleccionado, String seleccionCiudad, Stage ventanaActual) {
        if (localeSeleccionado == null || seleccionCiudad == null || seleccionCiudad.isEmpty()) {
            ResourceBundle bundle = coordinador.getBundle();
            ventanaInicio.mostrarAdvertencia(bundle.getString("Welcome_Warning"));
            return;
        }

        LOG.info("Usuario seleccionó localización='{}' y ciudad='{}'", localeSeleccionado.codigoCompleto(), seleccionCiudad);

        coordinador.setLocalizacion(localeSeleccionado);
        coordinador.setCiudadActual(seleccionCiudad);
        
        mostrarVentanaConsultas(ventanaActual);
    }
    
    /**
     * Muestra la ventana de inicio, cerrando la ventana actual si es necesario.
     * @param ventanaActual La ventana que se debe cerrar.
     */
    public void mostrarVentanaInicio(Stage ventanaActual) {
        ventanaConsultas.cerrar(ventanaActual);
        ventanaInicio.mostrar(new Stage());
    }

    /**
     * Muestra la ventana de consultas, cerrando la ventana actual si es necesario.
     * @param ventanaActual La ventana que se debe cerrar.
     */
    public void mostrarVentanaConsultas(Stage ventanaActual) {
        ventanaInicio.cerrar(ventanaActual);
        ventanaConsultas.mostrar(new Stage());
    }
    
    public void solicitarVolverAInicio(Stage ventanaActual) {
        coordinador.volverAInicio(ventanaActual);
    }
    
    // MÉTODOS DE FACHADA: Pasan las peticiones de la UI al Coordinador
    
    public List<List<Recorrido>> solicitarConsulta(Parada origen, Parada destino, int diaSemana, LocalTime hora) {
        return coordinador.consulta(origen, destino, diaSemana, hora);
    }
    
    public Map<Integer, Parada> getMapaParadas() {
        return coordinador.getMapaParadas();
    }

    public ResourceBundle getBundle() {
        return coordinador.getBundle();
    }
    
    public List<LocaleInfo> descubrirLocalizaciones() {
        return coordinador.descubrirLocalizaciones();
    }
    
    public LocaleInfo getLocale() {
        return coordinador.getLocale();
    }
    
    public void setLocalizacion(LocaleInfo localeInfo) {
        coordinador.setLocalizacion(localeInfo);
    }
}