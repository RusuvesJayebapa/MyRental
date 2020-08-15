package com.example.myrental;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class OnLogin extends AppCompatActivity implements View.OnClickListener {

    ImageView image;
    LinearLayout AddResidence, BookResidence, AddVehicle, BookVehicle;
    SessionManager sessionManager;
    ActionBarDrawerToggle actionBarDrawerToggle;
    TextView name;
    ExpandableListView expandableListView;
    List<String> list;
    int[] images = {R.drawable.ic_person, R.drawable.site, R.drawable.rented_site, R.drawable.car, R.drawable.rented_car, R.drawable.ic_cancel};
    HashMap<String,List<String>> listItem;
    MainAdapter adapter;

    StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl("gs://my-rental-4ff8d.appspot.com/Profile_Pics/");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_login);

        AddResidence = findViewById(R.id.AddResidence);
        BookResidence = findViewById(R.id.BookResidence);
        AddVehicle = findViewById(R.id.AddVehicle);
        BookVehicle = findViewById(R.id.BookVehicle);

        DrawerLayout drawerLayout = findViewById(R.id.drawer);
         actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);

        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        expandableListView = findViewById(R.id.expandable_listview);
        list = new ArrayList<>();
        listItem = new HashMap<>();
        adapter = new MainAdapter(this,list,listItem);
        expandableListView.setAdapter(adapter);
        expandableListView.setDivider(null);
        expandableListView.setChildDivider(null);
        initListData();

        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                if (groupPosition == 1 && childPosition == 0)
                {
                    startActivity(new Intent(OnLogin.this, com.example.myrental.InProgressAgreement.class));
                }
                if (groupPosition == 1 && childPosition == 1)
                {
                    startActivity(new Intent(OnLogin.this, com.example.myrental.MyResidence.class));
                }
                if (groupPosition == 2 && childPosition == 0)
                {
                    startActivity(new Intent(OnLogin.this, com.example.myrental.TenantRequests.class));
                }
                if (groupPosition == 2 && childPosition == 1)
                {
                    startActivity(new Intent(OnLogin.this, com.example.myrental.TenantAgreementPage.class));
                }
                return false;
            }
        });

        expandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                if (groupPosition == 0)
                {
                    startActivity(new Intent(OnLogin.this, com.example.myrental.ProfileUpdate.class));
                }
                if (groupPosition == 3)
                {
                    startActivity(new Intent(OnLogin.this, com.example.myrental.MyVehicle.class));
                }
                if (groupPosition == 4)
                {
                    startActivity(new Intent(OnLogin.this, com.example.myrental.VehicleRequested.class));
                }
                if (groupPosition == 5)
                {
                    sessionManager.setLogin(false);
                    startActivity(new Intent(OnLogin.this, com.example.myrental.MainActivity.class));
                }
                return false;
            }
        });

        name = findViewById(R.id.name);

        NavigationView navigationView = findViewById(R.id.navigation);
        View header = navigationView.getHeaderView(0);
        TextView panel = header.findViewById(R.id.name);
        image = header.findViewById(R.id.image);

        sessionManager = new SessionManager(getApplicationContext());
        storageReference = storageReference.child(sessionManager.getUID());
        panel.setText(sessionManager.getFirstname()+" "+sessionManager.getMiddlename()+" "+sessionManager.getLastname());
        panel.setTextColor(Color.BLACK);


        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
        {
            @Override
            public void onSuccess(Uri uri)
            {
                Picasso.get().load(uri).placeholder(R.drawable.loading).into(image);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) { System.out.println("Image "+e);}
        });

        AddResidence.setOnClickListener(this);
        BookResidence.setOnClickListener(this);
        AddVehicle.setOnClickListener(this);
        BookVehicle.setOnClickListener(this);
    }

    private void initListData() {
        list.add(getString(R.string.Profile));
        list.add(getString(R.string.MyResidence));
        list.add(getString(R.string.ResidenceAgreement));
        list.add(getString(R.string.MyVehicle));
        list.add(getString(R.string.VehicleAgreement));
        list.add(getString(R.string.LogOut));

        String[] array;

        List<String> list1 = new ArrayList<>();
        array = getResources().getStringArray(R.array.MyResidence);
        for (String item : array)
        {
            list1.add(item);
        }

        List<String> list2 = new ArrayList<>();
        array = getResources().getStringArray(R.array.ResidenceAgreement);
        for (String item : array)
        {
            list2.add(item);
        }

        List<String> list3 = new ArrayList<>();
        array = getResources().getStringArray(R.array.MyVehicle);
        for (String item : array)
        {
            list3.add(item);
        }

        listItem.put(list.get(0), null);
        listItem.put(list.get(1), list1);
        listItem.put(list.get(2), list2);
        listItem.put(list.get(3), null);
        listItem.put(list.get(4), null);
        listItem.put(list.get(5), null);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v)
    {
        if(v.getId() == R.id.AddResidence)
        {
            Intent intent = new Intent(OnLogin.this,com.example.myrental.landlord.class);
            startActivity(intent);
        }
        else if(v.getId() == R.id.BookResidence)
        {
            Intent intent = new Intent(OnLogin.this, com.example.myrental.tenant.class);
            startActivity(intent);
        }
        else if(v.getId() == R.id.AddVehicle)
        {
            Intent intent = new Intent(OnLogin.this, com.example.myrental.VehicleDetails.class);
            intent.putExtra("list", new HashMap<>());
            startActivity(intent);
        }
        else if(v.getId() == R.id.BookVehicle)
        {
            Intent intent = new Intent(OnLogin.this, com.example.myrental.VehicleTenant.class);
            intent.putExtra("category","Residence");
            startActivity(intent);
        }
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        NavigationView n = findViewById(R.id.navigation);
//        if(n != null)
//        {
////            n.setNavigationItemSelectedListener(this);
//        }
//        return super.onCreateOptionsMenu(menu);
//    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(actionBarDrawerToggle.onOptionsItemSelected(item))
        {
            return true;
        }
        return onOptionsItemSelected(item);
    }

//    @Override
//    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//        int id = item.getItemId();
//
//        switch (id)
//        {
//            case R.id.nav_profile:
//                Toast.makeText(OnLogin.this, "Profile", Toast.LENGTH_SHORT).show();
//                Intent intent1 = new Intent(OnLogin.this, com.example.myrental.ProfileUpdate.class);
//                startActivity(intent1);
//                return true;
//
//            case R.id.nav_sites:
//                Intent intent2 = new Intent(OnLogin.this, com.example.myrental.MainActivity.class);
//                startActivity(intent2);
//                return true;
//
//            case R.id.nav_chats:
//                Toast.makeText(OnLogin.this, "Settings", Toast.LENGTH_SHORT).show();
//                return true;
//
//            case R.id.nav_logout:
//                sessionManager.setLogin(false);
//                Intent intent4 = new Intent(OnLogin.this, com.example.myrental.MainActivity.class);
//                startActivity(intent4);
//                return true;
//        }
//        return true;
//    }

    public class MainAdapter extends BaseExpandableListAdapter {

        Context context;
        List<String> listGroup;
        HashMap<String,List<String>> listItem;

        public MainAdapter(Context context, List<String> listGroup, HashMap<String, List<String>> listItem)
        {
            this.context = context;
            this.listGroup = listGroup;
            this.listItem = listItem;
        }

        @Override
        public int getGroupCount() {
            return listGroup.size();
        }

        @Override
        public int getChildrenCount(int groupPosition)
        {
            if (groupPosition == 1)
            {
                return listItem.get(this.listGroup.get(groupPosition)).size();
            }
            else if (groupPosition == 2)
            {
                return listItem.get(this.listGroup.get(groupPosition)).size();
            }
            else
            {
                return 0;
            }
        }

        @Override
        public Object getGroup(int groupPosition) {
            return this.listGroup.get(groupPosition);
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            if (groupPosition == 1) {
                return listItem.get(this.listGroup.get(groupPosition)).get(childPosition);
            }
            else  if (groupPosition == 2) {
                return listItem.get(this.listGroup.get(groupPosition)).get(childPosition);
            }
            else
            {
                return 0;
            }
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            String group = (String) getGroup(groupPosition);
            if (convertView == null) {
                LayoutInflater layoutInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = layoutInflater.inflate(R.layout.list_group, null);
            }
            TextView textView = convertView.findViewById(R.id.list_parent);
            textView.setText(group);
            ImageView imageView = convertView.findViewById(R.id.list_image);
            imageView.setImageResource(images[groupPosition]);
            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            String child = (String)getChild(groupPosition, childPosition);
            if (convertView == null)
            {
                LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = layoutInflater.inflate(R.layout.list_item, null);
            }
            TextView textView = convertView.findViewById(R.id.list_child);
            textView.setText(child);
            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }
}
