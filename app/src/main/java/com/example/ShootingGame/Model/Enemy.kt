package com.example.ShootingGame.Model

class Enemy {
    private var available = true
    private var duration:Long = 0
    private var x:Float = 0F
    private var y:Float = 0F

    fun randomSetting(x:Float, d:Long){
        this.x = x
        duration = d
    }

    fun isAvailable() : Boolean{
        return available
    }
    fun toggleAvailable(){
        available = !available
    }
    fun getDuration():Long{
        return duration
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