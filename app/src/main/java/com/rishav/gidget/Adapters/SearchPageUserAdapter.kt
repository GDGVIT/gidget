package com.rishav.gidget.Adapters

import android.annotation.SuppressLint
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
import com.rishav.gidget.Models.SearchPage.ItemsRepo
import com.rishav.gidget.R
import com.rishav.gidget.Realm.AddToWidget
import com.squareup.picasso.Picasso
import io.realm.Realm
import io.realm.RealmObject
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

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holderUser: SearchPageUserViewHolder, position: Int) {
        val currentItem = searchPageDataList[position]
        //val profileURL = "https://github.com/${currentItem.login}"

        Picasso.get().load(currentItem.avatar_url).into(holderUser.profilePhoto)
        holderUser.username.text = "@${currentItem.login}"

        // getProfileDetails
        mService.getProfileInfo(currentItem.login, System.getenv("token") ?: "null")
                .enqueue(object : Callback<ProfilePageModel> {
                    override fun onResponse(
                            call: Call<ProfilePageModel>,
                            response: Response<ProfilePageModel>
                    ) {
                        holderUser.name.text = response.body()!!.name
                        holderUser.username.text = "@${currentItem.login}"
                        holderUser.location.text = response.body()?.location
                                ?: "Location not available"
                    }

                    override fun onFailure(call: Call<ProfilePageModel>, t: Throwable) {
                        println("ERROR - ${t.message}")
                    }
                })

        // Custom Animation
        var lastPosition: Int = -1
        val animation: Animation = AnimationUtils.loadAnimation(
                context,
                if (position > lastPosition) R.anim.up_from_bottom else R.anim.down_from_top
        )
        holderUser.itemView.startAnimation(animation)
        lastPosition = position

        //Add to widget
        holderUser.addToWidgetButton.setOnClickListener {
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

    private fun addDataToReam(realm: Realm, currentItem: Items) {
        val addToWidget = AddToWidget()
        addToWidget.username = currentItem.login
        addToWidget.type = "User"

        realm.beginTransaction()
        realm.copyToRealm(addToWidget)
        realm.commitTransaction()
    }

    class SearchPageUserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profilePhoto: ImageView = itemView.findViewById(R.id.searchPageRecyclerItemProfilePhoto)
        val name: TextView = itemView.findViewById(R.id.searchPageRecyclerItemNameText)
        val username: TextView = itemView.findViewById(R.id.searchPageRecyclerItemUsernameText)
        val location: TextView = itemView.findViewById(R.id.searchPageRecyclerItemLocationText)
        val addToWidgetButton: ImageButton = itemView.findViewById(R.id.searchPageAddToHomeButton)
    }
}
