package com.example.ukl_kasir

import androidx.lifecycle.ViewModel

class FoodViewModel : ViewModel() {
    private var foodList: List<FoodModel> = emptyList()

    fun getFoodList(): List<FoodModel> {
        return foodList
    }

    fun setFoodList(list: List<FoodModel>) {
        foodList = list
    }
}

