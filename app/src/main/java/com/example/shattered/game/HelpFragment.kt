package com.example.shattered.game

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.*
import androidx.fragment.app.DialogFragment
import com.example.shattered.databinding.FragmentHelpBinding

class HelpFragment : DialogFragment() {
    private var binding: FragmentHelpBinding? = null

    companion object {
        const val TAG = "HelpFragment"
        private const val KEY_BOOL = "KEY_BOOL"

        fun newInstance(xOrArrow: Boolean): HelpFragment {
            val args = Bundle()
            args.putBoolean(KEY_BOOL, xOrArrow)
            val fragment = HelpFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val fragmentBinding = FragmentHelpBinding.inflate(inflater, container, false)
        binding = fragmentBinding
        isCancelable = arguments?.getBoolean(KEY_BOOL)!!

        dialog?.window?.setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
        @Suppress("DEPRECATION")
        dialog?.window?.decorView?.systemUiVisibility = requireActivity().window.decorView.systemUiVisibility

        dialog?.setOnShowListener { //Clear the not focusable flag from the window
            dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)

            //Update the WindowManager with the new attributes (no nicer way I know of to do this)..
            val wm = requireActivity().getSystemService(Context.WINDOW_SERVICE) as WindowManager
            wm.updateViewLayout(dialog?.window?.decorView, dialog?.window?.attributes)
        }
        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.apply {
            lifecycleOwner = viewLifecycleOwner
            helpFragment = this@HelpFragment
        }

        setupView()
        setupClickListeners()
        val animation = binding?.deathNote?.drawable as AnimationDrawable
        animation.start()
    }

    override fun onStart() {
        super.onStart()
        val displayMetrics = DisplayMetrics()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) activity?.display?.getRealMetrics(displayMetrics)
        else @Suppress("DEPRECATION") activity?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)

        dialog?.window?.setLayout(
            displayMetrics.widthPixels - (displayMetrics.widthPixels * .05).toInt(),
            displayMetrics.heightPixels - (displayMetrics.heightPixels * .35).toInt()
        )

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val insetsController = view?.windowInsetsController ?: return
            insetsController.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            insetsController.hide(WindowInsets.Type.systemBars())
        } else {
            @Suppress("DEPRECATION")
            view?.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN)
        }
    }

    private fun setupView() {
        binding?.closeHelp?.visibility = if (arguments?.getBoolean(KEY_BOOL)!!) View.VISIBLE else View.GONE
        binding?.readHelp?.visibility = if (arguments?.getBoolean(KEY_BOOL)!!) View.GONE else View.VISIBLE
    }

    private fun setupClickListeners() {
        binding?.closeHelp?.setOnClickListener { dismiss() }
        binding?.readHelp?.setOnClickListener {
            (parentFragment as GameFragment).startTime()
            dismiss()
        }
    }
}