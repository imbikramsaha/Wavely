# Wavely v2 — Redesign Handoff

A complete visual + UX redesign of **Wavely**, an offline Android music player.
Direction: **calm minimal** — deep neutrals, one serene cyan→indigo accent (from the
Wavely logo), generated gradient fallback artwork for an art-less library. This is a
**pure reskin + UX polish**: every v1 screen, action, and data point is preserved. No
behavior changes.

Target stack to implement in: **Jetpack Compose + Material 3**, dark-primary, with light
theme and dynamic color (Material You). The HTML here is the spec/prototype, not the
shipping code.

## Open / run locally
These pages load component files and React/Babel, so a browser's `file://` security
blocks them on a plain double-click. Serve the folder instead (any one):

```bash
# from this folder
python3 -m http.server 8080
# then open http://localhost:8080/Wavely%20Prototype.html
```
(or `npx serve`, or VS Code "Live Server"). You need an internet connection the first
time — React and Babel load from a CDN.

## What's here

| File | What it is |
|------|------------|
| **Wavely Prototype.html** | The interactive app in a phone shell — tap mini-player to expand Now Playing, long-press rows to multi-select, switch theme in Settings, open every sheet/menu. Live playback + motion. |
| **Wavely Screens.html** | Pan/zoom design canvas: all ~27 screens & states, dark + full light. |
| **Wavely Design System.html** | Tokens, type scale, spacing/radius, icon set, fallback-art generator, component specs, and the **v1→v2 redline table**. |
| **Wavely Logo.html** | The redesigned 5-bar brand mark (white & black). |
| `wavely/` | The shared implementation (see below). |
| `frames/` | `design-canvas.jsx` (used by the canvas page). |
| `exports/` | Production logo assets: Android launcher icon set + logo pack (PNG/SVG). |

## Architecture (`wavely/`)
The prototype and the canvas share one component layer — the canvas just renders the
same components at frozen states.

| File | Role |
|------|------|
| `tokens.css` | All design tokens as CSS vars, scoped to `.theme-dark` / `.theme-light`; plus every component class (rows, mini-player, nav, sheets, dialogs, buttons, equalizer, seekbar). **Start here for the Compose theme.** |
| `data.js` | The realistic library/favorites/search data pulled from the screenshots. |
| `art.jsx` | `WavelyArt` (title-hashed gradient fallback tile + monogram + waveform), `wvAmbient` (Now-Playing background), `Eq` (animated equalizer), `WavelyMark` (5-bar logo), and the custom `Icon` set. |
| `chrome.jsx` | Status bar, top bar, `TrackRow`, `MiniPlayer`, `BottomNav`. |
| `screens.jsx` | Songs, Search, Playlists, Favorites/Playlist detail, Settings, multi-select bar. |
| `overlays.jsx` | Now Playing sheet, Queue, Sort, Sleep, New-playlist, Add-to-playlist, overflow menu, snackbar, permission, loading. |
| `app.jsx` | State controller (`useWavely` live / `makeStaticApp` frozen) + the root `WavelyApp` that composes the screen, dock, Now-Playing layer and overlays. |

## Implementation pointers
- **Tokens → Compose:** translate `wavely/tokens.css` `:root`/`.theme-*` vars into a
  M3 `ColorScheme` + `Typography` + shape/spacing constants. Hanken Grotesk is the
  typeface; 4dp spacing base.
- **Dynamic color:** map Android `primary → --wv-accent`, `surfaceContainer* →
  --wv-surface 1/2/3`, `onSurfaceVariant → --wv-text-2`; keep the brand gradient fixed
  for mark / now-playing progress / active equalizer.
- **Inset-safe (v1 bug):** the docked mini-player + bottom nav and the Now-Playing
  transport pad `env(safe-area-inset-bottom)` — nothing under the system bars.
- **Fallback art:** the hue is `hash(title) % 360`, stable per title — port the hash in
  `art.jsx` so a song always gets the same tile.
- The full v1→v2 element mapping and responsive/a11y/motion notes are in
  **Wavely Design System.html** (Implementation notes · Redlines).
