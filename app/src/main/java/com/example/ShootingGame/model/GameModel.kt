package com.example.ShootingGame.model

import kotlin.math.abs
import kotlin.math.floor

/*
        <메인 게임 모델> - Cannon 1개, Enemy 6개, Bullet 2개를 가짐
        각 세부 모델은 다른 모델과 직접적으로 연결되지 않음 -> GameModel이 개별 모델들의 controller 역할
        액티비티(controller)가 GameModel에게 요구하는 것 6가지
        ((사용자 입력에 의해 변경되는 데이터))
        1. 대포 회전 각도 갱신
        2. 탄환 발사를 위한 탄환 선택과 설정

        ((자동으로 변경되는 데이터))
        3. 주기적으로 랜덤 위치에 적 생성
        4. 발사한 탄환과 랜덤 발생하는 적들의 움직임
        5. 적과 탄환의 충돌 여부 체크 및 처리
        6. 게임 종료(life 손실로 인한 탈락) 여부 확인

        따라서 GameModel의 변수와 메소드는 모두 이들과 관련이 있음
        GameModel에서는 데이터 갱신이 발생하나, 이를 뷰에 적용하지는 않음
 */
class GameModel(displayX: Float, displayY: Float) {
    //사용 가능한 enemy 또는 bullet이 없다는 것을 표시하기 위한 상수
    private val invalidIndex = -1
    //게임 내 객체(Bullet, Enemy)의 위치 변경이 발생하는 빈도
    val updatePeriod = 40L
    //적 개체가 언제 나타날 주기의 기준(enemyPeriod)과 관련 카운터(countForEnemy)
    var enemyPeriod = 1000L
    private var countForEnemy = 0L

    //탄환과 적 개체 마지막 색인(범위)
    val finalBulletIdx = 1
    val finalEnemyIdx = 5

    //화면 크기 (컨트롤 관련 뷰를 제외한 게임 화면)
    private val xScale = displayX
    private val yScale = displayY

    //게임에서 나타나는 각 개체의 데이터를 가진 모델 변수들
    private val cannonModel = Cannon(xScale , yScale)
    private val enemyModels = arrayOf(Enemy(), Enemy(), Enemy(), Enemy(), Enemy(), Enemy())
    private val bulletModels = arrayOf(Bullet(), Bullet())

    //탄환 발사 시 목표 위치를 파악하기 위한 기준이 되는 기울기 값 (원점과 대포의 중심점을 연결한 그래프의 기울기)
    private val slope = cannonModel.getBaseY()/cannonModel.getBaseX()
    //적 개체의 random 속도
    private val randomVelocities = arrayOf(yScale/100, yScale/75, yScale/60, yScale/50, yScale/35)
    //플레이어의 게임 목숨 (기본 2개)
    private var life = 2

    //게임 속 고정된 cannon의 중심점
    private val cannonBaseX = cannonModel.getBaseX()
    private val cannonBaseY = cannonModel.getBaseY()

    //게임 속 bullet, enemy 크기
    private val bulletWidthHalf = 0.04F * xScale
    private val bulletHeightHalf = 0.04F * yScale
    private val enemyWidth = 0.1F * xScale
    private val enemyHeight = 0.1F * yScale

    /////////////////주기적 업데이트하는 데이터///////////////////////////////////////////
    /*  activity(controller)에서 40ms마다 실행되는 타이머로 반복 실행되는 함수
        1. 그때마다 랜덤하게 정해진 주기에 따라 enemy 생성
        2. 각 enemy와 bullet의 위치를 시간 단위마다 변경
            -크게 bullet 위치 적용, enemy 위치 적용, 충돌 여부 체크로 나뉨
    */
    fun periodicModelUpdate(){
        //1
        countForEnemy += updatePeriod //enemy 생성 관련 초 세기
        if(countForEnemy >= enemyPeriod) //시간이 됐으면 enemy 생성
            setEnemy()

        //2
        allBulletUpdate() //bullet update
        allEnemyUpdate() //enemy update
        allCollisionUpdate() //collision update
    }
    //0~1까지 activate 상태인 bullet이 있다면 그 bullet의 위치 값 갱신
    private fun allBulletUpdate(){
        for(i in 0..finalBulletIdx){
            if(!bulletModels[i].isAvailable())
                bulletModels[i].updateLocation()
        }
    }
    //0~4까지 activate 상태인 enemy가 있다면 그 enemy의 위치 값 갱신(x는 고정, y만 변화)
    private fun allEnemyUpdate(){
        for(i in 0..finalEnemyIdx) {
            if(!enemyModels[i].isAvailable())
                enemyModels[i].updateLocation()
        }
    }
    private fun allCollisionUpdate(){
        for(i in 0..finalBulletIdx) {
            if (!bulletModels[i].isAvailable()) {
                for (j in 0..finalEnemyIdx) {
                    if (!enemyModels[j].isAvailable()) {
                        if(collisionCheck(i, j)){
                            //충돌이 발생한 bullet과 enemy는 모두 초기화 (화면에서 사라지게 하기 위함)
                            bulletModels[i].settingReset()
                            enemyModels[j].settingReset()
                            break
                        }
                    }
                }
            }
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////충돌 관련 함수////////////////////////////////////////////
    /*  모델 내부에서 각 탄환과 적의 x, y 거리를 비교하여 충돌을 확인하는 함수
        모델의 dataUpdate에서 사용하므로 외부에서 접근할 수 없는 private 함수 */
    private fun collisionCheck(bIdx : Int, eIdx : Int): Boolean{
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
    }
    /*idx를 색인으로 가진 탄환이 현재 enemy와 충돌된 상황인지 확인하는 함수
      충돌이 되었다면 충돌한 enemy의 색인을 반환, 아니라면 -1을 반환
        컨트롤러에서 뷰의 충돌 여부를 알고, 충돌한 두 뷰를 없앨 수 있도록 한다.*/
    fun collidedEnemyWithBullet(idx: Int): Int{
        return bulletModels[idx].collisionCheck()
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////



    /////////////////////////////////////대포 관련 함수(회전)//////////////////////////////////////////
    /* cannonModel에 대포를 progress만큼 회전할 것을 명령 */
    fun cannonRotate(progress: Int) : Float{
        return cannonModel.rotate(progress)
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////탄환 관련 함수/////////////////////////////////////////////
    /* bullet의 시작 위치와 도착 위치를 계산하고 각 bulletModel에 적용하는 함수
           controller에서 bullet이 발사되어야 함을 알렸을 때 실행되는 함수
           대포의 회전 정도에 따라 각 위치가 달라지므로 slope(화면에서 대포 중심과 화구 사이 기울기)를 통해 판별*/
    fun setBullet(){
        //발사할 bullet 선택, 가능한 bullet이 없다면 -1
        val idx =
            if (bulletModels[0].isAvailable()) 0
            else if (bulletModels[1].isAvailable()) 1
            else invalidIndex

        //가능한 bullet이 있었다면 시작, 종료 위치를 정함
        if(idx != invalidIndex){
            bulletModels[idx].activate()
            //시작 위치 설정 (대포 포구의 위치, 단 탄환의 중심점이 여기 위치 하도록 미세 조정함.
            val sx = cannonModel.getMuzzleX() - bulletWidthHalf
            val sy = cannonModel.getMuzzleY() - bulletHeightHalf
            //종료 위치, 뒤에서 계산을 통해 구해야 하는 변수들
            val ex: Float
            val ey: Float
            //종료 위치 계산을 위한 기준이 되는 기울기 (대포 중심점에서 기기 왼쪽 모서리까지를 이은 선의 기울기)
            //계산에서 이용하기 위해 기울기의 역수도 따로 저장
            val cur = (cannonBaseY - cannonModel.getMuzzleY()) / (cannonBaseX - cannonModel.getMuzzleX())
            val inverseCur = 1 / cur


            //탄환의 목표는 크게 3가지로 나뉨 -> 화면 왼쪽, 화면 위쪽, 화면 오른쪽
            //base 좌표와 muzzle 좌표의 x,y 비율을 나타낸 cur(기울기)을 통해 적절한 목표 위치를 구한다.
            if(abs(cur) < slope){ //bullet target : 화면 측면
                if(cannonBaseX > sx) { //bullet target : 화면 왼쪽
                    ex = 0F
                    ey = cannonBaseY - cur * cannonBaseX - bulletHeightHalf
                }
                else{ //bullet target : 화면 오른쪽
                    ex = 0.92F * xScale
                    ey = cannonBaseY + cur * (xScale - cannonBaseX) - bulletHeightHalf
                }
            }
            else{ //bullet target : 화면 위쪽
                ex = cannonBaseX - inverseCur * cannonBaseY - bulletWidthHalf
                ey = 0F
            }
            //설정된 대로 bulletModel에 데이터(시작 위치, 도착 위치, 앞선 둘을 기반으로 계산한 속도) 적용
            bulletModels[idx].newSetting(sx, sy, ex, ey)
        }
    }

    /*탄환의 현재 (x, y)좌표를 확인하는 함수들 */
    fun getBulletX(idx: Int) : Float{
        return bulletModels[idx].getX()
    }
    fun getBulletY(idx: Int) : Float{
        return bulletModels[idx].getY()
    }

    /* 탄환이 현재 활성화 상태인지 확인하는 함수 (사용가능하지 않으면 활성화) */
    fun isBulletActivating(idx: Int) : Boolean{
        return !bulletModels[idx].isAvailable()
    }

    /* 탄환이 현재 화면 범위를 넘어섰는지 확인 및 처리하는 함수 */
    fun isBulletLimitOut(idx: Int) : Boolean{
        //범위를 넘었다면 관련 데이터 초기화
        if(bulletModels[idx].overLimit()){
            bulletModels[idx].settingReset()
            return true
        }
        return false
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////적 관련 함수///////////////////////////////////////////////

    /* 새로 게임에 등장할 적을 담당할 객체를 선택하고 세팅을 해주는 함수
       controller 내부 timer가 1초를 셀 때마다 실행되어 적 개체를 만든다.
       적의 시작 위치(x)와 내려오는 속도는 ramdom으로 정한다.*/
    private fun setEnemy(){
        countForEnemy = 0
        enemyPeriod = floor(Math.random() * 400).toLong() + 600L
        //적절한 enemy 선택
        for(i in 0..finalEnemyIdx){
            //random 위치와 속도 설정
            if(enemyModels[i].isAvailable()){
                enemyModels[i].activate()
                enemyModels[i].newSetting(
                    Math.random().toFloat() * (xScale - enemyWidth),
                    randomVelocities[floor(Math.random() * 5).toInt()]
                )
                return
            }
        }
    }

    /* 적이 현재 활성화 상태인지 확인하는 함수 (사용가능하지 않으면 활성화) */
    fun isEnemyActivating(idx: Int) : Boolean{
        return !enemyModels[idx].isAvailable()
    }

    /*적의 현재 (x, y)좌표를 확인하는 함수들 */
    fun getEnemyX(idx:Int): Float{
        return enemyModels[idx].getX()
    }
    fun getEnemyY(idx:Int): Float{
        return enemyModels[idx].getY()
    }

    /* 적이 현재 화면 범위를 넘어섰는지 확인 및 처리하는 함수
       적은 아래로만 이동하므로 yScale을 넘어가면 limit out */
    fun isEnemyLimitOut(idx: Int) : Boolean{
        if(enemyModels[idx].getY() > yScale){
            enemyModels[idx].settingReset()
            return true
        }
        return false
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////


    /*게임이 종료되었음을 사용자에게 알리기 위한 함수
      enemy를 놓칠 때마다 이 함수가 호출됨
      2번 이상 놓치는 경우 life가 0 이하가 되어 true를 반환
        -> controller에서 게임의 종료를 탐지하고 종료 */
    fun isGameEnd():Boolean{
        life -= 1
        if(life <= 0)
            return true
        return false
    }
}