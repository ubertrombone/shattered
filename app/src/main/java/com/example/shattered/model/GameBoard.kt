package com.example.shattered.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.shattered.model.XCases.*
import kotlin.random.Random

open class GameBoard {

    private var _width = 3
    val width: Int
        get() = _width

    private var _height = 50
    val height: Int
        get() = _height

    private var _lives = MutableLiveData<Int>()
    val lives: LiveData<Int>
        get() = _lives

    private var _timer = 0L
    val timer: Long
        get() = _timer

    private var _board = arrayOf<Array<String>>()
    val board: Array<Array<String>>
        get() = _board

    fun setupBoard(level: Int) {
        adjustHeightWidth(level)
        setLives(level)
        setTimer(level)
        _board = createBoard(level)
        _board.forEach { i ->
            println(i.contentToString())
        }
    }

    //TODO: Experiment with checkpoints and not making info always disappear
    private fun adjustHeightWidth(level: Int) {
        if (level <= 5) {
            _width = 5
            _height = 5
        } else {
            _width = 7
            _height = 7
        }
    }

    private fun setLives(level: Int) { _lives.value = if (level <= 5) 0 else 3 }
    // fun addLives(amount: Int) { _lives.value = _lives.value?.plus(amount) }
    fun removeLives(amount: Int) { _lives.value = _lives.value?.minus(amount) }

    private fun setTimer(level: Int) { _timer = if (level <= 5) 0 else 100000 }

    private fun createBoard(level: Int): Array<Array<String>> {
        val list = mutableListOf<String>()
        val random = Random
        return Array(_height) { createRowArrays(list, random, level) }
    }

    // TODO: In levels with lives, only 1 or 2 of each possible number?
    // TODO: if randomDigit is 0, 2, or 3 and if a level is X, there is a chance that that cell can be shown on start
    private fun createRowArrays(list: MutableList<String>, random: Random, level: Int): Array<String> {
        list.clear()
        list.add("1")
        do {
            // maximum of 3 for now
            val randomDigit = random.nextInt(0, 4)
            if (randomDigit == 1) continue
            // if (list.contains(randomDigit.toString())) continue
            list.add(randomDigit.toString())
        } while (list.size < _width)
        return list.shuffled().toTypedArray()
    }

    private fun getXCase(digit: Int) = when (digit) {
        0 -> ZERO.x
        1 -> ONE.x
        2 -> TWO.x
        3 -> THREE.x
        4 -> FOUR.x
        5 -> FIVE.x
        6 -> SIX.x
        7 -> SEVEN.x
        8 -> EIGHT.x
        9 -> NINE.x
        else -> ONE.x
    }

    fun convertCardIDToCoords(id: Int): List<Int> {
        val listOfXStarts = getXMinValues()
        val listOfXEnds = getXMaxValues()

        return when {
            listOfXStarts.contains(id) -> listOf(0, listOfXStarts.indexOf(id)) // If number should be in column 0
            listOfXEnds.contains(id) -> listOf(_width - 1, listOfXEnds.indexOf(id)) // if number should be in column _height - 1
            else -> listOf(id % _width, id / _width) // For all other numbers.
        }
    }
    private fun getXMinValues() = (0 until _height).map { it * _width }
    private fun getXMaxValues() = (0 until _height).map { it * _width + _width - 1 }

}