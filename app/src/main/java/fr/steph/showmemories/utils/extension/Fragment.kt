package fr.steph.showmemories.utils.extension

import androidx.fragment.app.Fragment
import androidx.navigation.NavDirections
import androidx.navigation.Navigation
import androidx.navigation.fragment.FragmentNavigator

fun Fragment.safeNavigate(direction: NavDirections, extras: FragmentNavigator.Extras? = null) {
    Navigation.findNavController(requireView()).apply {
        currentDestination?.getAction(direction.actionId)?.run {
            extras?.let { navigate(direction, extras) } ?: navigate(direction)
        }
    }
}