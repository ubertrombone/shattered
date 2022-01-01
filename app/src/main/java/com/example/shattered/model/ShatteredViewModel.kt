package com.example.shattered.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DatabaseReference
import kotlin.random.Random

class ShatteredViewModel: ViewModel() {

    private val _repository = ShatteredRepository()
    val repository: ShatteredRepository
        get() = _repository

    private val _usernameData = MutableLiveData<UsernameItem>()
    val usernameData: LiveData<UsernameItem>
        get() = _usernameData

    private val _currentLevelData = MutableLiveData<CurrentLevelItem>()
    val currentLevelData: LiveData<CurrentLevelItem>
        get() = _currentLevelData

    private val _listOfAllUsernamesData = MutableLiveData<List<String>>()
    val listOfAllUsernamesData: LiveData<List<String>>
        get() = _listOfAllUsernamesData

    private val _listOfAllLevelsData = MutableLiveData<List<AllLevelsItem>>()
    val listOfAllLevelsData: LiveData<List<AllLevelsItem>>
        get() = _listOfAllLevelsData

    private val _listOfAllPlayersOnLevelData = MutableLiveData<List<AllLevelsItem>>()
    val listOfAllPlayersOnLevelData: LiveData<List<AllLevelsItem>>
        get() = _listOfAllPlayersOnLevelData

    private val _finalCurrentLevelData = MutableLiveData<AllLevelsItem>()
    val finalCurrentLevelData: LiveData<AllLevelsItem>
        get() = _finalCurrentLevelData

    fun fetchUsername() { _repository.fetchUsername(_usernameData) }
    fun fetchCurrentLevel() { _repository.fetchCurrentLevel(_currentLevelData) }
    fun fetchAllUsernames() { _repository.fetchAllUsernames(_listOfAllUsernamesData) }
    fun fetchAllLevels() { _repository.fetchAllLevels(_listOfAllLevelsData) }
    fun fetchOneLevel(reference: String) { _repository.fetchOneLevel(_finalCurrentLevelData, reference) }
    fun fetchAllPlayersOnLevel(level: String) { _repository.fetchAllPlayersOnLevel(_listOfAllPlayersOnLevelData, level) }
    fun updateDatabase(value: Any, databaseReference: DatabaseReference) { _repository.updateDatabase(value, databaseReference) }
    fun getLevelReference(reference: String): DatabaseReference = _repository.getLevelReference(reference)

    private val profanityFilter = listOf("fuck", "f4ck", "f4k", "fck", "fuk", "shit", "sh1t", "shiz", "sh1z", "bitch", "b1tch",
        "biatch", "b1atch", "bi4tch", "b14tch", "cunt", "kunt", "ass", "4ss", "balls", "ballz", "b4lls", "b4llz", "dick",
        "d1ck", "dik", "d1k", "dic", "d1c", "fuc", "f4k", "f4c", "vagina", "v4gina", "v4g1na", "v4g1n4", "v4gin4", "vagin4",
        "vag1na", "vag1n4", "pussy", "pusy", "pussee", "pusee", "tits", "t1ts", "titz", "t1tz", "titties", "t1tties")

    fun checkProfanityFilter(string: String):Boolean {
        profanityFilter.forEach { if (string.contains(it)) return true }
        return false
    }

    private var _displayWidth: Int = 0
    val displayWidth: Int
        get() = _displayWidth

    private var _displayHeight: Int = 0
    val displayHeight: Int
        get() = _displayHeight

    fun setDisplayHeightAndWith(height: Int, width: Int) {
        _displayHeight = height
        _displayWidth = width
    }

    private var _replayLevelSwitch = false
    val replayLevelSwitch: Boolean
        get() = _replayLevelSwitch

    fun switchReplayLevel(switch: Boolean) { _replayLevelSwitch = switch }

    // TODO: run some functions that randomly select each level type going 20 deep at a time
    // TODO: 5 Easy in 20 set (shows some cells at start and doesn't clear each row); 8 Medium (shows some cells at start); 5 Hard (Show very few cells); 2 expert (no hints, lives and/or timer guaranteed)
    // TODO: 15% Lives; 5% Timed; Neither
    // TODO: Remove this function after running a few times
    fun setupLevels() {
        var levelCounter = 41
        var lives: Boolean
        var timer: Boolean
        val complexities = mutableMapOf("easy" to 5, "medium" to 8, "hard" to 5, "expert" to 2)
        (0 until 20).forEach { _ ->
            val random = Random
            val randomElement = complexities.entries.elementAt(random.nextInt(complexities.size))

            val probabilityDigitForLives = random.nextInt(100)
            lives = probabilityDigitForLives < 15

            val probabilityDigitForTimer = random.nextInt(100)
            timer = probabilityDigitForTimer < 5

            complexities[randomElement.key] = randomElement.value - 1
            if (complexities[randomElement.key] == 0) complexities.remove(randomElement.key)
            if (randomElement.key == "expert") {
                lives = true
                timer = true
            }

            val value = LevelMeta(levelCounter, randomElement.key, lives, timer)
            val ref = _repository.database.getReference("levels")
            ref.child(levelCounter.toString()).setValue(value)

            levelCounter ++
        }
    }

    private var _level = 1
    val level: Int
        get() = _level

    fun setLevel(level: Int) { _level = level }

    private var _gameBoard = GameBoard()

    fun getWidth() = _gameBoard.width
    fun getHeight() = _gameBoard.height
    fun getBoard() = _gameBoard.board
    fun getLives() = _gameBoard.lives
    fun getTimer() = _gameBoard.timer
    fun removeLives(amount: Int) = _gameBoard.removeLives(amount)
    fun createBoard(level: Int) = _gameBoard.setupBoard(level)
    fun convertIdToCoords(id: Int) = _gameBoard.convertCardIDToCoords(id)

    private var _score = Scoring()
    val score: Scoring
        get() = _score

    fun getMoves() = _score.moves
    fun getScore() = _score.score
    fun getStars() = _score.stars
    fun updateStars(value: Int) = _score.updateStars(value)
    fun setTimeIn(time: Long) = _score.setTimeIn(time)
    fun setTimeOnClick(time: Long) = _score.setTimeOnClick(time)
    fun setTimeOut(time: Long) = _score.setTimeOut(time)
    fun scoring(cell: String, rowNumber: Int) = _score.scoring(cell, rowNumber)
    fun getHeightAndWidth(height: Int, width: Int) = _score.getGameHeightAndWidth(height, width)
    fun perfectScore() = _score.calculatePerfectScore()

    fun resetGame() = _score.resetScore()
}