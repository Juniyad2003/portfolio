// Dashboard Logic

document.addEventListener('DOMContentLoaded', () => {
    loadDashboard();
});

async function loadDashboard() {
    const portfolioList = document.getElementById('portfolio-list');
    const portfolioCount = document.getElementById('portfolio-count');

    try {
        portfolioList.innerHTML = '<div class="loading-spinner"><i class="fa-solid fa-spinner fa-spin"></i> Loading portfolios...</div>';

        const response = await axios.get('/portfolios');
        const portfolios = response.data;

        renderPortfolios(portfolios, portfolioList);
        if (portfolioCount) portfolioCount.textContent = portfolios.length;

        // Calculate Totals
        let totalBalance = 0;
        let totalProfit = 0;
        let totalInvested = 0;

        portfolios.forEach(p => {
            totalBalance += (p.currentValue || 0);
            totalProfit += (p.totalProfitLoss || 0);
            totalInvested += (p.totalInvestment || 0);
        });

        // Update UI
        const balanceEl = document.getElementById('total-balance');
        const profitEl = document.getElementById('total-profit');
        const balanceTrendEl = document.getElementById('balance-trend');
        const profitTrendEl = document.getElementById('profit-trend');

        if (balanceEl) balanceEl.textContent = '$' + totalBalance.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 });
        if (profitEl) profitEl.textContent = (totalProfit >= 0 ? '+' : '') + '$' + totalProfit.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 });

        // Calculate Trends (ROI)
        let roi = 0;
        if (totalInvested > 0) {
            roi = (totalProfit / totalInvested) * 100;
        }

        console.log('=== Dashboard ROI Calculation ===');
        console.log('Total Invested:', totalInvested);
        console.log('Total Profit:', totalProfit);
        console.log('Total Balance:', totalBalance);
        console.log('Calculated ROI:', roi + '%');
        console.log('================================');

        const roiText = (roi >= 0 ? '+' : '') + roi.toFixed(1) + '%';
        const trendClass = roi >= 0 ? 'trend up' : 'trend down';
        const trendIcon = roi >= 0 ? '<i class="fa-solid fa-arrow-trend-up"></i> ' : '<i class="fa-solid fa-arrow-trend-down"></i> ';

        // Use ROI for both trends for now (simplified)
        if (balanceTrendEl) {
            balanceTrendEl.className = trendClass;
            balanceTrendEl.innerHTML = trendIcon + roiText + ' (ROI)';
        }
        if (profitTrendEl) {
            profitTrendEl.className = trendClass;
            profitTrendEl.innerHTML = trendIcon + roiText;
        }

    } catch (error) {
        console.error('Failed to load portfolios:', error);
        portfolioList.innerHTML = `
            <div class="empty-state">
                <p style="color: var(--danger)">Failed to load data. Is the backend running?</p>
            </div>
        `;
    }
}

function renderPortfolios(portfolios, container) {
    if (!portfolios || portfolios.length === 0) {
        container.innerHTML = `
            <div class="empty-state" style="grid-column: 1/-1; text-align: center; padding: 2rem; color: var(--text-muted);">
                <i class="fa-solid fa-folder-open" style="font-size: 2rem; margin-bottom: 1rem;"></i>
                <p>No portfolios found. Create one to get started.</p>
            </div>
        `;
        return;
    }

    container.innerHTML = portfolios.map(portfolio => {
        // Calculate ROI for this portfolio
        const investment = portfolio.totalInvestment || 0;
        const profitLoss = portfolio.totalProfitLoss || 0;
        let roi = 0;
        if (investment > 0) {
            roi = (profitLoss / investment) * 100;
        }

        const roiText = (roi >= 0 ? '+' : '') + roi.toFixed(2) + '%';
        const roiClass = roi >= 0 ? 'up' : 'down';

        return `
        <div class="portfolio-card" onclick="window.location.href='portfolio.html?id=${portfolio.id}'">
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
                    <span class="label">Total Value</span>
                    <span class="val">$${(portfolio.currentValue || 0).toLocaleString()}</span>
                </div>
                <div class="metric-row">
                    <span class="label">ROI</span>
                    <span class="val trend ${roiClass}">${roiText}</span>
                </div>
                 <div class="metric-row">
                    <span class="label">Last Updated</span>
                    <span class="val">Just now</span>
                </div>
            </div>
        </div>
    `;
    }).join('');
}
