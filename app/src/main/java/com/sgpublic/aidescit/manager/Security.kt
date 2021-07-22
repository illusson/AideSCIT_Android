package com.sgpublic.aidescit.manager

class Security {
    companion object {
        init {
            System.loadLibrary("scitedutool_secret")
        }

        external fun getAppKey(): String
        external fun getAppSecret(): String
    }
}