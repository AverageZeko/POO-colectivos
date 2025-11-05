# Manual de Usuario — POO-colectivos

Versión: Borrador

Este manual explica a un usuario final cómo instalar, iniciar y usar la aplicación POO-colectivos para realizar consultas al sistema de colectivos de diferentes ciudades

## Contenido
- 1. Requisitos mínimos
- 2. Instalación rápida
- 3. Iniciar la aplicación
- 4. Ventana principal y elementos de la interfaz
- 5. Cómo calcular recorridos (paso a paso)
- 6. Visualizar resultados y navegación por páginas
- 7. Visualizar mapa de un recorrido
- 8. Cambio de ciudad / esquema
- 9. Localización (idiomas)
- 10. Importar/Exportar datos (formatos)
- 11. Errores frecuentes y soluciones rápidas
- 12. Preguntas frecuentes (FAQ)
- 13. Contacto y soporte
- 14. Apéndice: ubicación de archivos importantes

---

## 1. Requisitos mínimos
- Java Runtime Environment (JRE) 11 o superior instalado.
- JavaFX runtime (compatible con la versión de JRE).
- Servidor PostgreSQL accesible y credenciales válidas (host, puerto, bd, usuario, password).

Si no usa la base de datos y prefiere archivos planos, revise la configuración `resources/secuencial.properties`.

---

## 2. Instalación rápida
1. Clone o descargue el repositorio.
2. Coloque las librerías necesarias en `lib/` (si fuera necesario): driver PostgreSQL, JARs JavaFX si hace falta, SLF4J, Logback-classic, Logback-core.
3. Asegúrate de que `resources/postgresql.properties` tenga los datos correctos si vas a usar la BD.

---

## 3. Iniciar la aplicación
### Desde un IDE
- Importa el proyecto y configura el classpath para incluir `lib/*`.
- Configura argumentos de ejecucion con lo siguiente = --module-path "lib/gui" --add-modules javafx.swt,javafx.base,javafx.controls,javafx.fxml,javafx.media,javafx.swing,javafx.web -Djava.library.path=lib/gui/bin -Dlogback.configurationFile=file:${workspaceFolder}/log/logback.xml
- Ejecuta la clase `colectivo.controlador.AplicacionConsultas`.

---

## 4. Ventana principal y elementos de la interfaz
Se tienen tres ventanas: La ventana inicial, la ventana de consultas y la ventana del mapa.
La ventana inicial presenta:
- Una lista con el nombre de las ciudades listas para realizar consultas
- Banderas que pueden ser seleccionadas para elegir el idioma de la aplicacion.
- Boton para pasar a la ventana de consultas.

La ventana principal presenta dos paneles: izquierdo (Entrada de datos) y derecho (resultados).

Entrada de datos (panel izquierdo):
- Lista de origen: Lista desplegable con las paradas de la ciudad elegida para determinar el inicio del recorrido.
- Lista de destino: Lista desplegable con las paradas de la ciudad elegida para deteminar el final del recorrido.
- Hora y minuto: Listas para elegir la hora y minutos donde se llego a la parada inicial
- Día de la semana: Botones para elegir el dia de la semana
- Botón "Calcular": inicia el cálculo de rutas.
- Botón "Volver": regresa a la pantalla inicial.

Panel derecho:
- Muestra los recorridos encontrados en formato texto.
- Controles para cambiar la pagina de los resultados (Anterior / Siguiente)
- Boton para visualizar el mapa.

La ventana del mapa presenta
- Un mapa de la ciudad elegida
- Un camino a seguir para alcanzar el destino
- Muestra referencias que indican que representa cada segmento del camino
- Provee botones para alejar/acercar la vista y para mover el mapa

Mensaje de advertencia:
- Si faltan campos obligatorios (origen/destino/hora/día), verá un texto en rojo indicando lo que falta.

---

## 5. Cómo calcular recorridos (paso a paso)
1. Seleccione la parada de origen en la lista de la parada origen.
2. Seleccione la parada de destino en la lista de la parada destino.
3. Seleccione la hora (hora y minutos) en la primera y segunda lista, respectivamente.
4. Seleccione el día de la semana.
5. Haga clic en `Calcular`.

Nota: la aplicación puede tardar unos segundos en calcular. No cierre la ventana mientras se calcula.

---

## 6. Visualizar resultados y navegación por páginas
- Si la búsqueda devuelve rutas, se muestran en el panel derecho.
- Use `Anterior` y `Siguiente` para navegar entre las diferentes opciones de recorrido.
- La etiqueta de página muestra el número actual y el total de páginas.

---

## 7. Visualizar mapa de un recorrido
- Si hay un recorrido seleccionado, haga clic en `Mapa` para abrir una ventana con la trazada del recorrido.
- Si el mapa no se abre, revise mensajes de error en la UI o la consola para ver faltantes de recursos.

---

## 8. Cambio de ciudad / esquema
- La aplicación puede cambiar la fuente de datos (schema) para cargar datos de distintas ciudades.
- Normalmente esto se hace desde la pantalla inicial (Si se usa una base de datos) o configuración (Si se usan archivos de texto)
- Si al cambiar de ciudad los datos no se actualizan, cierre y abra la aplicación. Si persiste el problema, contacte al desarrollador (ver sección Contacto).

---

## 9. Localización (idiomas)
- Archivos de etiquetas se encuentran en `resources/localizacion/` (ej.: `label_es_ARG.properties`).
- Para añadir un idioma nuevo, cree un nuevo archivo de propiedades con las claves necesarias y los valores traducidos. Junto con una imagen que represente al idioma.

---

## 10. Importar/Exportar datos (formatos)
- El proyecto incluye formatos ejemplo en `docs/`:
  - `linea_FORMATO.txt` — formato esperado para archivos de líneas.
  - `parada_FORMATO.txt` — formato para paradas.
  - `tramo_FORMATO.txt` — formato para tramos.
  - `frecuencia_FORMATO.txt` — formato para frecuencias/horarios.

Siga los formatos mostrados para cargar datos desde archivos de texto.

---

## 11. Errores frecuentes y soluciones rápidas
- Pantalla en blanco o UI no responde: asegúrese de que JavaFX esté correctamente configurado y que el JDK sea compatible.
- Si el mapa no muestra los recorridos, es que el programa se quedo sin consultas gratuitas en la API usada para la visualizacion del mapa. Contactar los desarrolladores en ese caso.
- Problemas de conexión a la base de datos: verifique `resources/postgresql.properties` y que el servidor PostgreSQL esté corriendo y sea accesible desde su máquina.
- Datos no actualizados al cambiar de ciudad/schema: cierre la app y reabra; si persiste, consulte al desarrollador.

---

## 12. Posibles preguntas
Q: ¿Puedo ejecutar la aplicación sin PostgreSQL?
- R: Sí, si configuras la aplicación en modo secuencial (archivos planos) usando `resources/secuencial.properties` y los ficheros de datos correctos.

Q: ¿Cómo cambio el idioma de la interfaz?
- R: Seleccione el idioma en la ventana inicial; la aplicación cargará el `ResourceBundle` correspondiente.

Q: ¿Esta este documento hecho con IA?
- R: La estructura del manual fue hecha con IA. Sin embargo, sus contenidos fueron revisados y editados por los desarrolladores para reflejar el proyecto.

---

## 13. Contacto y soporte
- Para consultas técnicas, bugs o sugerencias, abra un issue en el repositorio o contacte a los responsables del proyecto.

---

## 14. Apéndice: ubicación de archivos importantes
- Código fuente: `src/`
- Recursos y configuración: `resources/` (properties, formatos, localización)
- Librerías: `lib/`
- Documentos de referencia: `docs/`

