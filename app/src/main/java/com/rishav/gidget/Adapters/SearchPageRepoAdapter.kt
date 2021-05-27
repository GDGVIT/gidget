package com.rishav.gidget.Adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.rishav.gidget.Common.Common
import com.rishav.gidget.Common.Utils
import com.rishav.gidget.Interface.RetroFitService
import com.rishav.gidget.Models.SearchPage.ItemsRepo
import com.rishav.gidget.R
import com.squareup.picasso.Picasso

class SearchPageRepoAdapter(
    private val context: Context,
    private val searchPageDataList: MutableList<ItemsRepo>,
) : RecyclerView.Adapter<SearchPageRepoAdapter.SearchPageRepoViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchPageRepoViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.search_page_recycler_item, parent, false)
        return SearchPageRepoViewHolder(itemView)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holderRepo: SearchPageRepoViewHolder, position: Int) {
        val currentItem = searchPageDataList[position]

        Picasso.get().load(currentItem.owner.avatar_url).into(holderRepo.profilePhoto)

        val type = if (currentItem.private)
            "Private"
        else
            "Public"

        holderRepo.name.text = currentItem.name
        holderRepo.username.text = "@${currentItem.owner.login}"
        holderRepo.location.text = type

        // Custom Animation
        var lastPosition: Int = -1
        val animation: Animation = AnimationUtils.loadAnimation(
            context,
            if (position > lastPosition) R.anim.up_from_bottom else R.anim.down_from_top
        )
        holderRepo.itemView.startAnimation(animation)
        lastPosition = position

        // Add to widget
        holderRepo.addToWidgetButton.setOnClickListener {
            addToWidget(currentItem)
        }

        // onClick
        holderRepo.currentView.setOnClickListener { navigateToExternal(currentItem.owner.login) }
    }

    override fun getItemCount(): Int = searchPageDataList.size

    private fun addToWidget(currentItem: ItemsRepo) {
        val mService: RetroFitService = Common.retroFitService
        Utils().addToWidget(
            mService,
            false,
            currentItem.owner.login,
            currentItem.name,
            context
        )
    }

    private fun navigateToExternal(username: String) {
        Toast.makeText(context, "Opening in external site", Toast.LENGTH_LONG).show()
        val uri: Uri = Uri.parse("https://github.com/$username")
        val clickIntent = Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(clickIntent)
    }

    class SearchPageRepoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profilePhoto: ImageView = itemView.findViewById(R.id.searchPageRecyclerItemProfilePhoto)
        val name: TextView = itemView.findViewById(R.id.searchPageRecyclerItemNameText)
        val username: TextView = itemView.findViewById(R.id.searchPageRecyclerItemUsernameText)
        val location: TextView = itemView.findViewById(R.id.searchPageRecyclerItemLocationText)
        val addToWidgetButton: ImageButton = itemView.findViewById(R.id.searchPageAddToHomeButton)
        val currentView: RelativeLayout = itemView.findViewById(R.id.searchPageRecyclerViewRL)
    }
}
