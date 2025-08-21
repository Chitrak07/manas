🧠 Manas AI Gateway
<p align="center">
<img src="https://i.imgur.com/8a6Z1Yg.png" alt="Manas AI Gateway Banner" width="700"/>
</p>

<p align="center">
<em>A modern, multi-model AI chat application built with Java Spring Boot.</em>
<br/><br/>
<img src="https://img.shields.io/badge/Java-17%2B-blue?logo=java&logoColor=white" alt="Java 17+">
<img src="https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen?logo=spring&logoColor=white" alt="Spring Boot 3.2.5">
<img src="https://img.shields.io/badge/Frontend-JSP-orange" alt="JSP Frontend">
</p>

Manas (from Sanskrit: मनस्, meaning "mind" or "intellect") is a web-based AI gateway that allows you to have conversational chats with multiple leading AI models simultaneously. This project serves as a powerful demonstration of how to integrate different AI services into a single, cohesive, and modern-looking Java web application.

✨ Features
🧠 Conversational Memory: The AI remembers the context of your current chat, allowing for follow-up questions and more natural interactions.

🔄 Multi-Chat History: Manage multiple, separate conversations. Switch between different chat sessions and pick up right where you left off.

🤖 Multi-Model Support: Get responses from both OpenAI (GPT) and Google Gemini in the same chat.

🎨 Modern Theming: Switch between Light, Dark, and a vintage Reading mode to suit your preference. Your choice is saved locally!

✂️ Model Selection: Choose to query a specific model or both at the same time to save on API costs.

🚀 Built with Spring Boot: A robust and scalable backend powered by the latest Spring Boot 3.x.

🔐 Secure API Key Handling: API keys are managed securely using environment variables, not hardcoded in the source code.

🛠️ Tech Stack
Backend: Java 17, Spring Boot 3.2.5, Spring MVC, Spring WebFlux (WebClient)

Frontend: Jakarta Server Pages (JSP), JSTL, HTML5, CSS3, JavaScript

Build Tool: Apache Maven

Server: Embedded Apache Tomcat

🚀 Getting Started
Follow these steps to get the Manas AI Gateway running on your local machine.

Prerequisites
Java Development Kit (JDK): Version 17 or higher.

Apache Maven: To build the project and manage dependencies.

IntelliJ IDEA: The recommended IDE for this project.

API Keys: You will need active API keys from:

OpenAI

Google AI Studio (for Gemini)

1. Clone the Repository
Open your terminal and clone the project from GitHub:

git clone https://github.com/Chitrak07/manas.git
cd manas

2. Open in IntelliJ IDEA
Open the cloned manas folder as a new project in IntelliJ. The IDE will automatically detect it as a Maven project and download the required dependencies.

3. Set Up API Keys (Crucial Step)
We will use IntelliJ's run configuration to securely manage your API keys.

In the top-right corner of IntelliJ, click on the run configuration dropdown (it might say "Add Configuration...") and select "Edit Configurations...".

Click the + icon and choose "Spring Boot".

Give the configuration a name (e.g., ManasApplication).

For the Main class, click the small icon on the right and select ManasApplication.

Find the "Environment variables" field and click the icon to open the editor.

Add two new variables:

Name: OPENAI_API_KEY | Value: your_openai_secret_key_here

Name: GEMINI_API_KEY | Value: your_gemini_secret_key_here

Click OK, then Apply, and OK again.

<p align="center">
<img src="https://i.imgur.com/sB8v2zO.png" alt="IntelliJ Environment Variables Setup" width="600"/>
</p>

4. Build and Run
You're all set! To run the application:

Make sure your new ManasApplication configuration is selected in the top-right corner.

Click the green play button (▶️) next to it.

Once the console shows Started ManasApplication..., open your web browser and go to:

http://localhost:8081

💡 How to Use
Start a Chat: Simply type your message in the text box at the bottom and press "Send."

Select a Model: Use the dropdown to choose whether you want to query OpenAI, Gemini, or both.

Switch Themes: Use the buttons in the top-right corner to change the UI theme.

Manage Chats:

Click the "+ New Chat" button in the history sidebar to start a new, separate conversation.

Click on any previous chat in the history list to view it and continue the conversation.

This project is a great starting point for anyone looking to build modern, AI-powered applications with the Java ecosystem. Enjoy!
