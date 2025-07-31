document.addEventListener('DOMContentLoaded', function() {
    // DOM Elements
    const cityInput = document.getElementById('cityInput');
    const searchBtn = document.getElementById('searchBtn');
    const offlineToggle = document.getElementById('offlineToggle');
    const weatherContainer = document.getElementById('weatherContainer');
    const errorMessage = document.getElementById('errorMessage');
    const loadingIndicator = document.getElementById('loadingIndicator');
    const dataSource = document.getElementById('dataSource');
    const offlineLabel = document.getElementById('offlineLabel');

    // Constants
    const CACHE_PREFIX = 'weather_';
    const CACHE_TTL = 24 * 60 * 60 * 1000; // 24 hours cache validity

    // Event Listeners
    searchBtn.addEventListener('click', fetchWeather);
    cityInput.addEventListener('keypress', function(e) {
        if (e.key === 'Enter') fetchWeather();
    });

    offlineToggle.addEventListener('change', updateOfflineStatus);

    // Initialize
    updateOfflineStatus();

    // Main Functions
    async function fetchWeather() {
        const city = cityInput.value.trim();
        if (!city) {
            showError('Please enter a city name');
            return;
        }

        clearDisplay();
        showLoading();

        try {
            let weatherData;

            if (offlineToggle.checked) {
                // Offline mode - use cache only
                weatherData = getCachedWeather(city);
                if (!weatherData) {
                    throw new Error('No cached data available for this city');
                }
                weatherData.source = 'cache (offline mode)';
            } else {
                // Online mode - try API first
                try {
                    //const response = await fetch(`http://localhost:8080/forecast?city=${encodeURIComponent(city)}`);
                    const response = await fetch(`/forecast?city=${encodeURIComponent(city)}`);

                    if (!response.ok) {
                        const errorData = await response.json();
                        throw new Error(errorData.message || 'API request failed');
                    }

                    weatherData = await response.json();
                    weatherData.source = 'live API';
                    cacheWeatherData(city, weatherData);
                } catch (apiError) {
                    console.warn('API request failed:', apiError);
                    if (apiError.message.toLowerCase().includes("city not found")) {
                        throw new Error(
                          "City not found. Please check the spelling and try again."
                        );
                      }
                    weatherData = getCachedWeather(city);
                    if (!weatherData) {
                        throw new Error('Service unavailable and no cached data');
                    }
                    weatherData.source = 'cache (API unavailable)';
                }
            }

            displayWeather(weatherData);
        } catch (error) {
            showError(error.message);
        } finally {
            hideLoading();
        }
    }

    function displayWeather(weatherData) {
        weatherContainer.innerHTML = '';

        if (!weatherData?.forecastData?.length) {
            weatherContainer.innerHTML = `
                <div class="no-forecast">
                    No forecast data available for ${weatherData.city}
                </div>
            `;
            return;
        }

        // Display city name
        const cityHeader = document.createElement('h2');
        cityHeader.textContent = `Weather for ${weatherData.city}`;
        weatherContainer.appendChild(cityHeader);

        // Display forecast cards
        weatherData.forecastData.forEach(day => {
            const card = document.createElement('div');
            card.className = 'weather-card';

            // Convert Kelvin to Celsius
            const minTempC = Math.round(day.min_temp - 273.15);
            const maxTempC = Math.round(day.max_temp - 273.15);

            card.innerHTML = `
                <div class="weather-date">${formatDate(day.dt_txt)}</div>
                <div class="weather-temps">
                    <div class="temp-value">
                        <span class="temp-label">High</span>
                        <div>${maxTempC}°C</div>
                    </div>
                    <div class="temp-value">
                        <span class="temp-label">Low</span>
                        <div>${minTempC}°C</div>
                    </div>
                </div>
                <div class="weather-alerts">
                    ${day.alerts.map(alert => `
                        <span class="alert-item ${getAlertClass(alert)}">${alert}</span>
                    `).join('')}
                </div>
            `;

            weatherContainer.appendChild(card);
        });

        // Display data source
        dataSource.textContent = `Data source: ${weatherData.source}`;
    }

    // Helper Functions
    function formatDate(dateStr) {
        const date = new Date(dateStr);
        return date.toLocaleDateString('en-US', {
            weekday: 'short',
            month: 'short',
            day: 'numeric'
        });
    }

    function getAlertClass(alert) {
        if (alert.includes('umbrella')) return 'alert-rain';
        if (alert.includes('sunscreen')) return 'alert-high-temp';
        if (alert.includes('windy')) return 'alert-wind';
        if (alert.includes('Storm')) return 'alert-storm';
        return '';
    }

    function cacheWeatherData(city, data) {
        const cacheEntry = {
            data: data,
            timestamp: Date.now()
        };
        localStorage.setItem(
            `${CACHE_PREFIX}${city.toLowerCase()}`,
            JSON.stringify(cacheEntry)
        );
    }

    function getCachedWeather(city) {
        const cacheEntry = localStorage.getItem(
            `${CACHE_PREFIX}${city.toLowerCase()}`
        );

        if (!cacheEntry) return null;

        const { data, timestamp } = JSON.parse(cacheEntry);

        // Check if cache is still valid
        if (Date.now() - timestamp > CACHE_TTL) {
            localStorage.removeItem(`${CACHE_PREFIX}${city.toLowerCase()}`);
            return null;
        }

        return data;
    }

    function updateOfflineStatus() {
        if (offlineToggle.checked) {
            offlineLabel.textContent = 'Offline Mode (ON)';
            offlineLabel.style.color = 'var(--offline-color)';
        } else {
            offlineLabel.textContent = 'Offline Mode (OFF)';
            offlineLabel.style.color = 'var(--online-color)';
        }
    }

    function clearDisplay() {
        weatherContainer.innerHTML = '';
        errorMessage.style.display = 'none';
        errorMessage.textContent = '';
        dataSource.textContent = '';
    }

    function showLoading() {
        loadingIndicator.style.display = 'block';
    }

    function hideLoading() {
        loadingIndicator.style.display = 'none';
    }

    function showError(message) {
        errorMessage.textContent = message;
        errorMessage.style.display = 'block';
    }
});
