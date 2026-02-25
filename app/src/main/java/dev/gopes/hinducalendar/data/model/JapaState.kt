package dev.gopes.hinducalendar.data.model

import androidx.compose.ui.graphics.Color
import java.time.LocalDate

/** 6 mala bead materials with their visual properties. */
enum class MalaMaterial(
    val displayKey: String,
    val beadColor1: Long,
    val beadColor2: Long,
    val sumeruColorHex: Long
) {
    TULSI("tulsi", 0xFF5C4022, 0xFF472E14, 0xFFC5A029),
    RUDRAKSHA("rudraksha", 0xFF8C4D26, 0xFF66331A, 0xFFE88A2E),
    CRYSTAL("crystal", 0xFFE0E6F2, 0xFFBFC7D9, 0xFFB38CD9),
    SANDALWOOD("sandalwood", 0xFFD9B885, 0xFFB39461, 0xFFD9A638),
    ROSE_QUARTZ("rose_quartz", 0xFFEBB8C2, 0xFFCC94A6, 0xFFD9668C),
    LOTUS_SEED("lotus_seed", 0xFFE6D9BF, 0xFFC7B899, 0xFFCCB380);

    val beadGradient: List<Color> get() = listOf(Color(beadColor1), Color(beadColor2))
    val sumeruColor: Color get() = Color(sumeruColorHex)
}

/** 7 preset mantras. */
enum class PresetMantra(val displayKey: String, val sanskritKey: String) {
    OM_NAMAH_SHIVAYA("mantra_om_namah_shivaya", "ॐ नमः शिवाय"),
    HARE_KRISHNA("mantra_hare_krishna", "हरे कृष्ण हरे कृष्ण"),
    OM_GAN_GANAPATAYE("mantra_om_gan_ganapataye", "ॐ गं गणपतये नमः"),
    GAYATRI("mantra_gayatri", "ॐ भूर्भुवः स्वः"),
    MAHAMRITYUNJAYA("mantra_mahamrityunjaya", "ॐ त्र्यम्बकं यजामहे"),
    OM_NAMO_NARAYANAYA("mantra_om_namo_narayanaya", "ॐ नमो नारायणाय"),
    OM_MANI_PADME_HUM("mantra_om_mani_padme_hum", "ॐ मणि पद्मे हूँ");
}

/** Mantra selection: either a preset or custom text. */
sealed class MantraSelection {
    data class Preset(val mantra: PresetMantra) : MantraSelection()
    data class Custom(val text: String) : MantraSelection()

    fun displayText(): String = when (this) {
        is Preset -> mantra.sanskritKey
        is Custom -> text
    }
}

/** Japa counter state — persisted in UserPreferences. */
data class JapaState(
    val currentBead: Int = 0,
    val roundsToday: Int = 0,
    val totalRoundsLifetime: Int = 0,
    val selectedMaterialName: String = MalaMaterial.TULSI.name,
    val selectedMantraPresetName: String? = PresetMantra.OM_NAMAH_SHIVAYA.name,
    val customMantraText: String? = null,
    val lastJapaDate: String? = null,
    val japaStreak: Int = 0,
    val longestJapaStreak: Int = 0,
    val lastJapaRewardDate: String? = null,
    val roundsRewardedToday: Int = 0
) {
    val selectedMaterial: MalaMaterial
        get() = MalaMaterial.entries.find { it.name == selectedMaterialName } ?: MalaMaterial.TULSI

    val selectedMantra: MantraSelection
        get() = if (customMantraText != null) {
            MantraSelection.Custom(customMantraText)
        } else {
            val preset = PresetMantra.entries.find { it.name == selectedMantraPresetName }
                ?: PresetMantra.OM_NAMAH_SHIVAYA
            MantraSelection.Preset(preset)
        }

    /** Advance one bead. Returns new state + whether a round was completed. */
    fun advanceBead(): Pair<JapaState, Boolean> {
        val newBead = currentBead + 1
        return if (newBead >= 108) {
            // Round complete
            val today = LocalDate.now().toString()
            copy(
                currentBead = 0,
                roundsToday = roundsToday + 1,
                totalRoundsLifetime = totalRoundsLifetime + 1,
                lastJapaDate = today
            ) to true
        } else {
            copy(currentBead = newBead) to false
        }
    }

    /** Reset daily counters if the date changed. */
    fun resetDailyIfNeeded(): JapaState {
        val today = LocalDate.now().toString()
        if (lastJapaDate == today) return this
        val daysSince = if (lastJapaDate != null) {
            try {
                val last = LocalDate.parse(lastJapaDate)
                java.time.temporal.ChronoUnit.DAYS.between(last, LocalDate.now()).toInt()
            } catch (_: Exception) { 999 }
        } else 999
        val newStreak = if (daysSince == 1) japaStreak else 0
        return copy(
            roundsToday = 0,
            roundsRewardedToday = 0,
            japaStreak = newStreak,
            longestJapaStreak = maxOf(longestJapaStreak, newStreak)
        )
    }

    fun withMaterial(material: MalaMaterial) = copy(selectedMaterialName = material.name)

    fun withMantra(mantra: MantraSelection) = when (mantra) {
        is MantraSelection.Preset -> copy(
            selectedMantraPresetName = mantra.mantra.name,
            customMantraText = null
        )
        is MantraSelection.Custom -> copy(
            selectedMantraPresetName = null,
            customMantraText = mantra.text
        )
    }
}
