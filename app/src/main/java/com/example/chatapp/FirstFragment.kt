package com.example.chatapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class FirstFragment : Fragment() {

    private var userListener: ValueEventListener? = null
    private var messageListener: ValueEventListener? = null

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var txtNoUser: TextView

    private lateinit var adapter: UserAdapter
    private lateinit var dbRef: DatabaseReference
    private lateinit var msgRef: DatabaseReference

    private val allUsers = arrayListOf<UserModel>()
    private val filteredUsers = arrayListOf<UserModel>()
    private lateinit var currentUserEmail: String

    private var lastMessagesSnapshot: DataSnapshot? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_first, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)
        searchView = view.findViewById(R.id.searchView)
        txtNoUser = view.findViewById(R.id.txtNoUser)

        currentUserEmail = arguments?.getString("email") ?: ""

        dbRef = FirebaseDatabase.getInstance().getReference("users")
        msgRef = FirebaseDatabase.getInstance().getReference("messages")

        adapter = UserAdapter(filteredUsers) { selectedUser ->

            val intent = Intent(requireContext(), MessageBoxActivity::class.java)
            intent.putExtra("senderEmail", currentUserEmail)
            intent.putExtra("senderName", arguments?.getString("fullname") ?: "")
            intent.putExtra("receiverEmail", selectedUser.email)
            intent.putExtra("receiverName", selectedUser.fullname)
            startActivity(intent)
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        setupSearch()

        return view
    }

    override fun onStart() {
        super.onStart()
        startUserListener()
        startMessageListener()
    }

    override fun onStop() {
        super.onStop()
        userListener?.let { dbRef.removeEventListener(it) }
        messageListener?.let { msgRef.removeEventListener(it) }
    }

    private fun startUserListener() {
        userListener = dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                allUsers.clear()

                for (userSnap in snapshot.children) {
                    val fullname = userSnap.child("fullname").value.toString()
                    val email = userSnap.child("email").value.toString()
                    val phone = userSnap.child("phone").value.toString()

                    if (!email.equals(currentUserEmail, ignoreCase = true)) {
                        allUsers.add(UserModel(fullname, email, phone))
                    }
                }

                lastMessagesSnapshot?.let { rebuildChatList(it) }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }


    private fun startMessageListener() {
        messageListener = msgRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                lastMessagesSnapshot = snapshot

                if (allUsers.isNotEmpty()) {
                    rebuildChatList(snapshot)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }


    private fun rebuildChatList(snapshot: DataSnapshot) {

        val interactedEmails = mutableSetOf<String>()
        val lastMessageMap = mutableMapOf<String, String>()
        val unreadMap = mutableMapOf<String, Int>()
        val latestTimestampMap = mutableMapOf<String, Long>()
        val latestMessageTimestampMap = mutableMapOf<String, Long>()

        val myEmail = currentUserEmail.lowercase()

        for (chatSnap in snapshot.children) {
            for (msgSnap in chatSnap.children) {

                val sender = msgSnap.child("senderEmail").value?.toString()?.lowercase()?.trim() ?: ""
                val receiver = msgSnap.child("receiverEmail").value?.toString()?.lowercase()?.trim() ?: ""
                val messageText = msgSnap.child("message").value?.toString() ?: ""
                val msgTime = (msgSnap.child("timestamp").value as? Number)?.toLong() ?: 0L
                val isSeen = msgSnap.child("seen").value as? Boolean ?: false

                if (sender.isEmpty() || receiver.isEmpty()) continue

                val otherEmail = when {
                    sender == myEmail -> receiver
                    receiver == myEmail -> sender
                    else -> null
                }

                if (otherEmail != null) {

                    interactedEmails.add(otherEmail)

                    val existingLatestTime = latestTimestampMap[otherEmail] ?: -1L
                    if (msgTime >= existingLatestTime) {
                        latestTimestampMap[otherEmail] = msgTime
                    }

                    if (receiver == myEmail) {
                        val existingLatestMsgTime = latestMessageTimestampMap[otherEmail] ?: -1L
                        if (msgTime >= existingLatestMsgTime) {
                            lastMessageMap[otherEmail] = messageText
                            latestMessageTimestampMap[otherEmail] = msgTime
                        }
                    }

                    if (receiver == myEmail && !isSeen) {
                        unreadMap[otherEmail] = (unreadMap[otherEmail] ?: 0) + 1
                    }
                }
            }
        }

        val interactedUsers = allUsers.filter { user ->
            interactedEmails.contains(user.email.lowercase())
        }

        filteredUsers.clear()

        for (user in interactedUsers) {
            val key = user.email.lowercase()
            user.lastMessage = lastMessageMap[key] ?: ""
            user.unreadCount = unreadMap[key] ?: 0
            user.lastTimestamp = latestTimestampMap[key] ?: 0L
            filteredUsers.add(user)
        }

        filteredUsers.sortByDescending { it.lastTimestamp }
        adapter.updateList(filteredUsers)
    }


    private fun setupSearch() {

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = true

            override fun onQueryTextChange(newText: String?): Boolean {
                val search = newText?.trim()?.lowercase() ?: ""

                if (search.isEmpty()) {
                    adapter.updateList(filteredUsers)
                    txtNoUser.visibility = if (filteredUsers.isEmpty()) View.VISIBLE else View.GONE
                    return true
                }

                val results = allUsers.filter {
                    it.fullname.lowercase().contains(search)
                }

                txtNoUser.visibility = if (results.isEmpty()) View.VISIBLE else View.GONE
                adapter.updateList(results)
                return true
            }
        })
    }
}
