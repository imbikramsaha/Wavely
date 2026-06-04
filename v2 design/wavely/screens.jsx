/* WAVELY v2 — screens: Songs, Search, Playlists, Detail, Settings, multi-select bar */

function BrandTopBar({ app, actions }) {
  return (
    <TopBar>
      <div className="wv-brandrow" style={{ flex: 1 }}>
        <WavelyMark size={24} />
        <span className="wv-wordmark">Wavely</span>
      </div>
      {actions}
    </TopBar>
  );
}

function MultiSelectBar({ app }) {
  const n = app.s.selected.size;
  return (
    <TopBar style={{ background: 'var(--wv-surface-2)' }}>
      <button className="wv-iconbtn" onClick={() => app.exitSelect()}><Icon name="close" /></button>
      <span className="wv-title" style={{ flex: 1, marginLeft: 4 }}>{n} selected</span>
      <button className="wv-iconbtn" onClick={() => app.snack('Playing next')}><Icon name="playNext" /></button>
      <button className="wv-iconbtn" onClick={() => app.snack('Added to queue')}><Icon name="queue-nav" /></button>
      <button className="wv-iconbtn" onClick={() => app.open('addplaylist')}><Icon name="plus" /></button>
    </TopBar>
  );
}

function SongsScreen({ app }) {
  const { s } = app;
  return (
    <React.Fragment>
      {s.selectMode ? <MultiSelectBar app={app} /> : (
        <BrandTopBar app={app} actions={
          <React.Fragment>
            <button className={'wv-iconbtn' + (s.shuffle ? ' ' : '')} style={s.shuffle ? { color: 'var(--wv-accent)' } : null} onClick={() => app.toggleShuffle()}><Icon name="shuffle" /></button>
            <button className="wv-iconbtn" onClick={() => app.open('sort')}><Icon name="sort" /></button>
            <button className="wv-iconbtn" onClick={() => app.go('settings')}><Icon name="gear" /></button>
          </React.Fragment>
        } />
      )}
      <div className="wv-list">
        {app.sortedLibrary().map(t => (
          <TrackRow key={t.id} track={t} app={app} active={t.id === s.playingId} />
        ))}
        <div className="wv-bottom-pad"></div>
      </div>
    </React.Fragment>
  );
}

function SearchScreen({ app }) {
  const { s } = app;
  const q = s.searchQuery.trim().toLowerCase();
  const results = q
    ? [...WAVELY_DATA.searchResults, ...WAVELY_DATA.library].filter(t => t.title.toLowerCase().includes(q))
    : [];
  return (
    <React.Fragment>
      <TopBar>
        <span className="wv-title" style={{ flex: 1, textAlign: 'center', paddingLeft: 44 }}>Search</span>
        <button className="wv-iconbtn" onClick={() => app.go('settings')}><Icon name="gear" /></button>
      </TopBar>
      <div className={'wv-search-field' + (s.searchFocus ? ' focus' : '')}>
        <Icon name="search" size={20} />
        <input value={s.searchQuery} placeholder="Search songs"
          onChange={(e) => app.setSearch(e.target.value)}
          onFocus={() => app.setState({ searchFocus: true })}
          onBlur={() => app.setState({ searchFocus: false })} />
        {s.searchQuery && <button className="wv-iconbtn" style={{ width: 32, height: 32 }} onClick={() => app.setSearch('')}><Icon name="close" size={18} /></button>}
      </div>
      {q === '' ? (
        <div className="wv-center">
          <div className="wv-emoji" style={{ background: 'var(--wv-surface-2)', color: 'var(--wv-text-3)' }}><Icon name="search" size={40} /></div>
          <h4>Search songs</h4>
          <p>Find anything in your library by title or artist.</p>
        </div>
      ) : (
        <div className="wv-list">
          {results.map(t => <TrackRow key={t.id} track={t} app={app} active={t.id === s.playingId} />)}
          <div className="wv-bottom-pad"></div>
        </div>
      )}
    </React.Fragment>
  );
}

function PlaylistsScreen({ app }) {
  const { s } = app;
  return (
    <React.Fragment>
      <TopBar>
        <span className="wv-title" style={{ flex: 1, textAlign: 'center', paddingLeft: 88 }}>Playlists</span>
        <button className="wv-iconbtn" onClick={() => app.open('newplaylist')}><Icon name="plus" /></button>
        <button className="wv-iconbtn" onClick={() => app.go('settings')}><Icon name="gear" /></button>
      </TopBar>
      <div className="wv-list">
        <div className="wv-row" onClick={() => app.openDetail('favorites')}>
          <div className="wv-art" style={{ background: 'var(--wv-grad)' }}><Icon name="heartFill" size={26} /></div>
          <div className="wv-meta">
            <div className="wv-tt">Favorites</div>
            <div className="wv-sub">{s.favorites.size} tracks</div>
          </div>
          <Icon name="chevR" size={20} />
        </div>
        <div style={{ borderTop: '1px solid var(--wv-line)', margin: '8px 8px 4px' }}></div>
        {s.playlists.length === 0 ? (
          <div style={{ padding: '34px 24px', textAlign: 'center', color: 'var(--wv-text-2)', fontSize: 14, fontWeight: 500 }}>
            No playlists yet.<br />Tap + to create one.
          </div>
        ) : s.playlists.map(p => (
          <div key={p.id} className="wv-row" onClick={() => app.openDetail(p.id)}>
            <div className="wv-art" style={{ background: `linear-gradient(145deg, hsl(${wvHue(p.name)} 55% 50%), hsl(${(wvHue(p.name)+42)%360} 60% 38%))` }}><Icon name="note" size={22} /></div>
            <div className="wv-meta"><div className="wv-tt">{p.name}</div><div className="wv-sub">{p.trackIds.length} tracks</div></div>
            <Icon name="chevR" size={20} />
          </div>
        ))}
        <div className="wv-bottom-pad"></div>
      </div>
    </React.Fragment>
  );
}

function DetailScreen({ app }) {
  const { s } = app;
  const isFav = s.detailId === 'favorites';
  const pl = isFav ? null : s.playlists.find(p => p.id === s.detailId);
  const title = isFav ? 'Favorites' : (pl ? pl.name : 'Playlist');
  const ids = isFav ? [...s.favorites] : (pl ? pl.trackIds : []);
  const tracks = ids.map(id => WAVELY_DATA.byId[id]).filter(Boolean);
  return (
    <React.Fragment>
      <TopBar>
        <button className="wv-iconbtn" onClick={() => app.go('playlists')}><Icon name="back" /></button>
        <span className="wv-title" style={{ flex: 1, marginLeft: 4 }}>{title}</span>
        <button className="wv-iconbtn" onClick={() => app.shufflePlay(ids)}><Icon name="shuffle" /></button>
      </TopBar>
      <div className="wv-list">
        {tracks.map((t, i) => (
          <div key={t.id} className={'wv-row' + (t.id === s.playingId ? ' is-active' : '')} onClick={() => app.play(t.id)} style={{ paddingRight: isFav ? 8 : 4 }}>
            {t.id === s.playingId ? <ArtPlaying track={t} size={52} radius={14} /> : <WavelyArt track={t} size={52} radius={14} />}
            <div className="wv-meta"><div className="wv-tt">{t.title}</div><div className="wv-sub">{t.artist} · {t.dur}</div></div>
            {isFav
              ? <button className="wv-iconbtn" onClick={(e) => { e.stopPropagation(); app.toggleFav(t.id); }} style={{ color: 'var(--wv-accent)' }}><Icon name="heartFill" /></button>
              : <button className="wv-iconbtn" style={{ cursor: 'grab', color: 'var(--wv-text-3)' }}><Icon name="drag" /></button>}
          </div>
        ))}
        {tracks.length === 0 && <div style={{ padding: 40, textAlign: 'center', color: 'var(--wv-text-2)' }}>No tracks yet.</div>}
        <div className="wv-bottom-pad"></div>
      </div>
    </React.Fragment>
  );
}

function SettingsScreen({ app }) {
  const { s } = app;
  const themes = [['light', 'Light'], ['dark', 'Dark'], ['system', 'System']];
  return (
    <React.Fragment>
      <TopBar>
        <button className="wv-iconbtn" onClick={() => app.go(app.s.lastTab || 'songs')}><Icon name="back" /></button>
        <span className="wv-title" style={{ flex: 1, marginLeft: 4 }}>Settings</span>
      </TopBar>
      <div style={{ overflowY: 'auto' }}>
        <div className="wv-section" style={{ color: 'var(--wv-accent)' }}>Theme</div>
        <div style={{ padding: '0 12px' }}>
          {themes.map(([id, label]) => (
            <div key={id} className={'wv-opt' + (s.themePref === id ? ' sel' : '')} onClick={() => app.setTheme(id)}>
              <div className="wv-radio"></div><span>{label}</span>
            </div>
          ))}
        </div>
        <div style={{ height: 1, background: 'var(--wv-line)', margin: '14px 20px' }}></div>
        <div className="wv-opt" style={{ padding: '16px 24px', cursor: 'pointer' }} onClick={() => app.toggleDynamic()}>
          <span style={{ flex: 1, fontSize: 16, fontWeight: 600 }}>Dynamic color</span>
          <div style={{ width: 52, height: 31, borderRadius: 999, background: s.dynamicColor ? 'var(--wv-accent)' : 'var(--wv-surface-3)', position: 'relative', transition: 'background .2s', border: s.dynamicColor ? 'none' : '2px solid var(--wv-line-2)' }}>
            <div style={{ position: 'absolute', top: '50%', transform: 'translateY(-50%)', left: s.dynamicColor ? 24 : 4, width: 23, height: 23, borderRadius: '50%', background: s.dynamicColor ? 'var(--wv-on-accent)' : 'var(--wv-text-2)', transition: 'left .2s' }}></div>
          </div>
        </div>
        <div style={{ textAlign: 'center', padding: '60px 0 30px', color: 'var(--wv-text-3)' }}>
          <div style={{ display: 'flex', justifyContent: 'center', marginBottom: 12 }}><WavelyMark size={30} /></div>
          <div style={{ fontWeight: 700, color: 'var(--wv-text-2)' }}>Wavely 1.0</div>
          <div style={{ fontSize: 13, marginTop: 4, display: 'flex', gap: 5, justifyContent: 'center', alignItems: 'center' }}>Built by Bikram with <span style={{ color: 'var(--wv-accent)', display: 'inline-flex' }}><Icon name="heartFill" size={14} /></span></div>
        </div>
      </div>
    </React.Fragment>
  );
}

Object.assign(window, { BrandTopBar, MultiSelectBar, SongsScreen, SearchScreen, PlaylistsScreen, DetailScreen, SettingsScreen });
