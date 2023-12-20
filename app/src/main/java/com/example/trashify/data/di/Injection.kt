package com.example.trashify.data.di

import android.content.Context
import com.example.trashify.data.database.PostDatabase
import com.example.trashify.data.database.PostRepository
import com.example.trashify.data.retrofit.ApiConfigPost

object Injection {
    fun provideRepository(context: Context, token: String? = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOiJ1c2VyLWRJLTgyVENTb0tpbk1uUTYiLCJpYXQiOjE3MDMwOTA5NDR9.MMFJx06LQ_ndzETprqTx-kOMB7cXKojY5WhZmuY8xNM"): PostRepository{
        val database = PostDatabase.getDatabase(context)
        val apiService = ApiConfigPost.getApiServicePost(token)
        return PostRepository(database, apiService)
    }
}