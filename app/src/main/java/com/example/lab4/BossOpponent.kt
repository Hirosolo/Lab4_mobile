package com.example.lab4

import android.graphics.Bitmap
import android.graphics.Canvas

class BossOpponent(
    x: Float,
    y: Float,
    bitmap: Bitmap,
    maxHealth: Int = 20,
    private val minionSpawnBitmap: Bitmap
) : Opponent(x, y, 0f, bitmap, maxHealth) {
    private var direction = 1f
    private var spawnTimer = 0
    private val moveSpeed = 4f
    private val spawnInterval = 45

    fun updateBoss(screenWidth: Int): List<Pair<Float, Float>> {
        x += direction * moveSpeed

        if (x <= 0f) {
            x = 0f
            direction = 1f
        } else if (x + width >= screenWidth.toFloat()) {
            x = screenWidth.toFloat() - width
            direction = -1f
        }

        hitBox.set(x, y, x + width, y + height)

        spawnTimer++
        if (spawnTimer < spawnInterval) {
            return emptyList()
        }

        spawnTimer = 0
        val centerX = x + width / 2f
        val centerY = y + height / 2f
        return listOf(
            Pair(centerX - 50f, centerY),
            Pair(centerX + 50f, centerY)
        )
    }

    override fun update() {
        // Boss movement is handled by updateBoss(screenWidth).
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
    }

    fun createSpawnedMinion(x: Float, y: Float): Opponent {
        return Opponent(x, y, 6f, minionSpawnBitmap, 1)
    }
}