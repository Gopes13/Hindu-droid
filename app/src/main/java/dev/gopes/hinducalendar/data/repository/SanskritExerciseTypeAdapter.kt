package dev.gopes.hinducalendar.data.repository

import com.google.gson.*
import dev.gopes.hinducalendar.domain.model.*
import java.lang.reflect.Type

/**
 * Gson deserializer for the [SanskritExercise] sealed class.
 *
 * Each exercise in JSON must have a `"type"` discriminator field, e.g.:
 * ```json
 * { "type": "letter_to_sound", "letter": { ... }, "distractors": [ ... ] }
 * ```
 */
class SanskritExerciseTypeAdapter : JsonDeserializer<SanskritExercise> {

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): SanskritExercise {
        val obj = json.asJsonObject
        val type = obj.get("type")?.asString
            ?: throw JsonParseException("SanskritExercise missing 'type' field")

        return when (type) {
            // ── Kāṇḍa 1 types ──────────────────────────────────────────────
            "letter_to_sound" -> context.deserialize(obj, SanskritExercise.LetterToSound::class.java)
            "sound_to_letter" -> context.deserialize(obj, SanskritExercise.SoundToLetter::class.java)
            "word_meaning" -> context.deserialize(obj, SanskritExercise.WordMeaning::class.java)
            "syllable_to_sound" -> context.deserialize(obj, SanskritExercise.SyllableToSound::class.java)
            "sound_to_syllable" -> context.deserialize(obj, SanskritExercise.SoundToSyllable::class.java)
            "word_reading" -> context.deserialize(obj, SanskritExercise.WordReading::class.java)

            // ── Kāṇḍa 2 types ──────────────────────────────────────────────
            "word_detective" -> context.deserialize(obj, SanskritExercise.WordDetective::class.java)

            // ── Kāṇḍa 3 types ──────────────────────────────────────────────
            "case_detective" -> context.deserialize(obj, SanskritExercise.CaseDetective::class.java)
            "build_shloka" -> context.deserialize(obj, SanskritExercise.BuildShloka::class.java)
            "fill_in_vibhakti" -> context.deserialize(obj, SanskritExercise.FillInVibhakti::class.java)

            // ── Kāṇḍa 4 types ──────────────────────────────────────────────
            "sandhi_split" -> context.deserialize(obj, SanskritExercise.SandhiSplit::class.java)
            "sandhi_join" -> context.deserialize(obj, SanskritExercise.SandhiJoin::class.java)

            // ── Kāṇḍa 5 types ──────────────────────────────────────────────
            "paradigm_fill" -> context.deserialize(obj, SanskritExercise.ParadigmFill::class.java)
            "compound_cracker" -> context.deserialize(obj, SanskritExercise.CompoundCracker::class.java)

            // ── Kāṇḍa 6-7 types ────────────────────────────────────────────
            "translation_attempt" -> context.deserialize(obj, SanskritExercise.TranslationAttempt::class.java)

            else -> throw JsonParseException("Unknown SanskritExercise type: $type")
        }
    }
}
