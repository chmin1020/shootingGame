package com.example.ShootingGame.Model

class Bullet {
    private var available = true
    private var startX: Float = 0F
    private var startY: Float = 0F
    private var endX:Float = 0F
    private var endY:Float = 0F

    fun isAvailable() : Boolean{
        return available
    }

    fun toggleAvailable(){
        available = !available
    }

    fun setMoveData(startX: Float, startY: Float, endX: Float, endY:Float){
        this.startX = startX
        this.startY = startY
        this.endX = endX
        this.endY = endY
    }
}