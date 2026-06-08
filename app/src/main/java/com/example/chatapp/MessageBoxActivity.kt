package com.example.chatapp

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class MessageBoxActivity : AppCompatActivity() {

    private lateinit var senderEmail: String
    private lateinit var senderName: String
    private lateinit var receiverEmail: String
    private lateinit var receiverName: String

    private lateinit var messageManager: FirebaseMessageManager
    private lateinit var adapter: MessageAdapter
    private lateinit var recycler: RecyclerView
    private var lastMessageCount = 0


    private var messageListener: ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_message_box)


        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }


        senderEmail = intent.getStringExtra("senderEmail") ?: ""
        senderName = intent.getStringExtra("senderName") ?: ""
        receiverEmail = intent.getStringExtra("receiverEmail") ?: ""
        receiverName = intent.getStringExtra("receiverName") ?: ""
        listenForTyping()

        findViewById<TextView>(R.id.txtReceiver).text = receiverName


        messageManager = FirebaseMessageManager()


        recycler = findViewById(R.id.recyclerMessages)
        recycler.layoutManager = LinearLayoutManager(this)

        adapter = MessageAdapter(ArrayList())
        adapter.setCurrentUser(senderEmail)
        recycler.adapter = adapter

        val btnDelete = findViewById<ImageButton>(R.id.btnDeleteSelected)

        adapter.onSelectionModeChanged = { isSelected ->
            btnDelete.visibility = if (isSelected) View.VISIBLE else View.GONE
        }


        // Inside onCreate in MessageBoxActivity.kt
        btnDelete.setOnClickListener {
            val selected = adapter.getSelectedMessages()

            if (selected.isEmpty()) return@setOnClickListener


            selected.forEach { msg ->
                messageManager.deleteMessage(msg.chatId, msg.messageId)
            }


            Toast.makeText(this, "${selected.size} messages deleted", Toast.LENGTH_SHORT).show()


            adapter.clearSelection()

        }

        listenForMessages()

        val edtMessage = findViewById<EditText>(R.id.edtMessage)
        val btnSend = findViewById<ImageButton>(R.id.btnSend)


        btnSend.setOnClickListener {
            val text = edtMessage.text.toString().trim()

            if (text.isNotEmpty()) {
                messageManager.sendMessage(
                    senderEmail,
                    senderName,
                    receiverEmail,
                    receiverName,
                    text
                )

                Toast.makeText(this, "Message sent!", Toast.LENGTH_SHORT).show()
                edtMessage.text.clear()

                updateTypingStatus(false)
            }
        }


        edtMessage.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateTypingStatus(s?.isNotEmpty() == true)
            }
        })
    }


    private fun listenForMessages() {
        messageListener = messageManager.listenForMessages(senderEmail, receiverEmail) { list ->
            adapter.updateMessages(list)


            if (list.size > lastMessageCount) {
                recycler.scrollToPosition(list.size - 1)
                lastMessageCount = list.size


                messageManager.markAsSeen(senderEmail, receiverEmail)
            } else if (list.size == lastMessageCount) {

                messageManager.markAsSeen(senderEmail, receiverEmail)
            }
        }
    }

    private fun getSafePath(email: String): String {
        return email.replace(".", "_").replace("@", "_")
    }

    private fun updateTypingStatus(isTyping: Boolean) {
        if (senderEmail.isEmpty() || receiverEmail.isEmpty()) return

        val path = "typing/${getSafePath(senderEmail)}/${getSafePath(receiverEmail)}"

        FirebaseDatabase.getInstance()
            .getReference(path)
            .setValue(isTyping)
            .apply {
                FirebaseDatabase.getInstance().getReference(path).onDisconnect().setValue(false)
            }
    }



    private fun listenForTyping() {
        val path = "typing/${getSafePath(receiverEmail)}/${getSafePath(senderEmail)}"
        val typingRef = FirebaseDatabase.getInstance().getReference(path)

        typingRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val isTyping = snapshot.getValue(Boolean::class.java) ?: false
                val typingText = findViewById<TextView>(R.id.txtTyping)

                if (isTyping) {
                    typingText.visibility = TextView.VISIBLE
                    typingText.text = "Typing…"
                } else {
                    typingText.visibility = TextView.GONE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                android.util.Log.e("TYPING_ERROR", error.message)
            }
        })
    }


    override fun onStop() {
        super.onStop()
        messageListener?.let {
            messageManager.stopListening(senderEmail, receiverEmail, it)
        }
    }


}
