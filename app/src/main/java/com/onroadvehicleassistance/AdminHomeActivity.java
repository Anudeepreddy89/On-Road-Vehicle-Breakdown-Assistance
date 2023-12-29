package com.onroadvehicleassistance;

import static com.onroadvehicleassistance.UserLoginRegistrationActivity.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.onroadvehicleassistance.adapter.MechanicAdapter;
import com.onroadvehicleassistance.model.MechanicModel;
import com.onroadvehicleassistance.utils.Constants;
import com.onroadvehicleassistance.utils.toast;

public class AdminHomeActivity extends AppCompatActivity implements FirebaseAuth.AuthStateListener, MechanicAdapter.MechanicListener {
    RecyclerView recyclerView;
    MechanicAdapter mechanicAdapter;
    private FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);
        getSupportActionBar().setTitle("Admin Home");
        firebaseAuth= FirebaseAuth.getInstance();

        findViewById(R.id.floatingActionButton).setOnClickListener(view -> {
           startActivity(new Intent(getApplicationContext(),MechanicRegistrationActivity.class).
                   putExtra("from","admin"));
        });

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(false);
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

        mechanicAdapter = new MechanicAdapter(options,this,AdminHomeActivity.this);
        recyclerView.setAdapter(mechanicAdapter);

        mechanicAdapter.startListening();
        //toast.meActivity(AdminHomeActivity.this,"Swipe left on item to delete!");


        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    final ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            if (direction == ItemTouchHelper.LEFT) {
                Toast.makeText(AdminHomeActivity.this, "Deleting", Toast.LENGTH_SHORT).show();

                MechanicAdapter.MechanicViewHolder noteViewHolder = (MechanicAdapter.MechanicViewHolder) viewHolder;
                noteViewHolder.deletItem();

            }
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX,
                                float dY, int actionState, boolean isCurrentlyActive) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.logout) {
            firebaseAuth.signOut();
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void handleEditNote(DocumentSnapshot snapshot) {
        runOnUiThread(() -> {
            final CharSequence[] optionsMenu = {"Delete Mechanic","Cancel" }; // create a menuOption Array
            // create a dialog for showing the optionsMenu
            AlertDialog.Builder builder = new AlertDialog.Builder(AdminHomeActivity.this);
            // set the items in builder
            builder.setItems(optionsMenu, (dialogInterface, i) -> {
                if (optionsMenu[i].equals("Delete Mechanic")) {
                    DocumentReference documentReference = snapshot.getReference();
                    MechanicModel note = snapshot.toObject(MechanicModel.class);

                    documentReference.delete()
                            .addOnSuccessListener(aVoid -> Log.d(TAG, "onSuccess: Item Deleted"))
                            .addOnFailureListener(e -> Log.d(TAG, "onFailure: "+e.getLocalizedMessage()));

                    Snackbar.make(recyclerView, "Item deleted", Snackbar.LENGTH_LONG)
                            .setAction("Undo", v -> documentReference.set(note))
                            .show();
                }else if(optionsMenu[i].equals("Cancel")){
                    dialogInterface.dismiss();
                }
            });
            builder.show();
        });


    }

    @Override
    public void handledeleteItem(DocumentSnapshot snapshot) {
        DocumentReference documentReference = snapshot.getReference();
        MechanicModel model = snapshot.toObject(MechanicModel.class);

        documentReference.delete()
                .addOnSuccessListener(aVoid -> Log.d(TAG, "onSuccess: Item Deleted"))
                .addOnFailureListener(e -> Log.d(TAG, "onFailure: "+e.getLocalizedMessage()));

        Snackbar.make(recyclerView, "Item deleted", Snackbar.LENGTH_LONG)
                .setAction("Undo", v -> documentReference.set(model))
                .show();
    }

    @Override
    public void handleMechanic(MechanicModel model) {

    }
}