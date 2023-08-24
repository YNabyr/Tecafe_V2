package com.example.ukl_kasir

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
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

    private lateinit var binding: FragmentProfileBinding
    private lateinit var kasirActivity: KasirActivity
    private lateinit var adminActivity: AdminActivity
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()


        // Mengambil data dari Firestore berdasarkan userId
        val userId = getUserId()

        if (userId != null) {
            val profileRef = db.collection("profile").document(userId)
            profileRef.get().addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val profileData = document.toObject(ProfileModel::class.java)
                    if (profileData != null) {
                        // Set retrieved data to UI elements
                        binding.tvName.text = profileData.name
                        binding.tvAge.text = profileData.age
                        binding.tvBio.text = profileData.bio

                        if (!profileData.image.isNullOrEmpty()) {
                            val requestOptions = RequestOptions()
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .placeholder(R.drawable.placeholder_profile) // Placeholder image while loading
                                .error(R.drawable.placeholder_profile) // Image to show in case of error

                            Glide.with(requireContext())
                                .load(profileData.image)
                                .apply(requestOptions)
                                .into(binding.ivProfileImage)
                        }
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

        // Edit button click listener
        binding.btnEdit.setOnClickListener {
            val editIntent = Intent(requireContext(), EditProfileActivity::class.java)
            startActivity(editIntent)
        }

        binding.btnLogout.setOnClickListener {
            logOutUser()
        }


    }


    fun getUserId(): String? {
        return auth.currentUser?.uid
    }

    private fun logOutUser() {
        auth.signOut()
        showToastMessage("Logged out successfully.")

        // Pindahkan pengguna ke RegistrationActivity
        val intent = Intent(requireContext(), LoginActivity::class.java)
        startActivity(intent)
        requireActivity().finish() // Menutup activity saat ini

    }



    private fun showToastMessage(message: String) {
        val context = requireContext()
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun showError(message: String) {
        val context = requireContext()
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

}