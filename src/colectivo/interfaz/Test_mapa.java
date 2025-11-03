package colectivo.interfaz;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.net.URL;

public class Test_mapa extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Visor de Mapa en JavaFX");

        final WebView webView = new WebView();
        final WebEngine webEngine = webView.getEngine();

        // Obtenemos la URL del archivo HTML
        // IMPORTANTE: Este archivo debe estar en tu carpeta de 'resources'.
        // Si usas Eclipse sin Maven, ponlo en la raíz de tu 'src' y Eclipse lo copiará.
        URL url = getClass().getResource("/cargar_mapa.html");
        
        if (url == null) {
            System.err.println("No se pudo encontrar cargar_mapa.html. Asegúrate de que esté en la carpeta 'resources' (o 'src').");
            webEngine.loadContent("<h1>Error: No se pudo cargar el mapa.</h1>");
        } else {
            webEngine.load(url.toExternalForm());
        }

        VBox vBox = new VBox(webView);
        Scene scene = new Scene(vBox, 960, 600);

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}