package com.example.ukl_kasir

import android.content.Intent
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class FoodAdapter(private var foodList: List<FoodModel>) :
    RecyclerView.Adapter<FoodAdapter.FoodViewHolder>() {

    private lateinit var mListener: onItemClickListener

    interface onItemClickListener {
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(clickListener: onItemClickListener) {
        mListener = clickListener
    }

    class FoodViewHolder(view: View , clickListener: onItemClickListener) : RecyclerView.ViewHolder(view) {
        val menuImage: ImageView = view.findViewById(R.id.menuItemImageAdapter)
        val menuName: TextView = view.findViewById(R.id.menuNameAdapter)
        val menuType: TextView = view.findViewById(R.id.menuTypeAdapter)
        val menuPrice: TextView = view.findViewById(R.id.menuPriceAdapter)

        init {
            itemView.setOnClickListener {

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_menu, parent, false)
        return FoodViewHolder(view, mListener)
    }

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        val currentFood = foodList[position]

        Glide.with(holder.itemView.context)
            .load(currentFood.menuImage)
            .into(holder.menuImage)

        holder.menuName.text = currentFood.menuName
        holder.menuType.text = currentFood.menuType
        holder.menuPrice.text = currentFood.menuPrice.toString()

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, DetailFoodActivity::class.java)

            intent.putExtra("id", currentFood.menuId)
            intent.putExtra("gambar", currentFood.menuImage)
            intent.putExtra("nama", currentFood.menuName)
            intent.putExtra("type", currentFood.menuType)
            intent.putExtra("harga", currentFood.menuPrice.toString())
            context.startActivity(intent)
        }

    }

    fun getItemAtPosition(position: Int): FoodModel {
        return foodList[position]
    }
    override fun getItemCount(): Int {
        return foodList.size
    }


    fun updateData(newFoodList: List<FoodModel>) {
        foodList = newFoodList
        notifyDataSetChanged()
    }
}


