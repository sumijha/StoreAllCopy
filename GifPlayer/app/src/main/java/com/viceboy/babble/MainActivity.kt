package com.viceboy.babble

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.viceboy.babble.ui.base.AuthStateListener
import com.viceboy.babble.ui.util.setUpBottomNavigation
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import javax.inject.Inject


//TODO: Code refactor required
class MainActivity : AppCompatActivity(), HasSupportFragmentInjector {

    @Inject
    lateinit var authStateListener: AuthStateListener

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>

    private lateinit var navController: NavController


    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()
        if (savedInstanceState == null)
            setUpBottomNavigation()

    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        setUpBottomNavigation()
    }

    override fun onStart() {
        super.onStart()
        if (!authStateListener.hasValidSession())
            startAuthActivity()
    }

    override fun supportFragmentInjector(): AndroidInjector<Fragment> = dispatchingAndroidInjector

    override fun onSupportNavigateUp(): Boolean = navController.navigateUp()

    private fun startAuthActivity() {
        val intent = Intent(this, AuthActivity::class.java)
        startActivity(intent)
        finish()
    }


    private fun setUpBottomNavigation() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_nav)

        val controller = bottomNavigationView.setUpBottomNavigation(
            listOf(R.navigation.dashboard, R.navigation.capture_expense_test)
            , R.id.nav_host_container, supportFragmentManager
        )

        controller.observe(this, Observer {
            setupActionBarWithNavController(it)
            navController = it
        })
    }
}
