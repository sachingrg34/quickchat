package com.example.chatapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.fragment.app.Fragment
import android.view.animation.AnimationUtils

class DashboardActivity : AppCompatActivity() {

    private lateinit var btnChat: Button
    private lateinit var btnCalls: Button
    private lateinit var btnFriendList: Button
    private lateinit var btnMenu: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dashboard_layout)

        val fullname = intent.getStringExtra("fullname") ?: "Unknown User"
        val email = intent.getStringExtra("email") ?: ""

        btnChat = findViewById(R.id.FirstFragment)
         btnFriendList= findViewById(R.id.SecondFragment)
        btnCalls  = findViewById(R.id.ThirdFragment)

        btnMenu = findViewById(R.id.FourthFragment)

        val bounce = AnimationUtils.loadAnimation(this, R.anim.jelly_bounce)


        loadFirstFragment(fullname, email)
        selectButton(null)

        btnChat.setOnClickListener {
            it.startAnimation(bounce)
            selectButton(btnChat)
            loadFirstFragment(fullname, email)
        }


        btnFriendList.setOnClickListener {
            selectButton(btnFriendList)
            loadFragment(SecondFragment())
        }


        btnCalls.setOnClickListener {
            selectButton(btnCalls)
            loadFragment(ThirdFragment())
        }




        btnMenu.setOnClickListener {
            it.startAnimation(bounce)
            selectButton(btnMenu)

            val fragment = FourthFragment()
            val bundle = Bundle()
            bundle.putString("fullname", fullname)
            fragment.arguments = bundle

            loadFragment(fragment)
        }
    }

    private fun selectButton(selected: Button?) {
        btnChat.isSelected = (selected == btnChat)
        btnCalls.isSelected = (selected == btnCalls)
        btnFriendList.isSelected = (selected == btnFriendList)
        btnMenu.isSelected = (selected == btnMenu)

        btnChat.text = if (btnChat.isSelected) "" else "Chat"
        btnCalls.text = if (btnCalls.isSelected) "" else "Calls"
        btnFriendList.text = if (btnFriendList.isSelected) "" else "Friends"
        btnMenu.text = if (btnMenu.isSelected) "" else "Menu"
    }


    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    private fun loadFirstFragment(fullname: String, email: String) {
        val fragment = FirstFragment()
        val bundle = Bundle()
        bundle.putString("fullname", fullname)
        bundle.putString("email", email)
        fragment.arguments = bundle
        loadFragment(fragment)
    }
}

