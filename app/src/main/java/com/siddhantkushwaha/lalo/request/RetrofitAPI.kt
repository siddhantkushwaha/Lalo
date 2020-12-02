package com.siddhantkushwaha.lalo.request

import com.siddhantkushwaha.lalo.util.Util
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

class RetrofitAPI {

    private interface GetUsageQuotaInterface1 {
        @GET("portal/fetchUserQuotaPM.do")
        fun request(): Call<UsageQuotaResp>
    }

    private interface GetLocationInterface {
        @FormUrlEncoded
        @POST("getLocationByIP.do")
        fun request(@Field("actionName") actionName: String): Call<LocationResp>
    }

    private interface GetUsageQuotaInterface2 {
        @FormUrlEncoded
        @POST("fetchUserQuotaPM.do")
        fun request(@Field("location") location: String): Call<UsageQuotaResp>
    }

    companion object {
        private fun getRetrofitObject(baseUrl: String): Retrofit {
            return Retrofit.Builder().baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(Util.getUnsafeOkHttpClient())
                .build()
        }

        public fun getUsageQuotaMethod1(): Response<UsageQuotaResp> {
            val baseUrl = "https://redirect2.bbportal.bsnl.co.in/"
            val retrofit = getRetrofitObject(baseUrl)
            val usageQuotaInterface = retrofit.create(GetUsageQuotaInterface1::class.java)
            return usageQuotaInterface.request().execute()
        }

        public fun getLocation(): Response<LocationResp> {
            val baseUrl = "https://fuptopup.bsnl.co.in/"
            val retrofit = getRetrofitObject(baseUrl)
            val getLocationInterface = retrofit.create(GetLocationInterface::class.java)
            return getLocationInterface.request("manual").execute()
        }

        public fun getUsageQuotaMethod2(location: String): Response<UsageQuotaResp> {
            val baseUrl = "https://fuptopup.bsnl.co.in/"
            val retrofit = getRetrofitObject(baseUrl)
            val getUsageQuotaInterface2 = retrofit.create(GetUsageQuotaInterface2::class.java)
            return getUsageQuotaInterface2.request(location).execute()
        }
    }
}