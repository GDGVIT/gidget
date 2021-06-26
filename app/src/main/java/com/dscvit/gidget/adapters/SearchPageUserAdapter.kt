package com.dscvit.gidget.adapters

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.dscvit.gidget.R
import com.dscvit.gidget.common.Common
import com.dscvit.gidget.common.RoundedTransformation
import com.dscvit.gidget.common.Security
import com.dscvit.gidget.common.Utils
import com.dscvit.gidget.interfaces.RetroFitService
import com.dscvit.gidget.models.profilePage.ProfilePageModel
import com.dscvit.gidget.models.searchPage.Items
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchPageUserAdapter(
    private val context: Context,
    private val searchPageDataList: MutableList<Items>,
    private val mService: RetroFitService,
) : RecyclerView.Adapter<SearchPageUserAdapter.SearchPageUserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchPageUserViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.search_page_recycler_item, parent, false)
        return SearchPageUserViewHolder(itemView)
    }

    override fun onBindViewHolder(holderUser: SearchPageUserViewHolder, position: Int) {
        val currentItem = searchPageDataList[position]
        val username = "@${currentItem.login}"

        // Profile Photo
        Picasso.get().load(currentItem.avatar_url).error(R.drawable.github_logo).transform(
            RoundedTransformation(300, 0)
        ).into(holderUser.profilePhoto)
        holderUser.username.text = username

        // getProfileDetails
        mService.getProfileInfo(
            currentItem.login,
            "token ${Security.getToken()}"
        )
            .enqueue(object : Callback<ProfilePageModel> {
                override fun onResponse(
                    call: Call<ProfilePageModel>,
                    response: Response<ProfilePageModel>
                ) {
                    holderUser.name.text = response.body()!!.name
                    holderUser.username.text = username
                    holderUser.location.text = response.body()?.location
                        ?: "Location not available"
                }

                override fun onFailure(call: Call<ProfilePageModel>, t: Throwable) {
                    println("ERROR - ${t.message}")
                }
            })

        // Custom Animation
        val lastPosition: Int = -1
        val animation: Animation = AnimationUtils.loadAnimation(
            context,
            if (position > lastPosition) R.anim.up_from_bottom else R.anim.down_from_top
        )
        holderUser.itemView.startAnimation(animation)

        // Add to widget
        holderUser.addToWidgetButton.setOnClickListener {
            addToWidget(currentItem)
        }

        // onClick
        holderUser.currentView.setOnClickListener { navigateToExternal(currentItem.login) }
    }

    override fun getItemCount(): Int = searchPageDataList.size

    private fun addToWidget(currentItem: Items) {
        val mService: RetroFitService = Common.retroFitService
        Utils().addToWidget(
            mService,
            isUser = true,
            username = currentItem.login,
            name = "",
            context = context
        )
    }

    private fun navigateToExternal(username: String) {
        Toast.makeText(context, username, Toast.LENGTH_LONG).show()
        val uri: Uri = Uri.parse("https://github.com/$username")
        val clickIntent = Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(clickIntent)
    }

    class SearchPageUserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profilePhoto: ImageView = itemView.findViewById(R.id.searchPageRecyclerItemProfilePhoto)
        val name: TextView = itemView.findViewById(R.id.searchPageRecyclerItemNameText)
        val username: TextView = itemView.findViewById(R.id.searchPageRecyclerItemUsernameText)
        val location: TextView = itemView.findViewById(R.id.searchPageRecyclerItemLocationText)
        val addToWidgetButton: Button = itemView.findViewById(R.id.searchPageAddToHomeButton)
        val currentView: CardView = itemView.findViewById(R.id.searchPageCardView)
    }
}
