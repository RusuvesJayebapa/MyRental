package com.example.myrental;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import java.util.ArrayList;
import java.util.HashMap;

public class MyResidence extends AppCompatActivity {

    int count = 0, currentImageSelect = -1;
    RecyclerView recyclerView;
    ArrayList<HashMap> list = new ArrayList<>();
    ArrayList<Bitmap> ImageList = new ArrayList<>();
    HashMap<String,Object> hashMap = new HashMap<>();
    DatabaseReference reff;
    SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_residence);

        recyclerView = findViewById(R.id.Recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        reff = FirebaseDatabase.getInstance().getReference().child("AddResidenceOwner");

        sessionManager = new SessionManager(getApplicationContext());


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
                        if ((hashMap.get("Email").equals(sessionManager.getEmail()) && String.valueOf(hashMap.get("Phone")).equals(String.valueOf(sessionManager.getPhone())) && hashMap.get("Status").equals("NO")) || hashMap.get("AddedBy").equals(sessionManager.getEmail()))
                            {
                                count++;
                            list.add(hashMap);
                        }
                    }
                    MyAdapter myAdapter = new MyAdapter(list,MyResidence.this);
                    recyclerView.setAdapter(myAdapter);
                }
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

    class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder>
    {
        ArrayList<HashMap> arrayList;
        Context context;

        public MyAdapter(ArrayList<HashMap> arrayList, Context context) {
            this.arrayList = arrayList;
            this.context = context;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(context).inflate(R.layout.my_residence_cardview,parent,false);
            return new MyViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
            holder.landlord.setText(""+arrayList.get(position).get("Firstname")+" "+arrayList.get(position).get("Middlename")+" "+arrayList.get(position).get("Lastname"));
            holder.location.setText(arrayList.get(position).get("Buildingname")+", "+arrayList.get(position).get("Road")+", "+arrayList.get(position).get("Location")+", "+arrayList.get(position).get("City")+"-"+arrayList.get(position).get("Pincode")+", "+arrayList.get(position).get("State"));
            holder.mobile.setText(String.valueOf(arrayList.get(position).get("Phone")));
            holder.property.setText(""+arrayList.get(position).get("Category")+", "+arrayList.get(position).get("Property_Typeno")+" "+arrayList.get(position).get("Property_Type"));
            holder.area.setText(""+arrayList.get(position).get("Area"));
            holder.duration.setText(""+arrayList.get(position).get("Duration"));
            holder.deposit.setText(""+arrayList.get(position).get("Deposit"));
            holder.rent.setText(""+arrayList.get(position).get("Rent"));

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
                            MyResidence.ImageAdapter adapter = new MyResidence.ImageAdapter(context);
                            holder.viewPager.setAdapter(adapter);
                        }
                    });
                }

                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }

            holder.edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), com.example.myrental.landlord.class);
                    intent.putExtra("id",""+arrayList.get(position).get("Id"));
                    v.getContext().startActivity(intent);
                }
            });

            holder.rented.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage("Do you want to mark this residence as rented ?");
                    builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            update(arrayList.get(position).get("Id"), "1");
                        }
                    });

                    builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(context, "NEGATIVE", Toast.LENGTH_LONG).show();
                        }
                    });
                    builder.show();
                }

            });

            holder.requests.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), com.example.myrental.Requests.class);
                    intent.putExtra("id",""+arrayList.get(position).get("Id"));
                    v.getContext().startActivity(intent);
                }
            });

        }

        private void addimage(Bitmap bitmap) {
            ImageList.add(bitmap);
        }

        private void update(final Object id, String s) {

            final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("AddResidenceOwner");

            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    databaseReference.removeEventListener(this);
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        HashMap<String,Object> hashMap = (HashMap<String, Object>) ds.getValue();
                        if (id.equals(hashMap.get("Id"))) {
                            hashMap.put("Status","YES");

                            databaseReference.child(String.valueOf(ds.getKey())).setValue(hashMap);
                            break;
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        @Override
        public int getItemCount() {
            return arrayList.size();
        }


        class MyViewHolder extends RecyclerView.ViewHolder
        {
            ImageView imageView;
            TextView Owner, Address, Mobile, Type, Rent;
            EditText landlord, location, mobile, property, area, duration, deposit, rent;
            Button edit, rented, requests;
            ViewPager viewPager;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);

                imageView = itemView.findViewById(R.id.imageView);
                viewPager = itemView.findViewById(R.id.viewpager);
                landlord = itemView.findViewById(R.id.landlord);
                location = itemView.findViewById(R.id.landlord_address);
                mobile = itemView.findViewById(R.id.phone);
                property = itemView.findViewById(R.id.type);
                area = itemView.findViewById(R.id.area);
                duration = itemView.findViewById(R.id.duration);
                deposit = itemView.findViewById(R.id.deposit);
                rent = itemView.findViewById(R.id.rent);

                edit = itemView.findViewById(R.id.edit);
                rented = itemView.findViewById(R.id.rented);
                requests = itemView.findViewById(R.id.requests);
            }
        }

    }

}
