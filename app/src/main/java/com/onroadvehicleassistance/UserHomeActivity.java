package com.onroadvehicleassistance;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.onroadvehicleassistance.LocationUtil.GPS_Service;
import com.onroadvehicleassistance.adapter.MechanicAdapter;
import com.onroadvehicleassistance.adapter.MechanicSearchAdapter;
import com.onroadvehicleassistance.model.MechanicModel;
import com.onroadvehicleassistance.utils.Constants;
import com.onroadvehicleassistance.utils.toast;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class UserHomeActivity extends AppCompatActivity implements FirebaseAuth.AuthStateListener, MechanicAdapter.MechanicListener {

    private static final String TAG = "UserHomeActivity";
    private FirebaseAuth firebaseAuth;

    double latitude;
    double longitude;

    GPS_Service gps_service;

    RecyclerView recyclerView;
    MechanicAdapter mechanicAdapter;

    // instance for firebase storage and StorageReference
    FirebaseStorage storage;
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_home);
        getSupportActionBar().setTitle("Home");
        firebaseAuth= FirebaseAuth.getInstance();
        // get the Firebase  storage reference
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        gps_service = new GPS_Service(UserHomeActivity.this, "10");
        inits();
    }

    private void inits() {
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        findMechanicCity();
        findViewById(R.id.floatingActionButton).setOnClickListener(view -> {
            findMechanic();
        });

        findViewById(R.id.floatingActionButtonRefresh).setOnClickListener(v->{
            //initRecyclerView(firebaseAuth.getCurrentUser());
            /*recyclerView.clearAnimation();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection(Constants.COLLECTION_PATH).get().addOnSuccessListener(queryDocumentSnapshots -> {
                //Log.e(TAG,queryDocumentSnapshots.getDocuments()+"");
                if (!queryDocumentSnapshots.isEmpty()) {
                    List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                    //Log.e(TAG, "list size"+String.valueOf(list.size()));
                    List<MechanicModel> models = queryDocumentSnapshots.toObjects(MechanicModel.class);
                    //Log.e(TAG,"getMechanicPhoneString "+models.get(0).getMechanicPhoneString());
                    MechanicSearchAdapter adapter = new MechanicSearchAdapter(models,this,UserHomeActivity.this);
                    //Log.e(TAG,adapter.getItemCount()+" item cpounf");
                    recyclerView.setAdapter(adapter);
                    toast.meActivity(UserHomeActivity.this,"Refresh Done!");
                }
            });*/
            findMechanicCity();
            toast.meActivity(UserHomeActivity.this,"Refresh Done!");
        });

    }

    private void findMechanicCity() {
        if (gps_service.canGetLocation()) {
            latitude = gps_service.getLatitude();
            longitude = gps_service.getLongitude();
            //city
            if (getAddress(UserHomeActivity.this,latitude,longitude,1)!=null) {
                String mechanicAdds = getAddress(UserHomeActivity.this, latitude, longitude,1);
                findAndDisplayMechanicCity(mechanicAdds);
                // mechanicAddress.setText(getAddress(UserHomeActivity.this, latitude, longitude));
            }else {
                toast.meActivity(UserHomeActivity.this,"something went wrong!");
            }
        }
    }

    private void findAndDisplayMechanicCity(String mechanicAdds) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(Constants.COLLECTION_PATH).whereEqualTo(Constants.mechanicCity,mechanicAdds).get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()) {
                List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                Log.e(TAG, String.valueOf(list.size()));
                List<MechanicModel> models = queryDocumentSnapshots.toObjects(MechanicModel.class);
                MechanicSearchAdapter adapter = new MechanicSearchAdapter(models,UserHomeActivity.this,UserHomeActivity.this);
                recyclerView.setAdapter(adapter);
            }else {
                toast.message(getApplicationContext(),"No Mechanic found in your City!");
            }
        });
    }

    private void findMechanic() {
        //pincode
        if (gps_service.canGetLocation()) {
            latitude = gps_service.getLatitude();
            longitude = gps_service.getLongitude();
            if (getAddress(UserHomeActivity.this,latitude,longitude,0)!=null) {
                String mechanicAdds = getAddress(UserHomeActivity.this, latitude, longitude,0);
                findAndDisplayMechanic(mechanicAdds);
               // mechanicAddress.setText(getAddress(UserHomeActivity.this, latitude, longitude));
            }else {
                toast.meActivity(UserHomeActivity.this,"something went wrong!");
            }
        }
    }

    private void findAndDisplayMechanic(String mechanicAdds) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(Constants.COLLECTION_PATH).whereEqualTo(Constants.mechanicFindingLocation,mechanicAdds).get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()) {
                List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                Log.e(TAG, String.valueOf(list.size()));
                List<MechanicModel> models = queryDocumentSnapshots.toObjects(MechanicModel.class);
                MechanicSearchAdapter adapter = new MechanicSearchAdapter(models,UserHomeActivity.this,UserHomeActivity.this);
                recyclerView.setAdapter(adapter);
            }else {
                toast.message(getApplicationContext(),"No Mechanic found in your location!");
            }
        });
    }

    public String getAddress(Context context, double LATITUDE, double LONGITUDE,int from){
        //Set Address
        String city = null;
        String displayAddress = null;
        try {
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);

            if (addresses != null && addresses.size() > 0) {
                String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                city = addresses.get(0).getLocality();
                String state = addresses.get(0).getAdminArea();
                String country = addresses.get(0).getCountryName();
                String postalCode = addresses.get(0).getPostalCode();
                String knownName = addresses.get(0).getFeatureName(); // Only if available else return NULL
                Log.d(TAG, "getAddress:  address" + address);
                Log.d(TAG, "getAddress:  city" + city);
                Log.d(TAG, "getAddress:  state" + state);
                Log.d(TAG, "getAddress:  postalCode" + postalCode);
                Log.d(TAG, "getAddress:  knownName" + knownName);
                if (from==0) {
                    displayAddress = postalCode;
                }else {
                    displayAddress = city;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return displayAddress;
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseAuth.getInstance().addAuthStateListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseAuth.getInstance().removeAuthStateListener(this);
        if(mechanicAdapter != null){
            mechanicAdapter.stopListening();
        }
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        if(firebaseAuth.getCurrentUser() == null){
            //startLoginActivity();
            return;
        }
        initRecyclerView(firebaseAuth.getCurrentUser());
    }

    public void initRecyclerView(FirebaseUser user){
        Query query = FirebaseFirestore.getInstance().collection(Constants.COLLECTION_PATH);

        FirestoreRecyclerOptions<MechanicModel> options = new FirestoreRecyclerOptions.Builder<MechanicModel>()
                .setQuery(query, MechanicModel.class)
                .build();
        Log.e(TAG,options.getSnapshots().toString());

        mechanicAdapter = new MechanicAdapter(options,this,UserHomeActivity.this);
        recyclerView.setAdapter(mechanicAdapter);

        mechanicAdapter.startListening();

    }

    final ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            if (direction == ItemTouchHelper.LEFT) {
                Toast.makeText(UserHomeActivity.this, "Deleting", Toast.LENGTH_SHORT).show();

                MechanicAdapter.MechanicViewHolder noteViewHolder = (MechanicAdapter.MechanicViewHolder) viewHolder;
                noteViewHolder.deletItem();

            }
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX,
                                float dY, int actionState, boolean isCurrentlyActive) {
            /*new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    .addBackgroundColor(ContextCompat.getColor(HomeActivity.this, R.color.delete))
                    .addActionIcon(R.drawable.ic_baseline_delete_24)
                    .create()
                    .decorate();*/

            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    };

    @Override
    public void handleEditNote(DocumentSnapshot snapshot) {

        try {
            MechanicModel model = snapshot.toObject(MechanicModel.class);
            MechanicModel noteModelSer= new MechanicModel(model.getMechanicName(),model.getMechanicImage(),
                    model.getMechanicDescription(),model.getMechanicAddress(),model.getMechanicFindingLocation(),
                    model.getMechanicPhoneString(),model.getMechanicCity());
        /*EditText editText = new EditText( this);
        editText.setText(model.getNoteDesc().toString());
        editText.setSelection(model.getNoteDesc().length());*/
            new AlertDialog.Builder(this)
                    .setTitle("Mechanic Details")
                    .setNegativeButton("Call to Mechanic", (dialogInterface, i) -> {
                        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + model.getMechanicPhoneString()));
                        startActivity(intent);
                    })
                    .setNeutralButton("Cancel", null)
                    .show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handledeleteItem(DocumentSnapshot snapshot) {

        DocumentReference documentReference = snapshot.getReference();
        MechanicModel note = snapshot.toObject(MechanicModel.class);

        documentReference.delete()
                .addOnSuccessListener(aVoid -> Log.d(TAG, "onSuccess: Item Deleted"))
                .addOnFailureListener(e -> Log.d(TAG, "onFailure: "+e.getLocalizedMessage()));

        Snackbar.make(recyclerView, "Item deleted", Snackbar.LENGTH_LONG)
                .setAction("Undo", v -> documentReference.set(note))
                .show();
    }

    @Override
    public void handleMechanic(MechanicModel model) {
        new AlertDialog.Builder(this)
                .setTitle("Mechanic Details")
                .setNegativeButton("Call to Mechanic", (dialogInterface, i) -> {
                    Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + model.getMechanicPhoneString()));
                    startActivity(intent);
                })
                .setNeutralButton("Cancel", null)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.logout) {
            firebaseAuth.signOut();
            startActivity(new Intent(getApplicationContext(), UserLoginRegistrationActivity.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}