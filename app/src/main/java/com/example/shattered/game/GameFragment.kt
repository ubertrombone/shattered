package com.example.shattered.game

import android.annotation.SuppressLint
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.os.SystemClock
import android.view.*
import android.view.ViewGroup.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.shattered.MainActivity
import com.example.shattered.R
import com.example.shattered.databinding.FragmentGameBinding
import com.example.shattered.finalmessage.FinalMessageFragment
import com.example.shattered.model.ShatteredViewModel
import com.skydoves.balloon.*
import kotlin.math.roundToInt

//TODO: on easier levels randomly select a cell in each row and reveal the image beneath
//TODO: On easier levels, a left or right arrow will be kept and cells on the opposite side will be unclickable going forward
//TODO: Make level 1 a different fragment that uses balloons for tutorial
class GameFragment : Fragment() {

    private var binding: FragmentGameBinding? = null
    private val sharedViewModel: ShatteredViewModel by activityViewModels()
    private var perfectScore: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gameSetup()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val fragmentBinding = FragmentGameBinding.inflate(inflater, container, false)
        binding = fragmentBinding
        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.apply {
            lifecycleOwner = viewLifecycleOwner
            gameFragment = this@GameFragment
        }
    }

    override fun onResume() {
        super.onResume()
        binding?.boardLay?.removeAllViews()

        //TODO: Clean this mess up
        binding?.livesLayoutInGame?.visibility = if (sharedViewModel.getLives().value == 0) View.GONE else View.VISIBLE
        binding?.lives?.text = sharedViewModel.getLives().value.toString()
        binding?.scoreValueInGame?.text = sharedViewModel.getMoves().toString()
        binding?.scoreScore?.visibility = if (sharedViewModel.level >= 6) View.GONE else View.VISIBLE
        binding?.helpButton?.setOnClickListener { HelpFragment.newInstance(true)
            .show(childFragmentManager, HelpFragment.TAG) }
        binding?.timer?.visibility = if (sharedViewModel.getTimer() == 0L) View.GONE else View.VISIBLE
        if (binding?.timer?.isVisible == true) {
            binding?.timer?.base = SystemClock.elapsedRealtime() + sharedViewModel.getTimer()
            binding?.timer?.start()
            binding?.timer?.setOnChronometerTickListener { if (it.base <= SystemClock.elapsedRealtime()) {
                it.stop()
                FinalMessageFragment.newInstance(sharedViewModel.level.toString(), 0L, true)
                    .show(childFragmentManager, FinalMessageFragment.TAG)
            } }
        }
        val balloon = createBalloon(requireContext()) {
            setLayout(R.layout.correct_answer_tutorial)
            setArrowOrientation(ArrowOrientation.TOP)
            setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
            setArrowSize(10)
            setArrowPosition(0.5f)
            setWidth(BalloonSizeSpec.WRAP)
            setHeight(BalloonSizeSpec.WRAP)
            setCornerRadius(16f)
            setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.red))
            setBalloonAnimation(BalloonAnimation.ELASTIC)
            setLifecycleOwner(viewLifecycleOwner)
            setOnBalloonClickListener { Toast.makeText(context, "clicked", Toast.LENGTH_SHORT).show() }
            setOnBalloonDismissListener {
                Toast.makeText(context, "dismissed", Toast.LENGTH_SHORT).show()
                (activity as MainActivity).fullScreen()
            }
            build()
        }

        binding?.helpButton?.showAlignBottom(balloon)

        if (sharedViewModel.level == 1)
            HelpFragment.newInstance(false).show(childFragmentManager, HelpFragment.TAG)
        else startTime()

        sharedViewModel.fetchOneLevel(sharedViewModel.level.toString())

        setupBoard()
        enableDisableRows()
        observeScore()
        observeLives()
        observeStars()
        (activity as MainActivity).fullScreen()
    }

    private fun observeScore() {
        sharedViewModel.getScore().observe(viewLifecycleOwner) {
            val score = it.roundToInt()
            binding?.scoreValueInGame?.text = score.toString()
            when {
                score <= (perfectScore!!.times(.33)) -> sharedViewModel.updateStars(0)
                score <= (perfectScore!!.times(.4)) -> sharedViewModel.updateStars(1)
                score <= (perfectScore!!.times(.75)) -> sharedViewModel.updateStars(2)
                score > (perfectScore!!.times(.75)) -> sharedViewModel.updateStars(3)
            }
        }
    }

    private fun observeLives() {
        sharedViewModel.getLives().observe(viewLifecycleOwner) { binding?.lives?.text = it.toString() }
    }

    private fun observeStars() {
        sharedViewModel.getStars().observe(viewLifecycleOwner) {
            when (sharedViewModel.getStars().value) {
                0 -> setStars()
                1 -> setStars(first = R.drawable.on_star_24dp)
                2 -> setStars(first = R.drawable.on_star_24dp, second = R.drawable.on_star_24dp)
                3 -> setStars(first = R.drawable.on_star_24dp, second = R.drawable.on_star_24dp, third = R.drawable.on_star_24dp)
            }
        }
    }

    private fun setStars(
        first: Int = R.drawable.off_star_24dp,
        second: Int = R.drawable.off_star_24dp,
        third: Int = R.drawable.off_star_24dp
    ) {
        binding?.firstStarGame?.setImageResource(first)
        binding?.secondStarGame?.setImageResource(second)
        binding?.thirdStarGame?.setImageResource(third)
    }

    fun startTime() = sharedViewModel.setTimeIn(SystemClock.elapsedRealtime())

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun setupBoard() {
        var rowCounter = 0

        val params = RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        params.height = displayMetrics()
        params.width = displayMetrics()

        val mainLinearLayout = binding?.boardLay as LinearLayout
        (0 until sharedViewModel.getHeight()).forEach { i ->
            val rowLinearLayout = LinearLayout(context)
            rowLinearLayout.orientation = LinearLayout.HORIZONTAL

            // startIndicators operate in the range immediately after the height x width grid.
            val startId = sharedViewModel.getHeight() * sharedViewModel.getWidth() + rowCounter
            val startImage = setImage(R.drawable.indicator_start_highlighted)
            val startIndicatorCard = setupCardView(startId, params, startImage, View.INVISIBLE)
            rowLinearLayout.addView(startIndicatorCard)

            (0 until sharedViewModel.getWidth()).forEach { j ->
                val cardId = sharedViewModel.getWidth() * i + j
                val cardImage = setImage(R.drawable.game_piece_red_blue)
                val cardView = setupCardView(cardId, params, cardImage)

                if (sharedViewModel.level == 1) {
                    when (listOf(i, j)) {
                        listOf(1, 2) -> cardView.addView(setImage(R.drawable.game_piece_black))
                        listOf(2, 4) -> cardView.addView(setImage(R.drawable.game_piece_blue))
                        listOf(4, 0) -> setAnimationDrawable(cardView, R.drawable.deathnotered_infinite)
                    }
                }

                cardView.setOnClickListener {
                    val cardCoords = sharedViewModel.convertIdToCoords(cardView.id)
                    val rowNumber = cardCoords[1]
                    val row = sharedViewModel.getBoard()[rowNumber]
                    val cell = sharedViewModel.getBoard()[rowNumber][cardCoords[0]]
                    val listCheck = row.toList().subList(0, cardView.id % sharedViewModel.getWidth())

                    sharedViewModel.setTimeOnClick(SystemClock.elapsedRealtime())
                    changeGamePieceOnClick(cardView, cell, listCheck)
                    cellAction(cardView, cell, rowNumber, listCheck)
                    sharedViewModel.scoring(cell, rowNumber)
                    if (rowNumber == sharedViewModel.getHeight() - 1 && cell == "1") {
                        if (binding?.timer?.isVisible == true) binding?.timer?.stop()
                        FinalMessageFragment.newInstance(sharedViewModel.level.toString(),
                            sharedViewModel.getScore().value!!.roundToInt().toLong(), false)
                            .show(childFragmentManager, FinalMessageFragment.TAG)
                    }
                }
                rowLinearLayout.addView(cardView)
            }
            // endIndicators operate in the range immediately after startIndicators grid.
            val endId = sharedViewModel.getHeight() * sharedViewModel.getWidth() + rowCounter + sharedViewModel.getHeight()
            val endImage = setImage(R.drawable.indicator_end_highlighted)
            val endIndicatorCard = setupCardView(endId, params, endImage, View.INVISIBLE)
            rowLinearLayout.addView(endIndicatorCard)

            mainLinearLayout.addView(rowLinearLayout)
            rowCounter ++
        }
    }

    private fun displayMetrics() = ((sharedViewModel.displayWidth - (sharedViewModel.displayWidth * .05)) /
            (sharedViewModel.getWidth() + 2)).roundToInt()

    private fun enableDisableRows(row: Int = 0) {
        var rowCounter = 0
        sharedViewModel.getBoard().forEach { i ->
            var columnCounter = 0
            indicatorVisibility(rowCounter, row)

            i.forEach { _ ->
                val cardId = sharedViewModel.getWidth() * rowCounter + columnCounter
                val card = requireView().findViewById<CardView>(cardId)
                card.isClickable = rowCounter == row
                card.alpha = if (rowCounter == row) 1F else .75F

                val tutorialList = sharedViewModel.getTutorialList()
                if (sharedViewModel.level == 1 && tutorialList.contains(sharedViewModel.convertIdToCoords(cardId))) {
                    card.isClickable = false
                    card.alpha = 1F
                }
                columnCounter ++
            }
            rowCounter ++
        }
    }

    private fun resetRowPieces(rowCoords: Int = 0, offset: Int = 0) {
        var columnCounter = 0
        sharedViewModel.getBoard()[rowCoords + offset].forEach { _ ->
            val cardId = sharedViewModel.getWidth() * (rowCoords + offset) + columnCounter
            val tutorialList = sharedViewModel.getTutorialList()
            if (sharedViewModel.level == 1 && tutorialList.contains(sharedViewModel.convertIdToCoords(cardId)))
                return@forEach
            setGamePiece(cardId)
            columnCounter ++
        }
    }

    private fun setGamePiece(id: Int) {
        val card = requireView().findViewById<CardView>(id)
        card.removeAllViews()
        val newImage = setImage(R.drawable.game_piece_red_blue)
        card.addView(newImage)
    }

    private fun setupCardView(
        cardId: Int,
        params: LayoutParams,
        image: ImageView,
        viewVisibility: Int = View.VISIBLE
    ) = CardView(requireContext()).apply {
        id = cardId
        setBackgroundColor(requireContext().getColor(R.color.black))
        setContentPadding(10, 10, 10, 10)
        layoutParams = params
        visibility = viewVisibility
        addView(image)
    }
    private fun setImage(indicator: Int) = ImageView(requireContext()).apply { setImageResource(indicator) }

    private fun indicatorVisibility(rowCounter: Int, row: Int) {
        val startIndicator = requireView()
            .findViewById<CardView>(sharedViewModel.getWidth() * sharedViewModel.getHeight() + rowCounter)
        val endIndicator = requireView()
            .findViewById<CardView>(sharedViewModel.getWidth() * sharedViewModel.getHeight()
                    + rowCounter + sharedViewModel.getHeight())

        startIndicator.visibility = if (rowCounter == row) View.VISIBLE else View.INVISIBLE
        endIndicator.visibility = if (rowCounter == row) View.VISIBLE else View.INVISIBLE
        setAnimationDrawable(startIndicator, R.drawable.indicator_start_blinking)
        setAnimationDrawable(endIndicator, R.drawable.indicator_end_blinking)
    }

    private fun changeGamePieceOnClick(card: CardView, cell: String, listCheck: List<String>) {
        card.removeAllViews()
        val selectedGamePiece = setImage(when {
            cell == "0" ->
                if (listCheck.contains("1")) R.drawable.game_piece_blue else R.drawable.game_piece_red
            cell == "1" -> R.drawable.game_piece_black // right answer
            listCheck.contains("1") -> R.drawable.game_piece_blue // answer to the left
            else -> R.drawable.game_piece_red // answer to the right
        })
        card.addView(selectedGamePiece)
    }

    private fun cellAction(card: CardView, cell: String, coordinate: Int, listCheck: List<String>) {
        when (cell) {
            "1" -> oneSelected(coordinate)
            "2" -> twoSelected(coordinate)
            "3" -> threeSelected(card, listCheck, coordinate)
        }
    }

    private fun oneSelected(coordinate: Int) {
        enableDisableRows(row = if (sharedViewModel.level == 1 && coordinate == 0) 2 else coordinate + 1)
        if (coordinate == sharedViewModel.getHeight() - 1) sharedViewModel.setTimeOut(SystemClock.elapsedRealtime())
        else resetRowPieces(rowCoords = coordinate, offset = if (sharedViewModel.level == 1 && coordinate == 0) 2 else 1)
    }

    private fun twoSelected(coordinate: Int) {
        if (coordinate > 0) {
            enableDisableRows(row = coordinate - 1)
            resetRowPieces(rowCoords = coordinate, offset = -1)
        }
    }

    private fun threeSelected(card: CardView, listCheck: List<String>, coordinate: Int) {
        sharedViewModel.removeLives(1)
        val drawable = if (listCheck.contains("1")) R.drawable.deathnoteblue else R.drawable.deathnotered
        setAnimationDrawable(card, drawable)
        enableDisableRows()
        if (coordinate != 0) sharedViewModel.getBoard()[0].forEach { _ -> resetRowPieces() }
    }

    private fun setAnimationDrawable(card: CardView, drawable: Int) {
        val animationImage = setImage(drawable)
        val animation = animationImage.drawable as AnimationDrawable
        card.removeAllViews()
        card.addView(animationImage)
        animation.start()
    }

    fun resetGame() = sharedViewModel.resetGame()

    fun gameSetup() {
        sharedViewModel.resetGame()
        sharedViewModel.createBoard(sharedViewModel.level)
        sharedViewModel.getHeightAndWidth(sharedViewModel.getHeight(), sharedViewModel.getWidth())
        perfectScore = sharedViewModel.perfectScore()
    }

    fun navigateToMain() = findNavController().navigate(R.id.action_gameFragment_to_levelsFragment)

    fun backButton() {
        hideBackButton(true)
        binding?.goBack?.setOnClickListener { navigateToMain() }
    }

    fun hideBackButton(visibility: Boolean) { binding?.goBack?.visibility = if (visibility) View.VISIBLE else View.GONE }
}