package com.example.eyeglassesapp

import android.content.Intent
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.eyeglassesapp.entities.UserEntity

class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bind(user: UserEntity) {
        itemView.findViewById<TextView>(R.id.usernameTextView).text = "Username: ${user.username}"
        itemView.findViewById<TextView>(R.id.emailTextView).text = "Email: ${user.email}"
        itemView.findViewById<TextView>(R.id.genderTextView).text = "Gender: ${user.gender}"
    }

}
