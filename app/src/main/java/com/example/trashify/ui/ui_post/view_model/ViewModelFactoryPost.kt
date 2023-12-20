package com.example.trashify.ui.ui_post.view_model

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.trashify.data.di.Injection

class ViewModelFactoryPost(private val context: Context, private val token: String? = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOiJ1c2VyLWRJLTgyVENTb0tpbk1uUTYiLCJpYXQiOjE3MDMwOTA5NDR9.MMFJx06LQ_ndzETprqTx-kOMB7cXKojY5WhZmuY8xNM") : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PostViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PostViewModel(Injection.provideRepository(context, token = token)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}