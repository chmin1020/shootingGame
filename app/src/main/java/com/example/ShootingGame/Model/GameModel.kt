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

    private val slope = cannonModel.getBaseY()/cannonModel.getBaseX()
    private var life = 2

    private val bulletWidth = 0.08F * xScale
    private val bulletHeight = 0.08F * yScale
    private val enemyWidth = 0.1F * xScale
    private val enemyHeight = 0.1F * yScale

    fun dataUpdate(){
        //bullet update
        for(i in 0..1){
            if(!bulletModels[i].isAvailable())
                bulletModels[i].updateXY()
        }
        //enemy update
        for(i in 0..4) {
            if(!enemyModels[i].isAvailable())
                enemyModels[i].setY(enemyModels[i].getY() + enemyModels[i].getVelocity())
        }

        //collision or not
        for(i in 0..1) {
            if (!bulletModels[i].isAvailable()) {
                for (j in 0..4) {
                    if (!enemyModels[j].isAvailable()) {
                        if(collisionCheck(i, j)){
                            bulletModels[i].settingReset()
                            enemyModels[j].settingReset()
                            break
                        }
                    }
                }
            }
        }
    }

    fun cannonRotate(progress: Int) : Float{
        return cannonModel.rotate(progress)
    }

    fun bulletSetTarget(idx: Int){
        val sx = cannonModel.getMuzzleX() - bulletWidth/2
        val sy = cannonModel.getMuzzleY() - bulletHeight/2
        val ex: Float
        val ey: Float
        val cur = (cannonModel.getBaseY() - cannonModel.getMuzzleY()) / (cannonModel.getBaseX() - cannonModel.getMuzzleX())
        if(cur > 0){
            if(cur < slope) { //기울기가 낮 (0, 양수)
                ex = 0F
                ey =cannonModel.getBaseY() - cur * cannonModel.getBaseX() - bulletHeight/2
            }
            else { // (양수, 0)
                ex = cannonModel.getBaseX() - (1 / cur) * cannonModel.getBaseY() - bulletWidth/2
                ey = 0F
            }
        }
        else if(cur == 0F){
            if(cannonModel.getDegree() == 90F) {
                ex = 0.92F * xScale
                ey = cannonModel.getBaseY() + cur * (xScale - cannonModel.getBaseX()) - bulletHeight / 2
            }
            else {
                ex = 0F
                ey = cannonModel.getBaseY() - cur * cannonModel.getBaseX() - bulletHeight / 2
            }
        }
        else {
            if (cur < -slope) { //(양수, 0)
                ex = cannonModel.getBaseX() + (-1 / cur) * cannonModel.getBaseY() - bulletWidth / 2
                ey = 0F
            }
            else { //(경계, 경계)
                ex = 0.92F * xScale
                ey = cannonModel.getBaseY() + cur * (xScale - cannonModel.getBaseX()) - bulletHeight / 2
            }
        }
        bulletModels[idx].setMoveData(sx, sy, ex, ey)
    }

    fun bulletInfoX(idx: Int) : Float{
        return bulletModels[idx].getX()
    }

    fun bulletInfoY(idx: Int) : Float{
        return bulletModels[idx].getY()
    }

    fun bulletInfoCollision(idx: Int): Int{
        return bulletModels[idx].collisionCheck()
    }

    fun bulletSelect() : Int{
        val res =
            if (bulletModels[0].isAvailable()) 0
            else if (bulletModels[1].isAvailable()) 1
            else -1

        if(res != -1)
            bulletModels[res].activate()
        return res
    }

    fun isBulletLimitOut(idx: Int) : Boolean{
        if(bulletModels[idx].overLimit()){
            bulletModels[idx].settingReset()
            return true
        }
        return false
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
            enemyModels[idx].activate()
            enemyModels[idx].randomSetting(
                Math.random().toFloat() * (xScale - enemyWidth),
                Math.random().toFloat() + 0.5F
            )
        }
        return idx
    }

    fun enemyInfoX(idx:Int): Float{
        return enemyModels[idx].getX()
    }

    fun enemyInfoY(idx:Int): Float{
        return enemyModels[idx].getY()
    }

    fun isEnemyLimitOut(idx: Int) : Boolean{
        if(enemyModels[idx].getY() > yScale){
            enemyModels[idx].settingReset()
            return true
        }
        return false
    }

    private fun collisionCheck(bIdx : Int, eIdx : Int): Boolean{
        val xDif: Float = abs(bulletModels[bIdx].getX() - enemyModels[eIdx].getX())
        val yDif: Float = abs(bulletModels[bIdx].getY() - enemyModels[eIdx].getY())
        //계산
        if(xDif < enemyWidth && yDif < enemyHeight){
            bulletModels[bIdx].setCollision(eIdx)
            return true
        }
        return false
    }

    fun isGameEnd():Boolean{
        life -= 1
        if(life <= 0)
            return true
        return false
    }

}