package com.siddhantkushwaha.lalo

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

class RetrofitAPI {

    private interface GetUsageQuotaInterface1 {
        @GET("portal/fetchUserQuotaPM.do")
        fun request(): Call<UsageQuotaResp>
    }

    private interface GetLocationInterface {

    }

    private interface GetUsageQuotaInterface2 {

    }

    companion object {
        private fun getRetrofitObject(baseUrl: String): Retrofit {
            return Retrofit.Builder().baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }

        public fun getUsageQuotaMethod1(): Response<UsageQuotaResp> {
            val baseUrl = "https://redirect2.bbportal.bsnl.co.in/"
            val retrofit = getRetrofitObject(baseUrl)

            val usageQuotaInterface = retrofit.create(GetUsageQuotaInterface1::class.java)
            val call = usageQuotaInterface.request()
            return call.execute()
        }

        public fun getUsageQuotaMethod2(callback: Callback<UsageQuotaResp>) {
            val baseUrl = "https://fuptopup.bsnl.co.in/"
            val retrofit = getRetrofitObject(baseUrl)
            Retrofit.Builder().baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
    }
}