package com.onroadvehicleassistance.adapter;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.onroadvehicleassistance.R;
import com.onroadvehicleassistance.model.MechanicModel;

public class MechanicAdapter extends FirestoreRecyclerAdapter<MechanicModel,MechanicAdapter.MechanicViewHolder> {

    MechanicListener mechanicListener;
    private static final String TAG = "MechanicAdapter";
    Context context;

    public MechanicAdapter(@NonNull FirestoreRecyclerOptions<MechanicModel> options, MechanicListener mechanicListener, Context context) {
        super(options);
        this.mechanicListener = mechanicListener;
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull MechanicViewHolder holder, int position, @NonNull MechanicModel model) {
        holder.mechanicName.setText(model.getMechanicName());
        holder.mechanicDescription.setText(model.getMechanicDescription());
        holder.mechanicAddress.setText(model.getMechanicAddress());
        holder.mechanicPhone.setText(model.getMechanicPhoneString());

        DrawableCrossFadeFactory factory =
                new DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build();

        RequestOptions options =
                new RequestOptions()
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                        .placeholder(R.mipmap.ic_launcher_round)
                        .error(R.drawable.nopictures);
        Glide.with(context).load(model.getMechanicImage()).transition(withCrossFade(factory))
                .apply(options).into(holder.mechanicImage);
    }


    @NonNull
    @Override
    public MechanicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.mechanic_item, parent, false);
        return new MechanicViewHolder(view);
    }

    public class MechanicViewHolder extends RecyclerView.ViewHolder {

        TextView mechanicName, mechanicDescription, mechanicAddress, mechanicPhone;
        ImageView mechanicImage;

        public MechanicViewHolder(@NonNull View itemView) {
            super(itemView);

            mechanicName = itemView.findViewById(R.id.mechanicName);
            mechanicDescription = itemView.findViewById(R.id.mechanicDescription);
            mechanicAddress = itemView.findViewById(R.id.mechanicAddress);
            mechanicPhone = itemView.findViewById(R.id.mechanicPhone);
            mechanicImage = itemView.findViewById(R.id.mechanicImage);

            itemView.setOnClickListener(v -> {
                DocumentSnapshot snapshot = getSnapshots().getSnapshot(getAdapterPosition());
                mechanicListener.handleEditNote(snapshot);
            });
        }

        public void deletItem() {
            mechanicListener.handledeleteItem(getSnapshots().getSnapshot(getAdapterPosition()));
        }
    }

    public interface MechanicListener {
        void handleEditNote(DocumentSnapshot snapshot);
        void handledeleteItem(DocumentSnapshot snapshot);
        void handleMechanic(MechanicModel model);
    }
}
