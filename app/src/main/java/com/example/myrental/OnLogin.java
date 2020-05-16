package com.example.myrental;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class OnLogin extends AppCompatActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {

    ImageView image;
    Button tenant,owner;
    RadioGroup radioGroup;
    RadioButton radioButton;
    Spinner spinner;
    SessionManager sessionManager;
    ActionBarDrawerToggle actionBarDrawerToggle;
    TextView name;
    ProgressBar progressBar;
    ExpandableListView expandableListView;
    List<String> list;
    int[] images = {R.drawable.ic_person, R.drawable.site, R.drawable.chat, R.drawable.ic_cancel};
    HashMap<String,List<String>> listItem;
    MainAdapter adapter;
    MenuItem m;

    StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl("gs://my-rental-4ff8d.appspot.com/").child("Profile_Pics");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_login);

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
        initListData();
//        images[0] = (R.drawable.ic_person);
//        images[1] = (R.drawable.site);
//        images[2] = (R.drawable.chat);
//        images[3] = (R.drawable.cancel);

        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                Toast.makeText(OnLogin.this, ""+groupPosition+" "+childPosition, Toast.LENGTH_LONG).show();
                return false;
            }
        });

        expandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                if (groupPosition == 0) {
                    if (actionBarDrawerToggle.onOptionsItemSelected(m)) {
                        // Toast.makeText(OnLogin.this, "clicked"+item.getItemId(), Toast.LENGTH_LONG).show();
                        return true;
                    }
                }
                return false;
            }
        });

        name = findViewById(R.id.name);
        radioGroup = findViewById(R.id.radio);
        spinner = findViewById(R.id.spinner);
        tenant = findViewById(R.id.tenant);
        owner = findViewById(R.id.owner);

        NavigationView navigationView = findViewById(R.id.navigation);
        View header = navigationView.getHeaderView(0);
        TextView panel = header.findViewById(R.id.name);
        image = header.findViewById(R.id.image);


        sessionManager = new SessionManager(getApplicationContext());
        storageReference = storageReference.child(sessionManager.getEmail());
        panel.setText(sessionManager.getFirstname()+" "+sessionManager.getMiddlename()+" "+sessionManager.getLastname());

        try {
            final File file = File.createTempFile("video","mp4");
            storageReference.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                    image.setImageBitmap(bitmap);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });
        }
        catch (IOException e)
        {
            Toast.makeText(OnLogin.this, "yygyufu", Toast.LENGTH_LONG).show();
        }

        spinner.setVisibility(View.GONE);
        tenant.setOnClickListener(this);
        owner.setOnClickListener(this);
    }

            @Override
            public void onClick(View v)
            {

                int id = radioGroup.getCheckedRadioButtonId();
                radioButton = findViewById(id);

                if(v.getId() == R.id.tenant)
                {
                    if((radioButton.getText()).equals("Vehicle"))
                    {
                       Intent intent = new Intent(OnLogin.this,tenant.class);
                       intent.putExtra("category","Vehicle");
                       startActivity(intent);
                    }
                    else if((radioButton.getText()).equals("Residence"))
                    {
                       Intent intent = new Intent(OnLogin.this,com.example.myrental.tenant.class);
                        intent.putExtra("category","Residence");
                       startActivity(intent);
                    }
                }
                else if(v.getId() == R.id.owner)
                {
                    if((radioButton.getText()).equals("Vehicle"))
                    {
                        Intent intent = new Intent(OnLogin.this, landlord.class);
                        intent.putExtra("category","Vehicle");
                        startActivity(intent);
                    }
                    else if((radioButton.getText()).equals("Residence"))
                    {
                        Intent intent = new Intent(OnLogin.this, com.example.myrental.landlord.class);
                        intent.putExtra("category","Residence");
                        startActivity(intent);
                    }
                }
            }


    private void initListData() {
        list.add(getString(R.string.group1));
        list.add(getString(R.string.group2));
        list.add(getString(R.string.group3));
        list.add(getString(R.string.group4));

        String[] array;

        List<String> list1 = new ArrayList<>();
        array = getResources().getStringArray(R.array.group1);
        for (String item : array)
        {
            list1.add(item);
        }

        List<String> list2 = new ArrayList<>();
        array = getResources().getStringArray(R.array.group2);
        for (String item : array)
        {
            list2.add(item);
        }

        List<String> list3 = new ArrayList<>();
        array = getResources().getStringArray(R.array.group3);
        for (String item : array)
        {
            list3.add(item);
        }

        List<String> list4 = new ArrayList<>();
        array = getResources().getStringArray(R.array.group4);
        for (String item : array)
        {
            list4.add(item);
        }

        listItem.put(list.get(0), list1);
        listItem.put(list.get(1), list2);
        listItem.put(list.get(2), list3);
        listItem.put(list.get(3), list4);
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        NavigationView n = findViewById(R.id.navigation);
        if(n != null)
        {
            n.setNavigationItemSelectedListener(this);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        m = item;
        if(actionBarDrawerToggle.onOptionsItemSelected(item))
        {
            Toast.makeText(OnLogin.this, "clicked"+item.getItemId(), Toast.LENGTH_LONG).show();
            return true;
        }
        return onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        switch (id)
        {
            case R.id.nav_profile:
                Toast.makeText(OnLogin.this, "Profile", Toast.LENGTH_SHORT).show();
//                Intent intent1 = new Intent(OnLogin.this, com.example.myrental.CreateAccount.class);
//                intent1.putExtra("Key", "1");
//                startActivity(intent1);
                return true;

            case R.id.nav_sites:
                Intent intent2 = new Intent(OnLogin.this, com.example.myrental.MainActivity.class);
                startActivity(intent2);
                return true;

            case R.id.nav_chats:
                Toast.makeText(OnLogin.this, "Settings", Toast.LENGTH_SHORT).show();
                return true;

            case R.id.nav_logout:
                sessionManager.setLogin(false);
                Intent intent4 = new Intent(OnLogin.this, com.example.myrental.MainActivity.class);
                startActivity(intent4);
                return true;
        }
        return true;
    }

    public class MainAdapter extends BaseExpandableListAdapter {

        Context context;
        List<String> listGroup;
        HashMap<String,List<String>> listItem;

        public MainAdapter(Context context, List<String> listGroup, HashMap<String, List<String>> listItem) {
            this.context = context;
            this.listGroup = listGroup;
            this.listItem = listItem;
        }

        @Override
        public int getGroupCount() {
            return listGroup.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return listItem.get(this.listGroup.get(groupPosition)).size();
        }

        @Override
        public Object getGroup(int groupPosition) {
            return this.listGroup.get(groupPosition);
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return listItem.get(this.listGroup.get(groupPosition)).get(childPosition);
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
