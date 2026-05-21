package com.example.lab4

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlin.random.Random

class GameManager(private val context: Context) {
    private val defaultOpponentBitmap: Bitmap = BitmapFactory.decodeResource(
        context.resources,
        R.drawable.token_red_emovebg
    )

    fun createOpponent(x: Float, y: Float, speed: Float): Opponent {
        return Opponent(x, y, speed, defaultOpponentBitmap, Random.nextInt(1, 4))
    }

    fun createOpponent(x: Float, y: Float, speed: Float, bitmap: Bitmap): Opponent {
        return Opponent(x, y, speed, bitmap, Random.nextInt(1, 4))
    }

    fun createOpponent(x: Float, y: Float, speed: Float, bitmap: Bitmap, health: Int): Opponent {
        return Opponent(x, y, speed, bitmap, health)
    }

    fun createOpponent(
        x: Float,
        y: Float,
        speed: Float,
        bitmap: Bitmap,
        health: Int,
        laneTargetX: Float,
        laneSpeedX: Float
    ): Opponent {
        return Opponent(x, y, speed, bitmap, health, laneTargetX, laneSpeedX)
    }
}