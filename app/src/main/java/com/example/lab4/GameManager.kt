package com.example.lab4

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory

class GameManager(private val context: Context) {
    private val defaultOpponentBitmap: Bitmap = BitmapFactory.decodeResource(
        context.resources,
        R.drawable.token_red_emovebg
    )

    fun createOpponent(x: Float, y: Float, speed: Float): Opponent {
        return Opponent(x, y, speed, defaultOpponentBitmap)
    }

    fun createOpponent(x: Float, y: Float, speed: Float, bitmap: Bitmap): Opponent {
        return Opponent(x, y, speed, bitmap)
    }
}