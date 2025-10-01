package colectivo.logica;

import java.time.LocalTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import colectivo.modelo.Linea;
import colectivo.modelo.Parada;
import colectivo.modelo.Recorrido;
import colectivo.modelo.Tramo;


public class Calculo {

	public static List<List<Recorrido>> calcularRecorrido(Parada paradaOrigen, Parada paradaDestino, int diaSemana,
			LocalTime horaLlegaParada, Map<String, Tramo> tramos) {
		
		List<Linea> PLineas = paradaOrigen.getLineas();
		List<List<Recorrido>> returningList;
		List<Recorrido> RecorridoLista;
		//calcular todas las lines posibles, cuanto tardan y retornar la lista
		for (int i = 0; i < PLineas.size(); i++)
		{
			Linea lineaActual = PLineas.get(i);
			List<Parada> paradasActuales =lineaActual.getParadas();
			if(!paradasActuales.contains(paradaDestino)) continue;
			
			List<Parada> recorridoParadas;
			int recorridoDuracion = 0;
			
			int index = paradasActuales.indexOf(paradaOrigen);
			
			for (int j = index; j < paradasActuales.indexOf(paradaDestino); j++){
				
				Parada actual = paradasActuales.get(j);
				recorridoParadas.add(actual);
				recorridoParadas.add();
				RecorridoDuracion +=;
			}
			recorridoParadas.add(paradaDestino);
			Recorrido recorridoActual = new Recorrido(lineaActual, recorridoParadas, horaLlegaParada, recorridoDuracion);
		}

		return returningList;
	}

}
