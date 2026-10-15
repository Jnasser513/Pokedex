# Sistema de diseño

Este módulo concentra el tema Material 3, los tokens visuales y los componentes reutilizables de la aplicación.

## Referencias

- `DESIGN.md` y las tres capturas son la referencia funcional y visual.
- El proyecto de Stitch confirma la semilla de marca `#BA1A1A`, Roboto Flex y la escala de formas.
- Cuando el bloque YAML y la descripción de `DESIGN.md` asignan valores diferentes al mismo rol, prevalece la descripción: define el uso del color por componente y conserva mejor el contraste. Por ejemplo, `primary` usa `#BA1A1A` y `primaryContainer` usa `#FFDAD6`.
- Stitch solo define el esquema claro. El esquema oscuro usa los tonos inversos de Material 3 derivados de la misma paleta.

Los colores de tipos no definidos en `DESIGN.md` usan un estilo neutral. De esta manera, el sistema no inventa colores de marca; los tipos restantes podrán incorporarse cuando exista una referencia aprobada.

## Tipografía

Roboto Flex se incluye como recurso local para no depender de una descarga durante la ejecución. Todos los estilos conservan la escala, peso, interlineado y espaciado indicados en `DESIGN.md`.

El archivo proviene del [repositorio oficial de Google Fonts](https://github.com/google/fonts/tree/main/ofl/robotoflex) y conserva su licencia OFL en `src/main/res/raw/roboto_flex_license.txt`.
