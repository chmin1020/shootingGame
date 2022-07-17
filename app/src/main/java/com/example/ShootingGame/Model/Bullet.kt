package com.example.ShootingGame.Model


class Bullet {
    private var available = true
    private var collision = -1
    private var x:Float = 0F
    private var y:Float = 0F
    private var endX:Float = 0F
    private var endY:Float = 0F
    private var velocityX = 0F
    private var velocityY = 0F

    private val duration = 500

    fun setCollision(idx: Int){
        collision = idx
    }

    fun collisionCheck() : Int{
        val ret = collision
        collision = -1
        return ret
    }

    fun isAvailable() : Boolean{
        return available
    }

    fun activate(){
        available = false
    }

    fun settingReset(){
        available = true
        this.x = 0F
        this.y = 0F
        this.velocityX = 0F
        this.velocityY = 0F
    }

    fun setMoveData(startX: Float, startY: Float, endX: Float, endY:Float){
        this.x = startX
        this.y = startY
        this.endX = endX
        this.endY = endY

        velocityX = (endX - x) / duration
        velocityY = (endY - y) / duration
    }

    fun getX() : Float{
        return x
    }
    fun getY() : Float{
        return y
    }
    fun updateXY(){
        x += velocityX
        y += velocityY
    }

    fun overLimit() : Boolean{
        if((velocityX > 0 && x > endX) || (velocityX <= 0 && x < endX) || y < endY)
            return true
        return false
    }
}