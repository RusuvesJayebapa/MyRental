package com.example.myrental;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
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
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
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
import java.util.Random;

public class CreateAccount extends AppCompatActivity
{
    TextView DOB;
    ImageView Profile;
    EditText Firstname, Middlename, Lastname, Occupation, Email, Phone, Buildingname, Flatno, Floorno, Road, Location, City, Pincode, District, State, Password;
    Button button, profile;
    Spinner Gender;
    DatabaseReference databaseReference;
    HashMap<String,Object> hashMap = new HashMap<>();
    long flag = 0;
    String day, mon;
    Uri profile_uri;
    SessionManager sessionManager;
    DatePickerDialog datePickerDialog;
    StorageReference storageRef;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        getSupportActionBar().hide();

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
        Password = findViewById(R.id.password);
        Profile = findViewById(R.id.Profile_Pic);
        profile = findViewById(R.id.profile_Pic);
        button = findViewById(R.id.submit);

        sessionManager = new SessionManager(getApplicationContext());

        databaseReference = FirebaseDatabase.getInstance().getReference().child("RegistrationDatabase");

        mAuth = FirebaseAuth.getInstance();


        DOB.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                final Calendar c = Calendar.getInstance();


                datePickerDialog = new DatePickerDialog(CreateAccount.this, new DatePickerDialog.OnDateSetListener()
                {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        if (dayOfMonth<10)
                            day = "0"+dayOfMonth;
                        else
                            day = ""+dayOfMonth;
                        if (month+1<10)
                            mon = "0"+(month+1);
                        else
                            mon = ""+month;
                        DOB.setText(day+"/"+mon+"/"+year);
                    }
                }, c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.MONTH), c.get(Calendar.YEAR));
                datePickerDialog.show();
            }
        });

        ArrayAdapter<String> gender = new ArrayAdapter<String>(CreateAccount.this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.gender));
        gender.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Gender.setAdapter(gender);

        profile.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent,1);
            }
        });

        button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
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
                final String district = District.getText().toString();
                final String state = State.getText().toString();
                final String password = Password.getText().toString();

                if (firstname.isEmpty() || middlename.isEmpty() || lastname.isEmpty() || dob.isEmpty() || gender.isEmpty() || occupation.isEmpty() || email.isEmpty()
                        || phone.isEmpty() || buildingname.isEmpty() || flatno.isEmpty() || floorno.isEmpty() || road.isEmpty()
                        || location.isEmpty() || city.isEmpty() || pincode.isEmpty() || state.isEmpty() || password.isEmpty() || profile_uri == null )
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
                    databaseReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            databaseReference.removeEventListener(this);
                            for (DataSnapshot ds : dataSnapshot.getChildren() )
                            {
                                hashMap = ((HashMap<String, Object>) ds.getValue());
                                if (hashMap.get("Email").equals(email) || String.valueOf(hashMap.get("Phone")).equals(phone))
                                {
                                        flag = 1;
                                        Toast.makeText(CreateAccount.this, "Email or Phone No. Already Exists", Toast.LENGTH_SHORT).show();
                                        break;
                                }
                            }
                            if (flag == 0)
                            {
                                AlertDialog.Builder builder = new AlertDialog.Builder(CreateAccount.this);
                                builder.setMessage("Do You Want To Continue");
                                builder.setPositiveButton("YES", new DialogInterface.OnClickListener()
                                {
                                    @RequiresApi(api = Build.VERSION_CODES.O)
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        hashMap.clear();
                                        hashMap.put("Firstname", firstname);
                                        hashMap.put("Middlename", middlename);
                                        hashMap.put("Lastname",lastname);
                                        hashMap.put("Gender", gender);
                                        hashMap.put("DOB", dob);
                                        hashMap.put("Occupation", occupation);
                                        hashMap.put("Email", email);
                                        hashMap.put("Phone", phone);
                                        hashMap.put("Buildingname", buildingname);
                                        hashMap.put("Flatno", flatno);
                                        hashMap.put("Floorno", floorno);
                                        hashMap.put("Road", road);
                                        hashMap.put("Location", location);
                                        hashMap.put("City", city);
                                        hashMap.put("Pincode", pincode);
                                        hashMap.put("District", district);
                                        hashMap.put("State", state);

                                        Random random = new Random();

                                        String s = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
                                        String a = "";
                                        for (int j = 0; j < 12; j++)
                                        {
                                            int i = random.nextInt(62);
                                            a = a + s.charAt(i);
                                        }

                                        hashMap.put("UID",a);

                                        final String finalA = a;

                                        mAuth.createUserWithEmailAndPassword(email, password)
                                                .addOnCompleteListener(CreateAccount.this, new OnCompleteListener<AuthResult>()
                                                {
                                                    @Override
                                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                                        if (task.isSuccessful()) {
                                                            // Sign in success, update UI with the signed-in user's information
                                                            Log.d("TAG", "createUserWithEmail:success");
                                                            sessionManager.setLogin(true);
                                                            sessionManager.setUID(finalA);
                                                            sessionManager.setFirstname(firstname);
                                                            sessionManager.setMiddlename(middlename);
                                                            sessionManager.setLastname(lastname);
                                                            sessionManager.setDOB(dob);
                                                            sessionManager.setGender(gender);
                                                            sessionManager.setOccupation(occupation);
                                                            sessionManager.setEmail(email);
                                                            sessionManager.setPhone(Long.parseLong(phone));
                                                            sessionManager.setBuildingname(buildingname);
                                                            sessionManager.setFlatno(flatno);
                                                            sessionManager.setFloorno(Integer.parseInt(floorno));
                                                            sessionManager.setRoad(road);
                                                            sessionManager.setLocation(location);
                                                            sessionManager.setCity(city);
                                                            sessionManager.setPincode(Integer.parseInt(pincode));
                                                            sessionManager.setDistrict(district);
                                                            sessionManager.setState(state);
                                                            sessionManager.setPassword(password);

                                                            databaseReference.push().setValue(hashMap);

                                                            storageRef = FirebaseStorage.getInstance().getReference().child("Profile_Pics").child(sessionManager.getUID());
                                                            storageRef.putFile(profile_uri)
                                                                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>()
                                                                    {
                                                                        @Override
                                                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                                                                        {
                                                                            // Get a URL to the uploaded content
                                                                            // Uri downloadUrl = taskSnapshot.getDownloadUrl();
                                                                            Intent intent = new Intent(CreateAccount.this, com.example.myrental.OnLogin.class);
                                                                            startActivity(intent);
                                                                        }
                                                                    });
                                                        }
                                                        else
                                                        {
                                                            // If sign in fails, display a message to the user.
                                                            //Log.w("TAG", "createUserWithEmail:failure", task.getException().getMessage());
                                                            System.out.println(task.getException().getMessage());
                                                            Toast.makeText(CreateAccount.this, "Authentication failed.",
                                                                    Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                    }
                                }).setNegativeButton("NO", null);
                                builder.show();
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError)
                        {
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK)
        {
                if (data.getData() != null)
                {
                    Profile.setImageURI(data.getData());
                    profile_uri = data.getData();
                }
        }
    }
}
