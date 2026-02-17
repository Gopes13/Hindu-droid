package dev.gopes.hinducalendar.ui.texts.share

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.core.content.FileProvider
import androidx.core.view.drawToBitmap
import java.io.File

object VerseShareHelper {

    fun shareVerse(
        context: Context,
        sanskrit: String,
        transliteration: String?,
        translation: String,
        reference: String,
        textName: String
    ) {
        val bitmap = renderShareCard(context) {
            VerseShareCard(
                sanskrit = sanskrit,
                transliteration = transliteration,
                translation = translation,
                reference = reference,
                textName = textName
            )
        }

        val file = saveBitmapToCache(context, bitmap)
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val shareText = "\"$translation\"\n— $textName, $reference\n\nShared via Dharmic Companion"

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, shareText)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Verse"))
    }

    private fun renderShareCard(context: Context, content: @Composable () -> Unit): Bitmap {
        val composeView = ComposeView(context).apply {
            setContent { content() }
        }

        val widthPx = (360 * context.resources.displayMetrics.density).toInt()
        val heightPx = (500 * context.resources.displayMetrics.density).toInt()

        composeView.measure(
            View.MeasureSpec.makeMeasureSpec(widthPx, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(heightPx, View.MeasureSpec.AT_MOST)
        )
        composeView.layout(0, 0, composeView.measuredWidth, composeView.measuredHeight)

        // Force a layout pass
        (composeView as ViewGroup).let { vg ->
            for (i in 0 until vg.childCount) {
                vg.getChildAt(i)?.let { child ->
                    child.measure(
                        View.MeasureSpec.makeMeasureSpec(widthPx, View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(heightPx, View.MeasureSpec.AT_MOST)
                    )
                    child.layout(0, 0, child.measuredWidth, child.measuredHeight)
                }
            }
        }

        return composeView.drawToBitmap(Bitmap.Config.ARGB_8888)
    }

    private fun saveBitmapToCache(context: Context, bitmap: Bitmap): File {
        val cacheDir = File(context.cacheDir, "shared_verses")
        cacheDir.mkdirs()
        val file = File(cacheDir, "verse_share.png")
        file.outputStream().use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        return file
    }
}
