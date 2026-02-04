// Market Data Logic

let userPortfolios = [];
let selectedAssetType = 'STOCK'; // Default asset type

// Asset type display names and placeholders
const assetTypeConfig = {
    STOCK: { name: 'Stock', placeholder: 'Enter Stock Symbol (e.g., AAPL, TSLA)...' },
    CRYPTO: { name: 'Crypto', placeholder: 'Enter Crypto Symbol (e.g., BTC, ETH)...' },
    ETF: { name: 'ETF', placeholder: 'Enter ETF Symbol (e.g., SPY, QQQ)...' },
    MUTUAL_FUND: { name: 'Mutual Fund', placeholder: 'Enter Fund Symbol (e.g., VFIAX, FXAIX)...' }
};

// Utility function to format large numbers
function formatNumber(num) {
    if (num >= 1e12) return (num / 1e12).toFixed(2) + 'T';
    if (num >= 1e9) return (num / 1e9).toFixed(2) + 'B';
    if (num >= 1e6) return (num / 1e6).toFixed(2) + 'M';
    if (num >= 1e3) return (num / 1e3).toFixed(2) + 'K';
    return num.toLocaleString();
}



async function fetchUserPortfolios() {
    try {
        const response = await axios.get('/portfolios');
        userPortfolios = response.data;
        console.log("Loaded portfolios:", userPortfolios);
    } catch (error) {
        console.error("Failed to fetch portfolios", error);
    }
}

// Set Asset Type
function setAssetType(type) {
    selectedAssetType = type;

    // Update button active states
    document.querySelectorAll('.asset-type-btn').forEach(btn => {
        btn.classList.remove('active');
        if (btn.dataset.type === type) {
            btn.classList.add('active');
        }
    });

    // Update search placeholder
    const input = document.getElementById('stock-symbol-input');
    if (input && assetTypeConfig[type]) {
        input.placeholder = assetTypeConfig[type].placeholder;
    }

    // Re-render recommendations for the new asset type
    renderRecommendations();

    console.log(`Asset type changed to: ${type}`);
}

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
            renderStockData(response.data, container);
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

    if (btn) btn.addEventListener('click', searchStock);
    if (input) input.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') searchStock();
    });
}

function renderStockData(data, container) {
    // Parse RapidAPI Response Structure: { "quoteResponse": { "result": [ ... ] } }
    let quote = null;

    try {
        if (typeof data === 'string') {
            data = JSON.parse(data);
        }

        if (data.body && data.body.length > 0) {
            quote = data.body[0];
        } else if (data.quoteResponse && data.quoteResponse.result && data.quoteResponse.result.length > 0) {
            // Fallback for standard Yahoo format if it ever switches back
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
            <div class="actions">
                <button class="btn secondary-btn" onclick="openTradeModal('${quote.symbol}', ${price}, 'sell')">Sell</button>
                <button class="btn primary-btn" onclick="openTradeModal('${quote.symbol}', ${price}, 'buy')">Buy</button>
                <button class="btn ai-btn" style="background: linear-gradient(135deg, #6366f1, #8b5cf6); color: white;" onclick="getAiRecommendation('${quote.symbol}', ${price}, ${changePercent})"><i class="fa-solid fa-robot"></i> Ask AI</button>
            </div>
        </div>
        
        <div id="ai-recommendation-box" style="margin-top: 1rem; display: none;" class="profile-card">
            <div style="display: flex; align-items: center; gap: 0.5rem; margin-bottom: 0.5rem; color: var(--accent-primary);">
                <i class="fa-solid fa-robot"></i> <strong>AI Analysis</strong>
            </div>
            <p id="ai-recommendation-text" style="color: var(--text-secondary); line-height: 1.5; font-size: 0.95rem;"></p>
        </div>

        <div class="stock-details-grid" style="margin-top: 2rem;">
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


// AI Recommendation Logic
async function getAiRecommendation(symbol, price, changePercent, outputId) {
    let box, text;

    // Support both the main search result box (no outputId passed) and individual cards
    if (!outputId) {
        box = document.getElementById('ai-recommendation-box');
        text = document.getElementById('ai-recommendation-text');
    } else {
        box = document.getElementById(outputId);
        text = box; // For cards, the box itself handles the text
    }

    if (!box) return;

    box.style.display = 'block';
    if (!outputId) {
        text.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Analyzing market data...';
    } else {
        box.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Analyzing...';
    }

    const prompt = `I am considering buying ${symbol} stock. The current price is $${price} and it changed by ${changePercent}% today. Is it safe to buy? Provide a VERY short (max 2 sentences) risk assessment and recommendation (Buy/Hold/Wait). warning: this is for educational purposes.`;

    try {
        const response = await axios.post('/api/ai/chat', prompt, {
            headers: { 'Content-Type': 'text/plain' }
        });

        if (!outputId) {
            text.innerText = response.data;
        } else {
            box.innerText = response.data;
            box.style.display = 'block';
        }
    } catch (error) {
        console.error("AI Error", error);
        if (!outputId) {
            text.innerHTML = '<span style="color: var(--danger)">Failed to get recommendation.</span>';
        } else {
            box.innerHTML = '<span style="color: var(--danger)">Failed.</span>';
            box.style.display = 'block';
        }
    }
}

// Trade Modal Logic
function openTradeModal(symbol, price, type) {
    const quantity = prompt(`How many shares of ${symbol} do you want to ${type.toUpperCase()} at $${price}?`);
    if (!quantity || isNaN(quantity) || quantity <= 0) return;

    let defaultPortfolioId = "1";
    if (userPortfolios.length > 0) {
        defaultPortfolioId = userPortfolios[0].id.toString();
    }

    const portfolioId = prompt(`Enter Portfolio ID (available portfolios: ${userPortfolios.map(p => p.id).join(', ')}):`, defaultPortfolioId);
    if (!portfolioId) return;

    executeTrade(portfolioId, symbol, parseFloat(quantity), price, type);
}

async function executeTrade(portfolioId, symbol, quantity, price, type) {
    try {
        const endpoint = `/transactions/${type}`; // /transactions/buy or /transactions/sell

        console.log(`Executing ${type.toUpperCase()} trade:`, {
            symbol,
            quantity,
            price,
            assetType: selectedAssetType,
            portfolioId
        });

        alert(`Processing ${type.toUpperCase()}...`);

        await axios.post(endpoint, {
            portfolioId: parseInt(portfolioId),
            symbol: symbol,
            quantity: quantity,
            price: price,
            assetType: selectedAssetType
        });

        alert(`${type.toUpperCase()} Successful!`);
    } catch (error) {
        console.error(error);
        alert(`Trade Failed: ${error.response ? error.response.data : error.message}`);
    }
}

// Mock Stock Recommendations Data by Asset Type
const mockRecommendationsByType = {
    STOCK: [
        { symbol: 'AAPL', name: 'Apple Inc.', price: 178.25, change: 2.34, changePercent: 1.33, recommendation: 'Buy' },
        { symbol: 'TSLA', name: 'Tesla Inc.', price: 242.84, change: -3.21, changePercent: -1.30, recommendation: 'Hold' },
        { symbol: 'GOOGL', name: 'Alphabet Inc.', price: 141.80, change: 1.85, changePercent: 1.32, recommendation: 'Buy' },
        { symbol: 'MSFT', name: 'Microsoft Corp.', price: 420.55, change: 5.12, changePercent: 1.23, recommendation: 'Buy' },
        { symbol: 'AMZN', name: 'Amazon.com Inc.', price: 178.35, change: -2.15, changePercent: -1.19, recommendation: 'Hold' },
        { symbol: 'NVDA', name: 'NVIDIA Corp.', price: 875.28, change: 12.45, changePercent: 1.44, recommendation: 'Buy' },
        { symbol: 'META', name: 'Meta Platforms', price: 484.03, change: 3.67, changePercent: 0.76, recommendation: 'Buy' },
        { symbol: 'NFLX', name: 'Netflix Inc.', price: 612.50, change: -4.32, changePercent: -0.70, recommendation: 'Sell' }
    ],
    CRYPTO: [
        { symbol: 'BTC', name: 'Bitcoin', price: 45250.00, change: 1250.50, changePercent: 2.84, recommendation: 'Buy' },
        { symbol: 'ETH', name: 'Ethereum', price: 2420.75, change: -85.30, changePercent: -3.41, recommendation: 'Hold' },
        { symbol: 'BNB', name: 'Binance Coin', price: 315.40, change: 8.25, changePercent: 2.69, recommendation: 'Buy' },
        { symbol: 'SOL', name: 'Solana', price: 98.60, change: 4.15, changePercent: 4.40, recommendation: 'Buy' },
        { symbol: 'XRP', name: 'Ripple', price: 0.52, change: -0.02, changePercent: -3.70, recommendation: 'Hold' },
        { symbol: 'ADA', name: 'Cardano', price: 0.48, change: 0.01, changePercent: 2.13, recommendation: 'Buy' },
        { symbol: 'AVAX', name: 'Avalanche', price: 36.80, change: 1.90, changePercent: 5.45, recommendation: 'Buy' },
        { symbol: 'DOGE', name: 'Dogecoin', price: 0.08, change: -0.003, changePercent: -3.61, recommendation: 'Sell' }
    ],
    ETF: [
        { symbol: 'SPY', name: 'SPDR S&P 500 ETF', price: 485.20, change: 3.45, changePercent: 0.72, recommendation: 'Buy' },
        { symbol: 'QQQ', name: 'Invesco QQQ Trust', price: 412.85, change: 5.60, changePercent: 1.38, recommendation: 'Buy' },
        { symbol: 'VTI', name: 'Vanguard Total Stock', price: 245.30, change: 1.85, changePercent: 0.76, recommendation: 'Buy' },
        { symbol: 'IWM', name: 'iShares Russell 2000', price: 198.45, change: -2.15, changePercent: -1.07, recommendation: 'Hold' },
        { symbol: 'EEM', name: 'iShares MSCI Emerging', price: 41.20, change: 0.65, changePercent: 1.60, recommendation: 'Buy' },
        { symbol: 'GLD', name: 'SPDR Gold Shares', price: 185.75, change: -1.25, changePercent: -0.67, recommendation: 'Hold' },
        { symbol: 'TLT', name: 'iShares 20+ Year Bond', price: 92.30, change: -0.85, changePercent: -0.91, recommendation: 'Hold' },
        { symbol: 'VNQ', name: 'Vanguard Real Estate', price: 88.50, change: 1.20, changePercent: 1.37, recommendation: 'Buy' }
    ],
    MUTUAL_FUND: [
        { symbol: 'VFIAX', name: 'Vanguard 500 Index', price: 428.15, change: 3.20, changePercent: 0.75, recommendation: 'Buy' },
        { symbol: 'FXAIX', name: 'Fidelity 500 Index', price: 172.80, change: 1.30, changePercent: 0.76, recommendation: 'Buy' },
        { symbol: 'VTSAX', name: 'Vanguard Total Stock', price: 118.45, change: 0.95, changePercent: 0.81, recommendation: 'Buy' },
        { symbol: 'VGTSX', name: 'Vanguard Total Intl', price: 17.25, change: -0.15, changePercent: -0.86, recommendation: 'Hold' },
        { symbol: 'VBTLX', name: 'Vanguard Total Bond', price: 10.45, change: -0.05, changePercent: -0.48, recommendation: 'Hold' },
        { symbol: 'VWELX', name: 'Vanguard Wellington', price: 45.60, change: 0.35, changePercent: 0.77, recommendation: 'Buy' },
        { symbol: 'VTMFX', name: 'Vanguard Tax-Managed', price: 14.85, change: 0.12, changePercent: 0.81, recommendation: 'Buy' },
        { symbol: 'VWINX', name: 'Vanguard Wellesley', price: 28.90, change: 0.08, changePercent: 0.28, recommendation: 'Buy' }
    ]
};

// Render Stock Recommendations
function renderRecommendations() {
    const container = document.getElementById('recommendations-container');
    if (!container) return;

    // Get recommendations for the selected asset type
    const recommendations = mockRecommendationsByType[selectedAssetType] || mockRecommendationsByType.STOCK;

    container.innerHTML = recommendations.map((stock, index) => {
        const isPositive = stock.change >= 0;
        const trendIcon = isPositive ? 'fa-arrow-up' : 'fa-arrow-down';
        const trendClass = isPositive ? 'up' : 'down';
        const changeColor = isPositive ? 'var(--success)' : 'var(--danger)';

        // Recommendation badge color
        let recBadgeColor = 'var(--info)';
        if (stock.recommendation === 'Buy') recBadgeColor = 'var(--success)';
        else if (stock.recommendation === 'Sell') recBadgeColor = 'var(--danger)';
        else if (stock.recommendation === 'Hold') recBadgeColor = 'var(--warning)';

        return `
            <div class="recommendation-card">
                <div class="card-header">
                    <div>
                        <h3>${stock.symbol}</h3>
                        <span class="subtitle">${stock.name}</span>
                    </div>
                    <div class="rec-badge" style="background-color: ${recBadgeColor}20; color: ${recBadgeColor}; padding: 0.25rem 0.75rem; border-radius: 12px; font-size: 0.75rem; font-weight: 600;">
                        ${stock.recommendation}
                    </div>
                </div>
                <div class="card-body">
                    <div class="stock-price-info">
                        <div class="price-large">$${stock.price.toFixed(2)}</div>
                        <div class="price-change trend ${trendClass}" style="color: ${changeColor};">
                            <i class="fa-solid ${trendIcon}"></i>
                            ${isPositive ? '+' : ''}${stock.change.toFixed(2)} (${isPositive ? '+' : ''}${stock.changePercent.toFixed(2)}%)
                        </div>
                    </div>
                    <div class="rec-actions">
                        <button class="btn-rec btn-rec-secondary" onclick="openTradeModal('${stock.symbol}', ${stock.price}, 'sell')">
                            <i class="fa-solid fa-arrow-down"></i> Sell
                        </button>
                        <button class="btn-rec btn-rec-primary" onclick="openTradeModal('${stock.symbol}', ${stock.price}, 'buy')">
                            <i class="fa-solid fa-arrow-up"></i> Buy
                        </button>
                        <button class="btn-rec btn-rec-ai" onclick="getAiRecommendation('${stock.symbol}', ${stock.price}, ${stock.changePercent}, 'ai-rec-${index}')">
                            <i class="fa-solid fa-robot"></i> Ask AI
                        </button>
                    </div>
                    <div id="ai-rec-${index}" class="ai-rec-result" style="display: none; margin-top: 0.75rem; padding: 0.75rem; background: rgba(99, 102, 241, 0.1); border-radius: 8px; font-size: 0.85rem; color: var(--text-muted);"></div>
                </div>
            </div>
        `;
    }).join('');
}

// Dynamic Price Updates - Simulate live market data
function startDynamicPriceUpdates() {
    setInterval(() => {
        // Update prices for all asset types
        Object.keys(mockRecommendationsByType).forEach(assetType => {
            mockRecommendationsByType[assetType].forEach(asset => {
                // Generate random price change between -2% and +2%
                const changePercent = (Math.random() * 4 - 2);
                const priceChange = asset.price * (changePercent / 100);

                // Update price
                asset.price = Math.max(0.01, asset.price + priceChange);

                // Update change values
                asset.change = priceChange;
                asset.changePercent = changePercent;

                // Randomly update recommendation (70% stay same, 15% upgrade, 15% downgrade)
                const rand = Math.random();
                if (rand < 0.15) {
                    // Upgrade recommendation
                    if (asset.recommendation === 'Sell') asset.recommendation = 'Hold';
                    else if (asset.recommendation === 'Hold') asset.recommendation = 'Buy';
                } else if (rand < 0.30) {
                    // Downgrade recommendation
                    if (asset.recommendation === 'Buy') asset.recommendation = 'Hold';
                    else if (asset.recommendation === 'Hold') asset.recommendation = 'Sell';
                }
            });
        });

        // Re-render recommendations if on the page
        if (document.getElementById('recommendations-container')) {
            renderRecommendations();
        }
    }, 60000); // Update every 60 seconds (1 minute)
}

// Start dynamic updates when page loads
document.addEventListener('DOMContentLoaded', () => {
    initMarketData();
    fetchUserPortfolios();
    renderRecommendations();
    startDynamicPriceUpdates(); // Start live price updates
});
