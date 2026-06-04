/* WAVELY v2 — data model (from the real screenshots) */
(function () {
  // duration "m:ss" → seconds
  const D = (s) => { const p = s.split(':').map(Number); return p.length === 3 ? p[0]*3600+p[1]*60+p[2] : p[0]*60+p[1]; };

  // id, title, artist, durationLabel, hasArt(optional real art color cue)
  const T = (id, title, artist, dur) => ({ id, title, artist, dur, durSec: D(dur) });

  const library = [
    T('t01', 'AUD-20260530-WA0001', 'Unknown artist', '0:43'),
    T('t02', 'AUD-20260425-WA0002', 'Unknown artist', '0:13'),
    T('t03', 'Why should it take time', 'Unknown artist', '0:41'),
    T('t04', 'AUD-20260406-WA0004', 'Unknown artist', '0:16'),
    T('t05', 'Naga Naga Nagendraya  Naga Pratishtha Chant by Sadhguru', 'Unknown artist', '58:04'),
    T('t06', 'AUD-20260309-WA0008', 'Unknown artist', '1:28'),
    T('t07', 'Shiv Kailasho Ke Vasi | Sadhguru Sings with Mohit Chauhan', 'Sounds of Isha', '4:51'),
    T('t08', 'Kalabhairava Chant | Sadhguru | Sounds of Isha #shiva', 'Sounds of Isha', '59:49'),
    T('t09', 'Blossoming of Life Uyirnokkam #Sadhguru', 'Sadhguru - My Path To Shiva Shambho', '2:14'),
    T('t10', 'Namah Aum Namah Shivaya | Mahashivratri 2026 | Devotional Shiva Song #soundsofisha #meditation', 'Sounds of Isha', '8:42'),
    T('t11', 'AUD-20260214-WA0020', 'Unknown artist', '0:05'),
    T('t12', 'AUD-20260131-WA0000', 'Unknown artist', '2:08'),
    T('t13', 'Jagadeesha Sudheesha | Full Song - Lyrics & Meaning', 'Sounds of Isha', '11:31'),
    T('t14', 'Namami Shamishan', 'Unknown artist', '5:24'),
    T('t15', 'Yoga Yoga Yogeshwaraya  1 Hour  Adiyogi Shiva', 'Unknown artist', '1:00:42'),
    T('t16', 'Yoga Yoga Yogeshwaraya by Sadhguru 2023', 'Unknown artist', '5:08'),
    T('t17', 'Yogeshwaraya Mahadevaya  Sadhguru and Sounds of Isha', 'Unknown artist', '3:50'),
    T('t18', 'Yogi Hai Tu  Sadhguru  Guru Purnima 2021', 'Unknown artist', '4:35'),
    T('t19', 'Sounds Of Isha - Kalabhairavashtakam | Trigunatmaka', 'Sounds of Isha', '7:05'),
  ];

  // tracks that genuinely have cover art in the source app (use richer dual-tone art)
  const withArt = { t07: ['#3b5a7a', '#9fb8cf'], t08: ['#b3401d', '#f0a838'] };
  library.forEach(t => { if (withArt[t.id]) { t.hasArt = true; t.artColors = withArt[t.id]; } });

  const byId = Object.fromEntries(library.map(t => [t.id, t]));

  // 12 favorites
  const favorites = ['t14','t15','t16','t17','t18','t19','t13','t10','t08','t07','t09','t05'];

  // search "shiva" results
  const searchResults = [
    T('s1', 'Aum Namah Shivaya 2023  Vairagya Reprise', 'Unknown artist', '5:01'),
    T('s2', 'Dedo Prabhu  Adiyogi Shiva Bhajan  Chandrajeet', 'Unknown artist', '6:18'),
    T('s3', 'Hey Shivaya Shankaraya  SoundsofIsha  Mahashivratri', 'Unknown artist', '5:06'),
    T('s4', 'Om Namaste Astu Bhagwan Mantra Status', 'Unknown artist', '0:18'),
    T('s5', 'Rudrashtakam  Shiva  Sounds of Isha  In the Lotus', 'Unknown artist', '6:16'),
    T('s6', 'Shiva Stotra  Shiva Panchakshara Stotra with Lyrics', 'Unknown artist', '4:47'),
    T('s7', 'Shiva Tandava Stotram  Original Powerful  Best', 'Unknown artist', '6:30'),
    T('s8', 'Shiva Tandava Stotram  Powerful Shiva Aradhana', 'Unknown artist', '6:22'),
  ];

  window.WAVELY_DATA = {
    library, byId, favorites, searchResults,
    nowPlayingId: 't10',
    queue: ['t01','t02','t03','t04','t05','t06','t07','t08','t10','t11','t12'],
    playlists: [], // user playlists (none yet — matches "No playlists yet")
    fmt(sec) { sec = Math.max(0, Math.round(sec)); const h = Math.floor(sec/3600), m = Math.floor((sec%3600)/60), s = sec%60;
      return h ? `${h}:${String(m).padStart(2,'0')}:${String(s).padStart(2,'0')}` : `${m}:${String(s).padStart(2,'0')}`; },
  };
})();
