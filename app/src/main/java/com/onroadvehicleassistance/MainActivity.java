package com.onroadvehicleassistance;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);

        initviews();
        requestPermissions();
    }

    private void initviews() {
        findViewById(R.id.mechanicRegistration).setOnClickListener(v->{
            startActivity(new Intent(getApplicationContext(),MechanicRegistrationActivity.class)
                    .putExtra("from","main"));
        });

        findViewById(R.id.userLogin).setOnClickListener(v->{
            startActivity(new Intent(getApplicationContext(),UserLoginRegistrationActivity.class));
        });

        findViewById(R.id.adminLogin).setOnClickListener(v->{
            startActivity(new Intent(getApplicationContext(),AdminLoginActivity.class));
        });
    }

    private void requestPermissions() {
        Log.d("TAG","enter");
        // below line is use to request permission in the current activity.
        // this method is use to handle error in runtime permissions
        if (Build.VERSION.SDK_INT >= 33) {
            Dexter.withContext(this)
                    // below line is use to request the number of permissions which are required in our app.
                    .withPermissions(android.Manifest.permission.CAMERA,
                            // below is the list of permissions
                            android.Manifest.permission.READ_MEDIA_IMAGES,
                            android.Manifest.permission.RECORD_AUDIO,
                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.CALL_PHONE)
                    // after adding permissions we are calling an with listener method.
                    .withListener(new MultiplePermissionsListener() {
                        @Override
                        public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                            // this method is called when all permissions are granted
                            if (multiplePermissionsReport.areAllPermissionsGranted()) {
                                // do you work now
                                Toast.makeText(MainActivity.this, "All the permissions are granted..", Toast.LENGTH_SHORT).show();
                            }
                            // check for permanent denial of any permission
                            if (multiplePermissionsReport.isAnyPermissionPermanentlyDenied()) {
                                // permission is denied permanently, we will show user a dialog message.
                                showSettingsDialog();
                            }
                        }

                        @Override
                        public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                            // this method is called when user grants some permission and denies some of them.
                            permissionToken.continuePermissionRequest();
                        }
                    }).withErrorListener(error -> {
                        // we are displaying a toast message for error message.
                        Toast.makeText(getApplicationContext(), "Error occurred! ", Toast.LENGTH_SHORT).show();
                    })
                    // below line is use to run the permissions on same thread and to check the permissions
                    .onSameThread().check();
        }else {
            Dexter.withContext(this)
                    // below line is use to request the number of permissions which are required in our app.
                    .withPermissions(android.Manifest.permission.CAMERA,
                            // below is the list of permissions
                            android.Manifest.permission.READ_EXTERNAL_STORAGE,
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.RECORD_AUDIO,
                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.CALL_PHONE)
                    // after adding permissions we are calling an with listener method.
                    .withListener(new MultiplePermissionsListener() {
                        @Override
                        public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                            // this method is called when all permissions are granted
                            if (multiplePermissionsReport.areAllPermissionsGranted()) {
                                // do you work now
                                Toast.makeText(MainActivity.this, "All the permissions are granted..", Toast.LENGTH_SHORT).show();
                            }
                            // check for permanent denial of any permission
                            if (multiplePermissionsReport.isAnyPermissionPermanentlyDenied()) {
                                // permission is denied permanently, we will show user a dialog message.
                                showSettingsDialog();
                            }
                        }

                        @Override
                        public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                            // this method is called when user grants some permission and denies some of them.
                            permissionToken.continuePermissionRequest();
                        }
                    }).withErrorListener(error -> {
                        // we are displaying a toast message for error message.
                        Toast.makeText(getApplicationContext(), "Error occurred! ", Toast.LENGTH_SHORT).show();
                    })
                    // below line is use to run the permissions on same thread and to check the permissions
                    .onSameThread().check();
        }
    }

    // below is the shoe setting dialog method which is use to display a dialogue message.
    private void showSettingsDialog() {
        // we are displaying an alert dialog for permissions
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        // below line is the title for our alert dialog.
        builder.setTitle("Need Permissions");

        // below line is our message for our dialog
        builder.setMessage("This app needs permission to use this feature. You can grant them in app settings.");
        builder.setPositiveButton("GOTO SETTINGS", (dialog, which) -> {
            // this method is called on click on positive button and on clicking shit button
            // we are redirecting our user from our app to the settings page of our app.
            dialog.cancel();
            // below is the intent from which we are redirecting our user.
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            intent.setData(uri);
            startActivityForResult(intent, 101);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            // this method is called when user click on negative button.
            dialog.cancel();
        });
        // below line is used to display our dialog
        builder.show();
    }
}