package com.example.grocify.ui

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
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

class ProfileFragment : Fragment() {
    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var firebaseAuthCheck: FirebaseAuth.AuthStateListener

    private var _binding: ProfileFragmentBinding? = null
    private val binding get() = _binding!!

    private var isActionInProgress: Boolean = false
    private var isToastShown: Boolean = false

    private var validName: Boolean = true
    private var validEmail: Boolean = true
    private var validPassword: Boolean = true
    private var validPaymentMethod: Boolean = true
    private var validZipCode: Boolean = true

    private var newName: String = ""
    private var newEmail: String = ""
    private var newPassword: String = ""
    private var newPaymentMethod: String = ""
    private var newZipCode: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ProfileFragmentBinding.inflate(inflater, container, false)

        viewModel.updateHeader("Profile", null, favoritesVisible = false)

        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuthCheck = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser != null)
                populateProfile()
        }

        FirebaseAuth.getInstance().addAuthStateListener(firebaseAuthCheck)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        FirebaseAuth.getInstance().removeAuthStateListener(firebaseAuthCheck)
    }

    private fun populateProfile() {
        initializeProfile()

        binding.edit.setOnClickListener {
            updateProfile(true, R.color.black, View.VISIBLE, View.GONE)

            binding.editPassword.setOnFocusChangeListener {_, isFocused ->
                if (isFocused) {
                    binding.editPassword.text = null
                    binding.editPassword.hint = resources.getString(R.string.password)
                }
            }
        }

        binding.cancel.setOnClickListener {
            updateProfile(false, R.color.gray, View.GONE, View.VISIBLE)
            removeErrors()
        }

        binding.save.setOnClickListener {
            initializeSave()

            val firebaseUser = FirebaseAuth.getInstance().currentUser

            checkErrors()

            if (firebaseUser?.displayName != newName) {
                val profileUpdates = UserProfileChangeRequest.Builder().setDisplayName(newName).build()

                if (validName)
                    firebaseUser?.updateProfile(profileUpdates)?.addOnCompleteListener { }
            }

            if (firebaseUser?.email != newEmail) {
                if (validEmail)
                    firebaseUser?.updateEmail(newEmail)?.addOnCompleteListener { }
            }

            if (validPassword)
                firebaseUser?.updatePassword(newPassword)?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.e("HELLO", "HI")
                    } else {
                        // Password update failed
                        val exception = task.exception
                        // Handle the exception or log the error message
                        Log.e("YOOO", "Error updating password: ${exception?.message}")
                    }
                }
//                firebaseUser?.updatePassword(newPassword)?.addOnCompleteListener { }

            Log.d("HERE123", "${validFields()}")

            if (!isActionInProgress) {
                isActionInProgress = true
                binding.saveButton.isEnabled = false

                lifecycleScope.launch {
                    if (validFields()) {
                        viewModel.setIsApiRequestCompleted(false)
                        viewModel.getLocations(newZipCode)

                        viewModel.isApiRequestCompleted.observe(viewLifecycleOwner) { isCompleted ->
                            if (isCompleted) {
                                viewModel.locations.observe(viewLifecycleOwner) { locations ->
                                    if (locations.data.isEmpty()) {
                                        validZipCode = false
                                        binding.editZipCode.error = resources.getString(R.string.no_kroger_location)
                                    } else {
                                        binding.editZipCode.error = null
                                        if (validFields())
                                            updateUser(newEmail, newName, newPaymentMethod, newZipCode, locations.data[0].locationId)
                                    }

                                    binding.save.isEnabled = true
                                    isActionInProgress = false
                                }
                            }
                        }
                    }
                }
            }
        }

        binding.logout.setOnClickListener {
            val confirmationDialog = ConfirmationDialogFragment {
                FirebaseAuth.getInstance().signOut()
                Toast.makeText(
                    requireContext(),
                    resources.getString(R.string.logout_success),
                    Toast.LENGTH_SHORT
                ).show()
            }
            confirmationDialog.show(parentFragmentManager, resources.getString(R.string.logout))
        }

        binding.pastTransactionsTextView.setOnClickListener{
            val action = ProfileFragmentDirections.actionProfileFragmentToPastPurchasesFragment()
            findNavController().navigate(action)
        }
    }

    private fun initializeProfile() {
        binding.edit.bringToFront()
        binding.cancelOrSave.visibility = View.GONE
        binding.edit.setColorFilter(ContextCompat.getColor(requireContext(), R.color.gray))
        binding.editName.setText(FirebaseAuth.getInstance().currentUser?.displayName)
        binding.editEmail.setText(FirebaseAuth.getInstance().currentUser?.email)
        binding.editPassword.setText(FirebaseAuth.getInstance().currentUser?.uid)

        viewModel.user.observe(viewLifecycleOwner) {
            if (it != null) {
                binding.editPaymentMethod.setText(it.paymentMethod)
                binding.editZipCode.setText(it.zipCode)
            }
        }
    }

    private fun validFields(): Boolean {
        return(validName && validEmail && validPassword && validPaymentMethod && validZipCode)
    }

    private fun checkErrors() {
        if (newName.isBlank() || newName.length > 32) {
            validName = false
            binding.editName.error = resources.getString(R.string.invalid_name)
        }

        if (newEmail.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
            validEmail = false
            binding.editEmail.error = resources.getString(R.string.invalid_email_address)
        }

        if (newPassword.isBlank() || newPassword.length > 32) {
            validPassword = false
            binding.editPassword.error = resources.getString(R.string.invalid_password)
        }

        if (newPaymentMethod.isBlank() || newPaymentMethod.length != 16 || !newPaymentMethod.all { it.isDigit() }) {
            validPaymentMethod = false
            binding.editPaymentMethod.error = resources.getString(R.string.invalid_card_number)
        }

        if (newZipCode.isBlank() || newZipCode.length != 5 || !newZipCode.all { it.isDigit() }) {
            validZipCode = false
            binding.editZipCode.error = resources.getString(R.string.invalid_zip_code)
        }
    }

    private fun removeErrors() {
        binding.editName.error = null
        binding.editEmail.error = null
        binding.editPassword.error = null
        binding.editPaymentMethod.error = null
        binding.editZipCode.error = null
    }

    private fun initializeSave() {
        validName = true
        validEmail = true
        validPassword = true
        validPaymentMethod = true
        validZipCode = true

        newName = binding.editName.text.toString()
        newEmail = binding.editEmail.text.toString()
        newPassword = binding.editPassword.text.toString()
        newPaymentMethod = binding.editPaymentMethod.text.toString()
        newZipCode = binding.editZipCode.text.toString()

        isToastShown = false
    }

    private fun updateProfile(isEnabled: Boolean, color: Int, cancelOrSubmitVisibility: Int, logoutVisibility: Int) {
        binding.edit.setColorFilter(ContextCompat.getColor(requireContext(), color))
        binding.editName.isEnabled = isEnabled
        binding.editEmail.isEnabled = isEnabled
        binding.editPassword.isEnabled = isEnabled
        binding.editPaymentMethod.isEnabled = isEnabled
        binding.editZipCode.isEnabled = isEnabled
        binding.editName.setTextColor(ContextCompat.getColor(requireContext(), color))
        binding.editEmail.setTextColor(ContextCompat.getColor(requireContext(), color))
        binding.editPassword.setTextColor(ContextCompat.getColor(requireContext(), color))
        binding.editPaymentMethod.setTextColor(ContextCompat.getColor(requireContext(), color))
        binding.editZipCode.setTextColor(ContextCompat.getColor(requireContext(), color))
        binding.editName.setText(FirebaseAuth.getInstance().currentUser?.displayName)
        binding.editEmail.setText(FirebaseAuth.getInstance().currentUser?.email)
        binding.editPassword.setText(FirebaseAuth.getInstance().currentUser?.uid)
        binding.editPaymentMethod.setText(viewModel.user.value!!.paymentMethod)
        binding.editZipCode.setText(viewModel.user.value!!.zipCode)
        binding.cancelOrSave.visibility = cancelOrSubmitVisibility
        binding.logout.visibility = logoutVisibility
    }

    private fun updateUser(newEmail: String, newName: String, newPaymentMethod: String, newZipCode: String, locationId: String) {
        viewModel.updateUser(
                User(
                    viewModel.user.value!!.userId,
                    newEmail,
                    newName,
                    viewModel.user.value!!.createdAt,
                    viewModel.user.value!!.lastLoginAt,
                    newPaymentMethod,
                    newZipCode,
                    locationId
                ),
                onSuccess = {
                    if (!isToastShown) {
                        Toast.makeText(
                            context,
                            resources.getString(R.string.profile_update_successful),
                            Toast.LENGTH_SHORT
                        ).show()
                        updateProfile(false, R.color.gray, View.GONE, View.VISIBLE)
                        isToastShown = true
                    }
                },
                onFailure = {
                    if (!isToastShown) {
                        Toast.makeText(
                            context,
                            resources.getString(R.string.profile_update_failed),
                            Toast.LENGTH_SHORT
                        ).show()
                        isToastShown = true
                    }
                }
        )
    }
}

class ConfirmationDialogFragment(private val onConfirmListener: () -> Unit) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes") { _, _ ->
                    onConfirmListener.invoke()
                }
                .setNegativeButton("Cancel") { _, _ -> }
            builder.create()
        } ?: throw IllegalStateException("Error")
    }
}
