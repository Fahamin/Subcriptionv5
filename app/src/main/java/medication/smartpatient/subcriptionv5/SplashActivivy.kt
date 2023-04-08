package medication.smartpatient.subcriptionv5

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.android.billingclient.api.*

class SplashActivivy : AppCompatActivity() {
    private var billingClient: BillingClient? = null
    var prefs: Prefs? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_activivy)


        checkSubscription()


    }

    fun checkSubscription() {
        billingClient = BillingClient.newBuilder(this).enablePendingPurchases()
            .setListener { billingResult: BillingResult?, list: List<Purchase?>? -> }
            .build()
        val finalBillingClient: BillingClient = billingClient as BillingClient
        billingClient!!.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {}
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    finalBillingClient.queryPurchasesAsync(
                        QueryPurchasesParams.newBuilder()
                            .setProductType(BillingClient.ProductType.SUBS).build()
                    ) { billingResult1: BillingResult, list: List<Purchase?> ->
                        if (billingResult1.responseCode == BillingClient.BillingResponseCode.OK) {
                            Log.d("testOffer", list.size.toString() + " size")
                            if (list.size > 0) {
                                prefs?.premium = 1
                                prefs?.isRemoveAd = true
                                // set 1 to activate premium feature
// set 1 to activate premium feature
                                startActivity(
                                    Intent(
                                        this@SplashActivivy,
                                        AfterSubcribeAcivity::class.java
                                    )
                                )


                                var i = 0
                                for (purchase in list) {
                                    //Here you can manage each product, if you have multiple subscription
                                    //     Log.d("testOffer", purchase.getOriginalJson()); // Get to see the order information
                                    //   Log.d("testOffer", " index" + i);
                                    i++
                                }
                            } else {
                                prefs?.premium = 0
                                prefs?.isRemoveAd = false
                                startActivity(Intent(this@SplashActivivy, MainActivity::class.java))

                                // set 0 to de-activate premium feature
                            }
                        }
                    }
                }
            }
        })
    }
}