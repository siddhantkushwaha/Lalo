package com.siddhantkushwaha.lalo

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

class RetrofitAPI {

    companion object {
        @JvmStatic
        private val baseUrl = "https://redirect2.bbportal.bsnl.co.in/"

        @JvmStatic
        private val retrofit =
            Retrofit.Builder().baseUrl(baseUrl).addConverterFactory(GsonConverterFactory.create())
                .build()
    }

    private interface GetUsageQuotaInterface {
        @GET("portal/fetchUserQuotaPM.do")
        fun request(): Call<UsageQuotaResp>
    }

    public fun getUsageQuota(callback: Callback<UsageQuotaResp>) {
        val usageQuotaInterface = retrofit.create(GetUsageQuotaInterface::class.java)
        val call = usageQuotaInterface.request()
        call.enqueue(callback)
    }
}