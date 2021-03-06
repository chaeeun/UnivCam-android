package com.inuappcenter.univcam_android.activities

import android.support.v4.app.Fragment
import com.inuappcenter.univcam_android.fragments.SearchFragment


class SearchActivity : BaseFragmentActivity() {
    override fun createFragment(): Fragment {
        return SearchFragment.newInstance()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(0, 0)
        finish()
    }
}
