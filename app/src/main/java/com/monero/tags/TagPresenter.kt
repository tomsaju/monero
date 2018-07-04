package com.monero.tags

import android.arch.lifecycle.LiveData
import android.content.Context
import com.monero.models.Tag

/**
 * Created by tom.saju on 7/4/2018.
 */
class TagPresenter : ITagPresenter {


    var context: Context
    var view: ITagView

    constructor(context: Context, view: ITagView) {
        this.context = context
        this.view = view
    }



    override fun saveTag(tag: Tag) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getAllTags(): LiveData<List<Tag>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


}