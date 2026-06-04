/* WAVELY v2 — app controller + root phone component */

function wvDefaults() {
  return {
    themePref: 'dark', dynamicColor: true,
    route: 'songs', lastTab: 'songs', detailId: null,
    overlay: null, np: 'mini',
    playingId: WAVELY_DATA.nowPlayingId, isPlaying: true, position: 4,
    shuffle: false, repeat: 'one', speed: 1, sleep: null,
    favorites: new Set(WAVELY_DATA.favorites),
    queue: [...WAVELY_DATA.queue],
    searchQuery: '', searchFocus: false,
    sortKey: 'date', sortDir: 'desc',
    selectMode: false, selected: new Set(),
    overflowId: null, overflowY: 120,
    playlists: [...WAVELY_DATA.playlists],
    snackbar: null,
  };
}
const wvResolved = (s) => (s.themePref === 'system' ? 'dark' : s.themePref);
const SPEEDS = [1, 1.25, 1.5, 2, 0.75];

function wvSortedLibrary(s) {
  const arr = [...WAVELY_DATA.library];
  if (s.sortKey === 'title') arr.sort((a, b) => a.title.localeCompare(b.title));
  else if (s.sortKey === 'duration') arr.sort((a, b) => a.durSec - b.durSec);
  // 'date' = source order
  if (s.sortDir === 'desc') arr.reverse();
  return arr;
}

// shared method-builder over a setState fn
function wvMethods(get, set) {
  const merge = (p) => set(prev => ({ ...prev, ...(typeof p === 'function' ? p(prev) : p) }));
  return {
    setState: merge,
    go(route) { merge(s => ({ route, lastTab: ['songs', 'search', 'playlists'].includes(route) ? route : s.lastTab, overlay: null, selectMode: false, selected: new Set() })); },
    openDetail(id) { merge({ route: 'detail', detailId: id }); },
    play(id) { merge({ playingId: id, isPlaying: true, position: 4 }); },
    togglePlay() { merge(s => ({ isPlaying: !s.isPlaying })); },
    next() { merge(s => { const ids = wvSortedLibrary(s).map(t => t.id); const i = ids.indexOf(s.playingId); return { playingId: ids[(i + 1) % ids.length], position: 0, isPlaying: true }; }); },
    prev() { merge(s => { const ids = wvSortedLibrary(s).map(t => t.id); const i = ids.indexOf(s.playingId); return { playingId: ids[(i - 1 + ids.length) % ids.length], position: 0, isPlaying: true }; }); },
    toggleShuffle() { merge(s => ({ shuffle: !s.shuffle })); },
    cycleRepeat() { merge(s => ({ repeat: s.repeat === 'off' ? 'all' : s.repeat === 'all' ? 'one' : 'off' })); },
    cycleSpeed() { merge(s => ({ speed: SPEEDS[(SPEEDS.indexOf(s.speed) + 1) % SPEEDS.length] })); },
    toggleFav(id) { merge(s => { const f = new Set(s.favorites); f.has(id) ? f.delete(id) : f.add(id); return { favorites: f, snackbar: f.has(id) ? 'Added to Favorites' : 'Removed from Favorites' }; }); },
    enterSelect(id) { merge({ selectMode: true, selected: new Set([id]) }); },
    toggleSelect(id) { merge(s => { const sel = new Set(s.selected); sel.has(id) ? sel.delete(id) : sel.add(id); return { selected: sel }; }); },
    exitSelect() { merge({ selectMode: false, selected: new Set() }); },
    open(overlay) { merge({ overlay }); },
    close() { merge({ overlay: null }); },
    openOverflow(id, e) {
      let y = 120;
      try { const r = e.currentTarget.closest('.wv-screen').getBoundingClientRect(); y = e.clientY - r.top + 8; } catch (_) {}
      merge({ overlay: 'overflow', overflowId: id, overflowY: y });
    },
    expandNP() { merge({ np: 'full' }); },
    collapseNP() { merge({ np: 'mini' }); },
    setSearch(v) { merge({ searchQuery: v }); },
    setTheme(pref) { merge({ themePref: pref }); },
    toggleDynamic() { merge(s => ({ dynamicColor: !s.dynamicColor })); },
    shufflePlay(ids) { if (ids && ids.length) merge({ playingId: ids[0], isPlaying: true, shuffle: true, position: 0 }); },
    createPlaylist(name) { name = (name || '').trim(); if (!name) { merge({ overlay: null }); return; } merge(s => ({ playlists: [...s.playlists, { id: 'p' + Date.now(), name, trackIds: [] }], overlay: null, snackbar: 'Playlist created' })); },
    snack(msg) { merge({ snackbar: msg }); },
    sortedLibrary() { return wvSortedLibrary(get()); },
  };
}

// live hook
function useWavely(initial) {
  const [s, set] = React.useState(() => ({ ...wvDefaults(), ...(initial || {}) }));
  const ref = React.useRef(s); ref.current = s;
  const app = React.useMemo(() => ({ ...wvMethods(() => ref.current, set), interactive: true }), []);
  app.s = s;
  return app;
}

// frozen app for canvas (read methods work; mutators are no-ops)
function makeStaticApp(patch) {
  const s = { ...wvDefaults(), ...(patch || {}) };
  if (patch && patch.favorites) s.favorites = patch.favorites;
  const noop = () => {};
  const app = { s, interactive: false, sortedLibrary: () => wvSortedLibrary(s) };
  ['setState','go','openDetail','play','togglePlay','next','prev','toggleShuffle','cycleRepeat','cycleSpeed','toggleFav','enterSelect','toggleSelect','exitSelect','open','close','openOverflow','expandNP','collapseNP','setSearch','setTheme','toggleDynamic','shufflePlay','createPlaylist','snack']
    .forEach(m => { app[m] = noop; });
  return app;
}

function ScreenBody({ app }) {
  const r = app.s.route;
  if (r === 'permission') return <PermissionScreen app={app} />;
  if (r === 'loading') return <LoadingScreen />;
  if (r === 'songs') return <SongsScreen app={app} />;
  if (r === 'search') return <SearchScreen app={app} />;
  if (r === 'playlists') return <PlaylistsScreen app={app} />;
  if (r === 'detail') return <DetailScreen app={app} />;
  if (r === 'settings') return <SettingsScreen app={app} />;
  return <SongsScreen app={app} />;
}

function OverlayLayer({ app }) {
  switch (app.s.overlay) {
    case 'sort': return <SortSheet app={app} />;
    case 'sleep': return <SleepDialog app={app} />;
    case 'newplaylist': return <NewPlaylistDialog app={app} />;
    case 'addplaylist': return <AddPlaylistSheet app={app} />;
    case 'queue': return <QueueSheet app={app} />;
    case 'overflow': return <OverflowMenu app={app} />;
    default: return null;
  }
}

// full phone — used by prototype (interactive) and canvas (static frac)
function WavelyApp({ app, npFrac }) {
  const s = app.s;
  const resolved = wvResolved(s);
  const showDock = !['settings', 'permission', 'loading'].includes(s.route);
  const hasTrack = !!WAVELY_DATA.byId[s.playingId];

  // simulate playback
  React.useEffect(() => {
    if (!app.interactive || !s.isPlaying) return;
    const t = WAVELY_DATA.byId[s.playingId];
    const iv = setInterval(() => {
      app.setState(p => {
        const tr = WAVELY_DATA.byId[p.playingId];
        if (!tr || !p.isPlaying) return p;
        const np = p.position + p.speed;
        if (np >= tr.durSec) return { ...p, position: 0 };
        return { ...p, position: np };
      });
    }, 1000);
    return () => clearInterval(iv);
  }, [app.interactive, s.isPlaying, s.playingId, s.speed]);

  return (
    <div className={'wv-root theme-' + resolved}>
      <div className={'wv-screen' + (app.interactive ? '' : ' wv-static')}>
        <StatusBar />
        <div className="wv-body"><ScreenBody app={app} /></div>
        {showDock && hasTrack && (
          <div className="wv-dock">
            <MiniPlayer app={app} />
            <BottomNav app={app} />
          </div>
        )}
        {showDock && !hasTrack && <div className="wv-dock"><BottomNav app={app} /></div>}
        {hasTrack && s.route !== 'settings' && <NowPlaying app={app} frac={npFrac} />}
        <OverlayLayer app={app} />
        <Snackbar app={app} />
        <div className="wv-gesture" style={{ position: 'absolute', left: 0, right: 0, bottom: 0, zIndex: 60 }}><i></i></div>
      </div>
    </div>
  );
}

Object.assign(window, { wvDefaults, wvResolved, wvSortedLibrary, useWavely, makeStaticApp, WavelyApp, ScreenBody, OverlayLayer });
