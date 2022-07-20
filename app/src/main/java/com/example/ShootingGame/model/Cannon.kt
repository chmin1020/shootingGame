package com.example.ShootingGame.model


import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class Cannon {
    private val radianCal = PI/180

    //변하는 변수값 (각도, 포구의 위치(x, y))
    private var degree:Float = 0F
    private var xVector:Float = 0F
    private var yVector:Float = -1F

    /* 대포 회전 시 변경되는 각도를 적용하고, 그에 따른 포구의 위치 데이터를 변경하는 함수
       탄환 발사 시 변경 된 muzzle 위치 값이 목표 지정에 사용됨 */
    fun rotate(status: Int): Float{
        degree = 1.8F * status - 90F
        val radian = degree * radianCal
        xVector = sin(radian).toFloat()
        yVector = -cos(radian).toFloat()
        return degree
    }

    fun getVectorX(): Float{
        return xVector
    }

    fun getVectorY(): Float{
        return yVector
    }
}