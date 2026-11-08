package com.example.noisedet2.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.example.noisedet2.R
import com.example.noisedet2.feature.history.HistoryScreen
import dagger.hilt.android.AndroidEntryPoint

/**
 * Hosts the Compose measurement history screen.
 */
@AndroidEntryPoint
class HistoryFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                HistoryScreen(
                    onOpenMap = {
                        findNavController().navigate(
                            R.id.navigation_home,
                            null,
                            navOptions {
                                popUpTo(R.id.navigation_home) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        )
                    }
                )
            }
        }
    }
}
