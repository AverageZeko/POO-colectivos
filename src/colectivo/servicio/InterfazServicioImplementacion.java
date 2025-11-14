package colectivo.servicio;

import colectivo.app.Constantes;
import colectivo.interfaz.Mostrable;
import colectivo.util.Factory;

public class InterfazServicioImplementacion implements InterfazServicio {
    Mostrable interfaz;

    public InterfazServicioImplementacion() {
        interfaz = (Mostrable) Factory.getInstancia(Constantes.GUI);
    }

    @Override
    public Mostrable buscarInterfaz() {
        return interfaz;
    }

    
}
