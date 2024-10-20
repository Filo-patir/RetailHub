package com.iti4.retailhub.features.summary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.iti4.retailhub.MainActivity
import com.iti4.retailhub.R
import com.iti4.retailhub.databinding.FragmentSummaryBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SummaryFragment : Fragment() {
    private lateinit var binding: FragmentSummaryBinding
    private val viewModel by viewModels<SummaryViewModel>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnContinueShopping.setOnClickListener {
            requireActivity().findNavController(R.id.fragmentContainerView2).navigate(
                R.id.homeFragment, null,
                NavOptions.Builder().setPopUpTo(R.id.myBagFragment, true).build()
            )

        }
    }


    override fun onStart() {
        super.onStart()
        (activity as MainActivity).findViewById<BottomNavigationView>(R.id.navigationView).visibility =
            View.GONE
    }

    override fun onStop() {
        super.onStop()
        (activity as MainActivity).findViewById<BottomNavigationView>(R.id.navigationView).visibility =
            View.VISIBLE
    }


}
