/* ===================== Viasta — shared site script ===================== */

/* ---- Mobile nav drawer ---- */
(function () {
  const header = document.querySelector('header');
  const toggle = document.querySelector('.menu-toggle');
  if (!header || !toggle) return;
  toggle.addEventListener('click', () => {
    header.classList.toggle('nav-open');
  });
  document.querySelectorAll('nav.primary a').forEach(a => {
    a.addEventListener('click', () => header.classList.remove('nav-open'));
  });
})();

/* ---- Active nav link based on current URL ---- */
(function () {
  const path = window.location.pathname;
  document.querySelectorAll('nav.primary > a').forEach(function (a) {
    const href = a.getAttribute('href');
    const isActive =
      (href === '/' && path === '/') ||
      (href !== '/' && path.startsWith(href));
    if (isActive) a.classList.add('active');
    else a.classList.remove('active');
  });
})();

/* ---- Account dropdown ----  */
(function () {
  const wrap = document.querySelector('.nav-account');
  const trigger = document.getElementById('accountTrigger');
  const dropdown = document.getElementById('accountDropdown');
  if (!wrap || !trigger || !dropdown) return;

  function open() {
    wrap.classList.add('open');
    trigger.setAttribute('aria-expanded', 'true');
    dropdown.setAttribute('aria-hidden', 'false');
  }
  function close() {
    wrap.classList.remove('open');
    trigger.setAttribute('aria-expanded', 'false');
    dropdown.setAttribute('aria-hidden', 'true');
  }

  trigger.addEventListener('click', (e) => {
    e.stopPropagation();
    wrap.classList.contains('open') ? close() : open();
  });

  // Close when clicking outside
  document.addEventListener('click', (e) => {
    if (!wrap.contains(e.target)) close();
  });

  // Close on Escape
  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') close();
  });

  // Close when a link inside is clicked
  dropdown.querySelectorAll('a').forEach(a => {
    a.addEventListener('click', () => close());
  });
})();

/* ---- Scroll reveal ---- */
(function () {
  const targets = document.querySelectorAll('.reveal');
  if (!targets.length) return;
  if (!('IntersectionObserver' in window)) {
    targets.forEach(t => t.classList.add('in-view'));
    return;
  }
  const io = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        entry.target.classList.add('in-view');
        io.unobserve(entry.target);
      }
    });
  }, { threshold: 0.15 });
  targets.forEach(t => io.observe(t));
})();

/* ---- Functional search overlay ---- */
(function () {
  const overlay = document.getElementById('searchOverlay');
  const triggers = document.querySelectorAll('.search-trigger');
  const closeBtn = document.getElementById('searchClose');
  const input = document.getElementById('searchInput');
  const form = document.getElementById('searchForm');
  const hints = document.querySelectorAll('.search-hint');
  if (!overlay || !triggers.length) return;

  let lastFocused = null;

  function openSearch(e) {
    lastFocused = e && e.currentTarget ? e.currentTarget : document.activeElement;
    overlay.classList.add('open');
    overlay.setAttribute('aria-hidden', 'false');
    document.body.classList.add('search-lock');
    setTimeout(() => input && input.focus(), 200);
  }
  function closeSearch() {
    overlay.classList.remove('open');
    overlay.setAttribute('aria-hidden', 'true');
    document.body.classList.remove('search-lock');
    if (lastFocused && typeof lastFocused.focus === 'function') lastFocused.focus();
  }

  triggers.forEach(t => t.addEventListener('click', openSearch));
  closeBtn && closeBtn.addEventListener('click', closeSearch);
  overlay.addEventListener('click', (e) => { if (e.target === overlay) closeSearch(); });
  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape' && overlay.classList.contains('open')) closeSearch();
  });

  function submitSearch(query) {
    const q = (query || '').trim();
    if (!q) return;
    const onShopPage = /shop\.html$/.test(location.pathname) || location.pathname.endsWith('/shop.html');
    const target = 'shop.html?search=' + encodeURIComponent(q);
    if (onShopPage) {
      history.replaceState(null, '', target);
      applyShopSearch(q);
      closeSearch();
    } else {
      window.location.href = target;
    }
  }

  form && form.addEventListener('submit', (e) => {
    e.preventDefault();
    submitSearch(input.value);
  });

  hints.forEach(h => h.addEventListener('click', () => {
    input.value = h.dataset.q;
    submitSearch(h.dataset.q);
  }));

  // If arriving on shop.html with ?search=..., run it immediately
  const params = new URLSearchParams(location.search);
  const initialQuery = params.get('search');
  if (initialQuery) applyShopSearch(initialQuery);
})();

/* ---- Shop page: name-based search filter ---- */
function applyShopSearch(query) {
  const grid = document.querySelector('.shop-results .prod-grid');
  if (!grid) return;
  const cards = grid.querySelectorAll('.prod-card');
  const countEl = document.querySelector('.shop-toolbar .count');
  const emptyState = document.querySelector('.search-empty-state');
  const tagWrap = document.querySelector('.active-search-tag-wrap');
  const q = query.toLowerCase();

  let visible = 0;
  cards.forEach(card => {
    const name = (card.querySelector('h3')?.textContent || '').toLowerCase();
    const cat = (card.dataset.category || '').toLowerCase();
    const match = name.includes(q) || cat.includes(q);
    card.classList.toggle('is-hidden', !match);
    if (match) visible++;
  });

  if (countEl) countEl.textContent = `Showing ${visible} result${visible === 1 ? '' : 's'} for "${query}"`;
  if (emptyState) emptyState.classList.toggle('show', visible === 0);
  if (tagWrap) {
    tagWrap.innerHTML = `<span class="active-search-tag">Searching: "${query}" <button type="button" aria-label="Clear search" onclick="clearShopSearch()"><i class="fa-solid fa-xmark"></i></button></span>`;
  }
}

function clearShopSearch() {
  history.replaceState(null, '', 'shop.html');
  const grid = document.querySelector('.shop-results .prod-grid');
  if (!grid) return;
  grid.querySelectorAll('.prod-card').forEach(c => c.classList.remove('is-hidden'));
  const emptyState = document.querySelector('.search-empty-state');
  if (emptyState) emptyState.classList.remove('show');
  const tagWrap = document.querySelector('.active-search-tag-wrap');
  if (tagWrap) tagWrap.innerHTML = '';
  const countEl = document.querySelector('.shop-toolbar .count');
  if (countEl) countEl.textContent = `Showing 1–${grid.querySelectorAll('.prod-card').length} of ${grid.querySelectorAll('.prod-card').length} products`;
}

/* ---- Scroll progress bar + back-to-top ---- */
(function () {
  const bar = document.createElement('div');
  bar.className = 'scroll-progress';
  document.body.appendChild(bar);

  const btn = document.createElement('button');
  btn.className = 'back-to-top';
  btn.setAttribute('aria-label', 'Back to top');
  btn.innerHTML = '<i class="fa-solid fa-arrow-up"></i>';
  document.body.appendChild(btn);
  btn.addEventListener('click', () => window.scrollTo({ top: 0, behavior: 'smooth' }));

  window.addEventListener('scroll', () => {
    const scrollTop = window.scrollY;
    const docHeight = document.documentElement.scrollHeight - window.innerHeight;
    bar.style.width = docHeight > 0 ? (scrollTop / docHeight) * 100 + '%' : '0%';
    btn.classList.toggle('show', scrollTop > 600);
  }, { passive: true });
})();

/* ---- Animated stat counters ---- */
(function () {
  const counters = document.querySelectorAll('.count-up');
  if (!counters.length || !('IntersectionObserver' in window)) return;

  function animate(el) {
    const raw = el.dataset.target || el.textContent;
    const match = raw.match(/^([^\d]*)([\d,]+)(.*)$/);
    if (!match) return;
    const [, prefix, numStr, suffix] = match;
    const target = parseInt(numStr.replace(/,/g, ''), 10);
    const duration = 1400;
    const start = performance.now();
    function step(now) {
      const progress = Math.min((now - start) / duration, 1);
      const eased = 1 - Math.pow(1 - progress, 3);
      const value = Math.round(target * eased);
      el.textContent = prefix + value.toLocaleString('en-IN') + suffix;
      if (progress < 1) requestAnimationFrame(step);
    }
    requestAnimationFrame(step);
  }

  const io = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        animate(entry.target);
        io.unobserve(entry.target);
      }
    });
  }, { threshold: 0.5 });
  counters.forEach(c => io.observe(c));
})();

/* ---- Cart state + mini drawer ---- */
const ViastaCart = (function () {
  const STORAGE_KEY = 'viasta-cart-items';

  function load() {
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      return raw ? JSON.parse(raw) : [];
    } catch (e) { return []; }
  }

  function save() {
    try { localStorage.setItem(STORAGE_KEY, JSON.stringify(items)); } catch (e) { /* ignore */ }
  }

  let items = load();

  function updateBadges() {
    const count = items.reduce((sum, i) => sum + i.qty, 0);
    document.querySelectorAll('.cart-trigger .badge').forEach(b => {
      b.textContent = String(count);
    });
  }

  function render() {
    const body = document.getElementById('cartDrawerBody');
    const foot = document.getElementById('cartDrawerFoot');
    const subtotalEl = document.getElementById('cartSubtotal');
    if (!body) return;

    if (items.length === 0) {
      body.innerHTML = `
        <div class="cart-empty">
          <i class="fa-solid fa-bag-shopping"></i>
          <p>Your cart is empty.</p>
          <a href="shop.html" class="btn btn-outline">Start Shopping</a>
        </div>`;
      if (foot) foot.hidden = true;
      return;
    }

    body.innerHTML = items.map((item, idx) => `
      <div class="cart-line">
        <img src="${item.img}" alt="${item.name}">
        <div class="cart-line-info">
          <h4>${item.name}</h4>
          <span class="price">₹${item.price.toLocaleString('en-IN')}</span>
          <div class="cart-line-qty">
            <button type="button" data-action="dec" data-idx="${idx}" aria-label="Decrease quantity">−</button>
            <span>${item.qty}</span>
            <button type="button" data-action="inc" data-idx="${idx}" aria-label="Increase quantity">+</button>
          </div>
        </div>
        <button type="button" class="cart-line-remove" data-action="remove" data-idx="${idx}" aria-label="Remove item">
          <i class="fa-solid fa-trash"></i>
        </button>
      </div>
    `).join('');

    const subtotal = items.reduce((sum, i) => sum + i.price * i.qty, 0);
    if (subtotalEl) subtotalEl.textContent = '₹' + subtotal.toLocaleString('en-IN');
    if (foot) foot.hidden = false;

    body.querySelectorAll('button[data-action]').forEach(btn => {
      btn.addEventListener('click', () => {
        const idx = parseInt(btn.dataset.idx, 10);
        const action = btn.dataset.action;
        if (action === 'inc') items[idx].qty++;
        if (action === 'dec') { items[idx].qty--; if (items[idx].qty <= 0) items.splice(idx, 1); }
        if (action === 'remove') items.splice(idx, 1);
        save();
        updateBadges();
        render();
        document.dispatchEvent(new CustomEvent('viasta-cart-changed'));
      });
    });
  }

  function add(product) {
    const existing = items.find(i => i.name === product.name);
    if (existing) existing.qty++;
    else items.push({ ...product, qty: 1 });
    save();
    updateBadges();
    render();
    document.dispatchEvent(new CustomEvent('viasta-cart-changed'));
  }

  function getItems() { return items; }

  function setQty(idx, qty) {
    if (qty <= 0) { items.splice(idx, 1); }
    else { items[idx].qty = qty; }
    save();
    updateBadges();
    render();
    document.dispatchEvent(new CustomEvent('viasta-cart-changed'));
  }

  function remove(idx) {
    items.splice(idx, 1);
    save();
    updateBadges();
    render();
    document.dispatchEvent(new CustomEvent('viasta-cart-changed'));
  }

  function clear() {
    items = [];
    save();
    updateBadges();
    render();
    document.dispatchEvent(new CustomEvent('viasta-cart-changed'));
  }

  updateBadges();

  return { add, render, updateBadges, getItems, setQty, remove, clear };
})();

(function () {
  const drawer = document.getElementById('cartDrawer');
  const triggers = document.querySelectorAll('.cart-trigger');
  const closeBtn = document.getElementById('cartClose');
  if (!drawer || !triggers.length) return;

  let lastFocused = null;

  function openCart(e) {
    lastFocused = e && e.currentTarget ? e.currentTarget : document.activeElement;
    ViastaCart.render();
    drawer.classList.add('open');
    drawer.setAttribute('aria-hidden', 'false');
    document.body.classList.add('cart-lock');
  }
  function closeCart() {
    drawer.classList.remove('open');
    drawer.setAttribute('aria-hidden', 'true');
    document.body.classList.remove('cart-lock');
    if (lastFocused && typeof lastFocused.focus === 'function') lastFocused.focus();
  }

  triggers.forEach(t => t.addEventListener('click', openCart));
  closeBtn && closeBtn.addEventListener('click', closeCart);
  drawer.addEventListener('click', (e) => { if (e.target === drawer) closeCart(); });
  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape' && drawer.classList.contains('open')) closeCart();
  });
})();

/* ---- Product quick-add feedback ---- */
document.querySelectorAll('.prod-card').forEach(card => {
  const btn = card.querySelector('.quick-add button');
  if (!btn) return;
  btn.addEventListener('click', () => {
    const name = card.querySelector('h3')?.textContent?.trim() || 'Product';
    const priceText = card.querySelector('.prod-price .now')?.textContent || '₹0';
    const price = parseFloat(priceText.replace(/[^0-9.]/g, '')) || 0;
    const img = card.querySelector('.prod-media img')?.getAttribute('src') || '';

    ViastaCart.add({ name, price, img });

    const original = btn.innerHTML;
    btn.innerHTML = '<i class="fa-solid fa-check"></i> Added';
    setTimeout(() => (btn.innerHTML = original), 1500);
  });
});

/* ---- Product quick-wishlist toggle ---- */
document.querySelectorAll('.quick-wish').forEach(btn => {
  btn.addEventListener('click', () => {
    btn.classList.toggle('active');
    const icon = btn.querySelector('i');
    if (icon) {
      icon.classList.toggle('fa-regular');
      icon.classList.toggle('fa-solid');
    }
  });
});

/* ---- User Account Pages (Profile / Address / Orders) ---- */
(function () {

  /* -- My Profile: edit toggle -- */
  var editBtn    = document.getElementById('editProfileBtn');
  var profileView = document.getElementById('profileView');
  var editForm   = document.getElementById('profileEditForm');
  var cancelBtn  = document.getElementById('cancelEditBtn');
  if (editBtn && profileView && editForm) {
    editBtn.addEventListener('click', function () {
      profileView.style.display = 'none';
      editForm.style.display = 'block';
      editBtn.style.display = 'none';
    });
    cancelBtn && cancelBtn.addEventListener('click', function () {
      profileView.style.display = 'block';
      editForm.style.display = 'none';
      editBtn.style.display = 'flex';
    });
  }

  /* -- My Profile: change password toggle -- */
  var pwdBtn    = document.getElementById('togglePasswordBtn');
  var pwdForm   = document.getElementById('passwordForm');
  var pwdHint   = document.getElementById('passwordHint');
  var cancelPwd = document.getElementById('cancelPasswordBtn');
  if (pwdBtn && pwdForm) {
    pwdBtn.addEventListener('click', function () {
      var open = pwdForm.style.display !== 'none' && pwdForm.style.display !== '';
      pwdForm.style.display  = open ? 'none' : 'block';
      if (pwdHint) pwdHint.style.display = open ? 'block' : 'none';
    });
    cancelPwd && cancelPwd.addEventListener('click', function () {
      pwdForm.style.display = 'none';
      if (pwdHint) pwdHint.style.display = 'block';
    });
  }

  /* -- Saved Address: show/hide add form -- */
  var addBtn      = document.getElementById('addAddressBtn');
  var addrCard    = document.getElementById('addressFormCard');
  var cancelAddr  = document.getElementById('cancelAddressBtn');

  if (addBtn && addrCard) {
    addBtn.addEventListener('click', function () {
      addrCard.style.display = 'block';
      addrCard.scrollIntoView({ behavior: 'smooth', block: 'start' });
    });
    cancelAddr && cancelAddr.addEventListener('click', function () {
      addrCard.style.display = 'none';
    });
  }

  /* -- My Orders: filter chips -- */
  var chips = document.querySelectorAll('.user-order-chip');
  if (chips.length) {
    chips.forEach(function (chip) {
      chip.addEventListener('click', function () {
        chips.forEach(function (c) { c.classList.remove('active'); });
        chip.classList.add('active');
        var filter = chip.dataset.filter;
        document.querySelectorAll('.order-card').forEach(function (card) {
          var show = filter === 'all' || card.dataset.status === filter;
          card.style.display = show ? '' : 'none';
        });
      });
    });
  }

})();


document.querySelectorAll('.pass-toggle').forEach(btn => {
  btn.addEventListener('click', () => {
    const input = btn.previousElementSibling;
    if (!input) return;
    const showing = input.type === 'password';
    input.type = showing ? 'text' : 'password';
    const icon = btn.querySelector('i');
    if (icon) {
      icon.classList.toggle('fa-eye', !showing);
      icon.classList.toggle('fa-eye-slash', showing);
    }
  });
});

/* ---- Coupon copy (homepage offer section) ---- */
(function () {
  const copyBtn = document.getElementById('copyBtn');
  if (!copyBtn) return;
  copyBtn.addEventListener('click', () => {
    navigator.clipboard?.writeText('WELCOME20').catch(() => {});
    const original = copyBtn.textContent;
    copyBtn.textContent = 'Copied!';
    setTimeout(() => (copyBtn.textContent = original), 1800);
  });
})();

/* ---- Countdown timer (homepage offer section) ---- */
(function () {
  const h = document.getElementById('cd-h');
  const m = document.getElementById('cd-m');
  const s = document.getElementById('cd-s');
  if (!h || !m || !s) return;
  let remaining = 8 * 3600;
  setInterval(() => {
    if (remaining <= 0) remaining = 8 * 3600;
    remaining--;
    h.textContent = String(Math.floor(remaining / 3600)).padStart(2, '0');
    m.textContent = String(Math.floor((remaining % 3600) / 60)).padStart(2, '0');
    s.textContent = String(remaining % 60).padStart(2, '0');
  }, 1000);
})();

/* ---- Newsletter & contact forms intentionally have NO JS here ----
   Both now submit as plain HTML forms (action + method + name attributes
   only) so a Spring Boot controller can bind and handle them directly —
   e.g. @PostMapping("/newsletter/subscribe") / @PostMapping("/contact").
   Show success/error state from the server (redirect + flash message, or
   a Thymeleaf model attribute rendered back into the page) rather than
   faking it here, so the UI always reflects what the backend actually did. */

/* ---- Shop page: category filter + price filter + view toggle ---- */
(function () {
  const grid = document.querySelector('.shop-results .prod-grid');
  if (!grid) return;
  const cards = grid.querySelectorAll('.prod-card');
  const catChecks = document.querySelectorAll('.filter-cat');
  const countEl = document.querySelector('.shop-toolbar .count');
  const priceMin = document.getElementById('priceMin');
  const priceMax = document.getElementById('priceMax');
  const applyPriceBtn = document.querySelector('.apply-price-filter');

  function applyFilters() {
    const checkedCats = Array.from(catChecks).filter(c => c.checked).map(c => c.value);
    const min = priceMin ? parseFloat(priceMin.value) || 0 : 0;
    const max = priceMax ? parseFloat(priceMax.value) || Infinity : Infinity;

    let visible = 0;
    cards.forEach(card => {
      const cat = card.dataset.category;
      const price = parseFloat(card.dataset.price || '0');
      const matchesCat = checkedCats.length === 0 || checkedCats.includes(cat);
      const matchesPrice = price >= min && price <= max;
      const show = matchesCat && matchesPrice;
      card.classList.toggle('is-hidden', !show);
      if (show) visible++;
    });
    if (countEl) countEl.textContent = `Showing 1–${visible} of ${cards.length} products`;
  }

  catChecks.forEach(c => c.addEventListener('change', applyFilters));

  if (applyPriceBtn) {
    applyPriceBtn.addEventListener('click', applyFilters);
  }
  // Also apply on Enter key inside the price fields
  [priceMin, priceMax].forEach(input => {
    if (!input) return;
    input.addEventListener('keydown', (e) => {
      if (e.key === 'Enter') { e.preventDefault(); applyFilters(); }
    });
  });

  const clearBtn = document.querySelector('.filter-panel-head a');
  if (clearBtn) {
    clearBtn.addEventListener('click', (e) => {
      e.preventDefault();
      catChecks.forEach(c => (c.checked = false));
      if (priceMin) priceMin.value = 0;
      if (priceMax) priceMax.value = 10000;
      applyFilters();
    });
  }

  const viewBtns = document.querySelectorAll('.view-toggle button');
  viewBtns.forEach(btn => {
    btn.addEventListener('click', () => {
      viewBtns.forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      grid.classList.toggle('list-view', btn.dataset.view === 'list');
    });
  });

  const sortSelect = document.querySelector('.sort-select');
  if (sortSelect) {
    sortSelect.addEventListener('change', () => {
      const items = Array.from(cards);
      const priceOf = (el) => parseFloat((el.dataset.price || '0').replace(/[^0-9.]/g, ''));
      if (sortSelect.value.includes('Low to High')) items.sort((a, b) => priceOf(a) - priceOf(b));
      else if (sortSelect.value.includes('High to Low')) items.sort((a, b) => priceOf(b) - priceOf(a));
      items.forEach(item => grid.appendChild(item));
    });
  }
})();

/* ---- Blog page: topic chip filter ---- */
(function () {
  const chips = document.querySelectorAll('.topic-chip');
  if (!chips.length) return;
  const posts = document.querySelectorAll('[data-category]');
  const empty = document.querySelector('.topic-empty');

  chips.forEach(chip => {
    chip.addEventListener('click', () => {
      chips.forEach(c => c.classList.remove('active'));
      chip.classList.add('active');
      const cat = chip.dataset.cat;
      let visible = 0;
      posts.forEach(post => {
        const show = cat === 'all' || post.dataset.category === cat;
        post.classList.toggle('is-hidden', !show);
        if (show) visible++;
      });
      if (empty) empty.classList.toggle('show', visible === 0);
    });
  });
})();

/* ---- Full Cart page (cart.html) ----
   Reads and writes the same ViastaCart state used by the mini drawer, so
   whatever was added from any product page shows up here automatically. */
(function () {
  const layout = document.getElementById('cartPageLayout');
  const emptyPage = document.getElementById('cartEmptyPage');
  const list = document.getElementById('cartPageList');
  if (!layout || !emptyPage || !list) return;

  const FREE_SHIPPING_THRESHOLD = 5000;
  const FLAT_SHIPPING = 150;
  const PROMO_CODES = { WELCOME20: { minSubtotal: 2500, off: 500 } };

  let appliedPromo = null;

  function fmt(n) { return '₹' + Math.max(0, Math.round(n)).toLocaleString('en-IN'); }

  function renderCartPage() {
    const items = ViastaCart.getItems();

    if (!items.length) {
      layout.hidden = true;
      emptyPage.hidden = false;
      return;
    }
    layout.hidden = false;
    emptyPage.hidden = true;

    list.innerHTML = items.map((item, idx) => `
      <div class="cart-page-line">
        <img src="${item.img}" alt="${item.name}">
        <div class="cart-page-line-info">
          <h3>${item.name}</h3>
          <span class="unit-price">${fmt(item.price)} each</span>
          <div class="cart-page-line-actions">
            <div class="cart-page-qty">
              <button type="button" data-action="dec" data-idx="${idx}" aria-label="Decrease quantity">−</button>
              <span>${item.qty}</span>
              <button type="button" data-action="inc" data-idx="${idx}" aria-label="Increase quantity">+</button>
            </div>
            <span class="cart-page-line-total">${fmt(item.price * item.qty)}</span>
            <button type="button" class="cart-page-remove" data-action="remove" data-idx="${idx}">
              <i class="fa-solid fa-trash"></i> Remove
            </button>
          </div>
        </div>
      </div>
    `).join('');

    const subtotal = items.reduce((sum, i) => sum + i.price * i.qty, 0);
    let discount = 0;
    if (appliedPromo && PROMO_CODES[appliedPromo] && subtotal >= PROMO_CODES[appliedPromo].minSubtotal) {
      discount = PROMO_CODES[appliedPromo].off;
    }
    const shipping = subtotal >= FREE_SHIPPING_THRESHOLD ? 0 : FLAT_SHIPPING;
    const total = subtotal - discount + shipping;

    document.getElementById('summarySubtotal').textContent = fmt(subtotal);
    document.getElementById('summaryShipping').textContent = shipping === 0 ? 'Free' : fmt(shipping);
    document.getElementById('summaryTotal').textContent = fmt(total);

    const discountRow = document.getElementById('summaryDiscountRow');
    if (discount > 0) {
      discountRow.hidden = false;
      document.getElementById('summaryDiscount').textContent = '−' + fmt(discount);
    } else {
      discountRow.hidden = true;
    }

    list.querySelectorAll('button[data-action]').forEach(btn => {
      btn.addEventListener('click', () => {
        const idx = parseInt(btn.dataset.idx, 10);
        const action = btn.dataset.action;
        const current = ViastaCart.getItems()[idx];
        if (!current) return;
        if (action === 'inc') ViastaCart.setQty(idx, current.qty + 1);
        if (action === 'dec') ViastaCart.setQty(idx, current.qty - 1);
        if (action === 'remove') ViastaCart.remove(idx);
      });
    });
  }

  document.addEventListener('viasta-cart-changed', renderCartPage);

  const clearBtn = document.getElementById('clearCartBtn');
  if (clearBtn) clearBtn.addEventListener('click', () => ViastaCart.clear());

  const promoForm = document.getElementById('promoForm');
  const promoMsg = document.getElementById('promoMsg');
  if (promoForm) {
    promoForm.addEventListener('submit', (e) => {
      e.preventDefault();
      const input = document.getElementById('promoInput');
      const code = (input.value || '').trim().toUpperCase();
      const subtotal = ViastaCart.getItems().reduce((sum, i) => sum + i.price * i.qty, 0);

      if (!code) return;
      if (!PROMO_CODES[code]) {
        promoMsg.textContent = "That code doesn't look right.";
        promoMsg.className = 'promo-msg error';
        appliedPromo = null;
      } else if (subtotal < PROMO_CODES[code].minSubtotal) {
        promoMsg.textContent = `Add ${fmt(PROMO_CODES[code].minSubtotal)} more to use this code.`;
        promoMsg.className = 'promo-msg error';
        appliedPromo = null;
      } else {
        promoMsg.textContent = `${code} applied — ${fmt(PROMO_CODES[code].off)} off.`;
        promoMsg.className = 'promo-msg success';
        appliedPromo = code;
      }
      renderCartPage();
    });
  }

  renderCartPage();
})();

/* ---- Signature feature: Style Mood palette switcher ----
   Three curated accent palettes visitors can live-preview. The widget is
   injected once here so it appears identically on every page without
   touching each page's markup, and the chosen mood persists across
   navigation via localStorage (this is a delivered static site the user
   will host themselves, not a claude.ai artifact, so localStorage is safe
   and appropriate here). */
(function () {
  const MOODS = {
    copper: { name: 'Copper Bloom', gold: '#c97b4a', goldLight: '#e8a374', garnet: '#2f6f6a', garnetLight: '#57a39c' },
    teal:   { name: 'Monsoon Teal', gold: '#3f8f86', goldLight: '#6cbdb3', garnet: '#b5562f', garnetLight: '#d98a5c' },
    rose:   { name: 'Rosewood Dusk', gold: '#b5566b', goldLight: '#dd8fa0', garnet: '#4a7a6b', garnetLight: '#7bab9a' }
  };
  const STORAGE_KEY = 'viasta-style-mood';

  function applyMood(key) {
    const m = MOODS[key];
    if (!m) return;
    const root = document.documentElement.style;
    root.setProperty('--gold', m.gold);
    root.setProperty('--gold-light', m.goldLight);
    root.setProperty('--garnet', m.garnet);
    root.setProperty('--garnet-light', m.garnetLight);
  }

  function getSavedMood() {
    try { return localStorage.getItem(STORAGE_KEY) || 'copper'; }
    catch (e) { return 'copper'; }
  }

  function saveMood(key) {
    try { localStorage.setItem(STORAGE_KEY, key); } catch (e) { /* ignore */ }
  }

  const current = getSavedMood();
  applyMood(current);

  const widget = document.createElement('div');
  widget.className = 'mood-switcher';
  widget.innerHTML = `
    <button type="button" class="mood-switcher-toggle" aria-label="Choose style mood"><i class="fa-solid fa-palette"></i></button>
    <span class="mood-switcher-label">Style Mood</span>
    <div class="mood-swatches">
      ${Object.entries(MOODS).map(([key, m]) =>
        `<button type="button" class="mood-swatch${key === current ? ' active' : ''}" data-mood="${key}" title="${m.name}" style="background:${m.gold}" aria-label="${m.name}"></button>`
      ).join('')}
    </div>
  `;
  document.body.appendChild(widget);

  widget.querySelector('.mood-switcher-toggle').addEventListener('click', () => {
    widget.classList.toggle('open');
  });

  widget.querySelectorAll('.mood-swatch').forEach(btn => {
    btn.addEventListener('click', () => {
      const key = btn.dataset.mood;
      applyMood(key);
      saveMood(key);
      widget.querySelectorAll('.mood-swatch').forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
    });
  });

  document.addEventListener('click', (e) => {
    if (!widget.contains(e.target)) widget.classList.remove('open');
  });
})();
