package com.example.myrental;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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

public class InProgressAgreement extends AppCompatActivity {

    ArrayList<HashMap> arrayList = new ArrayList<>();
    ArrayList<HashMap> newArrayList = new ArrayList<>();
    HashMap<String,Object> hashMap = new HashMap<>();
    ArrayList<HashMap> landlord = new ArrayList<>();
    ArrayList<HashMap> tenant = new ArrayList<>();
    ArrayList<HashMap> request = new ArrayList<>();
    DatabaseReference databaseReference, requestOwner, registrationDatabase;
    SessionManager sessionManager;
    ProgressDialog progressDialog;
    RecyclerView rvItem;
    String day, mon;
    SharedPreferences sharedPreferences;
    MyNewAdapter itemAdapter;
    int max = 0, rentVar;
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
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

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
        final ArrayAdapter<String> categoryAdapter = new ArrayAdapter<String>(InProgressAgreement.this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.category));
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
        final ArrayAdapter<String> propertyTypeAdapter = new ArrayAdapter<String>(InProgressAgreement.this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.property_type));
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
        requestOwner = FirebaseDatabase.getInstance().getReference().child("RequestOwner");
        request.clear();
        requestOwner.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                rvItem.setAdapter(null);
                arrayList.clear();
                request.clear();
                landlord.clear();
                tenant.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren())
                {
                    if(((HashMap<String, Object>)ds.getValue()).get("Requestee_Id").equals(sessionManager.getUID()) && ((HashMap<String, Object>)ds.getValue()).get("Request_Status").equals("YES"))
                    {
                        request.add((HashMap<String, Object>)ds.getValue());
                    }
                }
                if (request.size() == 0)
                    progressDialog.cancel();
                else
                    requestMethod();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void requestMethod()
    {
        databaseReference = FirebaseDatabase.getInstance().getReference().child("AddResidenceOwner");
        databaseReference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                arrayList.clear();
                landlord.clear();
                tenant.clear();
                for (int i = 0; i < request.size(); i++)
                {
                    for (DataSnapshot ds : dataSnapshot.getChildren())
                    {
                        hashMap = (HashMap<String, Object>) ds.getValue();
                        if (request.get(i).get("Property_Id").equals(hashMap.get("Id")))
                        {
                            if (Integer.valueOf(hashMap.get("Rent").toString()) > max)
                                max = Integer.valueOf(hashMap.get("Rent").toString());
                            arrayList.add((HashMap<String, Object>) ds.getValue());
                        }
                    }
                }
                rentVar = max;
                if (request.size() == 0)
                    progressDialog.cancel();
                else
                    landlordDetails();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    private void landlordDetails()
    {
        registrationDatabase = FirebaseDatabase.getInstance().getReference().child("RegistrationDatabase");
        registrationDatabase.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                landlord.clear();
                tenant.clear();
                for (int i = 0; i < request.size(); i++)
                {
                    for (DataSnapshot ds : dataSnapshot.getChildren())
                    {
                        final HashMap<String, Object> hashMap = (HashMap<String, Object>) ds.getValue();
                        if (request.get(i).get("Requestee_Id").equals(hashMap.get("UID")))
                        {
                            landlord.add(hashMap);
                        }
                    }
                }
                if (request.size() == 0)
                    progressDialog.cancel();
                else
                    tenantDetails();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void tenantDetails()
    {
        registrationDatabase = FirebaseDatabase.getInstance().getReference().child("RegistrationDatabase");
        registrationDatabase.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                tenant.clear();
                for (int i = 0; i < request.size(); i++)
                {
                    for (DataSnapshot ds : dataSnapshot.getChildren())
                    {
                        final HashMap<String, Object> hashMap = (HashMap<String, Object>) ds.getValue();
                        if (request.get(i).get("Requestor_Id").equals(hashMap.get("UID")))
                        {
                            tenant.add(hashMap);
                        }
                    }
                }
                if (request.size() == 0)
                    progressDialog.cancel();
                else
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

            LinearLayoutManager layoutManager = new LinearLayoutManager(InProgressAgreement.this);
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
            progressDialog.cancel();
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
            this.mcontext = context;
            this.imageList = imageList;
            this.imageListFilter = imageList;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
        {
            View v = LayoutInflater.from(mcontext).inflate(R.layout.in_progress_agreement_cardview,parent,false);
            return new MyViewHolder(v);
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position)
        {
            holder.landlord.setText(""+landlord.get(position).get("Firstname")+" "+landlord.get(position).get("Middlename")+" "+landlord.get(position).get("Lastname"));
            holder.landlord_address.setText(landlord.get(position).get("Buildingname")+", "+landlord.get(position).get("Road")+", "+landlord.get(position).get("Location")
                    +", "+landlord.get(position).get("City")+"-"+landlord.get(position).get("Pincode")+", "+landlord.get(position).get("State"));
            holder.landlord_age.setText(""+ Period.between(LocalDate.parse(""+landlord.get(position).get("DOB"), DateTimeFormatter.ofPattern("dd/MM/yyyy")), LocalDate.now()).getYears());
            holder.landlord_mobile.setText(String.valueOf(landlord.get(position).get("Phone")));
            holder.landlord_occupation.setText(String.valueOf(landlord.get(position).get("Occupation")));

            holder.property_address.setText(imageListFilter.get(position).get("Buildingname")+", "+imageListFilter.get(position).get("Road")+", "+imageListFilter.get(position).get("Location")
                    +", "+imageListFilter.get(position).get("City")+"-"+imageListFilter.get(position).get("Pincode")+", "+imageListFilter.get(position).get("State"));
            holder.property_type.setText(""+imageListFilter.get(position).get("Category")+", "+imageListFilter.get(position).get("Property_Typeno")+" "+imageListFilter.get(position).get("Property_Type"));
            holder.property_area.setText(""+imageListFilter.get(position).get("Area"));
            holder.property_deposit.setText(""+imageListFilter.get(position).get("Deposit"));
            holder.property_rent.setText(""+imageListFilter.get(position).get("Rent") +" "+imageListFilter.get(position).get("Rent_Period"));

            holder.tenant.setText(""+tenant.get(position).get("Firstname")+" "+tenant.get(position).get("Middlename")+" "+tenant.get(position).get("Lastname"));
            holder.tenant_address.setText(tenant.get(position).get("Buildingname")+", "+tenant.get(position).get("Road")+", "+tenant.get(position).get("Location")
                    +", "+tenant.get(position).get("City")+"-"+tenant.get(position).get("Pincode")+", "+tenant.get(position).get("State"));
            holder.tenant_age.setText(""+ Period.between(LocalDate.parse(""+tenant.get(position).get("DOB"), DateTimeFormatter.ofPattern("dd/MM/yyyy")), LocalDate.now()).getYears());
            holder.tenant_mobile.setText(String.valueOf(tenant.get(position).get("Phone")));
            holder.tenant_occupation.setText(String.valueOf(tenant.get(position).get("Occupation")));

            TextView textView = new TextView(InProgressAgreement.this);
            textView.setTextColor(Color.BLACK);
            textView.setTextSize(16);
            if (imageListFilter.get(position).containsKey("Appliances"))
            {
                for (int i = 0; i < ((HashMap<String, Integer>) imageListFilter.get(position).get("Appliances")).keySet().size(); i++)
                    textView.append("" + ((HashMap<String, Integer>) imageListFilter.get(position).get("Appliances")).keySet().toArray()[i] + " = " + ((HashMap<String, Integer>) imageListFilter.get(position).get("Appliances")).values().toArray()[i] + "\n");
                holder.property_appliances.addView(textView);

                holder.property_appliances.removeAllViews();
            }
            else
            {
                holder.Property_Appliances.setVisibility(View.GONE);
            }

            if (imageListFilter.get(position).containsKey("Clause"))
            {
                for (int i = 0; i < ((ArrayList<String>) imageListFilter.get(position).get("Clause")).size(); i++)
                    textView.append("\n" + ((ArrayList<String>) imageListFilter.get(position).get("Clause")).get(i));
                holder.property_clauses.addView(textView);
            }
            else
            {
                holder.Property_Clauses.setVisibility(View.GONE);
            }

            holder.landord_details_image.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if (holder.landord_details.getVisibility() == View.GONE)
                    {
                        holder.landord_details.setVisibility(View.VISIBLE);
                        holder.landord_details_image.setImageResource(R.drawable.arrow_up);
                        holder.tenant_details.setVisibility(View.GONE);
                        holder.status.setVisibility(View.GONE);
                        if (holder.Tenant_Details.getVisibility() == View.GONE)
                            holder.view.setVisibility(View.VISIBLE);
                        else
                            holder.view.setVisibility(View.GONE);
                    }
                    else
                    {
                        holder.landord_details.setVisibility(View.GONE);
                        holder.view.setVisibility(View.GONE);
                        holder.landord_details_image.setImageResource(R.drawable.down_arrow);
                    }
                }
            });

            holder.tenant_details_image.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if (holder.tenant_details.getVisibility() == View.GONE)
                    {
                        holder.tenant_details.setVisibility(View.VISIBLE);
                        holder.tenant_details_image.setImageResource(R.drawable.arrow_up);
                        holder.landord_details.setVisibility(View.GONE);
                        holder.status.setVisibility(View.GONE);
                        holder.view.setVisibility(View.GONE);
                    }
                    else
                    {
                        holder.tenant_details.setVisibility(View.GONE);
                        holder.tenant_details_image.setImageResource(R.drawable.down_arrow);
                    }
                }
            });

            holder.status_image.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if (holder.status.getVisibility() == View.GONE)
                    {
                        holder.status.setVisibility(View.VISIBLE);
                        holder.view.setVisibility(View.VISIBLE);
                        holder.status_image.setImageResource(R.drawable.arrow_up);
                        holder.tenant_details.setVisibility(View.GONE);
                        holder.landord_details.setVisibility(View.GONE);
                    }
                    else
                    {
                        holder.status.setVisibility(View.GONE);
                        holder.view.setVisibility(View.GONE);
                        holder.status_image.setImageResource(R.drawable.down_arrow);
                    }
                }
            });

            if (request.get(position).get("Requestee_Doc_Verified").equals("YES"))
                holder.status1.setImageResource(R.drawable.right);
            else
                holder.status1.setImageResource(R.drawable.wrong);

            if (request.get(position).get("Requestor_Doc_Verified").equals("YES"))
                holder.status2.setImageResource(R.drawable.right);
            else
                holder.status2.setImageResource(R.drawable.wrong);

            if (request.get(position).get("StampDuty_Registration").equals("YES"))
                holder.status3.setImageResource(R.drawable.right);
            else
                holder.status3.setImageResource(R.drawable.wrong);

            if (request.get(position).get("Agreement").equals("YES"))
                holder.status4.setImageResource(R.drawable.right);
            else
                holder.status4.setImageResource(R.drawable.wrong);

            if (!request.get(position).get("End_Date").toString().equals("NULL"))
            {
                if (holder.Tenant_Details.getVisibility() == View.GONE)
                    holder.AgreementStatus.setText("Agreement Completed\n(Offline Agreement)");
                else
                    holder.AgreementStatus.setText("Agreement Completed");
                holder.AgreementStatus.setTextColor(Color.RED);
                holder.AgreementDate.setText("Start Date : " + request.get(position).get("Start_Date") + "  End Date : " + request.get(position).get("End_Date"));
                holder.AgreementDate.setTextColor(Color.RED);
                holder.cancelled.setVisibility(View.GONE);
                holder.completed.setVisibility(View.GONE);
            }
            else
                holder.AgreementDate.setText("Start Date : " + request.get(position).get("Start_Date"));

            holder.cancelled.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    final DatabaseReference deleteNode = FirebaseDatabase.getInstance().getReference().child("RequestOwner");
                    final DatabaseReference updateNode = FirebaseDatabase.getInstance().getReference().child("AddResidenceOwner");

                    deleteNode.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                        {
                            for (DataSnapshot ds : dataSnapshot.getChildren())
                            {
                                HashMap<String, Object> hashMap = (HashMap<String, Object>) ds.getValue();
                                if(hashMap.get("Property_Id").toString().equals(imageListFilter.get(position).get("Id")) ||
                                   hashMap.get("Requestee_Id").toString().equals(landlord.get(position).get("UID")) ||
                                   hashMap.get("Requestor_Id").toString().equals(tenant.get(position).get("UID")) ||
                                   hashMap.get("Requestor_Doc_Verified").toString().equals(request.get(position).get("Requestor_Doc_Verified")) ||
                                   hashMap.get("Requestee_Doc_Verified").toString().equals(request.get(position).get("Requestee_Doc_Verified")) ||
                                   hashMap.get("StampDuty_Registration").toString().equals(request.get(position).get("StampDuty_Registration")) ||
                                   hashMap.get("Agreement").toString().equals(request.get(position).get("Agreement")))
                                {
                                    updateNode.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                        {
                                            for (DataSnapshot ds : dataSnapshot.getChildren())
                                            {
                                                HashMap<String, Object> hashMap = (HashMap<String, Object>) ds.getValue();
                                                if (hashMap.get("Id").toString().equals(request.get(position).get("Property_Id")))
                                                {
                                                    hashMap.put("Status", "NO");
                                                    updateNode.child(ds.getKey()).setValue(hashMap);
                                                    break;
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                    deleteNode.child(ds.getKey()).removeValue();
                                    break;
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            });

            holder.completed.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    final Calendar c = Calendar.getInstance();
                    DatePickerDialog datePickerDialog = new DatePickerDialog(InProgressAgreement.this, new DatePickerDialog.OnDateSetListener()
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

                    AlertDialog.Builder builder = new AlertDialog.Builder(InProgressAgreement.this);
                    final String End_Date;
                    final TextView DOB = new TextView(InProgressAgreement.this);
                    DOB.setTextColor(Color.GREEN);
                    DOB.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f);
                    LinearLayout linearLayout = new LinearLayout(InProgressAgreement.this);
                    linearLayout.setGravity(Gravity.CENTER);
                    linearLayout.addView(DOB);

                    End_Date = day+"/"+mon+"/"+year;
                    DOB.setText("The End Date You Have Selected Is " + day+"/"+mon+"/"+year + "\nDo You Want To Continue?");
                    builder.setView(linearLayout);
                    builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            final DatabaseReference updateNode = FirebaseDatabase.getInstance().getReference().child("RequestOwner");
                            updateNode.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                {
                                    for (DataSnapshot ds : dataSnapshot.getChildren())
                                    {
                                        HashMap<String, Object> hashMap = (HashMap<String, Object>) ds.getValue();
                                        if (hashMap.get("Start_Date").toString().equals(request.get(position).get("Start_Date")) ||
                                            hashMap.get("Property_Id").toString().equals(request.get(position).get("Property_Id")))
                                        {
                                            hashMap.put("End_Date", End_Date);
                                            updateNode.child(ds.getKey()).setValue(hashMap);

                                            final DatabaseReference updatePropertyNode = FirebaseDatabase.getInstance().getReference().child("AddResidenceOwner");
                                            updatePropertyNode.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                                {
                                                    for (DataSnapshot ds : dataSnapshot.getChildren())
                                                    {
                                                        HashMap<String, Object> hashMap = (HashMap<String, Object>) ds.getValue();
                                                        if (hashMap.get("Id").toString().equals(imageListFilter.get(position).get("Id"))) {
                                                            hashMap.put("Status", "NO");
                                                            updatePropertyNode.child(ds.getKey()).setValue(hashMap);
                                                            break;
                                                        }
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                                }
                                            });
                                            break;
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
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
            });

            if (request.get(position).get("Requestee_Id").toString().equals(request.get(position).get("Requestor_Id").toString()))
            {
                holder.AgreementStatus.append("\n(Offline Agreement)");
                holder.Tenant_Details.setVisibility(View.GONE);
                holder.Status.setVisibility(View.GONE);
            }

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
                                    row.get("Rent_Period").toString().toLowerCase().contains(charcater.toLowerCase()) ||
                                    landlord.get(i).get("Firstname").toString().equals(charcater.toLowerCase()) ||
                                    landlord.get(i).get("Middlename").toString().equals(charcater.toLowerCase()) ||
                                    landlord.get(i).get("Lastname").toString().equals(charcater.toLowerCase()) ||
                                    landlord.get(i).get("Buildingname").toString().equals(charcater.toLowerCase()) ||
                                    landlord.get(i).get("Road").toString().equals(charcater.toLowerCase()) ||
                                    landlord.get(i).get("Location").toString().equals(charcater.toLowerCase()) ||
                                    landlord.get(i).get("City").toString().equals(charcater.toLowerCase()) ||
                                    landlord.get(i).get("Pincode").toString().equals(charcater.toLowerCase()) ||
                                    landlord.get(i).get("State").toString().equals(charcater.toLowerCase()) ||
                                    landlord.get(i).get("DOB").toString().equals(charcater.toLowerCase()) ||
                                    landlord.get(i).get("Phone").toString().equals(charcater.toLowerCase()) ||
                                    landlord.get(i).get("Occupation").toString().equals(charcater.toLowerCase()) ||
                                    tenant.get(i).get("Firstname").toString().equals(charcater.toLowerCase()) ||
                                    tenant.get(i).get("Middlename").toString().equals(charcater.toLowerCase()) ||
                                    tenant.get(i).get("Lastname").toString().equals(charcater.toLowerCase()) ||
                                    tenant.get(i).get("Buildingname").toString().equals(charcater.toLowerCase()) ||
                                    tenant.get(i).get("Road").toString().equals(charcater.toLowerCase()) ||
                                    tenant.get(i).get("Location").toString().equals(charcater.toLowerCase()) ||
                                    tenant.get(i).get("City").toString().equals(charcater.toLowerCase()) ||
                                    tenant.get(i).get("Pincode").toString().equals(charcater.toLowerCase()) ||
                                    tenant.get(i).get("State").toString().equals(charcater.toLowerCase()) ||
                                    tenant.get(i).get("DOB").toString().equals(charcater.toLowerCase()) ||
                                    tenant.get(i).get("Phone").toString().equals(charcater.toLowerCase()) ||
                                    tenant.get(i).get("Occupation").toString().equals(charcater.toLowerCase()))
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
            View view;
            TextView Property_Clauses, Property_Appliances, landlord, landlord_address, landlord_mobile, landlord_age, landlord_occupation,
                    property_address, property_type, property_rent, property_area, property_deposit, tenant, tenant_address,
                    tenant_mobile, tenant_age, tenant_occupation, AgreementStatus, AgreementDate;
            ImageView landord_details_image, tenant_details_image, status_image, status1, status2, status3, status4;
            RecyclerView rvSubItem;
            ImageView imageView;
            LinearLayout property_clauses, property_appliances, landord_details, tenant_details, status, Status, Tenant_Details;
            ImageView landlord_call, tenant_call;
            Button cancelled, completed;

            public MyViewHolder(@NonNull View itemView)
            {
                super(itemView);

                rvSubItem = itemView.findViewById(R.id.rv_sub_item);
                imageView = itemView.findViewById(R.id.imageView);

                landlord = itemView.findViewById(R.id.landlord);
                landlord_address = itemView.findViewById(R.id.landlord_address);
                landlord_mobile = itemView.findViewById(R.id.phone);
                landlord_call = itemView.findViewById(R.id.call);
                landlord_age = itemView.findViewById(R.id.age);
                landlord_occupation = itemView.findViewById(R.id.occupation);

                tenant = itemView.findViewById(R.id.tenant);
                tenant_address = itemView.findViewById(R.id.tenant_address);
                tenant_mobile = itemView.findViewById(R.id.tenant_phone);
                tenant_call = itemView.findViewById(R.id.call);
                tenant_age = itemView.findViewById(R.id.tenant_age);
                tenant_occupation = itemView.findViewById(R.id.tenant_occupation);

                Property_Appliances = itemView.findViewById(R.id.applicance_furniture);
                Property_Clauses = itemView.findViewById(R.id.clauses);
                property_address = itemView.findViewById(R.id.property_address);
                property_type = itemView.findViewById(R.id.type);
                property_area = itemView.findViewById(R.id.area);
                property_deposit = itemView.findViewById(R.id.deposit);
                property_appliances = itemView.findViewById(R.id.Applicance_Furniture);
                property_clauses = itemView.findViewById(R.id.Clauses);
                property_rent = itemView.findViewById(R.id.rent);

                landord_details = itemView.findViewById(R.id.landord_details);
                tenant_details = itemView.findViewById(R.id.tenant_details);
                status = itemView.findViewById(R.id.status);

                Tenant_Details = itemView.findViewById(R.id.Tenant_Details);
                Status = itemView.findViewById(R.id.Status);

                landord_details_image = itemView.findViewById(R.id.landord_details_image);
                tenant_details_image = itemView.findViewById(R.id.tenant_details_image);
                status_image = itemView.findViewById(R.id.statusimage);

                status1 = itemView.findViewById(R.id.status1);
                status2 = itemView.findViewById(R.id.status2);
                status3 = itemView.findViewById(R.id.status3);
                status4 = itemView.findViewById(R.id.status4);

                cancelled = itemView.findViewById(R.id.cancelled);
                completed = itemView.findViewById(R.id.completed);

                AgreementStatus = itemView.findViewById(R.id.AgreementStatus);
                AgreementDate = itemView.findViewById(R.id.AgreementDate);

                view = itemView.findViewById(R.id.view);
            }
        }
    }
}

