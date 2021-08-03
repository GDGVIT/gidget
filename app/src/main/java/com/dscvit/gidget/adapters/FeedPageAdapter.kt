package com.dscvit.gidget.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.dscvit.gidget.R
import com.dscvit.gidget.activities.ProfileActivity
import com.dscvit.gidget.models.activity.feedPage.FeedPageModel
import com.squareup.picasso.Picasso
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
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

    override fun onBindViewHolder(holder: FeedPageUserActivityViewHolder, position: Int) {
        val currentItem = feedPageDataList[position]

        // Profile Photo
        Picasso.get().load(currentItem.actor.avatar_url).into(holder.profilePhoto)
        holder.profilePhotoCard.setOnClickListener {
            val intent = Intent(context, ProfileActivity::class.java)
            intent.putExtra("username", currentItem.actor.login)
            intent.putExtra("owner", false)
            context.startActivity(intent)
        }

        // Event Message & Photo
        setEventData(currentItem, holder)

        // Event Details
        setEventDetails(currentItem, holder)

        // Username Text
        holder.username.text = currentItem.actor.login
        holder.username.setOnClickListener {
            val intent = Intent(context, ProfileActivity::class.java)
            intent.putExtra("username", currentItem.actor.login)
            intent.putExtra("owner", false)
            context.startActivity(intent)
        }

        // Repository Name
        holder.repositoryName.text = currentItem.repo.name

        // Date Text
        setDate(holder, currentItem)

        // Open Repository
        holder.feedPageRecyclerViewItem.setOnClickListener {
            val uri: Uri = Uri.parse(getHtmlUrl(currentItem))
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

    class FeedPageUserActivityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profilePhoto: ImageView =
            itemView.findViewById(R.id.feedPageRecyclerViewItemProfilePhoto)
        val profilePhotoCard: CardView =
            itemView.findViewById(R.id.feedPageRecyclerViewItemProfilePhotoCard)
        val eventPhoto: ImageView = itemView.findViewById(R.id.feedPageEventTypeIcon)
        val username: TextView = itemView.findViewById(R.id.feedPageRecyclerViewItemUsername)
        val repositoryName: TextView = itemView.findViewById(R.id.feedPageRecyclerViewItemRepoName)
        val dateText: TextView = itemView.findViewById(R.id.feedPageRecyclerViewItemDate)
        val feedPageRecyclerViewItem: CardView =
            itemView.findViewById(R.id.feedPageRecyclerViewItem)
        val message: TextView = itemView.findViewById(R.id.feedPageRecyclerViewItemMessage)
        val details: TextView = itemView.findViewById(R.id.feedPageRecyclerViewItemDetails)
    }
}

@SuppressLint("SetTextI18n")
internal fun setEventData(
    currentItem: FeedPageModel,
    holder: FeedPageAdapter.FeedPageUserActivityViewHolder
) {
    try {
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
                holder.message.text =
                    if (currentItem.payload?.action.isNullOrEmpty()) "User commented on an issue"
                    else when (currentItem.payload?.action) {
                        "edited" -> "User edited a comment"
                        "deleted" -> "User deleted a comment"
                        else -> "User commented on an issue"
                    }
            }
            "IssuesEvent" -> {
                holder.eventPhoto.setImageResource(R.drawable.ic_github_issue)
                holder.message.text =
                    if (currentItem.payload?.action.isNullOrEmpty()) "Activity related to an issue"
                    else "User ${currentItem.payload?.action} a issue"
            }
            "MemberEvent" -> {
                holder.eventPhoto.setImageResource(R.drawable.ic_baseline_group_24)
                holder.message.text =
                    if (currentItem.payload?.action.isNullOrEmpty()) "A collaborator was added or removed"
                    else "A collaborator was ${currentItem.payload?.action}"
            }
            "PublicEvent" -> {
                holder.eventPhoto.setImageResource(R.drawable.ic_baseline_public_24)
                holder.message.text = "Repository was made public"
            }
            "PullRequestEvent" -> {
                holder.eventPhoto.setImageResource(R.drawable.ic_github_pull_request)
                holder.message.text =
                    if (currentItem.payload?.action.isNullOrEmpty() || currentItem.payload?.pull_request?.title.isNullOrEmpty())
                        "User made a pull request"
                    else "User ${currentItem.payload?.action} a pull request"
            }
            "PullRequestReviewEvent" -> {
                holder.eventPhoto.setImageResource(R.drawable.pull_request_review_event)
                holder.message.text = "User reviewed a pull request"
            }
            "PullRequestReviewCommentEvent" -> {
                holder.eventPhoto.setImageResource(R.drawable.ic_baseline_comment_24)
                holder.message.text = "User commented on a pull request review"
            }
            "PushEvent" -> {
                holder.eventPhoto.setImageResource(R.drawable.ic_baseline_cloud_upload_24)
                holder.message.text = "User made a push event"
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
                holder.eventPhoto.setImageResource(R.drawable.github_star)
                holder.message.text = "User starred this repository"
            }
            else -> {
                holder.eventPhoto.setImageResource(R.drawable.github_logo)
                holder.message.text = "Unidentified event"
            }
        }
    } catch (e: Throwable) {
        holder.eventPhoto.setImageResource(R.drawable.github_logo)
        holder.message.text = "Unidentified event"
    }
}

@SuppressLint("SetTextI18n")
internal fun setEventDetails(
    currentItem: FeedPageModel,
    holder: FeedPageAdapter.FeedPageUserActivityViewHolder
) {
    try {
        when (currentItem.type) {
            "CommitCommentEvent" -> {
                holder.details.text =
                    if (currentItem.payload?.comment?.body.isNullOrEmpty()) null
                    else "${currentItem.payload?.comment?.body}"
            }
            "CreateEvent" -> holder.details.visibility = View.GONE
            "ForkEvent" -> holder.details.visibility = View.GONE
            "DeleteEvent" -> holder.details.visibility = View.GONE
            "GollumEvent" -> holder.details.visibility = View.GONE
            "IssueCommentEvent" -> {
                if (currentItem.payload?.action.isNullOrEmpty()) holder.details.visibility =
                    View.GONE
                else when (currentItem.payload?.action) {
                    "created" -> {
                        holder.details.visibility = View.VISIBLE
                        holder.details.text = "${currentItem.payload.comment?.body}"
                    }
                    else -> holder.details.visibility = View.GONE
                }
            }
            "IssuesEvent" -> {
                if (currentItem.payload?.action.isNullOrEmpty()) holder.details.visibility =
                    View.GONE
                else {
                    holder.details.visibility = View.VISIBLE
                    holder.details.text = "${currentItem.payload?.issue?.title}"
                }
            }
            "MemberEvent" -> {
                if (currentItem.payload?.action.isNullOrEmpty()) holder.details.visibility =
                    View.GONE
                else {
                    holder.details.visibility = View.VISIBLE
                    holder.details.text = "${currentItem.payload?.member?.login}"
                }
            }
            "PublicEvent" -> holder.details.visibility = View.GONE
            "PullRequestEvent" -> {
                if (currentItem.payload?.action.isNullOrEmpty() || currentItem.payload?.pull_request?.title.isNullOrEmpty())
                    holder.details.visibility = View.GONE
                else {
                    holder.details.visibility = View.VISIBLE
                    holder.details.text = "${currentItem.payload?.pull_request?.title}"
                }
            }
            "PullRequestReviewEvent" -> {
                if (currentItem.payload?.pull_request?.title.isNullOrEmpty()) holder.details.visibility =
                    View.GONE
                else {
                    holder.details.visibility = View.VISIBLE
                    holder.details.text = "${currentItem.payload?.pull_request?.title}"
                }
            }
            "PullRequestReviewCommentEvent" -> {
                if (currentItem.payload?.comment?.body.isNullOrEmpty()) holder.details.visibility =
                    View.GONE
                else {
                    holder.details.visibility = View.VISIBLE
                    holder.details.text = "${currentItem.payload?.comment?.body}"
                }
            }
            "PushEvent" -> {
                var message = ""
                currentItem.payload?.commits?.forEach {
                    if (!it.message.isNullOrEmpty()) {
                        message += if (currentItem.payload.commits.last() != it) "${it.message}, "
                        else it.message
                    }
                }
                if (currentItem.payload?.commits.isNullOrEmpty()) holder.details.visibility =
                    View.GONE
                else {
                    holder.details.visibility = View.VISIBLE
                    holder.details.text = message
                }
            }
            "ReleaseEvent" -> {
                if (currentItem.payload?.release?.name.isNullOrEmpty()) holder.details.visibility =
                    View.GONE
                else {
                    holder.details.visibility = View.VISIBLE
                    holder.details.text = "Release - ${currentItem.payload?.release?.name}"
                }
            }
            "SponsorshipEvent" -> holder.details.visibility = View.GONE
            "WatchEvent" -> holder.details.visibility = View.GONE
            else -> holder.details.visibility = View.GONE
        }
    } catch (e: Throwable) {
        holder.details.visibility = View.GONE
    }
}

internal fun setDate(
    holder: FeedPageAdapter.FeedPageUserActivityViewHolder,
    currentItem: FeedPageModel
) {
    val dateTimePattern = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
    val createDate = LocalDateTime.parse(currentItem.created_at, dateTimePattern)
    val currentDate = LocalDateTime.now(ZoneId.of("Etc/UTC"))
    val differenceTime = Duration.between(currentDate, createDate).abs()
    val finalResult: String = when {
        differenceTime.seconds < 60 -> "${differenceTime.seconds} secs ago"
        differenceTime.toMinutes().toInt() == 1 -> "${differenceTime.toMinutes()} min ago"
        differenceTime.toMinutes() < 60 -> "${differenceTime.toMinutes()} mins ago"
        differenceTime.toHours().toInt() == 1 -> "${differenceTime.toHours()} hr ago"
        differenceTime.toHours() < 24 -> "${differenceTime.toHours()} hrs ago"
        differenceTime.toDays().toInt() == 1 -> "${differenceTime.toDays()} day ago"
        else -> "${differenceTime.toDays()} days ago"
    }
    holder.dateText.text = finalResult
}

internal fun getHtmlUrl(currentItem: FeedPageModel): String {
    return when (currentItem.type) {
        "CommitCommentEvent" -> "https://github.com/${currentItem.repo.name}"
        "CreateEvent" -> "https://github.com/${currentItem.repo.name}"
        "ForkEvent" -> currentItem.payload!!.forkee!!.html_url!!
        "DeleteEvent" -> "https://github.com/${currentItem.repo.name}"
        "GollumEvent" -> "https://github.com/${currentItem.repo.name}"
        "IssueCommentEvent" -> currentItem.payload!!.issue!!.html_url!!
        "IssuesEvent" -> currentItem.payload!!.issue!!.html_url!!
        "MemberEvent" -> "https://github.com/${currentItem.repo.name}"
        "PublicEvent" -> "https://github.com/${currentItem.repo.name}"
        "PullRequestEvent" -> currentItem.payload!!.pull_request!!.html_url!!
        "PullRequestReviewEvent" -> currentItem.payload!!.review!!.html_url!!
        "PullRequestReviewCommentEvent" -> currentItem.payload!!.comment!!.html_url!!
        "PushEvent" -> "https://github.com/${currentItem.repo.name}/commit/${currentItem.payload!!.commits!![0].sha!!}"
        "ReleaseEvent" -> currentItem.payload!!.release!!.html_url!!
        "SponsorshipEvent" -> "https://github.com/${currentItem.repo.name}"
        "WatchEvent" -> "https://github.com/${currentItem.repo.name}"
        else -> "https://github.com/${currentItem.repo.name}"
    }
}
