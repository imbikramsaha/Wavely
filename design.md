# Offline Android Music Player — Design & Implementation Blueprint

**Document type:** Technical specification
**Target platform:** Android 8.0 (API 26) → Android 15 (API 35)
**Primary language:** Kotlin
**Audience:** Implementing developer / Claude Code
**Design lineage:** Google Podcasts (visual system) × Anrimian/music-player (feature & architecture patterns)
**Revision:** 2 — corrected against actual Google Podcasts screenshots; bottom nav capped at 3; stress-test appendix added.

---

## 0. How to Read This Document

This is a build specification, not a pitch. Sections 1–10 define *what to build*; Section 11 defines *build order and verification*; **Appendix C is a stress test** — read it before estimating, because it lists the places this plan is most likely to break and the decisions you must make up front.

Conventions:
- `dp` = density-independent pixels, `sp` = scale-independent (text).
- All sizes are baselines; respect user font-scale and display-size.
- "Reference app" = `github.com/Anrimian/music-player`. "GP" = Google Podcasts.
- Where music and podcasts genuinely differ, the doc **deviates from GP on purpose** and says so. Those are not oversights.

---

## 1. App Architecture

### 1.1 Architectural Pattern

Three-module clean architecture, mirroring the reference app's separation:

```
:app      → UI layer (single Activity, fragments/Compose, ViewModels, navigation)
:domain   → Pure Kotlin business logic (use cases, models). No Android imports.
:data     → Repositories, MediaStore access, Room DB, Media3 playback bindings
```

- **Presentation:** MVVM + `StateFlow`/`SharedFlow`. (Reference app uses MVP/Moxy + RxJava; this is the modern equivalent.)
- **DI:** Hilt. **Async:** Coroutines + Flow. **Lists:** Paging 3 (see Appendix C, risk T-7).
- **Persistence:** Room (playlists, queue, counts, favorites). Audio files read via `MediaStore`.
- **Playback:** AndroidX **Media3 (ExoPlayer)** inside a `MediaSessionService`; UI binds via `MediaController`.

**UI toolkit decision (commit to one — do not mix):** This document assumes **Jetpack Compose** for all UI, including the Now Playing bottom sheet, because the GP design is bottom-sheet- and tab-heavy and Compose handles that with less ceremony. If the team is XML-native, that is acceptable, but then the shared-element/bottom-sheet notes in §5.3 must be re-read — the animation mechanics differ. See Appendix C, risk T-2.

### 1.2 Core Screens

GP exposes **three** bottom-nav destinations. We match that. Everything else is reached *within* those three or from the top-bar overflow.

| # | Screen | Bottom nav? | Purpose |
|---|--------|-------------|---------|
| S1 | **Library** (home) | ✅ Tab 1 | Recently-added shelf + browse, via scrollable tabs: Tracks / Albums / Artists / Folders |
| S2 | **Search** | ✅ Tab 2 | Query across tracks, albums, artists, playlists |
| S3 | **Activity** | ✅ Tab 3 | Scrollable tabs: **Queue / Playlists / History / Favorites** (mirrors GP's "Your queue / Downloads / History / Subscriptions") |
| S4 | **Now Playing** | ❌ bottom sheet | Expanding sheet from the mini-player (not a nav destination) |
| S5 | **Detail** | ❌ pushed | Track listing for a tapped album / artist / folder / playlist |
| S6 | **Settings** | ❌ top-bar | Reached from the top-bar overflow/avatar slot. Theme, playback, library scan, EQ, about |

> **Why Settings is not in the bottom nav:** GP keeps Settings behind the profile avatar, and the user constraint is 3 nav items. Since an offline player has **no account**, the avatar slot is repurposed as a **Settings/overflow entry** (a gear or `⋮`), not a Google avatar. See Appendix C, risk D-3.

### 1.3 Navigation Model

```
                    ┌─────────────────────────────────────┐
                    │   Single Activity + NavHost          │
                    │   Top bar:  [cast]  Title  [⋮/gear]  │
                    └──────────────────┬──────────────────┘
                                       │
        ┌──────────────────────────────┼──────────────────────────────┐
        ▼                              ▼                              ▼
   [Library S1]                   [Search S2]                   [Activity S3]
   tabs: Tracks/Albums/           query → results               tabs: Queue/Playlists/
   Artists/Folders                                              History/Favorites
        │                                                            │
        ▼                                                            ▼
   [Detail S5] ◄─────────────────────────────────────────── (playlist → Detail)
        │
        │  (top-bar overflow, from any screen)
        └──────────────► [Settings S6]

   ┌──────────────────────────────────────────────────────────────────────┐
   │ Mini-player (persistent, above bottom nav)  ──swipe up / tap──►        │
   │                                              [Now Playing S4 sheet]    │
   └──────────────────────────────────────────────────────────────────────┘
```

Single-Activity. The **mini-player** and **3-item bottom nav** persist across destinations. Now Playing is a full-height **bottom sheet** that expands from the mini-player and collapses back to it.

---

## 2. UI Component Specifications

### 2.1 Persistent Mini-Player

Docked strip directly above the bottom navigation, visible whenever a track is loaded.

```
┌──────────────────────────────────────────────────────────┐
│ ▓▓ │ Track Title                                  ⏯  ⏭   │  height 64dp
│ ▓▓ │ Artist · Album                                       │
└──────────────────────────────────────────────────────────┘
└── 2dp determinate progress line pinned to top edge ───────┘
```

- Height **64dp**. Art **48dp**, 4dp radius, 8dp leading.
- Title (`Subtitle1`, 1 line) + artist (`Caption`, 1 line, 60%).
- Trailing: Play/Pause (40dp target), Next (40dp).
- **Tap / swipe up** → expand to Now Playing sheet.
- **Swipe horizontally** → previous/next.
- **Connection gap:** until the `MediaController` finishes binding to the service, render the strip in a neutral non-interactive state (no flicker, no crash). See Appendix C, risk T-3.

### 2.2 Now Playing (expanding bottom sheet)

Mirrors GP image 2: drag handle, big square art, title/show line, transport row, seekbar with elapsed/remaining, and a **bottom utility row**. The underlying screen peeks at the very top when expanded.

```
┌──────────────────────────────────────────────┐
│                   ▁▁▁▁                         │  drag handle
│                                                │
│            ┌──────────────────────┐            │
│            │      ALBUM ART       │            │  square, ~72% width, ≤360dp
│            └──────────────────────┘            │
│                                                │
│            Track Title (Headline6)             │  centered
│            Artist · Album (Subtitle2, 60%)     │
│                                                │
│      🔀     ⏮      ▶/⏸      ⏭     🔁           │  transport row
│                                                │
│   ──────────●───────────────────────           │  seekbar
│   1:24                              -2:34       │  elapsed / remaining
│                                                │
│   ☰        🌙        1.0x       ⓘ        ⋮      │  utility row
│  queue   sleep     speed     info    overflow   │
└──────────────────────────────────────────────┘
```

#### Transport row

| Control | Icon | Target | Behavior |
|---------|------|--------|----------|
| Shuffle | 24dp | 48² | Toggle. Active = accent; inactive = 60%. |
| Previous | 32dp | 48² | <3s → previous track; ≥3s → restart current. Long-press = rewind seek. |
| Play / Pause | 36dp in **64dp** filled accent circle | 64² | Dominant control (matches GP's blue circle). |
| Next | 32dp | 48² | Next track. Long-press = forward seek. |
| Repeat | 24dp | 48² | Off → All → One. "One" shows a 1-badge. |

> **Deliberate deviation from GP:** podcasts use skip −10 / +30 and have no track concept, shuffle, or repeat. Music needs prev/next/shuffle/repeat. We keep the GP *layout and blue play circle* but use music-standard transport. This is intentional, not a fidelity miss.

#### Seekbar
- 4dp track; 12dp thumb (16dp dragging). Active = accent; inactive = on-surface @24%.
- Tap-to-seek; drag pauses auto-update until release.
- Left label = elapsed; **right label = remaining (negative), GP-style**, tappable to toggle to total duration.

#### Utility row (replaces the volume slider from rev 1)
`Queue` (opens queue sheet) · `Sleep timer` · `Speed` (cycles/menu, default 1.0x) · `Info` (track metadata) · `Overflow` (add to playlist, go to album/artist, equalizer, share, delete). In-app volume is **not** surfaced (use hardware keys); a volume row is available only as a Settings opt-in.

#### Album art
- Square `1:1`, 72% width capped 360dp, 8dp radius, 2dp elevation.
- Placeholder = neutral tile + music-note glyph. Optional faint Palette-derived background tint, **default OFF** to preserve GP calm.

### 2.3 List Item (track row)

```
┌──────────────────────────────────────────────────┐
│ ▓▓▓ │ Track Title                            ⋮    │  64dp
│ ▓▓▓ │ Artist · 3:58                               │
└──────────────────────────────────────────────────┘
```

- 64dp (56dp dense). Thumbnail 48dp/4dp radius/16dp leading.
- Title `Subtitle1` 1 line; secondary `Body2` @60% (artist · duration).
- Overflow `⋮` 24dp icon / 48dp target.
- Currently-playing row: title in accent + animated equalizer glyph replacing thumbnail.

> **Deliberate deviation:** GP rows show a download icon. Files are already local in an offline player, so **no download action**. The freed slot is unused (keeps rows clean); favorite/queue live in the overflow.

### 2.4 Shelf Card (Library home + Detail headers)

Mirrors GP image 1/3 carousels: square art, name below (`Subtitle1`), secondary line (`Caption`, 60%). Used for the "Recently added" / "Albums" horizontal shelves. No subscribe "+" (no subscription concept); tap = open Detail.

---

## 3. Color Palette & Typography

### 3.1 Light

| Token | Hex | Use |
|-------|-----|-----|
| surface / background | `#FFFFFF` | App background, sheets |
| surface-variant | `#F2F2F5` | Cards, mini-player |
| on-surface | `#1F1F1F` | Primary text/icons |
| on-surface-medium | `#5F6368` | Secondary text |
| outline | `#E0E0E0` | Dividers, seekbar track |
| accent | `#1A73E8` | Active controls, progress, current track, active tab + underline |
| accent-pressed | `#1664CC` | Pressed |
| error | `#D93025` | Destructive |

### 3.2 Dark

| Token | Hex | Use |
|-------|-----|-----|
| surface / background | `#121212` | App background |
| surface-variant | `#1E1E1E` | Cards, mini-player |
| on-surface | `#E8EAED` | Primary text |
| on-surface-medium | `#9AA0A6` | Secondary text |
| outline | `#3C4043` | Dividers, seekbar track |
| accent | `#8AB4F8` | Active controls (lighter blue for dark bg) |
| error | `#F28B82` | Destructive |

- Optional Material 3 dynamic color (Android 12+) as a Settings toggle, falling back to the static palettes.
- Theme modes: Light / Dark / System. All color use via theme attributes — never literal hex in components.

### 3.3 Typography (Roboto)

| Token | Size / weight | Usage |
|-------|---------------|-------|
| Headline6 | 20sp / Medium | Now Playing title, **centered** screen titles |
| Subtitle1 | 16sp / Regular | List/shelf titles |
| Subtitle2 | 14sp / Medium | Now Playing artist line, section headers, tab labels |
| Body1 | 16sp / Regular | Body, dialogs |
| Body2 | 14sp / Regular | List secondary |
| Caption | 12sp / Regular | Timestamps, publisher/metadata |
| Button | 14sp / Medium | Text buttons |

> **Correction from rev 1:** GP does **not** use a large collapsing left-aligned title. Screen titles ("Explore", "Activity") are ~20sp **centered** in the app bar. The home screen shows a centered wordmark. No `Headline4` collapsing header.

---

## 4. Layout Grids & Spacing

### 4.1 Spacing scale — 4dp base
`xs 4` · `sm 8` · `md 16` (default edge margin) · `lg 24` (section separation) · `xl 32` (Now Playing rhythm).

### 4.2 Grid
- Horizontal screen margin 16dp (phones); 24dp + max content width 640dp (tablets).
- Album grid: 2 col (phone portrait), 3–4 (landscape/tablet), 16dp gutter.
- Whitespace over rules — 1dp `outline` divider only between major sections, inset 16dp.
- Touch targets ≥ 48×48dp always.

### 4.3 App bar (corrected to match GP)
- Height 56dp. **Title centered**, `Headline6`. Leading = cast icon (or none). Trailing = overflow/gear (the Settings entry).
- Scrollable underlined **text tabs** sit beneath the app bar on Library and Activity: active label + 2dp underline in accent; inactive in on-surface-medium.
- Elevation 0 at rest; raise 4dp only when content scrolls beneath.

---

## 5. Navigation Flow

### 5.1 Bottom Navigation — exactly 3

```
┌──────────────────────────────────────────────┐
│        ♫              🔍              ☰        │
│     Library        Search         Activity     │
└──────────────────────────────────────────────┘
```

- 56dp (+ gesture inset). Active = accent icon + label; inactive = on-surface-medium.
- Re-tapping the active tab scrolls its content to top.
- Bottom nav stays visible behind the Now Playing sheet until the sheet is near-fully expanded, then it slides out (GP behavior).
- **Settings is reached from the top-bar overflow/gear, not the nav.**

### 5.2 Back behavior

| State | Back result |
|-------|-------------|
| Now Playing sheet expanded | Collapse to mini-player |
| Detail (S5) | Pop to originating list |
| Non-Library tab | Return to Library |
| Library root | Exit app |
| Search with query | Clear query, then exit search |
| Any modal sheet (queue, sort, track menu, sleep) | Dismiss sheet |

Implement with `OnBackPressedDispatcher` per-destination callbacks. Predictive back (Android 14+) supported for sheet collapse — requires Material Components ≥ the version that ships predictive-back-enabled bottom sheets (see Appendix C, risk T-6).

### 5.3 Transitions
- **Mini-player → Now Playing:** the mini-player and the sheet live in the **same view hierarchy**, so this is **not** a Material shared-element transition (that framework is for fragment/activity boundaries). Animate the art and chrome via the bottom-sheet drag fraction (Compose `BottomSheetScaffold` progress, or `MotionLayout`/manual interpolation in XML): as the sheet expands, interpolate the 48dp mini art to the large art position. **Rev 1's "shared-element transition" was mechanically wrong here.** See Appendix C, risk T-2.
- Tab switches: instant (no slide), matching GP snappiness.
- Detail open: standard forward push.

---

## 6. Data Display

### 6.1 Source of truth
- Audio via **`MediaStore.Audio`** (`READ_MEDIA_AUDIO` on API 33+; `READ_EXTERNAL_STORAGE` with `maxSdkVersion="32"` below).
- App-owned data (playlists, queue, counts, favorites) in **Room**, keyed by a **stable track identity** — **not** the raw MediaStore `_ID` (which can change on re-index/remount). See §10.3 and Appendix C, risk T-1.
- **Lists are paged with Paging 3** (large libraries — Appendix C, risk T-7).

### 6.2 Library tabs
Tracks · Albums · Artists · Folders (Genres optional) — scrollable underlined tabs under the app bar.

### 6.3 List vs grid

| Content | Layout |
|---------|--------|
| Tracks | Vertical list (§2.3) |
| Albums | 2-col grid: art, title (`Subtitle1`), artist (`Caption`) |
| Artists | List: circular 48dp art, name, "N albums · M tracks" |
| Folders | List: folder icon, name, count |
| Playlists | List: stacked-art thumbnail, name, count |

### 6.4 Sorting & filtering
Per-list sort via modal bottom sheet, persisted per list (DataStore): Title A–Z/Z–A, Date added, Duration, Album, Artist; asc/desc toggle.

### 6.5 Empty / loading / permission states
- No audio → glyph + "No music on this device" + Rescan.
- Permission denied → explanation card + Grant access (routes to request or app settings on permanent deny).
- Loading → shimmer skeleton rows, never a blocking spinner.

---

## 7. Functionality Overview

### 7.1 Must-have (v1)
Offline local playback; background playback via `MediaSessionService` + media notification (lock-screen/notification controls); library browse (tracks/albums/artists/folders); queue (build/reorder/remove/play-next/add); shuffle (on/off); repeat (off/all/one); seek/skip/prev (3s restart rule); search; per-list sort; playlist CRUD; sleep timer; Light/Dark/System; resume last session (queue + position); headset/Bluetooth + audio-focus handling (pause on call, duck on notification, pause on unplug).

### 7.2 Should-have (v1.x)
Favorites quick playlist; Most-played / Recently-added smart playlists; gapless/crossfade (Media3 native); playback speed; equalizer (`audiofx.Equalizer`); folder include/exclude for scan; embedded/`.lrc` lyrics; home-screen widget; Android Auto.

### 7.3 Non-goals (v1)
Streaming, cloud sync, accounts, social, ads, rating prompts.

---

## 8. Responsive Behavior

### 8.1 Orientation
- **Portrait:** Now Playing = expanding **bottom sheet** (§2.2).
- **Landscape:** a bottom sheet cannot sensibly become a two-pane layout (it is bottom-anchored). **Decision:** in landscape, Now Playing expands to a **full-screen two-pane container** (art left, controls right) reached the same way (tap/swipe mini-player), *not* a bottom sheet. This is a real fork in the implementation — see Appendix C, risk T-5.

```
LANDSCAPE Now Playing (full-screen, not a sheet)
┌───────────────────────┬───────────────────────┐
│                       │  Title / Artist        │
│       ALBUM ART       │  ──────●────────        │
│                       │  🔀 ⏮ ▶ ⏭ 🔁           │
│                       │  ☰  🌙  1.0x  ⓘ  ⋮      │
└───────────────────────┴───────────────────────┘
```

### 8.2 Window size classes

| Width | Strategy |
|-------|----------|
| Compact (<600dp) | Bottom nav, single pane, mini-player docked, Now Playing = sheet |
| Medium (600–840dp) | **Navigation rail** replaces bottom nav; 3-col grids |
| Expanded (>840dp) | Rail + persistent two-pane (list + Now Playing side panel); text panes max 640dp |

Edge-to-edge; honor all `WindowInsets`; no content under system bars/cutouts; survive 200% font scale without clipping.

---

## 9. Interaction Patterns

| Action | Context | Result |
|--------|---------|--------|
| Tap | Track row | Play; set queue to that list's context |
| Tap / swipe up | Mini-player | Expand Now Playing |
| Swipe ←/→ | Mini-player or art | Next / previous |
| Swipe down / drag handle | Now Playing sheet | Collapse |
| Long-press | Track row | Multi-select (contextual bar: add to playlist/queue, delete, share) |
| Long-press | Prev/Next | Continuous rewind/forward seek |
| Drag handle | Queue/playlist item | Reorder |
| Swipe | Queue item | Remove + Undo snackbar |
| Pull-to-refresh | Library list | Rescan MediaStore |
| Overflow ⋮ | Any item | Context menu (play next, add to queue/playlist, go to album/artist, details, delete) |

- Material ripple on every interactive surface (bounded for rows/buttons, unbounded for icon-only).
- Light haptic on mode toggles and reorder drop; respect system haptics setting.
- Destructive actions → Undo snackbar (reversible) or confirmation dialog.

---

## 10. State Management

### 10.1 Playback state model
Single source of truth = Media3 `Player`, surfaced via `PlaybackStateFlow`:

```
data class PlaybackUiState(
    val current: TrackUi?,        // null = nothing loaded
    val queue: List<TrackUi>,
    val queueIndex: Int,
    val isPlaying: Boolean,
    val positionMs: Long,
    val durationMs: Long,
    val shuffle: Boolean,
    val repeat: RepeatMode,       // OFF | ALL | ONE
    val speed: Float,
    val sleepTimerEndMs: Long?,
)
```

State machine:

```
   IDLE ──load──► PREPARED ──play──► PLAYING ⇄(pause/play) PAUSED
    ▲                                   │ end of queue (repeat OFF)
    └────────── stop / clear queue ─────┴──────────► STOPPED
   PLAYING completion: next track | restart (repeat ONE) | wrap (repeat ALL) | STOPPED (last + OFF)
```

Position emits at 250–500ms **only while PLAYING and screen visible**; stop the ticker in `onStop` (battery).

### 10.2 Queue behavior
- Building from a list sets the queue to that list's current sort order from the tapped index.
- Play-next inserts after current; add-to-queue appends.
- **Shuffle** keeps an original-order list + a shuffled index map; toggling off restores natural order without losing the current track. **Editing the queue while shuffled** (add/remove/reorder) must keep both representations consistent — this is the trickiest correctness area; covered explicitly in tests (Appendix C, risk T-4).
- Reorder/remove apply live; removing the current track advances to the next.

### 10.3 Persistence & restoration
- Persist on every queue/position change (debounced ~2s) **and** on pause/`onStop` (so position isn't lost on backgrounding): queue track identities, index, position, shuffle, repeat, speed.
- **Track identity is a stable key, not the raw MediaStore `_ID`.** Store `{content URI + file path + size + date_added}` and re-resolve against MediaStore on load; if a URI no longer resolves, mark the queue/playlist entry missing and skip it. This survives re-index/remount. (Appendix C, risk T-1.)
- Cold start restores into **PAUSED** (never auto-play) and shows the mini-player.
- Settings via DataStore.

### 10.4 Service lifecycle
- `MediaSessionService` runs foreground while playing; declare `foregroundServiceType="mediaPlayback"` (manifest) and satisfy Android 12+/14 FGS launch rules.
- UI binds via `MediaController` (async `ListenableFuture`); handle the pre-connection window (§2.1).
- Audio focus: transient → duck/pause; permanent → pause + abandon.

---

## 11. Development Stages & Testing

Build in order. **Each stage's exit criteria must pass before the next.** Performance gates name a **baseline device** (define one mid-tier device, e.g. a Pixel 6a-class phone) and a **percentile** so they are falsifiable.

### Stage 0 — Scaffold
Create modules; configure Gradle catalog, Hilt, Media3, Room, Coroutines, Navigation, Paging 3. Theming (light/dark, type scale). Single-Activity NavHost + 3-item bottom nav with empty screens.
**Exit:** builds + launches on API 26 and API 35; nav switches 3 empty screens; theme follows system light/dark; lint + placeholder test green in CI.

### Stage 1 — Core Playback Engine (no real UI)
Media3 ExoPlayer in `MediaSessionService`; play a hardcoded file; expose `PlaybackUiState`; play/pause/seek/next/prev; audio focus + becoming-noisy; media notification.
**Exit:** file plays/pauses/resumes/seeks (±200ms); background playback + notification controls work; pause on unplug; pause/duck on call; **unit tests:** queue advance, repeat OFF/ALL/ONE, prev 3s rule, shuffle index map; LeakCanary clean on service stop.

### Stage 2 — Library Data Layer
MediaStore enumeration → repo → domain models; runtime permission flow; Room schema (playlists/queue/counts/favorites) with **stable-identity keys**; sort/grouping use cases; Paging 3 sources.
**Exit:** grant/deny/permanent-deny paths handled; tracks/albums/artists/folders counts match a seeded set; rescan reflects added/removed files; **unit tests:** comparators, grouping, identity resolution; **DB tests:** Room migrations + playlist CRUD; **regression test:** simulate MediaStore re-index that changes `_ID` → playlist/queue entries still resolve (risk T-1); cold-start restore returns the persisted queue.

### Stage 3 — UI Implementation
Build Library (tabs/grids), Detail, Search, Activity (Queue/Playlists/History/Favorites tabs), Settings per §2–6. Persistent mini-player + Now Playing **bottom sheet** with drag-fraction animation (not shared-element). Wire to Flows.
**Exit:** visual QA vs spec (4dp scale, centered titles, underlined tabs, targets ≥48dp, light/dark); mini-player↔sheet animation holds **P90 frame time ≤ 16ms on the baseline device** (Macrobenchmark); controls reflect engine state live; seekbar drag pauses auto-update and seeks on release; search debounced + correct; empty/loading/permission states render; **UI tests:** tap→play→mini-player appears, expand/collapse, tab switch, sort sheet; **a11y:** TalkBack labels all controls, 200% font scale, contrast WCAG AA; pre-connection mini-player state shows no flicker/crash (risk T-3).

### Stage 4 — Feature Completion
Playlist CRUD + reorder; queue editing (drag reorder, swipe-remove + Undo, play-next/add); shuffle/repeat UI; sleep timer; resume-last-session; multi-select. Should-haves behind flags as time allows.
**Exit:** playlist CRUD round-trips Room and survives restart; queue edits behave per §10.2 (remove-current advances; **shuffle toggle and queue-edit-while-shuffled preserve current track** — risk T-4); sleep timer stops at set time (+ optional fade); **integration test:** album→shuffle→reorder→remove-current→verify state; destructive ops confirm or Undo.

### Stage 5 — Responsive & Configuration
Landscape Now Playing **full-screen two-pane** (the explicit fork, risk T-5); navigation rail for medium/expanded; tablet grids; inset/cutout/IME handling; predictive back wired for sheet collapse (risk T-6).
**Exit:** rotate during playback → no interruption, no state loss, layout adapts (and Now Playing switches sheet↔two-pane cleanly); verified on phone portrait/landscape, foldable folded/unfolded, 7"/10" tablets; split-screen consistent; nothing clipped under bars/cutouts.

### Stage 6 — Final End-to-End & Edge Cases

**Happy path:** fresh install → grant → scan → browse album → play → background → notification controls → return → edit queue → add to playlist → sleep timer → kill → relaunch → session restored.

**Edge cases (must test):**
- Empty / 1-track / very large (10k+) library: scan time, scroll perf (Paging), memory.
- Corrupt/unsupported file → skip + toast, no crash.
- File deleted by another app while queued/playing → handle missing identity, advance.
- **MediaStore re-index changes IDs** → playlists/queue survive (risk T-1).
- Zero/unknown duration; missing art; missing/garbled tags (unicode, RTL, very long titles).
- Permission revoked while app open.
- Rapid control spamming (mash next/prev/play) → no race/ANR.
- Audio focus storms (back-to-back calls/notifications).
- Bluetooth connect/disconnect mid-playback; headset button single/double/triple = play-pause/next/prev.
- Low memory → process death during playback → restore.
- Battery: position ticker stops with screen off; no wakelock leak.
- Runtime dark/light + dynamic-color toggle.
- Locale change / RTL mirrors correctly.

**Release gates:** crash-free validated; no StrictMode disk/network-on-main; Baseline Profile for startup + Now Playing; Accessibility Scanner no critical issues; all prior suites green in CI.

---

## Appendix A — Module / Package Sketch

```
:domain   models/ repository/(interfaces) usecase/
:data     mediastore/ db/(Room) playback/(Media3, MediaSessionService, focus)
          repository/(impl) settings/(DataStore)
:app      ui/library ui/nowplaying ui/queue ui/search ui/activity ui/settings ui/detail
          ui/components(MiniPlayer, TrackRow, TransportControls, Seekbar)
          ui/theme navigation/ di/
```

## Appendix B — Key Dependencies

| Concern | Library |
|---------|---------|
| Playback | `androidx.media3:media3-exoplayer`, `media3-session`, `media3-ui` |
| DI | `com.google.dagger:hilt-android` |
| DB | `androidx.room:room-runtime`, `room-ktx` |
| Lists | `androidx.paging:paging-runtime` (+ compose) |
| Async | `kotlinx-coroutines-android` |
| Nav | `androidx.navigation:navigation-compose` |
| Images | `io.coil-kt:coil` (MediaStore + embedded-art loaders) |
| Prefs | `androidx.datastore:datastore-preferences` |
| Color | `androidx.palette:palette-ktx` (optional) |
| Test | JUnit, Turbine, MockK, Compose-test/Espresso, Macrobenchmark, LeakCanary |

> **Pin current stable versions at Stage 0** — Media3 and Material move fast; do not trust hardcoded version numbers from memory.

## Appendix C — Stress Test & Resolved Risks

This plan was deliberately attacked. Below are the failure points, severity, and the decision taken. Items already folded into the spec are marked **[fixed]**; items needing a human call are marked **[decide]**.

### Technical risks

**T-1 — MediaStore IDs are not stable. [fixed]**
Keying playlists/queue on `MediaStore._ID` looks fine until a re-index, SD-card remount, or OS media rescan reassigns IDs — then every playlist silently points at the wrong song or nothing. *Resolution:* store a composite identity (URI + path + size + date_added), re-resolve on load, mark unresolved entries missing. Added a Stage-2 regression test and a Stage-6 edge case.

**T-2 — "Shared-element transition into the Now Playing sheet" was mechanically wrong. [fixed]**
Material shared-element transitions cross fragment/activity boundaries; the mini-player and an in-layout bottom sheet are the *same* hierarchy, so that API doesn't apply. *Resolution:* drive the art/chrome animation from the sheet's drag fraction (Compose `BottomSheetScaffold` progress or MotionLayout). Rev 1 would have sent a developer down a dead end.

**T-3 — MediaController binds asynchronously. [fixed]**
On cold start the UI can render before the controller connects to the service, risking a flicker or NPE in the mini-player. *Resolution:* explicit neutral pre-connection state (§2.1), tested in Stage 3.

**T-4 — Queue editing while shuffled is the highest-correctness-risk feature. [fixed in test plan]**
Maintaining original-order + shuffle-map consistency through add/remove/reorder is where music players commonly ship bugs (current track lost, duplicates, wrong "next"). *Resolution:* called out in §10.2 and given a dedicated Stage-4 integration test. Recommend leaning on Media3's built-in shuffle order where possible rather than hand-rolling.

**T-5 — Bottom sheet vs landscape two-pane is a genuine fork. [fixed]**
A bottom sheet can't become a side-by-side two-pane layout cleanly. *Resolution:* §8.1 now specifies Now Playing is a **sheet in portrait, full-screen two-pane in landscape** — two layouts, one entry point. This is real extra work; it's now scoped into Stage 5 rather than discovered late.

**T-6 — Predictive back + bottom sheet needs a recent Material version. [decide]**
Predictive back for sheet dismissal (Android 14+) only works with a sufficiently new Material Components/Compose version and explicit wiring. *Decision needed:* confirm the version at Stage 0, or drop predictive back to a nice-to-have. Not a blocker.

**T-7 — Large libraries will OOM or jank without paging. [fixed]**
Rev 1 had no paging; loading 10k+ tracks into a list plus Coil art requests thrashes memory and frames. *Resolution:* Paging 3 added to architecture, dependencies, and Stage-2/Stage-6.

**T-8 — Compose-vs-XML was left ambiguous. [fixed]**
Rev 1 wrote "Compose/XML" and "Espresso/Compose-test" — mixing toolkits mid-build is a real cost. *Resolution:* §1.1 commits to Compose; the choice is reversible only at Stage 0.

### Product / IA risks (from the 3-nav constraint)

**D-1 — Two screens each carry their own tab row (Library: Tracks/Albums/…; Activity: Queue/Playlists/…). [decide]**
Tabs-within-a-tabbed-shell adds navigation depth and can disorient. GP accepts this, so we mirror it, but watch it in usability testing. If it confuses, the fallback is to surface Albums/Artists as shelves on the Library home rather than peer tabs.

**D-2 — "Queue" and "Playlists" living together under Activity conflates transient session state with durable user data. [decide]**
Queue is ephemeral (this session's order); playlists are saved forever; history is derived. Grouping them as Activity tabs is GP-faithful but conceptually loose. Acceptable for v1; revisit if users report losing playlists or confusing the two.

**D-3 — Settings discoverability. [fixed, with caveat]**
A music player's settings (EQ, gapless, scan folders, theme, speed defaults) are heavier than GP's, and we moved them behind a top-bar gear because the nav is capped at 3 and there's no account. *Caveat:* a gear icon is more discoverable than a Google avatar would be; keep it as a literal gear, not an avatar, so users find it.

**D-4 — Podcast metaphors that don't map to music. [fixed]**
Removed download actions (files are local), subscribe "+" (no subscriptions), and skip −10/+30 transport (replaced with prev/next/shuffle/repeat). Each is now an explicit, labeled deviation in §2 so a developer doesn't "restore GP fidelity" by mistake.

### Testing-plan risks

**Q-1 — Vague performance gates. [fixed]**
"Smooth, no jank" is unfalsifiable. Gates now name a baseline device and P90 frame budget (Stage 3).

**Q-2 — No test covered the T-1 ID-stability bug. [fixed]**
Added explicit Stage-2 regression and Stage-6 edge case.

### Residual open decisions (summary for the human)
1. **T-6** predictive back — confirm Material version at Stage 0 or defer.
2. **D-1 / D-2** — accept GP-style nested tabs and Activity grouping for v1, or restructure. Recommendation: ship as specced, validate in Stage 3 a11y/usability, adjust in v1.1.
3. Landscape effort (**T-5**) is non-trivial — confirm it's in scope for v1 or defer landscape Now Playing to "mini-player only" in landscape.

---

*End of specification.*
