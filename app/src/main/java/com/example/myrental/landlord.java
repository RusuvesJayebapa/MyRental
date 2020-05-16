package com.example.myrental;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.HashMap;

public class landlord extends AppCompatActivity {
    TextView DOB;
    EditText Firstname, Middlename, Lastname, Occupation, Email, Phone, Buildingname, Flatno, Floorno, Road, Location, City, Pincode, District, State, UID;
    Button button;
    Spinner Gender;

    DatabaseReference reference;
    HashMap<String, Object> edit = new HashMap<>();
    HashMap<String,Object> hashMap = new HashMap<>();
    DatePickerDialog datePickerDialog;
    String day, mon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landlord);

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

        button = findViewById(R.id.submit);

        if (getIntent().hasExtra("id"))
        {
            reference = FirebaseDatabase.getInstance().getReference().child("AddResidenceOwner");

            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    reference.removeEventListener(this);

                    for (DataSnapshot ds : dataSnapshot.getChildren())
                    {
                        edit = (HashMap<String, Object>) ds.getValue();

                        if (edit.get("Id").equals(getIntent().getStringExtra("id")))
                        {
                            add(edit, ds.getKey());
                            break;
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        DOB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();


                datePickerDialog = new DatePickerDialog(landlord.this, new DatePickerDialog.OnDateSetListener() {
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

        ArrayAdapter<String> gender = new ArrayAdapter<>(landlord.this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.gender));
        gender.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Gender.setAdapter(gender);

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
                final String district = District.getText().toString();
                final String state = State.getText().toString();
                final String uid = UID.getText().toString();

                if (firstname.isEmpty() || middlename.isEmpty() || lastname.isEmpty() || dob.isEmpty() || gender.isEmpty() || occupation.isEmpty() || email.isEmpty()
                        || phone.isEmpty() || buildingname.isEmpty() || flatno.isEmpty() || floorno.isEmpty() || road.isEmpty()
                        || location.isEmpty() || city.isEmpty() || pincode.isEmpty() || district.isEmpty() || state.isEmpty() || uid.isEmpty())
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(landlord.this);
                    builder.setMessage("No Field Should Be Empty");
                    builder.setCancelable(false);
                    builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    builder.show();
                }
                else
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(landlord.this);
                    builder.setMessage("Save & Continue ?");
                    builder.setCancelable(true);
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            hashMap.put("Firstname",firstname);
                            hashMap.put("Middlename",middlename);
                            hashMap.put("Lastname",lastname);
                            hashMap.put("DOB",dob);
                            hashMap.put("Age",21);
                            hashMap.put("Occupation",occupation);
                            hashMap.put("Email",email);
                            hashMap.put("Phone",Long.parseLong(phone));
                            hashMap.put("Landlord_Buildingname",buildingname);
                            hashMap.put("Landlord_Flatno",flatno);
                            hashMap.put("Landlord_Floorno",Integer.parseInt(floorno));
                            hashMap.put("Landlord_Road",road);
                            hashMap.put("Landlord_Location",location);
                            hashMap.put("Landlord_City",city);
                            hashMap.put("Landlord_Pincode",Integer.parseInt(pincode));
                            hashMap.put("Landlord_District",district);
                            hashMap.put("Landlord_State",state);
                            hashMap.put("Uid",Long.parseLong(uid));

                            Intent intent = new Intent(landlord.this, com.example.myrental.PropertyDetails.class);
                            intent.putExtra("list", hashMap);
                            startActivity(intent);
                        }
                    });
                    builder.setNeutralButton("NO", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    builder.show();
                }
            }
        });
    }

    private void add(HashMap<String, Object> edit, String key) {
        hashMap = edit;
        hashMap.put("Key",key);
        Firstname.setText(""+edit.get("Firstname"));
        Middlename.setText(""+edit.get("Middlename"));
        Lastname.setText(""+edit.get("Lastname"));
        DOB.setText(""+edit.get("DOB"));
        ArrayAdapter<String> gender = new ArrayAdapter<String>(landlord.this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.gender));
        Gender.setSelection(gender.getPosition(""+edit.get("Gender")));
        Occupation.setText(""+edit.get("Occupation"));
        Email.setText(""+edit.get("Email"));
        Phone.setText(""+edit.get("Phone"));
        Buildingname.setText(""+edit.get("Buildingname"));
        Flatno.setText(""+edit.get("Flatno"));
        Floorno.setText(""+edit.get("Floorno"));
        Road.setText(""+edit.get("Road"));
        Location.setText(""+edit.get("Location"));
        City.setText(""+edit.get("City"));
        Pincode.setText(""+edit.get("Pincode"));
        District.setText(""+edit.get("District"));
        State.setText(""+edit.get("State"));
        UID.setText(""+edit.get("Uid"));
    }
}