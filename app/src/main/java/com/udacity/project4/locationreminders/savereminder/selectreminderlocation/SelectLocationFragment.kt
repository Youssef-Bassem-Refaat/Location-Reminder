package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.authentication.AuthenticationActivity.Companion.TAG
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

private const val PERMISSION_CODE_LOCATION_REQUEST=1
class SelectLocationFragment : BaseFragment() {

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var googleMap: GoogleMap
    private lateinit var mapFragment: SupportMapFragment
    private val REQUEST_LOCATION_PERMISSION = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        _viewModel.showToast.value="Please select POI that you would like to save"
        mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(OnMapReadyCallback {
            googleMap = it

            onMapReadyCallBack()
        })



        return binding.root
    }

    private fun onMapReadyCallBack() {
        val zoomLev = 18f
        val homeLatLng = LatLng(30.149389, 31.323278)
        //googleMap.addMarker(MarkerOptions().position(homeLatLng).title("Marker in Sydney"))
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(homeLatLng, zoomLev))

        enableMyLocation()
        setPOIClick()
        setLongClick()
        setMapStyle(googleMap)

    }

    private fun setLongClick() {
        googleMap.setOnMapLongClickListener{
            val poi= PointOfInterest(it, getString(R.string.dropped_pin),getString(R.string.dropped_pin))
            _viewModel.reminderSelectedLocationStr.value = getString(R.string.dropped_pin)
            _viewModel.latitude.value=it.latitude
            _viewModel.longitude.value=it.longitude
            _viewModel.selectedPOI.value=poi

            _viewModel.navigationCommand.value=NavigationCommand.Back
            //   getFragmentManager()?.popBackStack()
        }

    }

    private fun setPOIClick() {
        onLocationSelected()
    }

    private fun setMapStyle(googleMap: GoogleMap?) {
        val success = googleMap?.setMapStyle(
            MapStyleOptions.loadRawResourceStyle(
                requireContext(),
                R.raw.map_style
            )
        )
        if (!success!!) {
            Log.i(TAG, "Style Parsing failed")
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.size > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                _viewModel.showErrorMessage.value = "Permission is activated"
                enableMyLocation()
            }
            else{
                _viewModel.showErrorMessage.value=getString(R.string.permission_denied_explanation)
            }
        }
        else{
            _viewModel.showToast.value=R.string.permission_denied_explanation.toString()
            _viewModel.showErrorMessage.value = R.string.permission_denied_explanation.toString()
        }
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (isPermissionGranted()) {
            googleMap.isMyLocationEnabled=true
        } else {
            this.requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSION_CODE_LOCATION_REQUEST)

        }
    }


    private fun isPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) === PackageManager.PERMISSION_GRANTED
    }

    private fun onLocationSelected() {
        googleMap.setOnPoiClickListener{poi->
            _viewModel.selectedPOI.value=poi
            _viewModel.reminderSelectedLocationStr.value = poi.name
            _viewModel.latitude.value=poi.latLng.latitude
            _viewModel.longitude.value=poi.latLng.longitude
            val poiMarker= googleMap.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            )
            poiMarker.showInfoWindow()
            _viewModel.navigationCommand.value=NavigationCommand.Back
         //   getFragmentManager()?.popBackStack()
        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            googleMap.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            googleMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            googleMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }


}
