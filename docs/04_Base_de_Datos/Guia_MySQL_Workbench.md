# Guía Paso a Paso — MySQL Workbench
### Creación de la Base de Datos y Extracción del Diagrama ER

---

## PARTE 1: Crear la Base de Datos en MySQL Workbench

---

### Paso 1 — Abrir MySQL Workbench y conectarse al servidor

1. Abre **MySQL Workbench** desde el menú de inicio o escritorio.
2. En la pantalla principal verás la sección **"MySQL Connections"**.
3. Haz doble clic en la conexión **"Local instance MySQL80"** (o la que tengas configurada).
4. Si te pide contraseña, ingresa la del usuario `root` que configuraste al instalar MySQL.
5. Una vez conectado, verás la interfaz principal con el editor de consultas.

> **¿No tienes una conexión creada?**
> Haz clic en el ícono `+` junto a "MySQL Connections" y completa:
> - Connection Name: `Local MySQL`
> - Hostname: `127.0.0.1`
> - Port: `3306`
> - Username: `root`
> Luego clic en **Test Connection** y después en **OK**.

---

### Paso 2 — Abrir el archivo SQL del proyecto

**Opción A — Desde el explorador de archivos:**
1. En el menú superior, ve a **File → Open SQL Script...**
2. Navega hasta la carpeta del proyecto:
   ```
   C:\Users\USUARIO\Documents\Creaciones-Edimile\docs\04_Base_de_Datos\
   ```
3. Selecciona el archivo `creaciones_edimile.sql` y haz clic en **Abrir**.
4. El script se abrirá en una nueva pestaña del editor.

**Opción B — Copiar y pegar:**
1. Haz clic en el ícono de nueva pestaña SQL (ícono de hoja con rayo ⚡) o presiona `Ctrl + T`.
2. Abre el archivo `creaciones_edimile.sql` con el Bloc de notas.
3. Selecciona todo (`Ctrl + A`), copia (`Ctrl + C`) y pega en el editor de Workbench (`Ctrl + V`).

---

### Paso 3 — Ejecutar el script completo

1. Asegúrate de estar en la pestaña con el script `creaciones_edimile.sql`.
2. Para ejecutar **todo el script**, haz clic en el botón del **rayo doble** (⚡⚡) en la barra de herramientas, o presiona `Ctrl + Shift + Enter`.

   > **Atención:** NO uses el rayo simple (⚡) que solo ejecuta la sentencia donde está el cursor.

3. En la parte inferior de la pantalla verás la pestaña **"Output"** con los resultados:
   - Un ✅ verde significa que esa sentencia se ejecutó correctamente.
   - Un ❌ rojo indica un error — lee el mensaje para identificar el problema.

4. Si todo sale bien, al final verás el mensaje:
   ```
   BASE DE DATOS CREADA EXITOSAMENTE
   ```

---

### Paso 4 — Verificar la base de datos y tablas creadas

1. En el panel izquierdo (**Navigator**), haz clic en la pestaña **"Schemas"**.
2. Busca **`creaciones_edimile`** en la lista de bases de datos.
3. Haz clic en la flecha ▶ para expandirla.
4. Expande la carpeta **"Tables"** — deberías ver las 11 tablas:
   - `categorias`
   - `clientes`
   - `cotizaciones`
   - `detalle_cotizacion`
   - `detalle_venta`
   - `insumos`
   - `movimientos_insumo`
   - `productos`
   - `roles`
   - `usuarios`
   - `ventas`

5. Para verificar los datos insertados, abre una nueva consulta (`Ctrl + T`) y ejecuta:
   ```sql
   USE creaciones_edimile;
   SELECT * FROM roles;
   SELECT * FROM usuarios;
   SELECT * FROM clientes;
   SELECT * FROM productos;
   SELECT * FROM insumos;
   ```

---

### Paso 5 — Configurar la base de datos como predeterminada

1. En el panel **Schemas**, haz clic derecho sobre **`creaciones_edimile`**.
2. Selecciona **"Set as Default Schema"**.
3. La base de datos ahora aparecerá en **negrita**, indicando que es la activa.

---
---

## PARTE 2: Extraer el Diagrama ER desde MySQL Workbench

---

### Método 1 — Ingeniería Inversa (Reverse Engineering) — **RECOMENDADO**

Este método genera automáticamente el diagrama ER a partir de la base de datos existente.

**Paso a paso:**

1. En el menú superior, ve a:
   ```
   Database → Reverse Engineer...
   ```
   (Atajo: `Ctrl + R`)

2. Se abre el asistente **"Reverse Engineer Database"**. Sigue estos pasos:

   **Pantalla 1 — Connection Options:**
   - Verifica que la conexión sea correcta (la misma que usaste para crear la BD).
   - Haz clic en **Next →**.

   **Pantalla 2 — Connect to DBMS:**
   - Workbench se conecta al servidor. Si pide contraseña, ingresa la del usuario `root`.
   - Haz clic en **Next →**.

   **Pantalla 3 — Select Schemas:**
   - En la lista de esquemas disponibles, **marca la casilla** junto a `creaciones_edimile`.
   - Haz clic en **Next →**.

   **Pantalla 4 — Retrieve Objects:**
   - Workbench carga los objetos de la base de datos.
   - Haz clic en **Next →**.

   **Pantalla 5 — Select Objects:**
   - Asegúrate de que estén marcadas las opciones:
     - ✅ **Import MySQL Table Objects**
     - ✅ **Place imported objects on a diagram**
   - Haz clic en **Execute →**.

   **Pantalla 6 — Reverse Engineer Progress:**
   - El proceso se completa en segundos.
   - Haz clic en **Next →** y luego en **Close**.

3. Se abre automáticamente una nueva pestaña con el **Diagrama EER** (Enhanced Entity-Relationship).
4. Las tablas aparecerán con sus columnas y las relaciones (líneas) entre ellas.

---

### Paso a paso para organizar el diagrama:

1. **Mover tablas:** Haz clic y arrastra cada tabla para organizarlas de forma clara.
2. **Sugerencia de organización:**
   ```
   [ROLES] ──── [USUARIOS]
                    │
        ┌───────────┼───────────┐
        │           │           │
   [MOVIMIENTOS] [COTIZACIONES] [VENTAS]
        │           │               │
   [INSUMOS]  [DET_COT]        [DET_VENTA]
              [PRODUCTOS] ─── [CATEGORIAS]
              [CLIENTES]
   ```
3. **Zoom:** Usa `Ctrl + Scroll` para hacer zoom, o los botones de lupa en la barra inferior.
4. **Autoarrancar:** Menú **Arrange → Autolayout** para acomodar automáticamente.

---

### Método 2 — Exportar el Diagrama como imagen

1. Con el diagrama abierto, ve al menú:
   ```
   File → Export → Export as PNG...
   ```
   o también:
   ```
   File → Export → Export as PDF...
   ```
2. Elige la carpeta de destino (ej: la carpeta `04_Base_de_Datos` del proyecto).
3. Nómbralo: `DER_Creaciones_Edimile.png`.
4. Haz clic en **Save**.

---

### Método 3 — Guardar el modelo EER

1. Ve al menú:
   ```
   File → Save Model As...
   ```
2. Guárdalo como `creaciones_edimile.mwb` en la carpeta del proyecto.
3. Este archivo `.mwb` puede abrirse y editarse en cualquier PC con MySQL Workbench.

---
---

## PARTE 3: Solución de Problemas Comunes

| Problema | Causa probable | Solución |
|----------|---------------|---------|
| Error: `Can't connect to MySQL server` | MySQL no está corriendo | Abre el panel de servicios de Windows (`services.msc`) y inicia el servicio **MySQL80** |
| Error: `Access denied for user 'root'` | Contraseña incorrecta | Verifica la contraseña de root ingresada al instalar MySQL |
| Las tablas no aparecen en Schemas | La BD no se seleccionó como activa | Haz clic derecho en `creaciones_edimile` → **Set as Default Schema** y refresca con F5 |
| Error en Reverse Engineer: `No schemas selected` | Se saltó la selección del esquema | Repite el proceso y **marca** la casilla de `creaciones_edimile` en el Paso 3 |
| El diagrama sale desordenado | Las tablas se superponen | Usa **Arrange → Autolayout** o reorganiza manualmente |
| Error: `Duplicate entry` en los INSERTs | El script se ejecutó dos veces | El script tiene `DROP DATABASE IF EXISTS` al inicio, así que es seguro ejecutarlo completo de nuevo |

---

## Notas Importantes

> ⚠️ **Sobre las contraseñas en el script:**
> Los hashes de contraseña en el script SQL son **de ejemplo para demostración**.
> En la aplicación Spring Boot, debes generar los hashes reales con:
> ```java
> new BCryptPasswordEncoder().encode("Edimile2024!")
> ```
> Y actualizar los registros de la tabla `usuarios` con los hashes correctos antes de iniciar sesión.

---

*Guía elaborada para la materia de Programación en Java — 2024*
