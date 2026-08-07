package com.example.dodger

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.MotionEvent
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class GameView(context: Context) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val prefs = context.getSharedPreferences("dodger_prefs", Context.MODE_PRIVATE)
    private val obstacles = mutableListOf<RectF>()
    private val player = RectF()

    private var playerSize = 0f
    private var targetX = 0f
    private var score = 0
    private var bestScore = 0
    private var gameOver = false
    private var lastFrameTime = 0L
    private var spawnTimer = 0f

    private var insetTop = 0f
    private var insetBottom = 0f
    private var insetLeft = 0f
    private var insetRight = 0f

    init {
        isFocusable = true
        bestScore = prefs.getInt("best_score", 0)
        ViewCompat.setOnApplyWindowInsetsListener(this) { _, insets ->
            val systemInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            insetTop = systemInsets.top.toFloat()
            insetBottom = systemInsets.bottom.toFloat()
            insetLeft = systemInsets.left.toFloat()
            insetRight = systemInsets.right.toFloat()
            insets
        }
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        playerSize = width * 0.16f
        targetX = width / 2f
        resetGame()
    }

    override fun onDraw(canvas: Canvas) {
        val now = System.nanoTime()
        val dt = if (lastFrameTime == 0L) 0f else (now - lastFrameTime) / 1_000_000_000f
        lastFrameTime = now

        update(dt)
        drawGame(canvas)
        postInvalidateOnAnimation()
    }

    private fun update(dt: Float) {
        if (width == 0 || height == 0 || gameOver) return

        val center = player.centerX()
        val nextCenter = center + (targetX - center) * min(1f, dt * 12f)
        val half = playerSize / 2f
        val left = (nextCenter - half).coerceIn(0f, width - playerSize)
        player.set(left, height - playerSize * 1.8f, left + playerSize, height - playerSize * 0.8f)

        spawnTimer -= dt
        if (spawnTimer <= 0f) {
            spawnObstacle()
            spawnTimer = max(0.28f, 0.85f - score * 0.012f)
        }

        val speed = height * (0.34f + score * 0.006f)
        val iterator = obstacles.iterator()
        while (iterator.hasNext()) {
            val obstacle = iterator.next()
            obstacle.offset(0f, speed * dt)

            when {
                RectF.intersects(player, obstacle) -> {
                    gameOver = true
                    if (score > bestScore) {
                        bestScore = score
                        prefs.edit().putInt("best_score", bestScore).apply()
                    }
                }
                obstacle.top > height -> {
                    iterator.remove()
                    score++
                }
            }
        }
    }

    private fun drawGame(canvas: Canvas) {
        canvas.drawColor(Color.rgb(18, 23, 33))

        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(255, 92, 92)
        obstacles.forEach { canvas.drawRoundRect(it, 18f, 18f, paint) }

        paint.color = Color.rgb(45, 212, 191)
        canvas.drawRoundRect(player, 22f, 22f, paint)

        paint.color = Color.rgb(245, 247, 250)
        paint.textSize = 48f
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText(context.getString(R.string.score_label, score), 36f + insetLeft, 72f + insetTop, paint)

        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText(context.getString(R.string.best_score_label, bestScore), width - 36f - insetRight, 72f + insetTop, paint)

        if (gameOver) {
            paint.textAlign = Paint.Align.CENTER
            paint.textSize = 68f
            canvas.drawText(context.getString(R.string.game_over_title), width / 2f, height * 0.42f, paint)
            paint.textSize = 38f
            canvas.drawText(context.getString(R.string.restart_hint), width / 2f, height * 0.50f, paint)
        }
    }

    private fun spawnObstacle() {
        val obstacleWidth = width * (0.18f + Random.nextFloat() * 0.16f)
        val obstacleHeight = playerSize * (0.55f + Random.nextFloat() * 0.45f)
        val left = Random.nextFloat() * max(1f, width - obstacleWidth)
        obstacles += RectF(left, -obstacleHeight, left + obstacleWidth, 0f)
    }

    private fun resetGame() {
        obstacles.clear()
        score = 0
        gameOver = false
        spawnTimer = 0.45f
        lastFrameTime = 0L

        val half = playerSize / 2f
        player.set(targetX - half, height - playerSize * 1.8f, targetX + half, height - playerSize * 0.8f)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN && gameOver) {
            targetX = event.x
            resetGame()
            return true
        }

        if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) {
            targetX = event.x
        }

        return true
    }
}
