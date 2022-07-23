package com.example.ShootingGame.model

/**
 * <화면 속 객체 타입들의 정보를 저장하는 데이터 클래스>
 *     게임 속 요소들은 모두 뷰로 표현될 때 타입에 따라 같은 크기를 가짐.
 *     대부분 imageView 활용으로 뷰를 표현하므로 리소스 id 역시 담음.
 *     따로 표현할 리소스 id가 없는 경우에 대비해 -1을 기본으로 가짐.
 */
data class ObjectInfo (
    val width: Int,
    val height: Int,
    val resId: Int = -1
)