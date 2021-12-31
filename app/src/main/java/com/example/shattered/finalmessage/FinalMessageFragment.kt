package com.example.shattered.finalmessage

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.*
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.shattered.MainActivity
import com.example.shattered.R
import com.example.shattered.databinding.FragmentFinalMessageBinding
import com.example.shattered.game.GameFragment
import com.example.shattered.model.AllLevelsItem
import com.example.shattered.model.CurrentLevelItem
import com.example.shattered.model.ShatteredViewModel

class FinalMessageFragment : DialogFragment() {
    private var binding: FragmentFinalMessageBinding? = null
    private val sharedViewModel: ShatteredViewModel by activityViewModels()

    companion object {
        const val TAG = "FinalMessageFragment"
        private const val KEY_TITLE = "KEY_TITLE"
        private const val KEY_SCORE = "KEY_SCORE"

        fun newInstance(title: String, score: Long): FinalMessageFragment {
            val args = Bundle()
            args.putString(KEY_TITLE, title)
            args.putLong(KEY_SCORE, score)
            val fragment = FinalMessageFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val fragmentBinding = FragmentFinalMessageBinding.inflate(inflater, container, false)
        binding = fragmentBinding
        isCancelable = cancelDialog()

        dialog?.window?.setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
        @Suppress("DEPRECATION")
        dialog?.window?.decorView?.systemUiVisibility = requireActivity().window.decorView.systemUiVisibility

        dialog?.setOnShowListener {
            dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)

            val wm = requireActivity().getSystemService(Context.WINDOW_SERVICE) as WindowManager
            wm.updateViewLayout(dialog?.window?.decorView, dialog?.window?.attributes)
        }
        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.apply {
            lifecycleOwner = viewLifecycleOwner
            finalMessageFragment = this@FinalMessageFragment
        }

        if (sharedViewModel.getStars().value!! < 1) {
            binding?.toNextLevel?.visibility = View.GONE
            binding?.tryAgain?.visibility = View.VISIBLE
        }

        //TODO: add argument? that takes a Boolean that says if the game was timed out, if true 0 stars and a score of 0
        sendResultsToDatabase()
        setupView()
        setStars()
        clickListeners()
    }

    override fun onStart() {
        super.onStart()
        val displayMetrics = DisplayMetrics()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) activity?.display?.getRealMetrics(displayMetrics)
        else @Suppress("DEPRECATION") activity?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)

        dialog?.window?.setLayout(
            displayMetrics.widthPixels - (displayMetrics.widthPixels * .2).toInt(),
            displayMetrics.heightPixels - (displayMetrics.heightPixels * .35).toInt()
        )

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).fullScreen()
    }

    private fun setupView() {
        binding?.levelHeaderFinal?.text = getString(R.string.level, arguments?.getString(KEY_TITLE))
        binding?.scoreValue?.text = arguments?.getLong(KEY_SCORE).toString()
    }

    private fun setStars() {
        when (sharedViewModel.getStars().value) {
            0 -> starsUI()
            1 -> starsUI(first = R.drawable.on_star_48dp)
            2 -> starsUI(first = R.drawable.on_star_48dp, second = R.drawable.on_star_48dp)
            3 -> starsUI(first = R.drawable.on_star_48dp, second = R.drawable.on_star_48dp, third = R.drawable.on_star_48dp)
        }
    }

    private fun starsUI(first: Int = R.drawable.off_star_48dp,
                        second: Int = R.drawable.off_star_48dp,
                        third: Int = R.drawable.off_star_48dp) {
        binding?.firstStarFinal?.setImageResource(first)
        binding?.secondStarFinal?.setImageResource(second)
        binding?.thirdStarFinal?.setImageResource(third)
    }

    private fun clickListeners() {
        binding?.toHome?.setOnClickListener {
            (parentFragment as GameFragment).resetGame()
            (parentFragment as GameFragment).navigateToMain()
        }

        binding?.toNextLevel?.setOnClickListener {
            sharedViewModel.setLevel(arguments?.getString(KEY_TITLE)!!.toInt() + 1)
            (parentFragment as GameFragment).gameSetup()
            (parentFragment as GameFragment).onResume()
            dismiss()
        }

        binding?.tryAgain?.setOnClickListener {
            (parentFragment as GameFragment).gameSetup()
            (parentFragment as GameFragment).onResume()
            dismiss()
        }

        binding?.closeFinal?.setOnClickListener {
            cancelDialog()
            dismiss()
        }
    }

    private fun sendResultsToDatabase() {
        val finalData = sharedViewModel.finalCurrentLevelData.value
        val levelReference = arguments?.getString(KEY_TITLE)!!
        val keyScore = arguments?.getLong(KEY_SCORE)!!.toInt()
        val score = if (keyScore > (finalData?.score ?: 0)) keyScore else finalData?.score ?: 0

        val numberOfStars = sharedViewModel.getStars().value!!
        val stars = if (numberOfStars > (finalData?.numberOfStars ?: 0)) numberOfStars else finalData?.numberOfStars ?: 0

        val allLevelsItem = AllLevelsItem(
            sharedViewModel.usernameData.value?.username!!,
            score,
            stars,
            levelReference.toInt()
        )

        sharedViewModel.updateDatabase(allLevelsItem, sharedViewModel.getLevelReference(levelReference))

        sharedViewModel.switchReplayLevel(sharedViewModel.currentLevelData.value?.currentLevel
                != levelReference.toInt())

        if (!sharedViewModel.replayLevelSwitch && stars >= 1) {
            val currentLevelsItem = CurrentLevelItem(levelReference.toInt() + 1)
            sharedViewModel.updateDatabase(currentLevelsItem, sharedViewModel.repository.currentLevelReference)
        }
    }

    private fun cancelDialog(): Boolean {
        (parentFragment as GameFragment).backButton()
        return true
    }
}