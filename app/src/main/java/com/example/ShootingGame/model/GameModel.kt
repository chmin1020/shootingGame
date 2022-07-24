package com.example.ShootingGame.model

import com.example.ShootingGame.R
import kotlin.math.floor

/**
 *     <메인 게임 모델> - 고정된 Cannon 1개, 숫자가 바뀌는 객체(Bullet, Enemy)는 리스트로 가짐
 *      각 세부 모델은 다른 모델과 직접적으로 연결되지 않음 -> GameModel = 개별 모델들의 controller
 *     `GameModel 역할은 아래에 기술, 역할에 따라 함수가 구성됨
 *
 *      ((액티비티에 데이터 현황 알리기))
 *          1. 지워진(화면을 벗어나거나 충돌해서) 객체들의 ID --> getDeletedBulletIds(), getDeletedEnemyIds()
 *          2. 화면에 남아있는 객체들의 리스트 --> getBullets(), getEnemies()
 *          3. 게임에서 남은 목숨 --> getLife()
 *
 *      ((사용자 입력에 의해 변경되는 데이터 갱신))
 *          1. 대포 회전 각도 갱신 --> cannonRotate()
 *          2. 새로운 탄환을 포구에 생성하고 발사 --> shootBullet()
 *
 *      ((자동으로 변경되는 데이터 갱신))
 *          1. 주기적으로 랜덤 위치에 적 생성 --> newEnemy()
 *          2. 화면에 있는 움직이는 객체들의 이동과 삭제 --> totalModelExistingDataUpdate()
 */
class GameModel(displayX: Int, displayY: Int) {
    //------------------------------------------------
    //상수 영역
    //

    //게임 내 객체(Bullet, Enemy)의 위치 변경이 발생하는 빈도
    val updatePeriod = 80L

    //게임 내 개체들의 정보 (가로, 세로, 이미지<생략 가능> 순), 뷰를 그릴 때나 두 객체 겹침 여부 체크할 때 사용함
    val bulletInfo = ObjectInfo((0.08F * displayX).toInt(), (0.08F * displayY).toInt(), R.drawable.bullet)
    val enemyInfo = ObjectInfo((0.1F * displayX).toInt(), (0.1F * displayY).toInt(), R.drawable.enemy)
    private val displayInfo = ObjectInfo(displayX, displayY)

    //게임에서 나타나는 각 개체의 데이터를 가진 모델 변수들
    private val cannon = Cannon()
    private val bullets =  mutableListOf<Bullet>()
    private val enemies =  mutableListOf<Enemy>()

    //deletion 과정에서 지워진 탄환과 적의 아이디를 임시 저장하는 리스트
    private val deletedBulletIds = mutableListOf<Long>()
    private val deletedEnemyIds = mutableListOf<Long>()

    //대포에 관한 데이터 (중심까지의 크기, 중심의 위치)
    //대포의 중심을 기점으로 회전하므로 중심 위주의 데이터가 필요함
    private val cannonHalf = displayY * 0.075F
    private val cannonBaseX = displayX * 0.5F
    private val cannonBaseY = displayY - cannonHalf

    //화면 속 탄환 최대 숫자 (난이도 조절을 위해서는 탄환이 무제한이면 안된다는 판단)
    private val bulletLimit = 3

    //화면 속 탄환들의 기본 속력
    private val bulletSpeed = 80

    //새로 만들어지는 적과 탄환에게 고유한 아이디를 부여해야 함 -> 현재 시간은 항상 고유한 값을 가짐
    private val movingObjectId: Long
        get() = System.currentTimeMillis()

    //탄환에 대한 데이터 (탄환의 첫 위치 좌표값 -> cannon 각도에 따라 달라짐)
    //좌표 기준을 중앙으로 맞추기 위하여 bulletWidth/2, bulletHeight/2를 차감
    private val bulletFirstX: Float
        get() = cannonBaseX + cannon.getVectorX() * cannonHalf - bulletInfo.width/2
    private val bulletFirstY: Float
        get() = cannonBaseY + cannon.getVectorY() * cannonHalf - bulletInfo.height/2

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
    //

    /* 1. 지워진(화면을 벗어나거나 충돌해서) 객체들의 ID */
    fun getDeletedBulletIds(): List<Long>{
        return deletedBulletIds
    }
    fun getDeletedEnemyIds(): List<Long>{
        return deletedEnemyIds
    }

    /* 2. 화면에 남아있는 객체들의 리스트 */
    fun getBullets(): List<Bullet> {
        return bullets
    }
    fun getEnemies(): List<Enemy>{
        return enemies
    }

    /* 3. 게임에서 남은 목숨 */
    fun getLife(): Int{
        return life
    }


    //------------------------------------------------
    //함수 영역 (사용자 입력에 의해 변경되는 데이터 갱신, 리스너에 의해 실행)
    //

    /*  대포 회전 각도 갱신 (각도를 return, 대포 회전 속성으로 사용)
        cannonModel 대포를 progress 만큼 회전할 것을 명령 */
    fun cannonRotate(progress: Int) : Float{
        return cannon.rotate(progress)
    }

    /* 새로운 탄환을 포구에 생성하고 발사 (bullet 데이터 리스트에 값 추가)
         bulletModel 내에 새로운 탄환의 시작 위치와 속도를 보냄  */
    fun shootBullet(): Bullet? {
        if(bulletLimit > bullets.size) {
            val bullet = Bullet(movingObjectId, bulletFirstX, bulletFirstY,
                                cannon.getVectorX() * bulletSpeed, cannon.getVectorY() * bulletSpeed)
            bullets.add(bullet)
            return bullet
        }
        return null
    }

    //------------------------------------------------
    //함수 영역 (자동으로 변경되는 데이터 갱신)
    //

    /* 시간이 적절하게 지났다면 새 적 개체를 추가하고 액티비티에도 전달하는 함수*/
    fun newEnemy(): Enemy?{
        //적을 만들 시간이 됐으면 랜덤 위치와 속도를 가진 enemy 생성
        if(isItTimeForNewEnemy()) {
            val enemy = Enemy(movingObjectId, enemyFirstX, enemyVelocity)
            enemies.add(enemy)
            return enemy
        }
        return null
    }

    /* 화면에 존재하는 객체들의 움직임 또는 삭제를 담당하는 함수 */
    fun totalModelExistingDataUpdate(){
        allMovingUpdate() //존재하는 객체들의 움직임
        allDeletionUpdate() // 존재하는 객체들의 삭제
    }

    /* 타이머에 의한 실행 시 마다 따로 시간을 재서 new enemy 등장 타이밍을 정하는 함수
       추가적으로 new enemy 마다 카운팅 시간을 새로 정한다. --> newEnemy()와 연결 */
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

    /* 발사한 탄환과 랜덤 발생하는 적들의 움직임 */
    private fun allMovingUpdate(){
        eachMovingUpdate(bullets)
        eachMovingUpdate(enemies)
    }

    /* 화면 속 움직이는 객체들의 (위치)정보 업데이트
       MovingObject 리스트를 받으므로 확장에 개방적 */
    private fun eachMovingUpdate(list: List<MovingObject>) {
        val it = list.iterator()
        while (it.hasNext())
            it.next().positionUpdate()
    }

    /* 탄환과 적들 적절하게 삭제, 적이 탈출하면 life 차감 */
    private fun allDeletionUpdate(){
        //삭제된 객체의 id를 담는 list 초기화 (새 주기에서 삭제된 객체 id를 담아야 함)
        deletedBulletIds.clear()
        deletedEnemyIds.clear()

        //화면에서 벗어난 객체 삭제와 충돌한 두 객체 삭제 순서대로 진행
        //deletion 때는 리스트 요소의 변경이 일어나므로 MovingObject list 사용 불가
        bulletDeletionOfOutLimit()
        enemyDeletionOfOutLimit()
        collisionBetweenBulletAndEnemy()
    }

    /* 탄환이 화면에서 벗어날 때 삭제하는 함수 */
    private fun bulletDeletionOfOutLimit(){
        //현재 작동하는 bullet 리스트를 순회하기 위한 반복자와 리스트 내부 각 객체를 임시로 담을 변수
        val bulletInList = bullets.iterator()
        var eachBullet: Bullet

        //모든 bullet 데이터를 순회하며 gameStage 밖에 있는(안 겹치는) 탄환 찾으면 제거
        while(bulletInList.hasNext()){
            eachBullet = bulletInList.next()
            if(!isInRange(eachBullet.getX(), eachBullet.getY(), bulletInfo, 0F, 0F, displayInfo)) {
                deletedBulletIds.add(eachBullet.getId())
                bulletInList.remove()
            }
        }
    }

    /* 적이 화면에서 벗어날 때 삭제하는 함수 */
    private fun enemyDeletionOfOutLimit(){
        //현재 작동하는 enemy 리스트를 순회하기 위한 반복자와 리스트 내부 각 객체를 임시로 담을 변수
        val enemyInList = enemies.iterator()
        var eachEnemy: Enemy

        //모든 enemy 데이터를 순회하며 gameStage 밖에 있는(안 겹치는) 적 찾으면 제거
        while(enemyInList.hasNext()){
            eachEnemy = enemyInList.next()
            if(!isInRange(eachEnemy.getX(), eachEnemy.getY(), enemyInfo, 0F, 0F, displayInfo)) {
                deletedEnemyIds.add(eachEnemy.getId())
                enemyInList.remove()
                life -= 1 //enemy 탈출 시 life 차감
            }
        }
    }

    /* 적과 탄환의 충돌 시, 두 객체를 삭제하기 위한 함수 */
    private fun collisionBetweenBulletAndEnemy(){
        val bulletInList = bullets.iterator()

        //리스트 속 각 데이터를 임시 저장할 변수
        var eachBullet: Bullet
        var eachEnemy: Enemy

        //각 탄환마다 모든 적들과 위치 비교
        while(bulletInList.hasNext()){
            eachBullet = bulletInList.next()
            val enemyInList = enemies.iterator()

            //두 정보 값을 봤을 때 겹친다고 판단이 된다면 둘 다 제거
            while(enemyInList.hasNext()){
                eachEnemy = enemyInList.next()
                if(isInRange(eachBullet.getX(), eachBullet.getY(), bulletInfo,
                        eachEnemy.getX(), eachEnemy.getY(), enemyInfo)){
                    deletedBulletIds.add(eachBullet.getId())
                    deletedEnemyIds.add(eachEnemy.getId())
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

        //두 객체가 가로 범위에서 겹치는 부분이 있는가?
        if(x1 !in x2..endX2 && x1 + info1.width !in x2..endX2)
            return false

        //두 객체가 세로 범위에서 겹치는 부분이 있는가?
        if(y1 !in y2..endY2 && y1 + info1.height !in y2..endY2)
            return false

        return true
    }
}