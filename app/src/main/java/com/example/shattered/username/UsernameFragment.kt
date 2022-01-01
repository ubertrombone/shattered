package com.example.shattered.username

import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.shattered.MainActivity
import com.example.shattered.R
import com.example.shattered.databinding.FragmentUsernameBinding
import com.example.shattered.levels.LevelsFragment
import com.example.shattered.model.ShatteredViewModel
import com.example.shattered.model.UsernameItem
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class UsernameFragment : DialogFragment() {
    private val sharedViewModel: ShatteredViewModel by activityViewModels()
    private var binding: FragmentUsernameBinding? = null

    companion object {
        const val TAG = "UsernameFragment"
        fun newInstance(): UsernameFragment = UsernameFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val fragmentBinding = FragmentUsernameBinding.inflate(inflater, container, false)
        binding = fragmentBinding
        isCancelable = false

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
            usernameFragment = this@UsernameFragment
        }

        binding?.username?.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                confirmUsername()
                return@setOnEditorActionListener true
            }
            false
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            sharedViewModel.displayWidth - (sharedViewModel.displayWidth * .2).toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).fullScreen()
    }

    private fun confirmUsername() {
        val usernameInput = binding?.username
        val usernameField = binding?.usernameField

        if (usernameInput?.text.toString().isEmpty()) {
            inputEmpty(usernameInput!!, usernameField!!, true)
            return
        } else inputEmpty(usernameInput!!, usernameField!!, false)

        if (sharedViewModel.checkProfanityFilter(usernameInput.text.toString())) {
            usernameProfane(usernameInput, usernameField, true)
            return
        } else usernameProfane(usernameInput, usernameField, false)

        sharedViewModel.fetchAllUsernames()
        sharedViewModel.listOfAllUsernamesData.observe(viewLifecycleOwner) {
            if (!it.contains(usernameInput.text.toString())) {
                sharedViewModel.updateDatabase(
                    UsernameItem(binding?.username?.text.toString()),
                    sharedViewModel.repository.usernameReference)
                usernameExists(usernameField, false)

                (parentFragment as LevelsFragment).hideCentralImage(false)
                (parentFragment as LevelsFragment).onResume()
                dismiss()
            } else {
                usernameExists(usernameField, true)
                return@observe
            }
        }
    }

    private fun inputEmpty(input: TextInputEditText, field: TextInputLayout, error: Boolean) {
        errorCheck(error, input.text.isNullOrEmpty(), field, R.string.choose_username)
    }

    private fun usernameExists(field: TextInputLayout, errorAndCondition: Boolean) {
        errorCheck(errorAndCondition, errorAndCondition, field, R.string.username_exists)
    }

    private fun usernameProfane(input: TextInputEditText, field: TextInputLayout, error: Boolean) {
        errorCheck(error, sharedViewModel.checkProfanityFilter(input.text.toString()), field, R.string.profanity_filter)
    }

    private fun errorCheck(error: Boolean, condition: Boolean, field: TextInputLayout, string: Int) {
        if (error) {
            if (condition) {
                field.isErrorEnabled = true
                field.error = getString(string)
            } else field.isErrorEnabled = false
        } else {
            if (!condition) field.isErrorEnabled = false
        }
    }
}