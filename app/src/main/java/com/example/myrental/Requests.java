package com.example.myrental;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class Requests extends AppCompatActivity {

    RecyclerView recyclerView;
    ArrayList<HashMap> list = new ArrayList<>();
    DatabaseReference reff;
    int count = 0;
    SessionManager sessionManager;
    EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tenant);

        recyclerView = findViewById(R.id.Recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        reff = FirebaseDatabase.getInstance().getReference().child("RequestOwner");

        sessionManager = new SessionManager(getApplicationContext());


        editText = findViewById(R.id.ed);

        reff.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                list.clear();
                if(dataSnapshot.exists())
                {
                    for (DataSnapshot ds : dataSnapshot.getChildren())
                    {
                        HashMap<String,Object> requestOwner = (HashMap<String, Object>) ds.getValue();
                        if (requestOwner.get("Id").equals(getIntent().getStringExtra("id")))
                        {
                            count++;
                            list.add(requestOwner);
                        }
                    }
                    MyRequests myAdapter = new MyRequests(list,Requests.this);
                    recyclerView.setAdapter(myAdapter);
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
        public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
            int status = 0;
            holder.name.setText(""+arrayList.get(position).get("requestor"));
            holder.address.setText(""+arrayList.get(position).get("requestor_Address"));
            holder.phone.setText(String.valueOf(arrayList.get(position).get("requestor_Phone")));

            if (arrayList.get(position).get("request_Accepted").equals("YES"))
            {
                holder.accept.setVisibility(View.GONE);
                holder.reject.setVisibility(View.VISIBLE);
            }
            for (int i = 0; i < arrayList.size(); i++)
            {
                if (arrayList.get(i).get("request_Accepted").equals("YES"))
                {
                    holder.accept.setVisibility(View.GONE);
                    break;
                }
            }

            holder.detail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    holder.imageView.setVisibility(View.VISIBLE);
                    holder.Address.setVisibility(View.VISIBLE);
                    holder.Phone.setVisibility(View.VISIBLE);
                    holder.detail.setVisibility(View.GONE);

                    ViewGroup.LayoutParams params = holder.Buttons.getLayoutParams();
                    params.width = 740;
                    holder.Buttons.setLayoutParams(params);
                }
            });

            holder.call.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+arrayList.get(position).get("requestor_Phone")));
                    v.getContext().startActivity(intent);
                }
            });

            holder.accept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage("Do you want to accept the request ?");
                    builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            update(arrayList.get(position).get("Id"), position, "1");

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

            holder.reject.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage("Do you want to reject the request ?");
                    builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            update(arrayList.get(position).get("Id"), position, "2");
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

        private void update(final Object id, final int position, String key) {

            final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("RequestOwner");
            final SessionManager sessionManager = new SessionManager(context);
            if (key.equals("1"))
            {
                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        databaseReference.removeEventListener(this);
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            HashMap<String,Object> hashMap = (HashMap<String, Object>) ds.getValue();
                            if (id.equals(hashMap.get("Id")) && arrayList.get(position).get("requestor_Email").equals(hashMap.get("requestor_Email")) && arrayList.get(position).get("requestor_Phone").equals(hashMap.get("requestor_Phone")))
                            {

                                hashMap.put("request_Accepted", "YES");

                                databaseReference.child(ds.getKey()).setValue(hashMap);

                                break;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
            else if (key.equals("2"))
            {
                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        databaseReference.removeEventListener(this);
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            HashMap<String,Object> hashMap = (HashMap<String, Object>) ds.getValue();
                            if (id.equals(hashMap.get("Id")) && arrayList.get(position).get("requestor_Email").equals(hashMap.get("requestor_Email")) && arrayList.get(position).get("requestor_Phone").equals(hashMap.get("requestor_Phone")))
                            {
                                hashMap.put("request_Accepted","NO");

                                databaseReference.child(ds.getKey()).setValue(hashMap);

                                break;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
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
                detail = itemView.findViewById(R.id.details);
                call = itemView.findViewById(R.id.call);
                accept = itemView.findViewById(R.id.accept);
                reject = itemView.findViewById(R.id.cancel);

                reject.setVisibility(View.GONE);
            }
        }

    }
}
