package com.example.grocify.ui

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.res.Resources.Theme
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape
import android.os.Bundle
import android.util.Patterns
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.grocify.R
import com.example.grocify.databinding.ProfileFragmentBinding
import com.example.grocify.models.User
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import java.util.UUID

class ProfileFragment : Fragment() {
    private val viewModel: MainViewModel by activityViewModels()
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

        var userName = FirebaseAuth.getInstance().currentUser?.displayName

        if (userName.isNullOrBlank())
            userName = "Please add your name"

        val userEmail = FirebaseAuth.getInstance().currentUser?.email

        val userPassword = FirebaseAuth.getInstance().currentUser?.uid

        binding.cancelOrSubmit.visibility = View.GONE

        binding.edit.setColorFilter(ContextCompat.getColor(requireContext(), R.color.gray))

        binding.editName.setText(userName)
        binding.editEmail.setText(userEmail)
        binding.editPassword.setText("Password123")

        viewModel.user.observe(viewLifecycleOwner) {
            if (it != null)
                binding.editPaymentMethod.setText(it.paymentMethod)
        }


        binding.edit.setOnClickListener {
            binding.edit.setColorFilter(ContextCompat.getColor(requireContext(), R.color.black))

            binding.logout.visibility = View.GONE
            binding.cancelOrSubmit.visibility = View.VISIBLE

            binding.editName.isEnabled = true
            binding.editEmail.isEnabled = true
            binding.editPassword.isEnabled = true
            binding.editPaymentMethod.isEnabled = true
        }

        binding.cancel.setOnClickListener {
            binding.edit.setColorFilter(ContextCompat.getColor(requireContext(), R.color.gray))
            binding.editName.isEnabled = false
            binding.editEmail.isEnabled = false
            binding.editPassword.isEnabled = false
            binding.editPaymentMethod.isEnabled = false
            binding.cancelOrSubmit.visibility = View.GONE
            binding.logout.visibility = View.VISIBLE
            binding.editName.error = null
            binding.editEmail.error = null
            binding.editPassword.error = null
            binding.editPaymentMethod.error = null
        }

        binding.save.setOnClickListener {
            var validName = true
            var validEmail = true
            var validPassword = true
            var validPaymentMethod = true

            val newName = binding.editName.text.toString()
            val newEmail = binding.editEmail.text.toString()
            val newPassword = binding.editPassword.text.toString()
            val newPaymentMethod = binding.paymentMethod.text.toString()

            val firebaseUser = FirebaseAuth.getInstance().currentUser

            if (newName.isNotBlank() && firebaseUser?.displayName != newName) {
                if (newName.length > 32) {
                    validName = false
                    Toast.makeText(requireContext(), "Name should not exceed 32 characters", Toast.LENGTH_SHORT).show()
                    binding.editName.error = "Invalid name"
                }

                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(newName)
                    .build()

                if (validName)
                    firebaseUser?.updateProfile(profileUpdates)?.addOnCompleteListener {}
            }

            if (newEmail.isNotBlank() && firebaseUser?.email != newEmail) {
                if (!Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
                    validEmail = false
                    Toast.makeText(requireContext(), "Invalid email address", Toast.LENGTH_SHORT).show()
                    binding.editEmail.error = "Invalid email address"
                }

                if (validEmail)
                    firebaseUser?.updateEmail(newEmail)?.addOnCompleteListener { }
            }

//            if (newPassword.isNotBlank()) {
//                user?.updatePassword(newPassword)
//                    ?.addOnCompleteListener {
//                    }
//            }

            if (newPaymentMethod.isNotBlank() && viewModel.user.value!!.paymentMethod != newPaymentMethod) {
                if (newPaymentMethod.length != 16 || !newPaymentMethod.all { it.isDigit() }) {
                    validPaymentMethod = false
                    Toast.makeText(requireContext(), "Invalid card number", Toast.LENGTH_SHORT).show()
                    binding.editPaymentMethod.error = "Invalid card number"
                }
            }

            if (validName && validEmail && validPassword && validPaymentMethod) {
                viewModel.updateUser(
                    User(viewModel.user.value!!.userId, newEmail, newName, newPassword, newPaymentMethod),
                    onSuccess = { },
                    onFailure = {
                        Toast.makeText(context, "Failed to update user", Toast.LENGTH_SHORT).show()
                    }
                )

                binding.edit.setColorFilter(ContextCompat.getColor(requireContext(), R.color.gray))
                binding.editName.isEnabled = false
                binding.editEmail.isEnabled = false
                binding.editPassword.isEnabled = false
                binding.editPaymentMethod.isEnabled = false
                binding.cancelOrSubmit.visibility = View.GONE
                binding.logout.visibility = View.VISIBLE
            }
        }

        binding.logout.setOnClickListener {
            val confirmationDialog = ConfirmationDialogFragment {
                FirebaseAuth.getInstance().signOut()
                Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()
            }
            confirmationDialog.show(parentFragmentManager, "LogoutDialog")
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
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
