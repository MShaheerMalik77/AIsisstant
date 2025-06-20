package com.example.aisisstant

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.aisisstant.HomeFragment.Companion.FIREBASE_URL
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class HomeViewModel : ViewModel() {

    private val database = FirebaseDatabase.getInstance(FIREBASE_URL).reference
    private val auth = FirebaseAuth.getInstance()

    private val _mood = MutableLiveData<String?>()
    val mood: LiveData<String?> get() = _mood

    private val _suggestion = MutableLiveData<String>()
    val suggestion: LiveData<String> get() = _suggestion

    fun loadMood() {
        val userId = auth.currentUser?.uid ?: return
        val moodRef = database.child("users").child(userId).child("moods")

        moodRef.orderByChild("timestamp").limitToLast(1)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val mood = snapshot.children.firstOrNull()?.child("mood")?.getValue(String::class.java)
                    _mood.value = mood
                    _suggestion.value = getSuggestionForMood(mood)
                }

                override fun onCancelled(error: DatabaseError) {
                    _mood.value = null
                    _suggestion.value = "Error loading mood."
                }
            })
    }

    private fun getSuggestionForMood(mood: String?): String {
        return when (mood?.lowercase()) {
            "happy" -> "You're in a good mood! Great time to tackle hard tasks."
            "stressed" -> "Take a breather. Focus on light or essential tasks."
            "sad" -> "Consider journaling or doing something you enjoy."
            "tired" -> "Prioritize rest. Reschedule less urgent tasks."
            else -> "Log your mood to get personalized suggestions!"
        }
    }

    fun getSmartCategorizedTasks(): List<String> = listOf("Assignment", "Health", "Personal")
    fun getActiveReminders(): List<String> = listOf("Submit report", "Call mentor")
    fun getUserProgress(): Int = 4
}
