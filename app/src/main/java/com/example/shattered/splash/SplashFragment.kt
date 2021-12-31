package com.example.shattered.splash

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.shattered.MainActivity
import com.example.shattered.R
import com.example.shattered.model.ShatteredViewModel

class SplashFragment : Fragment() {

    private val sharedViewModel: ShatteredViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        findNavController().navigate(
            if (sharedViewModel.repository.auth.currentUser != null) R.id.action_splashFragment_to_levelsFragment
            else R.id.action_splashFragment_to_loginFragment
        )
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).fullScreen()
    }
}