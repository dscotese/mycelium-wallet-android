package com.mycelium.wallet.activity


import android.app.Activity
import android.view.View
import android.view.ViewGroup
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingPolicies
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.core.internal.deps.guava.collect.Iterables
import androidx.test.espresso.intent.Intents.*
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import com.mycelium.wallet.R
import com.mycelium.wallet.activity.modern.ModernMain
import com.mycelium.wallet.activity.send.ManualAddressEntry
import com.mycelium.wallet.activity.send.SendInitializationActivity
import com.mycelium.wallet.activity.send.SendMainActivity
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.*
import org.hamcrest.TypeSafeMatcher
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

@LargeTest
@RunWith(AndroidJUnit4::class)
class SendAssetsTest {


    val addressSendTo = "2NEfsR6yeuF8tusetj5jhPz7LizY7frRfqu"
    val isTrasactionShouldBeCompleted = true

    @get:Rule
    var activityTestRule = ActivityTestRule(StartupActivity::class.java, false, false)


    @Before
    fun resetTimeout() {
        IdlingPolicies.setMasterPolicyTimeout(20, TimeUnit.MINUTES)
        IdlingPolicies.setIdlingResourceTimeout(20, TimeUnit.MINUTES)
    }

    @Test
    fun testBackupAndSendBTC() {
        init()
        val activityScenario =
                ActivityScenario.launch(StartupActivity::class.java)
        Thread.sleep(10000)
        val currentActivity = getCurrentActivity()
        if (currentActivity is StartupActivity) {
            onView(withText(R.string.master_seed_restore_backup_button))
                    .perform(scrollTo(), click())

            onView(withText(R.string.ok))
                    .perform(scrollTo(), click())

            repeat(12) {
                onView(withText("O")).perform(click())
                onView(withText("I")).perform(click())
                onView(withText("L")).perform(click())
            }
        }
        intended(hasComponent(ModernMain::class.java.name))
        onView(withId(R.id.btSend)).perform(click())
        intended(hasComponent(SendInitializationActivity::class.java.name))
        waitWhileAcitvity<SendInitializationActivity>()
        intended(hasComponent(SendMainActivity::class.java.name))
        onView(withId(R.id.btManualEntry)).perform(click())
        intended(hasComponent(ManualAddressEntry::class.java.name))
        onView(withId(R.id.etAddress)).perform(ViewActions.typeText(addressSendTo))
        onView(withId(R.id.btOk)).perform(click())
        onView(withId(R.id.btEnterAmount)).perform(click())
        intended(hasComponent(GetAmountActivity::class.java.name))
        onView(withText("0")).perform(click())
        onView(withText(".")).perform(click())
        onView(withText("0")).perform(click())
        onView(withText("0")).perform(click())
        onView(withText("1")).perform(click())
        onView(withId(R.id.btOk)).perform(click())
        intended(hasComponent(ManualAddressEntry::class.java.name))
        onView(withId(R.id.btSend)).perform(click())
        intended(hasComponent(ModernMain::class.java.name))

        if (isTrasactionShouldBeCompleted) {
            IdlingRegistry.getInstance().register(ViewVisibilityIdlingResource(getCurrentActivity()!!,R.id.tvSending, View.GONE))
            onView(withId(R.id.tvSending)).check(matches(not(isDisplayed())))
        } else {
            onView(withId(R.id.tvSending)).check(matches((isDisplayed())))
        }
        release()
        activityScenario.close()
    }

    inline fun <reified T : Activity> waitWhileAcitvity(): ActivityIdlingResource {
        val activityIdlingResource = ActivityIdlingResource(T::class.java)
        IdlingRegistry.getInstance().register(activityIdlingResource)
        return activityIdlingResource
    }

    private fun getCurrentActivity(): Activity? {
        getInstrumentation().waitForIdleSync()
        val activity = arrayOfNulls<Activity>(1)
        getInstrumentation().runOnMainSync {
            val activities = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED)
            activity[0] = Iterables.getOnlyElement(activities)
        }
        return activity[0]
    }

    private fun childAtPosition(
            parentMatcher: Matcher<View>, position: Int): Matcher<View> {

        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("Child at position $position in parent ")
                parentMatcher.describeTo(description)
            }

            public override fun matchesSafely(view: View): Boolean {
                val parent = view.parent
                return (parent is ViewGroup && parentMatcher.matches(parent)
                        && view == parent.getChildAt(position))
            }
        }
    }
}

