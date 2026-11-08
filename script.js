const SB_URL='https://qlzwqbbvjscfjaicbhks.supabase.co',SB_KEY='sb_publishable_DRIOqI7O90TinECl4K6FJA_l0nIaPCY',HDR={'apikey':SB_KEY,'Authorization':'Bearer '+SB_KEY};
let games=[],categories=[],stats=[],curGame=null,settings={},currentRating=0,favSet=new Set(),statMap={},currentSearchQuery='',searchTimer=null,carouselInterval=null,toastTimer=null,isChatLoaded=false,currentReviewFilter=0;
const $=(id)=>document.getElementById(id);
const enc=(s)=>encodeURIComponent(s);
const num=(n)=>Number(n)||0;

function saveCache(k,d){try{localStorage.setItem('bx_cache_'+k,JSON.stringify(d))}catch(e){}}
function getCache(k){try{const d=localStorage.getItem('bx_cache_'+k);return d?JSON.parse(d):null}catch(e){return null}}
function hexToRgb(h){let c=h.replace('#','');if(c.length===3)c=c.split('').map(x=>x+x).join('');const r=parseInt(c.substring(0,2),16),g=parseInt(c.substring(2,4),16),b=parseInt(c.substring(4,6),16);return isNaN(r)?{r:168,g:85,b:247}:{r,g,b}}
function applyAccent(h){const c=safeColor(h,'#A855F7');const rgb=hexToRgb(c);const root=document.documentElement.style;root.setProperty('--accent',c);root.setProperty('--accent-soft',`rgba(${rgb.r},${rgb.g},${rgb.b},0.1)`);root.setProperty('--accent-border',`rgba(${rgb.r},${rgb.g},${rgb.b},0.3)`);root.setProperty('--accent-glow',`rgba(${rgb.r},${rgb.g},${rgb.b},0.4)`)}
function gantiURL(p){if(window.location.pathname!==p)window.history.pushState({},'',p)}

window.addEventListener('popstate',function(){
    const p=window.location.pathname;
    if(p==='/'||p===''||p==='/index.html'){showPage('home')}
    else if(p.startsWith('/game/')||p.startsWith('/apps/')){
        const id=p.split('/')[2];
        if(games.length===0){ initHome().then(()=>openDetail(id)); } else { openDetail(id); }
    }
    else if(['/search','/chat','/request','/about'].includes(p)){showPage(p.replace('/',''))}
    else{showPage('home')}
});

function showToast(m,e=false){const t=$('toast');if(toastTimer)clearTimeout(toastTimer);t.innerHTML=(e?'❌ ':'✅ ')+escapeHTML(m);t.className=e?'err show':'show';toastTimer=setTimeout(()=>{t.className=''},3000)}
function toggleTheme(){const isDark=document.documentElement.classList.contains('dark');if(isDark){document.documentElement.classList.remove('dark');localStorage.setItem('theme','light')}else{document.documentElement.classList.add('dark');localStorage.setItem('theme','dark')}}
function toggleSidebar(){const s=$('sidebar'),b=$('sidebarBg');if(!s||!b)return;s.classList.toggle('show');b.classList.toggle('show')}
function escapeHTML(s){if(s===null||s===undefined)return'';return String(s).replace(/[&<>'"]/g,function(t){return{'&':'&amp;','<':'&lt;','>':'&gt;',"'":'&#39;','"':'&quot;'}[t]})}
function safeUrl(u){if(!u)return'';try{const p=new URL(u,window.location.origin);if(['http:','https:'].includes(p.protocol))return p.href}catch(e){}return''}
function safeColor(c,f){if(!c)return f||'#A855F7';const s=String(c).trim();if(/^#([0-9A-Fa-f]{3}|[0-9A-Fa-f]{6}|[0-9A-Fa-f]{8})$/.test(s))return s;return f||'#A855F7'}
function showPage(p){document.querySelectorAll('.page').forEach(el=>{el.classList.remove('active');el.classList.add('hidden')});document.querySelectorAll('.bnav-item,.sb-item').forEach(el=>el.classList.remove('active'));const target=$('page-'+p);if(target){target.classList.remove('hidden');target.classList.add('active')}document.querySelectorAll(`[data-nav="${p}"]`).forEach(el=>el.classList.add('active'));if(p!=='home')window.scrollTo(0,0);if(p==='search'){renderSearchSuggestions();renderSearchHistory();renderExploreCategories();const sw=$('searchSuggestionsWrap');if(sw)sw.style.display='block';if(currentSearchQuery)handleSearchPage(currentSearchQuery,true)}if(p==='chat'){if(!isChatLoaded)reloadChat()}if(p==='home'){gantiURL('/');setTimeout(()=>window.scrollTo(0,parseInt(sessionStorage.getItem('scrollPos')||0)),80)}else if(p!=='detail'){gantiURL('/'+p)}}
function getFp(){try{let salt=localStorage.getItem('bx_fp_salt');if(!salt){salt=Math.random().toString(36).slice(2)+Date.now().toString(36);localStorage.setItem('bx_fp_salt',salt)}const p=[navigator.userAgent,navigator.language,screen.width+'x'+screen.height,(Intl.DateTimeFormat().resolvedOptions().timeZone),salt];const raw=p.join('|');let h=5381;for(let i=0;i<raw.length;i++){h=((h<<5)-h)+raw.charCodeAt(i);h|=0}return'F'+Math.abs(h).toString(36)}catch(e){return'F_fallback'}}
async function fetchWithTimeout(url,opts,ms=8000){const c=new AbortController();const id=setTimeout(()=>c.abort(),ms);try{return await fetch(url,{...opts,signal:c.signal})}finally{clearTimeout(id)}}
async function sbGet(p){try{const r=await fetchWithTimeout(SB_URL+'/rest/v1/'+p,{headers:HDR});if(!r.ok)return[];return await r.json()}catch(e){return[]}}
async function sbPost(p,b,h={}){try{const r=await fetchWithTimeout(SB_URL+'/rest/v1/'+p,{method:'POST',headers:{...HDR,'Content-Type':'application/json','Prefer':'return=representation',...h},body:JSON.stringify(b)});return r.ok}catch(e){return false}}
async function sbDel(p){try{const r=await fetchWithTimeout(SB_URL+'/rest/v1/'+p,{method:'DELETE',headers:HDR});return r.ok}catch(e){return false}}
function getStat(k){return statMap[k]||0}
function formatNumber(n){n=num(n);if(n>=1000000)return(n/1000000).toFixed(1).replace('.0','')+'M';if(n>=1000)return(n/1000).toFixed(1).replace('.0','')+'K';return n.toString()}
function placeholder(w){return`data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='${w||150}' height='${w||150}'%3E%3Crect width='100%25' height='100%25' fill='%231C1C1E'/%3E%3C/svg%3E`}

function renderCard(g,isFav){
    const img=safeUrl(g.img)||placeholder(150);
    const badge=g.badge_text?`<span class="badge" style="background:var(--accent)">${escapeHTML(g.badge_text)}</span>`:'';
    return`<div class="card" data-id="${escapeHTML(g.game_id)}"><img src="${escapeHTML(img)}" class="card-img" loading="lazy" alt="">${badge}<button class="btn-fav ${isFav?'active':''}" data-fav="${escapeHTML(g.game_id)}"><svg fill="${isFav?'currentColor':'none'}" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z"/></svg></button><div class="card-body"><h4 class="card-title truncate">${escapeHTML(g.title)}</h4><div class="card-sub truncate">${escapeHTML(g.developer||'Unknown')}</div><div class="card-meta"><span class="flex items-center gap-1"><svg style="width:12px;height:12px;" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/></svg> ${formatNumber(getStat(g.game_id))}</span><span class="flex items-center gap-1" style="color:#eab308;"><svg style="width:12px;height:12px;" fill="currentColor" viewBox="0 0 24 24"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg> ${escapeHTML(g.rating_avg||'0.0')}</span></div></div></div>`
}
function renderSearchResultCard(g){const img=safeUrl(g.img)||placeholder(150);return`<div class="card" style="display:flex;align-items:center;padding:12px;gap:12px;margin-bottom:8px;border-radius:16px;" data-id="${escapeHTML(g.game_id)}"><img src="${escapeHTML(img)}" style="width:64px;height:64px;border-radius:14px;object-fit:cover;flex-shrink:0;" alt="" loading="lazy"><div style="flex:1;min-width:0;"><h4 style="font-size:14px;font-weight:900;margin-bottom:2px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;">${escapeHTML(g.title)}</h4><div style="font-size:11px;color:var(--text-muted);margin-bottom:4px;">${escapeHTML(g.developer||'Unknown')} • ${escapeHTML(g.category||'App')}</div><div style="display:flex;gap:12px;font-size:11px;font-weight:700;color:var(--text-muted);"><span style="color:#eab308;">★ ${escapeHTML(g.rating_avg||'0.0')}</span><span>${formatNumber(getStat(g.game_id))} Download</span></div></div></div>`}
function renderEmptyState(c,m){const el=$(c);if(el)el.innerHTML=`<div class="empty-state"><svg fill="none" stroke="currentColor" stroke-width="1.5" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4"/></svg><p>${escapeHTML(m)}</p></div>`}

async function loadFavs(){try{const f=await sbGet('favorites?fp_hash=eq.'+enc(getFp())+'&select=game_key');if(Array.isArray(f)){favSet=new Set(f.map(x=>x.game_key));localStorage.setItem('bx_favs',JSON.stringify([...favSet]))}else{throw new Error('Offline')}}catch(e){try{const c=JSON.parse(localStorage.getItem('bx_favs')||'[]');favSet=new Set(c)}catch(_){}}}
async function toggleFav(id,el){const isFav=favSet.has(id);const fp=getFp();if(isFav){const ok=await sbDel(`favorites?game_key=eq.${enc(id)}&fp_hash=eq.${enc(fp)}`);if(!ok){showToast('Failed to remove favorite',true);return}favSet.delete(id);el.classList.remove('active');el.querySelector('svg').setAttribute('fill','none')}else{const ok=await sbPost('favorites',{game_key:id,fp_hash:fp});if(!ok){showToast('Failed to save favorite',true);return}favSet.add(id);el.classList.add('active');el.querySelector('svg').setAttribute('fill','currentColor')}localStorage.setItem('bx_favs',JSON.stringify([...favSet]))}
function buildWatermark(){const wm=$('watermark');if(!wm||wm.dataset.built)return;let h='';for(let i=0;i<12;i++)h+='<span>BXIT MY ID</span>';wm.innerHTML=h;wm.dataset.built='1'}

async function initHome(){
    buildWatermark();
    const mg=$('mainGrid');if(mg)mg.innerHTML='<div class="skeleton"></div><div class="skeleton"></div><div class="skeleton"></div><div class="skeleton"></div><div class="skeleton"></div><div class="skeleton"></div>';
    try{
        const results = await Promise.allSettled([
            sbGet('web_settings?select=*'),
            sbGet('games?is_active=eq.true&order=created_at.desc&limit=1000'),
            sbGet('game_reviews?select=game_id,rating'),
            sbGet('game_categories?select=*&order=name.asc'),
            sbGet('stats?select=game_key,count&stat_type=eq.download')
        ]);
        const settRows = results[0].status==='fulfilled'?results[0].value:[];
        const gamesData = results[1].status==='fulfilled'?results[1].value:[];
        const allRevs = results[2].status==='fulfilled'?results[2].value:[];
        const catsData = results[3].status==='fulfilled'?results[3].value:[];
        const statsData = results[4].status==='fulfilled'?results[4].value:[];

        settings={};(settRows||[]).forEach(s=>settings[s.key]=s.value);
        saveCache('settings',settings);saveCache('games',gamesData);saveCache('cats',catsData);saveCache('stats',statsData);
        
        document.querySelectorAll('.sb-title').forEach(el=>el.innerText=settings.site_title||'BXIT ID');
        if(settings.about_text)$('abText1').innerText=settings.about_text;
        if(settings.footer_text)$('abText2').innerText=settings.footer_text;
        if(settings.wa_channel){$('abWa').href=settings.wa_channel;$('sbWa').href=settings.wa_channel}
        if(settings.tg_channel){$('abTg').href=settings.tg_channel;$('sbTg').href=settings.tg_channel}
        $('sbIg').href=settings.ig_channel||'#';$('sbYt').href=settings.yt_channel||'#';$('sbTt').href=settings.tt_channel||'#';
        if(settings.accent_color)applyAccent(settings.accent_color);
        $('searchPageInput').placeholder=settings.search_placeholder||'Search apps or games...';
        $('welTitle').innerText=settings.welcome_title||'WELCOME!';
        $('welSub').innerText=settings.welcome_subtitle||'FREE TRIAL ACTIVE: ALL MODS INCLUDED';
        $('welWa').innerText=settings.welcome_btn1_text||'WhatsApp Community';
        $('welWa').href=settings.welcome_btn1_link||settings.wa_channel||'#';
        $('welTg').innerText=settings.welcome_btn2_text||'Telegram Community';
        $('welTg').href=settings.welcome_btn2_link||settings.tg_channel||'#';
        
        const showCarousel=String(settings.show_carousel)!=='false';
        if(!showCarousel){const cc=$('carouselContainer');if(cc)cc.style.display='none'}
        
        games=(gamesData||[]).map(g=>({...g,rating_avg:'0.0'}));
        const ratingMap={};(allRevs||[]).forEach(r=>{if(!ratingMap[r.game_id])ratingMap[r.game_id]=[];ratingMap[r.game_id].push(num(r.rating))});
        games.forEach(g=>{const arr=ratingMap[g.game_id]||[];g.rating_avg=arr.length?(arr.reduce((a,b)=>a+b,0)/arr.length).toFixed(1):'0.0'});
        
        categories=catsData||[];
        const statAgg={};(statsData||[]).forEach(r=>{statAgg[r.game_key]=(statAgg[r.game_key]||0)+num(r.count)});
        stats=Object.keys(statAgg).map(k=>({game_key:k,download:statAgg[k]}));
        statMap={};stats.forEach(s=>statMap[s.game_key]=s.download);
        
        await loadFavs();
        if(showCarousel)await renderCarousel();
        renderFilters();
        if(games.length===0)renderEmptyState('mainGrid','No games available.');
        else { mg.innerHTML=games.map(g=>renderCard(g,favSet.has(g.game_id))).join(''); bindCardEvents(); }
        
        if(String(settings.welcome_active)==='true'){setTimeout(()=>{$('welcomeModal').style.display='flex'},1500)}
    }catch(e){
        console.warn("Offline mode / Fetch failed. Using cache.");
        const cGames=getCache('games'),cCats=getCache('cats'),cStats=getCache('stats'),cSett=getCache('settings');
        if(cSett){
            if(cSett.accent_color)applyAccent(cSett.accent_color);
            if(cSett.show_carousel==='false'){const cc=$('carouselContainer');if(cc)cc.style.display='none'}
            if(String(cSett.welcome_active)==='true'){setTimeout(()=>{$('welcomeModal').style.display='flex'},1500)}
            if(cSett.site_title)document.querySelectorAll('.sb-title').forEach(el=>el.innerText=cSett.site_title);
        }
        if(cGames){
            games=cGames.map(g=>({...g,rating_avg:'0.0'}));
            statMap={};
            (cStats||[]).forEach(r=>{statMap[r.game_key]=(statMap[r.game_key]||0)+num(r.count)});
            const mg2=$('mainGrid');if(mg2)mg2.innerHTML=games.map(g=>renderCard(g,favSet.has(g.game_id))).join('');
            bindCardEvents()
        } else {
            renderEmptyState('mainGrid','Failed to load data. Check your connection.');
            showToast('Failed to load data from server',true)
        }
    }
    return true;
}

async function renderCarousel(){
    const c=$('carouselContainer');if(!c)return;
    if(carouselInterval){clearInterval(carouselInterval);carouselInterval=null}
    if(String(settings.show_carousel)==='false'){c.style.display='none';return}
    try{
        const banners=await sbGet('banners?is_active=eq.true&order=id.desc');
        if(!banners||!banners.length){c.style.display='none';return}
        c.style.display='block';
        c.innerHTML=`<div class="carousel-wrap"><div class="carousel-track" id="carTrack">${banners.map((b,i)=>`<div class="carousel-slide"><img src="${escapeHTML(safeUrl(b.img_url)||placeholder(600))}" style="width:100%;height:100%;object-fit:cover;pointer-events:none;" alt="" loading="lazy"></div>`).join('')}</div><div class="carousel-dots" id="carDots">${banners.map((_,i)=>`<div class="carousel-dot ${i===0?'active':''}" data-idx="${i}"></div>`).join('')}</div></div>`;
        let currentSlide=0;const track=$('carTrack'),dots=document.querySelectorAll('.carousel-dot');
        if(banners.length>1){
            carouselInterval=setInterval(()=>{currentSlide=(currentSlide+1)%banners.length;track.style.transform=`translateX(-${currentSlide*100}%)`;dots.forEach((dot,i)=>dot.classList.toggle('active',i===currentSlide))},4000)
        }
        dots.forEach(d=>{d.addEventListener('click',()=>{currentSlide=parseInt(d.dataset.idx);track.style.transform=`translateX(-${currentSlide*100}%)`;dots.forEach((x,i)=>x.classList.toggle('active',i===currentSlide))})})
    }catch(e){c.style.display='none'}
}

function renderFilters(){
    let h=`<button class="chip active" data-cat="all">All</button>`;
    categories.filter(c=>c.is_home_filter).forEach(c=>h+=`<button class="chip" data-cat="${escapeHTML(c.name)}">${escapeHTML(c.name)}</button>`);
    const fc=$('filterContainer');if(!fc)return;
    fc.innerHTML=h;
    fc.querySelectorAll('.chip').forEach(ch=>{ch.addEventListener('click',()=>filterCat(ch,ch.dataset.cat))})
}

function filterCat(el,cat){
    document.querySelectorAll('#filterContainer .chip').forEach(c=>c.classList.remove('active'));el.classList.add('active');
    const f=cat==='all'?games:games.filter(g=>(g.category||'').split(',').map(x=>x.trim()).includes(cat));
    const mg=$('mainGrid');if(f.length===0)renderEmptyState('mainGrid','No games in this category.');else mg.innerHTML=f.map(g=>renderCard(g,favSet.has(g.game_id))).join('');
    bindCardEvents()
}

function bindCardEvents(){
    document.querySelectorAll('.card[data-id]').forEach(c=>{
        c.onclick=(e)=>{if(e.target.closest('.btn-fav'))return;openDetail(c.dataset.id)}
    });
    document.querySelectorAll('.btn-fav[data-fav]').forEach(b=>{
        b.onclick=(e)=>{e.stopPropagation();toggleFav(b.dataset.fav,b)}
    })
}

function saveSearchHistory(q){if(!q||q.trim()==='')return;let h=JSON.parse(localStorage.getItem('bx_hist')||'[]');q=q.trim();h=h.filter(x=>x.toLowerCase()!==q.toLowerCase());h.unshift(q);localStorage.setItem('bx_hist',JSON.stringify(h.slice(0,10)));renderSearchHistory()}
function renderSearchHistory(){const c=$('searchHistoryContainer');if(!c)return;const h=JSON.parse(localStorage.getItem('bx_hist')||'[]');if(h.length===0){c.innerHTML='';return}c.innerHTML=`<div class="sec-head"><svg fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg><h2>Search History</h2></div>`+h.map(x=>`<div class="chip" style="display:block;margin-bottom:8px;text-align:left;" data-q="${escapeHTML(x)}">${escapeHTML(x)}</div>`).join('');c.querySelectorAll('[data-q]').forEach(el=>el.addEventListener('click',()=>{const v=el.dataset.q;$('searchPageInput').value=v;handleSearchPage(v)}))}
function renderExploreCategories(){const c=$('exploreCategories');if(!c)return;if(categories.length===0){c.innerHTML='';return}c.innerHTML=categories.map(cat=>`<div class="card" style="padding:16px;display:flex;align-items:center;justify-content:space-between;cursor:pointer;" data-cat="${escapeHTML(cat.name)}"><span style="font-size:13px;font-weight:700;">${escapeHTML(cat.name)}</span><svg style="width:16px;height:16px;color:var(--text-muted);" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg></div>`).join('');c.querySelectorAll('[data-cat]').forEach(el=>el.addEventListener('click',()=>{const v=el.dataset.cat;$('searchPageInput').value=v;handleSearchPage(v)}))}

function handleSearchPage(q,skipFocus=false){
    currentSearchQuery=q;const hB=$('searchHistoryContainer'),sB=$('searchSuggestionsWrap'),rB=$('searchResultGrid');
    if(searchTimer){clearTimeout(searchTimer);searchTimer=null}
    if(!q||q.trim()===''){if(hB)hB.style.display='block';if(sB)sB.style.display='block';if(rB)rB.innerHTML='';renderSearchHistory();return}
    if(hB)hB.style.display='none';if(sB)sB.style.display='none';if(rB)rB.innerHTML='<div class="skeleton-search"></div><div class="skeleton-search"></div><div class="skeleton-search"></div>';
    searchTimer=setTimeout(()=>{
        const lowerQ=q.toLowerCase();
        const f=games.filter(g=>{
            const t=(g.title||'').toLowerCase().includes(lowerQ);
            const d=(g.developer||'').toLowerCase().includes(lowerQ);
            const c=(g.category||'').toLowerCase().includes(lowerQ);
            return t||d||c
        });
        if(f.length===0)renderEmptyState('searchResultGrid','Game not found.');
        else{rB.innerHTML=f.map(g=>renderSearchResultCard(g)).join('');bindCardEvents()}
    },300)
}

function renderSearchSuggestions(){const sug=[...games].sort((a,b)=>getStat(b.game_id)-getStat(a.game_id)).slice(0,5);const c=$('searchSuggestions');if(!c)return;c.innerHTML=sug.map(g=>`<div style="background:var(--card-bg);border:1px solid var(--border-color);border-radius:12px;padding:10px 12px;display:flex;justify-content:space-between;align-items:center;cursor:pointer;" data-q="${escapeHTML(g.title)}"><div style="display:flex;align-items:center;gap:12px;min-width:0;"><img src="${escapeHTML(safeUrl(g.img)||placeholder(50))}" style="width:40px;height:40px;border-radius:10px;object-fit:cover;flex-shrink:0;" alt="" loading="lazy"><span style="font-size:13px;font-weight:700;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;">${escapeHTML(g.title)}</span></div><svg style="width:18px;height:18px;color:var(--text-muted);flex-shrink:0;" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg></div>`).join('');c.querySelectorAll('[data-q]').forEach(el=>el.addEventListener('click',()=>{const v=el.dataset.q;$('searchPageInput').value=v;handleSearchPage(v)}))}

async function logDownload(e){
    if(!curGame)return;
    e.preventDefault();
    const targetUrl = curGame.apk_url;
    try{
        const r=await fetchWithTimeout(SB_URL+'/rest/v1/rpc/increment_stat',{method:'POST',headers:{...HDR,'Content-Type':'application/json'},body:JSON.stringify({p_game_key:curGame.game_id,p_type:'download'})});
        if(r.ok){statMap[curGame.game_id]=(statMap[curGame.game_id]||0)+1}
    }catch(err){}
    setTimeout(()=>{ if(targetUrl) window.open(targetUrl,'_blank'); }, 800);
}

async function openDetail(id){
    sessionStorage.setItem('scrollPos',window.scrollY);
    const game=games.find(g=>g.game_id===id);
    if(!game){showToast('Game not found',true);showPage('home');return}
    curGame=game;
    currentRating=0; $('revName').value=''; $('revText').value='';
    document.querySelectorAll('#starInput svg').forEach(s=>s.style.color='var(--border-color)');
    currentReviewFilter=0;

    const urlPath = (curGame.app_type==='app') ? '/apps/' : '/game/';
    gantiURL(urlPath+id);
    
    showPage('detail');$('dImg').src=safeUrl(curGame.img)||placeholder(150);
    $('dTitle').innerText=curGame.title||'';$('dDev').innerText=curGame.developer||'Unknown';
    $('dType').innerText=curGame.badge_text||'';
    $('dDesc').innerText=curGame.desc_game||'No description available.';
    $('dChangelog').innerText=curGame.changelog||'No recent updates.';
    $('dSecurity').innerText=curGame.security_info||'Data is encrypted in transit. You can request data deletion.';
    
    const dl=$('dDownload');if(curGame.apk_url){dl.href=safeUrl(curGame.apk_url)||'#';dl.style.opacity='1';dl.style.pointerEvents='auto'}else{dl.href='#';dl.style.opacity='0.5';dl.style.pointerEvents='none'}
    const lb=$('dLabel');if(curGame.label_text){lb.innerText=curGame.label_text;lb.style.background=safeColor(null);lb.style.display='inline-block'}else lb.style.display='none';
    
    const mBox=$('dMedia');mBox.innerHTML='<div class="skeleton" style="width:260px;height:150px;flex-shrink:0;"></div><div class="skeleton" style="width:260px;height:150px;flex-shrink:0;"></div>';
    try{
        const meds=await sbGet(`game_media?game_id=eq.${enc(id)}&order=sort_order.asc`);
        if(!meds||!meds.length){mBox.innerHTML='';}
        else mBox.innerHTML=meds.map(m=>{
            if(m.media_type==='video'){
                let vidId='';const regExp=/^.*(youtu.be\/|v\/|u\/\w\/|embed\/|watch\?v=|&v=)([^#&?]*).*/;
                const match=(m.url||'').match(regExp);
                if(match&&match[2].length===11)vidId=match[2];
                if(!vidId)return`<div style="flex-shrink:0;width:260px;height:150px;border-radius:16px;border:1px solid var(--border-color);background:var(--card-bg);display:flex;align-items:center;justify-content:center;font-size:12px;color:var(--text-muted);">Video Invalid</div>`;
                return`<div style="flex-shrink:0;width:260px;height:150px;border-radius:16px;overflow:hidden;border:1px solid var(--border-color);background:var(--bg-body);scroll-snap-align:start;"><iframe src="https://www.youtube.com/embed/${escapeHTML(vidId)}?autoplay=0&mute=1&loop=1&playlist=${escapeHTML(vidId)}" style="width:100%;height:100%;border:none;" allow="autoplay" allowfullscreen></iframe></div>`
            }
            return`<div style="flex-shrink:0;width:260px;height:150px;border-radius:16px;overflow:hidden;border:1px solid var(--border-color);background:var(--bg-body);scroll-snap-align:start;cursor:pointer;" data-img="${escapeHTML(safeUrl(m.url))}"><img src="${escapeHTML(safeUrl(m.url)||placeholder(260))}" style="width:100%;height:100%;object-fit:cover;" alt="" loading="lazy"></div>`
        }).join('');
        mBox.querySelectorAll('[data-img]').forEach(el=>el.addEventListener('click',()=>openLightbox(el.dataset.img)))
    }catch(e){mBox.innerHTML=''}
    
    const chipsBox=$('dChips');
    if(curGame.category){
        chipsBox.innerHTML=curGame.category.split(',').map(c=>c.trim()).filter(Boolean).map(c=>`<span class="chip" style="cursor:pointer;" data-cat="${escapeHTML(c)}">${escapeHTML(c)}</span>`).join('');
        chipsBox.querySelectorAll('[data-cat]').forEach(el=>el.addEventListener('click',()=>{const v=el.dataset.cat;$('searchPageInput').value=v;handleSearchPage(v);showPage('search')}))
    }else chipsBox.innerHTML='';
    
    await loadReviews(id);
    const rec=games.filter(g=>g.category===curGame.category&&g.game_id!==id).slice(0,4);
    $('recGrid').innerHTML=rec.map(g=>renderCard(g,favSet.has(g.game_id))).join('');bindCardEvents()
}

function shareGame(){
    if(!curGame)return;
    const type = (curGame.app_type==='app') ? 'apps' : 'game';
    const url=curGame.share_url ? safeUrl(curGame.share_url) : (`https://bxit.my.id/${type}/`+curGame.game_id);
    if(navigator.share){navigator.share({title:curGame.title,text:'Download '+curGame.title+' on BXIT MY ID',url}).catch(()=>{})}
    else if(navigator.clipboard){navigator.clipboard.writeText(url).then(()=>showToast('Link copied successfully!')).catch(()=>showToast('Copy failed',true))}
    else showToast(url)
}

function submitRequest(){
    const n=$('rName').value.trim(),l=$('rLink').value.trim(),d=$('rDesc').value.trim();
    if(!n){showToast('Name is required!',true);return}
    if(l&&!/^https?:\/\//i.test(l)){showToast('Link must start with http:// or https://',true);return}
    const email=settings.wa_request||'bagassasth136@gmail.com';
    const body='Hello BXIT Admin,\n\nI would like to request:\n\nName: '+n+'\nLink: '+l+'\nDescription: '+d+'\n\nThank you.';
    const mailto=`mailto:${email}?subject=${encodeURIComponent('Request Game/App: '+n)}&body=${encodeURIComponent(body)}`;
    const wa=`https://wa.me/?text=${encodeURIComponent(body)}`;
    window.location.href=mailto;
    setTimeout(()=>{ if(document.visibilityState==='visible'){ window.open(wa,'_blank'); } },2000);
}

function setRating(n){currentRating=n;document.querySelectorAll('#starInput svg').forEach((s,i)=>{s.style.color=i<n?'#eab308':'var(--border-color)'})}
async function submitReview(){
    if(!curGame){showToast('Game not found',true);return}
    if(!currentRating){showToast('Please select a star rating!',true);return}
    const name=$('revName').value.trim()||'Anonymous';
    const text=$('revText').value.trim()||'';
    const rating=Math.max(1,Math.min(5,Math.round(num(currentRating))));
    const body={game_id:curGame.game_id,name,rating,review_text:text};
    const ok=await sbPost('game_reviews',body);
    if(ok){
        showToast('Review submitted successfully!');$('revName').value='';$('revText').value='';currentRating=0;
        document.querySelectorAll('#starInput svg').forEach(s=>{s.style.color='var(--border-color)'});
        await loadReviews(curGame.game_id)
    }else showToast('Failed to submit review.',true)
}

async function loadReviews(id){
    const revs=await sbGet(`game_reviews?game_id=eq.${enc(id)}&order=created_at.desc`);
    const total=revs.length;const sum=revs.reduce((a,b)=>a+num(b.rating),0);
    const avg=total?(sum/total).toFixed(1):'0.0';
    $('rateAvgTxt').innerText=avg;$('rateCount').innerText=total+' reviews';
    let starsHTML='';for(let i=1;i<=5;i++)starsHTML+=`<svg style="width:16px;height:16px;color:${i<=Math.round(avg)?'#eab308':'var(--border-color)'};" fill="currentColor" viewBox="0 0 24 24"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg>`;
    $('rateStars').innerHTML=starsHTML;
    const counts=[0,0,0,0,0];
    revs.forEach(r=>{const i=Math.round(num(r.rating))-1;if(i>=0&&i<5)counts[i]++});
    let barHTML='';
    for(let i=4;i>=0;i--){
        const pct=total?(counts[i]/total*100):0;
        barHTML+=`<div onclick="filterReviews(${i+1})" style="cursor:pointer;display:flex;align-items:center;gap:8px;font-size:10px;color:var(--text-muted);padding:2px 0;transition:opacity .2s;opacity:${currentReviewFilter===0||currentReviewFilter===i+1?1:0.4};"><span style="width:8px;">${i+1}</span><div style="flex:1;height:6px;background:var(--border-color);border-radius:3px;overflow:hidden;"><div style="height:100%;background:var(--accent);border-radius:3px;width:${pct}%"></div></div></div>`
    }
    $('rateBars').innerHTML=barHTML;
    const filtered=currentReviewFilter===0?revs:revs.filter(r=>Math.round(num(r.rating))===currentReviewFilter);
    $('reviewList').innerHTML=filtered.map(r=>{
        const stars=Math.max(0,Math.min(5,Math.round(num(r.rating))));
        const starSvg=Array(stars).fill('<svg style="width:12px;height:12px;" fill="currentColor" viewBox="0 0 24 24"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg>').join('');
        return`<div style="background:var(--card-bg);border:1px solid var(--border-color);border-radius:16px;padding:16px;"><div style="display:flex;justify-content:space-between;font-weight:800;font-size:13px;margin-bottom:8px;color:var(--text-main);"><span>${escapeHTML(r.name||'User')}</span><span style="color:#eab308;display:flex;gap:2px;">${starSvg}</span></div><div style="font-size:12px;color:var(--text-muted);line-height:1.6;">${escapeHTML(r.review_text||'')}</div></div>`
    }).join('')||'<div style="text-align:center;color:var(--text-muted);font-size:12px;padding:24px 0;">No reviews yet for this rating.</div>'
}

function filterReviews(star){currentReviewFilter=(currentReviewFilter===star)?0:star;loadReviews(curGame.game_id)}
function openLightbox(url){if(!url)return;$('lightboxImg').src=url;$('lightbox').style.display='flex'}
function closeLightbox(){$('lightbox').style.display='none'}
function closeWelcome(){$('welcomeModal').style.display='none'}
function chatLoaded(){isChatLoaded=true;const err=$('chatError');if(err)err.style.display='none'}
function reloadChat(){const iframe=$('chatIframe'),err=$('chatError');if(!iframe)return;if(err)err.style.display='none';iframe.style.display='block';iframe.src='https://www5.cbox.ws/box/?boxid=967298&boxtag=LBrRLM&t='+Date.now();setTimeout(()=>{try{if(!iframe.contentWindow)showChatError()}catch(e){showChatError()}},8000)}
function showChatError(){const iframe=$('chatIframe'),err=$('chatError');if(iframe)iframe.style.display='none';if(err)err.style.display='flex'}

window.addEventListener('scroll',function(){const b=$('backToTop');if(!b)return;const threshold=Math.min(300,window.innerHeight*0.5);if(window.scrollY>threshold)b.classList.add('show');else b.classList.remove('show')});

document.addEventListener('DOMContentLoaded',()=>{
    const urlParams=new URLSearchParams(window.location.search);
    const redirectPath=urlParams.get('redirect');
    if(redirectPath)window.history.replaceState({},'',redirectPath);
    const currentPath=window.location.pathname;
    const currentSearch=new URLSearchParams(window.location.search);
    const isGamePage=currentSearch.has('id')||currentPath.startsWith('/game/')||currentPath.startsWith('/apps/');
    const validPages=['/search','/chat','/request','/about'];
    
    if(isGamePage){
        $('globalLoader').style.display='flex';
        document.querySelectorAll('.page').forEach(el=>el.classList.add('hidden'));
        initHome().then(()=>{
            const id=currentSearch.get('id')||currentPath.split('/')[2];
            openDetail(id).then(()=>{$('globalLoader').style.display='none'})
        }).catch(()=>{$('globalLoader').style.display='none'})
    }else if(validPages.includes(currentPath)){
        initHome().then(()=>showPage(currentPath.replace('/','')))
    }else{
        initHome().then(()=>bindCardEvents())
    }
});
