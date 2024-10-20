package com.iti4.retailhub.features.login_and_signup.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.iti4.retailhub.MainActivity
import com.iti4.retailhub.R
import com.iti4.retailhub.databinding.FragmentSignUpBinding
import com.iti4.retailhub.features.login_and_signup.NetworkUtils
import com.iti4.retailhub.features.login_and_signup.viewmodel.UserAuthunticationViewModelViewModel
import com.iti4.retailhub.userauthuntication.AuthState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
@AndroidEntryPoint
class SignUpFragment : Fragment() {
    private var isLaunched = false
    lateinit var signUpBinding: FragmentSignUpBinding
    val userAuthViewModel: UserAuthunticationViewModelViewModel by viewModels<UserAuthunticationViewModelViewModel>()
    lateinit var customMesssageDialog : CustomMessageDialog
    lateinit var customLoadingDialog : CustomLoadingDialog
    val EMAIL_REGEX: String = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$"
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        signUpBinding = FragmentSignUpBinding.inflate(inflater, container, false)
        return signUpBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        customLoadingDialog = CustomLoadingDialog(requireContext())
        customMesssageDialog = CustomMessageDialog(requireContext())
        signUpBinding.redArrowGoToLogin.setOnClickListener {
            findNavController(view).navigate(R.id.action_signUpFragment_to_loginInFragment)
        }
        signUpOnClickListner()
        signUpBinding.googleCard.setOnClickListener {
//            userAuthViewModel.signInWithGoogle()
            if (NetworkUtils.isInternetAvailable(requireContext())) {
                googleSignIn()
            }else{
                Toast.makeText(requireContext(), "No internet connection", Toast.LENGTH_LONG).show()
            }
        }

        signUpBinding.guest.setOnClickListener {
            userAuthViewModel.setLoginStatus("guest")
            val intent= Intent(requireContext(), MainActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }
        if (!isLaunched) {
            isLaunched = true
        lifecycleScope.launch {
            userAuthViewModel.authState.collect { authResultState ->
                when (authResultState) {
                    is AuthState.Loading -> {
                        customLoadingDialog.show()
                    }
                    is AuthState.Success -> {
                        customLoadingDialog.dismiss()
                        val intent= Intent(requireContext(), MainActivity::class.java)
                        startActivity(intent)
                    }
                    is AuthState.Messages -> {
                        if (authResultState.error!="Idle") {
                            customLoadingDialog.dismiss()
                            if (authResultState.error=="Verification email is send") {
                                customMesssageDialog.setText(authResultState.error,"Chechout your email and login")
                                customMesssageDialog.show()
                            }else if (authResultState.error=="Failed to send verification email") {
                                customMesssageDialog.setText(authResultState.error,"Please try again")
                                customMesssageDialog.show()
                            }else if (authResultState.error=="Password is too weak") {
                                Toast.makeText(
                                    requireContext(),
                                    "Password is too weak",
                                    Toast.LENGTH_LONG
                                ).show()
                            }else if (authResultState.error=="Invalid email format") {
                                Toast.makeText(
                                    requireContext(),
                                    "Invalid email format",
                                    Toast.LENGTH_LONG
                                ).show()
                            }else if (authResultState.error=="Email is already in use") {
                                signUpBinding.emailTextInput.isErrorEnabled = true
                                signUpBinding.emailTextInput.error = authResultState.error
                            }
                            else {
                                Toast.makeText(
                                    requireContext(),
                                    authResultState.error,
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                  /*  is AuthState.SignInIntent -> {
                        customLoadingDialog.dismiss()
                        val request = IntentSenderRequest.Builder(authResultState.intentSender).build()
                        signInResultLauncher.launch(request)
                    }*/

                }
            }
        }
    }
    }

    private fun signUpOnClickListner() {
        signUpBinding.sigInBtn.setOnClickListener {
            signUpBinding.nameTextInput.isErrorEnabled  = false // Clear error message
            signUpBinding.emailTextInput.isErrorEnabled  = false // Clear error message
            signUpBinding.passowrdTex.isErrorEnabled  = false
            signUpBinding.nameTextInput.error = null // Clear error message
            signUpBinding.emailTextInput.error = null // Clear error message
            signUpBinding.passowrdTex.error = null // Clear error message
            val userName = signUpBinding.nameTextInput.editText?.text.toString().removeSuffix(" ")
            val email = signUpBinding.emailTextInput.editText?.text.toString().removeSuffix(" ")
            val password = signUpBinding.passowrdTex.editText?.text.toString().removeSuffix(" ")
            if (userName.isEmpty()) {
                signUpBinding.nameTextInput.isErrorEnabled  = true
                signUpBinding.nameTextInput.error = "Please enter your name"
                return@setOnClickListener
            }
            if (email.isEmpty()) {
                signUpBinding.emailTextInput.isErrorEnabled  = true
                signUpBinding.emailTextInput.error = "Please enter your email"
                return@setOnClickListener
            } else if (!email.matches(EMAIL_REGEX.toRegex())) {
                signUpBinding.emailTextInput.isErrorEnabled  = true
                signUpBinding.emailTextInput.error = "Invalid email format"
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                signUpBinding.passowrdTex.isErrorEnabled  = true
                signUpBinding.passowrdTex.error = "Please enter your password"
                return@setOnClickListener
            } else if (password.length < 6) {
                signUpBinding.passowrdTex.isErrorEnabled  = true
                signUpBinding.passowrdTex.error = "Password must be at least 6 characters"
                return@setOnClickListener
            }
            if (NetworkUtils.isInternetAvailable(requireContext())) {
                userAuthViewModel.createUser(userName, email, password)
            }else{
                Toast.makeText(requireContext(), "No internet connection", Toast.LENGTH_LONG).show()
            }
        }
    }

    /* private val signInResultLauncher = registerForActivityResult(
         ActivityResultContracts.StartIntentSenderForResult()
     ) { result ->
         if (result.resultCode == Activity.RESULT_OK) {
             if (result.data != null){
             userAuthViewModel.handleSignInResult(result.data!!)
                 }
         }
     }*/
   private fun googleSignIn(){
       val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
           .requestIdToken(getString(R.string.default_web_client_id))
           .requestEmail()
           .build()
       val client = GoogleSignIn.getClient(requireActivity(), options)
       val signInIntent = client.signInIntent
       startActivityForResult(signInIntent, 1234)
   }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1234 && resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                account?.idToken?.let { idToken ->
                    userAuthViewModel.signWithGoogle(idToken)
                }
            } catch (e: ApiException) {
                Log.e("LoginInFragment", "Google sign-in failed", e)
            }
        }
    }

}