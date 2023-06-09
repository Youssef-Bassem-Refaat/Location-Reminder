package com.udacity.project4.locationreminders.reminderslist

import android.content.Context
import org.koin.androidx.viewmodel.dsl.viewModel
import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.FakeDataSource
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.not
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get
import org.koin.test.inject
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest : KoinTest{
    private val dataSource: ReminderDataSource by inject()
    private val reminder1 = ReminderDTO("Reminder1", "Description1", "Location1", 1.0, 1.0,"1")
    private val reminder2 = ReminderDTO("Reminder2", "Description2", "location2", 2.0, 2.0, "2")
    private val reminder3 = ReminderDTO("Reminder3", "Description3", "location3", 3.0, 3.0, "3")

    @Before
    fun initRepository() {
        stopKoin()
        // use Koin Library as a service locator
        val myModule = module {
            //Declare a ViewModel - be later inject into Fragment with dedicated injector using by viewModel()
            viewModel {
                RemindersListViewModel(
                    get(),
                    get()
                )
            }
            single {
                FakeDataSource() as ReminderDataSource
            }
        }
        startKoin {
            androidContext(getApplicationContext())
            modules(listOf(myModule))
        }
    }

    @After
    fun cleanupDb() = runBlockingTest { dataSource.deleteAllReminders() }
    // Here, we're testing the ReminderList to see if it accurately displays the reminders.

    // Here, we're testing the ReminderList to see what it would display in the absence of any items(reminders)
    @Test
    fun rMp1oViPb3EdvcJ5kxoqe52RuaiK6YiUYo() = runBlockingTest{
        // GIVEN - Empty Database
        dataSource.deleteAllReminders()
        // WHEN - start Fragment
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)
        scenario.onFragment { Navigation.setViewNavController(it.view!!, navController) }
        //THEN - We don't see any items(reminders), we only see No data string
        onView(withText(R.string.no_data)).check(matches(isDisplayed()))
        onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))
        onView(withText(reminder1.title)).check(doesNotExist())
    }

    @Test
    fun reminderListAndDisplayedInUi() = runBlockingTest {
        //while ADD ACTIVE (INCOMPLETE) TASK TO DB FOR GIVEN
        dataSource.saveReminder(reminder1)
        dataSource.saveReminder(reminder2)
        dataSource.saveReminder(reminder3)
        // WHEN - Start Fragment
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)
        scenario.onFragment { Navigation.setViewNavController(it.view!!, navController) }
        //THEN - We see 3 items(reminders) in the list, which we added above
        onView(withText(reminder1.title)).check(matches(isDisplayed()))
        onView(withText(reminder2.description)).check(matches(isDisplayed()))
        onView(withText(reminder3.title)).check(matches(isDisplayed()))
        onView(withId(R.id.noDataTextView)).check(matches(not(isDisplayed())))
    }


    // Here, we're trying clickFab, and the destination we want to reach is the Reminder Fragment.
    @Test
    fun clickFabAndNavigateToReminderFragment() = runBlockingTest {
        // GIVEN - On the home screen ReminderListFragment
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)
        scenario.onFragment { Navigation.setViewNavController(it.view!!, navController) }
        // WHEN - Click on the fab(float Action button)
        onView(withId(R.id.addReminderFAB)).perform(click())
        // THEN - Verify that we navigate to the (save reminder fragment)
        verify(navController).navigate(ReminderListFragmentDirections.toSaveReminder())
    }}