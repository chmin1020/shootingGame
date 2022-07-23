package com.example.ShootingGame.controller

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.ShootingGame.databinding.ActivityMainBinding
import com.example.ShootingGame.model.GameModel
import com.example.ShootingGame.model.MovingObject
import com.example.ShootingGame.model.ObjectInfo
import java.util.*
import kotlin.concurrent.timer

/**
 *     액티비티는 모델과 뷰의 연결고리 역할
 *     모델에서 갱신된 데이터를 가져와 뷰에 적용한다.
 *
 *      ((각 뷰의 적절한 listener 통해 모델 요청 및 뷰 적용, onCreate()에서 설정))
 *      <사용자 입력에 따른 데이터 갱신 및 적용>
 *          1. 대포 회전 각도
 *          2. 탄환 발사
 *
 *      ((timer 통해 40ms 단위로 모델 요청 및 뷰 적용, onResume()에서 설정))
 *      <주기적인 데이터 갱신 및 적용>
 *          1. 화면에 있는 탄환과 적의 움직임
 *          2. 게임 종료
 */
class MainActivity : AppCompatActivity() {
    //MovingObject(Bullet, Enemy)를 표시할 뷰를 관리하는 리스트
    private val bulletViews = mutableListOf<ImageView>()
    private val enemyViews = mutableListOf<ImageView>()
   
    //모델의 데이터와 뷰의 위치를 주기적으로 갱신하기 위한 타이머
    private lateinit var updateTimer: Timer

    //모델과 연결되는 객체
    private lateinit var gameModel: GameModel

    //뷰와 연결되는 객체 (뷰 바인딩)
    private lateinit var inView: ActivityMainBinding

    //게임 종료 체크 변수
    private var gameEnd = false


    //------------------------------------------------
    //생명 주기 함수
    //

    /* onCreate()에서는 화면 크기, MVC 연결 관련 변수들, 뷰로 입력을 받기 위한 listener 초기화 */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //vlew - controller 연결됨
        inView = ActivityMainBinding.inflate(layoutInflater)
        setContentView(inView.root)

        //model - controller 연결됨 (game stage 화면 크기 제공)
        gameModel = GameModel(resources.displayMetrics.widthPixels,
            (0.7F * resources.displayMetrics.heightPixels).toInt())

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
            val bullet = gameModel.shootBullet()
            if(bullet != null)
                bulletViews.add(makeNewMovingView(bullet, gameModel.bulletInfo))
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


    //------------------------------------------------
    //주기적 갱신을 위한 타이머

    /**
     *                       <타이머 업데이트 (40ms 단위로 반복)>
     *   ((Controller -> Model)) : gameModel.periodicModelUpdate
     *      1. 모델에게 게임 내 활성화된(움직이고 있는) 적과 탄환의 위치 데이터와 적 등장을 갱신할 것을 요청
     *   ((Controller -> View)) : totalUIUpdate(화면 접근이라 UI Thread 내부에서 동작)
     *      2. UI 내부에 갱신된 탄환 위치, 적 위치, 충돌 적용
     */
    private fun timerStartForPeriodicUpdate(){
        updateTimer = timer(period = gameModel.updatePeriod) {
            gameModel.totalMovingUpdate() //1

            runOnUiThread {
                val enemy = gameModel.newEnemy()
                if(enemy != null)
                    enemyViews.add(makeNewMovingView(enemy, gameModel.enemyInfo))

                totalUIPeriodicUpdate() //2
            }
        }
    }

    //------------------------------------------------
    //함수 영역 (UI 적용 프로세스 함수)
    //

    /**
     *   <ui update 위해서 할 것>
     *      0. 기존에 game stage 내에 존재하던 모든 view 제거
     *      1. 게임 종료 여부 확인
     *      2. bullet 새로 생성
     *      3. enemy 새로 생성
     */
    private fun totalUIPeriodicUpdate(){
        gameEndCheckProcess() //1
        movingViewsDelete(gameModel.getBulletDeleteInfo(), bulletViews)
        movingViewsDelete(gameModel.getEnemyDeleteInfo(), enemyViews)
        movingViewsPosUpdate(gameModel.getBullets(), bulletViews) //2
        movingViewsPosUpdate(gameModel.getEnemies(), enemyViews) //3
    }

    /* (게임 종료 여부 확인)을 위한 함수 */
    private fun gameEndCheckProcess(){
        //life 개수를 알리는 텍스트 내용 설정 (Life : ?)
        val curLife = gameModel.getLife()
        val lifeText = "Life : $curLife"
        inView.lifeCount.text = lifeText

        //life 0 이하로 떨어지면 게임 완전 종료
        if(curLife <= 0)
            gameEnd()
    }

    private fun movingViewsDelete(list: List<Int>, viewList: MutableList<ImageView>){
        val it = list.iterator()
        var num: Int

        while(it.hasNext()){
            num = it.next()
            inView.gameStage.removeView(viewList[num])
            viewList.removeAt(num)
        }
    }

    /* (뷰 새로 생성)을 위한 함수 */
    private fun movingViewsPosUpdate(list: List<MovingObject>, viewList: MutableList<ImageView>){
        val it = list.iterator()
        val viewIt = viewList.iterator()

        var curObj: MovingObject
        var curView: ImageView

        //데이터 개수만큼 업데이트
        while(it.hasNext()){
            curObj = it.next()
            curView = viewIt.next()

            curView.x = curObj.getX()
            curView.y = curObj.getY()
        }
    }

    /* movingViesUpdateProcess 내부에서 실제 뷰를 추가할 때 사용하는 함수 */
    private fun makeNewMovingView(obj: MovingObject, info: ObjectInfo): ImageView{
        //새로운 뷰의 속성 지정 (무슨 info 받았냐에 따라 크기와 소스 이미지 달라짐
        val newView = ImageView(this)
        newView.setImageResource(info.resId)
        newView.layoutParams = FrameLayout.LayoutParams(info.width, info.height)
        newView.x = obj.getX()
        newView.y = obj.getY()
        newView.scaleType = ImageView.ScaleType.FIT_XY

        inView.gameStage.addView(newView) //속성 적용이 끝났으면 화면에 추가
        return newView
    }

    //------------------------------------------------
    //함수 영역 (게임 종료와 연관됨)
    //

    /* onPause, onStop 상황이나, life -> 0이 됐을 때 게임이 중지됨 */
    private fun gameStop(){
        updateTimer.cancel() //타이머 동작 취소
        //사용자를 위한 컨트롤러 비활성화
        inView.rotateBar.isEnabled = false
        inView.fireButton.isEnabled = false
    }

    /* life -> 0이 된 경우라면 gameStop()에 더하여 isGameEnd -> true 세팅을 하고 Toast 메시지 출력 */
    private fun gameEnd(){
        Toast.makeText(applicationContext, "게임 종료", Toast.LENGTH_SHORT).show()
        gameEnd = true //게임 완전 종료 선언
        gameStop()
    }
}