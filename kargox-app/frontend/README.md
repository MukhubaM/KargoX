# KargoX Web (frontend source)

This is the source for the React UI. It's built straight into `../src/main/resources/static` so the Spring Boot app in the parent folder serves it — see the root `README.md` for the full run/build instructions.

```bash
npm install
npm run build     # → ../src/main/resources/static (Spring Boot picks this up)
npm run dev       # optional hot-reload dev server on :5173, proxies to :8080
```

## Design identity

Built around a **cargo waybill** motif — booking cards are laid out like a physical shipping manifest (route + dashed perforation + tracking-code/fare/status stamp), since the product moves goods, not people. Dark asphalt operations chrome (sidebar) against a pale paper content surface; amber signal + muted route-green + brick alert are the only saturated colors. Space Grotesk for headers, Inter for UI, IBM Plex Mono for tracking codes/PINs/OTPs.

## What's wired up

- JWT auth (access + refresh token in localStorage), role-based route guards
- Customer: 3-step booking wizard (route → goods → fare estimate/confirm), live tracking page (polls every 4s), booking history, PIN/OTP display, cancel, post-delivery rating
- Driver: onboarding, online/offline toggle, live job offers over STOMP/WebSocket, accept-to-assign, full lifecycle actions, vehicle registration, history
- Admin: dashboard stats, user search/suspend, all-bookings view

## Known simplifications

- Pickup/destination are entered as address text + manual lat/lng (no map picker wired in — swap in Google Maps/Mapbox where noted in `NewBookingPage.jsx`)
- Driver location is a fixed demo coordinate on "Go online" — swap in `navigator.geolocation.watchPosition` for a real device
- Tracking pages poll on an interval rather than subscribing to `/topic/bookings/{id}`; the WebSocket helper (`src/lib/ws.js`) already supports this if you want to switch it over
