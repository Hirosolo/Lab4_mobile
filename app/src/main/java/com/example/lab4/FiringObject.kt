package com.example.lab4

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF

class FiringObject(
    var x: Float,
    var y: Float,
    var speed: Float,
    private val velocityX: Float = 0f
) {
    private val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    val width: Float = 20f
    val height: Float = 40f
    val rect: RectF = RectF(x, y, x + width, y + height)

    init {
        paint.color = Color.YELLOW
    }

    fun update() {
        x += velocityX
        y -= speed
        rect.set(x, y, x + width, y + height)
    }

    fun draw(canvas: Canvas) {
        canvas.drawRect(rect, paint)
    }

    fun isOffScreen(screenHeight: Int): Boolean {
        return y + height < 0
    }
}