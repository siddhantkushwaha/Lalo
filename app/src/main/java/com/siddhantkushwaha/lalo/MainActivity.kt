package com.siddhantkushwaha.lalo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    private lateinit var retrofitAPI: RetrofitAPI

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        retrofitAPI = RetrofitAPI()

        populateInfo()
    }

    private fun populateInfo() {
        retrofitAPI.getUsageQuota(object : Callback<UsageQuotaResp> {
            override fun onResponse(
                call: Call<UsageQuotaResp>,
                response: Response<UsageQuotaResp>
            ) {
                val usageQuotaResp = response.body()
                if (response.isSuccessful && usageQuotaResp != null && usageQuotaResp.resultCode == 200 && usageQuotaResp.rows?.size ?: 0 > 0) {

                    try {

                        val result = usageQuotaResp.rows!![0]
                        val usageToday = result["dailyTotalUsage"]!!
                        val totalUsage = result["totalUsage"]
                        val serviceName = result["serviceName"]!!

                        // TODO - process above values

                    } catch (exception: Exception) {
                        // TODO - failure message
                        exception.printStackTrace()
                    }

                } else {
                    // TODO - failure message
                    Log.e(TAG, "${response.isSuccessful} ${usageQuotaResp?.msg}")
                }
            }

            override fun onFailure(call: Call<UsageQuotaResp>, t: Throwable) {
                // TODO - failure message
                t.printStackTrace()
            }
        })
    }
}