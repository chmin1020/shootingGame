package com.example.ShootingGame.Model

import kotlin.math.abs

class GameModel(displayX: Float, displayY: Float) {
    //display size
    private val xScale = displayX
    private val yScale = displayY

    //specific models
    private val cannonModel = Cannon(xScale , yScale)
    private val enemyModels = arrayOf(Enemy(), Enemy(), Enemy(), Enemy(), Enemy())
    private val bulletModels = arrayOf(Bullet(), Bullet())

    private val bulletInfo = arrayOf(0F, 0F, 0F)
    private val slope = cannonModel.getBaseY()/cannonModel.getBaseX()
    private var life = 2

    private val bulletWidth = 0.08F * xScale
    private val bulletHeight = 0.08F * yScale
    private val enemyWidth = 0.1F * xScale
    private val enemyHeight = 0.1F * yScale

    fun bulletReady(idx: Int){
        bulletModels[idx].toggleAvailable()
    }

    fun bulletSelect() : Array<Float>{
        bulletInfo[1] = cannonModel.getMuzzleX() - 0.04F * xScale
        bulletInfo[2] = cannonModel.getMuzzleY() - 0.04F * yScale

        bulletInfo[0] =
            if (bulletModels[0].isAvailable()) 0F
            else if (bulletModels[1].isAvailable()) 1F
            else -1F

        if(bulletInfo[0] != -1F)
            bulletReady(bulletInfo[0].toInt())
        return bulletInfo
    }

    fun setEnemy() : Int{
        //적절한 enemy 선택
        var idx:Int = -1
        for(i in 0..4){
            if(enemyModels[i].isAvailable()){
                idx = i
                break
            }
        }

        //random 위치와 속도 설정
        if(idx != -1) {
            enemyModels[idx].toggleAvailable()
            enemyModels[idx].randomSetting(
                Math.random().toFloat() * (xScale - enemyWidth),
                (2000L..4000L).random()
            )
        }
        return idx
    }

    fun updateEnemy(idx: Int, pos: Float){
        enemyModels[idx].setY(pos)
    }

    fun enemyInfoX(idx:Int): Float{
        return enemyModels[idx].getX()
    }

    fun enemyInfoD(idx:Int): Long{
        return enemyModels[idx].getDuration()
    }

    fun enemyReady(idx: Int){
        enemyModels[idx].toggleAvailable()
    }

    fun cannonRotate(progress: Int) : Float{
        return cannonModel.rotate(progress)
    }

    fun cannonShot() : Array<Float>{
        val cur = (cannonModel.getBaseY() - cannonModel.getMuzzleY()) / (cannonModel.getBaseX() - cannonModel.getMuzzleX())
        return if(cur > 0){
                if(cur < slope) { //기울기가 낮 (0, 양수)
                    arrayOf(0F, cannonModel.getBaseY() - cur * cannonModel.getBaseX() - bulletHeight/2)
                }
                else // (양수, 0)
                    arrayOf(cannonModel.getBaseX() - (1 / cur) * cannonModel.getBaseY() - bulletWidth/2, 0F)
            }
            else if(cur == 0F){
                if(cannonModel.getDegree() == 90F)
                    arrayOf(0.92F * xScale, cannonModel.getBaseY() + cur * (xScale - cannonModel.getBaseX()) - bulletHeight/2)
                else
                    arrayOf(0F, cannonModel.getBaseY() - cur * cannonModel.getBaseX() - bulletHeight/2)
            }
            else {
                if (cur < -slope) //(양수, 0)
                    arrayOf(cannonModel.getBaseX() + (-1 / cur) * cannonModel.getBaseY() - bulletWidth/2, 0F)
                else //(경계, 경계)
                    arrayOf(0.92F * xScale, cannonModel.getBaseY() + cur * (xScale - cannonModel.getBaseX()) - bulletHeight/2)
            }
    }

    fun collisionCheck(bulletX:Float, bulletY:Float): Int{
        var xDif: Float
        var yDif: Float
        for(i in 0..4){
            if(!enemyModels[i].isAvailable()){
                xDif = abs(bulletX - enemyModels[i].getX())
                yDif = abs(bulletY - enemyModels[i].getY())
                //계산
                if(xDif < enemyWidth && yDif < enemyHeight)
                    return i
            }
        }
        return -1
    }

    fun isGameEnd():Boolean{
        life -= 1
        if(life <= 0)
            return true
        return false
    }

}