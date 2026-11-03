# Setup and Verification Guide

## 1. Prerequisites

- Node.js with npm.
- The repository cloned locally.
- Optional: the LevTicket backend running at `http://localhost:8080`.

The frontend already contains `node_modules/` in the current workspace, but a clean setup should install dependencies from the lockfile.

## 2. Install dependencies

From the repository root:

```powershell
cd frontend
npm install
```

## 3. Run the frontend

```powershell
cd frontend
npm run dev
```

Vite prints the local URL, normally `http://localhost:5173`.

## 4. Run with the backend

Start the backend separately according to the backend project instructions. The frontend expects API requests at:

```text
http://localhost:8080/api
```

The Vite development proxy also forwards `/api` requests to `http://localhost:8080`.

## 5. Run a UI demonstration with mocks

Mock mode can be enabled in the frontend environment:

```powershell
cd frontend
$env:VITE_USE_MOCKS = 'true'
npm run dev
```

Mock support is selective. Full authentication, bidding, orders, reviews, and AI workflows still depend on backend services unless they are extended with additional mock implementations.

## 6. Build for production

```powershell
cd frontend
npm run build
```

The production output is generated in `frontend/dist/`.

To preview the production build:

```powershell
npm run preview
```

## 7. Verification checklist

- [ ] The application opens without a blank screen.
- [ ] The home page renders navigation and ticket content.
- [ ] Search navigation works.
- [ ] Login and registration forms submit correctly with the backend.
- [ ] Ticket details display ticket and auction information.
- [ ] Bid submission updates the current bid.
- [ ] Ticket publishing accepts event fields and images.
- [ ] Listings tabs load their respective statuses.
- [ ] Orders tabs load bought and sold records.
- [ ] Review modal submits a rating and comment.
- [ ] Profile data and reviews display correctly.
- [ ] Customer-service drawer opens from every route.
- [ ] Dedicated AI chat displays messages and loading states.
- [ ] The layout remains usable on a narrow mobile viewport.
- [ ] `npm run build` completes successfully.

## 8. Production considerations

Before deployment, configure the API URL through an environment variable instead of the current local development URL. Add HTTPS, route guards, server-side authorization, upload restrictions, automated tests, and monitoring.
