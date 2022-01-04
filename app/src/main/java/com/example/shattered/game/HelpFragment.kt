package com.example.shattered.game

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.shattered.MainActivity
import com.example.shattered.databinding.FragmentHelpBinding
import com.example.shattered.model.ShatteredViewModel

class HelpFragment : DialogFragment() {
    private var binding: FragmentHelpBinding? = null
    private val sharedViewModel: ShatteredViewModel by activityViewModels()

    companion object {
        const val TAG = "HelpFragment"
        fun newInstance() = HelpFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val fragmentBinding = FragmentHelpBinding.inflate(inflater, container, false)
        binding = fragmentBinding
        isCancelable = true

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
            helpFragment = this@HelpFragment
        }

        setupClickListeners()
        val animation = binding?.deathNote?.drawable as AnimationDrawable
        animation.start()
    }

    override fun onStart() {
        super.onStart()

        dialog?.window?.setLayout(
            sharedViewModel.displayWidth - (sharedViewModel.displayWidth * .05).toInt(),
            sharedViewModel.displayHeight - (sharedViewModel.displayHeight * .35).toInt()
        )

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).fullScreen()
    }

    private fun setupClickListeners() { binding?.closeHelp?.setOnClickListener { dismiss() } }
}