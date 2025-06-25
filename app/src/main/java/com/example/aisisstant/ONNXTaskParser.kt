package com.example.aisisstant

import android.content.Context
import ai.onnxruntime.*
import android.util.Log
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
    private lateinit var categoryTokenizer: HFTokenizer
    private lateinit var recurrenceTokenizer: HFTokenizer

    private var isInitialized = false
    private const val MAX_SEQ_LEN = 128

    fun initialize(context: Context) {
        if (isInitialized) return

        env = OrtEnvironment.getEnvironment()

        categorySession = loadModelFromAssets(context, "model_cat.onnx")
        recurrenceSession = loadModelFromAssets(context, "model_rec.onnx")

        categoryLabels = loadLabelMap(context, "id2label_cat.json")
        recurrenceLabels = loadLabelMap(context, "id2label_rec.json")

        categoryTokenizer = HFTokenizer(context, "vocab_cat.txt", "tokenizer_cat.json")
        recurrenceTokenizer = HFTokenizer(context, "vocab_rec.txt", "tokenizer_rec.json")

        isInitialized = true
    }

    fun parse(context: Context, input: String): Task {
        initialize(context)

        val categoryInputIds = categoryTokenizer.encode(input)
        val category = runInference(categorySession, categoryLabels, categoryInputIds)

        val (tokens, inputIds, attentionMask) = recurrenceTokenizer.encodeWithTokens(input)
        val recurrencePreds = runTokenClassification(
            recurrenceSession,
            recurrenceLabels,
            inputIds to attentionMask,
            tokens
        )
        val recurrence = extractRecurrencePhrase(recurrencePreds)

        return Task(
            id = UUID.randomUUID().toString(),
            title = extractTitle(input).ifEmpty { "Untitled Task" },
            date = extractDate(input),
            category = category,
            recurrence = recurrence
        )
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
            val logits = (result[0].value as Array<FloatArray>)[0]
            val predIndex = logits.indices.maxByOrNull { logits[it] } ?: 0
            return labels[predIndex] ?: "Unknown"
        }
    }

    private fun runTokenClassification(
        session: OrtSession,
        labels: Map<Int, String>,
        input: Pair<LongArray, LongArray>,
        tokens: List<String>
    ): List<Pair<String, String>> {
        val (inputIds, attentionMask) = input
        val shape = longArrayOf(1, inputIds.size.toLong())
        val inputTensor = OnnxTensor.createTensor(env, LongBuffer.wrap(inputIds), shape)
        val maskTensor = OnnxTensor.createTensor(env, LongBuffer.wrap(attentionMask), shape)

        val inputs = mapOf(
            "input_ids" to inputTensor,
            "attention_mask" to maskTensor
        )

        session.run(inputs).use { result ->
            val logits = result[0].value as Array<Array<FloatArray>>
            val predLogits = logits[0]

            val predictions = mutableListOf<Pair<String, String>>()

            for (i in 1 until tokens.size - 1) {  // Skip [CLS] and [SEP]
                val token = tokens[i]
                val tokenLogits = predLogits[i]
                val maxIndex = tokenLogits.indices.maxByOrNull { tokenLogits[it] } ?: 0
                val label = labels[maxIndex] ?: "O"
                predictions.add(token to label)
            }

            return predictions
        }
    }
    private fun extractRecurrencePhrase(preds: List<Pair<String, String>>): String {
        val phrases = mutableListOf<String>()
        var current = mutableListOf<String>()

        for ((token, label) in preds) {
            if (token.startsWith("##")) continue

            when (label) {
                "B-RECURRENCE" -> {
                    if (current.isNotEmpty()) phrases.add(current.joinToString(" "))
                    current = mutableListOf(token)
                }
                "I-RECURRENCE" -> {
                    if (current.isNotEmpty()) current.add(token)
                }
                else -> {
                    if (current.isNotEmpty()) {
                        phrases.add(current.joinToString(" "))
                        current = mutableListOf()
                    }
                }
            }
        }

        if (current.isNotEmpty()) phrases.add(current.joinToString(" "))

        return phrases.firstOrNull() ?: "none"
    }



    private fun loadLabelMap(context: Context, filename: String): Map<Int, String> {
        val jsonStr = context.assets.open(filename).bufferedReader().use { it.readText() }
        val jsonObject = JSONObject(jsonStr)
        return jsonObject.keys().asSequence().associate { it.toInt() to jsonObject.getString(it) }
    }

    private fun extractTitle(input: String): String {
        return input.replace(
            Regex("""\b(on|at|by|in|every|today|tomorrow|daily|weekly|monthly|next|this|due)\b.*""", RegexOption.IGNORE_CASE),
            ""
        ).replace(Regex("""\d{1,2}/\d{1,2}/\d{4}"""), "")
            .trim().replace("\\s+".toRegex(), " ")
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
                "Unspecified Date"
            }
        }
    }

    private fun formatDate(date: Date): String {
        return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)
    }

    class HFTokenizer(context: Context, vocabFile: String, tokenizerJsonFile: String) {
        private val vocab: Map<String, Int>
        private val unkToken: String
        private val clsToken: String
        private val sepToken: String
        private val doLowerCase: Boolean

        init {
            val map = mutableMapOf<String, Int>()
            context.assets.open(vocabFile).bufferedReader().useLines { lines ->
                lines.forEachIndexed { i, line ->
                    map[line.trim()] = i
                }
            }
            vocab = map

            val tokenizerJson = JSONObject(context.assets.open(tokenizerJsonFile).bufferedReader().use { it.readText() })
            unkToken = tokenizerJson.optString("unk_token", "[UNK]")
            clsToken = tokenizerJson.optString("cls_token", "[CLS]")
            sepToken = tokenizerJson.optString("sep_token", "[SEP]")
            doLowerCase = tokenizerJson.optJSONObject("normalizer")?.optBoolean("lowercase", true) ?: true
        }

        fun encode(text: String): Pair<LongArray, LongArray> {
            val (_, ids, mask) = encodeWithTokens(text)
            return ids to mask
        }

        fun encodeWithTokens(text: String): Triple<List<String>, LongArray, LongArray> {
            var normalized = text
            if (doLowerCase) normalized = normalized.lowercase(Locale.getDefault())

            val words = normalized.trim().split("\\s+".toRegex())

            val tokens = mutableListOf<String>()
            for (word in words) {
                tokens.addAll(wordPieceTokenize(word))
            }

            val allTokens = listOf(clsToken) + tokens + listOf(sepToken)
            val inputIdsRaw = allTokens.map { (vocab[it] ?: vocab[unkToken] ?: 1).toLong() }.toLongArray()
            val attentionMaskRaw = LongArray(inputIdsRaw.size) { 1L }

            val paddedInputIds = LongArray(MAX_SEQ_LEN) { 0L }
            val paddedMask = LongArray(MAX_SEQ_LEN) { 0L }

            inputIdsRaw.copyInto(paddedInputIds, endIndex = inputIdsRaw.size.coerceAtMost(MAX_SEQ_LEN))
            attentionMaskRaw.copyInto(paddedMask, endIndex = attentionMaskRaw.size.coerceAtMost(MAX_SEQ_LEN))

            return Triple(allTokens, paddedInputIds, paddedMask)
        }

        private fun wordPieceTokenize(word: String): List<String> {
            val tokens = mutableListOf<String>()
            var start = 0

            while (start < word.length) {
                var end = word.length
                var subword: String? = null

                while (start < end) {
                    val piece = if (start == 0) word.substring(start, end) else "##" + word.substring(start, end)
                    if (vocab.containsKey(piece)) {
                        subword = piece
                        break
                    }
                    end--
                }

                if (subword != null) {
                    tokens.add(subword)
                    start = end
                } else {
                    tokens.add(unkToken)
                    break
                }
            }
            return tokens
        }
    }

    private fun loadModelFromAssets(context: Context, assetName: String): OrtSession {
        val file = File(context.filesDir, assetName)
        if (!file.exists()) {
            context.assets.open(assetName).use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
        }
        return env.createSession(file.absolutePath)
    }
}
