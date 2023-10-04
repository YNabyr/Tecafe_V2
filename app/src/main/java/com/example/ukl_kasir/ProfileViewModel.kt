package com.example.ukl_kasir

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel


class ProfileViewModel : ViewModel() {
    private val _profileData = MutableLiveData<ProfileModel>()
    val profileData: LiveData<ProfileModel>
        get() = _profileData

    fun setProfileData(data: ProfileModel) {
        _profileData.value = data
    }
}