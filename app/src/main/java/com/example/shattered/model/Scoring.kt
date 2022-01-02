package com.example.shattered.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.util.concurrent.TimeUnit

class Scoring {

    private var _height = 50
    val height: Int
        get() = _height

    private var _width = 50
    val width: Int
        get() = _width

    private var _timeMultiplier = 50
    private var _rowNumber = 0
    private var _consecutiveUniqueOnes = 0

    fun getGameHeightAndWidth(height: Int, width: Int) {
        _height = height
        _width = width
        _timeMultiplier = height
    }

    private var _score = MutableLiveData<Double>()
    val score: LiveData<Double>
        get() = _score

    private fun changeScore(double: Double) { _score.value = _score.value?.plus(double) ?: 0.0 }

    private var _moves = 0
    val moves: Int
        get() = _moves

    private fun movesInc() = _moves ++

    private var _stars = MutableLiveData<Int>()
    val stars: LiveData<Int>
        get() = _stars

    fun updateStars(value: Int) { _stars.value = value }

    private var _timeIn: Long = 0
    private var _timeOnClick: Long = 0
    private var _timeOut: Long = 0

    fun setTimeIn(time: Long) { _timeIn = time }
    fun setTimeOnClick(time: Long) { _timeOnClick = time }
    fun setTimeOut(time: Long) { _timeOut = time }
    private fun getTimeDiff(timeNext: Long) = TimeUnit.MILLISECONDS.toSeconds(timeNext - _timeIn)

    fun scoring(cell: String, rowNumber: Int, level: Int) {
        onesMultiplier(cell, rowNumber)
        movesInc()
        timeMultiplier(cell)
        // Ones Multiplier should be 1 at a minimum when calculating the move score
        val moveOnesMultiplier = if (_consecutiveUniqueOnes == 0) 1 else _consecutiveUniqueOnes
        // score formula: (move score X time multiplier X Ones multiplier) + time Bonus (calculated at end of game) +
        // (correct answer bonus X Ones multiplier).
        val newPoints = (moveScore() * _timeMultiplier * moveOnesMultiplier) + timeBonus() +
                (correctAnswerBonus(rowNumber) * _consecutiveUniqueOnes)
        val penalty = penalty(cell, _score.value?.plus(newPoints)!!)
        val levelOneBonus = if (level == 1) levelOneBonus() else 0.0
        changeScore(newPoints - penalty + levelOneBonus)
        if (cell == "1" && rowNumber == _rowNumber) _rowNumber ++
    }

    private fun moveScore():Double = if (_moves <= _height * 3) 10.0 else {
        if (10 - ((_moves - _height * 3) * .1) < 1) 0.0 else 10 - ((_moves - _height * 3) * .1)
    }

    private fun timeMultiplier(cell: String): Double = if (_width <= 5) {
        val timeDiff = getTimeDiff(_timeOnClick)
        if (timeDiff % 15 == 0L && timeDiff != 0L) _timeMultiplier --
        if (cell != "1") 1.0 else _timeMultiplier.toDouble()
    } else 1.0

    private fun onesMultiplier(cell: String, rowNumber: Int) {
        if (cell != "1") {
            _consecutiveUniqueOnes = 0
            return
        }
        if (rowNumber >= _rowNumber) _consecutiveUniqueOnes ++
    }

    private fun penalty(cell: String, score: Double): Double = when (cell) {
        "3" -> score * .1
        "2" -> score * .05
        else -> 0.0
    }

    private fun timeBonus(): Double {
        val timeDiff = getTimeDiff(_timeOut)
        if (_timeOut == 0L) return 0.0
        return when {
            timeDiff <= _height * 5 -> _height * 100.0
            timeDiff <= _height * 6 -> _height * 50.0
            timeDiff <= _height * 7 -> _height * 25.0
            else -> _height * 10.0
        }
    }

    private fun levelOneBonus(): Double {
        if (_timeOut == 0L) return  0.0
        return 7000.0
    }

    private fun correctAnswerBonus(rowNumber: Int) = if (rowNumber == _rowNumber) _height * 100.0 else 0.0

    fun calculatePerfectScore(): Int {
        simulateConsecutiveOnesInMoves = _height
        calculatePerfectMoveScore()
        simulateConsecutiveOnesInOnesBonus = _height
        calculatePerfectConsecutiveOnesBonus()
        return perfectMoveScore + perfectOnesScore + 500
    }

    private var simulateConsecutiveOnesInMoves = 50
    private var perfectMoveScore = 0
    private fun calculatePerfectMoveScore() {
        if (simulateConsecutiveOnesInMoves >= 1) {
            perfectMoveScore += 10 * _height * simulateConsecutiveOnesInMoves
            simulateConsecutiveOnesInMoves --
            calculatePerfectMoveScore()
        }
        return
    }

    private var simulateConsecutiveOnesInOnesBonus = 50
    private var perfectOnesScore = 0
    private fun calculatePerfectConsecutiveOnesBonus() {
        if (simulateConsecutiveOnesInOnesBonus >= 1) {
            perfectOnesScore += _height * 100 * simulateConsecutiveOnesInOnesBonus
            simulateConsecutiveOnesInOnesBonus --
            calculatePerfectConsecutiveOnesBonus()
        }
        return
    }

    fun resetScore() {
        _score.value = 0.0
        _moves = 0
        _consecutiveUniqueOnes = 0
        _rowNumber = 0
        _timeMultiplier = 50
        _timeIn = 0
        _timeOnClick = 0
        _timeOut = 0
        _stars.value = 0
        perfectMoveScore = 0
        perfectOnesScore = 0
    }
}