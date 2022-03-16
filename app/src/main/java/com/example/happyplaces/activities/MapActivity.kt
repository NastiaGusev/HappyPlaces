package com.example.happyplaces.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.happyplaces.R
import com.example.happyplaces.databinding.ActivityMapBinding
import com.example.happyplaces.models.HappyPlaceModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_map.view.*


class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMapBinding
    private var happyPlaceDetails: HappyPlaceModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Get the HappyPlace details from the Intent
        if(intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)){
            happyPlaceDetails = intent.getParcelableExtra(MainActivity.EXTRA_PLACE_DETAILS) as HappyPlaceModel?
        }

        if(happyPlaceDetails != null) {
            setSupportActionBar(binding.toolBarMap)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.title = happyPlaceDetails!!.title
            binding.toolBarMap.setNavigationOnClickListener {
                onBackPressed()
            }

            val supportMapFragment : SupportMapFragment =
                supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
            supportMapFragment.getMapAsync(this)
        }

    }

    override fun onMapReady(googleMap: GoogleMap?) {
        val position = LatLng(happyPlaceDetails!!.latitude, happyPlaceDetails!!.longitude)
        googleMap!!.addMarker(MarkerOptions().position(position).title(happyPlaceDetails!!.location))
        val newLatLngZoom = CameraUpdateFactory.newLatLngZoom(position, 10f)
        googleMap.animateCamera(newLatLngZoom)
    }
}