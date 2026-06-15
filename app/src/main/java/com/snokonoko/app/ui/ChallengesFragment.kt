package com.snokonoko.app.ui

import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.snokonoko.app.R
import com.snokonoko.app.data.Challenge
import com.snokonoko.app.data.ChallengeManager
import com.snokonoko.app.databinding.FragmentChallengesBinding
import com.snokonoko.app.viewmodel.MainViewModel
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

class ChallengesFragment : Fragment() {

    private var _binding: FragmentChallengesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentChallengesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (parentFragment != null) {
            binding.btnBack.visibility = View.GONE
        } else {
            binding.btnBack.setOnClickListener { parentFragmentManager.popBackStack() }
        }

        val manager = ChallengeManager(requireContext())

        viewModel.transactions.observe(viewLifecycleOwner) { transactions ->
            val challenges = manager.getActiveChallenges(transactions ?: emptyList())
            renderChallenges(challenges)
        }
    }

    private fun maybeFireConfetti(challenges: List<Challenge>) {
        val prefs = requireContext().getSharedPreferences("snokonoko_prefs", 0)
        val celebrated = prefs.getStringSet("confetti_challenges", emptySet())!!.toMutableSet()
        val newKeys = challenges
            .filter { it.isComplete }
            .map { "${it.id}_${it.startDate}" }
            .filter { it !in celebrated }
        if (newKeys.isNotEmpty()) {
            celebrated.addAll(newKeys)
            prefs.edit().putStringSet("confetti_challenges", celebrated).apply()
            val parent = requireActivity().window.decorView as android.view.ViewGroup
            ConfettiView(requireContext()).burst(parent)
        }
    }

    private fun renderChallenges(challenges: List<Challenge>) {
        maybeFireConfetti(challenges)
        binding.weeklyContainer.removeAllViews()
        binding.monthlyContainer.removeAllViews()

        challenges.forEach { challenge ->
            val card = buildChallengeCard(challenge)
            val space = View(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 10.dp)
            }
            if (challenge.type == "weekly") {
                binding.weeklyContainer.addView(card)
                binding.weeklyContainer.addView(space)
            } else {
                binding.monthlyContainer.addView(card)
                binding.monthlyContainer.addView(space)
            }
        }
    }

    private fun buildChallengeCard(c: Challenge): View {
        val ctx = requireContext()

        val accentColor = if (c.isComplete) "#30D158" else "#C71585"
        val progressPct = (c.progress * 100).toInt()

        val card = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16.dp, 16.dp, 16.dp, 16.dp)
            setBackgroundColor(Color.parseColor("#0F0F0F"))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // Header row: title + status chip
        val header = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).also { it.bottomMargin = 6.dp }
        }

        header.addView(TextView(ctx).apply {
            text = c.title
            setTextColor(Color.WHITE)
            textSize = 15f
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        })

        header.addView(TextView(ctx).apply {
            text = if (c.isComplete) "DONE" else "$progressPct%"
            setTextColor(Color.parseColor(accentColor))
            setBackgroundColor(Color.parseColor(if (c.isComplete) "#1A30D158" else "#1AC71585"))
            textSize = 10.sp
            setPadding(8.dp, 3.dp, 8.dp, 3.dp)
        })

        card.addView(header)

        // Description
        card.addView(TextView(ctx).apply {
            text = c.description
            setTextColor(Color.parseColor("#8C8C8C"))
            textSize = 12f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).also { it.bottomMargin = 12.dp }
        })

        // Progress bar track
        val track = FrameLayout(ctx).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 4.dp)
                .also { it.bottomMargin = 8.dp }
            setBackgroundColor(Color.parseColor("#222222"))
        }
        val fill = View(ctx).apply {
            val w = (progressPct / 100f * resources.displayMetrics.widthPixels).toInt()
            layoutParams = FrameLayout.LayoutParams(w, FrameLayout.LayoutParams.MATCH_PARENT)
            setBackgroundColor(Color.parseColor(accentColor))
        }
        track.addView(fill)
        card.addView(track)

        // Current / target label
        card.addView(TextView(ctx).apply {
            text = buildProgressLabel(c)
            setTextColor(Color.parseColor("#474747"))
            textSize = 11f
        })

        return card
    }

    private fun buildProgressLabel(c: Challenge): String {
        val nf = NumberFormat.getInstance(Locale("en", "ZA")).apply {
            minimumFractionDigits = 0
            maximumFractionDigits = 0
        }
        return when (c.metric) {
            "category_cap", "total_cap" ->
                "R ${nf.format(abs(c.currentValue))} spent  ·  target under R ${nf.format(c.targetValue)}"
            "no_spend_days" ->
                "${c.currentValue.toInt()} no-spend days  ·  target ${c.targetValue.toInt()}"
            "tx_count" ->
                "${c.currentValue.toInt()} transactions  ·  target ${c.targetValue.toInt()}"
            "net_save" ->
                "R ${nf.format(abs(c.currentValue))} saved  ·  target R ${nf.format(c.targetValue)}"
            else -> ""
        }
    }

    private val Int.dp: Int get() = (this * resources.displayMetrics.density).toInt()
    private val Int.sp: Float get() = this.toFloat()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
