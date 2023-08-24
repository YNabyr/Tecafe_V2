package com.example.ukl_kasir

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [UserFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class UserFragment : Fragment() {
    private lateinit var rvUser: RecyclerView
    private lateinit var userAdapter: UserAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_user, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvUser = view.findViewById(R.id.rvUser)
        rvUser.layoutManager = LinearLayoutManager(requireContext())

        val firestore = FirebaseFirestore.getInstance()
        val userCollection = firestore.collection("User")

        userCollection.get()
            .addOnSuccessListener { result ->
                val userList = mutableListOf<UserModel>()
                for (document in result) {
                    val user = document.toObject(UserModel::class.java)
                    userList.add(user)
                }
                userAdapter = UserAdapter(userList)
                rvUser.adapter = userAdapter
            }
            .addOnFailureListener { exception ->
                // Handle any errors here
            }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            UserFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}