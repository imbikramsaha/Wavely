/* WAVELY v2 — art, equalizer, brand mark, icon set */

// ---- title → unique hue (stable hash) -------------------------------
function wvHue(str) {
  let h = 0;
  for (let i = 0; i < str.length; i++) h = (h * 31 + str.charCodeAt(i)) % 360;
  return h;
}
function wvMonogram(t) {
  const m = (t.title || '').replace(/^[^A-Za-z0-9]+/, '');
  // skip the generic "AUD-" recorder prefix → use first letter anyway
  return (m[0] || '♪').toUpperCase();
}

// generated gradient fallback tile (each track a unique hue)
function WavelyArt({ track, size = 52, radius = 14, monogram = true, dim = false }) {
  if (!track) {
    return <div style={{ width: size, height: size, borderRadius: radius, background: 'var(--wv-surface-3)' }} />;
  }
  let g, h;
  if (track.hasArt && track.artColors) {
    const [a, b] = track.artColors;
    g = `linear-gradient(150deg, ${a}, ${b})`;
  } else {
    h = wvHue(track.title);
    g = `linear-gradient(145deg, hsl(${h} 55% 50%), hsl(${(h + 42) % 360} 60% 38%))`;
  }
  const letterSize = Math.round(size * 0.42);
  return (
    <div style={{
      width: size, height: size, borderRadius: radius, position: 'relative',
      background: g, overflow: 'hidden', flex: 'none',
      boxShadow: 'inset 0 1px 0 rgba(255,255,255,.18), inset 0 0 0 1px rgba(0,0,0,.06)',
      opacity: dim ? .5 : 1,
    }}>
      {/* soft sheen */}
      <div style={{ position: 'absolute', inset: 0, background:
        'radial-gradient(120% 80% at 78% 12%, rgba(255,255,255,.28), transparent 60%)' }} />
      {/* faint waveform baseline — music identity */}
      <svg viewBox="0 0 100 40" preserveAspectRatio="none" style={{ position: 'absolute', left: 0, right: 0, bottom: 0, width: '100%', height: '46%', opacity: .28 }}>
        <path d="M0 30 Q 12 8 22 22 T 44 22 T 66 18 T 88 24 T 110 20 V40 H0 Z" fill="rgba(255,255,255,.9)" />
      </svg>
      {monogram && !track.hasArt && (
        <span style={{ position: 'absolute', inset: 0, display: 'grid', placeItems: 'center',
          fontFamily: 'var(--wv-font)', fontWeight: 700, fontSize: letterSize, color: 'rgba(255,255,255,.95)',
          textShadow: '0 1px 6px rgba(0,0,0,.25)', letterSpacing: '-.02em' }}>
          {wvMonogram(track)}
        </span>
      )}
      {track.hasArt && (
        <span style={{ position: 'absolute', inset: 0, display: 'grid', placeItems: 'center', color: 'rgba(255,255,255,.9)' }}>
          <Icon name="note" size={Math.round(size * 0.34)} />
        </span>
      )}
    </div>
  );
}

// big ambient art-derived background for Now Playing
function wvAmbient(track) {
  if (!track) return 'var(--wv-bg)';
  let a, b;
  if (track.hasArt && track.artColors) { [a, b] = track.artColors; }
  else { const h = wvHue(track.title); a = `hsl(${h} 55% 48%)`; b = `hsl(${(h + 42) % 360} 60% 36%)`; }
  return `radial-gradient(120% 70% at 50% 0%, ${a}, transparent 62%),
          radial-gradient(120% 80% at 50% 100%, ${b}, transparent 60%),
          #0A0C10`;
}

// equalizer (active row + mini player)
function Eq({ color }) {
  return <div className="wv-eq" style={color ? { '--wv-eq': color } : undefined}>
    <span></span><span></span><span></span><span></span>
  </div>;
}

// brand mark — 5 bars: small / little-big / big / little-big / small
function WavelyMark({ size = 22, mono }) {
  const u = 'wvgrad' + Math.random().toString(36).slice(2, 7);
  return (
    <svg width={size} height={size * (130 / 136)} viewBox="0 0 136 130" style={{ display: 'block' }}>
      <defs>
        <linearGradient id={u} x1="0" y1="0" x2="1" y2="0">
          <stop offset="0%" stopColor="#2FD3E6" /><stop offset="52%" stopColor="#5B8DF4" /><stop offset="100%" stopColor="#8E7BFF" />
        </linearGradient>
      </defs>
      <g fill={mono || `url(#${u})`}>
        <rect x="0" y="45" width="16" height="40" rx="8" />
        <rect x="30" y="29" width="16" height="72" rx="8" />
        <rect x="60" y="10" width="16" height="110" rx="8" />
        <rect x="90" y="29" width="16" height="72" rx="8" />
        <rect x="120" y="45" width="16" height="40" rx="8" />
      </g>
    </svg>
  );
}

// ---- icon set (custom, consistent 24px stroke) ----------------------
function Icon({ name, size = 24, fill = false }) {
  const s = { width: size, height: size, display: 'block', flex: 'none' };
  const C = { fill: 'none', stroke: 'currentColor', strokeWidth: 2, strokeLinecap: 'round', strokeLinejoin: 'round' };
  const F = { fill: 'currentColor' };
  const wrap = (kids, extra) => <svg viewBox="0 0 24 24" style={s} {...extra}>{kids}</svg>;
  switch (name) {
    case 'shuffle': return wrap(<g {...C}><path d="M16 3h5v5M21 3l-7 7M4 20l5-5M16 21h5v-5M14 14l7 7M3 4l5 5" /></g>);
    case 'sort': return wrap(<g {...C}><path d="M4 6h13M4 12h9M4 18h5" /></g>);
    case 'gear': return wrap(<g {...C}><circle cx="12" cy="12" r="3.2" /><path d="M19.4 13.5a1.7 1.7 0 0 0 .3 1.9l.1.1a2 2 0 1 1-2.8 2.8l-.1-.1a1.7 1.7 0 0 0-2.9 1.2V21a2 2 0 1 1-4 0v-.1A1.7 1.7 0 0 0 7 19.2l-.1.1a2 2 0 1 1-2.8-2.8l.1-.1a1.7 1.7 0 0 0-1.2-2.9H3a2 2 0 1 1 0-4h.1A1.7 1.7 0 0 0 4.8 7l-.1-.1a2 2 0 1 1 2.8-2.8l.1.1a1.7 1.7 0 0 0 1.9.3H10A1.7 1.7 0 0 0 11 3.1V3a2 2 0 1 1 4 0v.1a1.7 1.7 0 0 0 2.9 1.2l.1-.1a2 2 0 1 1 2.8 2.8l-.1.1a1.7 1.7 0 0 0-.3 1.9V10a1.7 1.7 0 0 0 1.6 1H21a2 2 0 1 1 0 4h-.1a1.7 1.7 0 0 0-1.5 1z" /></g>);
    case 'search': return wrap(<g {...C}><circle cx="11" cy="11" r="7" /><path d="M21 21l-4.3-4.3" /></g>);
    case 'queue-nav': return wrap(<g {...C}><path d="M3 6h12M3 12h12M3 18h8" /><path d="M17 13v6.5" /><circle cx="20" cy="19" r="2.2" {...F} stroke="none" /></g>);
    case 'note': return wrap(<g><path d="M9 17.5V7l9-2v8.5" {...C} /><circle cx="6.5" cy="17.5" r="2.6" {...F} /><circle cx="15.5" cy="13.5" r="2.6" {...F} /></g>);
    case 'heart': return wrap(<path d="M12 20s-7-4.6-9.3-9C1.2 8 2.6 4.8 5.8 4.8c2 0 3.3 1.2 4.2 2.5.9-1.3 2.2-2.5 4.2-2.5 3.2 0 4.6 3.2 3.1 6.2C19 15.4 12 20 12 20z" {...C} />);
    case 'heartFill': return wrap(<path d="M12 20s-7-4.6-9.3-9C1.2 8 2.6 4.8 5.8 4.8c2 0 3.3 1.2 4.2 2.5.9-1.3 2.2-2.5 4.2-2.5 3.2 0 4.6 3.2 3.1 6.2C19 15.4 12 20 12 20z" {...F} />);
    case 'more': return wrap(<g {...F}><circle cx="12" cy="5" r="1.9" /><circle cx="12" cy="12" r="1.9" /><circle cx="12" cy="19" r="1.9" /></g>);
    case 'play': return wrap(<path d="M7 4.5v15l13-7.5z" {...F} />);
    case 'pause': return wrap(<g {...F}><rect x="6" y="4.5" width="4.2" height="15" rx="1.4" /><rect x="13.8" y="4.5" width="4.2" height="15" rx="1.4" /></g>);
    case 'next': return wrap(<g {...F}><path d="M6 5.5v13l9-6.5z" /><rect x="16" y="5" width="2.6" height="14" rx="1.3" /></g>);
    case 'prev': return wrap(<g {...F}><path d="M18 5.5v13l-9-6.5z" /><rect x="5.4" y="5" width="2.6" height="14" rx="1.3" /></g>);
    case 'repeat': return wrap(<g {...C}><path d="M17 2l3 3-3 3" /><path d="M4 11V9a4 4 0 0 1 4-4h12" /><path d="M7 22l-3-3 3-3" /><path d="M20 13v2a4 4 0 0 1-4 4H4" /></g>);
    case 'repeatOne': return wrap(<g {...C}><path d="M17 2l3 3-3 3" /><path d="M4 11V9a4 4 0 0 1 4-4h12" /><path d="M7 22l-3-3 3-3" /><path d="M20 13v2a4 4 0 0 1-4 4H4" /><text x="12" y="14.5" fontSize="7" fontWeight="800" fill="currentColor" stroke="none" textAnchor="middle" fontFamily="var(--wv-font)">1</text></g>);
    case 'moon': return wrap(<path d="M20 14.5A8 8 0 1 1 9.5 4a6.3 6.3 0 0 0 10.5 10.5z" {...C} />);
    case 'queue': return wrap(<g {...C}><path d="M3 6h12M3 12h12M3 18h8" /><path d="M17 12l5 3-5 3z" fill="currentColor" stroke="none" /></g>);
    case 'plus': return wrap(<path d="M12 5v14M5 12h14" {...C} />);
    case 'back': return wrap(<g {...C}><path d="M20 12H4M10 6l-6 6 6 6" /></g>);
    case 'close': return wrap(<path d="M6 6l12 12M18 6L6 18" {...C} />);
    case 'check': return wrap(<path d="M5 12.5l4.5 4.5L19 7" {...C} />);
    case 'drag': return wrap(<g {...C}><path d="M5 9h14M5 15h14" /></g>);
    case 'playNext': return wrap(<g {...C}><path d="M4 6h9M4 12h9M4 18h6" /><path d="M16 8l5 4-5 4z" fill="currentColor" stroke="none" /></g>);
    case 'addQueue': return wrap(<g {...C}><path d="M3 6h11M3 12h11M3 18h7" /><path d="M18 14v6M15 17h6" /></g>);
    case 'addPlaylist': return wrap(<g {...C}><path d="M3 7h12M3 12h8M3 17h8" /><path d="M17 14v7M13.5 17.5h7" /></g>);
    case 'chevR': return wrap(<path d="M9 6l6 6-6 6" {...C} />);
    case 'speed': return wrap(<g {...C}><path d="M5 19a9 9 0 1 1 14 0" /><path d="M12 13l4-4" /></g>);
    case 'arrowDown': return wrap(<path d="M12 5v14M6 13l6 6 6-6" {...C} />);
    case 'arrowUp': return wrap(<path d="M12 19V5M6 11l6-6 6 6" {...C} />);
    case 'clock': return wrap(<g {...C}><circle cx="12" cy="12" r="8.5" /><path d="M12 7.5V12l3 2" /></g>);
    case 'wifi': return wrap(<g {...F}><path d="M12 18.5a1.6 1.6 0 1 0 0-3.2 1.6 1.6 0 0 0 0 3.2z" /><path d="M5 11.5a10 10 0 0 1 14 0l-1.6 1.6a7.7 7.7 0 0 0-10.8 0z" /></g>);
    case 'signal': return wrap(<g {...F}><rect x="3" y="13" width="3" height="6" rx="1" /><rect x="8" y="9" width="3" height="10" rx="1" /><rect x="13" y="5.5" width="3" height="13.5" rx="1" /></g>);
    case 'battery': return wrap(<g><rect x="3" y="7" width="16" height="10" rx="2.5" {...C} /><rect x="5" y="9" width="10" height="6" rx="1" {...F} /><rect x="20" y="10" width="2" height="4" rx="1" {...F} /></g>);
    default: return wrap(<circle cx="12" cy="12" r="8" {...C} />);
  }
}

Object.assign(window, { WavelyArt, wvAmbient, Eq, WavelyMark, Icon, wvHue, wvMonogram });
