package com.example.grocify.ui

import android.annotation.SuppressLint
import android.app.Dialog
import android.opengl.Visibility
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ProfileFragmentBinding.inflate(inflater, container, false)

        binding.edit.bringToFront()

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
        var userName = FirebaseAuth.getInstance().currentUser?.displayName
        val userEmail = FirebaseAuth.getInstance().currentUser?.email
        val userPassword = FirebaseAuth.getInstance().currentUser?.uid

        binding.cancelOrSave.visibility = View.GONE

        binding.edit.setColorFilter(ContextCompat.getColor(requireContext(), R.color.gray))

        binding.editName.setText(userName)
        binding.editEmail.setText(userEmail)
        binding.editPassword.setText("Password123")

        viewModel.user.observe(viewLifecycleOwner) {
            if (it != null) {
                binding.editPaymentMethod.setText(it.paymentMethod)
                binding.editZipCode.setText(it.zipCode)
            }
        }

        binding.edit.setOnClickListener {
            updateProfile(true, R.color.black, View.VISIBLE, View.GONE)
        }

        binding.cancel.setOnClickListener {
            updateProfile(false, R.color.gray, View.GONE, View.VISIBLE)
            binding.editName.error = null
            binding.editEmail.error = null
            binding.editPassword.error = null
            binding.editPaymentMethod.error = null
            binding.editZipCode.error = null
        }

        binding.save.setOnClickListener {
            var locationId = ""

            var validName = true
            var validEmail = true
            var validPassword = true
            var validPaymentMethod = true
            var validZipCode = true

            val newName = binding.editName.text.toString()
            val newEmail = binding.editEmail.text.toString()
            val newPassword = binding.editPassword.text.toString()
            var newPaymentMethod = binding.editPaymentMethod.text.toString()
            val newZipCode = binding.editZipCode.text.toString()

            val firebaseUser = FirebaseAuth.getInstance().currentUser

            if (firebaseUser?.displayName != newName) {
                if (newName.isBlank() || newName.length > 32) {
                    validName = false
                    Toast.makeText(requireContext(), resources.getString(R.string.invalid_name), Toast.LENGTH_SHORT).show()
                    binding.editName.error = resources.getString(R.string.invalid_name)
                }

                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(newName)
                    .build()

                if (validName)
                    firebaseUser?.updateProfile(profileUpdates)?.addOnCompleteListener { }
            }

            if (firebaseUser?.email != newEmail) {
                if (newEmail.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
                    validEmail = false
                    Toast.makeText(requireContext(), resources.getString(R.string.invalid_email_address), Toast.LENGTH_SHORT).show()
                    binding.editEmail.error = resources.getString(R.string.invalid_email_address)
                }

                if (validEmail)
                    firebaseUser?.updateEmail(newEmail)?.addOnCompleteListener { }
            }

//            if (newPassword.isNotBlank()) {
//                user?.updatePassword(newPassword)
//                    ?.addOnCompleteListener {
//                    }
//            }

            if (newPaymentMethod.isBlank() || newPaymentMethod.length != 16 || !newPaymentMethod.all { it.isDigit() }) {
                validPaymentMethod = false
                Toast.makeText(requireContext(), resources.getString(R.string.invalid_card_number), Toast.LENGTH_SHORT).show()
                binding.editPaymentMethod.error = resources.getString(R.string.invalid_card_number)
            }

            if (newZipCode.isBlank() || newZipCode.length != 5 || !newZipCode.all { it.isDigit() }) {
                validZipCode = false
                Toast.makeText(requireContext(), resources.getString(R.string.invalid_zip_code), Toast.LENGTH_SHORT).show()
                binding.editZipCode.error = resources.getString(R.string.invalid_zip_code)
            }

            if (validZipCode) {
                lifecycleScope.launch {
                    viewModel.getLocations(newZipCode)
                }

                viewModel.locations.observe(viewLifecycleOwner) { locations ->
                    if (locations.data.isEmpty()) {
                        validZipCode = false
                        Toast.makeText(requireContext(), resources.getString(R.string.no_kroger_location), Toast.LENGTH_SHORT).show()
                        binding.editZipCode.error = resources.getString(R.string.no_kroger_location)
                    }
                    else
                        locationId = locations.data[0].locationId
                }
            }

            if (validName && validEmail && validPassword && validPaymentMethod && validZipCode) {
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
                        Toast.makeText(context, "Profile successfully updated.", Toast.LENGTH_SHORT).show()
                        updateProfile(false, R.color.gray, View.GONE, View.VISIBLE)
                    },
                    onFailure = {
                        Toast.makeText(context, "Failed to update", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }

        binding.logout.setOnClickListener {
            val confirmationDialog = ConfirmationDialogFragment {
                FirebaseAuth.getInstance().signOut()
                Toast.makeText(requireContext(), "Logged out successfully.", Toast.LENGTH_SHORT).show()
            }
            confirmationDialog.show(parentFragmentManager, "LogoutDialog")
        }
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
        binding.cancelOrSave.visibility = cancelOrSubmitVisibility
        binding.logout.visibility = logoutVisibility
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
