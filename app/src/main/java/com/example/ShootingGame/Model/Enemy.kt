package com.example.ShootingGame.Model

class Enemy {
    private var available = true
    private var velocity:Float = 0F
    private var x:Float = 0F
    private var y:Float = 0F

    fun randomSetting(x:Float, v:Float){
        this.x = x
        velocity = v
    }

    fun settingReset(){
        available = true
        this.x = 0F
        this.y = 0F
        this.velocity = 0F
    }

    fun isAvailable() : Boolean{
        return available
    }
    fun activate(){
        available = false
    }
    fun getVelocity():Float{
        return velocity
    }

    fun getX():Float{
        return x
    }
    fun getY():Float{
        return y
    }
    fun setY(num:Float){
        y = num
    }
}