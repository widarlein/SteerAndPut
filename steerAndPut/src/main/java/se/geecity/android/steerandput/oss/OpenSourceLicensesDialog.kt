package se.geecity.android.steerandput.oss

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.text.method.LinkMovementMethod
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.webkit.WebView
import android.widget.LinearLayout
import android.widget.TextView
import se.geecity.android.steerandput.R

class OpenSourceLicensesDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val view = createView()

        return AlertDialog.Builder(activity)
                .setTitle(R.string.licence_dialog_title)
                .setView(view)
                .setPositiveButton(R.string.licence_dialog_ok, { dialogInterface, i ->
                    dialogInterface.dismiss()
                })
                .create()
    }

    private fun createView(): View {
        val linearLayout = LinearLayout(activity)
        linearLayout.orientation = LinearLayout.VERTICAL
        linearLayout.setPadding(0, dpToPixels(8), 0, 0)

        val textView = TextView(activity)
        textView.setText(R.string.licence_dialog_explaination)
        textView.movementMethod = LinkMovementMethod.getInstance()

        val webView = WebView(activity)
        webView.loadUrl("file:///android_asset/open_source_licenses.html")

        linearLayout.addView(textView, LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
            leftMargin = dpToPixels(8)
            rightMargin = dpToPixels(8)
            bottomMargin = dpToPixels(8)
        })
        linearLayout.addView(webView)

        return linearLayout
    }

    private fun dpToPixels(dp: Int): Int {
        val resources = getResources()
        val pixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), resources.displayMetrics)
        return pixels.toInt()
    }
}