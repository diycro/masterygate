# Frontend (Angular) — scaffolded in Sprint 1

To keep the repo light (no committed `node_modules`), the Angular app is generated
in **Sprint 1**, not Sprint 0. The **target UI already exists** as the clickable
prototype (`../../learning/learning_studio_prototype.html`) — Sprint 1 turns that
into a real Angular app wired to the API.

When you're ready (Sprint 1):

```bash
cd learning-studio
npx @angular/cli@latest new frontend --style=css --routing --ssr=false
cd frontend
npx ng serve        # http://localhost:4200
```

Then point it at the backend (`http://localhost:8080/api`) and start porting the
prototype's four views: Dashboard, Path, Module detail, Examiner.
