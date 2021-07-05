package com.dscvit.gidget.activities

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dscvit.gidget.R
import com.dscvit.gidget.adapters.SearchPageRepoAdapter
import com.dscvit.gidget.adapters.SearchPageUserAdapter
import com.dscvit.gidget.animations.BounceEdgeEffectFactory
import com.dscvit.gidget.common.Common
import com.dscvit.gidget.common.Security
import com.dscvit.gidget.interfaces.RetroFitService
import com.dscvit.gidget.models.searchPage.Items
import com.dscvit.gidget.models.searchPage.ItemsRepo
import com.dscvit.gidget.models.searchPage.SearchPageRepoModel
import com.dscvit.gidget.models.searchPage.SearchPageUserModel
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
        val emptySearchTextView: TextView = findViewById(R.id.searchPageNoItemsEmptyTextView)
        val searchText: EditText = findViewById(R.id.searchPageSearchText)
        val searchButton: ImageButton = findViewById(R.id.searchPageSearchButton)
        val orgButton: CardView = findViewById(R.id.searchPageOrganizationButton)
        val orgButtonText: TextView = findViewById(R.id.searchPageOrganizationButtonText)
        val repoButton: CardView = findViewById(R.id.searchPageRepoButton)
        val repoButtonText: TextView = findViewById(R.id.searchPageRepoButtonText)
        val recyclerView: RecyclerView = findViewById(R.id.searchPageRecyclerView)
        val progressBar: ProgressBar = findViewById(R.id.searchPageProgressBar)

        recyclerView.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        recyclerView.layoutManager = layoutManager
        recyclerView.edgeEffectFactory = BounceEdgeEffectFactory()

        var searchType = "users"
        orgButton.setOnClickListener {
            searchType = "users"
            searchText.hint = "Search User / Organization"
            repoButtonText.setTextColor(Color.WHITE)
            orgButtonText.setTextColor(Color.parseColor("#61B1FF"))
            recyclerView.removeAllViewsInLayout()
            recyclerView.visibility = View.INVISIBLE
            if (searchText.text.toString().isNotEmpty()) getSearchData(
                this,
                searchText.text.toString(),
                searchType,
                recyclerView,
                emptySearchTextView,
                progressBar
            )
        }
        repoButton.setOnClickListener {
            searchType = "repositories"
            searchText.hint = "Search Repositories"
            orgButtonText.setTextColor(Color.WHITE)
            repoButtonText.setTextColor(Color.parseColor("#61B1FF"))
            recyclerView.removeAllViewsInLayout()
            recyclerView.visibility = View.INVISIBLE
            if (searchText.text.toString().isNotEmpty())
                getSearchData(
                    this,
                    searchText.text.toString(),
                    searchType,
                    recyclerView,
                    emptySearchTextView,
                    progressBar
                )
        }
        searchButton.setOnClickListener {
            recyclerView.visibility = View.INVISIBLE
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
                recyclerView.visibility = View.INVISIBLE
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
        hideKeyBoard()
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
                        if (response.body()!!.total_count <= 0) {
                            val emptyResults = "No results found!"
                            progressBar.visibility = View.GONE
                            emptySearchTextView.text = emptyResults
                            emptySearchTextView.visibility = View.VISIBLE
                        } else if (response.body() != null) {
                            progressBar.visibility = View.GONE
                            emptySearchTextView.visibility = View.GONE

                            userAdapter = SearchPageUserAdapter(
                                this@SearchActivity,
                                response.body()!!.items as MutableList<Items>,
                                mService,
                            )
                            userAdapter.notifyDataSetChanged()
                            recyclerView.adapter = userAdapter
                            recyclerView.visibility = View.VISIBLE
                        }
                    }

                    override fun onFailure(call: Call<SearchPageUserModel>, t: Throwable) {
                        val notFoundResults = "No items searched…"
                        progressBar.visibility = View.GONE
                        emptySearchTextView.text = notFoundResults
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
                    if (response.body()!!.total_count <= 0) {
                        val emptyResults = "No results found!"
                        progressBar.visibility = View.GONE
                        emptySearchTextView.text = emptyResults
                        emptySearchTextView.visibility = View.VISIBLE
                    } else if (response.body() != null) {
                        progressBar.visibility = View.GONE
                        emptySearchTextView.visibility = View.GONE

                        repoAdapter = SearchPageRepoAdapter(
                            this@SearchActivity,
                            response.body()!!.items as MutableList<ItemsRepo>
                        )
                        repoAdapter.notifyDataSetChanged()
                        recyclerView.adapter = repoAdapter
                        recyclerView.visibility = View.VISIBLE
                    }
                }

                override fun onFailure(call: Call<SearchPageRepoModel>, t: Throwable) {
                    val notFoundResults = "No items searched…"
                    progressBar.visibility = View.GONE
                    emptySearchTextView.text = notFoundResults
                    emptySearchTextView.visibility = View.VISIBLE
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

    private fun hideKeyBoard() {
        this.currentFocus?.let { view ->
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}
