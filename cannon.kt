package com.example.test.Model

import kotlin.math.cos
import kotlin.math.sin

class Cannon(displayX: Float, displayY: Float) {
    private var degree:Float = 0F
    private val baseX:Float = displayX
    private val baseY:Float = displayY * 0.5F
    var muzzleX:Float = baseX * 0.85F
    var muzzleY:Float = baseY
    private val cannonLen:Float = baseX * 0.15F



    fun rotate(status: Int) : Float{
        degree = 1.8F * status - 90F
        muzzleX = baseX - (cannonLen * cos(degree))
        muzzleY = baseY + (cannonLen * sin(degree))
        return  1.8F * status - 90F
    }

    fun shot(){

    }
}
