package com.iti4.retailhub.features.profile

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.AdapterView
import android.widget.Button
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import com.iti4.retailhub.R
import com.iti4.retailhub.databinding.FragmentProfileBinding
import com.iti4.retailhub.features.login_and_signup.view.CustomMessageDialog
import com.iti4.retailhub.features.login_and_signup.view.LoginAuthinticationActivity
import com.iti4.retailhub.features.login_and_signup.viewmodel.UserAuthunticationViewModelViewModel
import com.iti4.retailhub.logic.ToolbarSetup
import com.iti4.retailhub.models.CountryCodes
import com.iti4.retailhub.models.CurrencySpinnerItem
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileFragment : Fragment() {
    private var isExpanded: Boolean = false;
    private lateinit var binding: FragmentProfileBinding
    private val viewModel: ProfileViewModel by viewModels()
    private val authuntication: UserAuthunticationViewModelViewModel by viewModels()
lateinit var intent:Intent
    lateinit var customMesssageDialog : CustomMessageDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSpinner()
        customMesssageDialog = CustomMessageDialog(requireContext())

        ToolbarSetup.setupToolbar(
            binding.profileAppbar,
            "My Profile",
            resources,
            {activity?.onBackPressed()}
        )
         intent = Intent(requireContext(), LoginAuthinticationActivity::class.java)
        binding.profileAppbar.collapsedPageName.visibility = View.GONE
        if (authuntication.isguestMode()) {
            binding.profileLogoutBtn.text="Login"
            binding.profileOrderBtn.setOnClickListener {
                showGuestDialog()
            }
            binding.profileShippingBtn.setOnClickListener {
                showGuestDialog()
            }
            binding.profileLogoutBtn.setOnClickListener {
                val intent = Intent(requireContext(), LoginAuthinticationActivity::class.java)
                intent.putExtra("guest","guest")
                startActivity(intent)
                requireActivity().finish()
            }
        } else {
        binding.profileOrderBtn.setOnClickListener {
            Navigation.findNavController(view)
                .navigate(R.id.action_profileFragment_to_ordersFragment)
        }




//--------------------------------------------------------
binding.profileLogoutBtn.setOnClickListener {
    showLoginOutAlert()
}
        binding.profileShippingBtn.setOnClickListener{
            val bundle = Bundle().apply {
                putString("reason", "profile")
            }
            requireActivity().findNavController(R.id.fragmentContainerView2)
                .navigate(R.id.addressFragment, bundle)
        }

        lifecycleScope.launch {
            viewModel.user.collect {
                Log.d("Filo", "onViewCreated: $it")
                if (!it.containsKey("error")){
                    binding.profileName.text = buildString {
                        append(it["fName"])
                        append(" ")
                        append(it["lName"])
                    }
                    binding.profileMail.text = it["email"]
                }
            }
        }
    }
        binding.profileSettingsBtn.setOnClickListener {
            if (!isExpanded) {
                binding.apply{
                    expandableLayout.visibility = View.VISIBLE
                    profileSettingsBtn.setCompoundDrawables(null, null, ResourcesCompat.getDrawable(resources, R.drawable.baseline_keyboard_arrow_down_24, null), null)
                }

            } else {
                binding.apply{
                    expandableLayout.visibility = View.GONE
                    profileSettingsBtn.setCompoundDrawables(null, null, ResourcesCompat.getDrawable(resources, R.drawable.baseline_arrow_forward_ios_24, null), null)
                }
            }
            isExpanded = !isExpanded
        }}


    private fun showGuestDialog(){
        val dialog = Dialog(requireContext())

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)

        dialog.setContentView(R.layout.guest_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))


        val btnYes: Button = dialog.findViewById(R.id.btn_okayd)
        val btnNo: Button = dialog.findViewById(R.id.btn_canceld)
        val messag=dialog.findViewById<TextView>(R.id.messaged)
        messag.text="You are guest, please login first"
        btnYes.setOnClickListener {
            val intent = Intent(requireContext(), LoginAuthinticationActivity::class.java)
            intent.putExtra("guest","guest")
            startActivity(intent)
            requireActivity().finish()
        }

        btnNo.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showLoginOutAlert() {
        val dialog = Dialog(requireContext())

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)

        dialog.setContentView(R.layout.favorit_delete_alert)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))


        val btnYes: Button = dialog.findViewById(R.id.btnYes)
        val btnNo: Button = dialog.findViewById(R.id.btnNo)
        val tvmessage=dialog.findViewById<TextView>(R.id.tvMessage)
        tvmessage.text="Are you sure you want to loginout?"
        btnYes.setOnClickListener {
            viewModel.signOut()
            startActivity(intent)
            requireActivity().finish()
            dialog.dismiss()
        }

        btnNo.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
    private fun setupSpinner() {

        val options = listOf(
            CurrencySpinnerItem(CountryCodes.EGP),
            CurrencySpinnerItem(CountryCodes.USD),
            CurrencySpinnerItem(CountryCodes.EUR),
            CurrencySpinnerItem(CountryCodes.AED),
        )
        val adapter = CurrencySpinnerAdapter(requireActivity(), options)
        binding.spinnerCurrency.adapter = adapter
        binding.spinnerCurrency.post {
            binding.spinnerCurrency.setSelection(
                when (viewModel.getCurrencyCode()) {
                    CountryCodes.EGP -> 0
                    CountryCodes.USD -> 1
                    CountryCodes.EUR -> 2
                    CountryCodes.AED -> 3
                }
            )
        }
        binding.spinnerCurrency.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val selectedOption = options[position]
                    viewModel.setCurrencyCode(selectedOption.country)
                }

                override fun onNothingSelected(parent: AdapterView<*>) {

                }
            }
    }

}