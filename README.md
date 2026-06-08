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

Technologies Used<br>
Android (Java/Kotlin)<br>
Firebase Authentication<br>
Firebase Realtime Database<br>
RecyclerView for chat lists<br>
XML Layouts for UI design<br>


How to Run the App<br>
Install Android Studio<br>
Clone the repository<br>
Add your google-services.json file under:<br>
app/<br>
Enable Firebase Authentication (Email/Password)<br>
Set up Firebase Realtime Database in test mode<br>
Build and run the project on an emulator or device<br>

Firebase Setup<br>
Create a Firebase project<br>
Add an Android app with your package name<br>
Download google-services.json<br>
Enable Authentication → Email/Password<br>

Create a Realtime Database<br>
Set rules (for development):<br>
Code
{
  "rules": {
    ".read": true,
    ".write": true
  }
}

Screenshots
<img width="1509" height="823" alt="Screenshot 2026-06-08 125916" src="https://github.com/user-attachments/assets/1c69e53a-7587-4f4f-a834-2f94d6815c5b" />


Future Improvements<br>
Push notifications (FCM)<br>
Message read receipts<br>
Profile pictures<br>
Group chats<br>
Dark mode<br>
License<br>
This project is open‑source. You may modify or distribute it as needed.<br>
