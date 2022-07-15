package com.example.shootinggame

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContentProviderCompat.requireContext
import com.example.shootinggame.Model.GameModel
import com.example.test.Model.Bullet
import com.example.test.Model.Cannon
import com.example.test.Model.Enemy
import kotlin.math.cos
import kotlin.math.sin

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //model - controller 연결됨
        val gameModel = GameModel(0.7F * resources.displayMetrics.widthPixels.toFloat(), resources.displayMetrics.widthPixels.toFloat())

        //view - controller 연결됨
        val gameStage = findViewById<ConstraintLayout>(R.id.game_stage)
        val cannon = findViewById<ImageView>(R.id.cannon)
        val rotateBar = findViewById<SeekBar>(R.id.rotate_bar)
        val fireButton = findViewById<ImageButton>(R.id.fire_button)
        val bullet1 = findViewById<ImageView>(R.id.bullet1)

        //대포의 회전 (seekbar.progress -> 0 ~ 100일 때 -90F ~ 90F)
        rotateBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                cannon.rotation = gameModel.cannonRotate(rotateBar.progress)
            }
            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })
        //탄환 발사
        fireButton.setOnClickListener{
            bullet1.x = gameModel.getMX()
            bullet1.y = gameModel.getMY()
            bullet1.visibility = View.VISIBLE
        }
    }
}
