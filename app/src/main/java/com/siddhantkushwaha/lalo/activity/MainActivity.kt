package com.siddhantkushwaha.lalo.activity

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.siddhantkushwaha.lalo.*
import com.siddhantkushwaha.lalo.entity.UsageInfo
import com.siddhantkushwaha.lalo.request.RetrofitAPI
import com.siddhantkushwaha.lalo.request.UsageQuotaResp
import com.siddhantkushwaha.lalo.util.RealmUtil
import io.realm.OrderedRealmCollectionChangeListener
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.log2
import kotlin.math.pow

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    private lateinit var retrofitAPI: RetrofitAPI
    private lateinit var realm: Realm

    private var usageInfo: RealmResults<UsageInfo>? = null
    private lateinit var observer: OrderedRealmCollectionChangeListener<RealmResults<UsageInfo>>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        retrofitAPI = RetrofitAPI()
        realm = RealmUtil.getCustomRealmInstance(this)

        usageInfo =
            realm.where(UsageInfo::class.java).sort("timestamp", Sort.DESCENDING).findAllAsync()
        observer =
            OrderedRealmCollectionChangeListener<RealmResults<UsageInfo>> { _, _ ->
                updateUI()
            }

        runUpdateThread()

        button_refresh.setOnClickListener {
            runUpdateThread()
        }

        button_refresh_2.setOnClickListener {
            runUpdateThread()
        }
    }

    override fun onResume() {
        super.onResume()

        updateUI()
        usageInfo?.addChangeListener(observer)
    }

    override fun onPause() {
        super.onPause()
        usageInfo?.removeAllChangeListeners()
    }

    private fun runUpdateThread() {
        val th = Thread {

            var ret = updateInfoInDbMethod1()
            if (ret == 0) {
                Log.d(TAG, "Method 1 was successfull.")
                return@Thread
            }

            ret = updateInfoInDbMethod2()
            if (ret == 0) {
                Log.d(TAG, "Method 2 was successfull.")
                return@Thread
            }

            showError()
        }
        th.start()
    }

    private fun updateInfoInDbMethod1(): Int {
        return try {
            val response = RetrofitAPI.getUsageQuotaMethod1()
            val usageQuotaResp = response.body()
            if (response.isSuccessful && usageQuotaResp != null && usageQuotaResp.resultCode == 200 && usageQuotaResp.rows?.size ?: 0 > 0) {
                parseUsageQuotaRespAndSave(usageQuotaResp)
            } else {
                Log.e(TAG, "${response.isSuccessful} ${usageQuotaResp?.msg}")
                throw Exception("Couldn't fetch usage quoto info.")
            }
            0
        } catch (exception: Exception) {
            exception.printStackTrace()
            1
        }
    }

    private fun updateInfoInDbMethod2(): Int {
        return try {
            val locationResp = RetrofitAPI.getLocation()
            val location = locationResp.body()?.location
            if (location != null) {
                val response = RetrofitAPI.getUsageQuotaMethod2(location)
                val usageQuotaResp = response.body()
                if (response.isSuccessful && usageQuotaResp != null && usageQuotaResp.resultCode == 200 && usageQuotaResp.rows?.size ?: 0 > 0) {
                    parseUsageQuotaRespAndSave(usageQuotaResp)
                } else {
                    Log.e(TAG, "${response.isSuccessful} ${usageQuotaResp?.msg}")
                    throw Exception("Couldn't fetch usage quota info.")
                }
            } else {
                throw Exception("Couldn't fetch location.")
            }
            0
        } catch (exception: Exception) {
            exception.printStackTrace()
            1
        }
    }

    private fun parseUsageQuotaRespAndSave(usageQuotaResp: UsageQuotaResp) {

        val result = usageQuotaResp.rows!![0]
        val usageToday = result["dailyTotalUsage"] ?: result["dailyUsedOctets"]!!
        val totalUsage = result["totalUsage"] ?: result["totalOctets"]!!
        val serviceName = result["serviceName"]!!

        // parse usage fields

        val totalUsageBytes = getBytesFromDataString(totalUsage)
        val usageTodayBytes = getBytesFromDataString(usageToday)

        // parse service string to other info

        var totalAvailableBytes = 0L
        var bandwidth = ""
        var bandwidthAfterDataUsed = ""

        serviceName.toLowerCase(Locale.getDefault()).split("-").forEach {
            if (it.contains(Regex(".*\\d.*"))) {
                if (it.contains("mb") || it.contains("bps")) {
                    if (bandwidth == "")
                        bandwidth = it
                    else
                        bandwidthAfterDataUsed = it
                } else {
                    totalAvailableBytes = getBytesFromDataString(it)
                }
            }
        }

        val usageInfo = UsageInfo()
        usageInfo.timestamp = Date().time
        usageInfo.usageTodayBytes = usageTodayBytes
        usageInfo.totalUsageBytes = totalUsageBytes
        usageInfo.totalAvailableBytes = totalAvailableBytes
        usageInfo.serviceName = serviceName
        usageInfo.bandwidth = bandwidth
        usageInfo.bandwidthAfterDataUsed = bandwidthAfterDataUsed

        val realmL = RealmUtil.getCustomRealmInstance(this@MainActivity)
        realmL.executeTransactionAsync {
            it.insertOrUpdate(usageInfo)
        }
        realmL.close()
    }

    private fun getBytesFromDataString(text: String): Long {

        val m = mapOf("k" to 10, "m" to 20, "g" to 30, "t" to 40, "p" to 50)

        var textModified = text.toLowerCase(Locale.getDefault())
        textModified = textModified.replace("b", "")

        val dataUnit = textModified.replace(Regex("[^a-z]"), "")
        val dataValue = textModified.replace(dataUnit, "").toDouble()

        val pow = m[dataUnit] ?: 0
        val usageInBytes = dataValue * 2.0.pow(pow)

        return usageInBytes.toLong()
    }

    private fun updateUI() {
        if (usageInfo?.isValid == true && usageInfo?.size ?: 0 > 0) {

            val lastObj = usageInfo?.get(0)
            if (lastObj == null) {
                scroll_view.visibility = View.GONE
                return
            } else {
                scroll_view.visibility = View.VISIBLE
            }

            val a = lastObj.totalUsageBytes?.toFloat() ?: 0F
            val b = lastObj.totalAvailableBytes?.toFloat() ?: 1F
            progress_bar.setProgressWithAnimation(a / b, 1000)

            usage_today.text = getBytesToHumanReadable(lastObj.usageTodayBytes ?: 0)

            val c = getBytesToHumanReadable(lastObj.totalUsageBytes ?: 0)
            val d = getBytesToHumanReadable(lastObj.totalAvailableBytes ?: 0)
            total_usage.text = "$c of $d"

            service_name.text = lastObj.serviceName

            val sdf = SimpleDateFormat("dd/MM/yy hh:mm a")
            val netDate = Date(lastObj.timestamp ?: 0)
            val date = sdf.format(netDate)
            timestamp.text = "Last checked on $date"
        } else {
            scroll_view.visibility = View.GONE
        }
    }

    private fun getBytesToHumanReadable(dataInBytes: Long): String {

        val lookUpKey = log2(dataInBytes.toDouble()).toInt() / 10
        val m = mapOf(1 to "KB", 2 to "MB", 3 to "GB", 4 to "TB", 5 to "PB")

        return "%.2f ${m[lookUpKey]}".format(dataInBytes / 2.0.pow(lookUpKey * 10))
    }

    private fun showError() {
        Snackbar.make(layout_root, "Are you connected to the WiFi?", Snackbar.LENGTH_LONG).show()
    }
}