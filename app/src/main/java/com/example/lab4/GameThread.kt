package com.example.lab4

import android.graphics.Canvas
import android.view.SurfaceHolder

class GameThread(
    private val surfaceHolder: SurfaceHolder,
    private val gameView: GameView
) : Thread() {
    var running: Boolean = false

    private val targetFPS = 60

    override fun run() {
        val frameTime = (1000 / targetFPS).toLong()

        while (running) {
            val startTime = System.nanoTime()
            var canvas: Canvas? = null

            try {
                canvas = surfaceHolder.lockCanvas()
                if (canvas != null) {
                    synchronized(surfaceHolder) {
                        gameView.update()
                        gameView.draw(canvas)
                    }
                }
            } finally {
                if (canvas != null) {
                    surfaceHolder.unlockCanvasAndPost(canvas)
                }
            }

            val timeMillis = (System.nanoTime() - startTime) / 1_000_000
            val waitTime = frameTime - timeMillis

            if (waitTime > 0) {
                try {
                    sleep(waitTime)
                } catch (_: InterruptedException) {
                    interrupt()
                }
            }
        }
    }
}