// Portfolio Logic

document.addEventListener('DOMContentLoaded', () => {
    loadPortfolios();

    // Attach Create Listener - REMOVED (Handled by inline onclick in HTML to verify double prompt)
    // const createBtn = document.querySelector('.primary-btn');
    // if (createBtn) {
    //    createBtn.addEventListener('click', createPortfolio);
    // }
});

async function loadPortfolios() {
    const list = document.getElementById('portfolio-list');

    try {
        const response = await axios.get('/portfolios');
        const portfolios = response.data;

        if (portfolios.length === 0) {
            list.innerHTML = '<div class="empty-state">No portfolios found. Click "New Portfolio" to create one.</div>';
            return;
        }

        list.innerHTML = portfolios.map(p => `
            <div class="portfolio-card" data-id="${p.id}">
                <div class="card-header">
                    <div class="header-content" style="flex: 1;">
                        <h3>${p.portfolioName || p.name}</h3>
                        <span>$${(p.currentValue || 0).toLocaleString()}</span>
                    </div>
                    <div class="card-actions" style="display: flex; gap: 0.5rem;">
                        <button class="btn icon-btn edit-btn" onclick="event.stopPropagation(); editPortfolio(${p.id}, '${p.portfolioName || p.name}')" style="padding: 0.25rem;"><i class="fa-solid fa-pencil"></i></button>
                        <button class="btn icon-btn delete-btn" onclick="event.stopPropagation(); deletePortfolio(${p.id})" style="padding: 0.25rem; color: var(--danger);"><i class="fa-solid fa-trash"></i></button>
                    </div>
                </div>
                <div class="card-body">
                    <p>Assets: ${p.assets ? p.assets.length : 0}</p>
                    <p style="color: ${(p.totalProfitLoss || 0) >= 0 ? 'var(--success)' : 'var(--danger)'}; font-weight: 500;">
                        Profit: ${(p.totalProfitLoss || 0) >= 0 ? '+' : ''}$${(p.totalProfitLoss || 0).toLocaleString()}
                    </p>
                </div>
            </div>
        `).join('');

        attachPortfolioClickHandlers(portfolios);

    } catch (error) {
        console.error(error);
        list.innerHTML = '<div class="empty-state">Error loading portfolios.</div>';
    }
}

async function editPortfolio(id, oldName) {
    const newName = prompt("Enter new name for portfolio:", oldName);
    if (newName && newName !== oldName) {
        try {
            // Fetch current details first to keep other fields
            const response = await axios.get(`/portfolios/${id}`);
            const portfolio = response.data;
            portfolio.portfolioName = newName;

            await axios.put(`/portfolios/${id}`, portfolio);
            loadPortfolios();
        } catch (error) {
            console.error(error);
            alert("Failed to update portfolio.");
        }
    }
}

async function deletePortfolio(id) {
    if (confirm("Are you sure you want to delete this portfolio? This cannot be undone.")) {
        try {
            await axios.delete(`/portfolios/${id}`);
            loadPortfolios();
            // Hide details if the deleted one was showing
            if (currentPortfolioId === id) {
                document.querySelector('.transaction-history-section').style.display = 'none';
                document.getElementById('chart-section').style.display = 'none';
            }
        } catch (error) {
            console.error(error);
            alert("Failed to delete portfolio.");
        }
    }
}


async function createPortfolio() {
    const name = prompt("Enter Portfolio Name (e.g., Tech Growth):");
    if (!name) return;

    // Hardcoded user details for MVP since we don't have auth/login flow yet
    const payload = {
        name: "User",
        email: "user@example.com",
        portfolioName: name,
        investmentGoal: "Growth",
        riskPreference: "Medium",
        totalInvestment: 100000 // Initial Cash/Investment
    };

    try {
        await axios.post('/portfolios', payload);
        alert("Portfolio created!");
        loadPortfolios();
    } catch (error) {
        console.error(error);
        alert("Failed to create portfolio.");
    }
}


// Transaction History Logic
let currentPortfolioId = null;
let currentPage = 0;
const PAGE_SIZE = 10;

async function loadTransactionHistory(portfolioId, page = 0) {
    currentPortfolioId = portfolioId;
    currentPage = page;

    // Show Analyze Button
    const analyzeBtn = document.getElementById('analyze-btn');
    if (analyzeBtn) analyzeBtn.style.display = 'flex';

    const tbody = document.getElementById('transaction-table-body');
    tbody.innerHTML = '<tr><td colspan="6" style="text-align: center; padding: 1rem;"><i class="fa-solid fa-spinner fa-spin"></i> Loading...</td></tr>';

    try {
        const response = await axios.get(`/transactions/portfolio/${portfolioId}?page=${page}&size=${PAGE_SIZE}`);
        const data = response.data; // Page object

        if (data.empty) {
            tbody.innerHTML = '<tr><td colspan="6" style="text-align: center; padding: 1rem; color: var(--text-muted);">No transactions found.</td></tr>';
            updatePaginationControls(data);
            return;
        }

        tbody.innerHTML = data.content.map(t => `
            <tr style="border-bottom: 1px solid var(--border);">
                <td style="padding: 1rem; color: var(--text-muted);">${new Date(t.displayDate || t.transactionDate).toLocaleDateString()} ${new Date(t.displayDate || t.transactionDate).toLocaleTimeString()}</td>
                <td style="padding: 1rem; font-weight: 500; color: ${t.transactionType === 'BUY' ? 'var(--success)' : 'var(--danger)'}">${t.transactionType}</td>
                <td style="padding: 1rem; font-weight: 600;">${t.symbol || (t.asset ? t.asset.assetName : 'N/A')}</td>
                <td style="padding: 1rem;">${t.quantity}</td>
                <td style="padding: 1rem;">$${t.amount ? (t.amount / t.quantity).toFixed(2) : '0.00'}</td>
                <td style="padding: 1rem;">$${t.amount ? t.amount.toFixed(2) : '0.00'}</td>
            </tr>
        `).join('');

        updatePaginationControls(data);

        // Scroll to history section
        document.querySelector('.transaction-history-section').scrollIntoView({ behavior: 'smooth' });

        // Load Chart
        loadPortfolioPerformance(portfolioId);

    } catch (error) {
        console.error("Error loading history", error);
        tbody.innerHTML = '<tr><td colspan="6" style="text-align: center; color: var(--danger); padding: 1rem;">Failed to load history.</td></tr>';
    }
}

let performanceChart = null;

async function loadPortfolioPerformance(portfolioId) {
    const chartSection = document.getElementById('chart-section');
    chartSection.style.display = 'block';

    try {
        const response = await axios.get(`/portfolios/${portfolioId}/performance`);
        const history = response.data;

        const labels = history.map(p => p.date);
        const data = history.map(p => p.value);

        const ctx = document.getElementById('performanceChart').getContext('2d');

        if (performanceChart) {
            performanceChart.destroy();
        }

        // Create Gradient
        const gradient = ctx.createLinearGradient(0, 0, 0, 400);
        gradient.addColorStop(0, 'rgba(99, 102, 241, 0.5)'); // Primary color
        gradient.addColorStop(1, 'rgba(99, 102, 241, 0.0)');

        performanceChart = new Chart(ctx, {
            type: 'line',
            data: {
                labels: labels,
                datasets: [{
                    label: 'Portfolio Value',
                    data: data,
                    borderColor: '#6366f1',
                    backgroundColor: gradient,
                    borderWidth: 2,
                    fill: true,
                    tension: 0.4, // Smooth curve
                    pointRadius: 0
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        display: false
                    },
                    tooltip: {
                        mode: 'index',
                        intersect: false,
                    }
                },
                scales: {
                    x: {
                        grid: {
                            display: false,
                            drawBorder: false
                        },
                        ticks: {
                            color: '#94a3b8'
                        }
                    },
                    y: {
                        grid: {
                            color: '#2d3748',
                            borderDash: [5, 5]
                        },
                        ticks: {
                            color: '#94a3b8',
                            callback: function (value) {
                                return '$' + value.toLocaleString();
                            }
                        }
                    }
                },
                interaction: {
                    mode: 'nearest',
                    axis: 'x',
                    intersect: false
                }
            }
        });

    } catch (error) {
        console.error("Error loading performance chart", error);
        chartSection.style.display = 'none';
    }
}

function updatePaginationControls(pageData) {
    const prevBtn = document.getElementById('prev-page-btn');
    const nextBtn = document.getElementById('next-page-btn');

    prevBtn.disabled = pageData.first;
    nextBtn.disabled = pageData.last;

    // Clear old listeners to avoid dupes (simple way)
    const newPrev = prevBtn.cloneNode(true);
    const newNext = nextBtn.cloneNode(true);
    prevBtn.parentNode.replaceChild(newPrev, prevBtn);
    nextBtn.parentNode.replaceChild(newNext, nextBtn);

    newPrev.addEventListener('click', () => loadTransactionHistory(currentPortfolioId, currentPage - 1));
    newNext.addEventListener('click', () => loadTransactionHistory(currentPortfolioId, currentPage + 1));
}

// Add click listener to portfolio cards
function attachPortfolioClickHandlers(portfolios) {
    console.log("Attaching click handlers to portfolios:", portfolios);
    const cards = document.querySelectorAll('.portfolio-card');
    cards.forEach((card, index) => {
        card.addEventListener('click', () => {
            console.log("Card clicked:", index);
            // Get ID from portfolios array assuming order matches
            const portfolio = portfolios[index];
            if (portfolio && portfolio.id) {
                console.log("Loading history for portfolio ID:", portfolio.id);
                loadTransactionHistory(portfolio.id);
            } else {
                console.warn("Portfolio ID not found, using index fallback");
                loadTransactionHistory(index + 1);
            }
        });
    });
}


async function analyzeCurrentPortfolio() {
    if (!currentPortfolioId) return;

    const btn = document.getElementById('analyze-btn');
    const originalText = btn.innerHTML;
    btn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Analyzing...';
    btn.disabled = true;

    try {
        const response = await axios.get(`/api/ai/analyze-portfolio/${currentPortfolioId}`);
        alert("AI Analysis:\n\n" + response.data);
    } catch (error) {
        console.error(error);
        alert("Failed to analyze portfolio.");
    } finally {
        btn.innerHTML = originalText;
        btn.disabled = false;
    }
}
