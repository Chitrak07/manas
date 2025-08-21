<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale-1.0">
    <title>Manas AI Gateway</title>
    <style>
        body {
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
            margin: 0;
            padding: 2rem;
            background-color: #f8f9fa;
            color: #212529;
        }
        .container {
            max-width: 900px;
            margin: 0 auto;
            background-color: #ffffff;
            padding: 2rem;
            border-radius: 8px;
            box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
        }
        h1 {
            text-align: center;
            color: #343a40;
            margin-bottom: 1.5rem;
        }
        form {
            display: flex;
            flex-direction: column;
            gap: 1rem;
        }
        textarea {
            width: 100%;
            padding: 0.75rem;
            font-size: 1rem;
            border: 1px solid #ced4da;
            border-radius: 4px;
            box-sizing: border-box;
        }
        button {
            padding: 0.75rem;
            font-size: 1.1rem;
            font-weight: bold;
            color: #fff;
            background-color: #007bff;
            border: none;
            border-radius: 4px;
            cursor: pointer;
            transition: background-color 0.2s;
        }
        button:hover {
            background-color: #0056b3;
        }
        .results-grid {
            margin-top: 2rem;
            display: grid;
            grid-template-columns: 1fr;
            gap: 1.5rem;
        }
        .result-card {
            background-color: #f8f9fa;
            border: 1px solid #dee2e6;
            border-radius: 4px;
            padding: 1.5rem;
        }
        .result-card h2 {
            margin-top: 0;
            color: #495057;
            border-bottom: 2px solid #007bff;
            padding-bottom: 0.5rem;
        }
        pre {
            white-space: pre-wrap;
            word-wrap: break-word;
            background-color: #e9ecef;
            padding: 1rem;
            border-radius: 4px;
            font-family: "SF Mono", "Fira Code", monospace;
            font-size: 0.9rem;
        }
        .query-display {
            margin-top: 2rem;
            padding: 1rem;
            background-color: #e9ecef;
            border-left: 5px solid #007bff;
        }
    </style>
</head>
<body>
<div class="container">
    <h1>Manas AI Gateway</h1>
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
                <pre>${openAIResponse}</pre>
            </div>
        </c:if>

        <c:if test="${not empty geminiResponse}">
            <div class="result-card">
                <h2>Google Gemini Response</h2>
                <pre>${geminiResponse}</pre>
            </div>
        </c:if>
    </div>
</div>
</body>
</html>
