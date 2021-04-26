package com.rishav.gidget.Adapters

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.rishav.gidget.Common.Common
import com.rishav.gidget.Interface.RetroFitService
import com.rishav.gidget.Models.ProfilePage.ProfilePageModel
import com.rishav.gidget.Models.SearchPage.Items
import com.rishav.gidget.Models.Widget.WidgetRepoModel
import com.rishav.gidget.R
import com.rishav.gidget.Realm.AddToWidget
import com.squareup.picasso.Picasso
import io.realm.Realm
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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
        // val profileURL = "https://github.com/${currentItem.login}"

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

        // Add to widget
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
        val mService: RetroFitService = Common.retroFitService

        mService.widgetUserEvents(currentItem.login)
            .enqueue(object : Callback<MutableList<WidgetRepoModel>> {
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onResponse(
                    call: Call<MutableList<WidgetRepoModel>>,
                    response: Response<MutableList<WidgetRepoModel>>
                ) {
                    if (response.body() != null) {
                        for (res in response.body()!!) {
                            val addToWidget = AddToWidget()

                            addToWidget.username = res.actor.login
                            addToWidget.name = res.repo.name
                            addToWidget.avatarUrl = res.actor.avatar_url
                            addToWidget.message = getMessage(res)
                            addToWidget.date = getDate(res)

                            realm.beginTransaction()
                            realm.copyToRealm(addToWidget)
                            realm.commitTransaction()
                        }
                        Toast.makeText(context, "Added to widget", Toast.LENGTH_LONG).show()
                    } else
                        Toast.makeText(context, "Could not add", Toast.LENGTH_LONG).show()
                }

                override fun onFailure(call: Call<MutableList<WidgetRepoModel>>, t: Throwable) {
                    println("ERROR - ${t.message}")
                }
            })
    }

    private fun getMessage(currentItem: WidgetRepoModel): String {
        return when (currentItem.type) {
            "CommitCommentEvent" -> "User commented on a commit"
            "CreateEvent" -> "User created a branch / tag"
            "ForkEvent" -> "User forked this repository"
            "DeleteEvent" -> "User deleted a branch / tag"
            "GollumEvent" -> "User created / updated a wiki page"
            "IssueCommentEvent" -> "User commented on an issue"
            "IssuesEvent" -> "Activity related to an issue"
            "MemberEvent" -> "A collaborator was added or removed"
            "PublicEvent" -> "Repository was made public"
            "PullRequestEvent" -> "User made a pull request"
            "PullRequestReviewCommentEvent" -> "User commented on a pull request review"
            "PushEvent" -> "User made a push request"
            "ReleaseEvent" -> "User made a new release"
            "SponsorshipEvent" -> "User started sponsoring"
            "WatchEvent" -> "User was watching"
            else -> "Unidentified event"
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getDate(currentItem: WidgetRepoModel): String {
        val dateTimePattern = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
        val createDate = LocalDateTime.parse(currentItem.created_at, dateTimePattern)
        val currentDate = LocalDateTime.now()
        val differenceTime = Duration.between(currentDate, createDate).abs()
        return when {
            differenceTime.toMinutes() < 60 -> "${differenceTime.toMinutes()} minutes ago"
            differenceTime.toHours() < 24 -> "${differenceTime.toHours()} hours ago"
            differenceTime.toDays() <= 1 -> "${differenceTime.toDays()} day ago"
            else -> "${differenceTime.toDays()} days ago"
        }
    }

    class SearchPageUserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profilePhoto: ImageView = itemView.findViewById(R.id.searchPageRecyclerItemProfilePhoto)
        val name: TextView = itemView.findViewById(R.id.searchPageRecyclerItemNameText)
        val username: TextView = itemView.findViewById(R.id.searchPageRecyclerItemUsernameText)
        val location: TextView = itemView.findViewById(R.id.searchPageRecyclerItemLocationText)
        val addToWidgetButton: ImageButton = itemView.findViewById(R.id.searchPageAddToHomeButton)
    }
}
