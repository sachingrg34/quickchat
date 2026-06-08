<h1 align="center">QuickChat – Real‑Time Mobile Messaging App</h1>

Overview
QuickChat is an Android‑based real‑time messaging application designed to provide fast, simple, and reliable one‑to‑one communication. It uses Firebase Authentication for secure login and Firebase Realtime Database for instant message delivery. The app includes features such as user registration, login, user search, typing indicators, timestamps, and message history.

Features<br>
User Authentication — Secure sign‑up and login using Firebase Authentication<br>
Real‑Time Messaging — Messages sync instantly using Firebase Realtime Database<br>
User Search — Find other registered users and start conversations<br>
Typing Indicators — Shows when the other user is typing<br>
Message Timestamps — Each message includes a time label<br>
Message History — Stores and loads previous chats<br>
Toast Notifications — Confirms when a message is sent<br>
Clean UI — Simple, intuitive, and responsive chat interface<br>

Technologies Used
Android (Java/Kotlin)
Firebase Authentication
Firebase Realtime Database
RecyclerView for chat lists
XML Layouts for UI design


How to Run the App
Install Android Studio
Clone the repository
Add your google-services.json file under:
app/
Enable Firebase Authentication (Email/Password)
Set up Firebase Realtime Database in test mode
Build and run the project on an emulator or device

Firebase Setup
Create a Firebase project
Add an Android app with your package name
Download google-services.json
Enable Authentication → Email/Password

Create a Realtime Database
Set rules (for development):
Code
{
  "rules": {
    ".read": true,
    ".write": true
  }
}

Screenshots
<img width="1509" height="823" alt="Screenshot 2026-06-08 125916" src="https://github.com/user-attachments/assets/1c69e53a-7587-4f4f-a834-2f94d6815c5b" />


Future Improvements
Push notifications (FCM)
Message read receipts
Profile pictures
Group chats
Dark mode
License
This project is open‑source. You may modify or distribute it as needed.
