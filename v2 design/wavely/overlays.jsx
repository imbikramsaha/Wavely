/* WAVELY v2 — overlays: Now Playing, Queue, Sort, Sleep, dialogs, overflow, snackbar, permission, loading */

// ---------------- Now Playing (expands from mini) ----------------
function NowPlaying({ app, frac }) {
  const { s } = app;
  const t = WAVELY_DATA.byId[s.playingId];
  if (!t) return null;
  const f = (frac === undefined) ? (s.np === 'full' ? 0 : 1) : frac;
  const drag = React.useRef(null);
  const [dy, setDy] = React.useState(0);

  const onDown = (e) => { drag.current = e.clientY; };
  const onMove = (e) => { if (drag.current != null) setDy(Math.max(0, e.clientY - drag.current)); };
  const onUp = () => { if (drag.current != null) { if (dy > 110) app.collapseNP(); drag.current = null; setDy(0); } };

  const elapsed = WAVELY_DATA.fmt(s.position);
  const remain = '-' + WAVELY_DATA.fmt(t.durSec - s.position);
  const pct = Math.min(100, (s.position / t.durSec) * 100);
  const fav = s.favorites.has(t.id);
  const repIcon = s.repeat === 'one' ? 'repeatOne' : 'repeat';
  const transY = f * 110;

  return (
    <div className="wv-np" style={{
      position: 'absolute', top: 38, left: 0, right: 0, bottom: 0, zIndex: 35,
      background: wvAmbient(t), display: 'flex', flexDirection: 'column',
      transform: `translateY(calc(${transY}% + ${dy}px))`,
      transition: drag.current != null ? 'none' : 'transform .42s cubic-bezier(.22,.9,.27,1)',
      opacity: f > .85 ? 0 : 1,
    }}>
      <div style={{ position: 'absolute', inset: 0, backdropFilter: 'blur(6px)', background: 'linear-gradient(180deg, rgba(8,10,14,.30), rgba(8,10,14,.72))' }}></div>
      <div style={{ position: 'relative', flex: 1, display: 'flex', flexDirection: 'column', padding: '8px 26px 26px' }}>
        {/* grip / collapse */}
        <div style={{ display: 'flex', justifyContent: 'center', padding: '6px 0 4px', cursor: 'grab', touchAction: 'none' }}
          onPointerDown={onDown} onPointerMove={onMove} onPointerUp={onUp} onPointerLeave={onUp}>
          <div className="wv-grip" style={{ background: 'rgba(255,255,255,.5)' }}></div>
        </div>

        {/* artwork */}
        <div style={{ flex: 1, display: 'grid', placeItems: 'center', minHeight: 0, padding: '10px 0' }}>
          <div style={{ width: 'min(290px, 78%)', aspectRatio: '1', position: 'relative' }}>
            <WavelyArt track={t} size={290} radius={26} />
            <div style={{ position: 'absolute', inset: 0, borderRadius: 26, boxShadow: '0 30px 60px -20px rgba(0,0,0,.6)' }}></div>
          </div>
        </div>

        {/* title */}
        <div style={{ color: '#fff', marginBottom: 18 }}>
          <div style={{ fontSize: 21, fontWeight: 700, letterSpacing: '-.01em', lineHeight: 1.25, display: '-webkit-box', WebkitLineClamp: 2, WebkitBoxOrient: 'vertical', overflow: 'hidden' }}>{t.title}</div>
          <div style={{ fontSize: 14, fontWeight: 500, color: 'rgba(255,255,255,.7)', marginTop: 6 }}>{t.artist} · Download</div>
        </div>

        {/* transport */}
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 18 }}>
          <button className="wv-iconbtn" style={{ color: s.shuffle ? 'var(--wv-accent)' : 'rgba(255,255,255,.78)' }} onClick={() => app.toggleShuffle()}><Icon name="shuffle" size={22} /></button>
          <button className="wv-iconbtn" style={{ color: '#fff' }} onClick={() => app.prev()}><Icon name="prev" size={30} /></button>
          <button onClick={() => app.togglePlay()} style={{ width: 72, height: 72, borderRadius: '50%', border: 'none', cursor: 'pointer', background: 'var(--wv-grad)', display: 'grid', placeItems: 'center', color: '#fff', boxShadow: '0 14px 30px -8px rgba(46,120,240,.7)' }}>
            <Icon name={s.isPlaying ? 'pause' : 'play'} size={32} />
          </button>
          <button className="wv-iconbtn" style={{ color: '#fff' }} onClick={() => app.next()}><Icon name="next" size={30} /></button>
          <button className="wv-iconbtn" style={{ color: s.repeat !== 'off' ? 'var(--wv-accent)' : 'rgba(255,255,255,.78)' }} onClick={() => app.cycleRepeat()}><Icon name={repIcon} size={22} /></button>
        </div>

        {/* seekbar */}
        <div style={{ marginBottom: 16 }}>
          <div style={{ position: 'relative', height: 6, borderRadius: 3, background: 'rgba(255,255,255,.2)' }}>
            <div style={{ position: 'absolute', left: 0, top: 0, bottom: 0, width: pct + '%', background: 'var(--wv-grad)', borderRadius: 3 }}></div>
            <div style={{ position: 'absolute', left: `calc(${pct}% - 7px)`, top: '50%', transform: 'translateY(-50%)', width: 14, height: 14, borderRadius: '50%', background: '#fff', boxShadow: '0 2px 6px rgba(0,0,0,.4)' }}></div>
          </div>
          <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: 8, fontSize: 12, fontWeight: 600, color: 'rgba(255,255,255,.72)' }}>
            <span>{elapsed}</span><span>{remain}</span>
          </div>
        </div>

        {/* utility row */}
        <div style={{ display: 'flex', justifyContent: 'space-around', color: 'rgba(255,255,255,.8)' }}>
          <button className="wv-util" onClick={() => app.open('queue')}><Icon name="queue" /><span className="wv-ulabel">Queue</span></button>
          <button className="wv-util" onClick={() => app.open('sleep')} style={s.sleep ? { color: 'var(--wv-accent)' } : null}><Icon name="moon" /><span className="wv-ulabel">{s.sleep ? s.sleep + 'm' : 'Sleep'}</span></button>
          <button className="wv-util" onClick={() => app.cycleSpeed()} style={s.speed !== 1 ? { color: 'var(--wv-accent)' } : null}><span style={{ fontSize: 17, fontWeight: 800, height: 24, display: 'grid', placeItems: 'center' }}>{s.speed}x</span><span className="wv-ulabel">Speed</span></button>
          <button className="wv-util" onClick={() => app.toggleFav(t.id)} style={fav ? { color: 'var(--wv-accent)' } : null}><Icon name={fav ? 'heartFill' : 'heart'} /><span className="wv-ulabel">Favorite</span></button>
        </div>
      </div>
    </div>
  );
}

// ---------------- Queue sheet ----------------
function QueueSheet({ app }) {
  const { s } = app;
  const tracks = s.queue.map(id => WAVELY_DATA.byId[id]).filter(Boolean);
  return (
    <div className="wv-overlay">
      <div className="wv-scrim" onClick={() => app.close()}></div>
      <div className="wv-sheet" style={{ maxHeight: '82%', display: 'flex', flexDirection: 'column' }}>
        <div className="wv-grip"></div>
        <div className="wv-sheet-title" style={{ fontSize: 17, textTransform: 'none', letterSpacing: '-.01em', color: 'var(--wv-text)' }}>Up next</div>
        <div style={{ overflowY: 'auto', margin: '4px -8px 0' }}>
          {tracks.map(t => (
            <div key={t.id} className={'wv-row' + (t.id === s.playingId ? ' is-active' : '')} onClick={() => app.play(t.id)}>
              {t.id === s.playingId ? <ArtPlaying track={t} size={48} radius={12} /> : <WavelyArt track={t} size={48} radius={12} />}
              <div className="wv-meta"><div className="wv-tt" style={{ fontSize: 14.5 }}>{t.title}</div><div className="wv-sub">{t.artist} · {t.dur}</div></div>
              <button className="wv-iconbtn" style={{ cursor: 'grab', color: 'var(--wv-text-3)' }}><Icon name="drag" /></button>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}

// ---------------- Sort sheet ----------------
function SortSheet({ app }) {
  const { s } = app;
  const opts = [['title', 'Title'], ['date', 'Date added'], ['duration', 'Duration']];
  return (
    <div className="wv-overlay">
      <div className="wv-scrim" onClick={() => app.close()}></div>
      <div className="wv-sheet">
        <div className="wv-grip"></div>
        <div className="wv-sheet-title">Sort by</div>
        {opts.map(([id, label]) => (
          <div key={id} className={'wv-opt' + (s.sortKey === id ? ' sel' : '')} onClick={() => app.setState({ sortKey: id })}>
            <div className="wv-radio"></div><span>{label}</span>
          </div>
        ))}
        <div style={{ height: 1, background: 'var(--wv-line)', margin: '8px 0' }}></div>
        <div className="wv-opt" style={{ cursor: 'pointer' }} onClick={() => app.setState({ sortDir: s.sortDir === 'asc' ? 'desc' : 'asc' })}>
          <span style={{ flex: 1 }}>{s.sortDir === 'asc' ? 'Ascending' : 'Descending'}</span>
          <Icon name={s.sortDir === 'asc' ? 'arrowUp' : 'arrowDown'} />
        </div>
        <div style={{ display: 'flex', justifyContent: 'flex-start', marginTop: 8 }}>
          <button className="wv-btn text" onClick={() => app.close()}>Done</button>
        </div>
      </div>
    </div>
  );
}

// ---------------- Sleep timer dialog ----------------
function SleepDialog({ app }) {
  const opts = [15, 30, 45, 60];
  return (
    <div className="wv-overlay">
      <div className="wv-scrim" onClick={() => app.close()}></div>
      <div className="wv-dialog">
        <h3>Sleep timer</h3>
        {opts.map(m => (
          <div key={m} className="wv-opt" style={{ padding: '12px 4px' }} onClick={() => { app.setState({ sleep: m }); app.close(); app.snack(`Sleep timer · ${m} min`); }}>
            <span>{m} minutes</span>
          </div>
        ))}
        <div className="wv-dialog-actions">
          <button className="wv-btn text" onClick={() => { app.setState({ sleep: null }); app.close(); }}>Cancel</button>
        </div>
      </div>
    </div>
  );
}

// ---------------- New / rename playlist dialog ----------------
function NewPlaylistDialog({ app }) {
  const [name, setName] = React.useState('');
  return (
    <div className="wv-overlay">
      <div className="wv-scrim" onClick={() => app.close()}></div>
      <div className="wv-dialog">
        <h3>New playlist</h3>
        <input className="wv-input" autoFocus placeholder="Playlist name" value={name} onChange={(e) => setName(e.target.value)} />
        <div className="wv-dialog-actions">
          <button className="wv-btn text" onClick={() => app.close()}>Cancel</button>
          <button className="wv-btn text" style={{ fontWeight: 800 }} onClick={() => app.createPlaylist(name)}>Create</button>
        </div>
      </div>
    </div>
  );
}

// ---------------- Add to playlist sheet ----------------
function AddPlaylistSheet({ app }) {
  const { s } = app;
  return (
    <div className="wv-overlay">
      <div className="wv-scrim" onClick={() => app.close()}></div>
      <div className="wv-sheet">
        <div className="wv-grip"></div>
        <div className="wv-sheet-title" style={{ fontSize: 17, textTransform: 'none', letterSpacing: '-.01em', color: 'var(--wv-text)' }}>Add to playlist</div>
        <div className="wv-opt" style={{ gap: 16 }} onClick={() => { app.close(); app.snack('Added to Favorites'); }}>
          <div className="wv-art" style={{ width: 40, height: 40, background: 'var(--wv-grad)' }}><Icon name="heartFill" size={20} /></div>
          <span>Favorites</span>
        </div>
        {s.playlists.map(p => (
          <div key={p.id} className="wv-opt" style={{ gap: 16 }} onClick={() => { app.close(); app.snack('Added to ' + p.name); }}>
            <div className="wv-art" style={{ width: 40, height: 40 }}><Icon name="note" size={18} /></div><span>{p.name}</span>
          </div>
        ))}
        <div style={{ height: 1, background: 'var(--wv-line)', margin: '6px 0' }}></div>
        <div className="wv-opt" style={{ gap: 16, color: 'var(--wv-accent)' }} onClick={() => app.open('newplaylist')}>
          <div style={{ width: 40, display: 'grid', placeItems: 'center' }}><Icon name="plus" /></div><span>New playlist</span>
        </div>
      </div>
    </div>
  );
}

// ---------------- Track overflow menu ----------------
function OverflowMenu({ app }) {
  const { s } = app;
  const fav = s.favorites.has(s.overflowId);
  const items = [
    ['playNext', 'Play next', () => app.snack('Playing next')],
    ['addQueue', 'Add to queue', () => app.snack('Added to queue')],
    ['addPlaylist', 'Add to playlist', () => app.open('addplaylist')],
    [fav ? 'heartFill' : 'heart', fav ? 'Remove from favorites' : 'Add to favorites', () => { app.toggleFav(s.overflowId); app.close(); }],
  ];
  return (
    <div className="wv-overlay">
      <div className="wv-scrim" style={{ background: 'transparent' }} onClick={() => app.close()}></div>
      <div className="wv-menu" style={{ top: Math.min(s.overflowY || 120, 540), right: 14 }}>
        {items.map(([icon, label, fn], i) => (
          <div key={i} className="wv-mi" onClick={() => { fn(); if (label !== 'Add to playlist') app.close(); }}>
            <Icon name={icon} /><span>{label}</span>
          </div>
        ))}
      </div>
    </div>
  );
}

// ---------------- snackbar ----------------
function Snackbar({ app }) {
  if (!app.s.snackbar) return null;
  return (
    <div className="wv-snack">
      <b>{app.s.snackbar}</b>
      <button className="wv-btn text" onClick={() => app.setState({ snackbar: null })}>UNDO</button>
    </div>
  );
}

// ---------------- permission / loading (full-screen) ----------------
function PermissionScreen({ app }) {
  return (
    <div className="wv-center" style={{ height: '100%' }}>
      <WavelyMark size={56} />
      <h4 style={{ marginTop: 22 }}>Listen to your library</h4>
      <p>Wavely plays the music stored on your device. Grant access to scan your audio files — nothing leaves your phone.</p>
      <button className="wv-btn primary" style={{ marginTop: 18 }} onClick={() => app && app.go && app.go('songs')}>Grant access</button>
    </div>
  );
}
function LoadingScreen() {
  return (
    <div className="wv-center" style={{ height: '100%' }}>
      <div className="wv-spin"></div>
      <h4 style={{ marginTop: 22 }}>Scanning your library</h4>
      <p>Finding the music on your device…</p>
    </div>
  );
}

Object.assign(window, { NowPlaying, QueueSheet, SortSheet, SleepDialog, NewPlaylistDialog, AddPlaylistSheet, OverflowMenu, Snackbar, PermissionScreen, LoadingScreen });
