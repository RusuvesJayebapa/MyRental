package com.example.myrental;

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
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
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
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.HashMap;

public class ProfileUpdate extends AppCompatActivity
{
    TextView DOB, PasswordReset;
    ImageView Profile, profile;
    EditText Firstname, Middlename, Lastname, Occupation, Email, Phone, Buildingname, Flatno, Floorno, Road, Location, City, Pincode, District, State;
    Button save, cancel;
    Spinner Gender;
    HashMap<String,Object> hashMap = new HashMap<>();
    String day, mon;
    Uri profile_uri;
    SessionManager sessionManager;
    DatePickerDialog datePickerDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_update);

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
        Profile = findViewById(R.id.Profile_Pic);
        profile = findViewById(R.id.profile_Pic);
        PasswordReset = findViewById(R.id.PasswordReset);
        save = findViewById(R.id.save);
        cancel = findViewById(R.id.cancel);

        sessionManager = new SessionManager(getApplicationContext());

        DOB.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                final Calendar c = Calendar.getInstance();


                datePickerDialog = new DatePickerDialog(ProfileUpdate.this, new DatePickerDialog.OnDateSetListener()
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

        ArrayAdapter<String> gender = new ArrayAdapter<String>(ProfileUpdate.this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.gender));
        gender.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Gender.setAdapter(gender);

        Firstname.setText(sessionManager.getFirstname());
        Middlename.setText(sessionManager.getMiddlename());
        Lastname.setText(sessionManager.getLastname());
        DOB.setText(sessionManager.getDOB());
        Gender.setSelection(gender.getPosition(sessionManager.getGender()));
        Occupation.setText(sessionManager.getOccupation());
        Email.setText(sessionManager.getEmail());
        Phone.setText(sessionManager.getPhone().toString());
        Buildingname.setText(sessionManager.getBuildingname());
        Flatno.setText(sessionManager.getFlatno());
        Floorno.setText(sessionManager.getFloorno().toString());
        Road.setText(sessionManager.getRoad());
        Location.setText(sessionManager.getLocation());
        City.setText(sessionManager.getCity());
        Pincode.setText(sessionManager.getPincode().toString());
        District.setText(sessionManager.getDistrict());
        State.setText(sessionManager.getState());

        System.out.println("UID : " + sessionManager.getUID());
        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl("gs://my-rental-4ff8d.appspot.com/Profile_Pics/").child(sessionManager.getUID());
        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
        {
            @Override
            public void onSuccess(Uri uri)
            {
                Picasso.get().load(uri).placeholder(R.drawable.loading).into(Profile);
                profile_uri = uri;
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) { System.out.println("Image "+e);}
        });

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

        PasswordReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(ProfileUpdate.this);
                LinearLayout linearLayout = new LinearLayout(ProfileUpdate.this);
                linearLayout.setOrientation(LinearLayout.VERTICAL);
                TextView Email = new TextView(ProfileUpdate.this);
                Email.setText("Enter Your Email");
                final EditText email = new EditText(ProfileUpdate.this);
                email.setText(" ");
                linearLayout.addView(Email);
                linearLayout.addView(email);
                builder.setView(linearLayout);
                builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FirebaseAuth.getInstance().sendPasswordResetEmail(email.getText().toString().trim())
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(ProfileUpdate.this, "Password Reset Link Is Sent To Your Mail-Id", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(ProfileUpdate.this, "Invalid Email", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                });
                builder.show();
            }
        });

        save.setOnClickListener(new View.OnClickListener()
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

                if (firstname.isEmpty() || middlename.isEmpty() || lastname.isEmpty() || dob.isEmpty() || gender.isEmpty() || occupation.isEmpty() || email.isEmpty()
                        || phone.isEmpty() || buildingname.isEmpty() || flatno.isEmpty() || floorno.isEmpty() || road.isEmpty()
                        || location.isEmpty() || city.isEmpty() || pincode.isEmpty() || state.isEmpty() || profile_uri == null )
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ProfileUpdate.this);
                    builder.setMessage("No Field Should Be Empty");
                    builder.setCancelable(false);
                    builder.setNeutralButton("OK", null);
                    builder.show();
                }
                else
                {
                    final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("RegistrationDatabase");
                    databaseReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            databaseReference.removeEventListener(this);
                            for (final DataSnapshot ds : dataSnapshot.getChildren() )
                            {
                                hashMap = ((HashMap<String, Object>) ds.getValue());
                                if (hashMap.get("UID").equals(sessionManager.getUID()))
                                {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(ProfileUpdate.this);
                                    builder.setMessage("Do You Want To Continue");
                                    builder.setPositiveButton("YES", new DialogInterface.OnClickListener()
                                    {
                                        @RequiresApi(api = Build.VERSION_CODES.O)
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            hashMap.clear();
                                            hashMap.put("UID", sessionManager.getUID());
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

                                        final FirebaseAuth user = FirebaseAuth.getInstance();

                                            user.signInWithEmailAndPassword(sessionManager.getEmail(), sessionManager.getPassword())
                                                    .addOnCompleteListener(ProfileUpdate.this, new OnCompleteListener<AuthResult>()
                                                    {
                                                        @Override
                                                        public void onComplete(@NonNull Task<AuthResult> task)
                                                        {
                                                            if (task.isSuccessful())
                                                            {
                                                                user.getCurrentUser().updateEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if (task.isSuccessful()) {
                                                                            Log.d("TAG", "Password updated");
                                                                        } else {
                                                                            Log.d("TAG", "Error password not updated");
                                                                        }
                                                                    }
                                                                }).addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        System.out.println("Invalid");
                                                                    }
                                                                });
                                                            }
                                                            else
                                                            {
                                                                // If sign in fails, display a message to the user.
                                                                Toast.makeText(ProfileUpdate.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    });

                                            databaseReference.child(ds.getKey()).setValue(hashMap);

                                            StorageReference storageReference1 = FirebaseStorage.getInstance().getReference().child("Profile_Pics").child(sessionManager.getUID());
                                            storageReference1.putFile(profile_uri)
                                                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>()
                                                    {
                                                        @Override
                                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                                                        {
                                                            // Get a URL to the uploaded content
                                                            // Uri downloadUrl = taskSnapshot.getDownloadUrl();

                                                            Toast.makeText(ProfileUpdate.this, "Profile Updated Successfully", Toast.LENGTH_LONG).show();
                                                        }
                                                    }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    System.out.println("Exception : " + e);
                                                }
                                            });

                                            sessionManager.setLogin(true);
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

                                        }
                                    }).setNegativeButton("NO", null);
                                    builder.show();

                                    break;
                                }
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

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileUpdate.this, com.example.myrental.OnLogin.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(ProfileUpdate.this, com.example.myrental.OnLogin.class);
        startActivity(intent);
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
                System.out.println("Upload : " + data.getData());
            }
        }
    }
}
