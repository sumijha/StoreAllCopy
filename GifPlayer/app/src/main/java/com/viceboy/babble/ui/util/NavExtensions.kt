package com.viceboy.babble.ui.util

import android.util.SparseArray
import android.view.View
import androidx.core.util.set
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

fun BottomNavigationView.setUpBottomNavigation(
    listOfNavGraphs: List<Int>,
    containerId: Int,
    fragmentManager: FragmentManager
): LiveData<NavController> {

    val graphIdToMap = SparseArray<String>()

    val mutableNavControllerLiveData = MutableLiveData<NavController>()

    var firstFragmentGraphId = 0

    listOfNavGraphs.forEachIndexed { index, navGraphId ->
        val fragmentTag = getFragmentTag(index)
        val navHostFragment = obtainNavHostFragment(
            fragmentManager, containerId, fragmentTag, navGraphId
        )

        //Get GraphId from above navHostFragment
        val graphId = navHostFragment.navController.graph.id

        if (index == 0) firstFragmentGraphId = graphId
        graphIdToMap[graphId] = fragmentTag

        //Attach and detach NavHostFragment based on the graph id selected and assign value to MutableLiveData
        if (this.selectedItemId == graphId) {
            attachNavHostFragment(fragmentManager, navHostFragment, index == 0)
            mutableNavControllerLiveData.value = navHostFragment.navController
        } else {
            detachNavHostFragment(fragmentManager, navHostFragment)
        }

        //Creating selected fragment tags to check if on first Fragment
        var selectedFragmentTag = graphIdToMap[this.selectedItemId]
        val firstFragmentTag = graphIdToMap[firstFragmentGraphId]
        var isOnFirstFragment = selectedFragmentTag == firstFragmentTag

        //Setting up Listener on Bottom Navigation menu selected
        setOnNavigationItemSelectedListener { item ->
            //Return false if fragment Manager state is saved
            if (fragmentManager.isStateSaved) {
                return@setOnNavigationItemSelectedListener false
            } else {
                val newSelectedFragmentTag = graphIdToMap[item.itemId]
                //Checking if nav item is not already selected then remove everything from backstack of first fragment
                if (newSelectedFragmentTag != selectedFragmentTag) {
                    fragmentManager.popBackStack(
                        firstFragmentTag,
                        FragmentManager.POP_BACK_STACK_INCLUSIVE
                    )

                    val newlySelectedFragment =
                        fragmentManager.findFragmentByTag(newSelectedFragmentTag) as NavHostFragment

                    // Don't add first fragment to
                    if (newSelectedFragmentTag != firstFragmentTag) {
                        fragmentManager.beginTransaction()
                            .attach(newlySelectedFragment)
                            .setPrimaryNavigationFragment(newlySelectedFragment)
                            .apply {
                                //Detach all other fragments
                                detach(fragmentManager.findFragmentByTag(firstFragmentTag) as NavHostFragment)
                            }
                            .addToBackStack(firstFragmentTag)
                            .setReorderingAllowed(true)
                            .commit()
                    }
                    //Reassigning selected fragment tags
                    selectedFragmentTag = newSelectedFragmentTag
                    isOnFirstFragment = selectedFragmentTag == firstFragmentTag
                    mutableNavControllerLiveData.value = newlySelectedFragment.navController
                    true
                } else {
                    false
                }
            }
        }

        //Setup Navigation Item Reselected Listener
        setOnItemReselectedListener(graphIdToMap, fragmentManager)

        //Setup BackStack change Listener so that user can get back to first if on any other tabs
        fragmentManager.addOnBackStackChangedListener {
            if (!isOnFirstFragment && !fragmentManager.isOnBackStack(firstFragmentTag)) {
                this.selectedItemId = firstFragmentGraphId
                /*if (this.visibility == View.GONE)
                    this.toggleBottomNavVisibility(View.VISIBLE)*/
            }
        }
    }
    return mutableNavControllerLiveData
}

/**
 *Detach NavHostFragment from the stack
 */
fun detachNavHostFragment(fm: FragmentManager, navHostFragment: NavHostFragment) {
    fm.beginTransaction()
        .detach(navHostFragment)
        .commitNow()
}

/**
 *Attach NavHostFragment after adding it and sets as Primary navigation fragment
 * if flag sets as true
 */
fun attachNavHostFragment(
    fm: FragmentManager,
    navHostFragment: NavHostFragment,
    isPrimaryFragment: Boolean
) {
    fm.beginTransaction()
        .attach(navHostFragment)
        .apply {
            if (isPrimaryFragment)
                setPrimaryNavigationFragment(navHostFragment)
        }
        .commitNow()
}


/**
 * Returns NavHostFragment by first finding fragment by tag initially or
 * create NavHostFragment and add the host fragment  if it didn't exists
 */
fun obtainNavHostFragment(
    fm: FragmentManager,
    containerId: Int,
    fragmentTag: String,
    navGraphId: Int
): NavHostFragment {
    //Lets find the NavHost Fragment by Tag, if exists then return
    val oldNavHostFragment = fm.findFragmentByTag(fragmentTag) as NavHostFragment?
    oldNavHostFragment?.let {
        return it
    }

    //Lets create if it didn't exists
    val newNavHostFragment = NavHostFragment.create(navGraphId)
    fm.beginTransaction().add(containerId, newNavHostFragment, fragmentTag).commitNow()
    return newNavHostFragment
}

//Get Fragment Tag with string as bottomNav$index
private fun getFragmentTag(index: Int) = "bottomNav$index"

//Setting up Navigation Item Reselected Listener
private fun BottomNavigationView.setOnItemReselectedListener(
    graphIdToMap: SparseArray<String>,
    fm: FragmentManager
) {
    setOnNavigationItemReselectedListener { item ->
        val newlySelectedFragmentTag = graphIdToMap[item.itemId]
        //Get the tag and find the fragment and pop the stacks of selected fragment
        val newlySelectedFragment =
            fm.findFragmentByTag(newlySelectedFragmentTag) as NavHostFragment
        newlySelectedFragment.navController.apply {
            popBackStack(graph.startDestination, false)
        }
    }
}

private fun FragmentManager.isOnBackStack(tag: String): Boolean {
    val backStackCount = this.backStackEntryCount
    for (i in 0 until backStackCount) {
        if (getBackStackEntryAt(i).name == tag)
            return true
    }
    return false
}