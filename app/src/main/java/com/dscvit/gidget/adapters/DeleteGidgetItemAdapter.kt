package com.dscvit.gidget.adapters

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.dscvit.gidget.R
import com.dscvit.gidget.activities.DeleteUserFromGidgetActivity
import com.dscvit.gidget.common.RoundedTransformation
import com.dscvit.gidget.common.Utils
import com.dscvit.gidget.models.widget.AddToWidget
import com.dscvit.gidget.widget.GidgetWidget
import com.google.gson.Gson
import com.squareup.picasso.Picasso

class DeleteGidgetItemAdapter(
    private val context: Context,
    private val userMap: MutableMap<String, MutableMap<String, String>>,
) : RecyclerView.Adapter<DeleteGidgetItemAdapter.DeleteGidgetItemViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DeleteGidgetItemViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.delete_user_widget_recycleritem, parent, false)
        return DeleteGidgetItemViewHolder(itemView)
    }

    override fun onBindViewHolder(
        holder: DeleteGidgetItemViewHolder,
        position: Int
    ) {
        val username: String = userMap.keys.elementAt(position)
        val currentMap: MutableMap<String, String>? = userMap[username]
        if (!currentMap.isNullOrEmpty()) {
            val name: String = currentMap["name"]!!
            val photoUrl: String = currentMap["photoUrl"]!!
            val isUser: Boolean = currentMap["isUser"]!!.toBoolean()

            holder.name.text = if (isUser) username else name
            holder.username.text = if (isUser) "" else name
            holder.isUser.text = if (isUser) "User/Org" else "Repo"

            Picasso.get().load(photoUrl).error(R.drawable.github_logo).transform(
                RoundedTransformation(300, 0)
            ).into(holder.profilePhoto)

            // Custom Animation
            val lastPosition: Int = -1
            val animation: Animation = AnimationUtils.loadAnimation(
                context,
                if (position > lastPosition) R.anim.up_from_bottom else R.anim.down_from_top
            )
            holder.itemView.startAnimation(animation)

            holder.addToWidgetButton.setOnClickListener {
                try {
                    userMap.remove(username)
                    updateGidget(username, name, context, userMap)
                    notifyItemRemoved(position)
                } catch (e: Exception) {
                    println(e.message)
                    Toast.makeText(context, "Failed to remove items", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun getItemCount(): Int = userMap.size

    class DeleteGidgetItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profilePhoto: ImageView =
            itemView.findViewById(R.id.deleteUserFromGidgetRecyclerItemProfilePhoto)
        val name: TextView = itemView.findViewById(R.id.deleteUserFromGidgetRecyclerItemNameText)
        val username: TextView =
            itemView.findViewById(R.id.deleteUserFromGidgetRecyclerItemUsernameText)
        val isUser: TextView =
            itemView.findViewById(R.id.deleteUserFromGidgetRecyclerItemIsUserText)
        val addToWidgetButton: Button =
            itemView.findViewById(R.id.deleteUserFromGidgetAddToHomeButton)
    }
}

internal fun updateGidget(
    username: String,
    name: String,
    context: Context,
    userMap: MutableMap<String, MutableMap<String, String>>
) {
    try {
        val utils = Utils()
        var dataSource: ArrayList<AddToWidget> = utils.getArrayList(context)
        dataSource = dataSource.filter { it.username != username && it.name != name } as ArrayList<AddToWidget>

        if (userMap.isNullOrEmpty()) {
            utils.deleteArrayList(context)
            val widgetIntent = Intent(context, GidgetWidget::class.java)
            widgetIntent.action = Utils.getClearWidgetItems()
            context.sendBroadcast(widgetIntent)
            (context as DeleteUserFromGidgetActivity).finish()
        } else {
            val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            val editor: SharedPreferences.Editor = prefs.edit()
            val gson = Gson()
            if (prefs.contains("dataSource") && prefs.contains("userDetails")) {
                editor.putString("dataSource", gson.toJson(dataSource))
                editor.putString("userDetails", gson.toJson(userMap))
                editor.apply()
            }

            val widgetIntent = Intent(context, GidgetWidget::class.java)
            widgetIntent.action = Utils.getOnRefreshButtonClicked()
            context.sendBroadcast(widgetIntent)
        }
        Toast.makeText(context, "Items removed from Gidget", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        println(e.message)
    }
}
