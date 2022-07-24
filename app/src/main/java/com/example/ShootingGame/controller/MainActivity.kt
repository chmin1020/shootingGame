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
 *      ((timer 통해 80ms 단위로 모델 요청 및 뷰 적용, onResume()에서 설정))
 *      <주기적인 데이터 갱신 및 적용>
 *          1. 화면에 있는 탄환과 적 이동 및 제거
 *          2. 게임 종료
 */
class MainActivity : AppCompatActivity() {
    //MovingObject 대응 뷰들을 관리하는 테이블
    private val bulletTable = mutableMapOf<Long,ImageView>()
    private val enemyTable = mutableMapOf<Long,ImageView>()

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
        //view - controller 연결됨 (view binding)
        inView = ActivityMainBinding.inflate(layoutInflater)
        setContentView(inView.root)

        //model - controller 연결됨 (game stage 화면 크기 제공)
        gameModel = GameModel(resources.displayMetrics.widthPixels,
            (0.7F * resources.displayMetrics.heightPixels).toInt())

        //대포의 회전 (seekbar.progress -> 0 ~ 100일 때 -90F ~ 90F)
        inView.rotateBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                inView.cannon.rotation = gameModel.cannonRotate(inView.rotateBar.progress)
            }
            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })

        //탄환 발사, 새 탄환 객체가 발생하면 바로 뷰 테이블에 새 이미지뷰와 함께 기록
        inView.fireButton.setOnClickListener {
            val bullet = gameModel.shootBullet()
            if(bullet != null) //bullet is null -> 생성된 bullet 없음
                bulletTable[bullet.getId()] = makeNewMovingView(bullet, gameModel.bulletInfo)
        }
    }

    /* onResume()에서는 게임이 종료되지 않은 한, 주기적 갱신을 위한 타이머를 설정하고 뷰를 사용가능하게 바꿈 */
    override fun onResume() {
        super.onResume()
        if(!gameEnd) {
            //타이머 재실행과 컨트롤러 활성화
            timerStartForPeriodicUpdate()
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
     *      ((뷰와 관련 x))
     *          1. 모델에게 게임 화면 내에 이미 있는 적과 탄환의 데이터를 갱신할 것을 요청
     *      ((뷰와 관련 o))
     *          2. 화면에서 벗어난 적 객체로 인해 목숨이 0이 되었다면 게임 종료 트리거 발동
     *          3. 새로 등장할 적 개체가 있다면 모델에게 그 데이터를 받아서 뷰와 연결
     *          4. 화면에서 벗어나거나 충돌해서 화면 내에서 사라져야 하는 뷰 삭제
     *          5. UI 내부에 갱신된 탄환 위치, 적 위치 적용
     */
    private fun timerStartForPeriodicUpdate(){
        updateTimer = timer(period = gameModel.updatePeriod) {
            gameModel.totalModelExistingDataUpdate() //1
            runOnUiThread {
                gameEndCheckUpdate() //4
                newEnemyUpdate() //2
                totalUIDeletingUpdate() //3
                totalUIMovingUpdate() //5
            }
        }
    }

    //------------------------------------------------
    //함수 영역 (UI 적용 프로세스 함수)
    //

    /* 화면에서 벗어난 적 객체로 인해 목숨이 0이 되었다면 게임 종료 트리거 발동하는 함수 */
    private fun gameEndCheckUpdate() {
        //life 개수를 알리는 텍스트 내용 설정 (Life : ?)
        val curLife = gameModel.getLife()
        val lifeText = "Life : $curLife"
        inView.lifeCount.text = lifeText

        //life 0 이하로 떨어지면 게임 완전 종료
        if (curLife <= 0)
            gameEnd()
    }

    /* 새로 등장할 적 개체가 있다면 모델에게 그 데이터를 받아서 뷰와 연결하는 함수 */
    private fun newEnemyUpdate(){
        val enemy = gameModel.newEnemy()
        if(enemy != null) //enemy is null -> 생성된 enemy 없음
            enemyTable[enemy.getId()] = makeNewMovingView(enemy, gameModel.enemyInfo)
    }

    /* 화면에서 벗어나거나 충돌해서 화면 내에서 사라져야 하는 뷰를 삭제하는 함수 */
    private fun totalUIDeletingUpdate(){
        eachViewDeletingUpdate(gameModel.getDeletedBulletIds(), bulletTable)
        eachViewDeletingUpdate(gameModel.getDeletedEnemyIds(), enemyTable)
    }

    //deleted id에 해당하는 움직이는 뷰들을 삭제하는 함수
    private fun eachViewDeletingUpdate(idList: List<Long>, viewTable: MutableMap<Long,ImageView>){
        val idIt = idList.iterator()
        var curId:Long

        //id에 해당하는 뷰를 테이블에서 삭제
        while(idIt.hasNext()){
            curId = idIt.next()
            inView.gameStage.removeView(viewTable[curId])
            viewTable.remove(curId)
        }
    }

    /* UI 내부에 갱신된 탄환 위치, 적 위치 적용하는 함수 */
    private fun totalUIMovingUpdate(){
        eachViewMovingUpdate(gameModel.getBullets(), bulletTable)
        eachViewMovingUpdate(gameModel.getEnemies(), enemyTable)
    }

    //실제 각 MovingObject(Bullet, Enemy)를 움직이는 함수
    private fun eachViewMovingUpdate(objectList: List<MovingObject>, viewTable: MutableMap<Long,ImageView>){
        val objectIt = objectList.iterator()
        val viewIt = viewTable.iterator()

        var curObj: MovingObject
        var curView: ImageView

        //데이터 개수만큼 업데이트 (객체와 대응하는 뷰 각각 가져와서 속성 갱신)
        while(objectIt.hasNext()){
            curObj = objectIt.next()
            curView = viewIt.next().value
            curView.x = curObj.getX()
            curView.y = curObj.getY()
        }
    }

    /* 모델에서 객체가 추가된 것을 인식하고 처음 연관된 뷰를 만들 때 사용하는 함수 */
    private fun makeNewMovingView(obj: MovingObject, info: ObjectInfo): ImageView{
        val newView = ImageView(this)
        newView.setImageResource(info.resId)
        newView.scaleType = ImageView.ScaleType.FIT_XY

        //무슨 info 받았냐에 따라 크기와 소스 이미지 달라짐
        newView.layoutParams = FrameLayout.LayoutParams(info.width, info.height)
        newView.x = obj.getX()
        newView.y = obj.getY()

        inView.gameStage.addView(newView) //속성 적용이 끝났으면 화면에 추가
        return newView //테이블에 추가하기 위해 반환
    }

    //------------------------------------------------
    //함수 영역 (게임 종료와 연관됨)
    //

    /* onPause, onStop 상황이나, life -> 0이 됐을 때 게임이 중지됨 */
    private fun gameStop(){
        //타이머 동작 취소와 컨트롤러 비활성화
        updateTimer.cancel()
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