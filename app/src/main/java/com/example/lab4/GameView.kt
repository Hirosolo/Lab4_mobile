package com.example.lab4

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import kotlin.math.max
import kotlin.random.Random

class GameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : SurfaceView(context, attrs), SurfaceHolder.Callback {
    private val surfaceHolder = holder
    private var gameThread = GameThread(surfaceHolder, this)
    private val gameManager = GameManager(context)

    private val backgroundBitmap: Bitmap = BitmapFactory.decodeResource(
        resources,
        R.drawable.galaxy_background
    )
    private var backgroundScaledBitmap: Bitmap = backgroundBitmap

    private val opponentBitmaps: List<Bitmap> = listOf(
        BitmapFactory.decodeResource(resources, R.drawable.rocket),
        BitmapFactory.decodeResource(resources, R.drawable.rocket_2),
        BitmapFactory.decodeResource(resources, R.drawable.alian)
    )

    private val scorePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 64f
    }

    private val gameOverPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.RED
        textSize = 96f
    }

    private val stateLock = Any()
    private val firingObjects = ArrayList<FiringObject>()
    private val opponents = ArrayList<Opponent>()

    private var screenWidth = 0
    private var screenHeight = 0
    private var score = 0
    private var gameOver = false

    private val startingOpponentBaseSpeed = 5f
    private val startingFiringObjectBaseSpeed = 15f
    private val maxSpeed = 12f
    private var opponentBaseSpeed = startingOpponentBaseSpeed
    private var firingObjectBaseSpeed = startingFiringObjectBaseSpeed

    init {
        surfaceHolder.addCallback(this)
        isFocusable = true
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        if (gameThread.isAlive) {
            return
        }
        gameThread = GameThread(surfaceHolder, this)
        gameThread.running = true
        gameThread.start()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        synchronized(stateLock) {
            screenWidth = width
            screenHeight = height
            backgroundScaledBitmap = Bitmap.createScaledBitmap(backgroundBitmap, width, height, true)
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        var retry = true
        gameThread.running = false
        while (retry) {
            try {
                gameThread.join()
                retry = false
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }

    fun update() {
        synchronized(stateLock) {
            if (gameOver || screenHeight <= 0 || screenWidth <= 0) {
                return
            }

            opponentBaseSpeed = max(
                startingOpponentBaseSpeed,
                (startingOpponentBaseSpeed + score * 0.0001f).coerceAtMost(maxSpeed)
            )
            firingObjectBaseSpeed = startingFiringObjectBaseSpeed + score * 0.0005f

            firingObjects.forEach { it.update() }
            firingObjects.removeAll { it.isOffScreen(screenHeight) }

            opponents.forEach { it.update() }
            opponents.removeAll { it.isOffScreen(screenHeight) }

            val firingObjectsCopy = ArrayList(firingObjects)
            val opponentsCopy = ArrayList(opponents)

            collisionLoop@ for (firingObject in firingObjectsCopy) {
                for (opponent in opponentsCopy) {
                    if (RectF.intersects(firingObject.rect, opponent.getRect())) {
                        firingObjects.remove(firingObject)
                        opponents.remove(opponent)
                        score += 10
                        break@collisionLoop
                    }
                }
            }

            for (opponent in opponents) {
                if (opponent.y + opponent.height >= screenHeight - 100f) {
                    gameOver = true
                    break
                }
            }

            if (Random.nextFloat() < 0.02f) {
                val randomBitmap = opponentBitmaps.randomOrNull() ?: return
                val resizedBitmap = Bitmap.createScaledBitmap(randomBitmap, 120, 120, true)
                val spawnX = Random.nextFloat() * (screenWidth - 120).coerceAtLeast(1)
                opponents.add(
                    gameManager.createOpponent(
                        spawnX,
                        -120f,
                        opponentBaseSpeed,
                        resizedBitmap
                    )
                )
            }
        }
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        synchronized(stateLock) {
            if (backgroundScaledBitmap.width > 0 && backgroundScaledBitmap.height > 0) {
                drawBackground(canvas)
            } else {
                canvas.drawColor(Color.BLACK)
            }

            opponents.forEach { it.draw(canvas) }
            firingObjects.forEach { it.draw(canvas) }

            canvas.drawText("Score: $score", 32f, 80f, scorePaint)

            if (gameOver) {
                canvas.drawText(
                    "Game Over",
                    screenWidth / 2f - 220f,
                    screenHeight / 2f,
                    gameOverPaint
                )
            }
        }
    }

    private fun drawBackground(canvas: Canvas) {
        canvas.drawBitmap(backgroundScaledBitmap, 0f, 0f, null)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            synchronized(stateLock) {
                if (gameOver) {
                    resetGame()
                } else if (screenHeight > 0) {
                    firingObjects.add(
                        FiringObject(
                            event.x,
                            screenHeight - 100f,
                            firingObjectBaseSpeed
                        )
                    )
                }
            }
        }
        return true
    }

    private fun resetGame() {
        score = 0
        opponentBaseSpeed = startingOpponentBaseSpeed
        firingObjectBaseSpeed = startingFiringObjectBaseSpeed
        firingObjects.clear()
        opponents.clear()
        gameOver = false
    }
}