package com.monero.Views

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.monero.R
import kotlin.properties.Delegates

/**
 * Created by tom.saju on 3/20/2018.
 */
class ContactsView : FrameLayout {
    var isCancelable:Boolean =false
   // var textValue:String =" "
    var textValue: String by Delegates.observable(" ") {
        prop, old, new ->
        invalidate()
        requestLayout()
    }
    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        val view = View.inflate(context, R.layout.contacts_view_layout, null)
        val typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.ContactsView)
         this.textValue = typedArray.getString(R.styleable.ContactsView_text)
         this.isCancelable = typedArray.getBoolean(R.styleable.ContactsView_isCancelable,false)

        val textLabel:TextView = view.findViewById(R.id.contacts_view_name)
        val cancelbtn:ImageView = view.findViewById(R.id.cancel_button)

        if(isCancelable){
            cancelbtn.visibility = View.VISIBLE
            cancelbtn.setOnClickListener(OnClickListener {
                removeView(view)
            })
        }else{
            cancelbtn.visibility = View.INVISIBLE
        }

        textLabel.text = textValue
        typedArray.recycle()
        addView(view)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        val view = View.inflate(context, R.layout.contacts_view_layout, null)
        val typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.ContactsView)
        this.textValue = typedArray.getString(R.styleable.ContactsView_text)
        this.isCancelable = typedArray.getBoolean(R.styleable.ContactsView_isCancelable,false)


        val textLabel:TextView = view.findViewById(R.id.contacts_view_name)
        val cancelbtn:ImageView = view.findViewById(R.id.cancel_button)

        if(isCancelable){
            cancelbtn.visibility = View.VISIBLE
            cancelbtn.setOnClickListener(OnClickListener {
                removeView(view)
            })
        }else{
            cancelbtn.visibility = View.INVISIBLE
        }

        textLabel.text = textValue
        typedArray.recycle()
        addView(view)
    }

    constructor(context: Context,text:String,isCancelable: Boolean) : super(context) {
        val view = View.inflate(context, R.layout.contacts_view_layout, null)

        this.textValue = text
        this.isCancelable = isCancelable

        val textLabel:TextView = view.findViewById(R.id.contacts_view_name)
        val cancelbtn:ImageView = view.findViewById(R.id.cancel_button)


        if(isCancelable){
            cancelbtn.visibility = View.VISIBLE
            cancelbtn.setOnClickListener(OnClickListener {
                Log.i("tag","clicked")
                removeView(view)
            })
        }else{
            cancelbtn.visibility = View.INVISIBLE
        }


        addView(view)
        textLabel.text = textValue
    }


    fun getIsCancelable():Boolean{
        return this.isCancelable
    }

    fun setIsCancelable(isCancelable:Boolean){
        this.isCancelable = isCancelable

    }

    fun getText():String{
        return this.textValue
    }

    fun setText(textValue:String){
        this.textValue = textValue
      invalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
    }
}