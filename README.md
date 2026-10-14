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
- `:core:designsystem`: tema Material 3, tokens visuales y componentes compartidos con soporte claro y oscuro.
- `:core:model`: modelos de dominio sin dependencias de Android.
- `:core:domain`: contratos de repositorio y casos de uso.

`build-logic` contiene plugins separados para aplicación Android, bibliotecas Android, Compose, Kotlin/JVM, pruebas, Retrofit y Room.

## Verificación

```bash
./gradlew :app:assembleDebug
./gradlew test
./gradlew lint
```
