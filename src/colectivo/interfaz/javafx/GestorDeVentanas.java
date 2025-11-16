package colectivo.interfaz.javafx;

import java.time.LocalTime;
import java.util.Map;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import colectivo.controlador.Coordinador;
import colectivo.interfaz.Mostrable;
import colectivo.modelo.Parada;
import colectivo.util.LocaleInfo;
import javafx.stage.Stage;

/**
 * GestorDeVentanas: Es la única clase de la capa de interfaz que comunica con el Coordinador.
 * Se asume que el Coordinador devolverá la representación textual ya formateada (List<List<String>>)
 * y por lo tanto la interfaz recibe y renderiza solamente strings.
 */
public class GestorDeVentanas implements Mostrable{

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

    public void mostrar(String[] args){
        ventanaInicio.lanzarAplicacion(args);
    }

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

    public void mostrarVentanaInicio(Stage ventanaActual) {
        ventanaConsultas.cerrar(ventanaActual);
        ventanaInicio.mostrar(new Stage());
    }

    public void mostrarVentanaConsultas(Stage ventanaActual) {
        ventanaInicio.cerrar(ventanaActual);
        ventanaConsultas.start(new Stage());
    }

    public void solicitarVolverAInicio(Stage ventanaActual) {
        coordinador.limpiarCacheCiudades();
        mostrarVentanaInicio(ventanaActual);
    }

    // MÉTODOS DE FACHADA: ahora devuelven la representación textual (List<List<String>>) preparada por el Coordinador

    public java.util.List<java.util.List<String>> solicitarConsulta(Parada origen, Parada destino, int diaSemana, LocalTime hora) {
        // Se delega a Coordinador, y se espera que Coordinador retorne List<List<String>> ya preparado
        return coordinador.consulta(origen, destino, diaSemana, hora);
    }

    public Map<Integer, Parada> getMapaParadas() {
        return coordinador.getMapaParadas();
    }

    public ResourceBundle getBundle() {
        return coordinador.getBundle();
    }

    public java.util.List<LocaleInfo> descubrirLocalizaciones() {
        return coordinador.descubrirLocalizaciones();
    }

    public LocaleInfo getLocale() {
        return coordinador.getLocale();
    }

    public void setLocalizacion(LocaleInfo localeInfo) {
        coordinador.setLocalizacion(localeInfo);
    }

    public java.util.List<String> getCiudades() {
        return coordinador.getCiudades();
    }
}