// State
const state = {
    currentView: 'dashboard',
    portfolios: [],
};

// Elements
const dom = {
    navItems: document.querySelectorAll('.nav-item'),
    viewSections: document.querySelectorAll('.view-section'),
    pageTitle: document.getElementById('page-title'),
    portfolioList: document.getElementById('portfolio-list'),
    portfolioCount: document.getElementById('portfolio-count'),
    chatMessages: document.getElementById('chat-messages'),
    aiInput: document.getElementById('ai-input'),
    sendAiBtn: document.getElementById('send-ai-btn'),
};

// Initialization
document.addEventListener('DOMContentLoaded', () => {
    initNavigation();
    loadDashboard();
    initChat();
    initMarketData();
});

// ... Navigation ...

// ... Dashboard Logic ...

// Market Data Logic
function initMarketData() {
    const input = document.getElementById('stock-symbol-input');
    const btn = document.getElementById('search-stock-btn');
    const container = document.getElementById('stock-data-container');

    const searchStock = async () => {
        const symbol = input.value.trim().toUpperCase();
        if (!symbol) return;

        container.classList.remove('hidden');
        container.innerHTML = '<div class="loading-spinner"><i class="fa-solid fa-spinner fa-spin"></i> Fetching market data...</div>';

        try {
            const response = await axios.get(`/api/market-data/${symbol}`);
            renderStockData(response.data);
        } catch (error) {
            console.error('Stock fetch error:', error);
            container.innerHTML = `
                <div class="empty-state" style="color: var(--danger)">
                    <i class="fa-solid fa-triangle-exclamation"></i>
                    <p>Failed to find stock "${symbol}". Please check the symbol and try again.</p>
                </div>
            `;
        }
    };

    btn.addEventListener('click', searchStock);
    input.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') searchStock();
    });
}

function renderStockData(data) {
    const container = document.getElementById('stock-data-container');

    // Parse RapidAPI Response Structure: { "quoteResponse": { "result": [ ... ] } }
    let quote = null;
    let stats = null; // RapidAPI quote response typically merges everything into the result object

    try {
        if (typeof data === 'string') {
            data = JSON.parse(data);
        }

        if (data.quoteResponse && data.quoteResponse.result && data.quoteResponse.result.length > 0) {
            quote = data.quoteResponse.result[0];
        }
    } catch (e) {
        console.error("Error parsing stock data", e);
    }

    if (!quote) {
        container.innerHTML = `<div class="empty-state">Data unavailable for this symbol.</div>`;
        return;
    }

    const price = quote.regularMarketPrice || 0;
    const change = quote.regularMarketChange || 0;
    const changePercent = quote.regularMarketChangePercent || 0;
    const isPositive = change >= 0;
    const trendClass = isPositive ? 'up' : 'down';
    const trendIcon = isPositive ? 'fa-arrow-up' : 'fa-arrow-down';
    const color = isPositive ? 'var(--success)' : 'var(--danger)';

    container.innerHTML = `
        <div class="stock-header">
            <div class="stock-title">
                <h3>${quote.shortName || quote.symbol}</h3>
                <span class="symbol">${quote.symbol}</span>
            </div>
            <div class="stock-price">
                <span class="current">$${price.toFixed(2)}</span>
                <span class="change" style="color: ${color}">
                    <i class="fa-solid ${trendIcon}"></i> ${change.toFixed(2)} (${changePercent.toFixed(2)}%)
                </span>
            </div>
        </div>
        <div class="stock-details-grid">
            <div class="detail-item">
                <label>Previous Close</label>
                <span>$${(quote.regularMarketPreviousClose || 0).toFixed(2)}</span>
            </div>
            <div class="detail-item">
                <label>Open</label>
                <span>$${(quote.regularMarketOpen || 0).toFixed(2)}</span>
            </div>
            <div class="detail-item">
                <label>Day's Range</label>
                <span>$${(quote.regularMarketDayLow || 0).toFixed(2)} - $${(quote.regularMarketDayHigh || 0).toFixed(2)}</span>
            </div>
             <div class="detail-item">
                <label>Volume</label>
                <span>${(quote.regularMarketVolume || 0).toLocaleString()}</span>
            </div>
             <div class="detail-item">
                <label>Market Cap</label>
                <span>$${formatNumber(quote.marketCap || 0)}</span>
            </div>
             <div class="detail-item">
                <label>52 Week Range</label>
                <span>$${(quote.fiftyTwoWeekLow || 0).toFixed(2)} - $${(quote.fiftyTwoWeekHigh || 0).toFixed(2)}</span>
            </div>
        </div>
    `;
}

function formatNumber(num) {
    if (num >= 1.0e+12) return (num / 1.0e+12).toFixed(2) + "T";
    if (num >= 1.0e+9) return (num / 1.0e+9).toFixed(2) + "B";
    if (num >= 1.0e+6) return (num / 1.0e+6).toFixed(2) + "M";
    return num.toLocaleString();
}

// Navigation
function initNavigation() {
    dom.navItems.forEach(item => {
        item.addEventListener('click', (e) => {
            e.preventDefault();
            const viewName = item.dataset.view;
            switchView(viewName);

            // Update Active State
            dom.navItems.forEach(nav => nav.classList.remove('active'));
            item.classList.add('active');

            // Update Title
            const titleMap = {
                'dashboard': 'Dashboard',
                'yfinance': 'Market Data',
                'ai-chat': 'AI Assistant'
            };
            dom.pageTitle.textContent = titleMap[viewName];
        });
    });
}

function switchView(viewName) {
    dom.viewSections.forEach(section => {
        section.classList.add('hidden');
        section.classList.remove('active');
    });

    const activeSection = document.getElementById(`view-${viewName}`);
    if (activeSection) {
        activeSection.classList.remove('hidden');
        activeSection.classList.add('active');
    }
}

// Dashboard Logic
async function loadDashboard() {
    try {
        dom.portfolioList.innerHTML = '<div class="loading-spinner"><i class="fa-solid fa-spinner fa-spin"></i> Loading portfolios...</div>';

        const response = await axios.get('/portfolios');
        state.portfolios = response.data;

        renderPortfolios(state.portfolios);
        updateStats(state.portfolios);
    } catch (error) {
        console.error('Failed to load portfolios:', error);
        dom.portfolioList.innerHTML = `
            <div class="empty-state">
                <p style="color: var(--danger)">Failed to load data. Is the backend running?</p>
            </div>
        `;
    }
}

function renderPortfolios(portfolios) {
    if (!portfolios || portfolios.length === 0) {
        dom.portfolioList.innerHTML = `
            <div class="empty-state" style="grid-column: 1/-1; text-align: center; padding: 2rem; color: var(--text-muted);">
                <i class="fa-solid fa-folder-open" style="font-size: 2rem; margin-bottom: 1rem;"></i>
                <p>No portfolios found. Create one to get started.</p>
            </div>
        `;
        return;
    }

    dom.portfolioList.innerHTML = portfolios.map(portfolio => `
        <div class="portfolio-card" onclick="viewPortfolio(${portfolio.id})">
            <div class="card-header">
                <div>
                    <h3>${portfolio.name || 'Untitled Portfolio'}</h3>
                    <span class="subtitle">ID: #${portfolio.id}</span>
                </div>
                <div class="icon-box purple" style="width: 32px; height: 32px; font-size: 0.9rem;">
                    <i class="fa-solid fa-chevron-right"></i>
                </div>
            </div>
            <div class="card-body">
                <div class="metric-row">
                    <span class="label">Total Assets</span>
                    <span class="val">--</span>
                </div>
                 <div class="metric-row">
                    <span class="label">Last Updated</span>
                    <span class="val">Just now</span>
                </div>
            </div>
        </div>
    `).join('');
}

function updateStats(portfolios) {
    dom.portfolioCount.textContent = portfolios.length;
}

function viewPortfolio(id) {
    // Placeholder for detailed view
    alert(`Viewing details for portfolio ${id}`);
}

// AI Chat Logic
function initChat() {
    const sendMessage = async () => {
        const text = dom.aiInput.value.trim();
        if (!text) return;

        // Add User Message
        addMessage(text, 'user');
        dom.aiInput.value = '';

        // Show Loading
        const loadingId = addMessage('Thinking...', 'ai', true);

        try {
            const response = await axios.post('/api/ai/chat', text, {
                headers: { 'Content-Type': 'text/plain' }
            });

            // Remove Loading & Add AI Response
            removeMessage(loadingId);
            addMessage(response.data, 'ai');
        } catch (error) {
            removeMessage(loadingId);
            addMessage('Sorry, I encountered an error connecting to the AI service.', 'ai');
            console.error(error);
        }
    };

    dom.sendAiBtn.addEventListener('click', sendMessage);
    dom.aiInput.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') sendMessage();
    });
}

function addMessage(text, type, isLoading = false) {
    const id = Date.now();
    const messageDiv = document.createElement('div');
    messageDiv.className = `message ${type}`;
    messageDiv.id = `msg-${id}`;

    messageDiv.innerHTML = `
        <div class="message-content">
            ${isLoading ? '<i class="fa-solid fa-spinner fa-spin"></i> ' : ''}${text}
        </div>
    `;

    dom.chatMessages.appendChild(messageDiv);
    dom.chatMessages.scrollTop = dom.chatMessages.scrollHeight;
    return id;
}

function removeMessage(id) {
    const el = document.getElementById(`msg-${id}`);
    if (el) el.remove();
}
