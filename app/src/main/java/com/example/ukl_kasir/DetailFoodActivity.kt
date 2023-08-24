package com.example.ukl_kasir

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.ukl_kasir.databinding.ActivityAddFoodBinding
import com.example.ukl_kasir.databinding.ActivityDetailFoodBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage

class DetailFoodActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailFoodBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    private val PICK_IMAGE_REQUEST = 1
    private var menuId: String? = null
    private var oldImageName: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailFoodBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()

        getMenuDetailsFromIntent()

        binding.btnEdit.setOnClickListener {
            navigateToEditFoodActivity()

        }

        binding.btnDelete.setOnClickListener {
            deleteMenuData()
        }
    }

    private fun getMenuDetailsFromIntent() {
        menuId = intent.getStringExtra("id")
        oldImageName = intent.getStringExtra("gambar")
        val menuName = intent.getStringExtra("nama")
        val menuPrice = intent.getStringExtra("harga")
        val menuType = intent.getStringExtra("type")

        populateMenuDetails(menuName, menuPrice, menuType)
        loadMenuImage()
    }

    private fun populateMenuDetails(name: String?, price: String?, type: String?) {
        binding.menuName.setText(name)
        binding.menuPrice.setText(price)
        binding.menuType.setText(type)
    }

    private fun loadMenuImage() {
        Glide.with(this)
            .load(oldImageName)
            .into(binding.menuItemImage)
    }


    private fun navigateToEditFoodActivity() {
        val intent = Intent(this, EditFoodActivity::class.java)
        intent.putExtra("id", menuId)
        intent.putExtra("gambar", oldImageName)
        intent.putExtra("nama", binding.menuName.text.toString())
        intent.putExtra("harga", binding.menuPrice.text.toString())
        intent.putExtra("type", binding.menuType.text.toString())
        startActivity(intent)
    }
    private fun deleteMenuData() {
        if (menuId != null) {
            // Delete menu document from Firestore
            db.collection("menu").document(menuId!!)
                .delete()
                .addOnSuccessListener {
                    // Delete image data from Firebase Storage
                    deleteMenuImage()

                    val intent = Intent(this, AdminActivity::class.java)
                    Toast.makeText(this, "Menu deleted successfully", Toast.LENGTH_SHORT).show()
                    startActivity(intent)
                    finish() // Close the activity after successful deletion
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error deleting menu: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun deleteMenuImage() {
        if (oldImageName != null) {
            val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(oldImageName!!)
            storageRef.delete()
                .addOnSuccessListener {
                    // Image deleted successfully
                }
                .addOnFailureListener { e ->
                    // Handle the error if the image deletion fails
                    Toast.makeText(this, "Error deleting image: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }


}