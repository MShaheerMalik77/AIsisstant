import com.example.aisisstant.Task
import java.text.SimpleDateFormat
import java.util.*

object NLPTaskParser {
    private val dayOfWeekMap = mapOf(
        "sunday" to Calendar.SUNDAY,
        "monday" to Calendar.MONDAY,
        "tuesday" to Calendar.TUESDAY,
        "wednesday" to Calendar.WEDNESDAY,
        "thursday" to Calendar.THURSDAY,
        "friday" to Calendar.FRIDAY,
        "saturday" to Calendar.SATURDAY
    )

    fun parse(input: String): Task {
        val date = extractDate(input)
        val title = cleanTitle(input)
        val category = categorize(input)

        return Task(
            id = "", // Firebase will generate it
            title = title,
            date = date,
            category = category
        )
    }

    private fun extractDate(input: String): String {
        val lower = input.lowercase(Locale.getDefault())

        // Check for explicit DD/MM/YYYY date
        val dateRegex = Regex("""\b(\d{1,2})/(\d{1,2})/(\d{4})\b""")
        val match = dateRegex.find(lower)
        if (match != null) {
            return match.value // Return as-is
        }

        // Check for keywords like today, tomorrow
        val calendar = Calendar.getInstance()
        when {
            "today" in lower -> return formatDate(calendar.time)
            "tomorrow" in lower -> {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
                return formatDate(calendar.time)
            }
        }

        // Check for day of week
        for ((dayName, dayConstant) in dayOfWeekMap) {
            if (dayName in lower) {
                return getNextDayOfWeek(dayConstant)
            }
        }

        return "Unspecified Date"
    }

    private fun cleanTitle(input: String): String {
        // Remove date parts from the input for a clean title
        return input.replace(Regex("""on|by|at|tomorrow|today|monday|tuesday|wednesday|thursday|friday|saturday|sunday|\d{1,2}/\d{1,2}/\d{4}""", RegexOption.IGNORE_CASE), "")
            .replace("\\s+".toRegex(), " ") // Replace multiple spaces with one
            .trim()
    }

    private fun categorize(input: String): String {
        val lower = input.lowercase()
        return when {
            lower.contains("assignment") || lower.contains("exam") || lower.contains("class") -> "Academic"
            lower.contains("gym") || lower.contains("workout") || lower.contains("doctor") -> "Health"
            lower.contains("meet") || lower.contains("party") || lower.contains("hangout") -> "Social"
            lower.contains("family") || lower.contains("home") || lower.contains("chores") -> "Personal"
            else -> "General"
        }
    }

    private fun getNextDayOfWeek(dayOfWeek: Int): String {
        val calendar = Calendar.getInstance()
        val today = calendar.get(Calendar.DAY_OF_WEEK)
        var daysUntilNext = dayOfWeek - today
        if (daysUntilNext <= 0) {
            daysUntilNext += 7
        }
        calendar.add(Calendar.DAY_OF_YEAR, daysUntilNext)
        return formatDate(calendar.time)
    }

    private fun formatDate(date: Date): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return sdf.format(date)
    }
}
