package com.example.shattered.levels

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.shattered.MainActivity
import com.example.shattered.R
import com.example.shattered.databinding.FragmentLevelsBinding
import com.example.shattered.model.ShatteredViewModel
import com.example.shattered.username.UsernameFragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

class LevelsFragment : Fragment(), View.OnClickListener {

    private var binding: FragmentLevelsBinding? = null
    private val sharedViewModel: ShatteredViewModel by activityViewModels()
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val fragmentBinding = FragmentLevelsBinding.inflate(inflater, container, false)
        binding = fragmentBinding
        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.apply {
            lifecycleOwner = viewLifecycleOwner
            levelsFragment = this@LevelsFragment
        }

        clickListeners()

        sharedViewModel.fetchUsername()
        usernameObserver()

        sharedViewModel.fetchCurrentLevel()
        currentLevelObserver()
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).fullScreen()
    }

    private fun usernameObserver() {
        sharedViewModel.usernameData.observe(viewLifecycleOwner) {
            if (it.username == null) {
                hideCentralImage(true)
                UsernameFragment.newInstance().show(childFragmentManager, UsernameFragment.TAG)
            }
        }
    }

    private fun currentLevelObserver() {
        sharedViewModel.currentLevelData.observe(viewLifecycleOwner) {
            val level = it.currentLevel
            binding?.level?.text = level.toString()
            sharedViewModel.setLevel(level ?: 0)
            binding?.centralImage?.setOnClickListener {
                findNavController().navigate(R.id.action_levelsFragment_to_gameFragment)
            }
        }
    }

    private fun clickListeners() {
        binding?.fabMenuActions?.setOnClickListener { setupFabButtons() }
        //TODO: makes these lead to new Fragments
        binding?.levelsFab?.setOnClickListener {
            findNavController().navigate(R.id.action_levelsFragment_to_allLevelsFragment)
        }
        binding?.recordsFab?.setOnClickListener { Toast.makeText(context, "RECORDS", Toast.LENGTH_SHORT).show() }

        binding?.signOut?.setOnClickListener(this)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
    }

    private fun setupFabButtons() {
        if (binding?.levelsFab?.visibility == View.VISIBLE) {
            binding?.recordsFab?.visibility = View.GONE
            binding?.recordsFabText?.visibility = View.GONE
            binding?.levelsFab?.visibility = View.GONE
            binding?.levelsFabText?.visibility = View.GONE
        } else {
            binding?.recordsFab?.visibility = View.VISIBLE
            binding?.recordsFabText?.visibility = View.VISIBLE
            binding?.levelsFab?.visibility = View.VISIBLE
            binding?.levelsFabText?.visibility = View.VISIBLE
        }
    }

    private fun signOut() {
        sharedViewModel.repository.auth.signOut()
        try {
            googleSignInClient.signOut().addOnCompleteListener(requireActivity()) { navigateToLogin() }
        } catch (_: Exception) {}
    }

    override fun onClick(v: View) { if (v.id == R.id.sign_out) signOut() }
    private fun navigateToLogin() = findNavController().navigate(R.id.action_levelsFragment_to_loginFragment)
    fun hideCentralImage(hide: Boolean) {
        binding?.centralImage?.visibility = when (hide) {
            true -> View.GONE
            false -> View.VISIBLE
        }
    }
}