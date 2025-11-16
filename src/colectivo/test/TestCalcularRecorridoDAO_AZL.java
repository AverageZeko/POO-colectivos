package colectivo.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import colectivo.configuracion.ConfigGlobal;
import colectivo.dao.LineaDAO;
import colectivo.dao.ParadaDAO;
import colectivo.dao.TramoDAO;
import colectivo.logica.Calculo;
import colectivo.modelo.Linea;
import colectivo.modelo.Parada;
import colectivo.logica.Recorrido;
import colectivo.modelo.Tramo;
import colectivo.util.Factory;

class TestCalcularRecorridoDAO_AZL {

	private Map<Integer, Parada> paradas;
	private Map<String, Linea> lineas;
	private Map<String, Tramo> tramos;

	private int diaSemana;
	private LocalTime horaLlegaParada;
	
	private Calculo calculo;	
	private ConfigGlobal configuracion;

	@BeforeEach
	void setUp() throws Exception {
		configuracion = ConfigGlobal.getConfiguracion();
		configuracion.cambiarCiudad("Azul");

		paradas = ((ParadaDAO) Factory.getInstancia("PARADA")).buscarTodos();
		tramos = ((TramoDAO) Factory.getInstancia("TRAMO")).buscarTodos();
		lineas = ((LineaDAO) Factory.getInstancia("LINEA")).buscarTodos();

		diaSemana = 1; // Lunes
		horaLlegaParada = LocalTime.of(10, 35);

		calculo = new Calculo();
	}

	@Test
	void testSinColectivo() {
		Parada paradaOrigen = paradas.get(1);
		Parada paradaDestino = paradas.get(205);

		List<List<Recorrido>> recorridos = calculo.calcularRecorrido(paradaOrigen, paradaDestino, diaSemana,
				horaLlegaParada, tramos);

		assertTrue(recorridos.isEmpty(), "No debería encontrarse un recorrido entre paradas tan lejanas sin múltiples conexiones.");
	}

	@Test
	void testDirecto1() {
		Parada paradaOrigen = paradas.get(35);
		Parada paradaDestino = paradas.get(51);

		List<List<Recorrido>> recorridos = calculo.calcularRecorrido(paradaOrigen, paradaDestino, diaSemana,
				horaLlegaParada, tramos);

		assertFalse(recorridos.isEmpty(), "Debería haber al menos una ruta directa.");
		assertEquals(1, recorridos.size(), "Debería haber exactamente una opción de ruta directa para este caso simple.");
		assertEquals(1, recorridos.get(0).size(), "La ruta directa debe tener un solo segmento.");

		Recorrido recorridoDirecto = recorridos.get(0).get(0);

		assertEquals(lineas.get("L501AI"), recorridoDirecto.getLinea(), "La línea debe ser 501 A Ida.");
		
		List<Parada> paradasEsperadas = new ArrayList<>();
		paradasEsperadas.add(paradas.get(35)); paradasEsperadas.add(paradas.get(36));
		paradasEsperadas.add(paradas.get(37)); paradasEsperadas.add(paradas.get(38));
		paradasEsperadas.add(paradas.get(39)); paradasEsperadas.add(paradas.get(40));
		paradasEsperadas.add(paradas.get(41)); paradasEsperadas.add(paradas.get(42));
		paradasEsperadas.add(paradas.get(43)); paradasEsperadas.add(paradas.get(44));
		paradasEsperadas.add(paradas.get(45)); paradasEsperadas.add(paradas.get(46));
		paradasEsperadas.add(paradas.get(47)); paradasEsperadas.add(paradas.get(48));
		paradasEsperadas.add(paradas.get(49)); paradasEsperadas.add(paradas.get(50));
		paradasEsperadas.add(paradas.get(51));
		assertIterableEquals(paradasEsperadas, recorridoDirecto.getParadas(), "La secuencia de paradas no es la esperada.");
		assertEquals(LocalTime.of(10, 35), recorridoDirecto.getHoraSalida(), "La hora de salida no coincide.");
		assertEquals(720, recorridoDirecto.getDuracion(), "La duración del viaje no es correcta.");
	}

	@Test
	void testDirecto2() {
		Parada paradaOrigen = paradas.get(1);
		Parada paradaDestino = paradas.get(20);

		List<List<Recorrido>> recorridos = calculo.calcularRecorrido(paradaOrigen, paradaDestino, diaSemana,
				horaLlegaParada, tramos);

		assertFalse(recorridos.isEmpty(), "Debería haber al menos una ruta directa.");
		assertEquals(1, recorridos.size(), "Debería haber exactamente una opción de ruta directa para este caso simple.");
		assertEquals(1, recorridos.get(0).size(), "La ruta directa debe tener un solo segmento.");

		Recorrido recorridoDirecto = recorridos.get(0).get(0);

		assertEquals(lineas.get("L503I"), recorridoDirecto.getLinea(), "La línea debe ser 503 Ida.");
		
		List<Parada> paradasEsperadas = new ArrayList<>();
		paradasEsperadas.add(paradas.get(1)); paradasEsperadas.add(paradas.get(2));
		paradasEsperadas.add(paradas.get(3)); paradasEsperadas.add(paradas.get(4));
		paradasEsperadas.add(paradas.get(5)); paradasEsperadas.add(paradas.get(6));
		paradasEsperadas.add(paradas.get(7)); paradasEsperadas.add(paradas.get(8));
		paradasEsperadas.add(paradas.get(9)); paradasEsperadas.add(paradas.get(10));
		paradasEsperadas.add(paradas.get(11)); paradasEsperadas.add(paradas.get(12));
		paradasEsperadas.add(paradas.get(13)); paradasEsperadas.add(paradas.get(14));
		paradasEsperadas.add(paradas.get(15)); paradasEsperadas.add(paradas.get(16));
		paradasEsperadas.add(paradas.get(17)); paradasEsperadas.add(paradas.get(18));
		paradasEsperadas.add(paradas.get(19)); paradasEsperadas.add(paradas.get(20));
		assertIterableEquals(paradasEsperadas, recorridoDirecto.getParadas(), "La secuencia de paradas no es la esperada.");
		assertEquals(LocalTime.of(10, 45), recorridoDirecto.getHoraSalida(), "La hora de salida no coincide.");
		assertEquals(1050, recorridoDirecto.getDuracion(), "La duración del viaje no es correcta.");
	}

	@Test
	void testConexion() {
		// Viaje con transbordo: De L503I a L501BI en la parada 10
		Parada paradaOrigen = paradas.get(1);
		Parada paradaDestino = paradas.get(77);

		List<List<Recorrido>> recorridos = calculo.calcularRecorrido(paradaOrigen, paradaDestino, diaSemana,
				horaLlegaParada, tramos);
		
		assertFalse(recorridos.isEmpty(), "No se encontraron recorridos de conexión.");
		
		// Buscamos la ruta específica que haga L503I -> L501BI
		List<Recorrido> rutaEncontrada = null;
		for(List<Recorrido> ruta : recorridos) {
			if (ruta.size() == 2 && ruta.get(0).getLinea().equals(lineas.get("L503I")) && ruta.get(1).getLinea().equals(lineas.get("L501BI"))) {
				rutaEncontrada = ruta;
				break;
			}
		}

		assertNotNull(rutaEncontrada, "No se encontró la ruta de conexión esperada L503I -> L501BI.");

		Recorrido tramo1 = rutaEncontrada.get(0);
		Recorrido tramo2 = rutaEncontrada.get(1);

		// Tramo 1: L503I
		assertEquals(lineas.get("L503I"), tramo1.getLinea());
		List<Parada> paradasTramo1 = new ArrayList<>();
		paradasTramo1.add(paradas.get(1)); paradasTramo1.add(paradas.get(2));
		paradasTramo1.add(paradas.get(3)); paradasTramo1.add(paradas.get(4));
		paradasTramo1.add(paradas.get(5)); paradasTramo1.add(paradas.get(6));
		paradasTramo1.add(paradas.get(7)); paradasTramo1.add(paradas.get(8));
		paradasTramo1.add(paradas.get(9)); paradasTramo1.add(paradas.get(10)); // Punto de trasbordo
		assertIterableEquals(paradasTramo1, tramo1.getParadas());
		assertEquals(LocalTime.of(10, 45), tramo1.getHoraSalida());
		assertEquals(630, tramo1.getDuracion());

		// Tramo 2: L501BI
		assertEquals(lineas.get("L501BI"), tramo2.getLinea());
		List<Parada> paradasTramo2 = new ArrayList<>();
		paradasTramo2.add(paradas.get(10)); paradasTramo2.add(paradas.get(11));
		paradasTramo2.add(paradas.get(12)); paradasTramo2.add(paradas.get(210));
		paradasTramo2.add(paradas.get(211)); paradasTramo2.add(paradas.get(212));
		paradasTramo2.add(paradas.get(213)); paradasTramo2.add(paradas.get(214));
		paradasTramo2.add(paradas.get(215)); paradasTramo2.add(paradas.get(51));
		paradasTramo2.add(paradas.get(106)); paradasTramo2.add(paradas.get(35));
		paradasTramo2.add(paradas.get(53)); paradasTramo2.add(paradas.get(54));
		paradasTramo2.add(paradas.get(55)); paradasTramo2.add(paradas.get(56));
		paradasTramo2.add(paradas.get(218)); paradasTramo2.add(paradas.get(219));
		paradasTramo2.add(paradas.get(220)); paradasTramo2.add(paradas.get(67));
		paradasTramo2.add(paradas.get(68)); paradasTramo2.add(paradas.get(221));
		paradasTramo2.add(paradas.get(72)); paradasTramo2.add(paradas.get(222));
		paradasTramo2.add(paradas.get(223)); paradasTramo2.add(paradas.get(77)); // Destino final
		assertIterableEquals(paradasTramo2, tramo2.getParadas());
		assertEquals(LocalTime.of(11, 10, 30), tramo2.getHoraSalida());
		assertEquals(1980, tramo2.getDuracion());
	}

	@Test
	void testConexionCaminando() {
		// Ruta con caminata: De L503I a L501BR
		Parada paradaOrigen = paradas.get(1);
		Parada paradaDestino = paradas.get(232);

		List<List<Recorrido>> recorridos = calculo.calcularRecorrido(paradaOrigen, paradaDestino, diaSemana,
				horaLlegaParada, tramos);
		
		assertFalse(recorridos.isEmpty(), "Debería existir una ruta con caminata.");

		List<Recorrido> rutaConCaminata = null;
		for (List<Recorrido> ruta : recorridos) {
			if (ruta.size() == 3 &&
				ruta.get(0).getLinea().equals(lineas.get("L503I")) &&
				ruta.get(1).getLinea() == null &&
				ruta.get(2).getLinea().equals(lineas.get("L501BR"))) {
				rutaConCaminata = ruta;
				break;
			}
		}
		
		assertNotNull(rutaConCaminata, "No se encontró la ruta esperada con caminata (L503I -> Caminata -> L501BR).");
		assertEquals(3, rutaConCaminata.size(), "La ruta debería tener 3 segmentos (bus, caminata, bus).");		

		Recorrido recorrido1 = rutaConCaminata.get(0);
		Recorrido recorrido2_caminando = rutaConCaminata.get(1);
		Recorrido recorrido3 = rutaConCaminata.get(2);
		
		// Recorrido 1 (L503I)
		assertEquals(lineas.get("L503I"), recorrido1.getLinea());
		List<Parada> paradasTramo1 = new ArrayList<>();
		paradasTramo1.add(paradas.get(1)); paradasTramo1.add(paradas.get(2));
		paradasTramo1.add(paradas.get(3)); paradasTramo1.add(paradas.get(4));
		paradasTramo1.add(paradas.get(5)); paradasTramo1.add(paradas.get(6));
		paradasTramo1.add(paradas.get(7)); paradasTramo1.add(paradas.get(8));
		paradasTramo1.add(paradas.get(9)); paradasTramo1.add(paradas.get(10));
		paradasTramo1.add(paradas.get(11)); paradasTramo1.add(paradas.get(12));
		paradasTramo1.add(paradas.get(13)); // Punto de bajada para caminar
		assertIterableEquals(paradasTramo1, recorrido1.getParadas());
		assertEquals(LocalTime.of(10, 45), recorrido1.getHoraSalida());
		assertEquals(780, recorrido1.getDuracion());
		
		// Recorrido 2 (Caminando)
		assertNull(recorrido2_caminando.getLinea(), "El segundo tramo debe ser caminando (línea nula).");
		List<Parada> paradasCaminando = new ArrayList<>();
		paradasCaminando.add(paradas.get(13)); // Desde
		paradasCaminando.add(paradas.get(236)); // Hasta
		assertIterableEquals(paradasCaminando, recorrido2_caminando.getParadas());
		assertEquals(LocalTime.of(10, 58), recorrido2_caminando.getHoraSalida());
		assertEquals(300, recorrido2_caminando.getDuracion(), "La caminata debería durar 300 segundos.");

		// Recorrido 3 (L501BR)
		assertEquals(lineas.get("L501BR"), recorrido3.getLinea());
		List<Parada> paradasTramo3 = new ArrayList<>();
		paradasTramo3.add(paradas.get(236)); paradasTramo3.add(paradas.get(189));
		paradasTramo3.add(paradas.get(190)); paradasTramo3.add(paradas.get(191));
		paradasTramo3.add(paradas.get(237)); paradasTramo3.add(paradas.get(238));
		paradasTramo3.add(paradas.get(111)); paradasTramo3.add(paradas.get(110));
		paradasTramo3.add(paradas.get(234)); paradasTramo3.add(paradas.get(204));
		paradasTramo3.add(paradas.get(232)); // Destino final
		assertIterableEquals(paradasTramo3, recorrido3.getParadas());
		assertEquals(LocalTime.of(11, 03, 15), recorrido3.getHoraSalida());
		assertEquals(855, recorrido3.getDuracion());
	}
}