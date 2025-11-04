package colectivo.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import colectivo.dao.LineaDAO;
import colectivo.dao.ParadaDAO;
import colectivo.dao.TramoDAO;
import colectivo.logica.Calculo;
import colectivo.modelo.Linea;
import colectivo.modelo.Parada;
import colectivo.modelo.Recorrido;
import colectivo.modelo.Tramo;
import colectivo.servicio.SchemaServicio;
import colectivo.util.Factory;

class TestCalcularRecorridoDAO_AZL {

	private Map<Integer, Parada> paradas;
	private Map<String, Linea> lineas;
	private Map<String, Tramo> tramos;

	private int diaSemana;
	private LocalTime horaLlegaParada;
	
	private Calculo calculo;	
	private SchemaServicio schemaServicio;

	@BeforeEach
	void setUp() throws Exception {
		// --- [INICIO] CORRECCIÓN ---
		// Obtener la instancia del servicio a través del Factory, no con 'new'
		schemaServicio = (SchemaServicio) Factory.getInstancia("SCHEMASERVICIO");
		// --- [FIN] CORRECCIÓN ---
		
		// Asegurarse de que estamos usando la base de datos de Azul
		schemaServicio.cambiarSchema("colectivo_AZL");

		// Cargar datos de la ciudad de Azul
		paradas = ((ParadaDAO) Factory.getInstancia("PARADA")).buscarTodos();
		tramos = ((TramoDAO) Factory.getInstancia("TRAMO")).buscarTodos();
		lineas = ((LineaDAO) Factory.getInstancia("LINEA")).buscarTodos();

		diaSemana = 1; // Lunes
		horaLlegaParada = LocalTime.of(10, 35);

		calculo = new Calculo();
	}

	@Test
	void testSinColectivo() {
		// Testear una ruta заведомо imposible en Azul
		Parada paradaOrigen = paradas.get(1); // Pellegrini 2100
		Parada paradaDestino = paradas.get(205); // Bidegain 2466 (extremo opuesto)

		List<List<Recorrido>> recorridos = calculo.calcularRecorrido(paradaOrigen, paradaDestino, diaSemana,
				horaLlegaParada, tramos);

		assertTrue(recorridos.isEmpty(), "No debería encontrarse un recorrido entre paradas tan lejanas sin múltiples conexiones.");
	}

	@Test
	void testDirecto() {
		// Un viaje directo en la línea 501 A Ida
		Parada paradaOrigen = paradas.get(35); // Hipólito Yrigoyen X Av. Canerva
		Parada paradaDestino = paradas.get(51); // Monseñor Caneva 766

		List<List<Recorrido>> recorridos = calculo.calcularRecorrido(paradaOrigen, paradaDestino, diaSemana,
				horaLlegaParada, tramos);

		assertFalse(recorridos.isEmpty(), "Debería haber al menos una ruta directa.");
		assertEquals(1, recorridos.size(), "Debería haber exactamente una opción de ruta directa para este caso simple.");
		assertEquals(1, recorridos.get(0).size(), "La ruta directa debe tener un solo segmento.");

		Recorrido recorridoDirecto = recorridos.get(0).get(0);

		assertEquals(lineas.get("L501AI"), recorridoDirecto.getLinea(), "La línea debe ser 501 A Ida.");
		
		List<Parada> paradasEsperadas = new ArrayList<>();
		paradasEsperadas.add(paradas.get(35)); // Origen
		paradasEsperadas.add(paradas.get(36));
		paradasEsperadas.add(paradas.get(37));
		paradasEsperadas.add(paradas.get(38));
		paradasEsperadas.add(paradas.get(39));
		paradasEsperadas.add(paradas.get(40));
		paradasEsperadas.add(paradas.get(41));
		paradasEsperadas.add(paradas.get(42));
		paradasEsperadas.add(paradas.get(43));
		paradasEsperadas.add(paradas.get(44));
		paradasEsperadas.add(paradas.get(45));
		paradasEsperadas.add(paradas.get(46));
		paradasEsperadas.add(paradas.get(47));
		paradasEsperadas.add(paradas.get(48));
		paradasEsperadas.add(paradas.get(49));
		paradasEsperadas.add(paradas.get(50));
		paradasEsperadas.add(paradas.get(51)); // Destino

		assertIterableEquals(paradasEsperadas, recorridoDirecto.getParadas(), "La secuencia de paradas no es la esperada.");
		
		// Asumiendo una frecuencia de 15 minutos para el test
		assertEquals(LocalTime.of(10, 45), recorridoDirecto.getHoraSalida(), "La hora de salida no coincide.");
		// Suma de duraciones: 90+60+30+60+30+30+30+30+30+30+30+60+60+30+30+90 = 750 segundos
		assertEquals(750, recorridoDirecto.getDuracion(), "La duración del viaje no es correcta.");
	}

	@Test
	void testConexion() {
		// Viaje con trasbordo: De L503R a L501AR
		Parada paradaOrigen = paradas.get(1); // Origen en L503
		Parada paradaDestino = paradas.get(77); // Destino en L501A

		List<List<Recorrido>> recorridos = calculo.calcularRecorrido(paradaOrigen, paradaDestino, diaSemana,
				horaLlegaParada, tramos);
		
		assertFalse(recorridos.isEmpty());
		
		// Buscamos una ruta específica que haga L503R -> L501AR
		List<Recorrido> rutaEncontrada = null;
		for(List<Recorrido> ruta : recorridos) {
			if (ruta.size() == 2 && ruta.get(0).getLinea().equals(lineas.get("L503R")) && ruta.get(1).getLinea().equals(lineas.get("L501AR"))) {
				rutaEncontrada = ruta;
				break;
			}
		}

		Recorrido tramo1 = rutaEncontrada.get(0);
		Recorrido tramo2 = rutaEncontrada.get(1);

		// Tramo 1: L503R
		assertEquals(lineas.get("L503R"), tramo1.getLinea());
		List<Parada> paradasTramo1 = new ArrayList<>();
		paradasTramo1.add(paradas.get(1));
		paradasTramo1.add(paradas.get(34));
		paradasTramo1.add(paradas.get(33));
		paradasTramo1.add(paradas.get(32));
		paradasTramo1.add(paradas.get(31));
		paradasTramo1.add(paradas.get(30));
		paradasTramo1.add(paradas.get(29));
		paradasTramo1.add(paradas.get(28));
		paradasTramo1.add(paradas.get(27));
		paradasTramo1.add(paradas.get(26));
		paradasTramo1.add(paradas.get(25));
		paradasTramo1.add(paradas.get(24));
		paradasTramo1.add(paradas.get(23));
		paradasTramo1.add(paradas.get(22));
		paradasTramo1.add(paradas.get(21)); // Punto de trasbordo
		
		assertIterableEquals(paradasTramo1, tramo1.getParadas());
		assertEquals(LocalTime.of(10, 45), tramo1.getHoraSalida());
		assertEquals(510, tramo1.getDuracion());

		// Tramo 2: L501AR
		assertEquals(lineas.get("L501AR"), tramo2.getLinea());
		List<Parada> paradasTramo2 = new ArrayList<>();
		paradasTramo2.add(paradas.get(21)); // Punto de trasbordo
		paradasTramo2.add(paradas.get(102));
		paradasTramo2.add(paradas.get(101));
		paradasTramo2.add(paradas.get(100));
		paradasTramo2.add(paradas.get(99));
		paradasTramo2.add(paradas.get(98));
		paradasTramo2.add(paradas.get(97));
		paradasTramo2.add(paradas.get(96));
		paradasTramo2.add(paradas.get(95));
		paradasTramo2.add(paradas.get(94));
		paradasTramo2.add(paradas.get(93));
		paradasTramo2.add(paradas.get(92));
		paradasTramo2.add(paradas.get(91));
		paradasTramo2.add(paradas.get(90));
		paradasTramo2.add(paradas.get(89));
		paradasTramo2.add(paradas.get(88));
		paradasTramo2.add(paradas.get(87));
		paradasTramo2.add(paradas.get(86));
		paradasTramo2.add(paradas.get(85));
		paradasTramo2.add(paradas.get(84));
		paradasTramo2.add(paradas.get(83));
		paradasTramo2.add(paradas.get(82));
		paradasTramo2.add(paradas.get(81));
		paradasTramo2.add(paradas.get(80));
		paradasTramo2.add(paradas.get(79));
		paradasTramo2.add(paradas.get(78));
		paradasTramo2.add(paradas.get(77)); // Destino final
		
		assertIterableEquals(paradasTramo2, tramo2.getParadas());
		assertEquals(LocalTime.of(11, 0), tramo2.getHoraSalida());
		assertEquals(1170, tramo2.getDuracion());
	}

	@Test
	void testConexionCaminando() {
		// Ruta que fuerza una caminata entre L503R y L501AI
		Parada paradaOrigen = paradas.get(1); // Comienzo en L503
		Parada paradaDestino = paradas.get(55); // Destino en L501A

		List<List<Recorrido>> recorridos = calculo.calcularRecorrido(paradaOrigen, paradaDestino, diaSemana,
				horaLlegaParada, tramos);
		
		assertFalse(recorridos.isEmpty(), "Debería existir una ruta con caminata.");

		List<Recorrido> rutaConCaminata = null;
		for (List<Recorrido> ruta : recorridos) {
			if (ruta.size() > 1) { // Buscar una ruta con más de un tramo
				boolean tieneCaminata = false;
				for (Recorrido tramo : ruta) {
					if (tramo.getLinea() == null) {
						tieneCaminata = true;
						break;
					}
				}
				if (tieneCaminata) {
					rutaConCaminata = ruta;
					break;
				}
			}
		}
		
		assertEquals(3, rutaConCaminata.size(), "La ruta debería tener 3 segmentos (bus, caminata, bus).");		

		Recorrido recorrido1 = rutaConCaminata.get(0);
		Recorrido recorrido2_caminando = rutaConCaminata.get(1);
		Recorrido recorrido3 = rutaConCaminata.get(2);
		
		// Recorrido 1 (L503R)
		assertEquals(lineas.get("L503R"), recorrido1.getLinea());
		assertEquals(paradas.get(1), recorrido1.getParadas().get(0));
		assertEquals(paradas.get(24), recorrido1.getParadas().get(recorrido1.getParadas().size() - 1)); // Bajada en Colón 745
		assertEquals(LocalTime.of(10, 45), recorrido1.getHoraSalida());
		assertEquals(390, recorrido1.getDuracion());
		
		// Recorrido 2 (Caminando)
		assertNull(recorrido2_caminando.getLinea(), "El segundo tramo debe ser caminando (línea nula).");
		List<Parada> paradasCaminando = new ArrayList<>();
		paradasCaminando.add(paradas.get(24)); // Desde Colón 745
		paradasCaminando.add(paradas.get(128)); // Hasta J.A. Roca 756
		assertIterableEquals(paradasCaminando, recorrido2_caminando.getParadas());
		assertEquals(LocalTime.of(10, 51, 30), recorrido2_caminando.getHoraSalida());
		assertEquals(120, recorrido2_caminando.getDuracion(), "La caminata debería durar 120 segundos.");

		// Recorrido 3 (L501AI)
		assertEquals(lineas.get("L501AI"), recorrido3.getLinea());
		assertEquals(paradas.get(48), recorrido3.getParadas().get(0)); // Sube en J.A. Roca 1301 (la más cercana tras caminar)
		assertEquals(paradas.get(55), recorrido3.getParadas().get(recorrido3.getParadas().size() - 1)); // Destino final
		assertEquals(LocalTime.of(10, 55), recorrido3.getHoraSalida());
		assertEquals(480, recorrido3.getDuracion());
	}
}