package com.example.trashify.ui.ui_post.add_post

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.trashify.R

class AddPostActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_post)
    }

    companion object{
        private const val REQUIRED_PERMISSION = Manifest.permission.CAMERA
        const val ADD_STORY_KEY = "ADD_STORY_KEY"
    }
}