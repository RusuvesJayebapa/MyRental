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
import android.widget.EditText;
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

public class MyResidence extends AppCompatActivity
{
    ArrayList<HashMap<String, Object>> arrayList = new ArrayList<>();
    ArrayList<HashMap<String, Object>> newArrayList = new ArrayList<>();
    HashMap<String,Object> hashMap = new HashMap<>();
    ArrayList<HashMap> list = new ArrayList<>();
    DatabaseReference AddResidenceOwner, RegistrationDatabase;
    SessionManager sessionManager;
    ProgressDialog progressDialog;
    RecyclerView rvItem;
    MyAdapter itemAdapter;
    SharedPreferences sharedPreferences;
    String day, mon;
    int flag, max = 0, rentVar;
    String categoryVar = "", propertyTypeNo = "", propertyType = "", areaVar = "";

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
        AlertDialog dialog = builder.create();
        dialog.show();

        ((ImageView)dialog.findViewById(android.R.id.icon)).setColorFilter(Color.BLACK);
    }

    private void filterDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Filter by");
        builder.setIcon(R.drawable.ic_sort_black_24dp);

        LinearLayout mainLinearLayout = new LinearLayout(this);
        mainLinearLayout.setPadding(20, 20, 20, 20);
        mainLinearLayout.setOrientation(LinearLayout.VERTICAL);

        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setPadding(20, 20, 20, 20);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setBackgroundResource(R.drawable.linearlayoutbox);

        LinearLayout.LayoutParams paramTextView = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        paramTextView.setMargins(0,50,0,0);

        LinearLayout.LayoutParams paramEditView = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 115);
        paramEditView.weight = 1;
        paramEditView.setMargins(2,20,2,10);

        LinearLayout.LayoutParams paramRentView = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        paramRentView.setMargins(2,20,2,10);

        final TextView categoryTextView = new TextView(this);
        categoryTextView.setText("Category : ");
        categoryTextView.setTextColor(Color.BLACK);
        final Spinner category = new Spinner(this);
        category.setLayoutParams(paramEditView);
        final ArrayAdapter<String> categoryAdapter = new ArrayAdapter<String>(MyResidence.this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.category));
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        category.setAdapter(categoryAdapter);
        category.setBackgroundResource(R.drawable.editbox);

        LinearLayout innerLinearLayout2 = new LinearLayout(this);
        innerLinearLayout2.setOrientation(LinearLayout.HORIZONTAL);
        final TextView propertyTypeTextView = new TextView(this);
        propertyTypeTextView.setText("PropertyType : ");
        propertyTypeTextView.setTextColor(Color.BLACK);
        propertyTypeTextView.setLayoutParams(paramTextView);
        final EditText propertyTypeNo1 = new EditText(this);
        propertyTypeNo1.setLayoutParams(paramEditView);
        propertyTypeNo1.setBackgroundResource(R.drawable.editbox);
        propertyTypeNo1.setText(propertyTypeNo);
        final Spinner propertyType1 = new Spinner(this);
        final ArrayAdapter<String> propertyTypeAdapter = new ArrayAdapter<String>(MyResidence.this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.property_type));
        propertyTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        propertyType1.setAdapter(propertyTypeAdapter);
        propertyType1.setLayoutParams(paramEditView);
        propertyType1.setBackgroundResource(R.drawable.editbox);
        innerLinearLayout2.addView(propertyTypeNo1);
        innerLinearLayout2.addView(propertyType1);

        final TextView areaTextView = new TextView(this);
        areaTextView.setText("Area (Sq. ft.) : ");
        areaTextView.setTextColor(Color.BLACK);
        areaTextView.setLayoutParams(paramTextView);
        final EditText area = new EditText(this);
        area.setLayoutParams(paramEditView);
        area.setText(areaVar);
        area.setBackgroundResource(R.drawable.editbox);

        final TextView rentTextView = new TextView(this);
        rentTextView.setText("Rent : ");
        rentTextView.setTextColor(Color.BLACK);
        rentTextView.setLayoutParams(paramTextView);
        final TextView seekBarNum = new TextView(this);
        seekBarNum.setLayoutParams(paramRentView);
        seekBarNum.setTextColor(Color.BLACK);
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

        linearLayout.addView(categoryTextView);
        linearLayout.addView(category);
        linearLayout.addView(propertyTypeTextView);
        linearLayout.addView(innerLinearLayout2);
        linearLayout.addView(areaTextView);
        linearLayout.addView(area);
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
                categoryVar = category.getSelectedItem().toString();
                propertyType = propertyType1.getSelectedItem().toString();
                propertyTypeNo = propertyTypeNo1.getText().toString();
                areaVar = area.getText().toString();
                display();
            }
        }).setNeutralButton("Clear All", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                categoryVar = "";
                propertyType = "";
                propertyTypeNo = "";
                areaVar = "";
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
        AddResidenceOwner = FirebaseDatabase.getInstance().getReference().child("AddResidenceOwner");

        AddResidenceOwner.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    rvItem.setAdapter(null);
                    arrayList.clear();
                    list.clear();
                    for (DataSnapshot ds : dataSnapshot.getChildren())
                    {
                        hashMap = (HashMap<String, Object>) ds.getValue();
                        if (hashMap.get("AddedBy").equals(sessionManager.getUID()))
                        {
                            if (Integer.valueOf(hashMap.get("Rent").toString()) > max)
                                max = Integer.valueOf(hashMap.get("Rent").toString());
                            hashMap.put("Key", ds.getKey());
                            arrayList.add(hashMap);
                        }
                    }
                    rentVar = max;
                    array();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void array()
    {
        if (arrayList.size() == 0)
            progressDialog.cancel();

        RegistrationDatabase = FirebaseDatabase.getInstance().getReference().child("RegistrationDatabase");

        RegistrationDatabase.addValueEventListener(new ValueEventListener()
        {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                list.clear();
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
                if (row.get("Category").toString().contains(categoryVar.trim()) &&
                        row.get("Property_Typeno").toString().contains(propertyTypeNo.trim()) &&
                        row.get("Property_Type").toString().contains(propertyType.trim()) &&
                        row.get("Area").toString().contains(areaVar) &&
                        Integer.valueOf(row.get("Rent").toString()) <= rentVar)
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
                        return o1.get("Rent").toString().compareTo(o2.get("Rent").toString());
                    }
                });
            }
            else if (mSortSetting.equals("descending"))
            {
                Collections.sort(newArrayList, new Comparator<HashMap>() {
                    @Override
                    public int compare(HashMap o1, HashMap o2) {
                        return o2.get("Rent").toString().compareTo(o1.get("Rent").toString());
                    }
                });
            }

            LinearLayoutManager layoutManager = new LinearLayoutManager(MyResidence.this);
            itemAdapter = new MyResidence.MyAdapter(newArrayList, this);
            rvItem.setAdapter(itemAdapter);
            rvItem.setLayoutManager(layoutManager);
        }
        else
        {
            progressDialog.cancel();
        }
    }

    class MyResidenceImageAdapter extends RecyclerView.Adapter<MyResidenceImageAdapter.ImageViewHolder>
    {
        Context mcontext;
        private ArrayList<String> subItemList;

        MyResidenceImageAdapter(ArrayList<String> subItemList, Context context)
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
            subItemViewHolder.imageView.setAdjustViewBounds(true);
            Picasso.get().load(Uri.parse(subItemList.get(i))).placeholder(R.drawable.loading).into(subItemViewHolder.imageView);

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
    public void onBackPressed() {
        Intent intent = new Intent(MyResidence.this, com.example.myrental.OnLogin.class);
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
            View v = LayoutInflater.from(mcontext).inflate(R.layout.my_residence_cardview,parent,false);
            return new MyViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position)
        {
            holder.landlord.setText(""+list.get(position).get("Firstname")+" "+list.get(position).get("Middlename")+" "+list.get(position).get("Lastname"));
            holder.location.setText(imageListFilter.get(position).get("Buildingname")+", "+imageListFilter.get(position).get("Road")+", "+imageListFilter.get(position).get("Location")+", "+imageListFilter.get(position).get("City")+"-"+imageListFilter.get(position).get("Pincode")+", "+imageListFilter.get(position).get("State"));
            holder.mobile.setText(""+list.get(position).get("Phone"));
            holder.property.setText(""+imageListFilter.get(position).get("Category")+", "+imageListFilter.get(position).get("Property_Typeno")+" "+imageListFilter.get(position).get("Property_Type"));
            holder.area.setText(""+imageListFilter.get(position).get("Area"));
            holder.deposit.setText(""+imageListFilter.get(position).get("Deposit"));
            holder.rent.setText(""+imageListFilter.get(position).get("Rent") +" "+imageListFilter.get(position).get("Rent_Period"));

            if (imageListFilter.get(position).containsKey("Appliances"))
            {
                for (int i = 0; i < ((HashMap<String, Integer>) imageListFilter.get(position).get("Appliances")).size(); i++)
                {
                    if (i%4 == 0)
                        holder.applicance_furniture.append("\n\n");
                    holder.applicance_furniture.append(((HashMap<String, Integer>) imageListFilter.get(position).get("Appliances")).keySet().toArray()[i]
                            + "-> " + ((HashMap<String, Integer>) imageListFilter.get(position).get("Appliances")).values().toArray()[i] + "\t\t\t\t\t");
                }
            }
            else
            {
                holder.Applicance_Furniture.setVisibility(View.GONE);
                holder.line1.setVisibility(View.GONE);
            }

            if (imageListFilter.get(position).containsKey("Clause"))
            {
                for (int i = 0; i < ((ArrayList<String>) imageListFilter.get(position).get("Clause")).size(); i++)
                    holder.clause.append("\n\n" + (i+1) + ") " + ((ArrayList<String>) imageListFilter.get(position).get("Clause")).get(i));
            }
            else
            {
                holder.Clause.setVisibility(View.GONE);
                holder.line2.setVisibility(View.GONE);
            }

            holder.edit.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    Intent intent = new Intent(v.getContext(), com.example.myrental.landlord.class);
                    intent.putExtra("edit", list.get(position));
                    intent.putExtra("hashMap", imageListFilter.get(position));
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
                        DatePickerDialog datePickerDialog = new DatePickerDialog(MyResidence.this, new DatePickerDialog.OnDateSetListener()
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

                                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(MyResidence.this);
                                final String Start_Date;
                                final Switch Options = new Switch(MyResidence.this);

                                LinearLayout newLinearLayout = new LinearLayout(MyResidence.this);
                                newLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
                                final TextView Title = new TextView(MyResidence.this);
                                Title.setText("Select Type Of Agreement");
                                Title.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                                final TextView Self = new TextView(MyResidence.this);
                                Self.setText("Self");
                                final TextView App = new TextView(MyResidence.this);
                                App.setText("App");
                                newLinearLayout.addView(Self);
                                newLinearLayout.addView(Options);
                                newLinearLayout.addView(App);
                                newLinearLayout.setGravity(Gravity.CENTER);

                                final TextView DOB = new TextView(MyResidence.this);
                                DOB.setTextColor(Color.GREEN);
                                DOB.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f);
                                DOB.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                                LinearLayout linearLayout = new LinearLayout(MyResidence.this);
                                linearLayout.setOrientation(LinearLayout.VERTICAL);
                                linearLayout.setGravity(Gravity.CENTER);
                                linearLayout.addView(Title);
                                linearLayout.addView(newLinearLayout);
                                linearLayout.addView(DOB);

                                Start_Date = day+"/"+mon+"/"+year;
                                DOB.setText("\nThe Start Date You Have Selected Is " + day+"/"+mon+"/"+year + "\nDo you want to mark this residence as rented ?");
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
                                        holder.rented.setChecked(false);
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
                }

            });

            holder.call.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+imageListFilter.get(position).get("Phone")));
                    v.getContext().startActivity(intent);
                }
            });

            holder.requests.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    Intent intent = new Intent(v.getContext(), com.example.myrental.Requests.class);
                    intent.putExtra("id",""+imageListFilter.get(position).get("Id"));
                    intent.putExtra("key","1");
                    v.getContext().startActivity(intent);
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
            MyResidence.MyResidenceImageAdapter imageAdapter = new MyResidence.MyResidenceImageAdapter(a, mcontext);
            holder.rvSubItem.setLayoutManager(layoutManager);
            holder.rvSubItem.setAdapter(imageAdapter);
            holder.rvSubItem.setRecycledViewPool(viewPool);

        }

        private void update(final Object id, String s, final String Start_Date)
        {
            if (s.equals("1"))
            {
                final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("AddResidenceOwner");

                final DatabaseReference requestOwner = FirebaseDatabase.getInstance().getReference().child("RequestOwner");

                final HashMap<String, Object> request = new HashMap<>();

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

                                request.clear();
                                request.put("Requestor_Id", hashMap.get("AddedBy"));
                                request.put("Requestee_Id", hashMap.get("AddedBy"));
                                request.put("Property_Id", hashMap.get("Id"));
                                request.put("Request_Status", "YES");
                                request.put("Requestee_Doc_Verified", "YES");
                                request.put("Requestor_Doc_Verified", "YES");
                                request.put("StampDuty_Registration", "YES");
                                request.put("Agreement", "YES");
                                request.put("Start_Date", Start_Date);
                                request.put("End_Date", "NULL");

                                requestOwner.push().setValue(request);

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
                final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("AddResidenceOwner");

                final DatabaseReference requestOwner = FirebaseDatabase.getInstance().getReference().child("RequestOwner");

                final HashMap<String, Object> request = new HashMap<>();

                requestOwner.addListenerForSingleValueEvent(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        for (DataSnapshot ds1 : dataSnapshot.getChildren())
                        {
                            flag = 0;
                            final HashMap<String, Object> request = (HashMap<String, Object>) ds1.getValue();
                            if (request.get("Property_Id").toString().equals(id) && request.get("Request_Status").toString().equals("YES")
                                    && request.get("End_Date").toString().equals("NULL"))
                            {
                                flag = 1;
                                request.put("Start_Date", Start_Date);
                                requestOwner.child(ds1.getKey()).setValue(request);

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
                            Toast.makeText(MyResidence.this, "You Have Not Accepted Any Request For Tenancy", Toast.LENGTH_LONG).show();
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
                        ArrayList<HashMap<String,Object>> filterList = new ArrayList<>();
                        for (int i = 0; i < imageListFilter.size(); i++)
                        {
                            HashMap<String, Object> row = imageListFilter.get(i);

                            if (row.get("Buildingname").toString().toLowerCase().contains(charcater.toLowerCase()) ||
                                    row.get("Road").toString().toLowerCase().contains(charcater.toLowerCase()) ||
                                    row.get("Location").toString().toLowerCase().contains(charcater.toLowerCase()) ||
                                    row.get("City").toString().toLowerCase().contains(charcater.toLowerCase()) ||
                                    row.get("State").toString().toLowerCase().contains(charcater.toLowerCase()) ||
                                    row.get("Pincode").toString().toLowerCase().contains(charcater.toLowerCase()) ||
                                    row.get("Category").toString().toLowerCase().contains(charcater.toLowerCase()) ||
                                    row.get("Property_Typeno").toString().toLowerCase().contains(charcater.toLowerCase()) ||
                                    row.get("Property_Type").toString().toLowerCase().contains(charcater.toLowerCase()) ||
                                    row.get("Area").toString().toLowerCase().contains(charcater.toLowerCase()) ||
                                    row.get("Duration").toString().toLowerCase().contains(charcater.toLowerCase()) ||
                                    row.get("Duration_Period").toString().toLowerCase().contains(charcater.toLowerCase()) ||
                                    row.get("Deposit").toString().toLowerCase().contains(charcater.toLowerCase()) ||
                                    row.get("Rent").toString().toLowerCase().contains(charcater.toLowerCase()) ||
                                    list.get(i).get("Firstname").toString().toLowerCase().contains(charcater.toLowerCase()) ||
                                    list.get(i).get("Middlename").toString().toLowerCase().contains(charcater.toLowerCase()) ||
                                    list.get(i).get("Lastname").toString().toLowerCase().contains(charcater.toLowerCase()) ||
                                    list.get(i).get("Phone").toString().toLowerCase().contains(charcater.toLowerCase()) ||
                                    row.get("Rent_Period").toString().toLowerCase().contains(charcater.toLowerCase())
                            ){
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
                    imageListFilter = (ArrayList<HashMap<String,Object>>) results.values ;
                    notifyDataSetChanged();
                }
            };
        }

        class MyViewHolder extends RecyclerView.ViewHolder
        {
            RecyclerView rvSubItem;
            ImageView call;
            TextView landlord, location, mobile, property, area, deposit, rent, applicance_furniture, clause, line1,line2;
            Button edit, requests;
            Switch rented;
            LinearLayout Applicance_Furniture, Clause;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);

                Applicance_Furniture = itemView.findViewById(R.id.Applicance_Furniture);
                Clause = itemView.findViewById(R.id.Clause);

                rvSubItem = itemView.findViewById(R.id.rv_sub_item);
                landlord = itemView.findViewById(R.id.landlord);
                location = itemView.findViewById(R.id.landlord_address);
                mobile = itemView.findViewById(R.id.phone);
                property = itemView.findViewById(R.id.type);
                area = itemView.findViewById(R.id.area);
                applicance_furniture = itemView.findViewById(R.id.applicance_furniture);
                clause = itemView.findViewById(R.id.clause);
                line1 = itemView.findViewById(R.id.line1);
                line2 = itemView.findViewById(R.id.line2);
                deposit = itemView.findViewById(R.id.deposit);
                rent = itemView.findViewById(R.id.rent);

                call = itemView.findViewById(R.id.call);

                edit = itemView.findViewById(R.id.edit);
                rented = itemView.findViewById(R.id.rented);
                requests = itemView.findViewById(R.id.requests);
            }
        }
    }
}
