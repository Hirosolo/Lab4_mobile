package com.example.lab4

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF

open class Opponent(
    var x: Float,
    var y: Float,
    var speed: Float,
    private val bitmap: Bitmap,
    private val maxHealth: Int = 1,
    private val laneTargetX: Float? = null,
    private val laneSpeedX: Float = 0f
) {
    val width: Float = bitmap.width.toFloat()
    val height: Float = bitmap.height.toFloat()
    protected val hitBox: RectF = RectF(x, y, x + width, y + height)
    private val healthBarPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }
    private val healthFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GREEN
    }
    private val healthBackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(180, 0, 0, 0)
    }

    var health: Int = maxHealth
        private set

    val isDead: Boolean
        get() = health <= 0

    open fun update() {
        laneTargetX?.let { targetX ->
            val distance = targetX - x
            if (kotlin.math.abs(distance) > laneSpeedX) {
                x += laneSpeedX * kotlin.math.sign(distance)
            } else {
                x = targetX
            }
        }
        y += speed
        hitBox.set(x, y, x + width, y + height)
    }

    open fun draw(canvas: Canvas) {
        canvas.drawBitmap(bitmap, null, hitBox, null)
        drawHealthBar(canvas)
    }

    fun takeDamage(damage: Int = 1) {
        health = (health - damage).coerceAtLeast(0)
    }

    fun isOffScreen(screenHeight: Int): Boolean {
        return y > screenHeight
    }

    fun getRect(): RectF {
        return hitBox
    }

    private fun drawHealthBar(canvas: Canvas) {
        if (maxHealth <= 1) {
            return
        }

        val barHeight = 10f
        val barBottom = y - 12f
        val barTop = barBottom - barHeight
        val fillRatio = health.toFloat() / maxHealth.toFloat()
        val barWidth = width

        canvas.drawRect(x, barTop, x + barWidth, barBottom, healthBackPaint)
        canvas.drawRect(x, barTop, x + barWidth * fillRatio, barBottom, healthFillPaint)
        canvas.drawRect(x, barTop, x + barWidth, barBottom, healthBarPaint)
    }
}