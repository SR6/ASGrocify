package com.example.grocify.ui

import android.annotation.SuppressLint
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
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.grocify.R
import com.example.grocify.databinding.ProfileFragmentBinding
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

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

        binding.save.visibility = View.GONE
        binding.edit.setColorFilter(ContextCompat.getColor(requireContext(), R.color.gray))

        binding.editName.setText(userName)
        binding.editEmail.setText(userEmail)
        binding.editPassword.setHint("Password123")

        binding.edit.setOnClickListener {
            binding.edit.setColorFilter(ContextCompat.getColor(requireContext(), R.color.black))

            binding.logout.visibility = View.GONE
            binding.save.visibility = View.VISIBLE

            binding.editName.isEnabled = true
            binding.editEmail.isEnabled = true
            binding.editPassword.isEnabled = true
        }

        binding.save.setOnClickListener {
            var validName = true
            var validEmail = true
            var validPassword = true

            val newName = binding.editName.text.toString()
            val newEmail = binding.editEmail.text.toString()
            val newPassword = binding.editPassword.text.toString()

            val user = FirebaseAuth.getInstance().currentUser

            if (newName.isNotBlank() && user?.displayName != newName) {
                if (newName.length > 32) {
                    validName = false
                    Toast.makeText(requireContext(), "Name should not exceed 32 characters", Toast.LENGTH_SHORT).show()
                    binding.editName.error = "Invalid name"
                }

                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(newName)
                    .build()

                if (validName)
                    user?.updateProfile(profileUpdates)?.addOnCompleteListener {}
            }

            if (newEmail.isNotBlank() && user?.email != newEmail) {
                if (!Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
                    validEmail = false
                    Toast.makeText(requireContext(), "Invalid email address", Toast.LENGTH_SHORT).show()
                    binding.editEmail.error = "Invalid email address"
                }

                if (validEmail)
                    user?.updateEmail(newEmail)?.addOnCompleteListener { }
            }

//            if (newPassword.isNotBlank()) {
//                user?.updatePassword(newPassword)
//                    ?.addOnCompleteListener {
//                    }
//            }

            if (validName && validEmail && validPassword) {
                binding.edit.setColorFilter(ContextCompat.getColor(requireContext(), R.color.gray))
                binding.editName.isEnabled = false
                binding.editEmail.isEnabled = false
                binding.editPassword.isEnabled = false
                binding.save.visibility = View.GONE
                binding.logout.visibility = View.VISIBLE
            }
        }

        binding.logout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
        }
    }
}