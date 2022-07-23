package com.example.ShootingGame.model

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * <대포 개체를 의미하는 클래스>
 *     대포는 움직이지 않으나, 회전을 하며 탄환의 초기화에 큰 영향을 준다.
 *     따라서 회전에 관련한 값(각도, 벡터)와 그에 관련된 함수만을 가진다.
 */
class Cannon {
    //삼각함수로 벡터를 구하기 위해서 사용되는 상수값
    private val radianCal = PI / 180

    //변하는 변수값 (각도, 벡터)
    private var degree: Float = 0F
    private var xVector: Float = 0F
    private var yVector: Float = -1F

    /* 대포 회전 시 변경되는 각도를 적용하고, 그에 따른 포구의 위치 데이터를 변경하는 함수
       탄환 발사 시 변경 된 muzzle 위치 값이 목표 지정에 사용됨 */
    fun rotate(status: Int): Float {
        degree = 1.8F * status - 90F
        val radian = degree * radianCal
        xVector = sin(radian).toFloat()
        yVector = -cos(radian).toFloat()
        return degree
    }

    /* x 방향 벡터 값을 얻는 함수 */
    fun getVectorX(): Float {
        return xVector
    }

    /* y 방향 벡터 값을 얻는 함수 */
    fun getVectorY(): Float {
        return yVector
    }
}