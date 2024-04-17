package com.example.eyeglassesapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.eyeglassesapp.entities.UserEntity

class UserAdapter(private var userList: List<UserEntity>,
                  private val onItemClick: (UserEntity) -> Unit) : RecyclerView.Adapter<UserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.user_item, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        holder.bind(user)
        holder.itemView.setOnClickListener { onItemClick(user) }
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    fun updateUsers(newUsers : List<UserEntity>){
        val diffCallback = object : DiffUtil.Callback(){
            override fun getOldListSize(): Int = userList.size
            override fun getNewListSize(): Int = newUsers.size
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return userList[oldItemPosition].userId == newUsers[newItemPosition].userId
            }
            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return userList[oldItemPosition] == newUsers[newItemPosition]
            }
        }
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        userList = newUsers
        diffResult.dispatchUpdatesTo(this)
    }

    fun deleteUser(userId : Int){
        val position = userList.indexOfFirst { it.userId == userId }
        if(position != -1){
            userList = userList.toMutableList().apply { removeAt(position) }
            notifyItemRemoved(position)
        }
    }
}
