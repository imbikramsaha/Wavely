/* WAVELY v2 — shared chrome: status bar, gesture, topbar, track row, mini-player, bottom nav */

function StatusBar() {
  return (
    <div className="wv-status">
      <span>9:41</span>
      <div className="wv-punch"></div>
      <div className="wv-status-icons">
        <Icon name="signal" size={16} />
        <Icon name="wifi" size={16} />
        <Icon name="battery" size={18} />
      </div>
    </div>
  );
}
function Gesture() { return <div className="wv-gesture"><i></i></div>; }

function TopBar({ children, style }) {
  return <div className="wv-topbar" style={style}>{children}</div>;
}

// active-row / mini art with equalizer overlay
function ArtPlaying({ track, size, radius }) {
  return (
    <div style={{ position: 'relative', width: size, height: size, flex: 'none' }}>
      <WavelyArt track={track} size={size} radius={radius} monogram={false} dim />
      <div style={{ position: 'absolute', inset: 0, display: 'grid', placeItems: 'center',
        background: 'rgba(0,0,0,.28)', borderRadius: radius }}>
        <Eq color="#fff" />
      </div>
    </div>
  );
}

function TrackRow({ track, app, active, secondary }) {
  const { s } = app;
  const selected = s.selected.has(track.id);
  const press = React.useRef(null);

  const down = () => { press.current = setTimeout(() => { app.enterSelect(track.id); press.current = 'done'; }, 420); };
  const up = () => { if (press.current && press.current !== 'done') { clearTimeout(press.current); press.current = null; } };
  const click = () => {
    if (press.current === 'done') { press.current = null; return; }
    if (s.selectMode) app.toggleSelect(track.id);
    else app.play(track.id);
  };

  const sub = secondary || `${track.artist} · ${track.dur}`;
  return (
    <div className={'wv-row' + (active ? ' is-active' : '')}
      onClick={click} onPointerDown={down} onPointerUp={up} onPointerLeave={up}>
      {s.selectMode
        ? <div className={'wv-check' + (selected ? ' on' : '')}>{selected && <Icon name="check" />}</div>
        : (active ? <ArtPlaying track={track} size={52} radius={14} /> : <WavelyArt track={track} size={52} radius={14} />)}
      <div className="wv-meta">
        <div className="wv-tt">{track.title}</div>
        <div className="wv-sub">{sub}</div>
      </div>
      {!s.selectMode &&
        <div className="wv-trailing">
          <button className="wv-iconbtn" onClick={(e) => { e.stopPropagation(); app.openOverflow(track.id, e); }}>
            <Icon name="more" />
          </button>
        </div>}
    </div>
  );
}

function MiniPlayer({ app }) {
  const { s } = app;
  const t = WAVELY_DATA.byId[s.playingId];
  if (!t) return null;
  const pct = Math.min(100, (s.position / t.durSec) * 100);
  return (
    <div className="wv-mini" onClick={() => app.expandNP()}>
      <div className="wv-prog" style={{ width: pct + '%' }}></div>
      {s.isPlaying ? <ArtPlaying track={t} size={44} radius={11} /> : <WavelyArt track={t} size={44} radius={11} />}
      <div className="wv-meta" style={{ marginRight: 4 }}>
        <div className="wv-tt" style={{ fontSize: 14.5 }}>{t.title}</div>
        <div className="wv-sub" style={{ fontSize: 12 }}>{t.artist} · Download</div>
      </div>
      <button className="wv-iconbtn" onClick={(e) => { e.stopPropagation(); app.togglePlay(); }}>
        <Icon name={s.isPlaying ? 'pause' : 'play'} />
      </button>
      <button className="wv-iconbtn" onClick={(e) => { e.stopPropagation(); app.next(); }}>
        <Icon name="next" />
      </button>
    </div>
  );
}

function BottomNav({ app }) {
  const { s } = app;
  const items = [
    { id: 'songs', label: 'Songs', icon: 'note' },
    { id: 'search', label: 'Search', icon: 'search' },
    { id: 'playlists', label: 'Playlists', icon: 'queue-nav' },
  ];
  const cur = (s.route === 'detail') ? 'playlists' : (s.route === 'settings' ? null : s.route);
  return (
    <div className="wv-nav">
      {items.map(it => (
        <button key={it.id} className={'wv-navitem' + (cur === it.id ? ' on' : '')} onClick={() => app.go(it.id)}>
          <div className="wv-pill"><Icon name={it.icon} /></div>
          <span className="wv-nlabel">{it.label}</span>
        </button>
      ))}
    </div>
  );
}

Object.assign(window, { StatusBar, Gesture, TopBar, ArtPlaying, TrackRow, MiniPlayer, BottomNav });
