package com.snokonoko.app.ui

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.snokonoko.app.R
import com.snokonoko.app.data.Badge
import com.snokonoko.app.data.BadgeManager
import com.snokonoko.app.data.StreakManager
import com.snokonoko.app.databinding.FragmentBadgesBinding
import com.snokonoko.app.viewmodel.MainViewModel

class BadgesFragment : Fragment() {

    private var _binding: FragmentBadgesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()
    private fun badgeIcon(id: String): String = when (id) {
        "first_transaction" -> "⭐"
        "login_7"           -> "🔥"
        "login_30"          -> "👑"
        "no_spend_3"        -> "🌿"
        "no_spend_7"        -> "🛡️"
        "budget_1"          -> "✅"
        "budget_3"          -> "🏆"
        "tx_50"             -> "📊"
        "tx_100"            -> "💯"
        "saver_1000"        -> "💰"
        else                -> "★"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBadgesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (parentFragment != null) {
            binding.btnBack.visibility = View.GONE
        } else {
            binding.btnBack.setOnClickListener { parentFragmentManager.popBackStack() }
        }

        val badgeManager  = BadgeManager(requireContext())
        val streakManager = StreakManager(requireContext())

        viewModel.transactions.observe(viewLifecycleOwner) { transactions ->
            val txList = transactions ?: emptyList()
            val goals  = viewModel.monthlyGoals.value ?: emptyList()
            val badges = badgeManager.evaluate(txList, streakManager, goals)
            renderBadges(badges)
        }
    }

    private fun maybeFireConfetti(badges: List<Badge>) {
        val prefs = requireContext().getSharedPreferences("snokonoko_prefs", 0)
        val celebrated = prefs.getStringSet("confetti_badges", emptySet())!!.toMutableSet()
        val newIds = badges.filter { it.earned }.map { it.id }.filter { it !in celebrated }
        if (newIds.isNotEmpty()) {
            celebrated.addAll(newIds)
            prefs.edit().putStringSet("confetti_badges", celebrated).apply()
            val parent = requireActivity().window.decorView as android.view.ViewGroup
            ConfettiView(requireContext()).burst(parent)
        }
    }

    private fun renderBadges(badges: List<Badge>) {
        maybeFireConfetti(badges)
        val earnedCount = badges.count { it.earned }
        binding.tvEarnedCount.text = "$earnedCount / ${badges.size}"

        binding.badgeListContainer.removeAllViews()

        badges.forEachIndexed { index, badge ->
            val row = layoutInflater.inflate(R.layout.item_badge, binding.badgeListContainer, false)

            val circle  = row.findViewById<FrameLayout>(R.id.badgeCircle)
            val initial = row.findViewById<TextView>(R.id.tvBadgeInitial)
            val name    = row.findViewById<TextView>(R.id.tvBadgeName)
            val desc    = row.findViewById<TextView>(R.id.tvBadgeDesc)
            val status  = row.findViewById<TextView>(R.id.tvBadgeStatus)

            val color = if (badge.earned) Color.parseColor(badge.color) else Color.parseColor("#2C2C2E")

            val bg = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(color)
            }
            circle.background = bg
            initial.text = badgeIcon(badge.id)
            initial.textSize = 20f

            name.text = badge.name
            name.setTextColor(if (badge.earned) Color.WHITE else Color.parseColor("#474747"))

            desc.text = badge.description

            if (badge.earned) {
                status.text = "EARNED"
                status.setTextColor(Color.parseColor(badge.color))
            } else {
                status.text = "Locked"
                status.setTextColor(Color.parseColor("#474747"))
            }

            binding.badgeListContainer.addView(row)

            // Divider between items
            if (index < badges.size - 1) {
                val divider = View(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 1
                    ).also { it.setMargins(16.dp, 0, 16.dp, 0) }
                    setBackgroundColor(Color.parseColor("#1A1A1A"))
                }
                binding.badgeListContainer.addView(divider)
            }
        }
    }

    private val Int.dp: Int get() = (this * resources.displayMetrics.density).toInt()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
