package com.example.ShootingGame.model

class Bullet {
    //탄환의 최대 개수 (3)
    private val cntLimit = 3
    private var currentCnt = 0

    //현재 탄환의 위치와 속도(x,y, velocityX, velocityY)
    var posAndVelocity = ArrayList<MovingObjectInfo>()

    fun getBulletCnt(): Int{
        return currentCnt
    }

    fun newShooting(startX: Float, startY: Float, velX: Float, velY: Float){
        if(currentCnt == cntLimit)
            return
        currentCnt += 1

        posAndVelocity.add(MovingObjectInfo(startX, startY, velX * 40, velY * 40))
    }

    fun removeBullet(idx: Int){
        currentCnt -= 1
        posAndVelocity.removeAt(idx)
    }

    /* activity에서 주기적으로 타이머를 실행할 때 활성화된 탄환의 위치를 갱신하는 함수 */
    fun updateLocation(){
        for(i in 0 until currentCnt)
            posAndVelocity[i].posUpdate()
    }
}