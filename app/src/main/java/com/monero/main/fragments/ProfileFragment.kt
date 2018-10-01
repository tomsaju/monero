package com.monero.main.fragments

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.widget.PopupMenu
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
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
import com.monero.Application.ApplicationController
import java.io.ByteArrayOutputStream


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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        storage = FirebaseStorage.getInstance();
        if(storage!=null) {
            val firebaseStorage = storage
            storageReference = firebaseStorage?.getReference();
        }

        // Create a reference to 'images/mountains.jpg'
         myImageReference = storageReference?.child("displayImages/"+auth.currentUser?.uid+".jpg")



    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var rootView = inflater?.inflate(R.layout.profile_fragment,container,false)
        //var name:TextView = rootView?.findViewById<TextView>(R.id.username_display) as TextView
         profileImageview = rootView.findViewById<CircleImageView>(R.id.profileImage)
         var displayPhoto = ApplicationController.preferenceManager!!.myDisplayPicture
        if(displayPhoto!=null&&displayPhoto.isNotEmpty()) {
            Glide.with(this).load(displayPhoto).into(profileImageview);
        }
        profileImageview.setOnClickListener { _:View->
            showImagePopup()
        }

        if(auth.currentUser!=null){
           // name.text = auth.currentUser?.displayName.toString()
        }else{
          //  name.text = "No users signed in"
        }
        return rootView
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
}