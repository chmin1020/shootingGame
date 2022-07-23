package com.example.ShootingGame.model

/**
 * <탄환 개체를 의미하는 클래스>
 *     기본적으로 화면에서 움직이므로 부모 클래스는 MovingObject.
 *     탄환은 대포의 위치벡터에 따라 시작 위치와 속도가 다르다.
 *     따라서 속도벡터로 2가지를 받으며, 이를 이용해서 위치 갱신을 한다.
 */
class Bullet(sx: Float, sy: Float, vx: Float, vy: Float): MovingObject(sx, sy) {
    private val velocityConst = 80
    private val velX = vx * velocityConst
    private val velY = vy * velocityConst

    override fun locationUpdate() {
        curX += velX
        curY += velY
    }
}