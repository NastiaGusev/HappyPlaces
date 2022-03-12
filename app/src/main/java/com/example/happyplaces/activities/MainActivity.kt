package com.example.happyplaces.activities

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.happyplaces.adapters.HappyPlacesAdapter
import com.example.happyplaces.database.DatabaseHandler
import com.example.happyplaces.databinding.ActivityMainBinding
import com.example.happyplaces.models.HappyPlaceModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding

    companion object{
        var EXTRA_PLACE_DETAILS = "extra_place_details"
    }

    private var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            //val data: Intent? = result.data
            Log.d("TAG", "happy place added")
            getHappyPlacesListFromLocalDB()
        } else {
            Log.d("TAG", "Cancelled or back pressed")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.floatingActionBarAdd.setOnClickListener {
            val intent = Intent(this, AddHappyPlaceActivity::class.java)
            resultLauncher.launch(intent)
        }
        getHappyPlacesListFromLocalDB()
    }

    private fun setUpHappyPlacesRecyclerView(happyPlacesList: ArrayList<HappyPlaceModel>){
        binding.happyPlacesRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.happyPlacesRecyclerView.setHasFixedSize(true)
        val placesAdapter = HappyPlacesAdapter(happyPlacesList)
        binding.happyPlacesRecyclerView.adapter = placesAdapter

        placesAdapter.setOnClickListener(object :HappyPlacesAdapter.OnClickListener{
            override fun onClick(position: Int, model: HappyPlaceModel) {
                val intent = Intent(this@MainActivity, HappyPlaceDetailActivity::class.java)
                intent.putExtra(EXTRA_PLACE_DETAILS, model)
                startActivity(intent)
            }
        })
    }

    private fun getHappyPlacesListFromLocalDB(){
        val dbHandler = DatabaseHandler(this)
        val happyPlaceList :ArrayList<HappyPlaceModel> = dbHandler.getHappyPlacesList()

        if(happyPlaceList.size > 0) {
            binding.happyPlacesRecyclerView.visibility = View.VISIBLE
            binding.txtNoRecordsAvailable.visibility = View.GONE
            setUpHappyPlacesRecyclerView(happyPlaceList)
        } else {
            binding.happyPlacesRecyclerView.visibility = View.GONE
            binding.txtNoRecordsAvailable.visibility = View.VISIBLE
        }

    }
}