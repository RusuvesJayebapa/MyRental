package com.example.myrental;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    int flag = 0;
    EditText username, password;
    Button login;
    TextView textView, textView2;
    DatabaseReference reff;
    SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        login = findViewById(R.id.button);
        textView = findViewById(R.id.textView);
        textView2 = findViewById(R.id.textView2);
        reff = FirebaseDatabase.getInstance().getReference().child("RegistrationDatabase");

        sessionManager = new SessionManager(getApplicationContext());

        login.setOnClickListener(this);
        textView.setOnClickListener(this);
        textView2.setOnClickListener(this);

        isOnline();
        sessionManager.setLogin(true);
        if(sessionManager.getLogin())
        {
            Intent intent = new Intent(MainActivity.this, com.example.myrental.CreateAgreement.class);
            startActivity(intent);
        }
    }

    private boolean isOnline() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnected() || !networkInfo.isAvailable())
        {
            Toast.makeText(MainActivity.this, "No Internet Connection!", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.button)
        {
            final String email, pass;
            email = username.getText().toString().trim();
            pass = password.getText().toString().trim();
            if(email.isEmpty() || pass.isEmpty())
            {
                Toast.makeText(MainActivity.this,"Enter Both Email And Password", Toast.LENGTH_SHORT).show();
            }
            else
            {
                flag = 0;
                reff.addValueEventListener(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        for (DataSnapshot ds : dataSnapshot.getChildren())
                        {
                            HashMap<String,Object> hashMap = (HashMap<String, Object>) ds.getValue();
                            if (hashMap.get("e_Mail").equals(email) && hashMap.get("password").equals(pass))
                            {
                                sessionManager.setLogin(true);
                                sessionManager.setFirstname(""+hashMap.get("Firstname"));
                                sessionManager.setMiddlename(""+hashMap.get("Middlename"));
                                sessionManager.setLastname(""+hashMap.get("Lastname"));
                                sessionManager.setDOB(""+hashMap.get("DOB"));
                                sessionManager.setEmail(""+hashMap.get("e_Mail"));
                                sessionManager.setPhone(Long.parseLong(""+hashMap.get("phone")));
                                sessionManager.setBuildingname(""+hashMap.get("Buildingname"));
                                sessionManager.setFlatno(""+hashMap.get("Flatno"));
                                sessionManager.setFloorno(Integer.parseInt(""+hashMap.get("Floorno")));
                                sessionManager.setRoad(""+hashMap.get("Road"));
                                sessionManager.setLocation(""+hashMap.get("Location"));
                                sessionManager.setCity(""+hashMap.get("City"));
                                sessionManager.setPincode(Integer.parseInt(""+hashMap.get("Pincode")));
                                sessionManager.setState(""+hashMap.get("State"));
                                flag = 1;
                                break;
                            }
                        }
                        if (flag == 0)
                        {
                            Toast.makeText(MainActivity.this, "Enter Valid E-Mail And Password", Toast.LENGTH_SHORT).show();
                        }
                        else if (flag == 1)
                        {
                            sessionManager.setLogin(true);

                            sessionManager.setEmail(email);
                            Intent intent = new Intent(MainActivity.this, com.example.myrental.OnLogin.class);
                            intent.putExtra("email", email);
                            startActivity(intent);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError)
                    {

                    }
                });
            }
        }

        if(v.getId() == R.id.textView)
        {

        }

        if(v.getId() == R.id.textView2)
        {
            Intent intent = new Intent(MainActivity.this, com.example.myrental.CreateAccount.class);
            startActivity(intent);
        }
    }
}
