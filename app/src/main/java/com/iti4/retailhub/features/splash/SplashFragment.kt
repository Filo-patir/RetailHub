package com.iti4.retailhub.features.splash

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation.findNavController
import com.iti4.retailhub.MainActivity
import com.iti4.retailhub.R
import com.iti4.retailhub.features.login_and_signup.viewmodel.UserAuthunticationViewModelViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SplashFragment : Fragment() {
    val viewModel: UserAuthunticationViewModelViewModel by viewModels<UserAuthunticationViewModelViewModel>()
    lateinit var receivedString:String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = requireActivity().intent // get the intent that started this activity
        receivedString =
            intent.getStringExtra("guest").toString() // access the extra with key "guest"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launch {
            delay(3000)
            Log.d("p00000000000000000", "onViewCreated:${viewModel.isUserLoggedIn()} ")
            if((!viewModel.isUserLoggedIn())||receivedString=="guest"){
                //navigate to home fragment
                findNavController(view).navigate(R.id.action_splashFragment_to_signUpFragment)
            }else{
                //navigate to login fragment
                val intent= Intent(requireContext(), MainActivity::class.java)
                startActivity(intent)
                requireActivity().finish()
            }
           /* if((!viewModel.isUserLoggedIn())||receivedString=="guest"){
                //navigate to home fragment
                findNavController(view).navigate(R.id.action_splashFragment_to_signUpFragment)
            }else{
                //navigate to login fragment
                val intent= Intent(requireContext(), MainActivity::class.java)
                startActivity(intent)
                requireActivity().finish()
            }*/
        }
    }
}