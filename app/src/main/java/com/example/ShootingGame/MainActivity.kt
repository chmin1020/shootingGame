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
    lateinit var updateTimer: Timer

    //실행 게임 화면 크기 저장을 위함
    private var realWidth:Float = 0F
    private var realHeight:Float = 0F

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        realWidth = resources.displayMetrics.widthPixels.toFloat()
        realHeight = 0.7F * resources.displayMetrics.heightPixels.toFloat()

        //model - controller 연결됨
        val gameModel = GameModel(realWidth, realHeight)

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
            val idx = gameModel.bulletSelect() //사용할 탄환 색인을 모델에서 가져옴
            if(idx != -1) { //-1인 경우 현재 사용 가능한 탄환 x
                gameModel.bulletSetTarget(idx) //탄환의 시작과 종료 위치 설정
                bullets[idx].x = gameModel.bulletInfoX(idx)
                bullets[idx].y = gameModel.bulletInfoY(idx)
                bullets[idx].visibility = View.VISIBLE
            }
        }

        //타이머 설정 (1ms마다 데이터 변경)
        var oneCheck = 0
        updateTimer = timer(period = 1){
            gameModel.dataUpdate() //뷰에 적용할 데이터를 모델에서 업데이트

            oneCheck += 1 //time check

            //enemy는 1초마다 하나씩 생성
            if(oneCheck == 1000) {
                oneCheck = 0
                val em = gameModel.setEnemy() //새로운 enemy 데이터 모델에서 가져오기
                //가져온 enemy 데이터 뷰에 적용
                if (em != -1) {
                    runOnUiThread {
                        enemies[em].x = gameModel.enemyInfoX(em)
                        enemies[em].y = gameModel.enemyInfoY(em)
                        enemies[em].visibility = View.VISIBLE
                    }
                }
                Log.d("enemies em", "$em")
            }

            //모델 데이터를 뷰에 적용
            runOnUiThread {
                //bullet 위치 적용
                for(i in 0..1){
                    if(gameModel.isBulletLimitOut(i)){
                        bullets[i].x = 0F
                        bullets[i].y = 0F
                        bullets[i].visibility = View.GONE
                    }
                    else{
                        bullets[i].x = gameModel.bulletInfoX(i)
                        bullets[i].y = gameModel.bulletInfoY(i)
                    }
                }

                //enemy 위치 적용
                for(i in 0..4) {
                    if(gameModel.isEnemyLimitOut(i)){
                        /*if(gameModel.isGameEnd()){
                            Toast.makeText(this@MainActivity, "게임 종료", Toast.LENGTH_SHORT).show()
                            enemyTimer.cancel()
                        }*/
                        enemies[i].x = 0F
                        enemies[i].y = 0F
                        enemies[i].visibility = View.GONE
                    }
                    else
                        enemies[i].y = gameModel.enemyInfoY(i)
                }

                //충돌한 bullet, enemy 삭제
                for(i in 0..1){
                    val idx = gameModel.bulletInfoCollision(i)
                    if(idx != -1){
                        enemies[idx].x = 0F
                        enemies[idx].y = 0F
                        enemies[idx].visibility = View.GONE

                        bullets[i].x = 0F
                        bullets[i].y = 0F
                        bullets[i].visibility = View.GONE
                    }
                }

            }

        }
    }
}


