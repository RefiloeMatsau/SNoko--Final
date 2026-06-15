package com.snokonoko.app.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View
import android.view.ViewGroup
import kotlin.random.Random

class ConfettiView(context: Context) : View(context) {

    private data class Particle(
        val color: Int,
        val size: Float,
        val speedY: Float,
        val speedX: Float,
        val rotSpeed: Float,
        var x: Float,
        var y: Float,
        var angle: Float = 0f
    )

    private val palette = intArrayOf(
        Color.parseColor("#F49AC2"),
        Color.parseColor("#FF9F0A"),
        Color.parseColor("#30D158"),
        Color.parseColor("#5AC8FA"),
        Color.parseColor("#BF5AF2"),
        Color.parseColor("#FF453A"),
        Color.parseColor("#FFD60A")
    )
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val particles = mutableListOf<Particle>()
    private var animator: ValueAnimator? = null

    fun burst(parent: ViewGroup) {
        isClickable = false
        parent.addView(this, ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        ))
        post {
            val w = parent.width.toFloat()
            repeat(100) {
                particles += Particle(
                    color  = palette[Random.nextInt(palette.size)],
                    size   = Random.nextFloat() * 14f + 5f,
                    speedY = Random.nextFloat() * 7f + 4f,
                    speedX = (Random.nextFloat() - 0.5f) * 5f,
                    rotSpeed = (Random.nextFloat() - 0.5f) * 10f,
                    x = Random.nextFloat() * w,
                    y = -Random.nextFloat() * 400f
                )
            }
            animator = ValueAnimator.ofFloat(0f, 1f).apply {
                duration = 3500L
                addUpdateListener {
                    particles.forEach { p ->
                        p.x += p.speedX
                        p.y += p.speedY
                        p.angle += p.rotSpeed
                    }
                    invalidate()
                }
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        (this@ConfettiView.parent as? ViewGroup)?.removeView(this@ConfettiView)
                    }
                })
                start()
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        val h = height.toFloat()
        particles.forEach { p ->
            if (p.y < h + p.size) {
                paint.color = p.color
                canvas.save()
                canvas.rotate(p.angle, p.x, p.y)
                val half = p.size / 2f
                canvas.drawRect(p.x - half, p.y - half, p.x + half, p.y + half, paint)
                canvas.restore()
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator?.cancel()
    }
}
