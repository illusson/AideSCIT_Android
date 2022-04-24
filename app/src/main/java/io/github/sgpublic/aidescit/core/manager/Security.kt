package io.github.sgpublic.aidescit.core.manager

object Security {
    init {
        System.loadLibrary("aidesecret")
    }

    external fun getAppKey(): String
    external fun getAppSecret(): String
}