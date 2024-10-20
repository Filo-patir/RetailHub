package com.iti4.retailhub.features.address

import android.graphics.Rect
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.iti4.retailhub.MainActivity
import com.iti4.retailhub.R
import com.iti4.retailhub.databinding.FragmentAddressMapBinding
import com.iti4.retailhub.datastorage.network.ApiState
import com.iti4.retailhub.modelsdata.PlaceLocation
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker


class AddressMapFragment : Fragment(), OnClickMap {

    private lateinit var binding: FragmentAddressMapBinding
    private lateinit var map: MapView
    private lateinit var sharedFlow: MutableSharedFlow<String>
    private val isFirstTimeViewMap = true
    private val viewModel: AddressViewModel by activityViewModels()
    private val adapter by lazy {
        AddressLookupRecyclerViewAdapter(mutableListOf(), this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddressMapBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Configuration.getInstance().load(
            this.requireContext(),
            PreferenceManager.getDefaultSharedPreferences(this.requireContext())
        )
        map = binding.mapView
        setupMap(null)

        binding.rvSearch.layoutManager = LinearLayoutManager(this.requireContext())
        binding.rvSearch.adapter = adapter
        initSearchListener()
        initAddressGeocoding()
    }


    private fun setupMap(location: PlaceLocation? = null) {
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.mapCenter
        map.getLocalVisibleRect(Rect())
        map.setMultiTouchControls(true)


        if (location != null) {
            map.controller.setZoom(19.0)
            map.controller.setCenter(GeoPoint(location.lat.toDouble(), location.lon.toDouble()))
        } else {
            map.controller.setZoom(19.0)
            map.controller.setCenter(GeoPoint(31.200092, 29.918739))
        }

        val mapEventsReceiver = object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(point: GeoPoint?): Boolean {
                point?.let {
                    val lat = it.latitude
                    val long = it.longitude
                    val marker = Marker(map)
                    marker.position = GeoPoint(lat!!, long!!)
                    map.overlays.add(marker)
                    map.controller.setCenter(GeoPoint(lat!!, long!!))
                    viewModel.getLocationGeocoding(lat.toString(), long.toString())
                }
                return true
            }

            override fun longPressHelper(p: GeoPoint?): Boolean {
                return false
            }
        }
        val mapEventsOverlay = MapEventsOverlay(mapEventsReceiver)
        map.overlays.add(mapEventsOverlay)
    }


    private fun initSearchListener() {
        sharedFlow = MutableSharedFlow()
        binding.searchView.addTextChangedListener {
            lifecycleScope.launch {
                if (!it.isNullOrEmpty())
                    sharedFlow.emit(it.toString())
            }
        }
        lifecycleScope.launch {
            sharedFlow.debounce(300).collect {
                viewModel.getLocationSuggestions(it)
            }
        }

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                viewModel.addressLookUp.collect { item ->
                    when (item) {
                        is ApiState.Success<*> -> {
                            if (!(item.data is Error)) {
                                val response = item.data as MutableList<PlaceLocation>
                                adapter.setData(response)
                            }
                        }

                        is ApiState.Error ->
                            Log.i("here", "initSearchListener: err")

                        is ApiState.Loading -> {
                            Log.i("here", "initSearchListener: lloading")
                        }
                    }
                }
            }
        }
    }

    private fun initAddressGeocoding() {
        lifecycleScope.launch {
            viewModel.addressGeocoding.collect { item ->
                when (item) {
                    is ApiState.Success<*> -> {
                        if (!(item.data is Error)) {
                            val response =
                                item.data as com.iti4.retailhub.features.address.PlaceLocation
                            if (response.display_name.isNullOrEmpty())
                                Toast.makeText(
                                    this@AddressMapFragment.requireContext(),
                                    "Location Not Found",
                                    Toast.LENGTH_SHORT
                                ).show()
                            else {
                                val dialog = AddressGeocodingDialog(this@AddressMapFragment.requireContext(),this@AddressMapFragment)
                                dialog.show()
                                dialog.getData(response)
                                viewModel.selectedMapAddress = response
                                viewModel.setState()
                            }
                        }
                    }

                    is ApiState.Error ->
                        Log.i("here", "initSearchListener: err")

                    is ApiState.Loading -> {
                        Log.i("here", "initSearchListener: lloading")
                    }
                }
            }

        }
    }

    override fun goToAddress(location: PlaceLocation) {
        (activity as MainActivity).hideKeyboard()
        setupMap(location)
    }

    override fun navigateToDetails() {
        val navOptions = NavOptions.Builder()
            .setPopUpTo(R.id.addressMapFragment, true)
            .build()
        val bundle = Bundle().apply {
            putString("reason", "map")
        }
        findNavController().navigate(R.id.addressDetailsFragment, bundle, navOptions)
    }

    override fun onResume() {
        super.onResume()
        map.onResume()
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
    }

}