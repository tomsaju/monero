package com.monero.activitydetail.banner

import com.monero.R

/**
 * Created by Dreamz on 13-10-2018.
 */
enum class BannerPagerEnum {


    //RED(R.string.red, R.layout.banner_layout_activity_info),
    BLUE(R.string.blue, R.layout.banner_layout_divisions);
   // ORANGE(R.string.orange, R.layout.view_orange);

    private var mTitleResId:Int;
    private var mLayoutResId:Int;

   constructor(titleResId:Int,layoutResId:Int) {
        mTitleResId = titleResId;
        mLayoutResId = layoutResId;
    }

    fun getTitleResId():Int {
        return mTitleResId;
    }

    fun  getLayoutResId():Int {
        return mLayoutResId;
    }
}