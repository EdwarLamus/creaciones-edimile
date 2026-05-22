# Credenciales Firebase — Instrucciones

Este directorio debe contener el archivo `service-account.json`
con las credenciales de Firebase Admin SDK.

**El archivo real está excluido del repositorio por `.gitignore`
para proteger las credenciales privadas.**

---

## Cómo obtener tu service-account.json

1. Ve a [https://console.firebase.google.com](https://console.firebase.google.com)
2. Abre tu proyecto (o crea uno nuevo)
3. En la barra lateral: **Configuración del proyecto** (ícono de engranaje)
4. Pestaña: **Cuentas de servicio**
5. Haz clic en **"Generar nueva clave privada"**
6. Guarda el archivo descargado con el nombre exacto:

   ```
   src/main/resources/firebase/service-account.json
   ```

---

## Configurar Firestore en Firebase

1. En Firebase Console → **Firestore Database**
2. Haz clic en **"Crear base de datos"**
3. Selecciona el modo: **Producción** (puedes ajustar reglas después)
4. Elige la región más cercana (ej: `us-east1`)
5. La colección `usuarios` se crea automáticamente al arrancar la app

---

## Estructura del archivo (referencia)

Ver `service-account-example.json` en este mismo directorio
para conocer los campos requeridos.
