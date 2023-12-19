package com.example.trashify.ui.ui_post

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.trashify.databinding.ActivityPostBinding
import com.example.trashify.ui.ui_post.add_post.AddPostActivity

class PostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPostBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.fabAddPost.setOnClickListener{
            val intentDetail = Intent(this, AddPostActivity::class.java)
            startActivity(intentDetail)
        }
    }
}