package com.example.shattered.levelslist

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.shattered.MainActivity
import com.example.shattered.R
import com.example.shattered.databinding.FragmentAllLevelsBinding
import com.example.shattered.model.ShatteredViewModel


class AllLevelsFragment : Fragment() {

    private var binding: FragmentAllLevelsBinding? = null
    private val sharedViewModel: ShatteredViewModel by activityViewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AllLevelsAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val fragmentBinding = FragmentAllLevelsBinding.inflate(inflater, container, false)
        binding = fragmentBinding
        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.apply {
            lifecycleOwner = viewLifecycleOwner
            allLevelsFragment = this@AllLevelsFragment
        }

        binding?.nothingToShow?.visibility = View.GONE

        binding?.navigateUp?.setOnClickListener {
            findNavController().navigate(R.id.action_allLevelsFragment_to_levelsFragment)
        }
        recyclerView = binding?.recyclerView!!
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        setUpAdapter()
        sharedViewModel.fetchAllLevels()
        observeViewModel()
        goToGameFragment()
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).fullScreen()
    }

    private fun setUpAdapter() {
        adapter = AllLevelsAdapter(requireContext())
        recyclerView.adapter = adapter
    }

    private fun observeViewModel() {
        sharedViewModel.listOfAllLevelsData.observe(viewLifecycleOwner) {
            adapter.submitList(it)
            if (adapter.itemCount == 0) binding?.nothingToShow?.visibility = View.VISIBLE
        }
    }

    private fun goToGameFragment() {
        binding?.nothingToShow?.setOnClickListener {
            sharedViewModel.setLevel(1)
            findNavController().navigate(R.id.action_allLevelsFragment_to_gameFragment)
        }
    }
}