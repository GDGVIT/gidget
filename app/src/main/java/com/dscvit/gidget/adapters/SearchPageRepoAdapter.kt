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
import com.dscvit.gidget.common.Utils
import com.dscvit.gidget.interfaces.RetroFitService
import com.dscvit.gidget.models.searchPage.ItemsRepo
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

    override fun onBindViewHolder(holderRepo: SearchPageRepoViewHolder, position: Int) {
        val currentItem = searchPageDataList[position]
        val username = "@${currentItem.owner.login}"

        Picasso.get().load(currentItem.owner.avatar_url).error(R.drawable.github_logo).transform(
            RoundedTransformation(300, 0)
        ).into(holderRepo.profilePhoto)

        val type = if (currentItem.private)
            "Private"
        else
            "Public"

        holderRepo.name.text = currentItem.name
        holderRepo.username.text = username
        holderRepo.location.text = type

        // Custom Animation
        val lastPosition: Int = -1
        val animation: Animation = AnimationUtils.loadAnimation(
            context,
            if (position > lastPosition) R.anim.up_from_bottom else R.anim.down_from_top
        )
        holderRepo.itemView.startAnimation(animation)

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
            mService = mService,
            isUser = false,
            username = currentItem.owner.login,
            name = currentItem.name,
            context = context
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
        val addToWidgetButton: Button = itemView.findViewById(R.id.searchPageAddToHomeButton)
        val currentView: CardView = itemView.findViewById(R.id.searchPageCardView)
    }
}
