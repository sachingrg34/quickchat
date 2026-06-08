package com.example.chatapp


data class UserModel(
    val fullname: String = "",
    val email: String = "",
    val phone: String = "",
    var lastMessage: String = "",
    var unreadCount: Int = 0,
    var lastTimestamp: Long = 0L
)
