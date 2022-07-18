package com.example.ShootingGame.model

class Bullet {
    //현재 탄환의 사용가능/불가능(비활성/활성) 여부
    private var available = true
    //현재 탄환이 충돌한 Enemy의 색인 (충돌하지 않으면 -1)
    private var collisionWith = -1
    //현재 탄환의 위치(x,y)
    private var x:Float = 0F
    private var y:Float = 0F
    //현재 탄환의 목표 위치(x,y)
    private var endX:Float = 0F
    private var endY:Float = 0F
    //목표를 향해 나아가는 탄환의 x,y 방향 속도
    private var velocityX = 0F
    private var velocityY = 0F
    //탄환이 화면을 벗어나는 시간
    private val duration = 500

    /* 이 탄환의 위치 값, (x, y)좌표 값을 반환하는 함수들 */
    fun getX() : Float{
        return x
    }
    fun getY() : Float{
        return y
    }

    /* activity에서 주기적으로 타이머를 실행할 때 활성화된 탄환의 위치를 갱신하는 함수 */
    fun updateLocation(){
        x += velocityX
        y += velocityY
    }

    /* 이 객체가 지금 사용가능한지, 즉 게임에서 비활성화 상태인지 알려주는 함수 */
    fun isAvailable() : Boolean{
        return available
    }

    /* 이 객체를 활성화시키는 함수, available -> true */
    fun activate(){
        available = false
    }

    /* 이 탄환이 선택되고 목표가 설정되면 그 위치 값과 그에 따른 속도를 설정하는 함수 */
    fun newSetting(startX: Float, startY: Float, endX: Float, endY:Float){
        this.x = startX
        this.y = startY
        this.endX = endX
        this.endY = endY

        velocityX = (endX - x) / duration * 40
        velocityY = (endY - y) / duration * 40
    }

    /* 변수를 모두 리셋하는 함수, 충돌하거나 화면 밖으로 나갈 때 실행 */
    fun settingReset(){
        available = true
        this.x = 0F
        this.y = 0F
        this.velocityX = 0F
        this.velocityY = 0F
    }

    /* 이 코드에서는 bullet 기준으로 충돌 데이터를 기록함
       따라서 이 함수를 통해서 해당 Bullet 객체가 어떤 색인을 가진 Enemy와 충돌했는지 저장 */
    fun setCollision(idx: Int){
        collisionWith = idx
    }

    /* GameModel에서 collidedEnemyWithBullet(idx)를 실행했을 때 내부에서 실행됨
       어떤 Enemy와 충돌했는지 색인을 반환하면서 혼란 방지를 위해 collisionWith 초기화 */
    fun collisionCheck() : Int{
        val ret = collisionWith
        collisionWith = -1
        return ret
    }

    /* 이 탄환의 현재 위치가 게임 화면 범위를 넘어섰는지 여부를 알려주는 함수.
        Bullet은 활성화될 때마다 목표가 달라지므로 Enemy와 달리 limit를 넘겼는지 여부를
        각 객체의 이 함수를 통해 확인한다. */
    fun overLimit() : Boolean{
        if((velocityX > 0 && x > endX) || (velocityX <= 0 && x < endX) || y < endY)
            return true
        return false
    }
}