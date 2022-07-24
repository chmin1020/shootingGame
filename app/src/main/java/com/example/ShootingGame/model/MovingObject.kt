package com.example.ShootingGame.model

/**
 * <화면에서 움직이는 객체들(Bullet, Enemy)을 위한 추상 클래스>
 *     어떤 객체라고 하더라도 움직이는 객체는 x,y 좌표를 가지고, 그 위치가 갱신되며 이동함.
 *     따라서 식별자, 현재의 좌표값(curX, curY)과 이들에 접근하기 위한 함수들은 기본으로 가지고 있음.
 *     추가적으로 각 객체마다 위치를 갱신하는 방식이 다를 수 있으므로, 이것의 구현은 자식 클래스에 맡김.
 */
abstract class MovingObject(identification: Long, sx: Float, sy:Float) {
    private var ident = identification
    protected var curX = sx
    protected var curY = sy

    fun getId(): Long{
        return ident
    }

    fun getX(): Float{
        return curX
    }
    fun getY(): Float{
        return curY
    }

    abstract fun positionUpdate()
}