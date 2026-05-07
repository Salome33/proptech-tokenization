# PropTech: Tokenización de Activos Inmobiliarios

Monolito con:

- **Backend** Spring Boot (Java 21) + **JWT** (registro/login) + H2
- **2 servicios PropTech**:
  - **Propiedades**: crear/listar/ver propiedades
  - **Tokenización e inversión**: crear ofertas, abrir/cerrar, invertir (simulado)
- **Frontend** Angular 21 (SPA) en español, consume el backend en local
- **Docker**: backend dockerizado
- **CI/CD**: workflow para desplegar backend a **Google Cloud Run**

## Requisitos

- Java 21 + Maven (para correr backend sin Docker)
- Node 20+ (para correr frontend)
- Docker (para correr backend dockerizado)

## Ejecutar en local (sin Docker)

En una terminal:

```bash
cd proptech-tokenization/backend
mvn spring-boot:run
```

Backend: `http://localhost:8080`

En otra terminal:

```bash
cd proptech-tokenization/frontend
npm install
npm start
```

Frontend: `http://localhost:4200`

## Ejecutar con Docker (backend)

```bash
cd proptech-tokenization
docker build -t proptech-tokenization-backend:local ./backend
docker run --rm -p 8080:8080 -e JWT_SECRET="dev-only-change-me-please-dev-only-change-me-please" proptech-tokenization-backend:local
```

## Endpoints principales

### Auth (público)

- `POST /api/auth/register`
- `POST /api/auth/login`

Body:

```json
{ "email": "demo@proptech.com", "password": "secret123" }
```

### PropTech (protegido con JWT)

- `GET /api/proptech/ping`
- `POST /api/proptech/properties`
- `GET /api/proptech/properties`
- `GET /api/proptech/properties/{id}`
- `POST /api/proptech/offerings`
- `GET /api/proptech/offerings`
- `PUT /api/proptech/offerings/{id}/status` (OPEN/CLOSED)
- `POST /api/proptech/investments`
- `GET /api/proptech/investments?offeringId=...`

## Pruebas

```bash
cd proptech-tokenization/backend
mvn test
```

## Despliegue a Google Cloud Run (GitHub Actions)

Workflow: `.github/workflows/deploy.yml` (en la raíz del repo)

Secrets requeridos en GitHub (Settings → Secrets and variables → Actions):

- `GCP_PROJECT_ID`
- `GCP_SA_KEY` (JSON completo de la service account)
- `JWT_SECRET`

Por defecto el workflow usa Artifact Registry repo `proptech-apis` en `us-central1`.

## Subir todo al repositorio (Git)

Desde la carpeta `proptech-tokenization`:

```bash
git status
git add .
git commit -m "add proptech backend frontend docker ci"
git push
```
