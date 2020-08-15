package com.example.myrental;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class VehicleRequests extends AppCompatActivity {

    RecyclerView recyclerView;
    ArrayList<HashMap> requestOwner = new ArrayList<>();
    ArrayList<HashMap> registrationDatabase = new ArrayList<>();
    DatabaseReference reff, databaseReference;
    SessionManager sessionManager;
    EditText editText;
    Uri uri;
    ImageView imageView;
    long count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requests);

        recyclerView = findViewById(R.id.Recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        reff = FirebaseDatabase.getInstance().getReference().child("RequestVehicleOwner");
        databaseReference = FirebaseDatabase.getInstance().getReference().child("RegistrationDatabase");

        sessionManager = new SessionManager(getApplicationContext());


        editText = findViewById(R.id.ed);
        imageView = new ImageView(this);

        reff.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                registrationDatabase.clear();
                requestOwner.clear();
                if(dataSnapshot.exists())
                {
                    count = dataSnapshot.getChildrenCount();
                    for (DataSnapshot ds : dataSnapshot.getChildren())
                    {
                        count--;
                        final HashMap<String,Object> request = (HashMap<String, Object>) ds.getValue();
                        if (request.get("Vehicle_Id").equals(getIntent().getStringExtra("id")))
                        {
                            requestOwner.add(request);
                            databaseReference.addValueEventListener(new ValueEventListener()
                            {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                {
                                    for (DataSnapshot ds : dataSnapshot.getChildren())
                                    {
                                        HashMap<String, Object> requestor_details = (HashMap<String, Object>) ds.getValue();
                                        if(requestor_details.get("UID").equals(request.get("Requestor_Id")))
                                        {
                                            registrationDatabase.add(requestor_details);
                                        }
                                        if(count == 0)
                                        {
                                            MyRequests myAdapter = new MyRequests(registrationDatabase, VehicleRequests.this);
                                            recyclerView.setAdapter(myAdapter);
                                        }
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) { }
                            });
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    class MyRequests extends RecyclerView.Adapter<MyRequests.MyViewHolder>
    {
        ArrayList<HashMap> arrayList;
        Context context;

        public MyRequests(ArrayList<HashMap> arrayList, Context context) {
            System.out.println(registrationDatabase.size() + " : SIZE");
            this.arrayList = arrayList;
            this.context = context;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(context).inflate(R.layout.requests_cardview,parent,false);
            return new MyViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position)
        {
            holder.name.setText(""+arrayList.get(position).get("Firstname")+" "+arrayList.get(position).get("Middlename")+" "+arrayList.get(position).get("Lastname"));
            holder.address.setText(arrayList.get(position).get("Buildingname")+", "+arrayList.get(position).get("Road")+", "+arrayList.get(position).get("Location")+", "+arrayList.get(position).get("City")+"-"+arrayList.get(position).get("Pincode")+", "+arrayList.get(position).get("State"));
            holder.phone.setText(String.valueOf(arrayList.get(position).get("Phone")));

            int flag = 0, pos = -1;
            //holder.accept.setVisibility(View.VISIBLE);
            for (int i = 0; i < requestOwner.size(); i++)
            {
                if (requestOwner.get(i).get("Request_Status").equals("YES"))
                {
                    flag = 1;
                    pos = i;
                    holder.accept.setVisibility(View.GONE);
                    holder.reject.setVisibility(View.GONE);
                    break;
                }
            }
            if (flag == 1 && position == pos)
            {
                holder.reject.setVisibility(View.VISIBLE);
            }

            try
            {
                final File file = File.createTempFile("image", "jpeg");
                final StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl("gs://my-rental-4ff8d.appspot.com/Profile_Pics/").child(String.valueOf(registrationDatabase.get(position).get("UID")));
                storageReference.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        final Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

                        holder.imageView.setImageBitmap(bitmap);
                    }
                });
            }
            catch (Exception e)
            {

            }

            holder.detail.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    holder.imageView.setVisibility(View.VISIBLE);
                    holder.Address.setVisibility(View.VISIBLE);
                    holder.Phone.setVisibility(View.VISIBLE);
                    holder.detail.setVisibility(View.GONE);

                    ViewGroup.LayoutParams params = holder.Buttons.getLayoutParams();
                    params.width = 740;
                    holder.Buttons.setLayoutParams(params);
                }
            });

            holder.call.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+arrayList.get(position).get("requestor_Phone")));
                    v.getContext().startActivity(intent);
                }
            });

            holder.accept.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    update(requestOwner.get(position).get("Property_Id").toString(), requestOwner.get(position).get("Requestor_Id").toString() , "1");
                }
            });

            holder.reject.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage("Do you want to reject the request ?");
                    builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            update(requestOwner.get(position).get("Property_Id").toString(), requestOwner.get(position).get("Requestor_Id").toString(), "2");
                        }
                    });

                    builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    builder.show();
                }
            });

        }

        private void update(final String pid, final String rid, final String key)
        {
            final DatabaseReference databaseReferences = FirebaseDatabase.getInstance().getReference().child("RequestVehicleOwner");
            databaseReferences.addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                    databaseReference.removeEventListener(this);
                    for (DataSnapshot ds : dataSnapshot.getChildren())
                    {
                        HashMap<String,Object> hashMap = (HashMap<String, Object>) ds.getValue();
                        if (pid.equals(hashMap.get("Vehicle_Id")) && rid.equals(hashMap.get("Requestor_Id")) && key.equals("1"))
                        {
                            hashMap.put("Request_Status", "YES");
                            databaseReferences.child(ds.getKey()).setValue(hashMap);
                            break;
                        }
                        else if (pid.equals(hashMap.get("Property_Id")) && rid.equals(hashMap.get("Requestor_Id")) && key.equals("2"))
                        {
                            hashMap.put("Request_Status", "NO");
                            databaseReferences.child(ds.getKey()).setValue(hashMap);
                            break;
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) { }

            });
        }

        @Override
        public int getItemCount() {
            return arrayList.size();
        }


        class MyViewHolder extends RecyclerView.ViewHolder
        {
            ImageView imageView, call, accept, reject;
            TextView Name, name, address, phone, detail;
            LinearLayout Address, Phone, Buttons;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);

                imageView = itemView.findViewById(R.id.imageView);
                Name = itemView.findViewById(R.id.Name);
                Address = itemView.findViewById(R.id.Landlord_Address);
                Phone = itemView.findViewById(R.id.Phone);
                Buttons = itemView.findViewById(R.id.buttons);
                name = itemView.findViewById(R.id.name);
                address = itemView.findViewById(R.id.landlord_address);
                phone = itemView.findViewById(R.id.phone);
                call = itemView.findViewById(R.id.call);
                accept = itemView.findViewById(R.id.accept);
                reject = itemView.findViewById(R.id.cancel);

                reject.setVisibility(View.GONE);
            }
        }


    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK)
        {
            if (data.getData() != null)
            {

                imageView.setImageURI(data.getData());
                uri = data.getData();
                AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
                builder1.setMessage("Your Signature");
                imageView.setImageURI(uri);
                imageView.setMaxHeight(900);
                imageView.setAdjustViewBounds(true);
                builder1.setView(imageView);
                builder1.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("Signature").child(sessionManager.getEmail());
                        storageRef.putFile(uri)
                                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        // Get a URL to the uploaded content
                                        // Uri downloadUrl = taskSnapshot.getDownloadUrl();
                                    }
                                });
                        Toast.makeText(VehicleRequests.this, "Uploaded Successfully", Toast.LENGTH_SHORT).show();

                    }
                });
                AlertDialog alertDialog = builder1.create();
                alertDialog.show();
                alertDialog.getWindow().setLayout(1080,1500);
            }
        }
    }
}
