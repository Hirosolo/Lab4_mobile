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
    private val bossBitmap: Bitmap = Bitmap.createScaledBitmap(
        BitmapFactory.decodeResource(context.resources, R.drawable.alian),
        220,
        220,
        true
    )
    private val bossMinionBitmap: Bitmap = Bitmap.createScaledBitmap(
        BitmapFactory.decodeResource(context.resources, R.drawable.alian),
        96,
        96,
        true
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

    fun createBoss(x: Float, y: Float): BossOpponent {
        return BossOpponent(x, y, bossBitmap, 30)
    }

    fun createBossMinion(x: Float, y: Float): Opponent {
        return Opponent(x, y, 6f, bossMinionBitmap, 1)
    }
}