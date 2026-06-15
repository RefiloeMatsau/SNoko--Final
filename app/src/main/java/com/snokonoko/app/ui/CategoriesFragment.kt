package com.snokonoko.app.ui

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.snokonoko.app.R
import com.snokonoko.app.data.Category
import com.snokonoko.app.databinding.FragmentCategoriesBinding
import com.snokonoko.app.databinding.ItemCategoryBinding
import com.snokonoko.app.viewmodel.MainViewModel

class CategoriesFragment : Fragment() {

    private var _binding: FragmentCategoriesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()

    private lateinit var adapter: CategoryAdapter

    // Available colours
    private val colours: Map<Int, String> = mapOf(
        R.id.rbRed to "#FF6B6B",
        R.id.rbBlue to "#0A84FF",
        R.id.rbPurple to "#BF5AF2",
        R.id.rbOrange to "#FF9F0A",
        R.id.rbGreen to "#30D158",
        R.id.rbGray to "#8E8E93"
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCategoriesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupAddButton()
        observeCategories()
    }

    private fun setupRecyclerView() {
        adapter = CategoryAdapter { category ->
            viewModel.deleteCategory(category)
        }
        binding.rvCategories.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCategories.adapter = adapter
    }

    private fun setupAddButton() {
        binding.btnAddCategory.setOnClickListener {
            val name = binding.etCategoryName.text.toString().trim()
            if (name.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter a category name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedColourId = binding.rgColours.checkedRadioButtonId
            val colour = colours[selectedColourId] ?: "#FF6B6B"

            val prefs = requireContext().getSharedPreferences("snokonoko_prefs", 0)
            val userId = prefs.getInt("user_id", -1)
            val category = Category(userId = userId, name = name, colour = colour)
            viewModel.addCategory(category)
            binding.etCategoryName.text.clear()
        }
    }

    private fun observeCategories() {
        viewModel.categories.observe(viewLifecycleOwner) { categories ->
            adapter.submitList(categories ?: emptyList())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class CategoryAdapter(
    private val onDelete: (Category) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    private var categories: List<Category> = emptyList()

    fun submitList(list: List<Category>) {
        categories = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCategoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(categories[position])
    }

    override fun getItemCount() = categories.size

    inner class ViewHolder(private val binding: ItemCategoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(category: Category) {
            binding.tvCategoryName.text = category.name

            // Set the colour circle
            val drawable = GradientDrawable()
            drawable.shape = GradientDrawable.OVAL
            drawable.setColor(Color.parseColor(category.colour))
            binding.vColourCircle.background = drawable

            binding.btnDelete.setOnClickListener {
                onDelete(category)
            }
        }
    }
}
