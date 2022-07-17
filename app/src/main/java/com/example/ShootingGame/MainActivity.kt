package com.example.ShootingGame

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.ShootingGame.Model.GameModel
import java.util.*
import kotlin.concurrent.timer


class MainActivity : AppCompatActivity() {
    //뷰와 모델을 1ms마다 업데이트하기 위한 타이머
    private lateinit var updateTimer: Timer

    //실행 게임 화면 크기 저장을 위함
    private var realWidth:Float = 0F
    private var realHeight:Float = 0F

    //뷰와 연결된 객체
    private lateinit var cannon:ImageView
    private lateinit var rotateBar:SeekBar
    private lateinit var fireButton: ImageButton
    private lateinit var bullets: Array<ImageView>
    private lateinit var enemies: Array<ImageView>

    private val NODATA = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //게임 동작 구현을 위한 실제 화면 크기 지정
        realWidth = resources.displayMetrics.widthPixels.toFloat()
        realHeight = 0.7F * resources.displayMetrics.heightPixels.toFloat()

        //model - controller 연결됨
        val gameModel = GameModel(realWidth, realHeight)

        //view - controller 연결됨
        cannon = findViewById(R.id.cannon)
        rotateBar = findViewById(R.id.rotate_bar)
        fireButton = findViewById(R.id.fire_button)
        bullets = arrayOf(findViewById(R.id.bullet1), findViewById(R.id.bullet2))
        enemies = arrayOf(
            findViewById(R.id.enemy1), findViewById(R.id.enemy2), findViewById(R.id.enemy3)
            ,findViewById(R.id.enemy4), findViewById(R.id.enemy5))

        //타이머 설정 (1ms마다 데이터 변경)
        var oneCheck = 0
        updateTimer = timer(period = 1){
            gameModel.dataUpdate() //뷰에 적용할 데이터를 모델에서 업데이트

            oneCheck += 1 //time check
            //enemy 새로 생성하는 코드(enemy는 1초마다 하나씩 생성)
            if(oneCheck == 1000) {
                oneCheck = 0 //1초 카운터 초기화
                val em = gameModel.setEnemy() //새로운 enemy 데이터 모델에서 가져오기
                //가져온 enemy 데이터 뷰에 적용
                if (em != NODATA) {
                    runOnUiThread {
                        enemyUpdate(em, gameModel.enemyInfoX(em), gameModel.enemyInfoY(em), true)
                    }
                }
            }

            //모델 데이터를 뷰에 적용
            runOnUiThread {
                //bullet 위치 적용
                for(i in 0..1){
                    if(gameModel.isBulletLimitOut(i))
                        bulletUpdate(i, 0F, 0F, false)
                    else
                        bulletUpdate(i,gameModel.bulletInfoX(i), gameModel.bulletInfoY(i), true)
                }

                //enemy 위치 적용
                for(i in 0..4) {
                    //life 2번 다 까이면 게임 종료
                    if(gameModel.isEnemyLimitOut(i)){
                        if(gameModel.isGameEnd()){
                            Toast.makeText(this@MainActivity, "게임 종료", Toast.LENGTH_SHORT).show()
                            updateTimer.cancel()
                        }
                        enemyUpdate(i, 0F, 0F, false)
                    }
                    else
                        enemies[i].y = gameModel.enemyInfoY(i)
                }

                //충돌한 bullet, enemy 삭제
                for(i in 0..1){
                    val idx = gameModel.bulletInfoCollision(i)
                    if(idx != NODATA){
                        enemyUpdate(idx, 0F, 0F, false)
                        bulletUpdate(i, 0F, 0F, false)
                    }
                }
            }
        }

        /*대포 회전(seekbar)과 탄환 발사(imageButton)의 listener 지정*/
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
            if(idx != NODATA) { //-1인 경우 현재 사용 가능한 탄환 x
                gameModel.bulletSetTarget(idx) //탄환의 시작과 종료 위치 설정
                bulletUpdate(idx, gameModel.bulletInfoX(idx), gameModel.bulletInfoY(idx), true)
            }
        }
    }

    private fun bulletUpdate(idx: Int, x:Float, y:Float, visible:Boolean){
        bullets[idx].x = x
        bullets[idx].y = y
        bullets[idx].visibility = if(visible) (View.VISIBLE) else (View.GONE)
    }
    private fun enemyUpdate(idx: Int, x:Float, y:Float, visible:Boolean){
        enemies[idx].x = x
        enemies[idx].y = y
        enemies[idx].visibility = if(visible) (View.VISIBLE) else (View.GONE)
    }
}