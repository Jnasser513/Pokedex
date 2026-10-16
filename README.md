# Pokédex

Aplicación Android nativa construida con Kotlin y Jetpack Compose.

## Requisitos

- JDK 17.
- Android Studio compatible con Android Gradle Plugin 9.2.1.
- Android SDK 36.1.

## Configuración

- Kotlin 2.2.10.
- Gradle 9.4.1 y Android Gradle Plugin 9.2.1.
- `compileSdk` 36.1, `targetSdk` 36 y `minSdk` 24.
- Dependencias centralizadas en `gradle/libs.versions.toml`.
- Convention Plugins definidos en el included build `build-logic`.

## Módulos

- `:app`: punto de entrada de la aplicación.
- `:core:data`: repositorio offline-first, datasource local, mappers y coordinación de datos.
- `:core:database`: persistencia Room, consultas locales, paginación y favoritos.
- `:core:designsystem`: tema Material 3, tokens visuales y componentes compartidos con soporte claro y oscuro.
- `:core:model`: modelos de dominio sin dependencias de Android.
- `:core:domain`: contratos de repositorio y casos de uso.
- `:core:network`: cliente Retrofit, DTOs y datasource de PokeAPI.
- `:feature:favorites`: favoritos reactivos desde Room, estado MVI y eliminación persistente.
- `:feature:pokemon-detail`: detalle observado desde Room, estado MVI y actualización de favoritos.
- `:feature:pokemon-list`: listado paginado, estado MVI y componentes de la pantalla principal.

`build-logic` contiene plugins separados para aplicación Android, bibliotecas Android, Compose, Kotlin/JVM, pruebas, Retrofit y Room.

## Diseño y accesibilidad

La interfaz toma como referencia las tres pantallas del proyecto de Stitch y los tokens estructurados de `DESIGN.md`. Cuando el texto descriptivo y el bloque de tokens difieren, prevalecen los valores estructurados; por ejemplo, el color primario claro es `#930009`, mientras `#BA1A1A` se conserva como contenedor primario. El tema oscuro utiliza la paleta tonal equivalente con contraste comprobado.

La navegación principal usa una barra inferior con los dos destinos incluidos en el alcance: Pokédex y Favoritos. El catálogo admite más columnas según el ancho disponible y el contenido de detalle y favoritos limita su ancho para mantener una lectura cómoda. Con fuentes grandes, el grid reduce columnas y las métricas y estadísticas cambian a una disposición vertical para evitar cortes. Las acciones mantienen objetivos táctiles mínimos de 48 dp y las animaciones se reservan para cambios de estado, favoritos y movimientos de elementos.

## Sincronización del catálogo

La aplicación sincroniza los primeros 151 Pokémon de la Pokédex Nacional al iniciar. Primero obtiene la lista de PokeAPI y después descarga los detalles con un máximo de ocho solicitudes simultáneas. El catálogo solo se escribe en Room cuando la respuesta está completa; la actualización del catálogo y sus metadatos usa una misma transacción.

Room es la fuente de verdad que observan las pantallas. Si una actualización falla y ya existen datos, se conserva el catálogo anterior y los favoritos. El listado pagina desde Room y el desplazamiento no inicia peticiones de red.

La búsqueda consulta todo el catálogo sincronizado en Room por nombre parcial o por número exacto. La consulta se conserva mediante `SavedStateHandle`, se aplica con un debounce de 300 ms y reemplaza el flujo de Paging anterior con `flatMapLatest`.

El detalle recibe el número del Pokémon mediante una clave tipada y serializable de Navigation 3, y observa sus datos desde Room. El back stack se conserva ante cambios de configuración y recreación del proceso. Abrir el detalle no realiza peticiones remotas. Muestra únicamente los campos sincronizados: nombre, imagen, tipos, altura, peso, habilidades y estadísticas. Evolución, género y descripción no forman parte de esta etapa porque sus fuentes no están persistidas.

Los favoritos se almacenan en una tabla separada relacionada por el número del Pokémon. La pantalla de favoritos observa Room mediante `Flow`, por lo que agregar o quitar un favorito actualiza de inmediato el listado, el detalle y la sección de favoritos. La navegación inferior incluye únicamente Pokédex y Favoritos; las funciones de entrenador que aparecen en la referencia visual quedan fuera del alcance.

La pantalla principal separa el estado de sincronización del estado de Paging. Una carga inicial fallida ocupa la pantalla cuando Room está vacío; si ya hay datos, estos permanecen visibles aunque la actualización remota falle.

La sincronización se ejecuta en un `CoroutineScope` de aplicación. Las solicitudes simultáneas comparten el mismo trabajo para evitar descargas duplicadas. No se usa WorkManager porque no hay una tarea periódica ni un requisito de ejecución diferida garantizada.

## Verificación

```bash
./gradlew :app:assembleDebug
./gradlew test
./gradlew lint
```
