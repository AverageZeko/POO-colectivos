package colectivo.interfaz.celdas;

import colectivo.modelo.Parada;
import javafx.scene.control.ListCell;

/**
 * Celda personalizada para mostrar el nombre de una {@link Parada} en un ComboBox.
 * Muestra la dirección de la parada.
 */
public class ParadasComboBox extends ListCell<Parada> {
    @Override
    protected void updateItem(Parada item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setText(null);
        } else {
            setText(item.getDireccion());
        }
    }
}