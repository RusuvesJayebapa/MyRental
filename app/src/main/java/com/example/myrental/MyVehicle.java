package com.example.myrental;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
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
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class MyVehicle extends AppCompatActivity
{
    ArrayList<HashMap<String, Object>> arrayList = new ArrayList<>();
    ArrayList<HashMap<String, Object>> newArrayList = new ArrayList<>();
    HashMap<String,Object> hashMap = new HashMap<>();
    ArrayList<HashMap> list = new ArrayList<>();
    DatabaseReference databaseReference;
    SessionManager sessionManager;
    ProgressDialog progressDialog;
    RecyclerView rvItem;
    MyAdapter itemAdapter;
    SharedPreferences sharedPreferences;
    String day, mon;
    int flag, max = 0, rentVar;
    String vehicleClassVar = "", fuelTypeVar = "";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_residence);

        getSupportActionBar().setTitle("Not Rented");

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

    private void sortDialog()
    {
        String[] options = {"Ascending", "Descending"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Sort by");
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
        builder.create().show();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
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
        final ArrayAdapter<String> vehicleClassAdapter = new ArrayAdapter<String>(MyVehicle.this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.vehicle_class));
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
        final ArrayAdapter<String> fuelTypeAdapter = new ArrayAdapter<String>(MyVehicle.this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.fuel));
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
        seekBar.setMin(0);
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
        databaseReference = FirebaseDatabase.getInstance().getReference().child("AddVehicleOwner");

        databaseReference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    rvItem.setAdapter(null);
                    arrayList.clear();
                    list.clear();
                    newArrayList.clear();
                    for (DataSnapshot ds : dataSnapshot.getChildren())
                    {
                        hashMap = (HashMap<String, Object>) ds.getValue();
                        if (hashMap.get("AddedBy").equals(sessionManager.getUID()))
                        {
                            if (Integer.valueOf(hashMap.get("Payment").toString()) > max)
                                max = Integer.valueOf(hashMap.get("Payment").toString());
                            hashMap.put("Key", ds.getKey());
                            arrayList.add(hashMap);
                        }
                    }
                    if (arrayList.isEmpty())
                        progressDialog.cancel();
                    rentVar = max;
                    array();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    private void array()
    {
        if (arrayList.size() == 0)
            progressDialog.cancel();

        databaseReference = FirebaseDatabase.getInstance().getReference().child("RegistrationDatabase");

        databaseReference.addValueEventListener(new ValueEventListener()
        {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                list.clear();
                newArrayList.clear();
                for(int i = 0; i < arrayList.size(); i++)
                {
                    for(DataSnapshot ds : dataSnapshot.getChildren())
                    {
                        HashMap<String, Object> hashMap1 = (HashMap<String, Object>) ds.getValue();
                        if(arrayList.get(i).get("AddedBy").toString().equals(hashMap1.get("UID")))
                        {
                            hashMap1.put("Key", ds.getKey());
                            list.add(hashMap1);
                        }
                    }
                }
                if(list.size() > 0)
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

            LinearLayoutManager layoutManager = new LinearLayoutManager(MyVehicle.this);
            itemAdapter = new MyAdapter(newArrayList, this);
            rvItem.setAdapter(itemAdapter);
            rvItem.setLayoutManager(layoutManager);
        }
        else
        {
            progressDialog.cancel();
        }
    }

    class MyVehicleImageAdapter extends RecyclerView.Adapter<MyVehicleImageAdapter.ImageViewHolder>
    {
        Context mcontext;
        private ArrayList<String> subItemList;

        MyVehicleImageAdapter(ArrayList<String> subItemList, Context context)
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

    @Override
    public void onBackPressed()
    {
        Intent intent = new Intent(MyVehicle.this, com.example.myrental.OnLogin.class);
        startActivity(intent);
    }

    class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> implements Filterable
    {
        RecyclerView.RecycledViewPool viewPool = new RecyclerView.RecycledViewPool();
        ArrayList<HashMap<String,Object>> imageList;
        ArrayList<HashMap<String,Object>> imageListFilter;
        Context mcontext;

        public MyAdapter(ArrayList<HashMap<String,Object>> imageList, Context context)
        {
            progressDialog.cancel();
            this.mcontext = context;
            this.imageList = imageList;
            this.imageListFilter = imageList;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(mcontext).inflate(R.layout.my_vehicle_cardview,parent,false);
            return new MyViewHolder(v);
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position)
        {
            if (!imageListFilter.get(position).get("VehicleClass").toString().equals("Bike"))
            {
                holder.seats.setText("" + imageListFilter.get(position).get("Seats") + " seats");
                holder.fuel.setText("" + imageListFilter.get(position).get("Fuel"));
                if (imageListFilter.get(position).get("AC").equals("YES"))
                    holder.ac.setText("Available");
                else
                    holder.ac.setText("Not Available");
            }

            holder.vehicle.setText(""+imageListFilter.get(position).get("VehicleName"));
            holder.vehicle_class.setText(""+imageListFilter.get(position).get("VehicleClass"));
            holder.mileage.setText(""+imageListFilter.get(position).get("Mileage"));
            holder.power.setText(""+imageListFilter.get(position).get("Power"));
            holder.deposit.setText(""+imageListFilter.get(position).get("Deposit"));
            holder.rent.setText(""+imageListFilter.get(position).get("Payment") +" "+imageListFilter.get(position).get("PaymentTimePeriod"));

            if (imageListFilter.get(position).get("Status").equals("YES"))
            {
                holder.rented.setChecked(true);
            }

            if (imageListFilter.get(position).get("VehicleName").equals("Bike"))
                holder.OnlyCar.setVisibility(View.GONE);

            holder.requests.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    Intent intent = new Intent(v.getContext(), com.example.myrental.Requests.class);
                    intent.putExtra("id",""+imageListFilter.get(position).get("Id"));
                    intent.putExtra("key","2");
                    v.getContext().startActivity(intent);
                }
            });

            holder.edit.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    Intent intent = new Intent(v.getContext(), com.example.myrental.VehicleDetails.class);
                    intent.putExtra("list", imageListFilter.get(position));
                    v.getContext().startActivity(intent);
                }
            });

            holder.rented.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if (holder.rented.isChecked())
                    {
                        final Calendar c = Calendar.getInstance();
                        DatePickerDialog datePickerDialog = new DatePickerDialog(MyVehicle.this, new DatePickerDialog.OnDateSetListener()
                        {
                            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
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

                                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(MyVehicle.this);
                                final String Start_Date;
                                final Switch Options = new Switch(MyVehicle.this);

                                LinearLayout newLinearLayout = new LinearLayout(MyVehicle.this);
                                newLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
                                final TextView Title = new TextView(MyVehicle.this);
                                Title.setText("Select Type Of Agreement");
                                Title.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                                final TextView Self = new TextView(MyVehicle.this);
                                Self.setText("Self");
                                final TextView App = new TextView(MyVehicle.this);
                                App.setText("App");
                                newLinearLayout.addView(Self);
                                newLinearLayout.addView(Options);
                                newLinearLayout.addView(App);
                                newLinearLayout.setGravity(Gravity.CENTER);

                                final TextView DOB = new TextView(MyVehicle.this);
                                DOB.setTextColor(Color.GREEN);
                                DOB.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f);
                                DOB.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                                LinearLayout linearLayout = new LinearLayout(MyVehicle.this);
                                linearLayout.setOrientation(LinearLayout.VERTICAL);
                                linearLayout.setGravity(Gravity.CENTER);
                                linearLayout.addView(Title);
                                linearLayout.addView(newLinearLayout);
                                linearLayout.addView(DOB);

                                Start_Date = day+"/"+mon+"/"+year;
                                DOB.setText("\nThe Start Date You Have Selected Is " + day+"/"+mon+"/"+year + "\nDo you want to mark this vehicle as rented ?");
                                builder.setView(linearLayout);
                                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which)
                                    {
                                        if(Options.isChecked())
                                        {
                                            update(imageListFilter.get(position).get("Id"), "2", Start_Date);
                                        }
                                        else
                                        {
                                            update(imageListFilter.get(position).get("Id"), "1", Start_Date);
                                        }
                                        holder.rented.setChecked(true);
                                    }
                                }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        holder.rented.setChecked(false);
                                    }
                                });
                                builder.show();
                            }
                        }, c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.MONTH), c.get(Calendar.YEAR));
                        datePickerDialog.show();
                    }
                    else
                    {
                        AlertDialog.Builder builder = new AlertDialog.Builder(MyVehicle.this);
                        builder.setTitle("Do You Want To Mark It As Unrented?");
                        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                update(imageListFilter.get(position).get("Id"), "3", "");
                                holder.rented.setChecked(false);
                            }
                        }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                holder.rented.setChecked(true);
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
            MyVehicle.MyVehicleImageAdapter imageAdapter = new MyVehicle.MyVehicleImageAdapter(a, mcontext);
            holder.rvSubItem.setLayoutManager(layoutManager);
            holder.rvSubItem.setAdapter(imageAdapter);
            holder.rvSubItem.setRecycledViewPool(viewPool);

        }

        private void update(final Object id, String s, final String Start_Date)
        {
            if (s.equals("1"))
            {
                final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("AddVehicleOwner");

                final DatabaseReference requestVehicleOwner = FirebaseDatabase.getInstance().getReference().child("RequestVehicleOwner");

                final HashMap<String, Object> requestVehicle = new HashMap<>();

                databaseReference.addListenerForSingleValueEvent(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        databaseReference.removeEventListener(this);
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            final HashMap<String, Object> hashMap = (HashMap<String, Object>) ds.getValue();
                            if (id.equals(hashMap.get("Id"))) {
                                hashMap.put("Status", "YES");

                                databaseReference.child(String.valueOf(ds.getKey())).setValue(hashMap);

                                requestVehicle.clear();
                                requestVehicle.put("Requestor_Id", hashMap.get("AddedBy"));
                                requestVehicle.put("Requestee_Id", hashMap.get("AddedBy"));
                                requestVehicle.put("Vehicle_Id", hashMap.get("Id"));
                                requestVehicle.put("Request_Status", "YES");
                                requestVehicle.put("Start_Date", Start_Date);
                                requestVehicle.put("End_Date", "NULL");

                                requestVehicleOwner.push().setValue(requestVehicle);

                                break;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
            else if (s.equals("2"))
            {
                final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("AddVehicleOwner");

                final DatabaseReference requestVehicleOwner = FirebaseDatabase.getInstance().getReference().child("RequestVehicleOwner");

                requestVehicleOwner.addListenerForSingleValueEvent(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        for (DataSnapshot ds1 : dataSnapshot.getChildren())
                        {
                            flag = 0;
                            final HashMap<String, Object> request = (HashMap<String, Object>) ds1.getValue();
                            if (request.get("Vehicle_Id").toString().equals(id) && request.get("Request_Status").toString().equals("YES")
                                    && request.get("End_Date").toString().equals("NULL"))
                            {
                                flag = 1;
                                request.put("Start_Date", Start_Date);
                                requestVehicleOwner.child(ds1.getKey()).setValue(request);

                                databaseReference.addListenerForSingleValueEvent(new ValueEventListener()
                                {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        databaseReference.removeEventListener(this);
                                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                            final HashMap<String, Object> hashMap = (HashMap<String, Object>) ds.getValue();
                                            if (id.equals(hashMap.get("Id")))
                                            {
                                                hashMap.put("Status", "YES");
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
                            break;
                        }
                        if (flag == 0) {
                            Toast.makeText(MyVehicle.this, "You Have Not Accepted Any Request For Tenancy", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
            else if (s.equals("3"))
            {
                final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("AddVehicleOwner");

                final DatabaseReference requestOwner = FirebaseDatabase.getInstance().getReference().child("RequestVehicleOwner");

                requestOwner.addListenerForSingleValueEvent(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        for (DataSnapshot ds1 : dataSnapshot.getChildren())
                        {
                            final HashMap<String, Object> request = (HashMap<String, Object>) ds1.getValue();
                            if (request.get("Vehicle_Id").toString().equals(id))
                            {
                                request.put("Start_Date", Start_Date);
                                requestOwner.child(ds1.getKey()).removeValue();
                                break;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                databaseReference.addListenerForSingleValueEvent(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            final HashMap<String, Object> hashMap = (HashMap<String, Object>) ds.getValue();
                            if (id.equals(hashMap.get("Id")))
                            {
                                hashMap.put("Status", "NO");
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
        }

        @Override
        public int getItemCount()
        {
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
                        ArrayList<HashMap<String, Object>> filterList = new ArrayList<>();
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
                    imageListFilter = (ArrayList<HashMap<String, Object>>) results.values ;
                    notifyDataSetChanged();
                }
            };
        }

        class MyViewHolder extends RecyclerView.ViewHolder
        {

            RecyclerView rvSubItem;
            LinearLayout OnlyCar;
            TextView vehicle, vehicle_class, mileage, mileageId, power, powerId, deposit, rent, seats, fuel, ac;
            Button edit, requests;
            Switch rented;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);

                rvSubItem = itemView.findViewById(R.id.rv_sub_item);

                OnlyCar = itemView.findViewById(R.id.OnlyCar);

                vehicle = itemView.findViewById(R.id.vehicle);
                vehicle_class = itemView.findViewById(R.id.vehicleclass);
                mileage = itemView.findViewById(R.id.mileage);
                mileageId = itemView.findViewById(R.id.mileageId);
                power = itemView.findViewById(R.id.power);
                powerId = itemView.findViewById(R.id.powerId);
                deposit = itemView.findViewById(R.id.deposit);
                rent = itemView.findViewById(R.id.rent);

                seats = itemView.findViewById(R.id.seats);
                fuel = itemView.findViewById(R.id.fuel);
                ac = itemView.findViewById(R.id.ac);

                requests = itemView.findViewById(R.id.requests);
                edit = itemView.findViewById(R.id.edit);
                rented = itemView.findViewById(R.id.rented);
            }
        }
    }
}
