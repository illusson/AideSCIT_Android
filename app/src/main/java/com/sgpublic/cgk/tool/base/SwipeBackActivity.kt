package com.sgpublic.cgk.tool.base

import android.annotation.SuppressLint
import android.os.Bundle;

import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

@SuppressLint("Registered")
abstract class SwipeBackActivity : SwipeBackActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
    }
}