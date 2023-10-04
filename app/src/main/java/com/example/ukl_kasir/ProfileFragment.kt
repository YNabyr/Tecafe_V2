package com.example.ukl_kasir

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.ukl_kasir.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var binding: FragmentProfileBinding
    private lateinit var viewModel: ProfileViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        viewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Fetch and display profile data
        fetchProfileData()

        // Edit button click listener
        binding.btnEdit.setOnClickListener {
            val editIntent = Intent(requireContext(), EditProfileActivity::class.java)
            startActivity(editIntent)
        }

        binding.btnLogout.setOnClickListener {
            logOutUser()
        }

        // Observe profile data from ViewModel
        viewModel.profileData.observe(viewLifecycleOwner, Observer { profileData ->
            if (profileData != null) {
                displayProfileData(profileData)
            }
        })
    }

    private fun fetchProfileData() {
        val userId = getUserId()
        if (userId != null) {
            val profileRef = db.collection("profile").document(userId)
            profileRef.get().addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val profileData = document.toObject(ProfileModel::class.java)
                    if (profileData != null) {
                        viewModel.setProfileData(profileData)
                    } else {
                        showError("Failed to retrieve profile data")
                    }
                } else {
                    showError("Profile data not found")
                }
            }.addOnFailureListener { exception ->
                showError("Error fetching profile data: ${exception.message}")
            }
        } else {
            showToastMessage("User not authenticated")
        }
    }

    private fun displayProfileData(data: ProfileModel) {
        // Display profile data on UI
        binding.tvName.text = data.name
        binding.tvAge.text = data.age
        binding.tvBio.text = data.bio

        if (!data.image.isNullOrEmpty()) {
            val requestOptions = RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.placeholder_profile)
                .error(R.drawable.placeholder_profile)

            Glide.with(requireContext())
                .load(data.image)
                .apply(requestOptions)
                .into(binding.ivProfileImage)
        }
    }

    private fun getUserId(): String? {
        return auth.currentUser?.uid
    }

    private fun logOutUser() {
        auth.signOut()
        showToastMessage("Logged out successfully.")
        val intent = Intent(requireContext(), LoginActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }

    private fun showToastMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}