package com.example.shootinggame.Model

import com.example.test.Model.Bullet
import com.example.test.Model.Cannon
import com.example.test.Model.Enemy

class GameModel(displayX: Float, displayY: Float) {
    //display size
    private val xScale = displayX
    private val yScale = displayY

    //specific models
    private val cannonModel = Cannon(xScale , yScale)
    val enemyModel = Enemy()
    val bulletModel = Bullet()

    fun cannonRotate(progress: Int) : Float{
        return cannonModel.rotate(progress)
    }

    fun getMX() : Float{
        return cannonModel.muzzleX
    }

    fun getMY() : Float{
        return cannonModel.muzzleY
    }

    fun cannonShot(){

    }


}
