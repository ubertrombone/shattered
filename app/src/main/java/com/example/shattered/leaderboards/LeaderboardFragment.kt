package com.example.shattered.leaderboards

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.shattered.MainActivity
import com.example.shattered.R
import com.example.shattered.databinding.FragmentLeaderboardBinding
import com.example.shattered.model.ShatteredViewModel

class LeaderboardFragment : DialogFragment() {

    private var binding: FragmentLeaderboardBinding? = null
    private val sharedViewModel: ShatteredViewModel by activityViewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: LeaderboardAdapter

    companion object {
        const val TAG = "LeaderboardFragment"
        private const val KEY_TITLE = "KEY_TITLE"

        fun newInstance(title: String): LeaderboardFragment {
            val args = Bundle()
            args.putString(KEY_TITLE, title)
            val fragment = LeaderboardFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedViewModel.switchReplayLevel(false)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val fragmentBinding = FragmentLeaderboardBinding.inflate(inflater, container, false)
        binding = fragmentBinding

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
            leaderboardFragment = this@LeaderboardFragment
        }

        setupView()
        recyclerView = binding?.leaderboardRecyclerView!!
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        setUpAdapter()
        val level = arguments?.getString(KEY_TITLE)
        sharedViewModel.fetchAllPlayersOnLevel(requireContext().getString(R.string.level, level))
        observeViewModel()
        clickListeners()
    }

    override fun onStart() {
        super.onStart()

        dialog?.window?.setLayout(
            sharedViewModel.displayWidth - (sharedViewModel.displayWidth * .1).toInt(),
            sharedViewModel.displayHeight - (sharedViewModel.displayHeight * .2).toInt()
        )

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).fullScreen()
    }

    private fun setupView() {
        val level = arguments?.getString(KEY_TITLE)
        binding?.levelHeader?.text = requireContext().getString(R.string.level, level)
    }

    private fun setUpAdapter() {
        adapter = LeaderboardAdapter()
        recyclerView.adapter = adapter
    }

    private fun observeViewModel() {
        sharedViewModel.listOfAllPlayersOnLevelData.observe(viewLifecycleOwner) { adapter.submitList(it) }
    }

    private fun clickListeners() {
        binding?.closePop?.setOnClickListener { dismiss() }

        binding?.playAgain?.setOnClickListener {
            sharedViewModel.switchReplayLevel(true)
            sharedViewModel.setLevel(arguments?.getString(KEY_TITLE)!!.toInt())
            findNavController().navigate(R.id.action_allLevelsFragment_to_gameFragment)
            dismiss()
        }
    }
}