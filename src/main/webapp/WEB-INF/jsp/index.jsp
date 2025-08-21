<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Manas AI Gateway</title>
    <style>
        @import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;700&family=Lora:ital,wght@0,400;0,700;1,400&display=swap');
        :root {
            --bg-color: #f8f9fa; --text-color: #212529; --card-bg: #ffffff;
            --border-color: #dee2e6; --primary-color: #007bff; --primary-hover: #0056b3;
            --font-main: 'Inter', sans-serif; --font-reading: 'Lora', serif;
        }
        body.dark-mode {
            --bg-color: #121212; --text-color: #e0e0e0; --card-bg: #1e1e1e;
            --border-color: #444; --primary-color: #3793ff; --primary-hover: #5fa4ff;
        }
        body.reading-mode {
            --bg-color: #fdf1d3; --text-color: #4d3d2b; --card-bg: #fbf5e2;
            --border-color: #e9e0cf; --font-main: 'Lora', serif; line-height: 1.7;
        }
        body {
            font-family: var(--font-main); margin: 0; padding: 2rem;
            background-color: var(--bg-color); color: var(--text-color);
            transition: background-color 0.3s, color 0.3s;
        }
        .container { max-width: 900px; margin: 0 auto; display: flex; gap: 2rem; }
        .main-column { flex-grow: 1; display: flex; flex-direction: column; height: calc(100vh - 4rem); }
        .history-column { flex-basis: 250px; flex-shrink: 0; }
        .header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem; }
        .theme-switcher button {
            padding: 0.5rem 1rem; font-size: 0.9rem; border: 1px solid var(--border-color);
            border-radius: 20px; background-color: transparent; color: var(--text-color); cursor: pointer;
        }
        .theme-switcher button.active { background-color: var(--primary-color); color: white; }
        .chat-container {
            background-color: var(--card-bg); border-radius: 12px;
            box-shadow: 0 8px 24px rgba(0, 0, 0, 0.05); border: 1px solid var(--border-color);
            flex-grow: 1; display: flex; flex-direction: column;
        }
        .chat-log { flex-grow: 1; padding: 1.5rem; overflow-y: auto; }
        .chat-message { margin-bottom: 1.5rem; }
        .chat-message .role { font-weight: bold; margin-bottom: 0.5rem; }
        .chat-message.user .role { color: var(--primary-color); }
        .chat-message.assistant .content { background-color: var(--bg-color); padding: 1rem; border-radius: 8px; }
        .model-info { font-size: 0.8rem; color: #6c757d; margin-top: 0.5rem; font-style: italic; }
        .form-container { padding: 1.5rem; border-top: 1px solid var(--border-color); }
        .form-row { display: flex; gap: 1rem; align-items: center; }
        textarea {
            flex-grow: 1; padding: 0.75rem 1rem; font-size: 1rem; border: 1px solid var(--border-color);
            border-radius: 8px; background-color: var(--bg-color); color: var(--text-color); resize: vertical;
        }
        select {
            padding: 0.75rem; border: 1px solid var(--border-color); border-radius: 8px;
            background-color: var(--bg-color); color: var(--text-color);
        }
        button[type="submit"] {
            padding: 0.8rem; font-size: 1.1rem; font-weight: bold; color: #fff;
            background-color: var(--primary-color); border: none; border-radius: 8px; cursor: pointer;
        }
        .history-header { display: flex; justify-content: space-between; align-items: center; }
        .new-chat-btn {
            padding: 0.4rem 0.8rem; font-size: 0.8rem; border: 1px solid var(--border-color);
            border-radius: 5px; background-color: var(--card-bg); color: var(--text-color); cursor: pointer;
            text-decoration: none;
        }
    </style>
</head>
<body>
<div class="container">
    <div class="history-column">
        <div class="history-header">
            <h3>Chat</h3>
            <a href="/new-chat" class="new-chat-btn">+ New Chat</a>
        </div>
        <%-- You can add a list of past chat sessions here later --%>
    </div>
    <div class="main-column">
        <header class="header">
            <h1>Manas AI Gateway</h1>
            <div class="theme-switcher">
                <button id="light-btn">Light</button>
                <button id="dark-btn">Dark</button>
                <button id="read-btn">Reading</button>
            </div>
        </header>
        <div class="chat-container">
            <div class="chat-log" id="chat-log">
                <c:if test="${empty chatHistory}">
                    <div class="chat-message">
                        <div class="content" style="text-align: center; background: none;">
                            Start a conversation by typing in the box below.
                        </div>
                    </div>
                </c:if>
                <c:forEach var="message" items="${chatHistory}">
                    <div class="chat-message ${message.role}">
                        <div class="role">${message.role == 'assistant' ? 'AI' : 'You'}</div>
                        <div class="content">${message.content}</div>
                        <c:if test="${not empty message.model}">
                            <div class="model-info">${message.model}</div>
                        </c:if>
                    </div>
                </c:forEach>
            </div>
            <div class="form-container">
                <form id="ai-form" action="/ask" method="post">
                    <textarea id="query-input" name="query" rows="2" placeholder="Enter your message..." required></textarea>
                    <div class="form-row">
                        <select name="model" id="model-select">
                            <option value="both">Both Models</option>
                            <option value="openai">OpenAI Only</option>
                            <option value="gemini">Gemini Only</option>
                        </select>
                        <button type="submit" id="submit-btn">Send</button>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>

<script>
    const chatLog = document.getElementById('chat-log');

    // --- AUTO-SCROLL TO BOTTOM ---
    // This line ensures the chat view is always scrolled to the latest message on page load.
    chatLog.scrollTop = chatLog.scrollHeight;

    // --- THEME SWITCHER ---
    const lightBtn = document.getElementById('light-btn');
    const darkBtn = document.getElementById('dark-btn');
    const readBtn = document.getElementById('read-btn');
    const body = document.body;
    const themeButtons = [lightBtn, darkBtn, readBtn];

    function setActiveButton(activeBtn) {
        themeButtons.forEach(btn => btn.classList.remove('active'));
        if (activeBtn) activeBtn.classList.add('active');
    }

    function setTheme(theme) {
        body.classList.remove('dark-mode', 'reading-mode');
        localStorage.setItem('theme', theme);
        if (theme === 'dark') { body.classList.add('dark-mode'); setActiveButton(darkBtn); }
        else if (theme === 'reading') { body.classList.add('reading-mode'); setActiveButton(readBtn); }
        else { setActiveButton(lightBtn); }
    }

    lightBtn.addEventListener('click', () => setTheme('light'));
    darkBtn.addEventListener('click', () => setTheme('dark'));
    readBtn.addEventListener('click', () => setTheme('reading'));

    document.addEventListener('DOMContentLoaded', () => {
        const savedTheme = localStorage.getItem('theme') || 'light';
        setTheme(savedTheme);
    });
</script>
</body>
</html>
