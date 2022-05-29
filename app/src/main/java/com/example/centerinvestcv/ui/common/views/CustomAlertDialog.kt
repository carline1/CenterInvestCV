package com.example.centerinvestcv.ui.common.views

import android.content.Context
import android.util.AttributeSet
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.example.centerinvestcv.R

class CustomAlertDialog : LinearLayout {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    private val title: TextView?
        get() = findViewById(R.id.title)
    val editText: EditText?
        get() = findViewById(R.id.editText)
    private val negativeButton: TextView?
        get() = findViewById(R.id.negativeButton)
    private val positiveButton: TextView?
        get() = findViewById(R.id.positiveButton)

    fun showDialog(
        title: String,
        negativeButtonTitle: String,
        positiveButtonTitle: String,
        negativeButtonCallback: AlertDialog.() -> Unit,
        positiveButtonCallback: AlertDialog.() -> Unit
    ) {
        val saveFaceAlertDialog = AlertDialog.Builder(context).create()
        saveFaceAlertDialog.apply {
            setView(this@CustomAlertDialog)
            setCancelable(false)
        }
        this.title?.text = title
        negativeButton?.apply {
            text = negativeButtonTitle
            setOnClickListener { negativeButtonCallback(saveFaceAlertDialog) }
        }
        positiveButton?.apply {
            text = positiveButtonTitle
            setOnClickListener { positiveButtonCallback(saveFaceAlertDialog) }
        }
        saveFaceAlertDialog.show()
    }

}