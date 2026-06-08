package com.example.chatapp

import android.util.Log
import com.google.firebase.database.*

class FirebaseMessageManager {

    private val db = FirebaseDatabase.getInstance().getReference("messages")


    private fun safeEmail(email: String): String {
        return email
            .replace(".", "_")
            .replace("#", "_")
            .replace("$", "_")
            .replace("[", "_")
            .replace("]", "_")
    }


    private fun getChatId(user1: String, user2: String): String {
        val u1 = safeEmail(user1)
        val u2 = safeEmail(user2)

        return if (u1 < u2) "${u1}_${u2}" else "${u2}_${u1}"
    }


    fun sendMessage(
        senderEmail: String,
        senderName: String,
        receiverEmail: String,
        receiverName: String,
        message: String
    ) {

        val chatId = getChatId(senderEmail, receiverEmail)

        val msgRef = db.child(chatId).push()
        val msgId = msgRef.key ?: ""

        val msgData = MessageModel(
            senderEmail = senderEmail,
            senderName = senderName,
            receiverEmail = receiverEmail,
            receiverName = receiverName,
            message = message,
            timestamp = System.currentTimeMillis(),
            seen = false,
            messageId = msgId,
            chatId = chatId
        )

        msgRef.setValue(msgData)
        NotificationHelper.sendLocalNotification(
            title = senderName,
            message = message
        )
    }


    fun listenForMessages(
        user1: String,
        user2: String,
        callback: (ArrayList<MessageModel>) -> Unit
    ): ValueEventListener {

        val chatId = getChatId(user1, user2)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = ArrayList<MessageModel>()

                for (msgSnap in snapshot.children) {
                    val msg = msgSnap.getValue(MessageModel::class.java)
                    if (msg != null) list.add(msg)
                }

                callback(list)
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        db.child(chatId).addValueEventListener(listener)
        return listener
    }


    fun stopListening(user1: String, user2: String, listener: ValueEventListener) {
        val chatId = getChatId(user1, user2)
        db.child(chatId).removeEventListener(listener)
    }


    fun markAsSeen(myEmail: String, otherEmail: String) {
        val chatId = getChatId(myEmail, otherEmail)

        db.child(chatId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (msgSnap in snapshot.children) {
                    val msg = msgSnap.getValue(MessageModel::class.java)

                    if (msg != null && msg.receiverEmail == myEmail && !msg.seen) {
                        msgSnap.ref.child("seen").setValue(true)

                        Log.d(
                            "CHAT_STATUS",
                            "User $myEmail marked message from ${msg.senderEmail} as SEEN"
                        )
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }


    fun deleteMessage(chatId: String, messageId: String) {
        db.child(chatId).child(messageId).removeValue()
    }
    fun getLastMessageAndUnread(
        currentUserEmail: String,
        otherUserEmail: String,
        callback: (String, Int) -> Unit
    ) {
        val chatId = getChatId(currentUserEmail, otherUserEmail)

        db.child(chatId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                var lastMsg = ""
                var unread = 0

                for (msgSnap in snapshot.children) {
                    val msg = msgSnap.getValue(MessageModel::class.java)

                    if (msg != null) {
                        lastMsg = msg.message

                        if (msg.receiverEmail == currentUserEmail && !msg.seen) {
                            unread++
                        }
                    }
                }

                callback(lastMsg, unread)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

}
