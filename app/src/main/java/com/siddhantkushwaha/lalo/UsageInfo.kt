package com.siddhantkushwaha.lalo

import io.realm.RealmObject

open class UsageInfo : RealmObject() {
    var timestamp: Long? = null

    var usageTodayBytes: Long? = null
    var totalUsageBytes: Long? = null
    var totalAvailableBytes: Long? = null

    var serviceName: String? = null

    var bandwidth: String? = null
    var bandwidthAfterDataUsed: String? = null
}