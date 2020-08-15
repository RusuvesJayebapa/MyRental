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
import android.widget.TextView;

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

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class tenant extends AppCompatActivity {

    ArrayList<HashMap> arrayList = new ArrayList<>();
    ArrayList<HashMap> newArrayList = new ArrayList<>();
    ArrayList<HashMap> property = new ArrayList<>();
    ArrayList<HashMap> list = new ArrayList<>();
    DatabaseReference databaseReference, requestOwner;
    SessionManager sessionManager;
    ProgressDialog progressDialog;
    RecyclerView rvItem;
    String day, mon;
    SharedPreferences sharedPreferences;
    MyNewAdapter itemAdapter;
    int  max = 0, rentVar;
    String categoryVar = "", propertyTypeNo = "", propertyType = "", areaVar = "";

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
                    getList();
                }
                if (which == 1)
                {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("Sort", "descending");
                    editor.apply();
                    getList();
                }
            }
        });
        builder.create().show();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void filterDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Filter by");
        builder.setIcon(R.drawable.ic_sort_black_24dp);

        LinearLayout mainLinearLayout = new LinearLayout(this);
        mainLinearLayout.setOrientation(LinearLayout.VERTICAL);
        mainLinearLayout.setPadding(20, 20, 20, 20);

        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0,10,0,10);

        LinearLayout.LayoutParams paramSub = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 115);
        paramSub.weight = 1;
        paramSub.setMargins(10,10,10,10);

        LinearLayout innerLinearLayout1 = new LinearLayout(this);
        innerLinearLayout1.setOrientation(LinearLayout.HORIZONTAL);
        innerLinearLayout1.setLayoutParams(params);
        final TextView categoryTextView = new TextView(this);
        categoryTextView.setText("Category : ");
        final Spinner category = new Spinner(this);
        final ArrayAdapter<String> categoryAdapter = new ArrayAdapter<String>(tenant.this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.category));
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        category.setAdapter(categoryAdapter);
        category.setLayoutParams(paramSub);
        category.setBackgroundResource(R.drawable.editbox);
        innerLinearLayout1.addView(categoryTextView);
        innerLinearLayout1.addView(category);

        LinearLayout innerLinearLayout2 = new LinearLayout(this);
        innerLinearLayout2.setOrientation(LinearLayout.HORIZONTAL);
        innerLinearLayout2.setLayoutParams(params);
        final TextView propertyTypeTextView = new TextView(this);
        propertyTypeTextView.setText("PropertyType : ");
        final EditText propertyTypeNo1 = new EditText(this);
        propertyTypeNo1.setLayoutParams(paramSub);
        propertyTypeNo1.setBackgroundResource(R.drawable.editbox);
        propertyTypeNo1.setText(propertyTypeNo);
        final Spinner propertyType1 = new Spinner(this);
        final ArrayAdapter<String> propertyTypeAdapter = new ArrayAdapter<String>(tenant.this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.property_type));
        propertyTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        propertyType1.setAdapter(propertyTypeAdapter);
        propertyType1.setLayoutParams(paramSub);
        propertyType1.setBackgroundResource(R.drawable.editbox);
        innerLinearLayout2.addView(propertyTypeTextView);
        innerLinearLayout2.addView(propertyTypeNo1);
        innerLinearLayout2.addView(propertyType1);

        LinearLayout innerLinearLayout3 = new LinearLayout(this);
        innerLinearLayout3.setOrientation(LinearLayout.HORIZONTAL);
        innerLinearLayout3.setLayoutParams(params);
        final TextView areaTextView = new TextView(this);
        areaTextView.setText("Area : ");
        final EditText area = new EditText(this);
        area.setLayoutParams(paramSub);
        area.setText(areaVar);
        area.setBackgroundResource(R.drawable.editbox);
        final TextView unit = new TextView(this);
        unit.setText(" sq. ft");
        innerLinearLayout3.addView(areaTextView);
        innerLinearLayout3.addView(area);
        innerLinearLayout3.addView(unit);

        LinearLayout innerLinearLayout4 = new LinearLayout(this);
        innerLinearLayout4.setOrientation(LinearLayout.HORIZONTAL);
        innerLinearLayout4.setLayoutParams(params);
        final TextView seekBarNum = new TextView(this);
        seekBarNum.setText("               " + 0 + "                                                                     " + max);
        final TextView rentTextView = new TextView(this);
        rentTextView.setText("Rent : ");
        SeekBar seekBar = new SeekBar(this);
        seekBar.setMin(0);
        seekBar.setMax(max);
        seekBar.setProgress(max);
        rentVar = seekBar.getProgress();
        seekBar.setLayoutParams(params);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                if (progress > 9999)
                {
                    seekBarNum.setText("               " + 0 + "                                                                     " + progress);
                    rentVar = progress;
                }
                else if (progress > 999)
                {
                    seekBarNum.setText("               " + 0 + "                                                                       " + progress);
                    rentVar = progress;
                }
                else if (progress > 99)
                {
                    seekBarNum.setText("               " + 0 + "                                                                         " + progress);
                    rentVar = progress;
                }
                else if (progress > 9)
                {
                    seekBarNum.setText("               " + 0 + "                                                                           " + progress);
                    rentVar = progress;
                }
                else
                {
                    seekBarNum.setText("               " + 0 + "                                                                             " + progress);
                    rentVar = progress;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        innerLinearLayout4.addView(rentTextView);
        innerLinearLayout4.addView(seekBar);

        linearLayout.addView(innerLinearLayout1);
        linearLayout.addView(innerLinearLayout2);
        linearLayout.addView(innerLinearLayout3);
        linearLayout.addView(seekBarNum);
        linearLayout.addView(innerLinearLayout4);
        mainLinearLayout.addView(linearLayout);

        builder.setView(mainLinearLayout);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
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
        });
        builder.create().show();
    }

//    private void getList()
//    {
//        requestOwner = FirebaseDatabase.getInstance().getReference().child("RequestOwner");
//
//        requestOwner.addValueEventListener(new ValueEventListener()
//        {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
//            {
//                rvItem.setAdapter(null);
//                property.clear();
//                arrayList.clear();
//                list.clear();
//                for (DataSnapshot ds : dataSnapshot.getChildren())
//                {
//                    final HashMap<String, Object> hashMap = (HashMap<String, Object>) ds.getValue();
//                    if (hashMap.get("Request_Status").equals("NO") && !hashMap.get("End_Date").equals("NULL"))
//                    {
//                        property.add(hashMap);
//                    }
//                }
//                if (property.size() == 0)
//                    progressDialog.cancel();
//                property();
//            }
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
//    }

    private void getList()
    {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("AddResidenceOwner");
        databaseReference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                rvItem.setAdapter(null);
                arrayList.clear();
                list.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    HashMap<String, Object> hashMap1 = (HashMap<String, Object>) ds.getValue();
                    if (hashMap1.get("Status").equals("NO")) //&& property.get(i).get("Property_Id").equals(hashMap1.get("Id")))
                        arrayList.add(hashMap1);
                }
                if (arrayList.size() == 0)
                    progressDialog.cancel();
                array();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {
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
            for (int i = 0; i < arrayList.size(); i++)
            {
                newArrayList.add(arrayList.get(i));
            }

            rvItem.setAdapter(null);
            if (!categoryVar.equals("") || !propertyTypeNo.equals("") || !propertyType.equals("") || !areaVar.equals("") || rentVar != 0)
            {
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

            LinearLayoutManager layoutManager = new LinearLayoutManager(tenant.this);
            itemAdapter = new MyNewAdapter(arrayList, this);
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
            System.out.println("subItemList : " + subItemList);
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
            Picasso.get().load(Uri.parse(subItemList.get(i))).placeholder(R.drawable.common_google_signin_btn_icon_dark_focused).into(subItemViewHolder.imageView);

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
            View v = LayoutInflater.from(mcontext).inflate(R.layout.tenant_cardview,parent,false);
            return new MyViewHolder(v);
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position)
        {
            holder.landlord.setText(""+list.get(position).get("Firstname")+" "+list.get(position).get("Middlename")+" "+list.get(position).get("Lastname"));
            holder.landlord_address.setText(list.get(position).get("Buildingname")+", "+list.get(position).get("Road")+", "+list.get(position).get("Location")
                    +", "+list.get(position).get("City")+"-"+list.get(position).get("Pincode")+", "+list.get(position).get("State"));
            holder.landlord_age.setText(""+ Period.between(LocalDate.parse(""+list.get(position).get("DOB"), DateTimeFormatter.ofPattern("dd/MM/yyyy")), LocalDate.now()).getYears());
            holder.property_address.setText(arrayList.get(position).get("Buildingname")+", "+arrayList.get(position).get("Road")+", "+arrayList.get(position).get("Location")
                    +", "+arrayList.get(position).get("City")+"-"+arrayList.get(position).get("Pincode")+", "+arrayList.get(position).get("State"));
            holder.landlord_mobile.setText(String.valueOf(list.get(position).get("Phone")));
            holder.landlord_occupation.setText(String.valueOf(list.get(position).get("Occupation")));
            holder.property_type.setText(""+arrayList.get(position).get("Category")+", "+arrayList.get(position).get("Property_Typeno")+" "+arrayList.get(position).get("Property_Type"));
            holder.property_area.setText(""+arrayList.get(position).get("Area"));
            holder.property_deposit.setText(""+arrayList.get(position).get("Deposit"));
            holder.property_rent.setText(""+arrayList.get(position).get("Rent") +" "+arrayList.get(position).get("Rent_Period"));

            TextView textView = new TextView(tenant.this);
            textView.setTextColor(Color.BLACK);
            textView.setTextSize(16);

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

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("RequestOwner");

            reference.addValueEventListener(new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot ds : dataSnapshot.getChildren())
                    {
                        HashMap<String,Object> requestOwner = (HashMap<String, Object>) ds.getValue();
                        if (requestOwner.get("Property_Id").equals(arrayList.get(position).get("Id")) && requestOwner.get("Requestor_Id").equals(sessionManager.getUID()))
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
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+arrayList.get(position).get("Phone")));
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
                                request(arrayList.get(position), "1");
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
                                request(arrayList.get(position), "2");
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
            final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("AddResidenceOwner");
            final DatabaseReference databaseReference1 = FirebaseDatabase.getInstance().getReference().child("RequestOwner");
            final SessionManager sessionManager = new SessionManager(mcontext);

            if (key.equals("1"))
            {
                final Calendar c = Calendar.getInstance();
                DatePickerDialog datePickerDialog = new DatePickerDialog(tenant.this, new DatePickerDialog.OnDateSetListener()
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

                        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(tenant.this);
                        final String Start_Date;
                        final TextView DOB = new TextView(tenant.this);
                        DOB.setTextColor(Color.GREEN);
                        DOB.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f);
                        LinearLayout linearLayout = new LinearLayout(tenant.this);
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
                                                requestOwner.put("Property_Id",a.get("Id"));
                                                requestOwner.put("Request_Status","NO");
                                                requestOwner.put("Requestee_Doc_Verified","NO");
                                                requestOwner.put("Requestor_Doc_Verified","NO");
                                                requestOwner.put("StampDuty_Registration","NO");
                                                requestOwner.put("Agreement","NO");
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
                            if (a.get("Id").equals(remove.get("Property_Id")) &&  a.get("AddedBy").equals(remove.get("Requestee_Id")) &&  sessionManager.getUID().equals(remove.get("Requestor_Id")) ) {
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
                        for (int i = 0; i < imageList.size(); i++)
                        {
                            HashMap<String, Object> row = imageList.get(i);

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
                    imageListFilter = (ArrayList<HashMap>) results.values ;
                    notifyDataSetChanged();
                }
            };
        }


        class MyViewHolder extends RecyclerView.ViewHolder
        {
            RecyclerView rvSubItem;
            LinearLayout Clause, Applicance_Furniture;
            TextView line1, line2, clause, applicance_furniture, landlord, landlord_address, landlord_mobile, landlord_age, landlord_occupation, property_address, property_type, property_rent, property_area, property_deposit;
            ImageView call;
            Button request;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);

                rvSubItem = itemView.findViewById(R.id.rv_sub_item);

                landlord = itemView.findViewById(R.id.landlord);
                landlord_address = itemView.findViewById(R.id.landlord_address);
                landlord_mobile = itemView.findViewById(R.id.phone);
                landlord_age = itemView.findViewById(R.id.age);
                landlord_occupation = itemView.findViewById(R.id.occupation);

                applicance_furniture = itemView.findViewById(R.id.applicance_furniture);
                clause = itemView.findViewById(R.id.clauses);
                property_address = itemView.findViewById(R.id.property_address);
                property_type = itemView.findViewById(R.id.type);
                property_area = itemView.findViewById(R.id.area);
                property_deposit = itemView.findViewById(R.id.deposit);
                line1 = itemView.findViewById(R.id.line1);
                line2 = itemView.findViewById(R.id.line2);
                Applicance_Furniture = itemView.findViewById(R.id.Applicance_Furniture);
                Clause = itemView.findViewById(R.id.Clauses);
                property_rent = itemView.findViewById(R.id.rent);

                call = itemView.findViewById(R.id.call);
                request = itemView.findViewById(R.id.request);
            }
        }
    }
}

