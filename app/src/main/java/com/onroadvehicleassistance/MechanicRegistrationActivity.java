package com.onroadvehicleassistance;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.onroadvehicleassistance.LocationUtil.GPS_Service;
import com.onroadvehicleassistance.model.MechanicModel;
import com.onroadvehicleassistance.utils.Constants;
import com.onroadvehicleassistance.utils.ImageUtils;
import com.onroadvehicleassistance.utils.NetworkUtils;
import com.onroadvehicleassistance.utils.toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

public class MechanicRegistrationActivity extends AppCompatActivity {
    private static final String TAG = "MechanicRegistrationActivity";
    private FirebaseAuth firebaseAuth;
    // instance for firebase storage and StorageReference
    FirebaseStorage storage;
    StorageReference storageReference;

    double latitude;
    double longitude;

    GPS_Service gps_service;

    private EditText mechanicAddress,mechanicName,mechanicDesc,mechanicPhone,mechanicPincode;
    private ImageView mechanicImage;

    public static final int IMAGE_REQ=20032;
    int select_image;
    String imageurl,mechanicNameString,mechanicDescString,mechanicAddressString,mechanicFindLocation,
            mechanicPhoneString,mechanicCityString;
    String from;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle("Mechanic Registration");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_mechanic_registration);
        firebaseAuth = FirebaseAuth.getInstance();// authentication
        storage = FirebaseStorage.getInstance();//images data storage
        storageReference = storage.getReference();
        startService(new Intent(MechanicRegistrationActivity.this, GPS_Service.class));// gps lat long
        inits();
        Bundle extras = getIntent().getExtras();
        if(extras == null) {

        } else {
            from = extras.getString("from");
            if(from.equals("admin")) {
                mechanicPincode.setVisibility(View.VISIBLE);
            }else {
                mechanicPincode.setVisibility(View.GONE);
            }
        }

    }

    private void inits() {
        mechanicPincode = findViewById(R.id.mechanicPincode);
        findViewById(R.id.locationIcon).setOnClickListener(v->{

            gps_service = new GPS_Service(MechanicRegistrationActivity.this, "10");
            if (gps_service.canGetLocation()) {
                latitude = gps_service.getLatitude();
                longitude = gps_service.getLongitude();
                if (getAddress(MechanicRegistrationActivity.this,latitude,longitude)!=null) {
                    mechanicAddress.setText(getAddress(MechanicRegistrationActivity.this, latitude, longitude));
                }else {
                    showToast("something went wrong!");
                }
        }
        });

        mechanicAddress = findViewById(R.id.mechanicAddress);
        mechanicImage = findViewById(R.id.mechanicImage);
        findViewById(R.id.addMechImage).setOnClickListener(v->{
            chooseImage(MechanicRegistrationActivity.this,IMAGE_REQ);
        });

        mechanicName = findViewById(R.id.mechanicName);
        mechanicDesc = findViewById(R.id.mechanicDescription);
        mechanicPhone = findViewById(R.id.mechanicPhone);

        findViewById(R.id.uploadMechDetails).setOnClickListener(v->{
            mechanicAddressString = mechanicAddress.getText().toString();
            mechanicNameString = mechanicName.getText().toString();
            mechanicDescString = mechanicDesc.getText().toString();
            mechanicPhoneString = mechanicPhone.getText().toString();
            if (mechanicNameString.equals("")||mechanicAddressString.equals("")||
                    mechanicDescString.equals("")||mechanicPhoneString.equals("")){
                toast.message(getApplicationContext(),"Please enter all details!");
            }else {
                //String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                if (mechanicPhoneString.length()!=10){
                    toast.message(getApplicationContext(),"Please enter correct phone number!");
                }else {
                    if (from.equals("admin")){
                        String sPincode = mechanicPincode.getText().toString();
                        if (sPincode.equals("")){
                            toast.meActivity(this,"Pincode cannot be empty!");
                        }else {
                            MechanicModel mechanicModel = new MechanicModel(mechanicNameString, imageurl,
                                    mechanicDescString, mechanicAddressString,
                                    sPincode, mechanicPhoneString, mechanicCityString);
                            uploadDetails(mechanicModel);
                        }
                    }else {
                        MechanicModel mechanicModel = new MechanicModel(mechanicNameString, imageurl,
                                mechanicDescString, mechanicAddressString,
                                mechanicFindLocation, mechanicPhoneString, mechanicCityString);
                        uploadDetails(mechanicModel);
                    }
                }
            }
        });

    }

    private void uploadDetails(MechanicModel mechanicModel) {
        if (imageurl!=null) {

            FirebaseFirestore.getInstance()
                    .collection(Constants.COLLECTION_PATH)
                    .add(mechanicModel)
                    .addOnSuccessListener(documentReference -> {
                        Log.d(TAG, "onSuccess: Successfully added note");
                        imageurl="";
                        Toast.makeText(MechanicRegistrationActivity.this, "Mechanic Registration Successful!", Toast.LENGTH_SHORT).show();
                        onBackPressed();
                    })
                    .addOnFailureListener(e -> Toast.makeText(MechanicRegistrationActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show());

        }
    }

    private void chooseImage(Context context, int requestCode) {
        final CharSequence[] optionsMenu = {"Take Photo","Choose from Gallery", "Exit" }; // create a menuOption Array
        // create a dialog for showing the optionsMenu
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        // set the items in builder
        builder.setItems(optionsMenu, (dialogInterface, i) -> {
            if(optionsMenu[i].equals("Take Photo")){
                // Open the camera and get the photo
                if (NetworkUtils.isNetworkAvailable(getApplicationContext())){
                    select_image=0;
                    Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(takePicture, requestCode);
                }else {
                    toast.message(getApplicationContext(),"Network not available!");
                }
            }
            else if(optionsMenu[i].equals("Choose from Gallery")){
                // choose from  external storage
                if (NetworkUtils.isNetworkAvailable(getApplicationContext())){
                    select_image=1;
                    Intent intenth = new Intent(Intent.ACTION_PICK,
                            MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                    intenth.setType("image/*");
                    intenth.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION );
                    intenth.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(intenth , requestCode);
                }else {
                    toast.message(getApplicationContext(),"Network not available!");
                }


            }
            else if (optionsMenu[i].equals("Exit")) {
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }

    public Uri getImageUri(Bitmap src, Bitmap.CompressFormat format, int quality) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        src.compress(format, quality, os);

        String path = MediaStore.Images.Media.insertImage(getContentResolver(), src, "title", null);
        return Uri.parse(path);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_REQ) {
                if (select_image==0){
                    //if image clicked
                    Bitmap photo = (Bitmap)data.getExtras().get("data");
                    Uri img = getImageUri(photo, Bitmap.CompressFormat.JPEG,50);
                    uploadImage(img);
                }else if (select_image==1) {
                    try {
                        //if image selected from db
                        Uri imageUri = data.getData();
                        String imagePath= ImageUtils.getPath(getApplicationContext(), imageUri);
                        Glide.with(MechanicRegistrationActivity.this).load(imagePath).into(mechanicImage);
                        uploadImage(imageUri);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        }
    }
    private void uploadImage(Uri filePath) {
        if (filePath != null) {

            // Code for showing progressDialog while uploading
            ProgressDialog progressDialog
                    = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            // Defining the child of storageReference
            StorageReference ref = storageReference.child("images/" + UUID.randomUUID().toString());

            // adding listeners on upload
            // or failure of image
            // Progress Listener for loading
            // percentage on the dialog box
            ref.putFile(filePath)
                    .addOnSuccessListener(
                            taskSnapshot -> {
                                // taskSnapshot.getUploadSessionUri().
                                // Image uploaded successfully
                                // Dismiss dialog
                                progressDialog.dismiss();
                                Toast.makeText(MechanicRegistrationActivity.this, "Image Uploaded!!", Toast.LENGTH_SHORT).show();
                                String myurl  = taskSnapshot.getMetadata().getReference().getDownloadUrl().toString();
                                Log.d(TAG, "image uploading url " + myurl);

                                ref.getDownloadUrl().addOnCompleteListener(task -> {
                                    imageurl=task.getResult().toString();
                                    Glide.with(MechanicRegistrationActivity.this).load(imageurl).into(mechanicImage);
                                    Log.i("URL",imageurl);
                                });
                            })

                    .addOnFailureListener(e -> {

                        // Error, Image not uploaded
                        progressDialog.dismiss();
                        Toast.makeText(MechanicRegistrationActivity.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    })
                    .addOnProgressListener(
                            (com.google.firebase.storage.OnProgressListener<? super UploadTask.TaskSnapshot>) taskSnapshot -> {
                                double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                                progressDialog.setMessage("Uploaded " + (int) progress + "%");
                            });
        }
    }

    public String getAddress(Context context, double LATITUDE, double LONGITUDE){
        //Set Address
        String city = null;
        String displayAddress = null;
        try {
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);

            if (addresses != null && addresses.size() > 0) {
                String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                String address2 = addresses.get(0).getAddressLine(1); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()

                city = addresses.get(0).getLocality();
                String state = addresses.get(0).getAdminArea();
                String country = addresses.get(0).getCountryName();
                String postalCode = addresses.get(0).getPostalCode();
                String knownName = addresses.get(0).getFeatureName(); // Only if available else return NULL
                Log.d(TAG, "getAddress:  address " + address);
                Log.d(TAG, "getAddress:  city " + city);
                Log.d(TAG, "getAddress:  state " + state);
                Log.d(TAG, "getAddress:  postalCode " + postalCode);
                Log.d(TAG, "getAddress:  knownName " + knownName);
                Log.d(TAG, "getAddress:  address2 " + address2);

                mechanicFindLocation = postalCode;
                mechanicCityString = city;
                displayAddress = address;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return displayAddress;
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    public void showToast(String message)
    {
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        onBackPressed();
        return true;
    }
}