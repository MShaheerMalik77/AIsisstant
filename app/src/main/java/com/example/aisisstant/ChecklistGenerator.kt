package com.example.aisisstant

object ChecklistGenerator {
        fun generate(goal: String): List<String> {
            return when {
                goal.contains("internship", true) -> listOf(
                    "Research companies",
                    "Update resume",
                    "Write cover letters",
                    "Apply online",
                    "Prepare for interviews"
                )

                goal.contains("exam", true) -> listOf(
                    "Gather study material",
                    "Create study schedule",
                    "Revise notes",
                    "Solve past papers",
                    "Take mock tests"
                )

                goal.contains("project", true) -> listOf(
                    "Define requirements",
                    "Create design",
                    "Implement modules",
                    "Test functionality",
                    "Deploy project"
                )

                else -> listOf(
                    "Break down the goal",
                    "Set deadlines",
                    "Track progress",
                    "Review and adjust",
                    "Celebrate completion!"
                )
            }
        }

}