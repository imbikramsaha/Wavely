# Listn — Offline Android Music Player

An offline, local-library music player built to the spec in [`design.md`](design.md).
Kotlin · Jetpack Compose · Media3 (ExoPlayer) · Room · Hilt · Paging 3.

This repository currently implements **Stages 0–3** of the build plan in `design.md §11`
(scaffold → playback engine → library data layer → UI). Stages 4–6 (advanced queue
editing UI, responsive/landscape two-pane, full edge-case hardening) are scoped but not
yet built — see *Status* below.

## Modules

```
:domain   Pure Kotlin. Models, repository interfaces, use cases (no Android imports).
:data     MediaStore scanning, Room DB, Media3 MediaSessionService + MediaController,
          repository impls, DataStore settings, Hilt wiring.
:app      Compose UI: single Activity + NavHost, 3-tab bottom nav, mini-player,
          Now Playing sheet, Library/Search/Activity/Detail/Settings, ViewModels.
```

## Building

No Android toolchain is bundled with this checkout. To build:

1. Install **JDK 17** and **Android Studio** (Ladybug or newer) with **SDK API 35**.
2. Open the project folder in Android Studio. It will generate the Gradle wrapper jar
   and sync automatically.
   - From the CLI instead: ensure a system `gradle` (8.11+) is installed, run
     `gradle wrapper` once to create `gradle/wrapper/gradle-wrapper.jar`, then
     `./gradlew :app:assembleDebug`.
3. Run on a device/emulator with **API 26–35**. Grant the audio-read permission when asked.

Useful tasks:

```
./gradlew :app:assembleDebug      # build the APK
./gradlew test                    # JVM unit tests (domain + data)
./gradlew :app:installDebug       # install on a connected device
```

## Architecture highlights (mapping to design.md)

- **Stable track identity** (`§10.3`, risk **T-1**): playlists/queue/favorites are keyed by
  `{path + size + date_added}` (`TrackIdentity`), re-resolved against MediaStore on load —
  never the mutable MediaStore `_ID`. Regression-tested in
  `data/src/test/.../MediaRepositoryImplTest.kt`.
- **Playback** (`§10.1/§10.4`): `PlaybackService : MediaSessionService` owns the ExoPlayer
  (audio focus + becoming-noisy enabled); the UI drives it through `PlaybackController`
  (a `MediaController` wrapper, `PlaybackConnection`) and never touches Media3 directly.
  The pre-connection window is surfaced as `isConnecting` (risk **T-3**).
- **Shuffle** (`§10.2`, risk **T-4**): uses Media3's built-in shuffle order rather than a
  hand-rolled index map.
- **Now Playing sheet** (`§5.3`, risk **T-2**): the mini-player and sheet live in one
  hierarchy; expansion is driven by a **drag fraction** (`MainScreen`), not a shared-element
  transition.
- **Large libraries** (`§6.1`, risk **T-7**): lists are Paging 3 (`ListPagingSource` over an
  in-memory snapshot).
- **Position ticker** (`§10.1`): runs only while playing AND the UI is foreground
  (`LifecycleAware` → `setUiActive`), to spare battery.

## Dependency versions

The catalog (`gradle/libs.versions.toml`) is pinned to a **known-good, internally
consistent set** (AGP 8.7.3 / Gradle 8.11.1 / Kotlin 2.1.0 / KSP 2.1.0-1.0.29 / Compose
BOM 2024.12.01 / Hilt 2.53.1 / Room 2.6.1 / Media3 1.5.1) rather than the absolute latest.
This set uses **KSP1** (Room 2.6.1 does not support KSP2 — that needs Room 2.7.0+), and
avoids the breaking changes in the AGP 9.x / Gradle 9.x / Compose 1.11 line that can't be
validated without building.

To move to current stable later (Android Studio will flag updates): bump Room to 2.8.x and
re-enable KSP2 (`ksp.useKSP2=true` in `gradle.properties`) **together**, switch
`fallbackToDestructiveMigration()` to the `dropAllTables = true` overload, and step AGP +
Gradle + Kotlin + KSP up in lockstep (each Kotlin release has a matching KSP build).

## Status

Implemented (Stages 0–3):
- Multi-module scaffold, Hilt DI, light/dark/system theme + dynamic color toggle.
- ExoPlayer playback: play/pause/seek/next/prev (3s restart rule), shuffle, repeat,
  speed, audio focus, media notification, background playback.
- MediaStore scan → tracks/albums/artists/folders, per-list sort (DataStore), search,
  permission flow, Room schema (playlists/favorites/history/counts/session).
- UI: Library (4 tabs), Search, Activity (Queue/Playlists/History/Favorites), Detail,
  Settings, persistent mini-player, drag-expanding Now Playing sheet, sleep timer.
- Session save (debounced) + cold-start restore into PAUSED.

Implemented (Stage 4):
- Track overflow context menu (play next, add to queue, add to playlist, go to album/artist,
  favorite) + add-to-playlist dialog with inline create.
- Queue editing: long-press **drag-reorder**, **swipe-to-remove with Undo** snackbar.
  Remove-current advances and shuffle/edit-while-shuffled preserve the current track — these
  correctness guarantees (risk **T-4**) are delegated to Media3's built-in queue + shuffle order.
- Playlist management: create / rename / delete (delete confirmed), playlist-detail
  drag-reorder + remove-track-with-Undo. Round-trips Room.
- Multi-select on the Library Tracks tab (long-press → contextual bar: play next / add to
  queue / add to playlist).

Implemented (Stage 5 — responsive, design.md §8):
- Width size classes (Compact / Medium / Expanded at 600/840dp, computed via `BoxWithConstraints`).
- **Compact**: bottom nav. **Medium/Expanded**: navigation rail replaces it. Album grid scales
  2 → 3 → 4 columns.
- Now Playing fork (risk **T-5**): portrait = expanding bottom sheet; landscape = full-screen
  two-pane (art left, controls right); **Expanded** width = persistent Now Playing side panel
  (no sheet). `NowPlayingControls` is shared across all three.
- **Predictive back** collapses the sheet/overlay via `PredictiveBackHandler` (risk **T-6**).
- Rotation preserves the open/closed Now Playing state (`rememberSaveable`); playback itself is
  unaffected (it lives in the service).
- Insets: edge-to-edge + status/navigation-bar padding per region + `displayCutoutPadding` on the
  rail and landscape panes; IME via `adjustResize`.

Implemented (Stage 6 — hardening, the code-level edge cases from design.md §11):
- **Corrupt / unsupported / deleted-while-playing track** → `onPlayerError` skips to the next
  item and emits a toast ("Can't play … — skipping"); no crash/stall.
- **Permission revoked while running** → MediaStore queries catch `SecurityException` and return
  an empty library instead of crashing.
- **Process death / low-memory restore** → session is saved debounced AND immediately on
  backgrounding (`persistNow()`), restored into PAUSED on next launch.
- **StrictMode** in debug builds (disk/network-on-main + leaked-closeable/SQLite detection) — the
  release gate against main-thread I/O.
- Audio focus, becoming-noisy, and media-button handling are delegated to Media3 (ExoPlayer
  `handleAudioFocus` + `MediaSession`); the position ticker already stops when backgrounded.

Verified earlier by construction: zero/unknown duration (clamped), missing art (placeholder),
long/RTL tags (ellipsis + `supportsRtl` + auto-mirrored icons), MediaStore-id change (T-1 test).

Still device-only (can't run here — see `design.md` §11 release gates):
- Macrobenchmark **P90 ≤ 16ms** frame gate, **Baseline Profile** generation, **Accessibility
  Scanner** pass, LeakCanary verification, and the full instrumented happy-path E2E.
- Manual edge cases needing hardware: Bluetooth connect/disconnect, headset single/double/triple
  tap, audio-focus storms, real 10k-track scroll/memory.

Remaining product gaps:
- Multi-select only on the Library Tracks tab; device-file deletion intentionally omitted
  (only library-membership edits).
- String externalization for the newer screens (some literals inline pending l10n).

Standing caveat (all stages): gesture/animation/feel code and the responsive layouts were written
without an on-device run. The logic is correct by construction; thresholds, transitions, and
inset/cutout behavior on real hardware will need a tuning pass once it builds.

## Tests

JVM unit tests (run with `gradle test`):
- `domain`: `PreviousTrackRuleTest`, `TrackSortingTest`, `RepeatModeTest`, `TrackIdentityTest`
  (asKey/fromKey round-trip + T-1 id-independence).
- `data`: `MediaRepositoryImplTest` (T-1 identity-stability regression + missing-file drop).
- `app`: `FormatTest` (duration/remaining/secondary-line), `NextSpeedTest` (speed cycle).

CI: `.github/workflows/ci.yml` runs `gradle test` + `gradle lint` on every push/PR (JDK 17,
pinned Gradle 8.11.1 so it doesn't need a committed wrapper jar).
