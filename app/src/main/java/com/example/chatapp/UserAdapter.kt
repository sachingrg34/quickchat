package com.example.chatapp

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class UserAdapter(
    private var users: List<UserModel>,
    private val onUserClick: (UserModel) -> Unit

) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtName: TextView = itemView.findViewById(R.id.txtName)
        val txtLastMessage: TextView = itemView.findViewById(R.id.txtLastMessage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.user_row, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]

        holder.txtName.text = user.fullname
        holder.txtLastMessage.text = user.lastMessage

        if (user.unreadCount > 0) {

            holder.txtName.setTypeface(null, android.graphics.Typeface.BOLD)
            holder.txtLastMessage.setTypeface(null, android.graphics.Typeface.BOLD)
            holder.txtLastMessage.setTextColor(android.graphics.Color.BLACK)
        } else {

            holder.txtName.setTypeface(null, android.graphics.Typeface.NORMAL)
            holder.txtLastMessage.setTypeface(null, android.graphics.Typeface.NORMAL)
            holder.txtLastMessage.setTextColor(android.graphics.Color.GRAY)
        }

        holder.itemView.setOnClickListener { onUserClick(user) }

    }

    override fun getItemCount(): Int = users.size

    fun updateList(newList: List<UserModel>) {
        users = newList
        notifyDataSetChanged()
    }
}
