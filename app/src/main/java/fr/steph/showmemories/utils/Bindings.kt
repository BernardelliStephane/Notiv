package fr.steph.showmemories.utils

import android.graphics.drawable.Drawable
import android.os.Build
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.google.android.material.textfield.TextInputLayout
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@BindingAdapter("inputType")
fun setInputType(view: EditText, oldOption: String?, option: String) {
    if(option != oldOption){
        view.inputType = when (option) {
            "number" -> InputType.TYPE_CLASS_NUMBER
            "cap_words" -> InputType.TYPE_TEXT_FLAG_CAP_WORDS
            else -> InputType.TYPE_CLASS_TEXT
        }
    }
}

@BindingAdapter("visibility")
fun setVisibility(view: View, isVisible: Boolean) {
    view.isVisible = isVisible
}

@BindingAdapter("imageUrl", "placeholder")
fun loadImage(view: ImageView, oldUrl: String?, oldPlaceholder: Drawable?, url: String, placeholder: Drawable) {
    if(url.isNotBlank() && oldUrl != url) Glide.with(view.context).load(url).placeholder(placeholder).into(view)
}

@RequiresApi(Build.VERSION_CODES.O)
@BindingAdapter("dateAsText")
fun setDate(view: TextView, oldDate: Long, date: Long) {
    if(date != oldDate) view.text = DateTimeFormatter.ofPattern("dd-MM-yyyy").format(LocalDate.ofEpochDay(date))
}

@BindingAdapter("errorRes")
fun setErrorMessage(view: TextInputLayout, resError: Int?) {
    view.setResError(resError)
}

fun TextInputLayout.setResError(resError: Int?) {
    error = if(resError == null) null
    else context.resources.getString(resError)
}