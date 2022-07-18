package com.example.ShootingGame

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.ShootingGame.model.GameModel
import java.util.*
import kotlin.concurrent.timer

/*
       액티비티는 모델과 뷰의 연결고리 역할
       뷰를 통해 입력을 받고 그에 따른 데이터 갱신을 모델에 요청
       반대로 모델에서 가져온 데이터를 뷰를 통해 사용에게 제공할 수 있음
        1. 대포 회전 각도
        2. 탄환 발사
        3. 탄환과 적의 움직임
        4. 탄환과 적의 충돌
        5. 게임 종료
 */
class MainActivity : AppCompatActivity() {
    //모델의 데이터와 뷰의 위치를 주기적으로 갱신하기 위한 타이머
    //timerForStartPeriodicUpdate() 함수에서 정의됨
    private lateinit var updateTimer: Timer

    //실행 게임 화면 크기 저장을 위함
    private var realWidth:Float = 0F
    private var realHeight:Float = 0F

    //모델과 연결되는 객체
    private lateinit var gameModel: GameModel

    //뷰와 연결되는 객체 (함수와 연결하기 위해 Activity 자체 변수로 선언)
    private lateinit var cannon:ImageView
    private lateinit var rotateBar:SeekBar
    private lateinit var fireButton: ImageButton
    private lateinit var bullets: Array<ImageView>
    private lateinit var enemies: Array<ImageView>

    private val invalidIndex = -1 //충돌 관련 동작에서 사용할 '해당 데이터 없음'의 표지
    private var isGameEnd = false //게임 종료 체크 변수

    ///////////////////////////////Activity의 생명 주기 함수//////////////////////////////////////////
    /* onCreate()에서는 화면 크기, MVC 연결 관련 변수들, 뷰로 입력을 받기 위한 listener 초기화 */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //게임 동작 구현을 위한 실제 화면 크기 지정
        realWidth = resources.displayMetrics.widthPixels.toFloat()
        realHeight = 0.7F * resources.displayMetrics.heightPixels.toFloat()

        //model - controller 연결됨
        gameModel = GameModel(realWidth, realHeight)

        //view - controller 연결됨
        cannon = findViewById(R.id.cannon)
        rotateBar = findViewById(R.id.rotate_bar)
        fireButton = findViewById(R.id.fire_button)
        bullets = arrayOf(findViewById(R.id.bullet1), findViewById(R.id.bullet2))
        enemies = arrayOf(
            findViewById(R.id.enemy1), findViewById(R.id.enemy2), findViewById(R.id.enemy3)
            ,findViewById(R.id.enemy4), findViewById(R.id.enemy5), findViewById(R.id.enemy6))

        //사용자가 뷰를 통해 입력하는 명령에 따른 모델 변경
        //대포 회전(seekbar)과 탄환 발사(imageButton)의 listener 지정

        //대포의 회전 (seekbar.progress -> 0 ~ 100일 때 -90F ~ 90F)
        rotateBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                cannon.rotation = gameModel.cannonRotate(rotateBar.progress)
            }
            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })
        //탄환 발사
        fireButton.setOnClickListener {
            gameModel.setBullet()
        }
    }

    /* onResume()에서는 게임이 종료되지 않은 한, 주기적 갱신을 위한 타이머를 설정하고 뷰를 사용가능하게 바꿈 */
    override fun onResume() {
        super.onResume()
        if(!isGameEnd) { //게임이 종료되지 않았다면...
            //모델과 뷰 자동갱신을 위한 타이머 재실행
            timerStartForPeriodicUpdate()
            //뷰(회전, 발사 컨트롤러) 터치 활성화
            rotateBar.isEnabled = true
            fireButton.isEnabled = true
        }
    }

    /* onPause()에서는 게임을 잠깐 정지(타이머 취소 및 뷰를 사용 불가능하게) */
    override fun onPause() {
        super.onPause()
        gameStop() //게임 중지
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////

    /////////////////////////////////주기적 갱신을 위한 타이머//////////////////////////////////////////
    /*
                            <타이머 업데이트 (40ms 단위로 반복)>
        ((Controller -> Model)) : gameModel.periodicModelUpdate
        1. 모델에게 게임 내 활성화된(움직이고 있는) 적과 탄환의 위치 데이터와 적 등장을 갱신할 것을 요청
        ((Controller -> View)) : totalUIUpdate(화면 접근이라 UI Thread 내부에서 동작)
        2. UI 내부에 갱신된 탄환 위치, 적 위치, 충돌 적용
     */
    private fun timerStartForPeriodicUpdate(){
        updateTimer = timer(period = gameModel.updatePeriod){
            gameModel.periodicModelUpdate() //1
            runOnUiThread {
                totalUIUpdate() //2
            }
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////

    //////////////////////////////////UI 적용 프로세스 함수////////////////////////////////////////////
    /*
        ui update 위해서 할 것
        1. bullet 위치 갱신
        2. enemy 위치 갱신
        3. 충돌한 뷰 제거
     */
    private fun totalUIUpdate(){
        bulletUIUpdateProcess() //1
        enemyUIUpdateProcess() //2
        collisionUIUpdateProcess() //3
    }

    /* (bullet 위치 갱신)을 위한 함수 */
    private fun bulletUIUpdateProcess(){
        for(i in 0..gameModel.finalBulletIdx){
            if(gameModel.isBulletActivating(i)) { //해당 bullet이 활성 상태라면...
                if (gameModel.isBulletLimitOut(i)) //화면 밖으로 넘어가면 화면에서 제거
                    bulletViewUpdate(i, 0F, 0F, false)
                else //안 나가면 좌표값(x, y 속성) 갱신
                    bulletViewUpdate(i,
                        gameModel.getBulletX(i), gameModel.getBulletY(i),
                        true)
            }
        }
    }
    /* (enemy 위치 갱신)을 위한 함수 */
    private fun enemyUIUpdateProcess(){
        for(i in 0..gameModel.finalEnemyIdx) {
            //life 2번 다 까이면 게임 종료
            if (gameModel.isEnemyActivating(i)) { //해당 enemy가 활성 상태라면...
                if (gameModel.isEnemyLimitOut(i)) { //화면 밖으로 넘어가면 화면에서 제거
                    //그 와중에 life를 다 쓴 상황이라면 --> 게임 종료
                    if (gameModel.isGameEnd())
                        gameEnd()
                    enemyViewUpdate(i, 0F, 0F, false)
                } else { //안 나가면 좌표값(x, y 속성) 갱신
                    if (enemies[i].visibility == View.GONE) //처음 나오는 enemy면 전부 갱신
                        enemyViewUpdate(
                            i,
                            gameModel.getEnemyX(i), gameModel.getEnemyY(i),
                            true
                        )
                    enemies[i].y = gameModel.getEnemyY(i) //아니면 y만 갱신
                }
            }
        }
    }
    /* (충돌한 뷰 제거)를 위한 함수 */
    private fun collisionUIUpdateProcess(){
        for(i in 0..gameModel.finalBulletIdx) {
            val idx = gameModel.collidedEnemyWithBullet(i)
            if(idx != invalidIndex) {
                bulletViewUpdate(i, 0F, 0F, false)
                enemyViewUpdate(idx, 0F, 0F, false)
            }
        }
    }

    /* idx를 색인으로 가진 bullet 뷰의 속성 값을 업데이트 */
    private fun bulletViewUpdate(idx: Int, x:Float, y:Float, visible:Boolean){
        bullets[idx].x = x
        bullets[idx].y = y
        bullets[idx].visibility = if(visible) (View.VISIBLE) else (View.GONE)
    }
    /* idx를 색인으로 가진 enemy 뷰의 속성 값을 업데이트 */
    private fun enemyViewUpdate(idx: Int, x:Float, y:Float, visible:Boolean){
        enemies[idx].x = x
        enemies[idx].y = y
        enemies[idx].visibility = if(visible) (View.VISIBLE) else (View.GONE)
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    ///////////////////////////////게임 종료와 연관됨 함수들///////////////////////////////////////////
    /*onPause, onStop 상황이나, life가 0이 됐을 때 게임이 중지됨*/
    private fun gameStop(){
        updateTimer.cancel()
        rotateBar.isEnabled = false
        fireButton.isEnabled = false
    }

    /*life가 0이 된 경우라면 gameStop()에 더하여 isGameEnd를 true로 하고 Toast 메시지 출력 */
    private fun gameEnd(){
        Toast.makeText(applicationContext, "게임 종료", Toast.LENGTH_SHORT).show()
        isGameEnd = true //게임 완전 종료 선언
        gameStop()
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////
}