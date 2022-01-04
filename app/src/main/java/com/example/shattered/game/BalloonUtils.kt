package com.example.shattered.game

import android.app.Activity
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.shattered.MainActivity
import com.example.shattered.R
import com.skydoves.balloon.*

object BalloonUtils {

    fun getCorrectAnswerAndReturnRowBalloons(
        context: Context,
        lifecycleOwner: LifecycleOwner,
        layout: Int
    ) = createBalloon(context) {
                setLayout(layout)
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
                build()
            }

    fun getLeftRightBalloon(context: Context, lifecycleOwner: LifecycleOwner) = createBalloon(context) {
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
        build()
    }

    fun getDeathBalloon(
        context: Context,
        lifecycleOwner: LifecycleOwner,
        activity: Activity
    ) = createBalloon(context) {
        setLayout(R.layout.death_tutorial)
        setArrowOrientation(ArrowOrientation.LEFT)
        setArrowPositionRules(ArrowPositionRules.ALIGN_BALLOON)
        setArrowSize(15)
        setArrowPosition(0.5f)
        setWidth(BalloonSizeSpec.WRAP)
        setHeight(BalloonSizeSpec.WRAP)
        setCornerRadius(16f)
        setBackgroundColor(ContextCompat.getColor(context, R.color.light_red))
        setBalloonAnimation(BalloonAnimation.ELASTIC)
        setLifecycleOwner(lifecycleOwner)
        setOnBalloonDismissListener { (activity as MainActivity).fullScreen() }
        build()
    }

}