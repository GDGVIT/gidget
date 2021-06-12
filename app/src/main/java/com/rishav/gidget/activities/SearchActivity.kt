package com.rishav.gidget.activities

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rishav.gidget.R
import com.rishav.gidget.adapters.SearchPageRepoAdapter
import com.rishav.gidget.adapters.SearchPageUserAdapter
import com.rishav.gidget.common.Common
import com.rishav.gidget.common.Security
import com.rishav.gidget.interfaces.RetroFitService
import com.rishav.gidget.models.searchPage.Items
import com.rishav.gidget.models.searchPage.ItemsRepo
import com.rishav.gidget.models.searchPage.SearchPageRepoModel
import com.rishav.gidget.models.searchPage.SearchPageUserModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchActivity : AppCompatActivity() {
    lateinit var mService: RetroFitService
    lateinit var layoutManager: LinearLayoutManager
    lateinit var userAdapter: SearchPageUserAdapter
    lateinit var repoAdapter: SearchPageRepoAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        mService = Common.retroFitService

        val backButton: ImageButton = findViewById(R.id.searchPageBackButton)
        val emptySearchTextView: TextView = findViewById(R.id.searchPageNoItemsSearchedText)
        val searchText: EditText = findViewById(R.id.searchPageSearchText)
        val searchButton: ImageButton = findViewById(R.id.searchPageSearchButton)
        val orgButton: CardView = findViewById(R.id.searchPageOrganizationButton)
        val orgButtonText: TextView = findViewById(R.id.searchPageOrganizationButtonText)
        val repoButton: CardView = findViewById(R.id.searchPageRepoButton)
        val repoButtonText: TextView = findViewById(R.id.searchPageRepoButtonText)
        val recyclerView: RecyclerView = findViewById(R.id.searchPageRecyclerView)
        val progressBar: ProgressBar = findViewById(R.id.searchPageProgressBar)

        recyclerView.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager

        var searchType = "users"
        orgButton.setOnClickListener {
            searchType = "users"
            searchText.hint = "Search User / Organization"
            repoButtonText.setTextColor(Color.WHITE)
            orgButtonText.setTextColor(Color.parseColor("#61B1FF"))
            recyclerView.removeAllViewsInLayout()
        }
        repoButton.setOnClickListener {
            searchType = "repositories"
            searchText.hint = "Search Repositories"
            orgButtonText.setTextColor(Color.WHITE)
            repoButtonText.setTextColor(Color.parseColor("#61B1FF"))
            recyclerView.removeAllViewsInLayout()
        }
        searchButton.setOnClickListener {
            if (searchText.text.isNullOrEmpty() || searchText.text.isBlank())
                Toast.makeText(this, "Empty search field", Toast.LENGTH_LONG).show()
            else
                getSearchData(
                    this,
                    searchText.text.toString(),
                    searchType,
                    recyclerView,
                    emptySearchTextView,
                    progressBar,
                )
        }
        searchText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                getSearchData(
                    this,
                    searchText.text.toString(),
                    searchType,
                    recyclerView,
                    emptySearchTextView,
                    progressBar,
                )
            }
            false
        }
        backButton.setOnClickListener { finish() }
    }

    private fun getSearchData(
        context: Context,
        searchText: String,
        searchType: String,
        recyclerView: RecyclerView,
        emptySearchTextView: TextView,
        progressBar: ProgressBar,
    ) {
        emptySearchTextView.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
        if (searchType == "users") {
            mService.searchUser(
                searchText,
                "token ${Security.getToken()}"
            )
                .enqueue(object : Callback<SearchPageUserModel> {
                    override fun onResponse(
                        call: Call<SearchPageUserModel>,
                        response: Response<SearchPageUserModel>
                    ) {
                        if (response.body() != null) {
                            progressBar.visibility = View.GONE
                            userAdapter = SearchPageUserAdapter(
                                this@SearchActivity,
                                response.body()!!.items as MutableList<Items>,
                                mService,
                            )
                            userAdapter.notifyDataSetChanged()
                            recyclerView.adapter = userAdapter
                        }
                    }

                    override fun onFailure(call: Call<SearchPageUserModel>, t: Throwable) {
                        progressBar.visibility = View.GONE
                        emptySearchTextView.visibility = View.VISIBLE
                        Toast.makeText(
                            context,
                            "Something went wrong! Please try again later",
                            Toast.LENGTH_LONG
                        ).show()
                        println("Error - ${t.message}")
                    }
                })
        } else if (searchType == "repositories") {
            mService.searchRepo(
                searchText,
                "token ${Security.getToken()}"
            ).enqueue(object : Callback<SearchPageRepoModel> {
                override fun onResponse(
                    call: Call<SearchPageRepoModel>,
                    response: Response<SearchPageRepoModel>
                ) {
                    if (response.body() != null) {
                        progressBar.visibility = View.GONE
                        repoAdapter = SearchPageRepoAdapter(
                            this@SearchActivity,
                            response.body()!!.items as MutableList<ItemsRepo>
                        )
                        repoAdapter.notifyDataSetChanged()
                        recyclerView.adapter = repoAdapter
                    }
                }

                override fun onFailure(call: Call<SearchPageRepoModel>, t: Throwable) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(
                        context,
                        "Something went wrong! Please try again later",
                        Toast.LENGTH_LONG
                    ).show()
                    println("Error - ${t.message}")
                }
            })
        }
    }
}
