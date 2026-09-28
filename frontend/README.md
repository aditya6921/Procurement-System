# ProcureFlow frontend

React single-page app, separate from the Spring Boot backend.

## Development

From this directory run:

```powershell
npm install
npm run dev
```

Open `http://localhost:5173`. Vite proxies `/api` requests to `http://localhost:8080`; start the Spring Boot backend first.

## Production build

```powershell
npm run build
```

The production files are written to `dist/`. To host the frontend separately from the API, set `VITE_API_BASE_URL` to the backend URL at build time and add the frontend origin to the backend CORS configuration.
