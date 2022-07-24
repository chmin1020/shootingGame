package com.example.ShootingGame.model

/**
 * <적 개체를 의미하는 클래스>
 *     기본적으로 화면에서 움직이므로 부모 클래스는 MovingObject.
 *     모든 적의 최초 위치 y값은 0이며, y 방향으로 움직인다.
 *     따라서 y 속도(velY)만을 받아와서 위치 갱신에 활용함.
 */
class Enemy(identification: Long, sx: Float, vy: Float): MovingObject(identification, sx, 0F) {
    private val velY = vy //y 속도

    /* 적은 y 방향으로만 속도를 가지고 위차가 변함 */
    override fun positionUpdate() {
        curY += velY
    }
}