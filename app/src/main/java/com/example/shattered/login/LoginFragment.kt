package com.example.shattered.login

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.shattered.MainActivity
import com.example.shattered.R
import com.example.shattered.databinding.FragmentLoginBinding
import com.example.shattered.model.CurrentLevelItem
import com.example.shattered.model.ShatteredViewModel
import com.example.shattered.username.UsernameFragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider

class LoginFragment : Fragment(), View.OnClickListener {

    private var binding: FragmentLoginBinding? = null
    private val sharedViewModel: ShatteredViewModel by activityViewModels()
    private lateinit var googleSignInClient: GoogleSignInClient

    companion object {
        const val RC_SIGN_IN = 9001
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val fragmentBinding = FragmentLoginBinding.inflate(inflater, container, false)
        binding = fragmentBinding
        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.apply {
            lifecycleOwner = viewLifecycleOwner
            loginFragment = this@LoginFragment
        }

        binding?.googleButton?.setOnClickListener(this)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).fullScreen()
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) { updateUI(null) }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
            sharedViewModel.repository.auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) updateUI(sharedViewModel.repository.auth.currentUser)
            }
    }

    private var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) { updateUI(null) }
        }
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) resultLauncher.launch(signInIntent)
        else @Suppress("DEPRECATION") startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onClick(v: View) { if (v.id == R.id.google_button) signIn() }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            sharedViewModel.fetchCurrentLevel()

            sharedViewModel.currentLevelData.observe(viewLifecycleOwner) {
                if (it.currentLevel == null) {
                    sharedViewModel.updateDatabase(
                        CurrentLevelItem(1),
                        sharedViewModel.repository.currentLevelReference
                    )
                    navigateToHome()
                } else navigateToHome()
            }
        }
    }

    private fun navigateToHome() = findNavController().navigate(R.id.action_loginFragment_to_levelsFragment)
}