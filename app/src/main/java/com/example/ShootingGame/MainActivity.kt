package com.example.ShootingGame

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.media.Image
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.ShootingGame.Model.GameModel
import java.util.*
import kotlin.concurrent.timer


class MainActivity : AppCompatActivity() {
    lateinit var enemyTimer: Timer
    var realWidth:Float = 0F
    var realHeight:Float = 0F

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        realWidth = resources.displayMetrics.widthPixels.toFloat()
        realHeight = 0.7F * resources.displayMetrics.heightPixels.toFloat()

        //model - controller 연결됨
        val gameModel = GameModel(realWidth, realHeight)
        var selectedBullet: Array<Float>

        //view - controller 연결됨
        val cannon = findViewById<ImageView>(R.id.cannon)
        val rotateBar = findViewById<SeekBar>(R.id.rotate_bar)
        val fireButton = findViewById<ImageButton>(R.id.fire_button)
        val bullets = arrayOf<ImageView>(findViewById(R.id.bullet1), findViewById(R.id.bullet2))
        val enemies = arrayOf<ImageView>(
            findViewById(R.id.enemy1), findViewById(R.id.enemy2), findViewById(R.id.enemy3)
            ,findViewById(R.id.enemy4), findViewById(R.id.enemy5))

        //대포의 회전 (seekbar.progress -> 0 ~ 100일 때 -90F ~ 90F)
        rotateBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                cannon.rotation = gameModel.cannonRotate(rotateBar.progress)
            }
            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })
        //탄환 발사
        fireButton.setOnClickListener{
            selectedBullet = gameModel.bulletSelect()
            val idx = selectedBullet[0].toInt()
            if(idx != -1) {
                bullets[idx].x = selectedBullet[1]
                bullets[idx].y = selectedBullet[2]
                bullets[idx].visibility = View.VISIBLE

                val arr = gameModel.cannonShot()

                //데이터 계산을 view animation 표현
                bullets[idx].animate().translationX(arr[0]).translationY(arr[1])
                    .setDuration(800L)
                    .setListener(object : AnimatorListenerAdapter(){
                        override fun onAnimationEnd(animation: Animator?) {
                            super.onAnimationEnd(animation)
                            bullets[idx].x = 0F
                            bullets[idx].y = 0F
                            bullets[idx].visibility = View.GONE
                            gameModel.bulletReady(idx)
                         }
                    })
                    .setUpdateListener {
                        val check = gameModel.collisionCheck(bullets[idx].x, bullets[idx].y)
                        if(check != -1) {
                            bullets[idx].animate().cancel()
                            enemies[check].animate().cancel()
                        }
                    }
            }
        }
        //적 등장 담당 타이머 설정
        enemyTimer = timer(period = 1000, initialDelay = 1000L){
            val em = gameModel.setEnemy()
            //뷰와 setEnemy 값 연결
            if(em != -1){
                runOnUiThread {
                    enemies[em].x = gameModel.enemyInfoX(em)
                    enemies[em].y = 0F
                    enemies[em].visibility = View.VISIBLE
                    //enemy의 애니메이션
                    enemies[em].animate().translationY(realHeight)
                        .setDuration(gameModel.enemyInfoD(em))
                        .setListener(object : AnimatorListenerAdapter(){
                            override fun onAnimationEnd(animation: Animator?) {
                                if(enemies[em].y == realHeight){
                                    if(gameModel.isGameEnd()){
                                        Toast.makeText(this@MainActivity, "게임 종료!", Toast.LENGTH_SHORT).show()
                                        enemyTimer.cancel()
                                    }
                                }
                                enemies[em].x = 0F
                                enemies[em].y = 0F
                                enemies[em].visibility = View.GONE
                                gameModel.enemyReady(em)
                            }
                        })
                    enemies[em].animate().setUpdateListener {
                        gameModel.updateEnemy(em, enemies[em].y)
                    }
                }
            }
        }
    }
}

