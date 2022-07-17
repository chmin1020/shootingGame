package com.example.ShootingGame.Model

import kotlin.math.abs

class GameModel(displayX: Float, displayY: Float) {
    //display size
    private val xScale = displayX
    private val yScale = displayY

    //specific models
    private val cannonModel = Cannon(xScale , yScale)
    private val enemyModels = arrayOf(Enemy(), Enemy(), Enemy(), Enemy(), Enemy())
    private val bulletModels = arrayOf(Bullet(), Bullet())

    //탄환 발사 시 목표 위치를 파악하기 위한 기준이 되는 기울기 값
    private val slope = cannonModel.getBaseY()/cannonModel.getBaseX()
    //플레이어의 게임 목숨 (기본 2개)
    private var life = 2

    private val NODATA = -1

    //게임 속 bullet * enemy 크기
    private val bulletWidth = 0.08F * xScale
    private val bulletHeight = 0.08F * yScale
    private val enemyWidth = 0.1F * xScale
    private val enemyHeight = 0.1F * yScale

    /*
        activity(controller)에서 1ms마다 실행되는 타이머로 반복 실행되는 함수
        각 enemy와 bullet의 위치를 여기서 변경해서, 각 이미지의 애니메이션을 수행
        크게 bullet 위치 적용, enemy 위치 적용, 충돌 여부 체크로 나뉨
     */
    fun dataUpdate(){
        //bullet update
        for(i in 0..1){
            if(!bulletModels[i].isAvailable())
                bulletModels[i].updateXY()
        }
        //enemy update
        for(i in 0..4) {
            if(!enemyModels[i].isAvailable())
                enemyModels[i].setY(enemyModels[i].getY() + enemyModels[i].getVelocity())
        }

        //collision or not
        for(i in 0..1) {
            if (!bulletModels[i].isAvailable()) {
                for (j in 0..4) {
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

    /////////////////대포 관련 함수(회전)/////////////////
    fun cannonRotate(progress: Int) : Float{
        return cannonModel.rotate(progress)
    }
    ///////////////////////////////////////////////////

    /////////////////탄환 관련 함수()/////////////////
    /* bullet의 시작 위치와 도착 위치를 계산하고 각 bulletModel에 적용하는 함수
       controller에서 bullet이 발사되어야 함을 알렸을 때 실행되는 함수
       대포의 회전 정도에 따라 각 위치가 달라지므로 slope(화면에서 대포 중심과 화구 사이 기울기)를 통해 판별
    */
    fun bulletSetTarget(idx: Int){
        val sx = cannonModel.getMuzzleX() - bulletWidth/2
        val sy = cannonModel.getMuzzleY() - bulletHeight/2
        val ex: Float
        val ey: Float
        val cur = (cannonModel.getBaseY() - cannonModel.getMuzzleY()) / (cannonModel.getBaseX() - cannonModel.getMuzzleX())
        if(cur > 0){
            if(cur < slope) { //기울기가 낮 (0, 양수)
                ex = 0F
                ey =cannonModel.getBaseY() - cur * cannonModel.getBaseX() - bulletHeight/2
            }
            else { // (양수, 0)
                ex = cannonModel.getBaseX() - (1 / cur) * cannonModel.getBaseY() - bulletWidth/2
                ey = 0F
            }
        }
        else if(cur == 0F){
            if(cannonModel.getDegree() == 90F) {
                ex = 0.92F * xScale
                ey = cannonModel.getBaseY() + cur * (xScale - cannonModel.getBaseX()) - bulletHeight / 2
            }
            else {
                ex = 0F
                ey = cannonModel.getBaseY() - cur * cannonModel.getBaseX() - bulletHeight / 2
            }
        }
        else {
            if (cur < -slope) { //(양수, 0)
                ex = cannonModel.getBaseX() + (-1 / cur) * cannonModel.getBaseY() - bulletWidth / 2
                ey = 0F
            }
            else { //(경계, 경계)
                ex = 0.92F * xScale
                ey = cannonModel.getBaseY() + cur * (xScale - cannonModel.getBaseX()) - bulletHeight / 2
            }
        }
        bulletModels[idx].setMoveData(sx, sy, ex, ey)
    }

    //탄환의 현재 x좌표를 확인하는 함수
    fun bulletInfoX(idx: Int) : Float{
        return bulletModels[idx].getX()
    }

    //탄환의 현재 y좌표를 확인하는 함수
    fun bulletInfoY(idx: Int) : Float{
        return bulletModels[idx].getY()
    }

    /*idx를 색인으로 가진 탄환이 현재 enemy와 충돌된 상황인지 확인하는 함수
      충돌이 되었다면 충돌한 enemy의 색인을 반환, 아니라면 -1을 반환 */
    fun bulletInfoCollision(idx: Int): Int{
        return bulletModels[idx].collisionCheck()
    }

    /*2개의 탄환 중 어떤 걸 발사할지 결정하여 색인을 반환하는 함수
      2개의 탄환 모두 이미 사용 중(화면에 존재)이라면 -1을 반환*/
    fun bulletSelect() : Int{
        val res =
            if (bulletModels[0].isAvailable()) 0
            else if (bulletModels[1].isAvailable()) 1
            else NODATA

        if(res != NODATA)
            bulletModels[res].activate()
        return res
    }

    fun isBulletLimitOut(idx: Int) : Boolean{
        if(bulletModels[idx].overLimit()){
            bulletModels[idx].settingReset()
            return true
        }
        return false
    }
    ///////////////////////////////////////////////////

    fun setEnemy() : Int{
        //적절한 enemy 선택
        var idx:Int = NODATA
        for(i in 0..4){
            if(enemyModels[i].isAvailable()){
                idx = i
                break
            }
        }

        //random 위치와 속도 설정
        if(idx != NODATA) {
            enemyModels[idx].activate()
            enemyModels[idx].randomSetting(
                Math.random().toFloat() * (xScale - enemyWidth),
                Math.random().toFloat() + 0.5F
            )
        }
        return idx
    }

    fun enemyInfoX(idx:Int): Float{
        return enemyModels[idx].getX()
    }

    fun enemyInfoY(idx:Int): Float{
        return enemyModels[idx].getY()
    }

    fun isEnemyLimitOut(idx: Int) : Boolean{
        if(enemyModels[idx].getY() > yScale){
            enemyModels[idx].settingReset()
            return true
        }
        return false
    }

    private fun collisionCheck(bIdx : Int, eIdx : Int): Boolean{
        val xDif: Float = abs(bulletModels[bIdx].getX() - enemyModels[eIdx].getX())
        val yDif: Float = abs(bulletModels[bIdx].getY() - enemyModels[eIdx].getY())
        //거리 계산 및 충돌 여부 체크
        if(xDif < enemyWidth && yDif < enemyHeight){
            //collision이 eIdx번째 enemy와 났다는 것을 임시 저장(뷰에 충돌을 적용하기 위한 데이터)
            bulletModels[bIdx].setCollision(eIdx)
            return true
        }
        return false
    }

    /*게임이 종료되었음을 사용자에게 알리기 위한 함수
      기본 life는 2인 상황에서 enemy를 놓칠 때마다 이 함수가 호출됨
      2번 이상 놓치는 경우 life가 0 이하가 되어 true를 반환 -> controller에서 게임의 종료를 탐지하고 종료*/
    fun isGameEnd():Boolean{
        life -= 1
        if(life <= 0)
            return true
        return false
    }
}