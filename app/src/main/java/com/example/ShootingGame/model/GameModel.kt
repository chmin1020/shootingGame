package com.example.ShootingGame.model

import android.util.Log
import kotlin.math.abs
import kotlin.math.floor

/**
        <메인 게임 모델> - Cannon 1개, Enemy 6개, Bullet 2개, Life 1개를 가짐
        각 세부 모델은 다른 모델과 직접적으로 연결되지 않음 -> GameModel = 개별 모델들의 controller
        GameModel 역할은 아래에 기술, 역할에 따라 함수가 구성됨
        ((사용자 입력에 의해 변경되는 데이터 갱신))
            1. 대포 회전 각도 갱신 --> cannonRotate()
            2. 탄환 발사를 위한 탄환 선택과 설정 --> shootBullet()
        ((자동으로 변경되는 데이터 갱신)) --> periodicModelUpdate()
            1. 주기적으로 랜덤 위치에 적 생성 --> setEnemy()
            2. 발사한 탄환과 랜덤 발생하는 적들의 움직임 --> allBulletUpdate() / allEnemyUpdate()
            3. 적과 탄환의 충돌 여부 체크 및 처리 --> allCollisionUpdate()
            4. 적이 화면을 빠져나갈 경우 life 차감 --> lifeDecrease() <allEnemyUpdate() 내부에서>
        ((액티비티에 데이터 현황 알리기))
            1. bullet 데이터 --> getBulletX(), getBulletY(), isBulletActivating()
            2. enemy 데이터 --> getEnemyX(), getEnemy(), isEnemyActivating()
            3. collision 데이터 --> collidedEnemyWithBullet()
            4. life 데이터 --> getLife()
*/
class GameModel(displayX: Float, displayY: Float) {
    //---------------------------
    //상수 영역
    //

    //대포에 관한 데이터
    private val cannonHalf = displayY * 0.075F
    private val cannonBaseX = displayX * 0.5F
    private val cannonBaseY = displayY - cannonHalf


    //게임 내 객체(Bullet, Enemy)의 위치 변경이 발생하는 빈도
    val updatePeriod = 40L
    //적 개체가 언제 나타날 주기의 기준(enemyPeriod)과 관련 카운터(countForEnemy)
    var enemyPeriod = 1000L
    private var countForEnemy = 0L

    //화면 크기 (컨트롤 관련 뷰를 제외한 게임 화면)
    private val xScale = displayX
    private val yScale = displayY

    //게임에서 나타나는 각 개체의 데이터를 가진 모델 변수들
    private val cannonModel = Cannon()
    private val bulletModel = Bullet()
    private val enemyModel = Enemy()
    private var lifeModel = Life()

    //적 개체의 random 속도 배열 (화면의 비율로 속도를 지정하여 기기 호환성 유지)
    private val randomVelocities = arrayOf(yScale/100, yScale/75, yScale/60, yScale/50, yScale/35)

    //게임 속 bullet, enemy 크기
    val bulletWidth = 0.08F * xScale
    val bulletHeight = 0.08F * yScale
    private val enemyWidth = 0.1F * xScale
    private val enemyHeight = 0.1F * yScale

    //게임 화면에 나타나는 bullet & enemy 개수
    var curBulletCnt = 0
    var curEnemyCnt = 0

    ///////////////////////////((사용자 입력에 의해 변경되는 데이터 갱신))////////////////////////////////
    //////////////////////////////////////1. 대포 회전 각도 갱신//////////////////////////////////////
    //////////////////////////////2. 탄환 발사를 위한 탄환 선택과 설정//////////////////////////////////

    /*  1. 대포 회전 각도 갱신
          cannonModel에 대포를 progress만큼 회전할 것을 명령 */
    fun cannonRotate(progress: Int) : Float{
        return cannonModel.rotate(progress)
    }

    /* 2. 탄환 발사를 위한 탄환 선택과 설정
         bullet의 시작 위치와 도착 위치를 계산하고 각 bulletModel에 적용하는 함수
         controller에서 bullet이 발사되어야 함을 알렸을 때 실행되는 함수
         대포의 회전 정도에 따라 각 위치가 달라지므로 slope(화면에서 대포 중심과 화구 사이 기울기)를 통해 판별 */
    fun shootBullet(){
        val sx = cannonBaseX + cannonModel.getVectorX() * cannonHalf - bulletWidth/2
        val sy = cannonBaseY + cannonModel.getVectorY() * cannonHalf - bulletHeight/2
        bulletModel.newShooting(sx, sy ,cannonModel.getVectorX(), cannonModel.getVectorY())
        curBulletCnt = bulletModel.getBulletCnt()
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////


    ////////////////////////////////((자동으로 변경되는 데이터 갱신))////////////////////////////////////
    ///////////////////////////////1. 주기적으로 랜덤 위치에 적 생성/////////////////////////////////////
    //////////////////////////2. 발사한 탄환과 랜덤 발생하는 적들의 움직임/////////////////////////////////
    /////////////////////////////3. 적과 탄환의 충돌 여부 체크 및 처리///////////////////////////////////
    ////////////////////////////4. 적이 화면을 빠져나갈 경우 life 차감///////////////////////////////////

    /*  activity(controller)에서 40ms마다 실행되는 타이머로 반복 실행되는 함수
        이 함수가 타이머에 따라 실행되면서 앞서 서술한 4가지 동작을 모두 수행함 */
    fun totalModelPeriodicUpdate(){
        setEnemy() //1
        //2, 3, 4
        allBulletUpdate() //bullet update
        allEnemyUpdate() //enemy update
        //allDeletionUpdate() //collision & crossing limit update
    }
    /* 1. 주기적으로 랜덤 위치에 적 생성
        새로 게임에 등장할 적을 담당할 객체를 선택하고 세팅을 해주는 함수
        controller 내부 timer가 1초를 셀 때마다 실행되어 적 개체를 만든다.
        적의 시작 위치(x)와 내려오는 속도는 무작위로 정한다. */
    private fun setEnemy(){
        //enemy 카운터 갱신, 아직 생성할 때가 아니면 함수 종료
        countForEnemy += updatePeriod
        if(countForEnemy < enemyPeriod)
            return

        //enemy 카운터를 초기화하고 목표 시간 다시 랜덤으로 설정
        //목표 시간은 최소 600 ~ 최대 1000ms
        countForEnemy = 0
        enemyPeriod = floor(Math.random() * 400).toLong() + 600L

        enemyModel.newEnemy(
                    Math.random().toFloat() * (xScale - enemyWidth),
                    randomVelocities[floor(Math.random() * 5).toInt()]
                )
        curEnemyCnt = enemyModel.getEnemyCnt()
    }

    /*
    /* 2. 발사한 탄환과 랜덤 발생하는 적들의 움직임
         allBulletUpdate - processAboutBulletLimit / allEnemyUpdate - processAboutEnemyLimit
         두 쌍의 함수가 서로 연결되며, 추가적으로 enemy의 움직임은 lifeDecrease와 연결됨 */
    /* 0~1까지 activate 상태인 bullet이 있다면 그 bullet의 위치 값 갱신 */*/
    private fun allBulletUpdate(){
        bulletModel.updateLocation()
    }

    /* 0~4까지 activate 상태인 enemy가 있다면 그 enemy의 위치 값 갱신(x는 고정, y만 변화) */
    private fun allEnemyUpdate(){
        enemyModel.updateLocation()
    }

    /* 3. 적과 탄환의 충돌 여부 체크 및 처리
         allCollisionUpdate에서 각 탄환과 적을 비교하여 충돌을 체크
         collisionCheck에서 두 객체 사이 거리를 확인하여 충돌 여부 확인 */
    /* 각 탄환과 적의 충돌 여부를 확인하고, 충돌 발생 확인 시 초기화하는 함수 */
    private fun allDeletionUpdate(){
        for(i in curBulletCnt - 1 downTo 0) {
            if(overLimit(bulletModel.posAndVelocity[i]))
                bulletModel.removeBullet(i)
        }
        curBulletCnt = bulletModel.getBulletCnt()

        for(i in curEnemyCnt - 1 downTo 0) {
            if(overLimit(enemyModel.posAndVelocity[i]))
                enemyModel.removeEnemy(i)
        }
        curEnemyCnt = enemyModel.getEnemyCnt()
    }
    /*  모델 내부에서 각 탄환과 적의 x, y 거리를 비교하여 충돌을 확인하는 함수
       모델의 dataUpdate에서 사용하므로 외부에서 접근할 수 없는 private 함수 */

    /*private fun collisionCheck(bIdx : Int, eIdx : Int): Boolean{
        //두 객체 bullet과 enemy의 x좌표, y좌표 차이를 구하기
        val xDif: Float = abs(bulletModels[bIdx].getX() - enemyModels[eIdx].getX())
        val yDif: Float = abs(bulletModels[bIdx].getY() - enemyModels[eIdx].getY())

        //거리 계산 및 충돌 여부 체크
        if(xDif < enemyWidth && yDif < enemyHeight){
            //충돌이 eIdx번째 enemy와 났다는 것을 임시 저장(뷰에 충돌을 적용하기 위한 데이터)
            bulletModels[bIdx].setCollision(eIdx)
            return true
        }
        return false
    }*/



    /* 4. 적이 화면을 빠져나갈 경우 life 차감
         게임 속 목숨을 차감할 때 실행되는 함수 */
    private fun lifeDecrease(){
        lifeModel.lifeDecrease()
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////((액티비티에 데이터 현황 알리기))////////////////////////////////////
    /////////////////////////////////////1. bullet 데이터////////////////////////////////////////////
    /////////////////////////////////////2. enemy 데이터/////////////////////////////////////////////
    ///////////////////////////////////3. collision 데이터///////////////////////////////////////////
    /////////////////////////////////////4. life 데이터//////////////////////////////////////////////

    /* 1.bullet 데이터 (위치값, 활성화 여부) */
    /* 탄환의 현재 (x, y)좌표를 확인하는 함수들 */
    fun getBulletInfo(): ArrayList<MovingObjectInfo>{
        return bulletModel.posAndVelocity
    }

    /* 2.enemy 데이터 (위치값, 활성화 여부) */
    /* 적의 현재 (x, y)좌표를 확인하는 함수들 */
    fun getEnemyInfo(): ArrayList<MovingObjectInfo>{
        return enemyModel.posAndVelocity
    }

    /*
    /* 3. collision 데이터 (각 탄환을 기준으로 collision 발생 확인) */
    /* idx를 색인으로 가진 탄환이 현재 enemy와 충돌된 상황인지 확인하는 함수
      충돌이 되었다면 충돌한 enemy의 색인을 반환, 아니라면 -1을 반환
        컨트롤러에서 뷰의 충돌 여부를 알고, 충돌한 두 뷰를 없앨 수 있도록 한다.*/
    fun collidedEnemyWithBullet(idx: Int): Int{
        return bulletModels[idx].collisionCheck()
    }
*/
    /* 4. life 데이터 */
    fun getLife(): Int{
        return lifeModel.getLife()
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private fun overLimit(info: MovingObjectInfo): Boolean {
        if(!(info.x in 0F..xScale))
            return true
        if(!(info.y in 0F..yScale))
            return true
        return false
    }
}