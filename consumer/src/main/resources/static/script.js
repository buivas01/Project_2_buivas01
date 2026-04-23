function checkFiles(files) {
    if (files.length != 1) {
        alert("Please upload exactly one file.");
        return;
    }

    const fileSize = files[0].size / 1024 / 1024;
    if (fileSize > 10) {
        alert("File too large (max. 10MB)");
        return;
    }

    document.getElementById("answerPart").classList.remove("hidden");

    const file = files[0];
    if (file) {
        document.getElementById("preview").src = URL.createObjectURL(files[0]);
    }

    document.getElementById("loadingPart").style.display = "block";
    document.getElementById("resultsPart").style.display = "none";

    const formData = new FormData();
    formData.append("image", files[0]);

    fetch('/analyze', {
        method: 'POST',
        body: formData
    }).then(response => {
        if (!response.ok) {
            document.getElementById("loadingPart").style.display = "none";
            alert("Server error: " + response.status + " " + response.statusText);
            return;
        }
        response.text().then(function (text) {
            try {
                const jsonData = JSON.parse(text);
                document.getElementById("loadingPart").style.display = "none";
                document.getElementById("resultsPart").style.display = "block";
                displayResults(jsonData);
            } catch (e) {
                document.getElementById("loadingPart").style.display = "none";
                alert("Error processing the response: " + e.message + "\nRaw: " + text);
            }
        });
    }).catch(error => {
        document.getElementById("loadingPart").style.display = "none";
        alert("Error uploading file: " + error);
    });
}

function displayResults(jsonData) {
    let classifications = [];

    if (Array.isArray(jsonData)) {
        classifications = jsonData.map(item => ({
            className: item.className || item.class || item.name || String(item),
            probability: parseFloat(item.probability || 0)
        }));
    } else if (jsonData.classes && Array.isArray(jsonData.classes)) {
        classifications = jsonData.classes.map(item => ({
            className: item.className || item.class || item.name,
            probability: parseFloat(item.probability || 0)
        }));
    } else if (typeof jsonData === 'object') {
        for (const [key, value] of Object.entries(jsonData)) {
            if (typeof value === 'number') {
                classifications.push({ className: key, probability: parseFloat(value) });
            }
        }
    }

    classifications.sort((a, b) => b.probability - a.probability);

    if (classifications.length > 0) {
        const top = classifications[0];
        document.getElementById("topLabel").textContent = top.className || "Unknown";
        document.getElementById("topPercentage").textContent = (parseFloat(top.probability) * 100).toFixed(1) + "%";
        document.getElementById("topResult").style.display = "flex";
    }

    let html = "";
    classifications.forEach((item, index) => {
        if (index === 0) return;
        const label = item.className || "Unknown";
        const pct = (parseFloat(item.probability) * 100).toFixed(1);
        html += `
            <div class="classification-item">
                <div class="classification-label">${label}</div>
                <div class="classification-bar">
                    <div class="progress">
                        <div class="progress-bar" role="progressbar" style="width: ${pct}%"></div>
                    </div>
                </div>
                <div class="classification-percentage">${pct}%</div>
            </div>
        `;
    });

    document.getElementById("classificationList").innerHTML = html;
}
