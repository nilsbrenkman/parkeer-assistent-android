package nl.parkeerassistent.amsterdam.review

import android.app.Activity
import android.content.Context
import com.google.android.play.core.review.ReviewManagerFactory
import nl.parkeerassistent.amsterdam.util.Log

/**
 * Play In-App Review (iOS `SKStoreReviewController`). The flow may show nothing — quota and
 * eligibility are decided by Play, and there's no callback indicating whether a review was left.
 * Failures (no Play services, debug build) are swallowed.
 */
class AppReview(context: Context) {

    private val manager = ReviewManagerFactory.create(context.applicationContext)

    fun request(activity: Activity) {
        manager.requestReviewFlow().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                runCatching { manager.launchReviewFlow(activity, task.result) }
            } else {
                Log.warning("In-app review request failed: ${task.exception?.message}")
            }
        }
    }
}
