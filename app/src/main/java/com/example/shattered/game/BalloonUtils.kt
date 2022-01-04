package com.example.shattered.game

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.shattered.MainActivity
import com.example.shattered.R
import com.skydoves.balloon.*

object BalloonUtils {

    fun getCorrectAnswerBalloon(
        context: Context,
        lifecycleOwner: LifecycleOwner,
        activity: Activity
    ): Balloon {
        return createBalloon(context) {
            setLayout(R.layout.correct_answer_tutorial)
            setArrowOrientation(ArrowOrientation.TOP)
            setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
            setArrowSize(20)
            setArrowPosition(0.5f)
            setWidth(BalloonSizeSpec.WRAP)
            setHeight(BalloonSizeSpec.WRAP)
            setCornerRadius(16f)
            setBackgroundColor(ContextCompat.getColor(context, R.color.red))
            setBalloonAnimation(BalloonAnimation.ELASTIC)
            setLifecycleOwner(lifecycleOwner)
            setOnBalloonClickListener { Toast.makeText(context, "clicked", Toast.LENGTH_SHORT).show() }
            setOnBalloonDismissListener {
                Toast.makeText(context, "dismissed", Toast.LENGTH_SHORT).show()
                (activity as MainActivity).fullScreen()
            }
            build()
        }
    }

    fun getLeftRightBalloon(
        context: Context,
        lifecycleOwner: LifecycleOwner,
        activity: Activity
    ): Balloon {
        return createBalloon(context) {
            setLayout(R.layout.left_right_tutorial)
            setArrowOrientation(ArrowOrientation.RIGHT)
            setArrowPositionRules(ArrowPositionRules.ALIGN_BALLOON)
            setArrowSize(15)
            setArrowPosition(0.5f)
            setWidth(BalloonSizeSpec.WRAP)
            setHeight(BalloonSizeSpec.WRAP)
            setCornerRadius(16f)
            setBackgroundColor(ContextCompat.getColor(context, R.color.light_red))
            setBalloonAnimation(BalloonAnimation.ELASTIC)
            setLifecycleOwner(lifecycleOwner)
            setOnBalloonClickListener { Toast.makeText(context, "clicked", Toast.LENGTH_SHORT).show() }
            setOnBalloonDismissListener {
                Toast.makeText(context, "dismissed", Toast.LENGTH_SHORT).show()
                (activity as MainActivity).fullScreen()
            }
            build()
        }
    }

}