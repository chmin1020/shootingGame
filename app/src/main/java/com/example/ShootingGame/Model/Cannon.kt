package com.example.ShootingGame.Model

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class Cannon(displayX: Float, displayY: Float) {
    private var degree:Float = 0F
    private val cannonLen:Float = displayY * 0.15F
    private val baseX:Float = displayX * 0.5F
    private val baseY:Float = displayY - cannonLen / 2
    private var muzzleX:Float = baseX
    private var muzzleY:Float = displayY - cannonLen


    fun rotate(status: Int) : Float{
        degree = 1.8F * status - 90F
        muzzleX = baseX + (cannonLen/2 * sin(degree * PI/180).toFloat())
        muzzleY = baseY - (cannonLen/2 * cos(degree * PI/180).toFloat())
        return  1.8F * status - 90F
    }

    fun getDegree() : Float{
        return degree
    }

    fun getBaseX(): Float{
        return baseX
    }

    fun getBaseY(): Float{
        return baseY
    }

    fun getMuzzleX(): Float{
        return muzzleX
    }

    fun getMuzzleY(): Float{
        return muzzleY
    }
}