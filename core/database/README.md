# Persistencia local

Room es la fuente de verdad para los datos estructurados que consumirá la interfaz.

## Esquema

- `pokemon`: datos base y marca de detalle completo.
- `pokemon_type`, `pokemon_ability` y `pokemon_stat`: relaciones del detalle.
- `favorite`: favoritos separados del catálogo.
- `catalog_sync_metadata`: progreso y resultado de la sincronización.

La actualización del catálogo usa upserts y reemplaza las relaciones de cada Pokémon dentro de una transacción. No existe una operación pública que elimine el catálogo completo, y actualizarlo no modifica la tabla de favoritos.

## Consultas

El listado se pagina exclusivamente desde Room. La búsqueda consulta todo el contenido almacenado y acepta coincidencias parciales por nombre o un número exacto con o sin `#`. Los caracteres `%` y `_` se tratan como texto, no como comodines SQL.

Los metadatos guardan por separado la cantidad esperada y la cantidad sincronizada. Tener filas en `pokemon` no implica que el catálogo esté completo.
