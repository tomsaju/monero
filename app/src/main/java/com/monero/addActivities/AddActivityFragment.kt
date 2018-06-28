package com.monero.addActivities


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*

import com.monero.R
import com.monero.models.Tag
import com.monero.models.User
import kotlinx.android.synthetic.main.new_activity_fragment.*
import me.gujun.android.taggroup.TagGroup


/**
 * A simple [Fragment] subclass.
 */
public class AddActivityFragment : Fragment() {

    lateinit var title:AutoCompleteTextView;
    lateinit var description:AutoCompleteTextView;
    lateinit var modeSelector:Spinner
    lateinit var addTagButton:ImageButton
    lateinit var addMembersButton:ImageButton
    lateinit var tagContainer:TagGroup
    lateinit var memberListParent:LinearLayout
    lateinit var addTagText:TextView
    lateinit var addMemberBanner:TextView
    lateinit var doneButton:TextView
    lateinit var cancelButton:TextView
    lateinit var selectedList:ArrayList<User>
    lateinit var selectedTagList:ArrayList<Tag>

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        var view = inflater!!.inflate(R.layout.new_activity_fragment, container, false);
        title = view.findViewById(R.id.title_activity_autotextview)
        description = view.findViewById(R.id.description_activity_autotextview)
        modeSelector = view.findViewById(R.id.mode_spinner_activity)
        addTagButton = view.findViewById(R.id.add_tag_button)
        addMembersButton = view.findViewById(R.id.add_members_button)
        /*tagContainer = view.findViewById(R.id.container)
        memberListParent = members_layout
        addTagText = add_tag_text
        addMemberBanner = add_mebers_banner
        doneButton = done_button_new_activity
        cancelButton = cancel_button_new_activity*/
        selectedList = ArrayList(emptyList<User>())

       /* doneButton.setOnClickListener {
            v: View? ->
            {
                if(checkifInputValid()){

                }
            }
        }*/

        return view;
    }

    private fun checkifInputValid(): Boolean {
        var valid:Boolean = true
        if(title.text.isEmpty()){
            Toast.makeText(context,"Enter a valid title",Toast.LENGTH_SHORT)
            valid=false
        }else if(description.text.isEmpty()){
            Toast.makeText(context,"Enter a description",Toast.LENGTH_SHORT)
            valid=false
        }

        if(selectedList.isEmpty()){
            Toast.makeText(context,"Please add members",Toast.LENGTH_SHORT)
            valid=false
        }

        return  valid

    }

}// Required empty public constructor
