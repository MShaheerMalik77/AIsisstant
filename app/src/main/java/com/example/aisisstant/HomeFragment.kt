package com.example.aisisstant

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.aisisstant.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.loadMood()

        // Observe LiveData
        viewModel.mood.observe(viewLifecycleOwner) { mood ->
            binding.moodStatus.text = "😊 Mood: ${mood ?: "Not set"}"
        }

        viewModel.suggestion.observe(viewLifecycleOwner) { suggestion ->
            binding.moodSuggestion.text = suggestion
        }

        binding.taskSummary.text = "📋 Tasks: ${viewModel.getSmartCategorizedTasks().size} Today"
        binding.reminderNote.text = "🔔 ${viewModel.getActiveReminders().size} Reminders set"
        binding.progressStatus.text = "📊 Task Streak: ${viewModel.getUserProgress()} days"

        // Button actions
        binding.addTaskButton.setOnClickListener {
            startActivity(Intent(requireContext(), TaskInputActivity::class.java))
        }

        binding.logMoodButton.setOnClickListener {
            startActivity(Intent(requireContext(), MoodLogActivity::class.java))
        }

        binding.voiceInputButton.setOnClickListener {
            startActivity(Intent(requireContext(), VoiceTaskActivity::class.java))
        }

        binding.summarizerButton.setOnClickListener {
            startActivity(Intent(requireContext(), SummarizerActivity::class.java))
        }

        binding.checklistButton.setOnClickListener {
            startActivity(Intent(requireContext(), ChecklistActivity::class.java))
        }

        binding.logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(requireContext(), LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val FIREBASE_URL = "https://aisisstant-default-rtdb.asia-southeast1.firebasedatabase.app/"
    }
}
