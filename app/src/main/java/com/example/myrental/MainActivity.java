package com.example.myrental;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements View.OnClickListener
{
    int flag = 0;
    boolean isWIFI, isMOBILE;
    TextView changePassword, createAccount;
    EditText useremail, password;
    Button login;
    DatabaseReference databaseReference;
    SessionManager sessionManager;
    FirebaseAuth mAuth;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().hide();

        useremail = findViewById(R.id.useremail);
        password = findViewById(R.id.password);
        login = findViewById(R.id.login);
        changePassword = findViewById(R.id.changePassword);
        createAccount = findViewById(R.id.createAccount);


        databaseReference = FirebaseDatabase.getInstance().getReference().child("RegistrationDatabase");
        mAuth = FirebaseAuth.getInstance();

        sessionManager = new SessionManager(getApplicationContext());

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Required Internet Connection");
        progressDialog.show();

        login.setOnClickListener(this);
        changePassword.setOnClickListener(this);
        createAccount.setOnClickListener(this);

        if (isOnline())
        {
            progressDialog.cancel();
        }

        if (sessionManager.getLogin())
        {
            System.out.println("UID : " + sessionManager.getUID());
            Intent intent = new Intent(MainActivity.this, com.example.myrental.OnLogin.class);
            startActivity(intent);
        }
    }

    private boolean isOnline()
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] networkInfo = connectivityManager.getAllNetworkInfo();
        for (NetworkInfo info:networkInfo)
        {
            if (info.getTypeName().equalsIgnoreCase("WIFI") && info.isConnected())
                isWIFI = true;
            if (info.getTypeName().equalsIgnoreCase("MOBILE") && info.isConnected())
                isMOBILE = true;
        }

        return isMOBILE || isWIFI;
    }

    @Override
    public void onClick(View v)
    {
        if(v.getId() == R.id.login)
        {
            final String email, pass;
            email = useremail.getText().toString().trim();
            pass = password.getText().toString().trim();

            if(email.isEmpty() || pass.isEmpty())
            {
                Toast.makeText(MainActivity.this,"Enter Both Email And Password", Toast.LENGTH_SHORT).show();
            }
            else
            {
                mAuth.signInWithEmailAndPassword(email, pass)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>()
                        {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task)
                            {
                                if (task.isSuccessful())
                                {
                                    // Sign in success, update UI with the signed-in user's information

                                    System.out.println(email + " " + pass);
                                    flag = 0;
                                    databaseReference.addValueEventListener(new ValueEventListener()
                                    {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                        {
                                            for (DataSnapshot ds : dataSnapshot.getChildren())
                                            {
                                                HashMap<String, Object> hashMap = (HashMap<String, Object>) ds.getValue();
                                                if (hashMap.get("Email").equals(email))
                                                {
                                                    sessionManager.setLogin(true);
                                                    sessionManager.setUID("" + hashMap.get("UID"));
                                                    sessionManager.setFirstname("" + hashMap.get("Firstname"));
                                                    sessionManager.setMiddlename("" + hashMap.get("Middlename"));
                                                    sessionManager.setLastname("" + hashMap.get("Lastname"));
                                                    sessionManager.setGender("" + hashMap.get("Gender"));
                                                    sessionManager.setDOB("" + hashMap.get("DOB"));
                                                    sessionManager.setOccupation(""+hashMap.get("Occupation"));
                                                    sessionManager.setEmail("" + hashMap.get("Email"));
                                                    sessionManager.setPhone(Long.parseLong("" + hashMap.get("Phone")));
                                                    sessionManager.setBuildingname("" + hashMap.get("Buildingname"));
                                                    sessionManager.setFlatno("" + hashMap.get("Flatno"));
                                                    sessionManager.setFloorno(Integer.parseInt("" + hashMap.get("Floorno")));
                                                    sessionManager.setRoad("" + hashMap.get("Road"));
                                                    sessionManager.setLocation("" + hashMap.get("Location"));
                                                    sessionManager.setCity("" + hashMap.get("City"));
                                                    sessionManager.setPincode(Integer.parseInt("" + hashMap.get("Pincode")));
                                                    sessionManager.setDistrict(""+hashMap.get("District"));
                                                    sessionManager.setState("" + hashMap.get("State"));
                                                    sessionManager.setPassword(pass);
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
                                                Intent intent = new Intent(MainActivity.this, com.example.myrental.OnLogin.class);
                                                startActivity(intent);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError)
                                        {
                                        }
                                    });
                                }
                                else
                                {
                                    // If sign in fails, display a message to the user.
                                    Log.w("TAG", "signInWithEmail:failure", task.getException());
                                    Toast.makeText(MainActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                                }
                            }
                });
            }
        }

        if(v.getId() == R.id.changePassword)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            LinearLayout linearLayout = new LinearLayout(this);
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            TextView Email = new TextView(this);
            Email.setText("Enter Your Email");
            final EditText email = new EditText(this);
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
                                        Toast.makeText(MainActivity.this, "Password Reset Link Is Sent To Your Mail-Id", Toast.LENGTH_LONG).show();
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MainActivity.this, "Invalid Email", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            });
            builder.show();
        }

        if(v.getId() == R.id.createAccount)
        {
            Intent intent = new Intent(MainActivity.this, com.example.myrental.CreateAccount.class);
            startActivity(intent);
        }
    }
}
