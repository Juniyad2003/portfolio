(function () {
    const THEME_KEY = 'portfolio-theme';

    function initTheme() {
        const savedTheme = localStorage.getItem(THEME_KEY);
        const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
        
        const shouldBeDark = savedTheme ? savedTheme === 'dark' : prefersDark;
        
        if (shouldBeDark) {
            document.body.classList.add('dark-theme');
            updateThemeIcon(true);
        } else {
            document.body.classList.remove('dark-theme');
            updateThemeIcon(false);
        }
    }

    function updateThemeIcon(isDark) {
        const themeToggleBtn = document.getElementById('theme-toggle');
        if (themeToggleBtn) {
            const icon = themeToggleBtn.querySelector('i');
            if (icon) {
                if (isDark) {
                    icon.classList.remove('fa-moon');
                    icon.classList.add('fa-sun');
                } else {
                    icon.classList.remove('fa-sun');
                    icon.classList.add('fa-moon');
                }
            }
        }
    }

    function toggleTheme() {
        const isDark = document.body.classList.toggle('dark-theme');
        localStorage.setItem(THEME_KEY, isDark ? 'dark' : 'light');
        updateThemeIcon(isDark);
    }

    function setupTheme() {
        initTheme();
        
        const themeToggleBtn = document.getElementById('theme-toggle');
        if (themeToggleBtn) {
            themeToggleBtn.addEventListener('click', toggleTheme);
        }
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', setupTheme);
    } else {
        setupTheme();
    }
    
    window.addEventListener('load', setupTheme);
})();
