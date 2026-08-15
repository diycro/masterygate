# Frontend (Angular)

The Angular app that replaced the original vanilla-JS single-page app. It's a set of
standalone components (login, dashboard, topics, path, module, gate, and the three
mock-interview screens) talking to the same `/api/**` backend as before.

## Just running the app (no Node.js needed)

The production build of this app is already committed into
`../backend/src/main/resources/static/`. `mvn spring-boot:run` (or the packaged jar)
serves it directly at `http://localhost:8080/` — nothing extra to install or build.
You only need Node/Angular if you want to *change* the frontend.

## Developing the frontend

```bash
npm install
npm start            # ng serve --proxy-config proxy.conf.json, http://localhost:4200
```

`proxy.conf.json` forwards `/api/**` to `http://localhost:8080` so `ng serve` and the
Spring Boot backend (run separately, e.g. `./mvnw spring-boot:run` in `../backend`) work
together with live reload.

## Publishing a change to production

After editing the frontend, rebuild it and copy the output into the backend's static
resources so `mvn spring-boot:run` picks it up with no separate frontend process:

```bash
ng build --configuration production
rm -f ../backend/src/main/resources/static/*
cp -r dist/frontend/browser/. ../backend/src/main/resources/static/
```

Then rebuild/restart the backend as usual. Commit both the source changes and the
regenerated `static/` output together.

## Notes

- Routing is real (path-based, not hash-based): `/dashboard`, `/mock/start`, etc. are
  actual browser URLs. A `SpaForwardController` on the backend forwards any non-API,
  non-asset path to `index.html` so refreshes and direct links work.
- Cross-page state that used to live in the vanilla app's global `state` object (the
  active learning path, in-progress gate/mock-interview session, etc.) now lives in
  `AppStateService` — it's intentionally transient (in-memory only), matching the old
  behavior.
