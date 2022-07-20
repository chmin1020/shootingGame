package com.example.ShootingGame.model

class Life {
    //최초 life 개수 (상수)
    private val firstVal = 2

    //현재 life 개수 (변수)
    private var curVal = firstVal

    /* life 개수를 얻어올 때 사용하는 함수 */
    fun getLife() : Int{
        return curVal
    }

    /*life를 차감할 때 사용하는 함수 */
    fun lifeDecrease(){
        curVal--
    }
}