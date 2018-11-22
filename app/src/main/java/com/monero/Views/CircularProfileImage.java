package com.monero.Views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.StorageReference;
import com.monero.Application.ApplicationController;
import com.monero.R;

import java.io.File;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by tom.saju on 7/11/2018.
 */

public class CircularProfileImage extends ConstraintLayout {
    private static final String TAG = "CircularProfileImage";
    Context context;
    private View rootView;
    CircleImageView profileImage;
    TextView profileTextView;
    Button closeButton;
    Drawable mDrawable;
    ICircularProfileImageListener mCircularProfileImageListener;
    private int circleRadius;
    private boolean cancelable;
    public String labelText;
    private int deviceWidth;
    private String id;
    private TextView userId;
    private TextView useremail;
    private TextView userphone;
    private TextView username;
    private String email;
    private String phone;
    private StorageReference downloadReference;
    private File file;
    private RequestOptions options;

    public CircularProfileImage(Context context) {
        super(context);
        this.context = context;
        Log.d(TAG, "CircularProfileImage() returned: ");
        init();
    }

    public CircularProfileImage(Context context, Drawable profileImage, String name,String phone,String email,boolean cancelable, String id) {
        super(context);
        this.context = context;
        Log.d(TAG, "CircularProfileImage() returned: ");
        mDrawable = profileImage;
        labelText = name;
        this.email = email;
        this.phone = phone;
        this.cancelable = cancelable;
        this.id=id;
        init();
    }

    public CircularProfileImage(Context context, @NonNull StorageReference profileImageUrl, String name,String phone,String email,boolean cancelable, String id) {
        super(context);
        this.context = context;
        Log.d(TAG, "CircularProfileImage() returned: ");
        mDrawable = null;
        this.downloadReference = profileImageUrl;
        labelText = name;
        this.email = email;
        this.phone = phone;
        this.cancelable = cancelable;
        this.id=id;
        init();
    }


    public CircularProfileImage(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        Log.d(TAG, "CircularProfileImage() returned: ");
        TypedArray ta = context.getTheme().obtainStyledAttributes(attrs, R.styleable.profile, 0, 0);
        mDrawable = ta.getDrawable(R.styleable.profile_src);
        cancelable = ta.getBoolean(R.styleable.profile_cancelable,true);
        labelText = ta.getString(R.styleable.profile_labelText);
        init();
        ta.recycle();

        //barHeight = ta.getDimensionPixelSize(R.styleable.ValueBar_barHeight, 0);
    }

    public CircularProfileImage(Context context, AttributeSet attrs, int defStyleAttr) {

        super(context, attrs, defStyleAttr);
        Log.d(TAG, "CircularProfileImage() returned: ");
        this.context = context;
        TypedArray ta = context.getTheme().obtainStyledAttributes(attrs, R.styleable.profile, 0, 0);
        mDrawable = ta.getDrawable(R.styleable.profile_src);
        cancelable = ta.getBoolean(R.styleable.profile_cancelable,true);
        labelText = ta.getString(R.styleable.profile_labelText);
        init();
        ta.recycle();
    }


    public void setProfileImageListener(ICircularProfileImageListener mCircularProfileImageListener){
        this.mCircularProfileImageListener = mCircularProfileImageListener;
    }


    private void init(){
        Log.d(TAG, "init() returned: " );
        //do setup work here
        setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        final Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        Point deviceDisplay = new Point();
        display.getSize(deviceDisplay);
        deviceWidth = deviceDisplay.x;

        rootView = inflate(context, R.layout.circular_profile_item, this);
        profileImage = (CircleImageView) rootView.findViewById(R.id.imageview);
        profileTextView = (TextView) rootView.findViewById(R.id.name_text);
        useremail = (TextView) rootView.findViewById(R.id.user_email);
        userphone = (TextView) rootView.findViewById(R.id.user_phone);
        username = (TextView) rootView.findViewById(R.id.user_name);
        closeButton = (Button) rootView.findViewById(R.id.close_button);
        options = new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.default_profile)
                .error(R.drawable.default_profile)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .priority(Priority.HIGH);

        if(mDrawable !=null){

            Glide.with(context).load(mDrawable).into(profileImage);
        }else{
            if(downloadReference!=null){
                downloadReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if(task.isSuccessful()) {
                            String downloadUri = task.getResult().toString();
                            Glide.with(context).load(downloadUri).into(profileImage);
                        }else{
                            Glide.with(context).load(context.getResources().getDrawable(R.drawable.default_profile)).into(profileImage);
                        }
                    }
                });
            }else{




                try {
                    String path = context.getFilesDir().getAbsolutePath() + "/profile";
                    file = new  File(path + "/" +id + ".jpg");
                    Uri uri = Uri.fromFile(file);
                    Glide.with(context).load(uri).apply(options).into(profileImage);
                } catch (Exception e) {
                    e.printStackTrace();
                    Glide.with(context).load(context.getResources().getDrawable(R.drawable.default_profile)).into(profileImage);
                }
            }


        }


            if(labelText!=null&&!labelText.isEmpty()) {
                profileTextView.setText(labelText);
            }else if(phone!=null&&!phone.isEmpty()){
                profileTextView.setText(phone);
            }else if(email!=null&&!email.isEmpty()){
                profileTextView.setText(email);
            }


        if(labelText!=null&&!labelText.isEmpty()) {
            username.setText(labelText);
        }

        if(id!=null&&!id.isEmpty()){
            profileTextView.setTag(id);
        }

        if(email!=null&&!email.isEmpty()){

            if(useremail!=null){
                useremail.setText(email);
            }
        }
        if(phone!=null&&!phone.isEmpty()){

            if(userphone!=null){
                userphone.setText(phone);
            }
        }

        if(cancelable){
            closeButton.setVisibility(View.VISIBLE);
        }else{
            closeButton.setVisibility(View.GONE);
        }

        closeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mCircularProfileImageListener!=null) {
                    mCircularProfileImageListener.onProfileClosed(username.getText().toString(),userphone.getText().toString(),useremail.getText().toString());
                }
            }
        });

        Log.d(TAG, "init() called "+labelText );

    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        Log.d(TAG, "onLayout() returned: " );
        final int count = getChildCount();
        int curWidth, curHeight, curLeft, curTop, maxHeight;
        //get the available size of child view
        final int childLeft = this.getPaddingLeft();
        final int childTop = this.getPaddingTop();
        final int childRight = this.getMeasuredWidth() - this.getPaddingRight();
        final int childBottom = this.getMeasuredHeight() - this.getPaddingBottom();
        final int childWidth = childRight - childLeft;
        final int childHeight = childBottom - childTop;
        maxHeight = 0;
        curLeft = childLeft;
        curTop = childTop;
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == GONE)
                return;
            //Get the maximum size of the child
            child.measure(MeasureSpec.makeMeasureSpec(childWidth, MeasureSpec.AT_MOST), MeasureSpec.makeMeasureSpec(childHeight, MeasureSpec.AT_MOST));
            curWidth = child.getMeasuredWidth();
            curHeight = child.getMeasuredHeight();
            //wrap is reach to the end
            if (curLeft + curWidth >= childRight) {
                curLeft = childLeft;
                curTop += maxHeight;
                maxHeight = 0;
            }
            //do the layout
            child.layout(curLeft, curTop, curLeft + curWidth, curTop + curHeight);
            //store the max height
            if (maxHeight < curHeight)
                maxHeight = curHeight;
            curLeft += curWidth;
        }
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.d(TAG, "onMeasure() returned: " );
        int count = getChildCount();
        // Measurement will ultimately be computing these values.
        int maxHeight = 0;
        int maxWidth = 0;
        int childState = 0;
        int mLeftWidth = 0;
        int rowCount = 0;
        // Iterate through all children, measuring them and computing our dimensions
        // from their size.
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() == GONE)
                continue;
            // Measure the child.
            measureChild(child, widthMeasureSpec, heightMeasureSpec);
            maxWidth += Math.max(maxWidth, child.getMeasuredWidth());
            mLeftWidth += child.getMeasuredWidth();
            if ((mLeftWidth / deviceWidth) > rowCount) {
                maxHeight += child.getMeasuredHeight();
                rowCount++;
            } else {
                maxHeight = Math.max(maxHeight, child.getMeasuredHeight());
            }
            childState = combineMeasuredStates(childState, child.getMeasuredState());
        }
        // Check against our minimum height and width
        maxHeight = Math.max(maxHeight, getSuggestedMinimumHeight());
        maxWidth = Math.max(maxWidth, getSuggestedMinimumWidth());
        // Report our final dimensions.
        setMeasuredDimension(resolveSizeAndState(maxWidth, widthMeasureSpec, childState),
                resolveSizeAndState(maxHeight, heightMeasureSpec, childState << MEASURED_HEIGHT_STATE_SHIFT));
    }

    public Drawable getmDrawable() {
        return mDrawable;
    }

    public void setmDrawable(Drawable mDrawable) {
        this.mDrawable = mDrawable;
        requestLayout();
        invalidate();
    }

    public boolean isCancelable() {
        return cancelable;
    }

    public void setCancelable(boolean cancelable) {
        this.cancelable = cancelable;
        requestLayout();
        invalidate();
    }

    public String getLabelText() {
        return labelText;
    }

    public void setLabelText(String labelText) {
        this.labelText = labelText;
        requestLayout();
        invalidate();
    }


    public interface ICircularProfileImageListener{
        void onProfileClosed(String name,String phone,String email);
    }
}
