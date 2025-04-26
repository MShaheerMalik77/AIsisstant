import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.aisisstant.TaskInputActivity
import com.example.aisisstant.MoodLogActivity
import com.example.aisisstant.VoiceTaskActivity
import com.example.aisisstant.ChecklistActivity
import com.example.aisisstant.SummarizerActivity
import com.example.aisisstant.LoginActivity
import com.google.firebase.auth.FirebaseAuth


import com.example.aisisstant.R

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mood = getUserMood()

        view.findViewById<TextView>(R.id.taskSummary).text = "📋 Tasks: 5 Today"
        view.findViewById<TextView>(R.id.moodStatus).text = "😊 Mood: $mood"
        view.findViewById<TextView>(R.id.reminderNote).text = "🔔 2 Reminders set"
        view.findViewById<TextView>(R.id.moodSuggestion).text = getSuggestionForMood(mood)

        view.findViewById<Button>(R.id.addTaskButton).setOnClickListener {
            startActivity(Intent(requireContext(), TaskInputActivity::class.java))
        }
        view.findViewById<Button>(R.id.logMoodButton).setOnClickListener {
            startActivity(Intent(requireContext(), MoodLogActivity::class.java))
        }
        view.findViewById<Button>(R.id.voiceInputButton).setOnClickListener {
            startActivity(Intent(requireContext(), VoiceTaskActivity::class.java))
        }
        view.findViewById<Button>(R.id.summarizerButton).setOnClickListener {
            startActivity(Intent(requireContext(), SummarizerActivity::class.java))
        }
        view.findViewById<Button>(R.id.checklistButton).setOnClickListener {
            startActivity(Intent(requireContext(), ChecklistActivity::class.java))
        }
        view.findViewById<Button>(R.id.logoutButton).setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }


    private fun getUserMood(): String {
        return "Stressed" // Later, fetch from mood logging feature
    }

    private fun getSuggestionForMood(mood: String): String {
        return when (mood.lowercase()) {
            "happy" -> "You're in a good mood! Great time to tackle hard tasks."
            "stressed" -> "Take a breather. Focus on light or essential tasks."
            "sad" -> "Consider journaling or doing something you enjoy."
            "tired" -> "Prioritize rest. Reschedule less urgent tasks."
            else -> "Set your mood to get personalized suggestions."
        }
    }
}



