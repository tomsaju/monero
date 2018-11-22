package com.monero.main.fragments

import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.preference.PreferenceFragment
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.widget.PopupMenu
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.monero.R
import com.monero.Views.CircularProfileImage
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.profile_fragment.*
import java.io.IOException
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import com.monero.Application.ApplicationController
import com.monero.helper.ImageSaver
import com.monero.helper.PreferenceManager
import com.monero.models.User
import com.mynameismidori.currencypicker.CurrencyPicker
import com.mynameismidori.currencypicker.CurrencyPickerListener
import net.glxn.qrgen.android.QRCode
import java.io.ByteArrayOutputStream
import java.util.*


/**
 * Created by tom.saju on 3/6/2018.
 */
class ProfileFragment:Fragment() {
    val auth = FirebaseAuth.getInstance()!!
     lateinit var profileImageview:CircleImageView
    val CAMERA = 5
    val GALLERY =6
    lateinit var imageSelectDialog:AlertDialog
    var storage: FirebaseStorage? = null
    var storageReference: StorageReference? = null
    var myImageReference:StorageReference? = null
    lateinit var currencySelectionMenuItem:RelativeLayout
    lateinit var currencySubtitle:TextView
    lateinit var userName:TextView
    lateinit var userEmail:TextView
    lateinit var userPhone:TextView
    lateinit var seeQRcode:TextView

    var prefListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
        if(key == ApplicationController.preferenceManager?.PREFERRED_CURRENCY_CODE ||
                key == ApplicationController.preferenceManager?.PREFERRED_CURRENCY_NAME ||
                key == ApplicationController.preferenceManager?.PREFERRED_CURRENCY_SYMBOL){

            if(currencySubtitle!=null) {
                currencySubtitle.text=ApplicationController.preferenceManager?.preferredCurrencyName
            }

        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        storage = FirebaseStorage.getInstance();
        if(storage!=null) {
            val firebaseStorage = storage
            storageReference = firebaseStorage?.getReference();
        }

         // Create a reference to 'images/mountains.jpg'
         myImageReference = storageReference?.child("displayImages/"+auth.currentUser?.uid+".jpg")
         ApplicationController.preferenceManager?.prefs?.registerOnSharedPreferenceChangeListener(prefListener)


    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var rootView = inflater?.inflate(R.layout.profile_fragment,container,false)
        //var name:TextView = rootView?.findViewById<TextView>(R.id.username_display) as TextView
         profileImageview = rootView.findViewById<CircleImageView>(R.id.profileImage)
         currencySelectionMenuItem = rootView.findViewById<RelativeLayout>(R.id.select_currency_row)
         currencySubtitle = rootView.findViewById(R.id.select_currency_row_subtitle)
         userName = rootView.findViewById(R.id.text_name)
         userEmail = rootView.findViewById(R.id.user_email_tv)
         userPhone = rootView.findViewById(R.id.user_phone_tv)
         seeQRcode = rootView.findViewById(R.id.see_qr_code)



         var displayPhoto = ApplicationController.preferenceManager!!.myDisplayPicture
        if(displayPhoto!=null&&displayPhoto.isNotEmpty()) {
            Glide.with(this).load(displayPhoto).into(profileImageview);
        }

        if(auth?.currentUser!=null){
            userName.text=auth?.currentUser?.displayName
            userPhone.text = auth?.currentUser?.phoneNumber
            if(!auth?.currentUser?.email.isNullOrEmpty()){
                userEmail.text = auth?.currentUser?.email
            }else{
                userEmail.text = "Not available"
            }
        }

        if(currencySubtitle!=null) {
            currencySubtitle.text=ApplicationController.preferenceManager?.preferredCurrencyName
        }

        profileImageview.setOnClickListener { _:View->
            showImagePopup()
        }

        currencySelectionMenuItem.setOnClickListener {
            showCurrencySelectDialog()
        }

        if(auth.currentUser!=null){
           // name.text = auth.currentUser?.displayName.toString()
        }else{
          //  name.text = "No users signed in"
        }

        seeQRcode.setOnClickListener {

            var userId = auth?.currentUser?.uid
            var userPhone = auth?.currentUser?.phoneNumber
            var userEmail = auth?.currentUser?.email
            var displayName = auth?.currentUser?.displayName

            var myContact = User(userId!!,displayName!!,userPhone!!,userEmail!!)
            var gson = Gson()

            var userJson = gson.toJson(myContact)

            showDialog(userJson)


        }
        return rootView
    }

    private fun showCurrencySelectDialog() {
        var picker:CurrencyPicker = CurrencyPicker.newInstance("Select Currency")  // dialog title
        picker.setListener({ name, code, symbol, flagDrawableResID ->

            ApplicationController.preferenceManager?.preferredCurrencyCode = code
            ApplicationController.preferenceManager?.preferredCurrencySymbol = symbol
            ApplicationController.preferenceManager?.preferredCurrencyName = name

            picker.dismiss()
        })
        picker.show(activity?.supportFragmentManager, "CURRENCY_PICKER")
    }


    fun uploadImage(file:Bitmap){

        var progressDialog =  ProgressDialog(requireContext());
        progressDialog.setTitle("Uploading...");
        progressDialog.show();

        val baos = ByteArrayOutputStream()
        file.compress(Bitmap.CompressFormat.JPEG, 50, baos)
        val data = baos.toByteArray()

        var uploadTask = myImageReference?.putBytes(data)
        uploadTask?.addOnFailureListener { exception ->
            // Handle unsuccessful uploads
            Log.d("Profile","Error uploading"+exception.stackTrace)
            progressDialog.dismiss()
        }?.addOnSuccessListener { taskSnapshot ->
            // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
            // ...

            val ONE_MEGABYTE: Long = 1024 * 1024
            myImageReference?.getBytes(ONE_MEGABYTE)?.addOnSuccessListener {bytes ->
                // Data for "images/island.jpg" is returned, use this as needed

                var options =  BitmapFactory.Options()
                options.inMutable = true
                var bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options);


                ImageSaver(requireContext())
                        .setFileName(ApplicationController.preferenceManager!!.myUid+".jpg")
                        .setExternal(false)//image save in external directory or app folder default value is false
                        .setDirectory("profile")
                        .save(bmp); //Bitmap from your code


            }?.addOnFailureListener {
                // Handle any errors
            }


            myImageReference?.downloadUrl?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result
                    Glide.with(this).load(downloadUri).into(profileImageview);
                    ApplicationController.preferenceManager!!.myDisplayPicture = downloadUri.toString()
                    progressDialog.dismiss()
                } else {
                    // Handle failures
                    // ...
                }
            }
                Log.d("Profile", "Upload Successfull")
            }

    }

    fun showImagePopup(){



        //
        var popup: PopupMenu? = null;
        popup = PopupMenu(requireContext(), profileImage)
        popup.inflate(R.menu.profile_menu)

        popup.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item: MenuItem? ->

            when (item!!.itemId) {
                R.id.edit_image -> {
                   pickImage()
                }
                R.id.delete_image -> {

                }

            }

            true
        })

        popup.show()

        //


    }

    fun pickImage(){

        val builder = AlertDialog.Builder(requireContext())
        // Seems ok to inflate view with null rootView
        val view = layoutInflater.inflate(R.layout.profile_image_pick_image_layout, null)
        builder.setView(view)
        //var pickerView = LayoutInflater.from(requireContext()).inflate(R.layout.profile_image_pick_image_layout,null,false)
        var galleryLayout = view.findViewById<LinearLayout>(R.id.useGallery)
        var cameraLayout = view.findViewById<LinearLayout>(R.id.useCamera)

        galleryLayout.setOnClickListener({_:View ->

           choosePhotoFromGallary()
            imageSelectDialog.dismiss()

        })
        cameraLayout.setOnClickListener({_:View ->

            takePhotoFromCamera()
            imageSelectDialog.dismiss()

        })

        builder.setPositiveButton(null,null)
        builder.setNegativeButton(null,null)
        imageSelectDialog = builder.show()
        imageSelectDialog.show()

    }

    fun choosePhotoFromGallary() {
        val galleryIntent = Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

        startActivityForResult(galleryIntent, GALLERY)
    }

    private fun takePhotoFromCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, CAMERA)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == GALLERY)
        {
            if (data != null)
            {
                val contentURI = data!!.data
                try
                {
                    val bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, contentURI)
                    uploadImage(bitmap)

                }
                catch (e: IOException) {
                    e.printStackTrace()

                }

            }

        }
        else if (requestCode == CAMERA)
        {
            val thumbnail = data!!.extras!!.get("data") as Bitmap
            uploadImage(thumbnail)
        }

    }

    private fun showDialog(content: String) {
        var dialogs = Dialog(activity)
        dialogs.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogs.setCancelable(true)
        dialogs.setContentView(R.layout.show_qr_dialog)
        val imageview = dialogs.findViewById<ImageView>(R.id.qr_container) as ImageView
        var myBitmap = QRCode.from(content).bitmap()
        imageview.setImageBitmap(myBitmap);
        dialogs.show()

    }


}

