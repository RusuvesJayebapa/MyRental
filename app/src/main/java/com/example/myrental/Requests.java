package com.example.myrental;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class Requests extends AppCompatActivity
{
    MyRequests itemAdapter;
    RecyclerView rvItem;
    ArrayList<HashMap> requestOwner = new ArrayList<>();
    ArrayList<HashMap> registrationDatabase = new ArrayList<>();
    DatabaseReference reff, databaseReference;
    ProgressDialog progressDialog;
    SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requests);

        rvItem = findViewById(R.id.Recycler);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        sessionManager = new SessionManager(getApplicationContext());

        getList();
    }

    private void getList()
    {
        if (getIntent().getStringExtra("key").equals("1"))
            reff = FirebaseDatabase.getInstance().getReference().child("RequestOwner");
        else if (getIntent().getStringExtra("key").equals("2"))
            reff = FirebaseDatabase.getInstance().getReference().child("RequestVehicleOwner");

        reff.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                rvItem.setAdapter(null);
                registrationDatabase.clear();
                requestOwner.clear();
                if(dataSnapshot.exists())
                {
                    for (DataSnapshot ds : dataSnapshot.getChildren())
                    {
                        final HashMap<String,Object> request = (HashMap<String, Object>) ds.getValue();
                        if (getIntent().getStringExtra("key").equals("1"))
                        {
                            if (request.get("Property_Id").equals(getIntent().getStringExtra("id"))) {
                                requestOwner.add(request);
                            }
                        }
                        else if (getIntent().getStringExtra("key").equals("2"))
                        {
                            if (request.get("Vehicle_Id").equals(getIntent().getStringExtra("id"))) {
                                requestOwner.add(request);
                            }
                        }
                    }
//                    if (requestOwner.size() == 0)
                    getRequestor();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getRequestor()
    {
        databaseReference = FirebaseDatabase.getInstance().getReference().child("RegistrationDatabase");

        databaseReference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                for (int i = 0; i < requestOwner.size(); i++)
                {
                    for (DataSnapshot ds : dataSnapshot.getChildren())
                    {
                        HashMap<String, Object> requestor_details = (HashMap<String, Object>) ds.getValue();
                        if(requestor_details.get("UID").equals(requestOwner.get(i).get("Requestor_Id")))
                        {
                            registrationDatabase.add(requestor_details);
                        }
                    }
                }
//                if (registrationDatabase.size() == 0)
                display();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    private void display()
    {
        if(requestOwner.size() > 0)
        {
            rvItem.setAdapter(null);

            LinearLayoutManager layoutManager = new LinearLayoutManager(Requests.this);
            itemAdapter = new MyRequests(registrationDatabase, Requests.this);
            rvItem.setAdapter(itemAdapter);
            rvItem.setLayoutManager(layoutManager);
        }
        else
        {
            progressDialog.cancel();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.search_bar , menu);

        MenuItem item = menu.findItem(R.id.search_bar);

        SearchView searchView = (SearchView)item.getActionView();
        searchView.setMaxWidth(Integer.MAX_VALUE);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                itemAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                itemAdapter.getFilter().filter(newText);
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed()
    {
        Intent intent;
        if (getIntent().getStringExtra("key").equals("1"))
            intent = new Intent(Requests.this, com.example.myrental.MyResidence.class);
        else
            intent = new Intent(Requests.this, com.example.myrental.MyVehicle.class);
        startActivity(intent);
    }

    class MyRequests extends RecyclerView.Adapter<MyRequests.MyViewHolder> implements Filterable
    {
        ArrayList<HashMap> requestorList;
        ArrayList<HashMap> requestorFilterList;
        Context context;

        public MyRequests(ArrayList<HashMap> requestorList, Context context)
        {
            this.requestorList = requestorList;
            this.requestorFilterList = requestorList;
            this.context = context;
            progressDialog.cancel();
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
        {
            View v = LayoutInflater.from(context).inflate(R.layout.requests_cardview,parent,false);
            return new MyViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position)
        {
            holder.name.setText(requestorFilterList.get(position).get("Firstname")+" "+requestorFilterList.get(position).get("Middlename")+" "+requestorFilterList.get(position).get("Lastname"));
            holder.address.setText(requestorFilterList.get(position).get("Buildingname")+", "+requestorFilterList.get(position).get("Road")+", "+requestorFilterList.get(position).get("Location")+", "+requestorFilterList.get(position).get("City")+"-"+requestorFilterList.get(position).get("Pincode")+", "+requestorFilterList.get(position).get("State"));
            holder.phone.setText(String.valueOf(requestorFilterList.get(position).get("Phone")));

            StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl("gs://my-rental-4ff8d.appspot.com/Profile_Pics/").child(String.valueOf(requestorFilterList.get(position).get("UID")));
            storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
            {
                @Override
                public void onSuccess(Uri uri)
                {
                    Picasso.get().load(uri).placeholder(R.drawable.loading).into(holder.imageView);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) { System.out.println("Image "+e);}
            });

            holder.call.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+requestorFilterList.get(position).get("requestor_Phone")));
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
            final DatabaseReference databaseReferences = FirebaseDatabase.getInstance().getReference().child("RequestOwner");
            databaseReferences.addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                    databaseReference.removeEventListener(this);
                    for (DataSnapshot ds : dataSnapshot.getChildren())
                    {
                        HashMap<String,Object> hashMap = (HashMap<String, Object>) ds.getValue();
                        if (pid.equals(hashMap.get("Property_Id")) && rid.equals(hashMap.get("Requestor_Id")) && key.equals("1"))
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
        public int getItemCount()
        {
            if (requestorFilterList == null)
                return 0;
            return requestorFilterList.size();
        }

        @Override
        public Filter getFilter()
        {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    String character = constraint.toString();
                    if (character.isEmpty())
                    {
                        requestorFilterList = registrationDatabase;
                    }
                    else
                    {
                        ArrayList<HashMap> filterList = new ArrayList<>();
                        for (int i = 0; i < registrationDatabase.size(); i++)
                        {
                            System.out.println(character.toLowerCase() + "      " + registrationDatabase.get(i).get("Firstname").toString().toLowerCase());
                            if (registrationDatabase.get(i).get("Firstname").toString().toLowerCase().contains(character.toLowerCase()) ||
                                registrationDatabase.get(i).get("Middlename").toString().toLowerCase().contains(character.toLowerCase()) ||
                                registrationDatabase.get(i).get("Lastname").toString().toLowerCase().contains(character.toLowerCase()) ||
                                registrationDatabase.get(i).get("Phone").toString().toLowerCase().contains(character.toLowerCase()) ||
                                registrationDatabase.get(i).get("Buildingname").toString().toLowerCase().contains(character.toLowerCase()) ||
                                registrationDatabase.get(i).get("Road").toString().toLowerCase().contains(character.toLowerCase()) ||
                                registrationDatabase.get(i).get("Location").toString().toLowerCase().contains(character.toLowerCase()) ||
                                registrationDatabase.get(i).get("City").toString().toLowerCase().contains(character.toLowerCase()) ||
                                registrationDatabase.get(i).get("State").toString().toLowerCase().contains(character.toLowerCase()) ||
                                registrationDatabase.get(i).get("Pincode").toString().toLowerCase().contains(character.toLowerCase()) ||
                                registrationDatabase.get(i).get("Area").toString().toLowerCase().contains(character.toLowerCase()))
                            {
                                filterList.add(registrationDatabase.get(i));
                            }
                        }
                        requestorFilterList = filterList ;
                    }
                    FilterResults filterResults = new FilterResults();
                    filterResults.values = requestorFilterList;
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    requestorFilterList = (ArrayList<HashMap>) results.values ;
                    notifyDataSetChanged();
                }
            };
        }

        class MyViewHolder extends RecyclerView.ViewHolder
        {
            ImageView imageView, call;
            Button accept, reject;
            TextView name, address, phone;

            public MyViewHolder(@NonNull View itemView)
            {
                super(itemView);

                imageView = itemView.findViewById(R.id.imageView);
                name = itemView.findViewById(R.id.name);
                address = itemView.findViewById(R.id.landlord_address);
                phone = itemView.findViewById(R.id.phone);
                call = itemView.findViewById(R.id.call);
                accept = itemView.findViewById(R.id.accept);
                reject = itemView.findViewById(R.id.cancel);
            }
        }
    }
}
