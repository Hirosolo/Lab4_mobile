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
    companion object {
        private const val GAME_PREFS = "lab4_game_state"
        private const val HIGH_SCORE_KEY = "high_score"
        private const val MAX_LIVES = 3
    }

    private val surfaceHolder = holder
    private var gameThread = GameThread(surfaceHolder, this)
    private val gameManager = GameManager(context)
    private val gamePreferences = context.getSharedPreferences(GAME_PREFS, Context.MODE_PRIVATE)

    private val backgroundBitmap: Bitmap = BitmapFactory.decodeResource(
        resources,
        R.drawable.galaxy_background
    )
    private var backgroundScaledBitmap: Bitmap = backgroundBitmap

    private val playerShipBitmap: Bitmap = BitmapFactory.decodeResource(
        resources,
        R.drawable.rocket_2
    )
    private var playerShipScaledBitmap: Bitmap = Bitmap.createScaledBitmap(
        playerShipBitmap,
        140,
        140,
        true
    )
    private val playerShipRect = RectF()

    private val opponentBitmaps: List<Bitmap> = listOf(
        BitmapFactory.decodeResource(resources, R.drawable.rocket),
        BitmapFactory.decodeResource(resources, R.drawable.rocket_2),
        BitmapFactory.decodeResource(resources, R.drawable.alian)
    )

    private val scorePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 64f
    }

    private val statusPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 44f
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
    private var playerShipX = 0f
    private var playerShipY = 0f
    private var score = 0
    private var highScore = gamePreferences.getInt(HIGH_SCORE_KEY, 0)
    private var lives = MAX_LIVES
    private var gameOver = false

    private val startingOpponentBaseSpeed = 5f
    private val startingFiringObjectBaseSpeed = 15f
    private val maxSpeed = 12f
    private var opponentBaseSpeed = startingOpponentBaseSpeed
    private var firingObjectBaseSpeed = startingFiringObjectBaseSpeed
    private val laneCount = 4

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
            playerShipScaledBitmap = Bitmap.createScaledBitmap(playerShipBitmap, 140, 140, true)
            setPlayerShipPosition(width / 2f, height - 170f)
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
            firingObjects.removeAll { it.isOffScreen(screenWidth, screenHeight) }

            opponents.forEach { it.update() }
            val escapedOpponents = opponents.filter { it.isOffScreen(screenHeight) }
            if (escapedOpponents.isNotEmpty()) {
                lives -= escapedOpponents.size
                opponents.removeAll(escapedOpponents.toSet())
                if (lives <= 0) {
                    gameOver = true
                    lives = 0
                }
            }

            val firingObjectsCopy = ArrayList(firingObjects)
            val opponentsCopy = ArrayList(opponents)

            collisionLoop@ for (firingObject in firingObjectsCopy) {
                for (opponent in opponentsCopy) {
                    if (RectF.intersects(firingObject.rect, opponent.getRect())) {
                        firingObjects.remove(firingObject)
                        opponent.takeDamage()
                        if (opponent.isDead) {
                            opponents.remove(opponent)
                            score += 10
                            if (score > highScore) {
                                highScore = score
                                gamePreferences.edit().putInt(HIGH_SCORE_KEY, highScore).apply()
                            }
                        }
                        break@collisionLoop
                    }
                }
            }

            if (Random.nextFloat() < 0.02f) {
                val randomBitmap = opponentBitmaps.randomOrNull() ?: return
                val resizedBitmap = Bitmap.createScaledBitmap(randomBitmap, 120, 120, true)
                val laneWidth = (screenWidth - 120).coerceAtLeast(1) / laneCount.toFloat()
                val laneIndex = Random.nextInt(laneCount)
                val laneTargetX = (laneIndex * laneWidth) + laneWidth / 2f
                val spawnX = Random.nextFloat() * (screenWidth - 120).coerceAtLeast(1)
                opponents.add(
                    gameManager.createOpponent(
                        spawnX,
                        -120f,
                        opponentBaseSpeed,
                        resizedBitmap,
                        Random.nextInt(1, 4),
                        laneTargetX,
                        2.5f
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

            drawPlayerShip(canvas)
            opponents.forEach { it.draw(canvas) }
            firingObjects.forEach { it.draw(canvas) }

            canvas.drawText("Score: $score", 32f, 80f, scorePaint)
            canvas.drawText("High Score: $highScore", 32f, 140f, statusPaint)
            canvas.drawText("Lives: $lives", 32f, 190f, statusPaint)

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
                    setPlayerShipPosition(event.x, event.y)
                    spawnBulletPattern()
                }
            }
        }
        return true
    }

    private fun spawnBulletPattern() {
        val shipCenterX = playerShipRect.centerX()
        val bulletOriginY = playerShipRect.top
        val bulletLevel = when (score) {
            in 0 until 100 -> 1
            in 100 until 250 -> 2
            in 250 until 500 -> 3
            else -> 4
        }

        val spreadOffsets = when (bulletLevel) {
            1 -> listOf(0f)
            2 -> listOf(-18f, 18f)
            3 -> listOf(-30f, 0f, 30f)
            else -> listOf(-42f, -18f, 18f, 42f)
        }

        spreadOffsets.forEachIndexed { index, offset ->
            val velocityX = when (bulletLevel) {
                1 -> 0f
                2 -> if (index == 0) -1.8f else 1.8f
                3 -> when (index) {
                    0 -> -2.4f
                    1 -> 0f
                    else -> 2.4f
                }
                else -> when (index) {
                    0 -> -3.0f
                    1 -> -1.2f
                    2 -> 1.2f
                    else -> 3.0f
                }
            }

            firingObjects.add(
                FiringObject(
                    shipCenterX + offset,
                    bulletOriginY,
                    firingObjectBaseSpeed,
                    velocityX
                )
            )
        }
    }

    private fun resetGame() {
        score = 0
        opponentBaseSpeed = startingOpponentBaseSpeed
        firingObjectBaseSpeed = startingFiringObjectBaseSpeed
        lives = MAX_LIVES
        firingObjects.clear()
        opponents.clear()
        gameOver = false
        setPlayerShipPosition(screenWidth / 2f, screenHeight - 170f)
    }

    private fun setPlayerShipPosition(centerX: Float, centerY: Float) {
        val shipWidth = playerShipScaledBitmap.width.toFloat()
        val shipHeight = playerShipScaledBitmap.height.toFloat()
        val left = (centerX - shipWidth / 2f).coerceIn(0f, (screenWidth - shipWidth).coerceAtLeast(0f))
        val top = (centerY - shipHeight / 2f).coerceIn(0f, (screenHeight - shipHeight).coerceAtLeast(0f))
        playerShipX = left
        playerShipY = top
        playerShipRect.set(left, top, left + shipWidth, top + shipHeight)
    }

    private fun drawPlayerShip(canvas: Canvas) {
        canvas.drawBitmap(playerShipScaledBitmap, null, playerShipRect, null)
    }
}