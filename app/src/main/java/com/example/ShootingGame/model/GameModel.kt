package com.example.ShootingGame.model

import com.example.ShootingGame.R
import kotlin.math.floor

/**
 *     <메인 게임 모델> - Cannon, Enemy, Bullet 클래스를 1개씩 가짐
 *      각 세부 모델은 다른 모델과 직접적으로 연결되지 않음 -> GameModel = 개별 모델들의 controller
 *     `GameModel 역할은 아래에 기술, 역할에 따라 함수가 구성됨
 *
 *      ((액티비티에 데이터 현황 알리기))
 *          1. 화면 속 탄환들 --> getBullets()
 *          2. 화면 속 적들 --> getEnemies()
 *          3. 게임에서 남은 목숨 --> getLife()
 *
 *      ((사용자 입력에 의해 변경되는 데이터 갱신))
 *          1. 대포 회전 각도 갱신 --> cannonRotate()
 *          2. 새로운 탄환을 포구에 생성하고 발사 --> shootBullet()
 *
 *      ((자동으로 변경되는 데이터 갱신)) --> periodicModelUpdate()
 *          1. 주기적으로 랜덤 위치에 적 생성 --> setEnemy()
 *          2. 발사한 탄환과 랜덤 발생하는 적들의 움직임 --> allMovingUpdate()
 *          3. 탄환과 적들 적절하게 삭제, 적이 탈출하면 life 차감 --> allDeletionUpdate()
 */
class GameModel(displayX: Int, displayY: Int) {
    //------------------------------------------------
    //상수 영역
    //

    //게임 내 객체(Bullet, Enemy)의 위치 변경이 발생하는 빈도
    val updatePeriod = 80L

    //게임 내 개체들의 정보 (이미지, 가로, 세로 순)
    val bulletInfo = ObjectInfo((0.08F * displayX).toInt(), (0.08F * displayY).toInt(), R.drawable.bullet)
    val enemyInfo = ObjectInfo((0.1F * displayX).toInt(), (0.1F * displayY).toInt(), R.drawable.enemy)
    private val displayInfo = ObjectInfo(displayX, displayY)

    //게임에서 나타나는 각 개체의 데이터를 가진 모델 변수들
    private val cannonModel = Cannon()
    private val bulletModels =  mutableListOf<Bullet>()
    private val enemyModels =  mutableListOf<Enemy>()

    //대포에 관한 데이터 (중심까지의 크기, 중심의 위치)
    //대포의 중심을 기점으로 회전하므로 중심 위주의 데이터가 필요함
    private val cannonHalf = displayY * 0.075F
    private val cannonBaseX = displayX * 0.5F
    private val cannonBaseY = displayY - cannonHalf

    //화면 속 탄환 최대 숫자 (난이도 조절을 위해서는 탄환이 무제한이면 안된다는 판단)
    private val bulletLimit = 3

    //탄환에 대한 데이터 (탄환의 첫 위치 좌표값 -> cannon 각도에 따라 달라짐)
    //좌표 기준을 중앙으로 맞추기 위하여 bulletWidth/2, bulletHeight/2를 차감
    private val bulletFirstX: Float
        get() = cannonBaseX + cannonModel.getVectorX() * cannonHalf - bulletInfo.width/2
    private val bulletFirstY: Float
        get() = cannonBaseY + cannonModel.getVectorY() * cannonHalf - bulletInfo.height/2

    //적에 대한 데이터 (적의 첫 x 위치, 내려오는 속도)
    //위치는 가로 범위 내에서 랜덤,속도는 20~35 사이에서 랜덤
    private val enemyFirstX: Float
        get() = Math.random().toFloat() * (displayInfo.width - bulletInfo.width)
    private val enemyVelocity : Float
        get() = Math.random().toFloat() * 20F + 30F

    //------------------------------------------------
    //변수 영역
    //

    //적 개체가 언제 나타날 주기의 기준(enemyPeriod)과 관련 카운터(countForEnemy)
    //enemyPeriod 역시 랜덤하게 변하도록 설정했기 때문에 변수 영역에 넣었음
    private var enemyPeriod = 800L
    private var countTimeForEnemy = 0L

    //현재 life 개수를 담당하는 변수
    private var life = 2


    //------------------------------------------------
    //함수 영역 (액티비티에 데이터 현황 알리기)
    //   1. bullet 데이터
    //   2. enemy 데이터
    //   3. life 데이터
    //

    /* 1,2. bullet or enemy 데이터
         액티비티에서 내용을 변경하는 일이 없도록 List 변환을 한 뒤 반환 */
    fun getBullets(): List<Bullet> {
        return bulletModels.toList()
    }
    fun getEnemies(): List<Enemy>{
        return enemyModels.toList()
    }

    /* 3. life 데이터 */
    fun getLife(): Int{
        return life
    }


    //------------------------------------------------
    //함수 영역 (사용자 입력에 의해 변경되는 데이터 갱신)
    //   1. 대포 회전 각도 갱신
    //   2. 새로운 탄환을 포구에 생성하고 발사
    //

    /*  1. 대포 회전 각도 갱신 (각도를 return, 대포 회전 속성으로 사용)
          사용자가 rotateBar 움직임을 통해 대포를 회전하려 할 때 사용된다.
          cannonModel 대포를 progress 만큼 회전할 것을 명령 */
    fun cannonRotate(progress: Int) : Float{
        return cannonModel.rotate(progress)
    }

    /* 2. 새로운 탄환을 포구에 생성하고 발사 (bullet 데이터 리스트에 값 추가)
         bulletModel 내에 새로운 탄환의 시작 위치와, 방향 벡터를 보낸다. (내부에서 이것으로 속도 설정)
         사용자가 fire 버튼을 눌러서 탄환을 발사하려고 할 때 사용된다. */
    fun shootBullet(){
        if(bulletLimit > bulletModels.size)
            bulletModels.add(Bullet(bulletFirstX, bulletFirstY, cannonModel.getVectorX(), cannonModel.getVectorY()))
    }

    //------------------------------------------------
    //함수 영역 (자동으로 변경되는 데이터 갱신)
    //

    /**  activity(controller)에서 40ms마다 실행되는 타이머로 반복 실행되는 함수
     *    1. 주기적으로 랜덤 위치에 적 생성
     *    2. 발사한 탄환과 랜덤 발생하는 적들의 움직임
     *    3. 탄환과 적들 적절하게 삭제, 적이 탈출하면 life 차감
     *
     *   이 함수가 타이머에 따라 실행되며 위 3가지 동작을 수행함
     */
    fun totalModelPeriodicUpdate(){
        setEnemy() //1
        allMovingUpdate() //2
        allDeletionUpdate() // 3, 4
    }

    //데이터를 변경하는 과정은 private -> 액티비티는 데이터 변경 요청만 한다.

    /* 1. 주기적으로 랜덤 위치에 적 생성
        새로 게임에 등장할 적을 담당할 객체를 선택하고 세팅을 해주는 함수
        controller 내부 timer 에서 1초를 셀 때마다 실행되어 적 개체를 만든다. */
    private fun setEnemy(){
        //랜덤 위치와 속도를 가진 enemy 생성
        if(isItTimeForNewEnemy())
            enemyModels.add(Enemy(enemyFirstX, enemyVelocity))
    }

    /* 타이머에 의한 실행 시 마다 따로 시간을 재서 new enemy 등장 타이밍을 정하는 함수
       추가적으로 new enemy 마다 카운팅 시간을 새로 정한다. */
    private fun isItTimeForNewEnemy(): Boolean{
        //enemy 카운터 갱신, 아직 생성할 때가 아니면 함수 종료
        countTimeForEnemy += updatePeriod
        if(countTimeForEnemy < enemyPeriod)
            return false

        //enemy 카운터를 초기화하고 목표 시간 다시 랜덤으로 설정(최소 600 ~ 최대 900ms)
        countTimeForEnemy = 0
        enemyPeriod = floor(Math.random() * 300).toLong() + 600L

        return true
    }

    /* 2. 발사한 탄환과 랜덤 발생하는 적들의 움직임 */
    private fun allMovingUpdate(){
        movingObjectUpdate(bulletModels)
        movingObjectUpdate(enemyModels)
    }

    /* 화면 속 움직이는 객체들의 (위치)정보 업데이트 */
    /* MovingObject 리스트를 받으므로 확장에 개방적 */
    private fun movingObjectUpdate(list: List<MovingObject>) {
        val it = list.iterator()
        var curInfo: MovingObject
        while (it.hasNext()) {
            curInfo = it.next()
            curInfo.locationUpdate()
        }
    }

    /* 3. 탄환과 적들 적절하게 삭제, 적이 탈출하면 life 차감
            1) 화면에서 벗어남
            2) 적과 탄환이 충돌함                     */
    private fun allDeletionUpdate(){
        deletionOfOutLimit() //1
        deletionOfCollision() //2
    }

    /* 객체가 화면에서 벗어났을 때 객체를 삭제하는 함수 */
    private fun deletionOfOutLimit(){
        //현재 작동하는 bullet, enemy 데이터 리스트를 순회하기 위한 iterator
        val bulletInList = bulletModels.iterator()
        val enemyInList = enemyModels.iterator()

        //리스트 속 각 데이터를 임시 저장할 변수
        var eachObject: MovingObject

        //모든 bullet 데이터를 순회하며 gameStage 밖에 있는(안 겹치는) 탄환 찾으면 제거
        while(bulletInList.hasNext()){
            eachObject = bulletInList.next()
            if(!isInRange(eachObject.getX(), eachObject.getY(), bulletInfo,
                    0F, 0F, displayInfo))
                bulletInList.remove()
        }

        //모든 enemy 데이터를 순회하며 gameStage 밖에 있는(안 겹치는) 적 찾으면 제거
        while(enemyInList.hasNext()){
            eachObject = enemyInList.next()
            if(!isInRange(eachObject.getX(), eachObject.getY(), enemyInfo,
                    0F, 0F, displayInfo)) {
                enemyInList.remove()
                life -= 1 //enemy 탈출 시 life 차감
            }
        }
    }

    /* 객체가 화면에서 벗어났을 때 객체를 삭제하는 함수 */
    private fun deletionOfCollision(){
        val bulletInList = bulletModels.iterator()

        //리스트 속 각 데이터를 임시 저장할 변수
        var eachBullet: Bullet
        var eachEnemy: Enemy

        //탄환을 기준으로 각 탄환마다 모든 적들과 위치 비교
        while(bulletInList.hasNext()){
            eachBullet = bulletInList.next()
            val enemyInList = enemyModels.iterator()
            while(enemyInList.hasNext()){
                eachEnemy = enemyInList.next()
                //두 정보 값을 봤을 때 겹친다고 판단이 된다면 둘 다 제거
                if(isInRange(eachBullet.getX(), eachBullet.getY(), bulletInfo,
                        eachEnemy.getX(), eachEnemy.getY(), enemyInfo)){
                    bulletInList.remove()
                    enemyInList.remove()
                    break
                }
            }
        }
    }

    /* 두 객체가 서로 겹치는지 확인하는 함수 */
    private fun isInRange(x1: Float, y1:Float, info1: ObjectInfo, x2: Float, y2:Float, info2: ObjectInfo): Boolean {
        //비교 대상의 우하단 꼭지점 좌표 계산 (범위 계산을 위해)
        val endX2 = x2 + info2.width
        val endY2 = y2 + info2.height

        //type1 개체가 비교 대상(type2)의 가로 범위와 겹치는가?
        if(x1 !in x2..endX2 && x1 + info1.width !in x2..endX2)
            return false

        //type1 개체가 비교 대상(type2)의 세로 범위와 겹치는가?
        if(y1 !in y2..endY2 && y1 + info1.height !in y2..endY2)
            return false

        return true
    }
}