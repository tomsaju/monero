package com.monero.activitydetail.banner

import android.content.Context
import android.support.v4.view.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.monero.Application.ApplicationController
import com.monero.R
import com.monero.models.BannerItem
import com.monero.utility.Utility

/**
 * Created by Dreamz on 13-10-2018.
 */

class BannerViewPager(var mContext:Context,var bannerItem:BannerItem): PagerAdapter() {


    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container!!.removeView(`object` as View)
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view==`object`

    }

    override fun getCount(): Int {
        return BannerPagerEnum.values().size
    }


    override fun instantiateItem(collection: ViewGroup, position:Int): View {


        var customPagerEnum:BannerPagerEnum = BannerPagerEnum.values()[position]
        var inflater = LayoutInflater.from(mContext);
        var layout =  inflater.inflate(customPagerEnum.getLayoutResId(), collection, false) as ViewGroup;
        var totalTv = layout.findViewById<TextView>(R.id.total_expense_value)
        var myExpenseValue = layout.findViewById<TextView>(R.id.my_expenses_value)
        var youoweValue = layout.findViewById<TextView>(R.id.you_owe_value)
        var theyOweValue = layout.findViewById<TextView>(R.id.they_owe_value)


        var prefix = Utility.getCurrencySymbol()

        totalTv.text = prefix + bannerItem.total
        myExpenseValue.text = prefix+ bannerItem.iSpend
        youoweValue.text =prefix+ bannerItem.iOwe
        theyOweValue.text =prefix+ bannerItem.theyOwe

        collection.addView(layout);
        return layout;
    }


}