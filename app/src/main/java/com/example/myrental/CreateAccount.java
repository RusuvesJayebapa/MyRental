package com.example.myrental;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Calendar;
import java.util.HashMap;

public class CreateAccount extends AppCompatActivity {
    TextView DOB;
    ImageView Profile;
    EditText Firstname, Middlename, Lastname, Occupation, Email, Phone, Buildingname, Flatno, Floorno, Road, Location, City, Pincode, District, State, UID, Password;
    Button button, profile, uid;
    Spinner Gender;
    DatabaseReference reff;
    HashMap<String,Object> hashMap = new HashMap<>();
    long flag=0;
    String day, mon;
    Uri uri;
    SessionManager sessionManager;
    DatePickerDialog datePickerDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        Firstname = findViewById(R.id.firstname);
        Middlename = findViewById(R.id.middlename);
        Lastname = findViewById(R.id.lastname);
        DOB = findViewById(R.id.dob);
        Gender = findViewById(R.id.gender);
        Occupation = findViewById(R.id.occupation);
        Email = findViewById(R.id.email);
        Phone = findViewById(R.id.phone);
        Buildingname = findViewById(R.id.buildingname);
        Flatno = findViewById(R.id.flatno);
        Floorno = findViewById(R.id.floorno);
        Road = findViewById(R.id.road);
        Location = findViewById(R.id.location);
        City = findViewById(R.id.place);
        Pincode = findViewById(R.id.pincode);
        District = findViewById(R.id.district);
        State = findViewById(R.id.state);
        UID = findViewById(R.id.uid);
        uid = findViewById(R.id.uid_file);
        Password = findViewById(R.id.password);
        Profile = findViewById(R.id.Profile_Pic);
        profile = findViewById(R.id.profile_Pic);
        button = findViewById(R.id.submit);

        sessionManager = new SessionManager(getApplicationContext());

        reff = FirebaseDatabase.getInstance().getReference().child("RegistrationDatabase");

        DOB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();


                datePickerDialog = new DatePickerDialog(CreateAccount.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        if (dayOfMonth<10)
                            day = "0"+dayOfMonth;
                        if (month+1<10)
                            mon = "0"+(month+1);
                        DOB.setText(day+"/"+mon+"/"+year);
                    }
                }, c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.MONTH), c.get(Calendar.YEAR));
                datePickerDialog.show();
            }
        });

        ArrayAdapter<String> gender = new ArrayAdapter<String>(CreateAccount.this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.gender));
        gender.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Gender.setAdapter(gender);

//        if (getIntent().hasExtra("Key"))
//        {
//            Firstname.setText(sessionManager.getFirstname());
//            Middlename.setText(sessionManager.getMiddlename());
//            Lastname.setText(sessionManager.getLastname());
//            DOB.setText(sessionManager.getDOB());
//            Gender.setSelection(gender.getPosition(sessionManager.getFirstname()));
//            Email.setText(sessionManager.getEmail());
//            Phone.setText(""+sessionManager.getPhone());
//            Buildingname.setText(sessionManager.getBuildingname());
//            Flatno.setText(sessionManager.getFlatno());
//            Floorno.setText(""+sessionManager.getFloorno());
//            Road.setText(sessionManager.getRoad());
//            Location.setText(sessionManager.getLocation());
//            City.setText(sessionManager.getCity());
//            Pincode.setText(""+sessionManager.getPincode());
//            District.setText(sessionManager.getDistrict());
//            State.setText(sessionManager.getState());
//
//            Title = findViewById(R.id.Title);
//            Title.setText("Edit Profile");
//            password = findViewById(R.id.Password);
//            password.setVisibility(View.GONE);
//            Password.setVisibility(View.GONE);
//        }

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*|application/*|video/*");
                startActivityForResult(intent,1);
            }
        });

        uid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent,2);
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String firstname = Firstname.getText().toString();
                final String middlename = Middlename.getText().toString();
                final String lastname = Lastname.getText().toString();
                final String dob = DOB.getText().toString();
                final String gender = Gender.getSelectedItem().toString();
                final String occupation = Occupation.getText().toString();
                final String email = Email.getText().toString();
                final String phone = Phone.getText().toString();
                final String buildingname = Buildingname.getText().toString();
                final String flatno = Flatno.getText().toString();
                final String floorno = Floorno.getText().toString();
                final String road = Road.getText().toString();
                final String location = Location.getText().toString();
                final String city = City.getText().toString();
                final String pincode = Pincode.getText().toString();
                final String state = State.getText().toString();
                final String uid = UID.getText().toString();
                final String password = Password.getText().toString();

                if (firstname.isEmpty() || middlename.isEmpty() || lastname.isEmpty() || dob.isEmpty() || gender.isEmpty() || occupation.isEmpty() || email.isEmpty()
                        || phone.isEmpty() || buildingname.isEmpty() || flatno.isEmpty() || floorno.isEmpty() || road.isEmpty()
                        || location.isEmpty() || city.isEmpty() || pincode.isEmpty() || state.isEmpty() || uid.isEmpty() || password.isEmpty() || uri == null)
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(CreateAccount.this);
                    builder.setMessage("No Field Should Be Empty");
                    builder.setCancelable(false);
                    builder.setNeutralButton("OK", null);
                    builder.show();
                }
                else
                {
                    flag = 0;

                    reff.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            reff.removeEventListener(this);
                            for (DataSnapshot ds : dataSnapshot.getChildren() )
                            {
                                hashMap = ((HashMap<String, Object>) ds.getValue());
                                if (hashMap.get("e_Mail").equals(firstname) || String.valueOf(hashMap.get("phone")).equals(phone))
                                {
//                                    if (getIntent().hasExtra("Key"))
//                                    {
//                                        Key = ds.getKey();
//                                        flag = 0;
//                                        break;
//                                    }
//                                    else
//                                        {
                                        flag = 1;
                                        Toast.makeText(CreateAccount.this, "Email or Phone No. Already Exists", Toast.LENGTH_SHORT).show();
                                        break;
                                    //}
                                }
                            }
                            if (flag == 0)
                            {
                                AlertDialog.Builder builder = new AlertDialog.Builder(CreateAccount.this);
                                builder.setMessage("Do You Want To Continue");
                                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        hashMap.clear();
                                        hashMap.put("e_Mail","oNAME");
                                        hashMap.put("Middlename",Middlename.getText().toString());
                                        hashMap.put("Lastname",Lastname.getText().toString());
                                        hashMap.put("DOB",DOB.getText().toString());
                                        hashMap.put("Age",21);
                                        hashMap.put("Occupation",Occupation.getText().toString());
                                        hashMap.put("Email",Email.getText().toString());
                                        hashMap.put("phone",Phone.getText().toString());
                                        hashMap.put("Buildingname",Buildingname.getText().toString());
                                        hashMap.put("Flatno",Flatno.getText().toString());
                                        hashMap.put("Floorno",Floorno.getText().toString());
                                        hashMap.put("Road",Road.getText().toString());
                                        hashMap.put("Location",Location.getText().toString());
                                        hashMap.put("City",City.getText().toString());
                                        hashMap.put("Pincode",Pincode.getText().toString());
                                        hashMap.put("State",State.getText().toString());
                                        hashMap.put("Uid",UID.getText().toString());

//                                        if (getIntent().hasExtra("Key"))
//                                        {
//                                            reff.child(Key).setValue(hashMap);
//                                        }
//                                        else {
                                            hashMap.put("Password",Password.getText().toString());
                                            reff.push().setValue(hashMap);
//                                        }

                                        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("Profile_Pics").child(email);

                                        storageRef.putFile(uri)
                                                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                    @Override
                                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                            // Get a URL to the uploaded content
                                                            // Uri downloadUrl = taskSnapshot.getDownloadUrl();
                                                    }
                                                });

                                        sessionManager.setLogin(true);
                                        sessionManager.setFirstname(Firstname.getText().toString());
                                        sessionManager.setMiddlename(Middlename.getText().toString());
                                        sessionManager.setLastname(Lastname.getText().toString());
                                        sessionManager.setEmail(Email.getText().toString());
                                        sessionManager.setPhone(Long.parseLong(phone));
                                        sessionManager.setBuildingname(Buildingname.getText().toString());
                                        sessionManager.setFlatno(flatno);
                                        sessionManager.setFloorno(Integer.parseInt(Floorno.getText().toString()));
                                        sessionManager.setRoad(Road.getText().toString());
                                        sessionManager.setLocation(Location.getText().toString());
                                        sessionManager.setCity(City.getText().toString());
                                        sessionManager.setPincode(Integer.parseInt(Pincode.getText().toString()));
                                        sessionManager.setState(State.getText().toString());

//                                        if (getIntent().hasExtra("Key"))
//                                        {
//                                            Toast.makeText(CreateAccount.this, "Profile Updated", Toast.LENGTH_LONG).show();
//                                        }
//                                        else
//                                        {
                                            Intent intent = new Intent(CreateAccount.this, com.example.myrental.OnLogin.class);
                                            startActivity(intent);
                                        //}

                                    }
                                }).setNegativeButton("NO", null);
                                builder.show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1)
        {
            if (resultCode == RESULT_OK)
            {
                if (data.getData() != null)
                {
                    Profile.setImageURI(data.getData());
                    uri = data.getData();
                }
            }
        }
    }
}
