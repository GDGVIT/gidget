package com.rishav.gidget.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.rishav.gidget.Interface.RetroFitService
import com.rishav.gidget.Models.ProfilePage.ProfilePageModel
import com.rishav.gidget.Models.SearchPage.ItemsRepo
import com.rishav.gidget.R
import com.rishav.gidget.Realm.AddToWidget
import com.squareup.picasso.Picasso
import io.realm.Realm
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchPageRepoAdapter(
        private val context: Context,
        private val searchPageDataList: MutableList<ItemsRepo>,
        private val mService: RetroFitService,
) : RecyclerView.Adapter<SearchPageRepoAdapter.SearchPageRepoViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchPageRepoViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.search_page_recycler_item, parent, false)
        return SearchPageRepoViewHolder(itemView)
    }

    override fun onBindViewHolder(holderRepo: SearchPageRepoViewHolder, position: Int) {
        val currentItem = searchPageDataList[position]
        //val profileURL = "https://github.com/${currentItem.owner.login}"

        Picasso.get().load(currentItem.owner.avatar_url).into(holderRepo.profilePhoto)

        val type = if (!currentItem.private)
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

        //Add to widget
        holderRepo.addToWidgetButton.setOnClickListener {
            val realm: Realm = Realm.getDefaultInstance()
            if (realm.isEmpty) {
                addDataToReam(realm, currentItem)
            } else {
                val results = realm.where(AddToWidget::class.java).findAll()
                if (results.isEmpty()) {
                    addDataToReam(realm, currentItem)
                } else {
                    realm.beginTransaction()
                    results.deleteAllFromRealm()
                    realm.commitTransaction()

                    addDataToReam(realm, currentItem)
                }
            }
        }
    }

    override fun getItemCount(): Int = searchPageDataList.size

    private fun addDataToReam(realm: Realm, currentItem: ItemsRepo) {
        val addToWidget = AddToWidget()
        addToWidget.username = currentItem.full_name
        addToWidget.type = "Repo"

        realm.beginTransaction()
        realm.copyToRealm(addToWidget)
        realm.commitTransaction()
    }

    class SearchPageRepoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profilePhoto: ImageView = itemView.findViewById(R.id.searchPageRecyclerItemProfilePhoto)
        val name: TextView = itemView.findViewById(R.id.searchPageRecyclerItemNameText)
        val username: TextView = itemView.findViewById(R.id.searchPageRecyclerItemUsernameText)
        val location: TextView = itemView.findViewById(R.id.searchPageRecyclerItemLocationText)
        val addToWidgetButton: ImageButton = itemView.findViewById(R.id.searchPageAddToHomeButton)
    }
}