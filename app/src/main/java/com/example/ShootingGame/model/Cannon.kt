package com.example.ShootingGame.model

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class Cannon(displayX: Float, displayY: Float) {
    //변하지 않는 변수값 (대포 길이, 대포의 중심점(회전 시 기준이 됨))
    private val cannonLen:Float = displayY * 0.15F
    private val baseX:Float = displayX * 0.5F
    private val baseY:Float = displayY - cannonLen / 2
    //변하는 변수값 (각도, 포구의 위치(x, y))
    private var degree:Float = 0F
    private var muzzleX:Float = baseX
    private var muzzleY:Float = displayY - cannonLen

    /* 대포 회전 시 변경되는 각도를 적용하고, 그에 따른 포구의 위치 데이터를 변경하는 함수
       탄환 발사 시 변경 된 muzzle 위치 값이 목표 지정에 사용됨 */
    fun rotate(status: Int) : Float{
        degree = 1.8F * status - 90F
        val radian = degree * PI/180
        muzzleX = baseX + (cannonLen/2 * sin(radian).toFloat())
        muzzleY = baseY - (cannonLen/2 * cos(radian).toFloat())
        return degree
    }

    /* 대포의 회전 중심점 위치 값(x,y 좌표) 획득
       이 값은 화면 생성 시 항상 고정되며, 이 위치를 축으로 대포가 회전함 */
    fun getBaseX(): Float{
        return baseX
    }
    fun getBaseY(): Float{
        return baseY
    }

    /* 대포의 포구 위치(x,y 좌표) 획득
       회전에 따라 달라지는 포구 위치는 탄환 발사의 시작점이 된다. */
    fun getMuzzleX(): Float{
        return muzzleX
    }
    fun getMuzzleY(): Float{
        return muzzleY
    }
}