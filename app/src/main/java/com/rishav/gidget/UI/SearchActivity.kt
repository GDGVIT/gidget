package com.rishav.gidget.UI

import android.graphics.Color
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rishav.gidget.Adapters.SearchPageRepoAdapter
import com.rishav.gidget.Adapters.SearchPageUserAdapter
import com.rishav.gidget.Common.Common
import com.rishav.gidget.Interface.RetroFitService
import com.rishav.gidget.Models.SearchPage.Items
import com.rishav.gidget.Models.SearchPage.ItemsRepo
import com.rishav.gidget.Models.SearchPage.SearchPageRepoModel
import com.rishav.gidget.Models.SearchPage.SearchPageUserModel
import com.rishav.gidget.R
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
        val searchText: EditText = findViewById(R.id.searchPageSearchText)
        val searchButton: ImageButton = findViewById(R.id.searchPageSearchButton)
        val orgButton: Button = findViewById(R.id.searchPageOrganizationButton)
        val repoButton: Button = findViewById(R.id.searchPageRepoButton)
        val recyclerView: RecyclerView = findViewById(R.id.searchPageRecyclerView)
        recyclerView.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        var searchType = "users"
        orgButton.setOnClickListener {
            searchType = "users"
            searchText.hint = "Search User / Organization"
            repoButton.setTextColor(Color.WHITE)
            orgButton.setTextColor(Color.parseColor("#61B1FF"))
            recyclerView.removeAllViewsInLayout()
        }

        repoButton.setOnClickListener {
            searchType = "repositories"
            searchText.hint = "Search Repositories"
            orgButton.setTextColor(Color.WHITE)
            repoButton.setTextColor(Color.parseColor("#61B1FF"))
            recyclerView.removeAllViewsInLayout()
        }
        searchButton.setOnClickListener {
            getSearchData(searchText.text.toString(), searchType, recyclerView)
        }
        searchText.setOnEditorActionListener { textView, actionId, keyEvent ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                getSearchData(searchText.text.toString(), searchType, recyclerView)
            }
            false
        }
        backButton.setOnClickListener { finish() }
    }

    private fun getSearchData(
        searchText: String,
        searchType: String,
        recyclerView: RecyclerView
    ) {
        if (searchType == "users") {
            mService.searchUser(searchText, System.getenv("token") ?: "null")
                .enqueue(object : Callback<SearchPageUserModel> {
                    override fun onResponse(
                        call: Call<SearchPageUserModel>,
                        response: Response<SearchPageUserModel>
                    ) {
                        if (response.body() != null) {
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
                        println("Error - ${t.message}")
                    }
                })
        } else if (searchType == "repositories") {
            mService.searchRepo(
                searchText,
                System.getenv("token")
                    ?: "null"
            ).enqueue(object : Callback<SearchPageRepoModel> {
                override fun onResponse(
                    call: Call<SearchPageRepoModel>,
                    response: Response<SearchPageRepoModel>
                ) {
                    if (response.body() != null) {
                        repoAdapter = SearchPageRepoAdapter(
                            this@SearchActivity,
                            response.body()!!.items as MutableList<ItemsRepo>,
                        )
                        repoAdapter.notifyDataSetChanged()
                        recyclerView.adapter = repoAdapter
                    }
                }

                override fun onFailure(call: Call<SearchPageRepoModel>, t: Throwable) {
                    println("Error - ${t.message}")
                }
            })
        }
    }
}
