package com.twain.say.ui.common


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.twain.say.R
import com.twain.say.databinding.FragmentSplashBinding
import com.twain.say.utils.Extensions.dataStore
import com.twain.say.utils.Extensions.statusBarColorFromResource
import com.twain.say.utils.PreferenceKeys
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class SplashFragment : Fragment() {

    private var _binding: FragmentSplashBinding? = null
    private val binding get() = _binding!!
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentSplashBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)
        statusBarColorFromResource(R.color.fragment_background)

        lifecycleScope.launch(Dispatchers.Main) {
            delay(1500L)
            isFirstLogin()
        }
    }

    private suspend fun isFirstLogin() {
        requireContext().dataStore.data.map { it[PreferenceKeys.FIRST_TIME_LOGIN] }.collect {
            if (it == null)
                navController.navigate(R.id.action_splashFragment_to_onboardingFragment)
            else
                navController.navigate(R.id.action_splashFragment_to_homeFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
