package com.sgpublic.scit.tool.manager

class Security {
    companion object {
        init {
            System.loadLibrary("scitedutool_secret")
        }

        external fun getAppKey(): String
        external fun getAppSecret(): String
    }
}