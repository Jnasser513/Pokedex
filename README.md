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

`build-logic` contiene plugins separados para aplicación Android, bibliotecas Android, Compose, Kotlin/JVM, pruebas, Retrofit y Room.

## Sincronización del catálogo

La aplicación sincroniza los 151 Pokémon de Kanto al iniciar. Primero obtiene la lista de PokeAPI y después descarga los detalles con un máximo de ocho solicitudes simultáneas. El catálogo solo se escribe en Room cuando la respuesta está completa; la actualización del catálogo y sus metadatos usa una misma transacción.

Room es la fuente de verdad que observan las pantallas. Si una actualización falla y ya existen datos, se conserva el catálogo anterior y los favoritos. La paginación de la interfaz será local y no iniciará peticiones de red.

La sincronización se ejecuta en un `CoroutineScope` de aplicación. Las solicitudes simultáneas comparten el mismo trabajo para evitar descargas duplicadas. No se usa WorkManager porque no hay una tarea periódica ni un requisito de ejecución diferida garantizada.

## Verificación

```bash
./gradlew :app:assembleDebug
./gradlew test
./gradlew lint
```
