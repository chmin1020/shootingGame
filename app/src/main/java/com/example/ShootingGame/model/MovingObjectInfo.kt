package com.example.ShootingGame.model

class MovingObjectInfo(px: Float, py: Float, vx: Float, vy: Float) {
    var x:Float = px
    var y:Float = py
    var velX:Float = vx
    var velY:Float = vy

    fun posUpdate(){
        x += velX
        y += velY
    }
}