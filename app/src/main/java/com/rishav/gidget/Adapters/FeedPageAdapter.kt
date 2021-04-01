package com.rishav.gidget.Adapters

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.rishav.gidget.Models.FeedPage.FeedPageModel
import com.rishav.gidget.R
import com.squareup.picasso.Picasso
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class FeedPageAdapter(
    private val context: Context,
    private val feedPageDataList: MutableList<FeedPageModel>
) :
    RecyclerView.Adapter<FeedPageAdapter.FeedPageUserActivityViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FeedPageUserActivityViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.feed_page_recycler_item, parent, false)
        return FeedPageUserActivityViewHolder(itemView)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: FeedPageUserActivityViewHolder, position: Int) {
        val currentItem = feedPageDataList[position]

        // Profile Photo
        Picasso.get().load(currentItem.actor.avatar_url).into(holder.profilePhoto)

        // Event Photo
        setEventData(currentItem, holder)

        // Username Text
        holder.username.text = currentItem.actor.login

        // Repository Name
        holder.repositoryName.text = currentItem.repo.name

        // Date Text
        setDate(holder, currentItem)

        // Custom Animation
        var lastPosition: Int = -1
        val animation: Animation = AnimationUtils.loadAnimation(
            context,
            if (position > lastPosition) R.anim.up_from_bottom else R.anim.down_from_top
        )
        holder.itemView.startAnimation(animation)
        lastPosition = position

        // Open Repository
        holder.recyclerViewItemRelativeLayout.setOnClickListener {
            val uri: Uri = Uri.parse("https://github.com/${currentItem.repo.name}")
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    uri
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }

    override fun getItemCount(): Int = feedPageDataList.size

    override fun onViewDetachedFromWindow(holder: FeedPageUserActivityViewHolder) {
        super.onViewDetachedFromWindow(holder)
        holder.itemView.clearAnimation()
    }

    private fun setEventData(currentItem: FeedPageModel, holder: FeedPageUserActivityViewHolder) {
        when (currentItem.type) {
            "CommitCommentEvent" -> {
                holder.eventPhoto.setImageResource(R.drawable.ic_baseline_comment_24)
                holder.message.text = "User commented on a commit"
            }
            "CreateEvent" -> {
                holder.eventPhoto.setImageResource(R.drawable.ic_git_branch)
                holder.message.text = "User created a branch / tag"
            }
            "ForkEvent" -> {
                holder.eventPhoto.setImageResource(R.drawable.ic_github_fork)
                holder.message.text = "User forked this repository"
            }
            "DeleteEvent" -> {
                holder.eventPhoto.setImageResource(R.drawable.ic_baseline_delete_24)
                holder.message.text = "User deleted a branch / tag"
            }
            "GollumEvent" -> {
                holder.eventPhoto.setImageResource(R.drawable.github_gollum)
                holder.message.text = "User created / updated a wiki page"
            }
            "IssueCommentEvent" -> {
                holder.eventPhoto.setImageResource(R.drawable.ic_baseline_comment_24)
                holder.message.text = "User commented on an issue"
            }
            "IssuesEvent" -> {
                holder.eventPhoto.setImageResource(R.drawable.ic_github_issue)
                holder.message.text = "Activity related to an issue"
            }
            "MemberEvent" -> {
                holder.eventPhoto.setImageResource(R.drawable.ic_baseline_group_24)
                holder.message.text = "A collaborator was added or removed"
            }
            "PublicEvent" -> {
                holder.eventPhoto.setImageResource(R.drawable.ic_baseline_public_24)
                holder.message.text = "Repository was made public"
            }
            "PullRequestEvent" -> {
                holder.eventPhoto.setImageResource(R.drawable.ic_github_pull_request)
                holder.message.text = "User made a pull request"
            }
            "PullRequestReviewCommentEvent" -> {
                holder.eventPhoto.setImageResource(R.drawable.ic_baseline_comment_24)
                holder.message.text = "User commented on a pull request review"
            }
            "PushEvent" -> {
                holder.eventPhoto.setImageResource(R.drawable.ic_baseline_cloud_upload_24)
                holder.message.text = "User made a push request"
            }
            "ReleaseEvent" -> {
                holder.eventPhoto.setImageResource(R.drawable.ic_baseline_new_releases_24)
                holder.message.text = "User made a new release"
            }
            "SponsorshipEvent" -> {
                holder.eventPhoto.setImageResource(R.drawable.ic_baseline_monetization_on_24)
                holder.message.text = "User started sponsoring"
            }
            "WatchEvent" -> {
                holder.eventPhoto.setImageResource(R.drawable.ic_baseline_remove_red_eye_24)
                holder.message.text = "User was watching"
            }
            else -> {
                holder.eventPhoto.setImageResource(R.drawable.github_logo)
                holder.message.text = "Unidentified event"
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setDate(holder: FeedPageUserActivityViewHolder, currentItem: FeedPageModel) {
        val dateTimePattern = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
        val createDate = LocalDateTime.parse(currentItem.created_at, dateTimePattern)
        val currentDate = LocalDateTime.now()
        val differenceTime = Duration.between(currentDate, createDate).abs()
        val finalResult: String = when {
            differenceTime.toMinutes() < 60 -> "${differenceTime.toMinutes()} minutes ago"
            differenceTime.toHours() < 24 -> "${differenceTime.toHours()} hours ago"
            differenceTime.toDays() <= 1 -> "${differenceTime.toDays()} day ago"
            else -> "${differenceTime.toDays()} days ago"
        }
        holder.dateText.text = finalResult
    }

    class FeedPageUserActivityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profilePhoto: ImageView =
            itemView.findViewById(R.id.feedPageRecyclerViewItemProfilePhoto)
        val eventPhoto: ImageView = itemView.findViewById(R.id.feedPageEventTypeIcon)
        val username: TextView = itemView.findViewById(R.id.feedPageRecyclerViewItemUsername)
        val repositoryName: TextView = itemView.findViewById(R.id.feedPageRecyclerViewItemRepoName)
        val dateText: TextView = itemView.findViewById(R.id.feedPageRecyclerViewItemDate)
        val recyclerViewItemRelativeLayout: RelativeLayout =
            itemView.findViewById(R.id.feedPageRecyclerViewItemRelativeLayout)
        val message: TextView = itemView.findViewById(R.id.feedPageRecyclerViewItemMessage)
    }
}