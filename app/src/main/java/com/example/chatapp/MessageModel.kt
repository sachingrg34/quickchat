package com.example.chatapp

import com.google.firebase.database.Exclude

data class MessageModel(
    var senderEmail: String = "",
    var senderName: String = "",
    var receiverEmail: String = "",
    var receiverName: String = "",
    var message: String = "",
    var timestamp: Long = 0, // MUST be var
    var seen: Boolean = false,
    var messageId: String = "",
    var chatId: String = "",
    @get:Exclude
    var isSelected: Boolean = false
)