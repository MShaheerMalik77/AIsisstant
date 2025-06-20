package com.example.aisisstant

import android.content.Context
import ai.onnxruntime.*
import org.json.JSONObject
import java.io.*
import java.nio.LongBuffer
import java.text.SimpleDateFormat
import java.util.*

object ONNXTaskParser {
    private lateinit var categorySession: OrtSession
    private lateinit var recurrenceSession: OrtSession
    private lateinit var env: OrtEnvironment
    private lateinit var categoryLabels: Map<Int, String>
    private lateinit var recurrenceLabels: Map<Int, String>
    private lateinit var categoryTokenizer: SimpleTokenizer
    private lateinit var recurrenceTokenizer: SimpleTokenizer

    private var isInitialized = false
    private const val MAX_SEQ_LEN = 128

    fun initialize(context: Context) {
        if (isInitialized) return

        env = OrtEnvironment.getEnvironment()

        categorySession = loadModelFromAssets(context, "model_cat.onnx")
        recurrenceSession = loadModelFromAssets(context, "model_rec.onnx")

        categoryLabels = loadLabelMap(context, "id2label_cat.json")
        recurrenceLabels = loadLabelMap(context, "id2label_rec.json")

        categoryTokenizer = SimpleTokenizer(context, "vocab_cat.txt")
        recurrenceTokenizer = SimpleTokenizer(context, "vocab_rec.txt")

        isInitialized = true
    }

    fun parse(context: Context, input: String): Task {
        initialize(context)

        val categoryInputIds = prepareInput(categoryTokenizer, input)
        val recurrenceInputIds = prepareInput(recurrenceTokenizer, input)

        val category = runInference(categorySession, categoryLabels, categoryInputIds)
        val recurrence = runInference(recurrenceSession, recurrenceLabels, recurrenceInputIds)

        return Task(
            id = UUID.randomUUID().toString(),
            title = extractTitle(input).ifEmpty { "Untitled Task" },
            date = extractDate(input),
            category = category,
            recurrence = recurrence
        )
    }

    private fun prepareInput(tokenizer: SimpleTokenizer, text: String): Pair<LongArray, LongArray> {
        var inputIds = tokenizer.encode(text).map { it.toLong() }.toLongArray()
        var attentionMask = LongArray(inputIds.size) { 1L }

        if (inputIds.isEmpty()) {
            inputIds = LongArray(1) { 0 }
            attentionMask = LongArray(1) { 1 }
        }

        if (inputIds.size > MAX_SEQ_LEN) {
            inputIds = inputIds.copyOfRange(0, MAX_SEQ_LEN)
            attentionMask = attentionMask.copyOfRange(0, MAX_SEQ_LEN)
        }

        return Pair(inputIds, attentionMask)
    }

    private fun runInference(session: OrtSession, labels: Map<Int, String>, input: Pair<LongArray, LongArray>): String {
        val (inputIds, attentionMask) = input
        val shape = longArrayOf(1, inputIds.size.toLong())
        val inputTensor = OnnxTensor.createTensor(env, LongBuffer.wrap(inputIds), shape)
        val maskTensor = OnnxTensor.createTensor(env, LongBuffer.wrap(attentionMask), shape)

        val inputs = mapOf(
            "input_ids" to inputTensor,
            "attention_mask" to maskTensor
        )

        session.run(inputs).use { result ->
            val output = result[0].value
            val logits = when {
                output is Array<*> && output.firstOrNull() is FloatArray -> (output as Array<FloatArray>)[0]
                output is Array<*> && output.firstOrNull() is Array<*> -> (output as Array<Array<FloatArray>>)[0][0]
                else -> return "Unknown"
            }
            val predIndex = logits.indices.maxByOrNull { logits[it] } ?: 0
            return labels[predIndex] ?: "Unknown"
        }
    }

    private fun loadLabelMap(context: Context, filename: String): Map<Int, String> {
        val jsonStr = context.assets.open(filename).bufferedReader().use { it.readText() }
        val jsonObject = JSONObject(jsonStr)
        val map = mutableMapOf<Int, String>()
        jsonObject.keys().forEach { key ->
            val label = jsonObject.optString(key, "")
            if (label.isNotEmpty()) {
                map[key.toInt()] = label
            }
        }
        return map
    }

    private fun extractTitle(input: String): String {
        val cleaned = input.replace(
            Regex("""\b(on|at|by|in|every|today|tomorrow|daily|weekly|monthly|next|this|due)\b.*""", RegexOption.IGNORE_CASE),
            ""
        ).replace(Regex("""\d{1,2}/\d{1,2}/\d{4}"""), "")
        return cleaned.trim().replace("\\s+".toRegex(), " ")
    }

    private fun extractDate(input: String): String {
        val lower = input.lowercase(Locale.getDefault())
        val calendar = Calendar.getInstance()

        return when {
            "today" in lower -> formatDate(calendar.time)
            "tomorrow" in lower -> {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
                formatDate(calendar.time)
            }
            else -> {
                val weekdays = mapOf(
                    "sunday" to Calendar.SUNDAY,
                    "monday" to Calendar.MONDAY,
                    "tuesday" to Calendar.TUESDAY,
                    "wednesday" to Calendar.WEDNESDAY,
                    "thursday" to Calendar.THURSDAY,
                    "friday" to Calendar.FRIDAY,
                    "saturday" to Calendar.SATURDAY
                )

                for ((name, day) in weekdays) {
                    if (name in lower) {
                        val currentDay = calendar.get(Calendar.DAY_OF_WEEK)
                        var daysUntilNext = (day - currentDay + 7) % 7
                        if (daysUntilNext == 0) daysUntilNext = 7
                        calendar.add(Calendar.DAY_OF_YEAR, daysUntilNext)
                        return formatDate(calendar.time)
                    }
                }

                return "Unspecified Date"
            }
        }
    }

    private fun formatDate(date: Date): String {
        return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)
    }

    class SimpleTokenizer(context: Context, vocabFile: String) {
        private val vocab: Map<String, Int>

        init {
            val map = mutableMapOf<String, Int>()
            val stream = context.assets.open(vocabFile)
            stream.bufferedReader().useLines { lines ->
                var id = 0
                for (line in lines) {
                    val token = line.trim()
                    if (token.isNotEmpty()) map[token] = id++
                }
            }
            vocab = map
        }

        fun encode(text: String): List<Int> {
            return text.lowercase(Locale.getDefault())
                .replace(Regex("[^a-z0-9\\s]"), "")
                .split(" ")
                .filter { it.isNotBlank() }
                .map { vocab[it] ?: vocab["[UNK]"] ?: 1 }
        }
    }
    private fun loadModelFromAssets(context: Context, assetName: String): OrtSession {
        val file = File(context.filesDir, assetName)
        if (!file.exists()) {
            context.assets.open(assetName).use { input ->
                FileOutputStream(file).use { output ->
                    val buffer = ByteArray(4 * 1024)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                    }
                }
            }
        }
        return env.createSession(file.absolutePath)
    }

}
