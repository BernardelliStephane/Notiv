package fr.steph.showmemories

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import fr.steph.showmemories.databinding.ActivityShowsBinding
import fr.steph.showmemories.viewmodels.ShowsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ShowsActivity : AppCompatActivity() {
    private lateinit var navController: NavController
    private lateinit var binding: ActivityShowsBinding
    private val showsViewModel: ShowsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        installSplashScreen().apply {
            val timeout = lifecycleScope.launch { delay(3000L) }
            setKeepOnScreenCondition {
                showsViewModel.shows.value == null && timeout.isActive
            }
        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_shows)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
        navController = navHostFragment.navController

        binding.navigationBarView.setupWithNavController(navController)

        initializeComponents()
        initializeObservers()
    }

    private fun initializeComponents() {
        val navBarFragments = listOf(R.id.navigation_home, R.id.navigation_collection, R.id.navigation_profile)
        navController.addOnDestinationChangedListener { _, destination: NavDestination, _ ->
            binding.navigationBarView.isVisible = destination.id in navBarFragments
        }
    }

    private fun initializeObservers() {
        showsViewModel.messageId.observe(this){
            it?.let {
                showsViewModel.resetMessageIdValue()
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }
    }
}