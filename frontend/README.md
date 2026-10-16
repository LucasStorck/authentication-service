# Frontend

React + TypeScript app built with Vite, styled with Tailwind CSS v4 and the
[neobrutalism.dev](https://www.neobrutalism.dev/) component library
(shadcn-style components, installed into `src/components/ui`).

## Scripts

```bash
npm install
npm start       # dev server on http://localhost:4200, /api proxied to localhost:8080
npm run build   # type-check and build into dist/
npm run lint
```

## Adding components

```bash
npx shadcn@latest add https://neobrutalism.dev/r/<component>.json
```

## Layout

```
src/
├── core/    API client, auth context, route guard
├── pages/   Login, register, home
└── components/ui/   neobrutalism components
```
