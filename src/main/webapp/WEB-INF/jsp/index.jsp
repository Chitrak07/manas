<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Manas AI Gateway</title>
    <style>
        /* Modern Font */
        @import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;700&family=Lora:ital,wght@0,400;0,700;1,400&display=swap');

        /* CSS Variables for Theming */
        :root {
            --bg-color: #f8f9fa;
            --text-color: #212529;
            --card-bg: #ffffff;
            --border-color: #dee2e6;
            --primary-color: #007bff;
            --primary-hover: #0056b3;
            --font-main: 'Inter', sans-serif;
            --font-reading: 'Lora', serif;
        }

        body.dark-mode {
            --bg-color: #121212;
            --text-color: #e0e0e0;
            --card-bg: #1e1e1e;
            --border-color: #444;
            --primary-color: #3793ff;
            --primary-hover: #5fa4ff;
        }

        body.reading-mode {
            --bg-color: #fdf1d3; /* More yellowish vintage paper */
            --text-color: #4d3d2b; /* Darker brown ink color */
            --card-bg: #fbf5e2;
            --border-color: #e9e0cf;
            --font-main: 'Lora', serif;
            line-height: 1.7;
        }

        body {
            font-family: var(--font-main);
            margin: 0;
            padding: 2rem;
            background-color: var(--bg-color);
            color: var(--text-color);
            transition: background-color 0.3s, color 0.3s;
        }
        .container {
            max-width: 900px;
            margin: 0 auto;
        }
        .header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 2rem;
        }
        h1 {
            color: var(--text-color);
            margin: 0;
        }
        .theme-switcher {
            display: flex;
            gap: 0.5rem;
        }
        .theme-switcher button {
            padding: 0.5rem 1rem;
            font-size: 0.9rem;
            border: 1px solid var(--border-color);
            border-radius: 20px;
            background-color: transparent;
            color: var(--text-color);
            cursor: pointer;
            transition: background-color 0.2s, color 0.2s;
        }
        .theme-switcher button.active {
            background-color: var(--primary-color);
            color: white;
            border-color: var(--primary-color);
        }
        .main-content {
            background-color: var(--card-bg);
            padding: 2rem;
            border-radius: 12px;
            box-shadow: 0 8px 24px rgba(0, 0, 0, 0.05);
            transition: background-color 0.3s;
            border: 1px solid var(--border-color);
        }
        body.dark-mode .main-content {
            box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
        }
        form {
            display: flex;
            flex-direction: column;
            gap: 1rem;
        }
        textarea {
            width: 100%;
            padding: 0.75rem 1rem;
            font-size: 1rem;
            font-family: var(--font-main);
            border: 1px solid var(--border-color);
            border-radius: 8px;
            box-sizing: border-box;
            background-color: var(--bg-color);
            color: var(--text-color);
            resize: vertical;
        }
        button[type="submit"] {
            padding: 0.8rem;
            font-size: 1.1rem;
            font-weight: bold;
            color: #fff;
            background-color: var(--primary-color);
            border: none;
            border-radius: 8px;
            cursor: pointer;
            transition: background-color 0.2s;
        }
        button[type="submit"]:hover {
            background-color: var(--primary-hover);
        }
        .results-grid {
            margin-top: 2rem;
            display: grid;
            grid-template-columns: 1fr;
            gap: 1.5rem;
        }
        .result-card {
            border: 1px solid var(--border-color);
            border-radius: 8px;
            padding: 1.5rem;
            background-color: var(--bg-color);
        }
        .result-card h2 {
            margin-top: 0;
            color: var(--text-color);
            border-bottom: 2px solid var(--primary-color);
            padding-bottom: 0.5rem;
        }
        .response-content {
            font-size: 1rem;
            line-height: 1.6;
        }
        .model-info {
            text-align: right;
            font-size: 0.8rem;
            color: #6c757d;
            margin-top: 1rem;
            font-style: italic;
        }
        .query-display {
            margin-top: 2rem;
            padding: 1rem;
            background-color: var(--bg-color);
            border-left: 5px solid var(--primary-color);
        }
    </style>
</head>
<body>
<div class="container">
    <header class="header">
        <h1>Manas AI Gateway</h1>
        <div class="theme-switcher">
            <button id="light-btn">Light</button>
            <button id="dark-btn">Dark</button>
            <button id="read-btn">Reading</button>
        </div>
    </header>

    <div class="main-content">
        <form action="/ask" method="post">
            <textarea name="query" rows="4" placeholder="Enter your query here..." required></textarea>
            <button type="submit">Ask All AIs</button>
        </form>

        <c:if test="${not empty query}">
            <div class="query-display">
                <strong>Your Query:</strong>
                <p>${query}</p>
            </div>
        </c:if>

        <div class="results-grid">
            <c:if test="${not empty openAIResponse}">
                <div class="result-card">
                    <h2>OpenAI GPT Response</h2>
                    <div class="response-content">${openAIResponse}</div>
                    <p class="model-info">Model: ${openAIModel}</p>
                </div>
            </c:if>

            <c:if test="${not empty geminiResponse}">
                <div class="result-card">
                    <h2>Google Gemini Response</h2>
                    <div class="response-content">${geminiResponse}</div>
                    <p class="model-info">Model: ${geminiModel}</p>
                </div>
            </c:if>
        </div>
    </div>
</div>

<script>
    const lightBtn = document.getElementById('light-btn');
    const darkBtn = document.getElementById('dark-btn');
    const readBtn = document.getElementById('read-btn');
    const body = document.body;

    const themeButtons = [lightBtn, darkBtn, readBtn];

    function setActiveButton(activeBtn) {
        themeButtons.forEach(btn => {
            btn.classList.remove('active');
        });
        if (activeBtn) {
            activeBtn.classList.add('active');
        }
    }

    function setTheme(theme) {
        body.classList.remove('dark-mode', 'reading-mode');
        localStorage.setItem('theme', theme);

        if (theme === 'dark') {
            body.classList.add('dark-mode');
            setActiveButton(darkBtn);
        } else if (theme === 'reading') {
            body.classList.add('reading-mode');
            setActiveButton(readBtn);
        } else {
            // Default to light theme
            setActiveButton(lightBtn);
        }
    }

    lightBtn.addEventListener('click', () => setTheme('light'));
    darkBtn.addEventListener('click', () => setTheme('dark'));
    readBtn.addEventListener('click', () => setTheme('reading'));

    // On page load, apply the saved theme from localStorage
    document.addEventListener('DOMContentLoaded', () => {
        const savedTheme = localStorage.getItem('theme') || 'light';
        setTheme(savedTheme);
    });
</script>

</body>
</html>
