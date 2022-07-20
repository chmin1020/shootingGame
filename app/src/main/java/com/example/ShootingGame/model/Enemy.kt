package com.example.ShootingGame.model

class Enemy {
    //적의 최대 개수 (10)
    private val cntLimit = 10
    private var currentCnt = 0

    //현재 적의 위치와 속도(x,y, velocityX, velocityY)
    var posAndVelocity = ArrayList<MovingObjectInfo>()

    fun getEnemyCnt(): Int{
        return currentCnt
    }

    fun newEnemy(startX: Float, velY: Float){
        if(currentCnt == cntLimit)
            return
        currentCnt += 1

        posAndVelocity.add(MovingObjectInfo(startX, 0F, 0F, velY))
    }

    fun removeEnemy(idx: Int){
        currentCnt -= 1
        posAndVelocity.removeAt(idx)
    }

    /* activity에서 주기적으로 타이머를 실행할 때 활성화된 탄환의 위치를 갱신하는 함수 */
    fun updateLocation(){
        for(i in 0 until currentCnt)
            posAndVelocity[i].posUpdate()
    }
}