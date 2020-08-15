package com.example.myrental;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class VehicleTenant extends AppCompatActivity {

    ArrayList<HashMap> arrayList = new ArrayList<>();
    ArrayList<HashMap> newArrayList = new ArrayList<>();
    ArrayList<HashMap> list = new ArrayList<>();
    DatabaseReference databaseReference;
    SessionManager sessionManager;
    ProgressDialog progressDialog;
    RecyclerView rvItem;
    String day, mon;
    SharedPreferences sharedPreferences;
    MyNewAdapter itemAdapter;
    int  max = 0, rentVar;
    String vehicleClassVar = "", fuelTypeVar = "";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_residence);

        sessionManager = new SessionManager(getApplicationContext());

        rvItem = findViewById(R.id.Recycler);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_menu);
        bottomNavigationView.setItemIconTintList(null);
        bottomNavigationView.setOnNavigationItemSelectedListener(navigationItemSelectedListener);

        sharedPreferences = this.getSharedPreferences("My_Pref", MODE_PRIVATE);

        getList();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    int id = menuItem.getItemId();

                    if (id == R.id.sorting)
                    {
                        sortDialog();
                        return true;
                    }
                    if (id == R.id.filter)
                    {
                        filterDialog();
                        return true;
                    }
                    return true;
                }
            };

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
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        int id = item.getItemId();

        if (id == R.id.sorting)
        {
            sortDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void sortDialog()
    {
        String[] options = {"Ascending", "Descending"};
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);

        builder.setTitle("Sort By Rent");
        builder.setIcon(R.drawable.ic_sort_black_24dp);

        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                if (which == 0)
                {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("Sort", "ascending");
                    editor.apply();
                    display();
                }
                if (which == 1)
                {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("Sort", "descending");
                    editor.apply();
                    display();
                }
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();

        ((ImageView)dialog.findViewById(android.R.id.icon)).setColorFilter(Color.BLACK);
    }

    private void filterDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Filter By");
        builder.setIcon(R.drawable.ic_sort_black_24dp);

        LinearLayout mainLinearLayout = new LinearLayout(this);
        mainLinearLayout.setPadding(20, 20, 20, 20);
        mainLinearLayout.setOrientation(LinearLayout.VERTICAL);

        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setPadding(20, 20, 20, 20);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setBackgroundResource(R.drawable.linearlayoutbox);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0,20,0,10);

        LinearLayout.LayoutParams paramSub = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        paramSub.setMargins(0,50,0,0);

        final TextView vehicleClassTextView = new TextView(this);
        vehicleClassTextView.setText("Vehicle Type : ");
        vehicleClassTextView.setTextColor(Color.BLACK);
        final Spinner vehicleClass = new Spinner(this);
        vehicleClass.setLayoutParams(params);
        final ArrayAdapter<String> vehicleClassAdapter = new ArrayAdapter<String>(VehicleTenant.this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.vehicle_class));
        vehicleClass.setAdapter(vehicleClassAdapter);
        if (!vehicleClassVar.equals(""))
            vehicleClass.setSelection(vehicleClassAdapter.getPosition(vehicleClassVar));
        vehicleClass.setBackgroundResource(R.drawable.editbox);

        final TextView fuelTypeTextView = new TextView(this);
        fuelTypeTextView.setText("Fuel : ");
        fuelTypeTextView.setTextColor(Color.BLACK);
        fuelTypeTextView.setLayoutParams(paramSub);
        final Spinner fuelType = new Spinner(this);
        fuelType.setLayoutParams(params);
        final ArrayAdapter<String> fuelTypeAdapter = new ArrayAdapter<String>(VehicleTenant.this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.fuel));
        fuelType.setAdapter(fuelTypeAdapter);
        if (!fuelTypeVar.equals(""))
            fuelType.setSelection(fuelTypeAdapter.getPosition(fuelTypeVar));
        fuelType.setBackgroundResource(R.drawable.editbox);

        final TextView rentTextView = new TextView(this);
        rentTextView.setText("Rent : ");
        rentTextView.setTextColor(Color.BLACK);
        rentTextView.setLayoutParams(paramSub);
        final TextView seekBarNum = new TextView(this);
        seekBarNum.setTextColor(Color.BLACK);
        seekBarNum.setLayoutParams(params);
        String s = " ";
        for (int i = 0 ; i < 90-(String.valueOf(rentVar).length()*2); i++)
            s += " ";
        seekBarNum.setText(" 0" + s + rentVar);
        SeekBar seekBar = new SeekBar(this);
        seekBar.setPadding(15,0,15,0);
        seekBar.setMax(max);
        seekBar.setProgress(rentVar);
        rentVar = seekBar.getProgress();
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                String s = " ";
                for (int i = 0 ; i < 90-(String.valueOf(progress).length()*2); i++)
                    s += " ";
                seekBarNum.setText(" 0" + s + progress);
                rentVar = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        linearLayout.addView(vehicleClassTextView);
        linearLayout.addView(vehicleClass);
        linearLayout.addView(fuelTypeTextView);
        linearLayout.addView(fuelType);
        linearLayout.addView(rentTextView);
        linearLayout.addView(seekBarNum);
        linearLayout.addView(seekBar);

        mainLinearLayout.addView(linearLayout);

        builder.setView(mainLinearLayout);
        builder.setPositiveButton("Apply", new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                vehicleClassVar = vehicleClass.getSelectedItem().toString();
                fuelTypeVar = fuelType.getSelectedItem().toString();
                display();
            }
        }).setNeutralButton("Clear All", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                vehicleClassVar = "";
                fuelTypeVar = "";
                rentVar = max;
                display();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();

        ((ImageView)dialog.findViewById(android.R.id.icon)).setColorFilter(Color.BLACK);
    }

    private void getList()
    {
        DatabaseReference requestVehicleOwner = FirebaseDatabase.getInstance().getReference().child("RequestVehicleOwner");
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("AddVehicleOwner");

        requestVehicleOwner.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                databaseReference.addValueEventListener(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        rvItem.setAdapter(null);
                        arrayList.clear();
                        list.clear();
                        newArrayList.clear();
                        for (DataSnapshot ds : dataSnapshot.getChildren())
                        {
                            HashMap<String, Object> hashMap = (HashMap<String, Object>) ds.getValue();
                            if (hashMap.get("Status").equals("NO"))
                            {
                                if (Integer.valueOf(hashMap.get("Payment").toString()) > max)
                                    max = Integer.valueOf(hashMap.get("Payment").toString());
                                arrayList.add(hashMap);
                            }
                        }
                        if (arrayList.size() == 0)
                            progressDialog.cancel();
                        rentVar = max;
                        array();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError)
                    {
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void array()
    {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("RegistrationDatabase");

        databaseReference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                list.clear();
                newArrayList.clear();
                for(int i = 0; i < arrayList.size(); i++) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        HashMap<String, Object> hashMap1 = (HashMap<String, Object>) ds.getValue();
                        if (arrayList.get(i).get("AddedBy").toString().equals(hashMap1.get("UID"))) {
                            list.add(hashMap1);
                        }
                    }
                }
                if(list.size() == 0)
                    progressDialog.cancel();
                display();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void display()
    {
        if(arrayList.size() > 0)
        {
            rvItem.setAdapter(null);

            newArrayList.clear();
            for (HashMap row : arrayList)
            {
                if (row.get("VehicleClass").toString().contains(vehicleClassVar.trim()) &&
                        row.get("Fuel").toString().contains(fuelTypeVar.trim()) &&
                        Integer.valueOf(row.get("Payment").toString()) <= rentVar)
                {
                    newArrayList.add(row);
                }
            }

            String mSortSetting = sharedPreferences.getString("Sort", "ascending");

            if (mSortSetting.equals("ascending"))
            {
                Collections.sort(newArrayList, new Comparator<HashMap>() {
                    @Override
                    public int compare(HashMap o1, HashMap o2)
                    {
                        return o1.get("Payment").toString().compareTo(o2.get("Payment").toString());
                    }
                });
            }
            else if (mSortSetting.equals("descending"))
            {
                Collections.sort(newArrayList, new Comparator<HashMap>() {
                    @Override
                    public int compare(HashMap o1, HashMap o2) {
                        return o2.get("Payment").toString().compareTo(o1.get("Payment").toString());
                    }
                });
            }

            LinearLayoutManager layoutManager = new LinearLayoutManager(VehicleTenant.this);
            itemAdapter = new MyNewAdapter(newArrayList, this);
            rvItem.setAdapter(itemAdapter);
            rvItem.setLayoutManager(layoutManager);
        }
        else
        {
            progressDialog.cancel();
        }
    }

    class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder>
    {
        Context mcontext;
        private ArrayList<String> subItemList;

        ImageAdapter(ArrayList<String> subItemList, Context context)
        {
            this.mcontext = context;
            this.subItemList = subItemList;
        }

        @NonNull
        @Override
        public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
        {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.image_recycle_view, viewGroup, false);
            return new ImageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ImageViewHolder subItemViewHolder, final int i)
        {
            subItemViewHolder.imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            Picasso.get().load(subItemList.get(i)).placeholder(R.drawable.loading).into(subItemViewHolder.imageView);

            subItemViewHolder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    final ImageView imageView = new ImageView(mcontext);
                    imageView.setMinimumWidth(1080);
                    imageView.setMaxWidth(1080);
                    imageView.setMaxHeight(1500);
                    imageView.setMinimumHeight(1500);
                    imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                    Picasso.get().load(subItemList.get(i)).into(imageView);
                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(mcontext);
                    builder.setCancelable(true);
                    builder.setView(imageView);
                    builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    android.app.AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                    alertDialog.getWindow().setLayout(1080,1500);

                }
            });
        }

        @Override
        public int getItemCount()
        {
            return subItemList.size();
        }

        class ImageViewHolder extends RecyclerView.ViewHolder
        {
            ImageView imageView;

            ImageViewHolder(View itemView)
            {
                super(itemView);
                imageView = itemView.findViewById(R.id.img_sub_item);
            }
        }
    }

    class MyNewAdapter extends RecyclerView.Adapter<MyNewAdapter.MyViewHolder> implements Filterable
    {
        RecyclerView.RecycledViewPool viewPool = new RecyclerView.RecycledViewPool();
        ArrayList<HashMap> imageList;
        ArrayList<HashMap> imageListFilter;
        Context mcontext;

        public MyNewAdapter(ArrayList<HashMap> imageList, Context context)
        {
            progressDialog.cancel();
            this.mcontext = context;
            this.imageList = imageList;
            this.imageListFilter = imageList;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
        {
            View v = LayoutInflater.from(mcontext).inflate(R.layout.vehicle_tenant_cardview, parent,false);
            return new MyViewHolder(v);
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position)
        {
            StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl("gs://my-rental-4ff8d.appspot.com/Profile_Pics/");
            storageReference = storageReference.child(list.get(position).get("UID").toString());
            storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
            {
                @Override
                public void onSuccess(Uri uri)
                {
                    Picasso.get().load(uri).placeholder(R.drawable.loading).into(holder.profile_image);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) { System.out.println("Image "+e);}
            });

            holder.owner.setText(""+list.get(position).get("Firstname")+" "+list.get(position).get("Middlename")+" "+list.get(position).get("Lastname"));
            holder.address.setText(list.get(position).get("Buildingname")+", "+list.get(position).get("Road")+", "+list.get(position).get("Location")
                    +", "+list.get(position).get("City")+"-"+list.get(position).get("Pincode")+", "+list.get(position).get("State"));
            holder.age.setText(""+ Period.between(LocalDate.parse(""+list.get(position).get("DOB"), DateTimeFormatter.ofPattern("dd/MM/yyyy")), LocalDate.now()).getYears());
            holder.mobile.setText(String.valueOf(list.get(position).get("Phone")));
            holder.occupation.setText(String.valueOf(list.get(position).get("Occupation")));

            if (!imageListFilter.get(position).get("VehicleClass").toString().equals("Bike"))
            {
                holder.seats.setText("" + imageListFilter.get(position).get("Seats") + " seats");
                if (imageListFilter.get(position).get("AC").equals("YES"))
                    holder.ac.setText("Available");
                else
                    holder.ac.setText("Not Available");
            }
            else
            {
                holder.seats.setVisibility(View.GONE);
                holder.ac.setVisibility(View.GONE);
            }

            holder.vehicle.setText(""+imageListFilter.get(position).get("VehicleName"));
            holder.vehicle_class.setText(""+imageListFilter.get(position).get("VehicleClass"));
            holder.mileage.setText(""+imageListFilter.get(position).get("Mileage"));
            holder.power.setText(""+imageListFilter.get(position).get("Power"));
            holder.deposit.setText(""+imageListFilter.get(position).get("Deposit"));
            holder.rent.setText(""+imageListFilter.get(position).get("Payment") +" "+imageListFilter.get(position).get("PaymentTimePeriod"));


            DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("RequestVehicleOwner");

            reference.addValueEventListener(new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot ds : dataSnapshot.getChildren())
                    {
                        HashMap<String,Object> requestOwner = (HashMap<String, Object>) ds.getValue();
                        if (requestOwner.get("Vehicle_Id").equals(imageListFilter.get(position).get("Id")) && requestOwner.get("Requestor_Id").equals(sessionManager.getUID()))
                        {
                            holder.request.setText("Sent Request");
                            holder.request.setTextColor(Color.parseColor("#03A9F4"));
                            holder.request.setBackgroundResource(R.drawable.linearlayoutbox);
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
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+imageListFilter.get(position).get("Phone")));
                    v.getContext().startActivity(intent);
                }
            });

            holder.request.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v) {
                    if (holder.request.getText().equals("Request"))
                    {
                        AlertDialog.Builder builder = new AlertDialog.Builder(mcontext);
                        builder.setMessage("Do you want to request the Owner ?");
                        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                request(imageListFilter.get(position), "1");
                            }
                        });

                        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                        builder.show();
                    }
                    else if (holder.request.getText().equals("Sent Request"))
                    {
                        AlertDialog.Builder builder = new AlertDialog.Builder(mcontext);
                        builder.setMessage("Do you want to cancel request?");
                        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                request(imageListFilter.get(position), "2");
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

            // Create layout manager with initial prefetch item count
            LinearLayoutManager layoutManager = new LinearLayoutManager(
                    holder.rvSubItem.getContext(),
                    LinearLayoutManager.HORIZONTAL,
                    false
            );
            ArrayList<String> a = (ArrayList<String>) imageListFilter.get(position).get("URL");
            layoutManager.setInitialPrefetchItemCount(a.size());

            //Create sub item view adapter
            ImageAdapter imageAdapter = new ImageAdapter(a, mcontext);
            holder.rvSubItem.setLayoutManager(layoutManager);
            holder.rvSubItem.setAdapter(imageAdapter);
            holder.rvSubItem.setRecycledViewPool(viewPool);
        }

        private void request(final HashMap<String, Object> a, String key)
        {
            final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("AddVehicleOwner");
            final DatabaseReference databaseReference1 = FirebaseDatabase.getInstance().getReference().child("RequestVehicleOwner");
            final SessionManager sessionManager = new SessionManager(mcontext);

            if (key.equals("1"))
            {
                final Calendar c = Calendar.getInstance();
                DatePickerDialog datePickerDialog = new DatePickerDialog(VehicleTenant.this, new DatePickerDialog.OnDateSetListener()
                {
                    @Override
                    public void onDateSet(DatePicker view, final int year, int month, int dayOfMonth) {
                        if (dayOfMonth<10)
                            day = "0"+dayOfMonth;
                        else
                            day = ""+dayOfMonth;
                        if (month+1<10)
                            mon = "0"+(month+1);
                        else
                            mon = ""+month;

                        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(VehicleTenant.this);
                        final String Start_Date;
                        final TextView DOB = new TextView(VehicleTenant.this);
                        DOB.setTextColor(Color.GREEN);
                        DOB.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f);
                        LinearLayout linearLayout = new LinearLayout(VehicleTenant.this);
                        linearLayout.setGravity(Gravity.CENTER);
                        linearLayout.addView(DOB);

                        Start_Date = day+"/"+mon+"/"+year;
                        DOB.setText("The Start Date You Have Selected Is " + day+"/"+mon+"/"+year + "\nDo You Want To Continue?");
                        builder.setView(linearLayout);
                        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                    {
                                        databaseReference.removeEventListener(this);
                                        for (DataSnapshot ds : dataSnapshot.getChildren())
                                        {
                                            HashMap<String, Object> requestOwner = (HashMap<String, Object>) ds.getValue();
                                            if (a.get("Id").equals(requestOwner.get("Id")))
                                            {
                                                requestOwner.clear();
                                                requestOwner.put("Requestor_Id",sessionManager.getUID());
                                                requestOwner.put("Requestee_Id",a.get("AddedBy"));
                                                requestOwner.put("Vehicle_Id",a.get("Id"));
                                                requestOwner.put("Request_Status","NO");
                                                requestOwner.put("Start_Date",Start_Date);
                                                requestOwner.put("End_Date","NULL");

                                                databaseReference1.push().setValue(requestOwner);

                                                break;
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) { }
                                });
                            }
                        }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        builder.show();
                    }
                }, c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.MONTH), c.get(Calendar.YEAR));
                datePickerDialog.show();
            }

            if (key.equals("2"))
            {
                databaseReference1.addListenerForSingleValueEvent(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        databaseReference1.removeEventListener(this);
                        for (DataSnapshot ds : dataSnapshot.getChildren())
                        {
                            HashMap<String, Object> remove = (HashMap<String, Object>) ds.getValue();
                            if (a.get("Id").equals(remove.get("Vehicle_Id")) &&  a.get("AddedBy").equals(remove.get("Requestee_Id")) &&  sessionManager.getUID().equals(remove.get("Requestor_Id")) )
                            {
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
            if (imageListFilter == null)
                return 0;
            return imageListFilter.size();
        }

        @Override
        public Filter getFilter()
        {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    String charcater = constraint.toString();
                    if (charcater.isEmpty())
                    {
                        imageListFilter = imageList ;
                    }
                    else
                    {
                        ArrayList<HashMap> filterList = new ArrayList<>();
                        for (int i = 0; i < imageListFilter.size(); i++)
                        {
                            HashMap<String, Object> row = imageListFilter.get(i);

                            if (row.get("VehicleName").toString().toLowerCase().contains(charcater.toLowerCase()) ||
                                    (row.get("ModelNumber").toString().toLowerCase().contains(charcater.toLowerCase()) ||
                                            row.get("VehicleClass").toString().toLowerCase().contains(charcater.toLowerCase()) ||
                                            row.get("Mileage").toString().toLowerCase().contains(charcater.toLowerCase()) ||
                                            row.get("Power").toString().toLowerCase().contains(charcater.toLowerCase()) ||
                                            row.get("Seats").toString().toLowerCase().contains(charcater.toLowerCase()) ||
                                            row.get("Fuel").toString().toLowerCase().contains(charcater.toLowerCase()) ||
                                            row.get("AC").toString().toLowerCase().contains(charcater.toLowerCase()) ||
                                            row.get("Deposit").toString().toLowerCase().contains(charcater.toLowerCase()) ||
                                            row.get("Payment").toString().toLowerCase().contains(charcater.toLowerCase()) ||
                                            list.get(i).get("Firstname").toString().toLowerCase().contains(charcater.toLowerCase()) ||
                                            list.get(i).get("Middlename").toString().toLowerCase().contains(charcater.toLowerCase()) ||
                                            list.get(i).get("Lastname").toString().toLowerCase().contains(charcater.toLowerCase()) ||
                                            list.get(i).get("Phone").toString().toLowerCase().contains(charcater.toLowerCase()) ||
                                            list.get(i).get("Buildingname").toString().toLowerCase().contains(charcater.toLowerCase()) ||
                                            list.get(i).get("Flatno").toString().toLowerCase().contains(charcater.toLowerCase()) ||
                                            list.get(i).get("Floorno").toString().toLowerCase().contains(charcater.toLowerCase()) ||
                                            list.get(i).get("Road").toString().toLowerCase().contains(charcater.toLowerCase()) ||
                                            list.get(i).get("Location").toString().toLowerCase().contains(charcater.toLowerCase()) ||
                                            list.get(i).get("City").toString().toLowerCase().contains(charcater.toLowerCase()) ||
                                            list.get(i).get("Pincode").toString().toLowerCase().contains(charcater.toLowerCase()) ||
                                            list.get(i).get("District").toString().toLowerCase().contains(charcater.toLowerCase()) ||
                                            list.get(i).get("State").toString().toLowerCase().contains(charcater.toLowerCase())))
                            {
                                filterList.add(row);
                            }
                        }
                        imageListFilter = filterList ;
                    }
                    FilterResults filterResults = new FilterResults();
                    filterResults.values = imageListFilter;
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    imageListFilter = (ArrayList<HashMap>) results.values ;
                    notifyDataSetChanged();
                }
            };
        }

        class MyViewHolder extends RecyclerView.ViewHolder
        {
            RecyclerView rvSubItem;
            TextView vehicle, vehicle_class, mileage, mileageId, power, powerId, deposit, rent, seats, ac, owner, address, mobile, age, occupation;
            ImageView call, profile_image;
            Button request;

            public MyViewHolder(@NonNull View itemView)
            {
                super(itemView);

                rvSubItem = itemView.findViewById(R.id.rv_sub_item);

                vehicle = itemView.findViewById(R.id.vehicle);
                vehicle_class = itemView.findViewById(R.id.vehicleclass);
                mileage = itemView.findViewById(R.id.mileage);
                mileageId = itemView.findViewById(R.id.mileageId);
                power = itemView.findViewById(R.id.power);
                powerId = itemView.findViewById(R.id.powerId);
                deposit = itemView.findViewById(R.id.deposit);
                rent = itemView.findViewById(R.id.rent);

                seats = itemView.findViewById(R.id.seats);
                ac = itemView.findViewById(R.id.ac);

                profile_image = itemView.findViewById(R.id.profile_image);
                owner = itemView.findViewById(R.id.owner);
                address = itemView.findViewById(R.id.address);
                mobile = itemView.findViewById(R.id.phone);
                age = itemView.findViewById(R.id.age);
                occupation = itemView.findViewById(R.id.occupation);

                call = itemView.findViewById(R.id.call);
                request = itemView.findViewById(R.id.request);
            }
        }
    }
}

