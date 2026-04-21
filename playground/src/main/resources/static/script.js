function predict(text) {
    if (!text || text.trim() === '') {
        return;
    }

    const resultCard = document.getElementById('resultCard');
    const resultBars = document.getElementById('resultBars');

    resultCard.style.display = 'none';
    resultBars.innerHTML = '';

    fetch('/sentiment?text=' + encodeURIComponent(text))
        .then(response => response.json())
        .then(data => {
            let items = Array.isArray(data) ? data : [];

            items.forEach(item => {
                const label = item.className;
                const percentage = (item.probability * 100).toFixed(2);
                const isPositive = label.toLowerCase().includes('positive');
                const barColor = isPositive ? 'bg-success' : 'bg-danger';

                resultBars.insertAdjacentHTML('beforeend', `
                    <div class="mb-3">
                        <div class="d-flex justify-content-between align-items-center mb-1">
                            <span class="fw-semibold">${label}</span>
                            <span class="text-muted">${percentage}%</span>
                        </div>
                        <div class="progress" style="height: 24px;">
                            <div class="progress-bar ${barColor}" role="progressbar"
                                style="width: ${percentage}%"></div>
                        </div>
                    </div>`);
            });

            resultCard.style.display = 'block';
        })
        .catch(error => console.log(error));
}
