package com.example.chatapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.view.ContextThemeWrapper
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class MessageAdapter(private var messageList: ArrayList<MessageModel>)
    : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {
    fun clearSelection() {
        isSelectionMode = false
        messageList.forEach { it.isSelected = false }
        onSelectionModeChanged?.invoke(false)
        notifyDataSetChanged()
    }
    private var currentUserEmail: String = ""
    var onSelectionModeChanged: ((Boolean) -> Unit)? = null
    private var isSelectionMode = false

    fun setCurrentUser(email: String) {
        currentUserEmail = email
    }

    fun addMessage(message: MessageModel) {
        messageList.add(message)
        notifyItemInserted(messageList.size - 1)
    }

    fun updateMessages(newList: ArrayList<MessageModel>) {
        messageList = newList
        notifyDataSetChanged()
    }

    fun toggleSelection(position: Int) {
        if (position < 0 || position >= messageList.size) return

        messageList[position].isSelected = !messageList[position].isSelected
        val wasInSelectionMode = isSelectionMode
        isSelectionMode = messageList.any { it.isSelected }

        if (wasInSelectionMode != isSelectionMode) {
            onSelectionModeChanged?.invoke(isSelectionMode)
            notifyDataSetChanged()
        } else {
            notifyItemChanged(position)
        }
    }


    fun getSelectedMessages(): List<MessageModel> {
        return messageList.filter { it.isSelected }
    }

    override fun getItemViewType(position: Int): Int {
        return if (messageList[position].senderEmail == currentUserEmail) {
            1
        } else {
            2
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val layout = if (viewType == 1) {
            R.layout.item_message_sender
        } else {
            R.layout.item_message_receiver
        }

        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val msg = messageList[position]


        holder.txtMessage.text = msg.message


        val previous = if (position > 0) messageList[position - 1] else null

        if (previous == null) {
            // First message always shows time
            holder.txtTime.visibility = View.VISIBLE
            holder.txtTime.text = formatTimestamp(msg.timestamp)
        } else {
            val gap = msg.timestamp - previous.timestamp
            val tenMinute = 10 * 60 * 1000L

            if (gap >= tenMinute) {
                holder.txtTime.visibility = View.VISIBLE
                holder.txtTime.text = formatTimestamp(msg.timestamp)
            } else {
                holder.txtTime.visibility = View.GONE
            }
        }


        if (position == messageList.size - 1 && msg.senderEmail == currentUserEmail) {
            holder.txtStatus?.visibility = View.VISIBLE
            holder.txtStatus?.text = if (msg.seen) "Seen" else "Sent"
        } else {
            holder.txtStatus?.visibility = View.GONE
        }


        val previousMsg = if (position > 0) messageList[position - 1] else null
        val nextMsg = if (position < messageList.size - 1) messageList[position + 1] else null

        val samePrev = previousMsg?.senderEmail == msg.senderEmail
        val sameNext = nextMsg?.senderEmail == msg.senderEmail

        val isFirstOfPair = sameNext && !samePrev
        val isSecondOfPair = samePrev && !sameNext


        val background = if (msg.senderEmail == currentUserEmail) {
            when {
                isFirstOfPair -> R.drawable.sender_top
                isSecondOfPair -> R.drawable.sender_bottom
                else -> R.drawable.sender_single
            }
        } else {
            when {
                isFirstOfPair -> R.drawable.receiver_top
                isSecondOfPair -> R.drawable.receiver_bottom
                else -> R.drawable.receiver_single
            }
        }

        holder.txtMessage.setBackgroundResource(background)

        holder.itemView.alpha = if (msg.isSelected) 0.5f else 1.0f

        // ⭐ ADD THIS BLOCK HERE
        if (isSelectionMode) {
            holder.txtMessage.setCompoundDrawablesWithIntrinsicBounds(
                if (msg.isSelected) android.R.drawable.checkbox_on_background
                else android.R.drawable.checkbox_off_background,
                0, 0, 0
            )
        } else {
            holder.txtMessage.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        }


        holder.itemView.setOnClickListener {
            if (isSelectionMode) toggleSelection(holder.bindingAdapterPosition)
        }

        holder.itemView.setOnLongClickListener {
            toggleSelection(holder.bindingAdapterPosition)
            true
        }
    }


    override fun getItemCount(): Int = messageList.size


    private fun showDeletePopup(view: View, position: Int) {
        val wrapper = ContextThemeWrapper(view.context, R.style.MyPopupMenuTheme)
        val popup = androidx.appcompat.widget.PopupMenu(wrapper, view)


        val deleteItem = popup.menu.add(0, 1, 0, "Delete")
        deleteItem.setIcon(R.drawable.red_delete_icon) // Your new icon


        try {
            val fields = popup.javaClass.declaredFields
            for (field in fields) {
                if ("mPopup" == field.name) {
                    field.isAccessible = true
                    val menuPopupHelper = field.get(popup)
                    val classPopupHelper = Class.forName(menuPopupHelper.javaClass.name)
                    val setForceIcons = classPopupHelper.getMethod("setForceShowIcon", Boolean::class.javaPrimitiveType)
                    setForceIcons.invoke(menuPopupHelper, true)
                    break
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        popup.setOnMenuItemClickListener {
            if (it.itemId == 1) {
                deleteMessage(position)
            }
            true
        }
        popup.show()
    }

    private fun deleteMessage(position: Int) {
        if (position < 0 || position >= messageList.size) return

        val msg = messageList[position]

        FirebaseMessageManager().deleteMessage(msg.chatId, msg.messageId)


        messageList.removeAt(position)


        notifyItemRemoved(position)


        notifyItemRangeChanged(position, messageList.size)
    }


    private fun formatTimestamp(timestamp: Long): String {
        if (timestamp == 0L) return ""

        val now = System.currentTimeMillis()
        val diff = now - timestamp
        val oneDay = 24 * 60 * 60 * 1000L

        val date = Date(timestamp)
        val sdfTime = SimpleDateFormat("h:mm a", Locale.getDefault())
        val sdfDay = SimpleDateFormat("EEE", Locale.getDefault())
        val sdfDate = SimpleDateFormat("MMM d", Locale.getDefault())

        return when {
            diff < oneDay -> sdfTime.format(date)
            diff < 2 * oneDay -> "Yesterday"
            diff < 7 * oneDay -> sdfDay.format(date)
            else -> sdfDate.format(date)
        }
    }

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtMessage: TextView = itemView.findViewById(R.id.txtMessage)
        val txtStatus: TextView? = itemView.findViewById(R.id.txtStatus)
        val txtTime: TextView = itemView.findViewById(R.id.txtTime)
    }
}
