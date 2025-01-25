package app.gomuks.android

import android.app.Activity
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.result.contract.ActivityResultContract
import app.gomuks.android.BetterGetContent.Result
import app.gomuks.android.GeckoPrompts.Companion
import kotlin.math.min

class BetterGetContent : ActivityResultContract<BetterGetContent.Params, Result?>() {
    companion object {
        private const val LOGTAG = "Gomuks/BetterGetContent"
    }

    class Params(
        val mimeTypes: Array<String>? = null,
        val multiple: Boolean = false,
    ) {
        val type: String
            get() {
                if (mimeTypes == null) {
                    return "*/*"
                }
                var mimeType: String? = null
                var mimeSubtype: String? = null
                for (rawType in mimeTypes) {
                    val normalizedType = rawType.trim { it <= ' ' }.lowercase()
                    val len = normalizedType.length
                    var slash = normalizedType.indexOf('/')
                    if (slash < 0) {
                        slash = len
                    }
                    val newType = normalizedType.substring(0, slash)
                    val newSubtype: String =
                        normalizedType.substring(min(slash + 1, len))
                    if (mimeType == null) {
                        mimeType = newType
                    } else if (mimeType != newType) {
                        mimeType = "*"
                    }
                    if (mimeSubtype == null) {
                        mimeSubtype = newSubtype
                    } else if (mimeSubtype != newSubtype) {
                        mimeSubtype = "*"
                    }
                }
                return (mimeType ?: "*") + '/' + (mimeSubtype ?: "*")
            }
    }

    class Result(
        val uri: Uri?,
        val clip: ClipData?,
    )

    override fun createIntent(context: Context, input: Params) =
        Intent(Intent.ACTION_GET_CONTENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = input.type
            putExtra(Intent.EXTRA_LOCAL_ONLY, true)
            if (!input.mimeTypes.isNullOrEmpty()) {
                putExtra(Intent.EXTRA_MIME_TYPES, input.mimeTypes)
            }
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, input.multiple)
            Log.d(LOGTAG, "Creating intent with type=${input.type}, multiple=${input.multiple}, ${input.mimeTypes?.size} mime types")
        }

    override fun parseResult(resultCode: Int, intent: Intent?): Result? {
        return intent.takeIf { resultCode == Activity.RESULT_OK }?.let {
            Result(
                uri = it.data,
                clip = it.clipData,
            )
        }
    }
}