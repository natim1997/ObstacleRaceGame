package com.example.mykotlin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions


class MapScoresFragment : Fragment(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private var pendingLocation: LatLng? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_scores_map, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val mapFrag = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFrag.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        pendingLocation?.let { moveTo(it) }
    }


    fun showLocation(lat: Double, lng: Double) {
        val loc = LatLng(lat, lng)
        if (::map.isInitialized) {
            moveTo(loc)
        } else {
            pendingLocation = loc
        }
    }

    private fun moveTo(loc: LatLng) {
        map.clear()
        map.addMarker(MarkerOptions().position(loc))
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 12f))
    }
}
