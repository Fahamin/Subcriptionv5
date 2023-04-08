package medication.smartpatient.subcriptionv5

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.billingclient.api.*
import com.android.billingclient.api.BillingFlowParams.ProductDetailsParams
import com.android.billingclient.api.QueryProductDetailsParams.Product
import com.google.android.material.snackbar.Snackbar
import com.google.common.collect.ImmutableList

class RemoveAdsActivity : AppCompatActivity(), ItemClickListener {
    var activity: Activity? = null
    var prefs: Prefs? = null
    private var billingClient: BillingClient? = null
    var productDetailsList: MutableList<ProductDetails>? = null
    var loadProducts: ProgressBar? = null
    var recyclerView: RecyclerView? = null
    var handler: Handler? = null
    private val adContainerView: FrameLayout? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_remove_ads)
        val actionBar = supportActionBar
        actionBar?.hide()
        initViews()
        title = "Remove Ads"
        //Initialize a BillingClient with PurchasesUpdatedListener onCreate method
        billingClient = BillingClient.newBuilder(this)
            .enablePendingPurchases()
            .setListener { billingResult: BillingResult, list: List<Purchase>? ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && list != null) {
                    for (purchase in list) {
                        handlePurchase(purchase)
                    }
                }
            }.build()

        //start the connection after initializing the billing client
        establishConnection()
        /*    //restore purchases
        btn_restore_fab.setOnClickListener(v -> {
            restorePurchases();
        });*/

        //Checks if the user has a removeAd if not then show ads.
        //Checks if the user has a premium/subscription if not then show ads.
        if (prefs!!.premium == 0) {
            if (!prefs!!.isRemoveAd) {
                Log.d("RemoveAds", "Remove ads off")
            } else {
                Log.d("RemoveAds", "Remove ads On")
                //    mAdView.setVisibility(View.GONE);
            }
        }
    }

    fun establishConnection() {
        billingClient!!.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                    showProducts()
                }
            }

            override fun onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                establishConnection()
            }
        })
    }

    @SuppressLint("SetTextI18n")
    fun showProducts() {
        val productList = ArrayList<QueryProductDetailsParams.Product>()
        productList.add(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId("product_id_example")
                .setProductType(BillingClient.ProductType.SUBS)
                .build(),


            )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()
        billingClient!!.queryProductDetailsAsync(
            params
        ) { billingResult: BillingResult?, prodDetailsList: List<ProductDetails>? ->
            // Process the result
            productDetailsList!!.clear()
            handler!!.postDelayed({
                loadProducts!!.visibility = View.INVISIBLE
                productDetailsList!!.addAll(prodDetailsList!!)
                val adapter = RemoveAdsAdapter(
                    applicationContext,
                    productDetailsList!!,
                    this@RemoveAdsActivity,
                    this
                )
                recyclerView!!.setHasFixedSize(true)
                recyclerView!!.layoutManager =
                    LinearLayoutManager(this@RemoveAdsActivity, LinearLayoutManager.VERTICAL, false)
                recyclerView!!.adapter = adapter
            }, 2000)
        }
    }

    fun launchPurchaseFlow(productDetails: ProductDetails) {
        assert(productDetails.subscriptionOfferDetails != null)
        val productDetailsParamsList = ImmutableList.of(
            ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .setOfferToken(productDetails.subscriptionOfferDetails!![0].offerToken)
                .build()
        )
        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()
        billingClient!!.launchBillingFlow(activity!!, billingFlowParams)
    }

    fun handlePurchase(purchases: Purchase) {
        if (!purchases.isAcknowledged) {
            billingClient!!.acknowledgePurchase(
                AcknowledgePurchaseParams
                    .newBuilder()
                    .setPurchaseToken(purchases.purchaseToken)
                    .build()
            ) { billingResult: BillingResult ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    //Setting setIsRemoveAd to true
                    // true - No ads
                    // false - showing ads.
                    prefs!!.isRemoveAd = true
                    reloadScreen()
                }
            }
        }
    }

    private fun reloadScreen() {
        //Reload the screen to activate the removeAd and remove the actual Ad off the screen.
        finish()
        overridePendingTransition(0, 0)
        startActivity(intent)
        overridePendingTransition(0, 0)
    }

    override fun onResume() {
        super.onResume()
        billingClient!!.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP)
                .build()
        ) { billingResult: BillingResult, list: List<Purchase> ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                for (purchase in list) {
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged) {
                        handlePurchase(purchase)
                    }
                }
            }
        }
    }

    fun restorePurchases() {
        billingClient = BillingClient.newBuilder(this).enablePendingPurchases()
            .setListener { billingResult: BillingResult?, list: List<Purchase?>? -> }
            .build()
        val finalBillingClient = billingClient!!
        billingClient!!.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {
                establishConnection()
            }

            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    finalBillingClient.queryPurchasesAsync(
                        QueryPurchasesParams.newBuilder()
                            .setProductType(BillingClient.ProductType.INAPP).build()
                    ) { billingResult1: BillingResult, list: List<Purchase?> ->
                        if (billingResult1.responseCode == BillingClient.BillingResponseCode.OK) {
                            if (list.size > 0) {
                                prefs!!.isRemoveAd = true // set true to activate remove ad feature
                            } else {
                                prefs!!.isRemoveAd =
                                    false // set false to de-activate remove ad feature
                            }
                        }
                    }
                }
            }
        })
    }

    private fun initViews() {
        activity = this
        handler = Handler()
        prefs = Prefs(this)
        productDetailsList = ArrayList()
        recyclerView = findViewById(R.id.recycleViewID)
        loadProducts = findViewById(R.id.progressID)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    fun showSnackbar(view: View?, message: String?) {
        Snackbar.make(view!!, message!!, Snackbar.LENGTH_SHORT).show()
    }

    override fun clickListener(pos: Int) {
        launchPurchaseFlow(productDetailsList!![pos])
    }
}