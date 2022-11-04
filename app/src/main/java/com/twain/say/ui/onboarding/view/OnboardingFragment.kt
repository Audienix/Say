package com.twain.say.ui.onboarding.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.datastore.preferences.core.edit
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.twain.say.R
import com.twain.say.databinding.FragmentOnboardingBinding
import com.twain.say.ui.onboarding.model.SliderItem
import com.twain.say.utils.Extensions.dataStore
import com.twain.say.utils.Extensions.statusBarColorFromResource
import com.twain.say.utils.PreferenceKeys
import kotlinx.coroutines.launch

class OnboardingFragment : Fragment() {

    private var _binding: FragmentOnboardingBinding? = null
    private val binding get() = _binding!!
    private lateinit var sliderItem: ArrayList<SliderItem>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentOnboardingBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpSliderItem()
        setUpViewPager()

        binding.btnGetStarted.setOnClickListener {
            lifecycleScope.launch {
                requireContext().dataStore.edit {
                    it[PreferenceKeys.FIRST_TIME_LOGIN] = false
                }
                findNavController().navigate(R.id.action_onboardingFragment_to_homeFragment)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        statusBarColorFromResource(R.color.translucent_white)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setUpSliderItem() {
        sliderItem = ArrayList()
        sliderItem.add(
            SliderItem(
                R.drawable.ic_home_empty, getString(R.string.view_pager_title_audio_recording),
                getString(R.string.view_pager_description_audio_recording)
            )
        )
        sliderItem.add(
            SliderItem(
                R.drawable.ic_home_empty, getString(R.string.view_pager_title_notification),
                getString(R.string.view_pager_description_notification)
            )
        )
        sliderItem.add(
            SliderItem(
                R.drawable.ic_home_empty, getString(R.string.view_pager_title_dark_mode),
                getString(R.string.view_pager_description_dark_mode)
            )
        )
    }

    private fun setUpViewPager() {
        val viewPagerAdapter = ViewPagerAdapter()
        viewPagerAdapter.submitList(sliderItem)
        binding.viewPager.adapter = viewPagerAdapter
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.indicator.selection = position
                if (position == sliderItem.size - 1) {
                    binding.indicator.count = sliderItem.size
                    binding.indicator.selection = position
                }
            }
        })
    }
}