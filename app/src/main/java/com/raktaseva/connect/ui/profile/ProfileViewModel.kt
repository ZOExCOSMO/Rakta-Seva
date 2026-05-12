package com.raktaseva.connect.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.raktaseva.connect.data.model.Donor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _donor = MutableStateFlow<Donor?>(null)
    val donor: StateFlow<Donor?> = _donor

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    init { loadDonor() }

    fun loadDonor() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val doc = db.collection("donors").document(uid).get().await()
                _donor.value = doc.toObject(Donor::class.java)
                _isLoading.value = false
            } catch (e: Exception) {
                _isLoading.value = false
            }
        }
    }

    fun updateAvailability(isAvailable: Boolean) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                db.collection("donors").document(uid)
                    .update("isAvailable", isAvailable).await()
                _donor.value = _donor.value?.copy(isAvailable = isAvailable)
            } catch (e: Exception) { }
        }
    }

    fun updateField(field: String, value: Any) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                db.collection("donors").document(uid)
                    .update(field, value).await()
            } catch (e: Exception) { }
        }
    }

    fun signOut(onSignedOut: () -> Unit) {
        auth.signOut()
        onSignedOut()
    }
}