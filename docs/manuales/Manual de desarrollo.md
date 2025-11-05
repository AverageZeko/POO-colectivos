# Manual de Desarrollo — POO-colectivos

Este documento está pensado para desarrolladores que vayan a compilar, ejecutar y contribuir al proyecto "POO-colectivos".

## 1. Resumen del proyecto
POO-colectivos es una aplicación Java para hacer consultas sobre los recorridos de un sistema de colectivos. Incluye:
- Interfaz gráfica basada en JavaFX (`src/colectivo/interfaz`).
- Lógica de negocio y cálculos (`src/colectivo/logica`).
- DAOs para acceso a datos (implementaciones `secuencial` y `postgresql`) (`src/colectivo/dao`).
- Un archivo `Factory` para obtener instancias de servicios/DAOs (`src/colectivo/conexion/Factory.java`).
- Servicios que encapsulan operaciones (en `src/colectivo/servicio`).

> Nota: El documento de alcance NO sustituye al manual de desarrollo. Debe usarse como referencia de requisitos.

---

## 2. Estructura del repositorio
- `src/` — código fuente Java
  - `colectivo/conexion` — `BDConexion` (conexión a BD)
  - `colectivo/controlador` — `Coordinador`, `AplicacionConsultas` (arranque y coordinación entre paquetes)
  - `colectivo/dao` — interfaces DAO y subpaquetes `postgresql`, `secuencial`
  - `colectivo/interfaz` — clases JavaFX (Interfaz, VentanaInicio, VentanaMapa, ...)
  - `colectivo/logica` — lógica de cálculo de recorridos (Clases `Calculo`, `EmpresaColectivos`, etc.)
  - `colectivo/modelo` — modelos `Linea`, `Parada`, `Tramo`, `Recorrido`
  - `colectivo/servicio` — capas de servicio que usan DAOs
  - `colectivo/test` — pruebas unitarias (JUnit de las ciudades Puerto Madryn y Azul)
  - `colectivo/util` — utilidades (Creacion de instancias, Tiempo y Localizacion)
  - `resources/*` — archivos de configuración y datos secuenciales(`*.properties`, `*_AZL.txt`)
  - `localizacion/*` — archivos relacionados a la localizacion(`Bandera del idioma`, `label_*_*.properties`)
- `lib/` — librerías jar necesarias para ejecución y pruebas (`API slf4j`, `implementacion logback`, `JavaFX y su carpeta bin`, `postgresql`)
- `doc/` — documentación del programa (`Diagramas UML`, `Documentacion de usuario`)
- `log/` — bitacoras de las consultas realizadas y los detalles de cada accion realizada

---

## 3. Requisitos de desarrollo
- JDK 11 o superior (se recomienda JDK 17+ para compatibilidad con librerías modernas).
- JavaFX SDK compatible con la versión del JDK
- PostgreSQL JDBC driver si se usa `postgresql` como fuente de datos.
- JUnit 5 (Jupiter) para pruebas unitarias.

---

## 4. Dependencias y JARs
El proyecto cuenta con todas las librerias que necesita para su ejecucion dentro de la carpeta `lib/`, con la excepcion de los JARs de JUnit
- `org.postgresql:postgresql` — driver JDBC
- `org.slf4j.Logger` — API de SLF4J
- `org.slf4j.LoggerFactory` — Implementacion de SLF4J por medio de Logback
- JavaFX SDK JARs

Si falta algún jar, se puede añadir a `lib/`

---

## 5. Compilar y ejecutar
### A) Ejecutar desde IntelliJ/Eclipse/VS Code
1. Importa el proyecto como proyecto Java (o como proyecto sin build tool).
2. Añade los jars de `lib/` al classpath del proyecto (scope: Compile/Test según corresponda).
3. Configura JavaFX (si tu IDE no lo detecta automáticamente) apuntando al JavaFX SDK.
4. El argumento de ejecucion debe ser el siguiente = --module-path "lib/gui" --add-modules javafx.swt,javafx.base,javafx.controls,javafx.fxml,javafx.media,javafx.swing,javafx.web -Djava.library.path=lib/gui/bin -Dlogback.configurationFile=file:${workspaceFolder}/log/logback.xml
5. Ejecuta la clase `colectivo.controlador.AplicacionConsultas` como aplicación JavaFX.

---

## 6. Ejecutar pruebas unitarias (JUnit 5)
- Evita mezclar JUnit 4 y JUnit 5 en las mismas clases de prueba. Usa las importaciones de `org.junit.jupiter.api.*`.
- Si ves el error `Cannot find 'org.junit.platform.commons.annotation.Testable'` asegúrate de tener `junit-platform-commons-1.11.0.jar` en el classpath.
- Si ejecutas pruebas desde un IDE, añade las dependencias JUnit 5 a la configuración del proyecto.

---

## 7. Configuración y archivos importantes
- `resources/postgresql.properties` — parámetros de conexión a la base de datos(host, puerto, bd, usuario, password).
- `resources/secuencial.properties` — configuración del dao secuencial (nombre de los archivos).
- `resources/factory.properties` — nombres de clase que `Factory` usa para crear instancias.
- Archivos de datos: `docs/*_FORMATO.txt` (formatos: `linea_FORMATO.txt`, `parada_FORMATO.txt`, etc.)

Cambio de *schema* (ciudad): algunas clases usan `SchemaServicio`  para mantener el schema actual (A traves de `SchemaPostgresqlDAO`). Para cambiar de ciudad en tiempo de ejecución usa la funcionalidad de la aplicación (Interfaz/VentanaInicio) o `Coordinador.setCiudadActual(...)`.

---

## 8. Flujo de los datos
- `Coordinador` tiene un mapa de `EmpresaColectivo`, donde cada objeto del mapa representa los datos de una ciudad. Cuando se cambia la ciudad (Ya sea desde la base de datos o secuencialmente) se revisa que los datos de la ciudad se encuentren en el mapa. En caso de que no se encuentren se crea un nuevo objeto de EmpresaColectivo que buscara los datos de la ciudad elegida
- `Factory` devuelve instancias de DAOs/Servicios a partir de `factory.properties` y las cachea en memoria hasta que se cambie de ciudad

---

## 9. Consideraciones
- Se deben eliminar los objetos asociados a lineas, tramos y paradas almacenados en el mapa concurrente utilizado en `Factory` luego de cambiar de ciudad
   `Constantes` contiene las claves utilizadas para las lineas, tramos y paradas
- JavaFX: no uses `SwingWorker`. Usa `javafx.concurrent.Task` o `Service`. En `Interfaz.manejarCalculo()` se usa `Task`, pero conviene usar un `ExecutorService` para control de hilos.
- Se debe seguir el patron de diseño MVC en todo el proyecto

---

## 10. Estándares de código y buenas prácticas
- Mantener convenciones Java (CamelCase para clases y métodos, paquetes en minúsculas).
- Documentar métodos públicos con Javadoc cuando sean parte de la API del proyecto.
- Minimizar uso de variables estáticas a no ser que sea necesario (ej.: `Coordinador` se usa como referencia estática en la UI).
- Evitar mezclar frameworks de pruebas (no mezclar JUnit 4/5 en una misma clase).

---

## 11. Cómo contribuir / flujo de trabajo
1. Compila y ejecuta tests localmente antes de realizar cambios al repositorio.
2. Agregar pruebas unitarias para cambios en lógica o DAOs cuando sea posible.
3. Documenta cambios importantes en `docs/` y actualiza `Documento de alcance` si los requisitos fueron cambiados.

---

## 12. Operaciones con base de datos
1. Crear un esquema en la base de datos y cargar los datos de la nueva ciudad.
2. Revisar `resources/postgresql.properties` y actualizar credenciales en caso necesario.
3. La aplicación usa `SchemaPostgresqlDAO` para seleccionar el `search_path`:
   - Los schemas se llaman `colectivo_XX`, donde XX es la abreviatura de la ciudad.
4. Si encuentras problemas de permisos u objetos no encontrados, verifica el `search_path` y que las tablas existan en el schema correcto.

---

## 13. Próximos pasos sugeridos (por prioridad)
1. El proyecto esta creado sin build tool, seria ideal que se recreara el proyecto utilizando alguna herramienta como Maven o Gradle para simplificar el uso de dependencias
2. Ahora mismo cambiar de ciudad en la GUI unicamente sirve para la busqueda de datos desde la base de datos. Implementar cambio de ciudad desde archivos secuenciales en la GUI.
3. La API utilizada para la visualizacion del mapa podria ser implementado con JMapViewer.



