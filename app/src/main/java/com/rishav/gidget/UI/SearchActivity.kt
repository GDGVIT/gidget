package com.rishav.gidget.UI

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rishav.gidget.Adapters.SearchPageAdapter
import com.rishav.gidget.Common.Common
import com.rishav.gidget.Interface.RetroFitService
import com.rishav.gidget.Models.SearchPage.Items
import com.rishav.gidget.Models.SearchPage.SearchPageUserModel
import com.rishav.gidget.R
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchActivity : AppCompatActivity() {
    lateinit var mService: RetroFitService
    lateinit var layoutManager: LinearLayoutManager
    lateinit var adapter: SearchPageAdapter

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

        searchButton.setOnClickListener {
            getSearchData(searchText.text.toString(), orgButton, repoButton, recyclerView)
        }
    }

    private fun getSearchData(
        searchText: String,
        orgButton: Button,
        repoButton: Button,
        recyclerView: RecyclerView
    ) {
        mService.searchUser(searchText).enqueue(object : Callback<SearchPageUserModel> {
            override fun onResponse(
                call: Call<SearchPageUserModel>,
                response: Response<SearchPageUserModel>
            ) {
                if (response.body() != null) {
                    adapter = SearchPageAdapter(
                        this@SearchActivity,
                        response.body()!!.items as MutableList<Items>,
                        mService,
                    )
                }

                adapter.notifyDataSetChanged()
                recyclerView.adapter = adapter
            }

            override fun onFailure(call: Call<SearchPageUserModel>, t: Throwable) {
                println("Error - ${t.message}")
            }

        })
    }
}