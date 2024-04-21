package com.example.grocify.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.grocify.R
import com.example.grocify.databinding.ProfileFragmentBinding
import com.example.grocify.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.launch

class ProfileFragment: Fragment() {
    private val viewModel: MainViewModel by activityViewModels()

    private var _binding: ProfileFragmentBinding? = null
    private val binding get() = _binding!!

    private var isToastShown: Boolean = false

    private var validName: Boolean = true
    private var validPaymentMethod: Boolean = true
    private var validZipCode: Boolean = true

    private var newName: String = ""
    private var newPaymentMethod: String = ""
    private var newZipCode: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ProfileFragmentBinding.inflate(inflater, container, false)

        viewModel.updateHeader(
            resources.getString(R.string.profile),
            null,
            favoritesVisible = false
        )

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.user.observe(viewLifecycleOwner) { user ->
            if (user != null)
                populateProfile()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun populateProfile() {
        binding.edit.apply {
            setColorFilter(ContextCompat.getColor(requireContext(), R.color.gray))
            bringToFront()
        }

        binding.cancelOrSave.visibility = View.GONE

        binding.editName.setText(FirebaseAuth.getInstance().currentUser?.displayName)
        binding.editEmail.setText(FirebaseAuth.getInstance().currentUser?.email)

        viewModel.user.observe(viewLifecycleOwner) {
            if (it != null) {
                binding.editPaymentMethod.setText(viewModel.obfuscateCardNumber(requireContext(), it.paymentMethod))
                binding.editZipCode.setText(it.zipCode)
            }
        }

        binding.edit.setOnClickListener {
            updateAllFields(isValid = true, obfuscateCardNumber = false, R.color.black, View.VISIBLE, View.GONE)
            setNewValues()
            binding.editName.addTextChangedListener { onTextChanged() }
            binding.editPaymentMethod.addTextChangedListener { onTextChanged() }
            binding.editZipCode.addTextChangedListener { onTextChanged() }
        }

        binding.cancel.setOnClickListener {
            updateAllFields(isValid = false, obfuscateCardNumber = true, R.color.gray, View.GONE, View.VISIBLE)
        }

        binding.save.setOnClickListener {
            isToastShown = false

            setNewValues()
            checkErrors()

            if (validFields()) {
                lifecycleScope.launch {
                    viewModel.setIsApiRequestCompleted(false)
                    val locations = viewModel.getLocations(newZipCode)

                    viewModel.isApiRequestCompleted.observe(viewLifecycleOwner) { isCompleted ->
                        if (isCompleted) {
                            if (!locations?.data.isNullOrEmpty() && validFields()) {
                                val profileUpdates = UserProfileChangeRequest.Builder().setDisplayName(newName).build()
                                FirebaseAuth.getInstance().currentUser?.updateProfile(profileUpdates)?.addOnCompleteListener { }

                                updateUser(newName, newPaymentMethod, newZipCode, locations!!.data[0].locationId)
                            }
                            else
                                binding.editZipCode.error = resources.getString(R.string.no_kroger_location)
                        }
                    }
                }
            }
        }

        binding.logout.setOnClickListener {
            val confirmationDialog = Helpers.ConfirmationDialogFragment(
                {
                    FirebaseAuth.getInstance().signOut()
                    Toast.makeText(requireContext(), resources.getString(R.string.logout_success), Toast.LENGTH_SHORT).show()
                },
                resources.getString(R.string.logout_message),
                resources.getString(R.string.yes),
                resources.getString(R.string.cancel),
            )
            confirmationDialog.show(parentFragmentManager, resources.getString(R.string.logout))
        }

        binding.pastTransactionsTextView.setOnClickListener {
            val action = ProfileFragmentDirections.actionProfileFragmentToPastPurchasesFragment()
            findNavController().navigate(action)
        }
    }


    private fun validFields(): Boolean {
        return(validName && validPaymentMethod && validZipCode)
    }

    private fun setNewValues() {
        newName = binding.editName.text.toString()
        newPaymentMethod = binding.editPaymentMethod.text.toString()
        newZipCode = binding.editZipCode.text.toString()
    }

    private fun checkErrors() {
        if (newName.isBlank() || newName.length > 32) {
            validName = false
            binding.editName.error = resources.getString(R.string.invalid_name)
        }
        else
            validName = true

        if (newPaymentMethod.isBlank() || newPaymentMethod.length != 16 || !newPaymentMethod.all { it.isDigit() }) {
            validPaymentMethod = false
            binding.editPaymentMethod.error = resources.getString(R.string.invalid_card_number)
        }
        else
            validPaymentMethod = true

        if (newZipCode.isBlank() || newZipCode.length != 5 || !newZipCode.all { it.isDigit() }) {
            validZipCode = false
            binding.editZipCode.error = resources.getString(R.string.invalid_zip_code)
        }
        else
            validZipCode = true
    }

    private fun clearErrors() {
        binding.editName.error = null
        binding.editPaymentMethod.error = null
        binding.editZipCode.error = null

        binding.editName.removeTextChangedListener(null)
        binding.editPaymentMethod.removeTextChangedListener(null)
        binding.editZipCode.removeTextChangedListener(null)
    }

    private fun onTextChanged() {
        setNewValues()
        checkErrors()
        clearErrors()
        toggleSaveButton(validFields(), if (validFields()) R.color.black else R.color.gray)
    }

    private fun toggleSaveButton(isValid: Boolean, color: Int) {
        binding.saveText.apply{
            isEnabled = isValid
            setTextColor(context.resources.getColor(color, null))
        }

        binding.saveButton.apply{
            isEnabled = isValid
            setColorFilter(context.resources.getColor(color, null))
        }
    }

    private fun updateAllFields(isValid: Boolean, obfuscateCardNumber: Boolean, color: Int, cancelOrSubmitVisibility: Int, logoutVisibility: Int) {
        clearErrors()

        binding.edit.setColorFilter(ContextCompat.getColor(requireContext(), color))

        binding.editName.apply {
            isEnabled = isValid
            setTextColor(ContextCompat.getColor(requireContext(), color))
            setText(FirebaseAuth.getInstance().currentUser?.displayName)
        }

        binding.editPaymentMethod.apply {
            isEnabled = isValid
            setTextColor(ContextCompat.getColor(requireContext(), color))
            if (obfuscateCardNumber)
                setText(viewModel.obfuscateCardNumber(requireContext(), viewModel.user.value!!.paymentMethod))
            else
                setText(viewModel.user.value!!.paymentMethod)
        }

        binding.editZipCode.apply {
            isEnabled = isValid
            setTextColor(ContextCompat.getColor(requireContext(), color))
            setText(viewModel.user.value!!.zipCode)
        }

        binding.cancelOrSave.visibility = cancelOrSubmitVisibility
        binding.logout.visibility = logoutVisibility
    }

    private fun updateUser(newName: String, newPaymentMethod: String, newZipCode: String, locationId: String) {
        viewModel.updateUser(
            User(viewModel.user.value!!.userId,
                viewModel.user.value!!.email,
                newName,
                viewModel.user.value!!.createdAt,
                viewModel.user.value!!.lastLoginAt,
                newPaymentMethod,
                newZipCode,
                locationId
            ),
            onSuccess = {
                if (!isToastShown) {
                    Toast.makeText(context, resources.getString(R.string.profile_update_successful), Toast.LENGTH_SHORT).show()
                    updateAllFields(isValid = false, obfuscateCardNumber = true, R.color.gray, View.GONE, View.VISIBLE)
                    isToastShown = true
                }
            },
            onFailure = {
                if (!isToastShown) {
                    Toast.makeText(context, resources.getString(R.string.profile_update_failed), Toast.LENGTH_SHORT).show()
                    isToastShown = true
                }
            }
        )
    }
}
