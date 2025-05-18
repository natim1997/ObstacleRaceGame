package com.example.mykotlin

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*

class MapScoresFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private var googleMap: GoogleMap? = null
    private var pending: LatLng? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_scores_map, container, false).also {
        mapView = it.findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map.apply {
            uiSettings.isZoomControlsEnabled = true

            moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(0.0, 0.0), 1f))
        }
        pending?.let { showLocation(it.latitude, it.longitude) }
    }

    fun showLocation(lat: Double, lng: Double) {
        val pos = LatLng(lat, lng)
        if (googleMap != null) {
            googleMap!!.clear()
            googleMap!!.addMarker(MarkerOptions().position(pos))
            googleMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 12f))
        } else {
            pending = pos
        }
    }

    override fun onResume()    { super.onResume();    mapView.onResume() }
    override fun onPause()     { mapView.onPause();    super.onPause() }
    override fun onDestroy()   { mapView.onDestroy();   super.onDestroy() }
    override fun onLowMemory() { super.onLowMemory();   mapView.onLowMemory() }
}
