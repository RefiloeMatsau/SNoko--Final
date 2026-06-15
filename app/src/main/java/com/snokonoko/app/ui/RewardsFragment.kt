package com.snokonoko.app.ui

import android.graphics.Color
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.snokonoko.app.databinding.FragmentRewardsBinding

class RewardsFragment : Fragment() {

    private var _binding: FragmentRewardsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRewardsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnTabChallenges.setOnClickListener { showTab("challenges") }
        binding.btnTabBadges.setOnClickListener { showTab("badges") }

        showTab("challenges")
    }

    private fun showTab(tab: String) {
        val fragment = if (tab == "challenges") ChallengesFragment() else BadgesFragment()
        childFragmentManager.beginTransaction()
            .replace(com.snokonoko.app.R.id.rewardsContainer, fragment)
            .commit()

        val pink = Color.parseColor("#F49AC2")
        val dim  = Color.parseColor("#8C8C8C")
        if (tab == "challenges") {
            binding.btnTabChallenges.setTextColor(pink)
            binding.btnTabBadges.setTextColor(dim)
        } else {
            binding.btnTabBadges.setTextColor(pink)
            binding.btnTabChallenges.setTextColor(dim)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
