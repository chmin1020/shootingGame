package com.example.ShootingGame.controller

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.ShootingGame.R
import com.example.ShootingGame.databinding.ActivityMainBinding
import com.example.ShootingGame.model.GameModel
import java.util.*
import kotlin.concurrent.timer

/*
       액티비티는 모델과 뷰의 연결고리 역할
       뷰를 통해 입력을 받고 그에 따른 데이터 갱신을 모델에 요청
       반대로 모델에서 가져온 데이터를 뷰를 통해 사용에게 제공할 수 있음
        ((각 뷰의 적절한 listener 통해 모델 요청 및 뷰 적용, onCreate()에서 설정))
        <사용자 입력에 따른 데이터 갱신 및 적용>
        1. 대포 회전 각도
        2. 탄환 발사
        ((timer 통해 40ms 단위로 모델 요청 및 뷰 적용, onResume()에서 설정))
        <주기적인 데이터 갱신 및 적용>
        1. 탄환과 적의 움직임
        2. 탄환과 적의 충돌
        3. 게임 종료
 */
class MainActivity : AppCompatActivity() {
    //모델의 데이터와 뷰의 위치를 주기적으로 갱신하기 위한 타이머
    //timerForStartPeriodicUpdate() 함수에서 정의됨
    private lateinit var updateTimer: Timer

    //모델과 연결되는 객체
    private lateinit var gameModel: GameModel

    //뷰와 연결되는 객체 (함수와 연결하기 위해 Activity 자체 변수로 선언)
    private lateinit var inView: ActivityMainBinding

    private val invalidIndex = -1 //충돌 관련 동작에서 사용할 '해당 데이터 없음'의 표지
    private var gameEnd = false //게임 종료 체크 변수

    ///////////////////////////////Activity의 생명 주기 함수//////////////////////////////////////////
    /* onCreate()에서는 화면 크기, MVC 연결 관련 변수들, 뷰로 입력을 받기 위한 listener 초기화 */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        inView = ActivityMainBinding.inflate(layoutInflater)
        setContentView(inView.root)

        //model - controller 연결됨 (game stage 화면 크기 제공)
        gameModel = GameModel(resources.displayMetrics.widthPixels.toFloat(),
            0.7F * resources.displayMetrics.heightPixels.toFloat())

        //대포 회전(seekbar)과 탄환 발사(imageButton)의 listener 지정
        //대포의 회전 (seekbar.progress -> 0 ~ 100일 때 -90F ~ 90F)
        inView.rotateBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                inView.cannon.rotation = gameModel.cannonRotate(inView.rotateBar.progress)
            }
            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })
        //탄환 발사
        inView.fireButton.setOnClickListener {
            gameModel.shootBullet()
        }
    }

    /* onResume()에서는 게임이 종료되지 않은 한, 주기적 갱신을 위한 타이머를 설정하고 뷰를 사용가능하게 바꿈 */
    override fun onResume() {
        super.onResume()
        if(!gameEnd) { //게임이 종료되지 않았다면...
            //모델과 뷰 자동갱신을 위한 타이머 재실행
            timerStartForPeriodicUpdate()
            //뷰(회전, 발사 컨트롤러) 터치 활성화
            inView.rotateBar.isEnabled = true
            inView.fireButton.isEnabled = true
        }
    }

    /* onPause()에서는 게임을 잠깐 정지(타이머 취소 및 뷰를 사용 불가능하게) */
    override fun onPause() {
        super.onPause()
        gameStop() //게임 중지
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////

    /////////////////////////////////주기적 갱신을 위한 타이머//////////////////////////////////////////
    /**
     *                       <타이머 업데이트 (40ms 단위로 반복)>
     *   ((Controller -> Model)) : gameModel.periodicModelUpdate
     *   1. 모델에게 게임 내 활성화된(움직이고 있는) 적과 탄환의 위치 데이터와 적 등장을 갱신할 것을 요청
     *   ((Controller -> View)) : totalUIUpdate(화면 접근이라 UI Thread 내부에서 동작)
     *   2. UI 내부에 갱신된 탄환 위치, 적 위치, 충돌 적용
     */
    private fun timerStartForPeriodicUpdate(){
        updateTimer = timer(period = gameModel.updatePeriod){
            gameModel.totalModelPeriodicUpdate() //1
            runOnUiThread {
                totalUIPeriodicUpdate() //2
            }
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////

    //////////////////////////////////UI 적용 프로세스 함수////////////////////////////////////////////
    /*
        ui update 위해서 할 것
        1. 게임 종료 여부 확인
        2. bullet 위치 갱신
        3. enemy 위치 갱신
     */
    private fun totalUIPeriodicUpdate(){
        //기존 뷰를 전부 제거
        inView.gameStage.removeAllViews()
        //모델 내부에서 충돌한 두 객체는 알아서 제거해야함
        gameEndCheckProcess() //1
        bulletUIUpdateProcess() //2
        enemyUIUpdateProcess() //3
    }

    /* (게임 종료 여부 확인)을 위한 함수 */
    private fun gameEndCheckProcess(){
        //life 개수를 알리는 텍스트 내용 설정 (Life : ?)
        val lifeText = "Life : " + gameModel.getLife().toString()
        inView.lifeCount.text = lifeText
        //life가 0 이하로 떨어지면 게임 완전 종료
        if(gameModel.getLife() <= 0)
            gameEnd()
    }


    /* (bullet 위치 갱신)을 위한 함수 */
    private fun bulletUIUpdateProcess(){
        val arr = gameModel.getBulletInfo()

        for(i in 0 until gameModel.curBulletCnt)
            inView.gameStage.addView(makeBullet(arr[i].x, arr[i].y))
    }

    /* (enemy 위치 갱신)을 위한 함수 */
    private fun enemyUIUpdateProcess(){
        val arr = gameModel.getEnemyInfo()

        for(i in 0 until gameModel.curEnemyCnt)
            inView.gameStage.addView(makeEnemy(arr[i].x, arr[i].y))
    }

    private fun makeBullet(xPos: Float, yPos: Float): ImageView {
        val bullet = ImageView(this)
        bullet.setImageResource(R.drawable.circle)
        bullet.layoutParams = FrameLayout.LayoutParams(gameModel.bulletWidth.toInt(), gameModel.bulletHeight.toInt())
        bullet.x = xPos
        bullet.y = yPos
        bullet.scaleType = ImageView.ScaleType.FIT_XY
        return bullet
    }

    private fun makeEnemy(xPos: Float, yPos: Float): ImageView {
        val enemy = ImageView(this)
        enemy.setImageResource(R.drawable.enemy)
        enemy.layoutParams = FrameLayout.LayoutParams(gameModel.bulletWidth.toInt(), gameModel.bulletHeight.toInt())
        enemy.x = xPos
        enemy.y = yPos
        enemy.scaleType = ImageView.ScaleType.FIT_XY
        return enemy
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    ///////////////////////////////게임 종료와 연관된 함수들///////////////////////////////////////////
    /*onPause, onStop 상황이나, life가 0이 됐을 때 게임이 중지됨*/
    private fun gameStop(){
        updateTimer.cancel() //타이머 동작 취소
        //사용자를 위한 컨트롤러 비활성화
        inView.rotateBar.isEnabled = false
        inView.fireButton.isEnabled = false
    }

    /*life가 0이 된 경우라면 gameStop()에 더하여 isGameEnd를 true로 하고 Toast 메시지 출력 */
    private fun gameEnd(){
        Toast.makeText(applicationContext, "게임 종료", Toast.LENGTH_SHORT).show()
        gameEnd = true //게임 완전 종료 선언
        gameStop()
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////
}