package com.rishav.gidget.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.rishav.gidget.Interface.RetroFitService
import com.rishav.gidget.Models.ProfilePage.ProfilePageModel
import com.rishav.gidget.Models.SearchPage.Items
import com.rishav.gidget.R
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchPageAdapter(
    private val context: Context,
    private val searchPageDataList: MutableList<Items>,
    private val mService: RetroFitService,
) : RecyclerView.Adapter<SearchPageAdapter.SearchPageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchPageViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.search_page_recycler_item, parent, false)
        return SearchPageViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: SearchPageViewHolder, position: Int) {
        val currentItem = searchPageDataList[position]
        val profileURL: String = "https://github.com/${currentItem.login}"

        Picasso.get().load(currentItem.avatar_url).into(holder.profilePhoto)
        holder.name.text = currentItem.login
        holder.username.text = profileURL
        holder.location.text = currentItem.type

        //getProfileDetails
        mService.getProfileInfo(currentItem.login).enqueue(object : Callback<ProfilePageModel> {
            override fun onResponse(
                call: Call<ProfilePageModel>,
                response: Response<ProfilePageModel>
            ) {
                println("PROFILE DATA - ${response.body()}")
            }

            override fun onFailure(call: Call<ProfilePageModel>, t: Throwable) {
                println("ERROR - ${t.message}")
            }

        })


        //Custom Animation
        var lastPosition: Int = -1
        val animation: Animation = AnimationUtils.loadAnimation(
            context,
            if (position > lastPosition) R.anim.up_from_bottom else R.anim.down_from_top
        )
        holder.itemView.startAnimation(animation)
        lastPosition = position
    }

    override fun getItemCount(): Int = searchPageDataList.size

    class SearchPageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profilePhoto: ImageView = itemView.findViewById(R.id.searchPageRecyclerItemProfilePhoto)
        val name: TextView = itemView.findViewById(R.id.searchPageRecyclerItemNameText)
        val username: TextView = itemView.findViewById(R.id.searchPageRecyclerItemUsernameText)
        val location: TextView = itemView.findViewById(R.id.searchPageRecyclerItemLocationText)
    }
}