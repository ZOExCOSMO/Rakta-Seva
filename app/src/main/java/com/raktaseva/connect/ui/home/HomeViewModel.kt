package com.raktaseva.connect.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.raktaseva.connect.data.model.BloodRequest
import com.raktaseva.connect.data.model.Donor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _donor = MutableStateFlow<Donor?>(null)
    val donor: StateFlow<Donor?> = _donor

    private val _requests = MutableStateFlow<List<BloodRequest>>(emptyList())
    val requests: StateFlow<List<BloodRequest>> = _requests

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _selectedFilter = MutableStateFlow("All")
    val selectedFilter: StateFlow<String> = _selectedFilter

    init {
        loadDonorRealTime()   // ← real-time listener
        loadRequests()
    }

    fun setFilter(filter: String) {
        _selectedFilter.value = filter
    }

    private fun loadDonorRealTime() {
        val uid = auth.currentUser?.uid ?: return
        // Real-time listener — updates automatically when Firestore changes
        db.collection("donors").document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null && snapshot.exists()) {
                    _donor.value = snapshot.toObject(Donor::class.java)
                }
                _isLoading.value = false
            }
    }

    fun loadRequests() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                db.collection("blood_requests")
                    .whereEqualTo("status", "ACTIVE")
                    .addSnapshotListener { snapshot, _ ->
                        val list = snapshot?.documents?.mapNotNull {
                            it.toObject(BloodRequest::class.java)
                        } ?: emptyList()
                        _requests.value = list.sortedByDescending { it.aiUrgencyScore }
                        _isLoading.value = false
                    }
            } catch (e: Exception) {
                _isLoading.value = false
            }
        }
    }
}