# Wavely — v2 UI/UX Redesign Brief (paste into Claude, attach the screenshots)

## Your role & the goal
You are a senior product/visual designer specializing in Android (Material 3 / Jetpack
Compose) apps. Redesign the **visual UI and UX** of **Wavely**, an offline, on-device
Android music player, into a modern, premium, cohesive product — **without changing any
functionality, features, screens, navigation, or the information shown.**

This is a **reskin + UX polish**, NOT a re-architecture. Every control, screen, list item,
and interaction visible in the attached screenshots must still exist and do exactly the
same thing. Only the *design* changes: layout refinement, visual hierarchy, color,
typography, spacing, iconography, components, states, motion, and micro-interactions.

## The product (context you need)
- **Offline local-library player.** It scans music files already on the phone. No
  streaming, no accounts, no social, no online catalog.
- **Real-world content is messy:** many tracks have **no album art** and weak metadata
  ("Unknown artist", very long titles like "Namah Aum Namah Shivaya | Mahashivratri 2026 |
  Devotional Shiva Song …"). The design MUST look great in this art-less, long-title,
  mixed-metadata reality — not only with perfect album art. Strong **placeholder/fallback
  artwork** is a priority.
- **Stack:** single-Activity **Jetpack Compose + Material 3**. **Dark theme is primary**;
  **light theme** and Android **dynamic color (Material You)** are both supported and must
  be designed. The redesign must be implementable as a Compose theme + component restyle.
- **Structure:** 3 bottom-nav tabs — **Songs / Search / Playlists** — plus a persistent
  **mini-player** docked above the bottom nav that **expands into a full "Now Playing"
  sheet** (drag up to expand, drag/swipe down or predictive-back to collapse). Settings is
  reached from a top-bar **gear**. Sort, Queue, Sleep-timer, Add-to-playlist, and New/Rename
  playlist are modal sheets/dialogs.

## HARD CONSTRAINTS — do NOT change (functionality lock)
1. **Keep every screen and state** listed below, and **every control on each**. Do not add,
   remove, merge, or split features or screens.
2. **Keep the navigation model:** 3 bottom tabs (Songs/Search/Playlists); persistent
   mini-player; expanding Now Playing sheet (drag fraction between mini and full); top-bar
   gear → Settings; Favorites/Playlist **Detail** screens; modal sheets/dialogs for Sort,
   Queue, Sleep timer, Add-to-playlist, New/Rename playlist.
3. **Keep the same data and actions** per row/screen: track title + secondary
   "artist · album/duration" line, current-track indicator, and all actions — tap-to-play,
   play next, add to queue, add to playlist, favorite, shuffle-all, sort (Title/Date added/
   Duration + asc/desc), long-press **multi-select** with batch actions, **queue** reorder &
   swipe-to-remove-with-undo, sleep timer (15/30/45/60), playback **speed**, shuffle, repeat,
   theme (Light/Dark/System) + dynamic-color toggle.
4. **Stay implementable & platform-correct:** Compose/Material 3 components; **edge-to-edge
   with correct status/navigation-bar insets** (bottom nav, mini-player, and Now Playing
   controls must never sit under the system bars — this was a real bug in v1, so call out
   inset-safe layouts explicitly); **≥48dp touch targets**; predictive-back; RTL/auto-mirrored
   icons; **responsive** width classes (compact phone / medium / expanded-tablet) where the
   bottom nav becomes a navigation rail and Now Playing becomes a **two-pane** in landscape /
   a persistent side panel on wide screens.

## What to improve (design goals)
- A **distinctive, calm, premium identity** for a music player: a refined "Wavely" wordmark/
  mark, a signature accent, and tasteful use of color, gradient, depth, and motion — clearly
  beyond stock Material defaults (current build looks like default Material with gray
  placeholder tiles).
- **Typographic hierarchy & rhythm** (the app uses a 4dp spacing base). Make long titles and
  "Unknown artist" read cleanly: clear primary/secondary contrast, graceful truncation,
  comfortable density.
- A first-class **album-art system with generated fallbacks** (e.g., gradient/initial/
  monogram tiles derived from the title) since most tracks lack art; consistent corner radius;
  a polished **now-playing equalizer** indicator on the active row.
- A **premium Now Playing** screen: large expressive artwork, an **ambient art-derived
  background** (blur/gradient that also works for the gradient fallbacks), refined transport
  controls + seekbar (elapsed / remaining), and the utility row (queue / sleep / speed /
  favorite). Make the **mini-player ⇄ full-sheet expansion** feel smooth and physical.
- Polished **mini-player, bottom nav, top bars, list rows, sheets & dialogs, empty / loading /
  permission states, multi-select contextual bar**, and selection/drag affordances.
- Cohesive **iconography** and a subtle, purposeful **motion language** (press, select,
  favorite toggle, drag-reorder, snackbar/undo, sheet expand/collapse).
- Full **light + dark** + **dynamic-color** variants; verify **WCAG AA** contrast and that
  layouts survive **large font scales**.

## Screens & states to redesign (attached screenshots — redesign EACH)
- **01 — Songs (Library):** flat list of all tracks; top bar = Wavely + Shuffle-all + Sort +
  Settings; rows with art/title/secondary line + 3-dot overflow; collapsed mini-player above
  bottom nav.
- **02 — Search (empty state):** search field + centered empty prompt.
- **14 — Search (results):** query typed, results list.
- **03 — Playlists:** "Favorites" pinned row (heart, track count) + user playlists; "+" to
  create; empty hint when none.
- **06 — Favorites / Playlist Detail:** back + title + shuffle; track list; (playlist detail
  also has reorder + remove-with-undo — design those affordances).
- **04 — Settings:** Theme (Light/Dark/System) radios, Dynamic-color toggle, app footer.
- **05 — New playlist dialog** (also represents Rename): title + text field + Cancel/Create.
- **08 — Sort bottom sheet:** Title / Date added / Duration + Ascending/Descending + Done.
- **09 — Track overflow menu:** Play next / Add to queue / Add to playlist / Add to favorites.
- **10 — Multi-select mode:** contextual top bar ("N selected", clear, play-next, add-to-queue,
  add-to-playlist) + per-row selection state.
- **11 — Now Playing (expanded sheet):** drag handle, large art, title/source, shuffle·prev·
  play/pause·next·repeat, seekbar (elapsed/remaining), utility row (queue/sleep/speed/favorite).
- **12 — Queue sheet ("Up next"):** queued tracks with drag-reorder handles (+ swipe-to-remove).
- **13 — Sleep-timer dialog:** 15/30/45/60 minutes + Cancel.
- **Also design (not screenshotted, exists in code):** the **mini-player collapsed** strip on
  its own; the **audio-permission** empty state (music icon + rationale + "Grant access"
  button); **loading** state (scanning library spinner); and a **snackbar with Undo** (used for
  remove-from-queue/playlist).

## Deliverables
1. **A concise design system:** color tokens (dark + light, and how dynamic color maps in),
   type scale, spacing/radius/elevation, icon set, and component specs (list row, mini-player,
   bottom nav/rail, top bar, buttons, chips, sheets, dialogs, the "+"/FAB, selection bar,
   seekbar, equalizer indicator, artwork-fallback generator).
2. **High-fidelity mockups of every screen/state above**, **dark (primary)** plus the key
   screens in **light**, sized for a compact phone (~1080×2340 / 9:19.5). Include the
   collapsed mini-player, the expanded Now Playing, and 1–2 **intermediate frames** of the
   mini→full expansion.
3. **Implementation notes / redlines** mapping each redesigned element to the existing control
   it replaces, so engineering can apply it as a pure restyle (no logic changes).
4. **Responsive notes** (at least schematic): medium-width nav rail, expanded-width Now Playing
   side panel, and landscape Now Playing two-pane.

## Pick a direction (propose 2–3, then commit to one)
Offer a couple of distinct visual directions before going high-fidelity — e.g.
(a) **"Calm minimal"** — deep neutrals, one serene accent, generous whitespace, soft art tiles;
(b) **"Expressive Material You"** — dynamic-color-forward, rounded, playful motion, vibrant
gradient fallbacks; (c) **"Editorial/premium"** — high-contrast type, bold now-playing artwork
treatment, refined grid. Recommend one and explain why it fits an offline, art-sparse library.

## Reminder
Preserve all current functionality and information architecture **exactly**. If a visual idea
would require a behavioral or feature change, list it separately under "Optional future ideas"
rather than building it into the core redesign.
