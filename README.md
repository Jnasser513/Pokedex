# Pokédex

Aplicación Android nativa para consultar los primeros 151 Pokémon de la Pokédex Nacional, ver su detalle y administrar favoritos persistentes. La interfaz está construida completamente con Jetpack Compose y Material 3.

## Requisitos

- Android Studio compatible con Android Gradle Plugin 9.2.1.
- JDK 17 o superior. El proyecto genera bytecode para Java 17 y el CI utiliza Temurin 21.
- Android SDK 36.1.
- Emulador o dispositivo con Android 7.0 (API 24) o superior.
- Conexión a internet para la primera sincronización y para imágenes que Coil todavía no haya almacenado en caché.

## Instalación y ejecución

1. Clonar el repositorio y entrar en su directorio:

   ```bash
   git clone https://github.com/Jnasser513/Pokedex.git
   cd Pokedex
   ```

2. Abrir el proyecto desde Android Studio y esperar la sincronización de Gradle.
3. Instalar el SDK solicitado si Android Studio lo indica.
4. Seleccionar un dispositivo o emulador y ejecutar la configuración `app`.

También se puede generar el APK de depuración desde la terminal:

```bash
./gradlew :app:assembleDebug
```

El resultado queda en `app/build/outputs/apk/debug/app-debug.apk`.

## Configuración principal

- Kotlin 2.2.10.
- Gradle 9.4.1 y Android Gradle Plugin 9.2.1.
- `compileSdk` 36.1, `targetSdk` 36 y `minSdk` 24.
- Compose BOM 2026.02.01 y Material 3.
- Navigation 3, Paging 3, Room, Retrofit, Koin, Coroutines/Flow y Coil.
- Versiones centralizadas en `gradle/libs.versions.toml`.
- Convention Plugins definidos en el included build `build-logic`.

`build-logic` contiene plugins separados para aplicación Android, bibliotecas Android, Compose, Kotlin/JVM, pruebas, Retrofit y Room. Cada módulo aplica únicamente la configuración que necesita.

## Módulos y dependencias

- `:app`: arranque, composición de Koin, sincronización inicial y navegación principal.
- `:core:model`: modelos compartidos sin dependencias de Android.
- `:core:domain`: contrato del repositorio y casos de uso.
- `:core:network`: Retrofit, servicio de PokeAPI, DTOs y datasource remoto.
- `:core:database`: Room, entidades, relaciones, DAOs y `PagingSource`.
- `:core:data`: datasources, mappers y repositorio offline-first.
- `:core:designsystem`: tema claro/oscuro, tokens y componentes reutilizables.
- `:feature:pokemon-list`: listado paginado, búsqueda y favoritos.
- `:feature:pokemon-detail`: detalle local y actualización de favoritos.
- `:feature:favorites`: listado reactivo y eliminación de favoritos.

Las dependencias principales siguen esta dirección:

```text
:app ───────────────> :feature:*
  │                        │
  │                        ├──> :core:domain ──> :core:model
  │                        ├──> :core:model
  │                        └──> :core:designsystem
  │
  ├──> :core:data ─────> :core:domain
  │          ├─────────> :core:model
  │          ├─────────> :core:network
  │          └─────────> :core:database
  ├──> :core:network ──> :core:model
  └──> :core:database
```

Las features consumen contratos y casos de uso de dominio; no conocen Retrofit, Room ni las implementaciones del repositorio. `:core:domain` no depende de Android, Compose, Room o Retrofit.

## MVI y responsabilidades

Cada feature define un estado inmutable, acciones explícitas y efectos puntuales:

```text
UI ──acción──> ViewModel ──> caso de uso ──> repositorio
UI <─StateFlow─ ViewModel <──── Flow/PagingData <──── Room
UI <──efecto── Channel/Flow
```

- Los composables renderizan estado y envían acciones; no contienen reglas de negocio.
- Los ViewModels exponen `StateFlow` y la UI lo observa con `collectAsStateWithLifecycle()`.
- Los casos de uso expresan operaciones de dominio y delegan en `PokemonRepository`.
- Los efectos de una sola ejecución, como un error al cambiar un favorito, viajan por un `Channel` con entrega única y no se guardan como estado persistente.
- La consulta de búsqueda se conserva en `SavedStateHandle`. Los ViewModels son creados por Koin y sobreviven a cambios de configuración.

## Estrategia offline-first

Room es la única fuente de verdad para los datos estructurados que consume la UI:

```text
PokeAPI → datasource remoto → repositorio/sincronización → Room → Flow/PagingSource → UI
```

Al iniciar la aplicación, `CatalogSyncCoordinator` lanza una sincronización en un `CoroutineScope` de aplicación con `SupervisorJob` y `Dispatchers.Default`. El coordinador comparte el trabajo activo para impedir sincronizaciones duplicadas.

- Si Room contiene datos, se muestran inmediatamente mientras la actualización continúa.
- Si Room está vacío, la pantalla mantiene la carga inicial hasta disponer de un catálogo utilizable.
- Si la primera carga falla, se muestra un error con reintento.
- Si una actualización falla con datos locales, el contenido permanece visible y el fallo se presenta sin bloquear la pantalla.
- La cancelación se propaga y los errores de red, servidor, almacenamiento o respuesta incompleta se registran en los metadatos de sincronización.

No se usa WorkManager porque la sincronización ocurre al arrancar la aplicación y no requiere periodicidad ni ejecución garantizada cuando el proceso termina. Si el producto necesitara actualización programada o diferida, WorkManager sería la opción adecuada.

## Catálogo y consistencia

El alcance se limita a los Pokémon con ID del 1 al 151. La sincronización obtiene la lista de PokeAPI y descarga cada detalle con un máximo de ocho solicitudes simultáneas. Antes de persistir se comprueba el número de registros, IDs esperados y campos mínimos.

El catálogo y sus metadatos se actualizan en una transacción. Una respuesta parcial nunca reemplaza el catálogo válido anterior. Los favoritos viven en una tabla separada relacionada por el ID, por lo que una actualización del catálogo no los borra ni sobrescribe.

Los metadatos distinguen una sincronización iniciada, completa o fallida, además del último intento, último éxito y cantidad sincronizada. Tener filas en la tabla no se interpreta por sí solo como una carga completa.

## Paging y búsqueda

Paging 3 pagina exclusivamente desde el `PagingSource` de Room, con páginas locales de 20 elementos y `cachedIn(viewModelScope)`. El desplazamiento no genera peticiones a PokeAPI y no se utiliza `RemoteMediator`; la sincronización remota es independiente de la paginación visual.

La búsqueda consulta todo el catálogo sincronizado por nombre parcial o número exacto. La entrada se normaliza y procesa con `debounce` de 300 ms, `distinctUntilChanged` y `flatMapLatest`, de forma que una consulta nueva reemplaza la anterior.

Los estados de sincronización y Paging se manejan por separado para distinguir carga inicial, actualización, error remoto, error de página y búsqueda sin resultados.

## Detalle y favoritos

El detalle recibe el ID mediante una clave tipada y serializable de Navigation 3. Observa Room y no realiza una llamada remota al abrirse. Muestra nombre, imagen, tipos, altura, peso, habilidades y estadísticas. Evolución, género y descripción no se incluyen porque sus fuentes no forman parte del catálogo sincronizado.

Los favoritos se observan mediante `Flow`. Agregar o quitar uno actualiza de inmediato listado, detalle y pantalla de favoritos, y el estado persiste entre sesiones.

## Diseño y accesibilidad

La interfaz toma como referencia las tres pantallas de Stitch y los tokens estructurados de `DESIGN.md`. Cuando el texto descriptivo y el bloque de tokens difieren, prevalecen los valores estructurados; por ejemplo, el color primario claro es `#930009`, mientras `#BA1A1A` se utiliza como contenedor primario.

El diseño incluye tema claro y oscuro, objetivos táctiles mínimos de 48 dp, contenido adaptable a fuentes grandes, anchos máximos de lectura, claves estables en listas y animaciones discretas para cambios de estado. La navegación inferior contiene únicamente Pokédex y Favoritos; las funciones de entrenador mostradas en las referencias están fuera del alcance.

## Imágenes y funcionamiento offline

Room guarda las URLs de las imágenes, no los archivos. Coil administra su propio caché de memoria y disco. Una imagen vista anteriormente puede seguir disponible sin conexión mientras permanezca en ese caché, pero no se garantiza que una imagen nunca descargada pueda mostrarse offline. La UI presenta placeholders durante la carga y ante errores.

## Pruebas

La suite local utiliza JUnit 4, `kotlinx-coroutines-test`, Turbine, Paging Testing y MockWebServer. Cubre:

- ViewModels: estados, acciones, búsqueda, favoritos, errores y efectos.
- Repositorio: lectura desde Room abstraída por el datasource local, sincronización, fallos y conservación de favoritos.
- Datasource remoto: mapeo, respuestas incompletas y propagación de errores HTTP.
- Dominio: delegación de los casos de uso.
- Coordinador: reutilización del trabajo activo y nuevos intentos después de completarse.
- Consultas de búsqueda, tokens visuales y contraste de colores.

Comandos de verificación:

```bash
./gradlew test
./gradlew lint
./gradlew :app:assembleDebug
```

La entrega no incluye pruebas instrumentadas ni de extremo a extremo. Las consultas SQL y migraciones futuras deberían validarse con Room real en un dispositivo o emulador cuando ese alcance sea requerido; los tests unitarios actuales no se presentan como prueba de persistencia física.

## Integración continua

GitHub Actions ejecuta tests, lint y compilación del APK en cada pull request hacia `develop` o `main`, y en cada push a `develop`. Los reportes de fallo se conservan como artefactos durante siete días.

## Decisiones y pendientes

- Se priorizó un catálogo completo y consistente de 151 Pokémon frente a una sincronización ilimitada.
- Se eligió un coordinador de aplicación en lugar de WorkManager porque no existe un requisito periódico o diferido.
- Paging es local para mantener a Room como única fuente de verdad y evitar que el scroll controle la red.
- Se limitaron las pantallas y campos a información respaldada por PokeAPI y persistida localmente; no se inventan CP, género, evolución o descripciones.
- Las imágenes dependen del caché de Coil y no tienen garantía offline completa.
- Quedan fuera del alcance pruebas UI/instrumentadas, actualización periódica, sincronización incremental y catálogos posteriores al ID 151.

## Flujo de ramas y commits

`develop` es la rama de integración. Cada etapa se desarrolla desde un `develop` actualizado en una rama `feature/<nombre-descriptivo>`, se verifica y se integra mediante pull request después de la revisión.

No se encadenan features pendientes ni se trabaja directamente sobre `develop`. Los commits siguen Conventional Commits con los prefijos `feat:`, `fix:`, `test:`, `build:`, `docs:` y `refactor:`. El repositorio no requiere force push para el flujo normal.
