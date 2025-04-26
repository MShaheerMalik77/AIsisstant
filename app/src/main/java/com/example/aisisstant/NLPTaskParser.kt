object NLPTaskParser {
    fun parse(input: String): Task {
        val title = input.replace(Regex("on|by|at|tomorrow|today|\\d{1,2}(am|pm)?"), "").trim()
        val date = when {
            input.contains("tomorrow", true) -> "Tomorrow"
            input.contains("today", true) -> "Today"
            input.contains("monday", true) -> "Monday"
            input.contains("tuesday", true) -> "Tuesday"
            else -> "Unspecified Date"
        }
        val category = categorize(input)
        return Task(title, date, category)
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
}

data class Task(
    val title: String,
    val date: String,
    val category:String
)