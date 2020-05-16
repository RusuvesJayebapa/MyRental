package com.example.myrental;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;

public class tenant extends AppCompatActivity {

    RecyclerView recyclerView;
    HashMap<String,Object> hashMap = new HashMap<>();
    ArrayList<HashMap> list = new ArrayList<>();
    ArrayList<Bitmap> ImageList = new ArrayList<>();
    DatabaseReference reff, requestOwner;
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
        reff = FirebaseDatabase.getInstance().getReference().child("AddResidenceOwner");
        requestOwner = FirebaseDatabase.getInstance().getReference().child("RequestOwner");


        sessionManager = new SessionManager(getApplicationContext());


        editText = findViewById(R.id.ed);

        requestOwner.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    reff.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists())
                        {
                            list.clear();
                            hashMap.clear();
                            for (DataSnapshot ds : dataSnapshot.getChildren())
                            {
                                hashMap = (HashMap<String, Object>) ds.getValue();
                                if (!hashMap.get("Email").equals(sessionManager.getEmail()) && !String.valueOf(hashMap.get("Phone")).equals(String.valueOf(sessionManager.getPhone())) || !hashMap.get("Status").equals("YES"))
                                {
                                    count++;
                                    list.add(hashMap);
                                }
                            }
                            MyNewAdapter myAdapter = new MyNewAdapter(list,tenant.this);
                            recyclerView.setAdapter(myAdapter);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public class ImageAdapter extends PagerAdapter {
        private Context mcontext;

        ImageAdapter(Context context) {
            mcontext = context;
        }

        @Override
        public int getCount() {
            return ImageList.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, final int position) {
            final ImageView imageView = new ImageView(mcontext);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            imageView.setImageBitmap(ImageList.get(position));
            container.addView(imageView, 0);

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final ImageView imageView = new ImageView(mcontext);
                    imageView.setMinimumWidth(1080);
                    imageView.setMaxWidth(1080);
                    imageView.setMaxHeight(1500);
                    imageView.setMinimumHeight(1500);
                    imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                    imageView.setImageBitmap(ImageList.get(position));
                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(mcontext);
                    builder.setCancelable(true);
                    builder.setView(imageView);
                    android.app.AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                    alertDialog.getWindow().setLayout(1080,1500);
                }
            });

            return imageView;

        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((ImageView) object);
        }
    }


    class MyNewAdapter extends RecyclerView.Adapter<MyNewAdapter.MyViewHolder>
    {
        ArrayList<HashMap> arrayList;
        Context context;

        public MyNewAdapter(ArrayList<HashMap> arrayList, Context context) {
            this.arrayList = arrayList;
            this.context = context;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(context).inflate(R.layout.tenant_cardview,parent,false);
            return new MyViewHolder(v);
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
            holder.landlord.setText(""+arrayList.get(position).get("Firstname")+" "+arrayList.get(position).get("Middlename")+" "+arrayList.get(position).get("Lastname"));
            holder.landlord_address.setText(arrayList.get(position).get("Landlord_Buildingname")+", "+arrayList.get(position).get("Landlord_Road")+", "+arrayList.get(position).get("Landlord_Location")
                    +", "+arrayList.get(position).get("Landlord_City")+"-"+arrayList.get(position).get("Landlord_Pincode")+", "+arrayList.get(position).get("Landlord_State"));
            holder.landlord_age.setText(""+ Period.between(LocalDate.parse(""+arrayList.get(position).get("DOB"), DateTimeFormatter.ofPattern("dd/MM/yyyy")), LocalDate.now()).getYears());
            holder.property_address.setText(arrayList.get(position).get("Buildingname")+", "+arrayList.get(position).get("Road")+", "+arrayList.get(position).get("Location")
                    +", "+arrayList.get(position).get("City")+"-"+arrayList.get(position).get("Pincode")+", "+arrayList.get(position).get("State"));
            holder.landlord_mobile.setText(String.valueOf(arrayList.get(position).get("Phone")));
            holder.property_type.setText(""+arrayList.get(position).get("Category")+", "+arrayList.get(position).get("Property_Typeno")+" "+arrayList.get(position).get("Property_Type"));
            holder.property_area.setText(""+arrayList.get(position).get("Area"));
            holder.property_duration.setText(""+arrayList.get(position).get("Duration"));
            holder.property_deposit.setText(""+arrayList.get(position).get("Deposit"));
            holder.property_rent.setText(""+arrayList.get(position).get("Rent"));

            TextView textView = new TextView(tenant.this);
            textView.setTextColor(Color.BLACK);
            textView.setTextSize(16);
            for (int i = 0; i < ((HashMap<String,Integer>) arrayList.get(position).get("Appliances")).keySet().size(); i++)
                textView.append(""+((HashMap<String,Integer>) arrayList.get(position).get("Appliances")).keySet().toArray()[i]+" = "+((HashMap<String,Integer>) arrayList.get(position).get("Appliances")).values().toArray()[i]+"\n");
            holder.property_appliances.addView(textView);

            holder.property_appliances.removeAllViews();
            for (int i = 0; i < ((ArrayList<String>) arrayList.get(position).get("Clause")).size(); i++)
                textView.append("\n"+((ArrayList<String>) arrayList.get(position).get("Clause")).get(i));
            holder.property_clauses.addView(textView);

            int currentImageSelect = -1;
            ImageList.clear();
            while (currentImageSelect < 10)
            {
                try
                {
                    currentImageSelect++;
                    final File file = File.createTempFile("image","jpeg");
                    final StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl("gs://my-rental-4ff8d.appspot.com/Residence_Pics/").child(String.valueOf(arrayList.get(position).get("Id"))).child(String.valueOf(currentImageSelect));
                    storageReference.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>()
                    {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot)
                        {
                            final Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                            addimage(bitmap);
                            tenant.ImageAdapter adapter = new tenant.ImageAdapter(context);
                            holder.viewPager.setAdapter(adapter);
                        }
                    });
                }

                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
            
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("RequestOwner");

            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot ds : dataSnapshot.getChildren())
                    {
                        HashMap<String,Object> requestOwner = (HashMap<String, Object>) ds.getValue();
                        if (requestOwner.get("Id").equals(arrayList.get(position).get("Id")) && requestOwner.get("request_Accepted").equals("YES"))
                        {
                            holder.form.setVisibility(View.VISIBLE);
                        }
                        if (requestOwner.get("Id").equals(arrayList.get(position).get("Id")))
                        {
                            holder.request.setText("Sent Request");
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


            holder.call.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+arrayList.get(position).get("Phone")));
                    v.getContext().startActivity(intent);
                }
            });

            holder.request.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (holder.request.getText().equals("Request")) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setMessage("Do you want to request the Owner ?");
                        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                request(arrayList.get(position).get("Id"), "1");
                            }
                        });

                        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                        builder.show();
                    }
                    else if (holder.request.getText().equals("Sent Request")) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setMessage("Do you want to cancel request?");
                        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                request(arrayList.get(position).get("Id"), "2");
                            }
                        });

                        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                        builder.show();
                    }


                }

            });

            holder.form.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(context, "YESSSSS", Toast.LENGTH_LONG).show();
                }
            });

        }

        private void addimage(Bitmap bitmap) {
            ImageList.add(bitmap);
        }

        private void request(final Object id, String key) {
            final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("AddResidenceOwner");
            final DatabaseReference databaseReference1 = FirebaseDatabase.getInstance().getReference().child("RequestOwner");
            final SessionManager sessionManager = new SessionManager(context);

            if (key.equals("1"))
            {
                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        databaseReference.removeEventListener(this);
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            HashMap<String, Object> requestOwner = (HashMap<String, Object>) ds.getValue();
                            if (id.equals(requestOwner.get("Id"))) {
                                requestOwner.clear();
                                requestOwner.put("Id", id);
                                requestOwner.put("request_Accepted", "NO");
                                requestOwner.put("requestor", sessionManager.getFirstname() + " " + sessionManager.getMiddlename() + " " + sessionManager.getLastname());
                                requestOwner.put("requestor_Address", sessionManager.getBuildingname() + ", " + sessionManager.getRoad() + ", " + sessionManager.getLocation()
                                        + ", " + sessionManager.getCity() + "-" + sessionManager.getPincode() + ", " + sessionManager.getState());
                                requestOwner.put("Age", sessionManager.getDOB());
                                requestOwner.put("requestor_Phone", sessionManager.getPhone());
                                requestOwner.put("requestor_Email", sessionManager.getEmail());

                                databaseReference1.push().setValue(requestOwner);

                                break;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            if (key.equals("2"))
            {
                databaseReference1.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        databaseReference1.removeEventListener(this);
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            HashMap<String, Object> remove = (HashMap<String, Object>) ds.getValue();
                            if (id.equals(remove.get("Id")) && remove.get("requestor_Email").equals(sessionManager.getEmail()) && remove.get("requestor_Phone").equals(sessionManager.getPhone())) {
                                databaseReference1.child(ds.getKey()).removeValue();
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
            ImageView imageView;
            ViewPager viewPager;
            LinearLayout property_clauses, property_appliances;
            EditText landlord, landlord_address, landlord_mobile, landlord_age, landlord_occupation, property_address, property_type, property_rent, property_area, property_duration, property_deposit;
            Button call, request, form;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);

                imageView = itemView.findViewById(R.id.imageView);
                viewPager = itemView.findViewById(R.id.viewpager);
                landlord = itemView.findViewById(R.id.landlord);
                landlord_address = itemView.findViewById(R.id.landlord_address);
                landlord_mobile = itemView.findViewById(R.id.phone);
                landlord_age = itemView.findViewById(R.id.age);
                landlord_occupation = itemView.findViewById(R.id.occupation);
                property_address = itemView.findViewById(R.id.property_address);
                property_type = itemView.findViewById(R.id.type);
                property_area = itemView.findViewById(R.id.area);
                property_duration = itemView.findViewById(R.id.duration);
                property_deposit = itemView.findViewById(R.id.deposit);
                property_appliances = itemView.findViewById(R.id.Applicance_Furniture);
                property_clauses = itemView.findViewById(R.id.Clauses);
                property_rent = itemView.findViewById(R.id.rent);
                call = itemView.findViewById(R.id.call);
                request = itemView.findViewById(R.id.request);
                form = itemView.findViewById(R.id.form);
            }
        }

    }
}

