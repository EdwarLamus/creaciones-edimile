# Guía de Despliegue en Producción
## Firebase Hosting + Google Cloud Run

> **Arquitectura**: Firebase Hosting actúa como CDN/proxy y redirige todo el
> tráfico a Cloud Run, donde corre el contenedor Docker con Spring Boot.

---

## Prerrequisitos

1. **Google Cloud CLI** instalado → https://cloud.google.com/sdk/docs/install
2. **Docker Desktop** instalado → https://www.docker.com/products/docker-desktop/
3. **Firebase CLI** instalado:
   ```bash
   npm install -g firebase-tools
   ```
4. Tener el proyecto en **Firebase Console** creado (mismo proyecto donde está Firestore).

---

## Paso 1 — Configurar credenciales de producción

### 1.1 Configurar application.properties para producción

En producción **no** debes usar el archivo `service-account.json` directamente.
En su lugar, Cloud Run puede autenticarse automáticamente usando la cuenta de servicio
del proyecto sin necesidad del archivo JSON.

Edita `src/main/resources/application.properties`:
```properties
# En Cloud Run, Firebase detecta las credenciales automáticamente
# (Application Default Credentials). Comentar la línea de classpath:
# firebase.credentials.path=classpath:firebase/service-account.json
```

O crea un perfil de producción en `src/main/resources/application-prod.properties`:
```properties
# En Cloud Run: ADC (Application Default Credentials) — no necesita archivo JSON
firebase.credentials.path=
```

### 1.2 Configurar variables sensibles

Las contraseñas de correo y otros secretos NO deben estar en el código.
En Cloud Run se configuran como **variables de entorno** o **Secret Manager**.

---

## Paso 2 — Autenticarse en Google Cloud

```bash
# Iniciar sesión
gcloud auth login

# Seleccionar tu proyecto (usa el ID de tu proyecto Firebase)
gcloud config set project TU-PROYECTO-FIREBASE-ID

# Autenticar Docker con Google Artifact Registry
gcloud auth configure-docker us-central1-docker.pkg.dev
```

---

## Paso 3 — Crear repositorio en Artifact Registry

```bash
gcloud artifacts repositories create creaciones-edimile \
  --repository-format=docker \
  --location=us-central1 \
  --description="Imágenes Docker de Creaciones Edimile"
```

---

## Paso 4 — Construir y subir la imagen Docker

Desde la raíz del proyecto (`e:\5 semestre\JAVA\creaciones-edimile`):

```bash
# Definir variables
$PROJECT_ID = "TU-PROYECTO-FIREBASE-ID"
$IMAGE = "us-central1-docker.pkg.dev/$PROJECT_ID/creaciones-edimile/app:latest"

# Construir la imagen
docker build -t $IMAGE .

# Subir al registro
docker push $IMAGE
```

---

## Paso 5 — Desplegar en Cloud Run

```bash
gcloud run deploy creaciones-edimile `
  --image $IMAGE `
  --platform managed `
  --region us-central1 `
  --allow-unauthenticated `
  --port 8080 `
  --memory 512Mi `
  --set-env-vars "SPRING_MAIL_USERNAME=tu-correo@gmail.com" `
  --set-env-vars "SPRING_MAIL_PASSWORD=tu-contraseña-app" `
  --set-env-vars "SPRING_PROFILES_ACTIVE=prod"
```

> **Importante**: Para secretos usa Secret Manager en lugar de `--set-env-vars`:
> ```bash
> gcloud run deploy creaciones-edimile --set-secrets="SPRING_MAIL_PASSWORD=mail-password:latest"
> ```

---

## Paso 6 — Configurar Firebase Hosting

### 6.1 Actualizar .firebaserc

Edita `.firebaserc` y reemplaza `TU-PROYECTO-FIREBASE-ID` con el ID real de tu proyecto.

### 6.2 Iniciar sesión en Firebase CLI

```bash
firebase login
```

### 6.3 Desplegar Firebase Hosting

```bash
firebase deploy --only hosting
```

Esto conecta Firebase Hosting con el servicio de Cloud Run `creaciones-edimile`.

---

## Paso 7 — Dominio personalizado (opcional)

En **Firebase Console → Hosting → Dominio personalizado**:
1. Agrega tu dominio (ej: `sistema.tudominio.com`)
2. Firebase genera registros DNS para verificar
3. Agrega los registros DNS en tu proveedor de dominio
4. El SSL se genera automáticamente

---

## Variables de entorno en Cloud Run

| Variable | Descripción |
|---|---|
| `SPRING_MAIL_USERNAME` | Correo Gmail para enviar emails |
| `SPRING_MAIL_PASSWORD` | Contraseña de aplicación de Gmail |
| `SPRING_PROFILES_ACTIVE` | `prod` para activar perfil de producción |
| `SERVER_PORT` | Puerto (Cloud Run usa `8080` por defecto) |

---

## Consideraciones de producción

### Uploads de imágenes
Cloud Run no tiene sistema de archivos persistente. Los archivos subidos en `/uploads/`
se perderán al reiniciar el contenedor. Solución: usar **Google Cloud Storage** para
almacenar imágenes de productos.

### Credenciales de Firebase
En Cloud Run, la app se autentica automáticamente con Firestore usando la cuenta de
servicio del proyecto (sin necesidad de `service-account.json`). Asegúrate de que
la cuenta de servicio tenga el rol **Firestore User**.

### HTTPS
Firebase Hosting + Cloud Run sirve automáticamente con HTTPS/SSL. No se necesita
configuración adicional.

---

## Flujo de actualización (CI/CD básico)

Para actualizar el sistema después del primer deploy:

```bash
# 1. Construir nueva imagen
docker build -t $IMAGE .

# 2. Subir imagen
docker push $IMAGE

# 3. Cloud Run actualiza automáticamente al detectar nueva imagen,
#    o forzar el nuevo deploy:
gcloud run deploy creaciones-edimile --image $IMAGE --region us-central1
```

---

## Verificación

Una vez desplegado, Firebase te dará una URL como:
`https://TU-PROYECTO-FIREBASE-ID.web.app`

Visita esa URL para confirmar que la aplicación funciona correctamente.
