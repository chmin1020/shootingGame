package com.example.ShootingGame.model

class Enemy {
    private var available = true
    private var velocity:Float = 0F
    private var x:Float = 0F
    private var y:Float = 0F

    /* 적의 위치 값, (x, y)좌표 값을 반환하는 함수들 */
    fun getX():Float{
        return x
    }
    fun getY():Float{
        return y
    }

    /* activity에서 주기적으로 타이머를 실행할 때 활성화된 적의 위치를 갱신하는 함수 */
    fun updateLocation(){
        y += velocity
    }

    /* 이 객체가 지금 사용가능한지, 즉 게임에서 비활성화 상태인지 알려주는 함수 */
    fun isAvailable() : Boolean{
        return available
    }

    /* 이 객체를 활성화시키는 함수, available -> true */
    fun activate(){
        available = false
    }

    /* 이 적이 선택됐을 때 랜덤으로 정해진 위치와 속도를 받아오는 함수 */
    fun newSetting(x:Float, v:Float){
        this.x = x
        velocity = v
    }

    /* 변수를 모두 리셋하는 함수, 충돌하거나 화면 밖으로 나갈 때 실행 */
    fun settingReset(){
        available = true
        this.x = 0F
        this.y = 0F
        this.velocity = 0F
    }
}