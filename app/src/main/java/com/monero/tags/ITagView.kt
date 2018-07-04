package com.monero.tags

import android.arch.lifecycle.LiveData
import com.monero.models.Tag

/**
 * Created by tom.saju on 7/4/2018.
 */
interface ITagView {
    fun setTags( allTagList:LiveData<List<Tag>>)
}