package com.example.myrental;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class VehicleDetails extends AppCompatActivity
{
    Spinner vehicle_class, payment_period, fuel_type;
    LinearLayout linearLayout, vehicle_photo;
    EditText Vehicle_Name, Model_Number, Mileage, Power, Seats, Payment, Deposit;
    RadioGroup AC;
    CheckBox Refundable;
    ImageView add_vehicle_photo, add_clauses;
    Button button;
    DatabaseReference reference;
    ArrayList<String> arrayList = new ArrayList<>();
    ArrayList<Uri> ImageList = new ArrayList<>();
    int count = 0, currentImageSelect = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_details);

        getSupportActionBar().hide();

        linearLayout = findViewById(R.id.Clause);
        Vehicle_Name = findViewById(R.id.vehiclename);
        Model_Number = findViewById(R.id.modelnumber);
        Mileage = findViewById(R.id.mileage);
        Power = findViewById(R.id.power);
        Seats = findViewById(R.id.seats);
        AC = findViewById(R.id.ac);
        fuel_type = findViewById(R.id.fuel);
        Payment = findViewById(R.id.payment);
        payment_period = findViewById(R.id.payment_time_period);
        vehicle_class = findViewById(R.id.vehicleclass);
        Deposit = findViewById(R.id.despositAmount);
        Refundable = findViewById(R.id.RefundableDeposit);
        add_clauses = findViewById(R.id.add_clauses);
        vehicle_photo = findViewById(R.id.vehicle_photo);
        add_vehicle_photo = findViewById(R.id.add_vehicle_photo);
        button = findViewById(R.id.submit);

        reference = FirebaseDatabase.getInstance().getReference().child("AddVehicleOwner");

        final HashMap<String, Object> hashMap = (HashMap<String, Object>) getIntent().getSerializableExtra("list");

        vehicle_class.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (!vehicle_class.getSelectedItem().equals("Bike")) {
                    findViewById(R.id.additional_info).setVisibility(View.VISIBLE);
                } else {
                    findViewById(R.id.additional_info).setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        if (hashMap.containsKey("Id"))
        {
            Vehicle_Name.setText("" + hashMap.get("VehicleName"));
            Model_Number.setText("" + hashMap.get("ModelNumber"));
            vehicle_class.setSelection(new ArrayAdapter<>(VehicleDetails.this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.vehicle_class)).getPosition("" + hashMap.get("VehicleClass")));
            Mileage.setText("" + hashMap.get("Mileage"));
            Power.setText("" + hashMap.get("Power"));
            if (!vehicle_class.equals("Bike")) {
                Seats.setText("" + hashMap.get("Seats"));
                if (hashMap.get("AC").equals("YES"))
                    ((RadioButton) findViewById(R.id.ayes)).setChecked(true);
                else
                    ((RadioButton) findViewById(R.id.ano)).setChecked(true);
            }
            fuel_type.setSelection(new ArrayAdapter<>(VehicleDetails.this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.fuel)).getPosition("" + hashMap.get("Fuel")));
            Deposit.setText("" + hashMap.get("Deposit"));
            if (hashMap.get("Refundable").equals("YES")) {
                Refundable.setChecked(true);
            }
            Payment.setText("" + hashMap.get("Payment"));
            payment_period.setSelection(new ArrayAdapter<>(VehicleDetails.this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.vehicle_time_period)).getPosition("" + hashMap.get("PaymentTimePeriod")));

            ArrayList<String> clauses = (ArrayList<String>) hashMap.get("Clause");
            for (int i = 0; i < clauses.size(); i++) {
                CheckBox checkBox = new CheckBox(VehicleDetails.this);
                checkBox.setId(count);
                checkBox.setText(clauses.get(i));
                checkBox.setChecked(true);
                linearLayout.addView(checkBox);
                count++;
            }

            while (currentImageSelect < 100)
            {
                try {
                    currentImageSelect++;
                    final File file = File.createTempFile("image", "jpeg");
                    final StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl("gs://my-rental-4ff8d.appspot.com/Vehicle_Pics/").child(String.valueOf(hashMap.get("Id"))).child(String.valueOf(currentImageSelect));
                    storageReference.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

                            ImageView imageView = new ImageView(VehicleDetails.this);
                            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(980, 980);
                            layoutParams.setMargins(15, 15, 15, 15);
                            imageView.setLayoutParams(layoutParams);
                            imageView.setPadding(3, 3, 3, 3);
                            imageView.setBackgroundColor(getResources().getColor(android.R.color.black));
                            imageView.setAdjustViewBounds(true);
                            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                            imageView.setImageBitmap(bitmap);
                            imageView.setId(currentImageSelect);
                            vehicle_photo.addView(imageView);
                            storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    ImageList.add(uri);
                                }
                            });
                        }
                    });
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }

        add_clauses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final CheckBox checkBox = new CheckBox(VehicleDetails.this);
                checkBox.setId(count);

                AlertDialog.Builder builder = new AlertDialog.Builder(VehicleDetails.this);
                builder.setMessage("Enter The Clause");
                final EditText editText = new EditText(VehicleDetails.this);
                builder.setView(editText);
                builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!editText.getText().toString().isEmpty()) {
                            count++;
                            checkBox.setText(editText.getText().toString());
                            checkBox.setChecked(true);
                            linearLayout.addView(checkBox);
                        }
                    }
                });
                builder.show();

            }
        });

        add_vehicle_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                startActivityForResult(intent, 1);
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String vehicle = Vehicle_Name.getText().toString();
                final String model = Model_Number.getText().toString();
                final String vehicleclass = vehicle_class.getSelectedItem().toString();
                final String mileage = Mileage.getText().toString();
                final String power = Power.getText().toString();
                final String deposit = Deposit.getText().toString();
                final String fuel = fuel_type.getSelectedItem().toString();
                final String refundable = Refundable.isChecked() ? "YES" : "NO";
                final String payment = Payment.getText().toString();
                final String paymentperiod = payment_period.getSelectedItem().toString();

                if (!vehicleclass.equals("Bike")) {
                    final String seats = Seats.getText().toString();
                    final String ac = ((RadioButton) findViewById(AC.getCheckedRadioButtonId())).getText().toString();

                    hashMap.put("Seats", seats);
                    hashMap.put("AC", ac);
                }

                arrayList.clear();

                for (int i = 0; i < count; i++)
                {
                    CheckBox checkBox = findViewById(i);
                    if (checkBox.isChecked()) {
                        arrayList.add(checkBox.getText().toString());
                    }
                }

                if (vehicle.isEmpty() || model.isEmpty() || mileage.isEmpty() || power.isEmpty() || deposit.isEmpty() || payment.isEmpty() || ImageList.isEmpty())
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(VehicleDetails.this);
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
                    AlertDialog.Builder builder = new AlertDialog.Builder(VehicleDetails.this);
                    builder.setMessage("Save & Continue ?");
                    builder.setCancelable(true);
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            SessionManager sessionManager = new SessionManager(getApplicationContext());
                            hashMap.put("AddedBy", sessionManager.getUID());
                            hashMap.put("VehicleName", vehicle);
                            hashMap.put("ModelNumber", model);
                            hashMap.put("VehicleClass", vehicleclass);
                            hashMap.put("Mileage", mileage);
                            hashMap.put("Power", power);
                            hashMap.put("Fuel", fuel);
                            hashMap.put("Deposit", deposit);
                            hashMap.put("Refundable", refundable);
                            hashMap.put("Payment", payment);
                            hashMap.put("PaymentTimePeriod", paymentperiod);
                            hashMap.put("Clause", arrayList);
                            if (hashMap.get("Status") == null)
                                hashMap.put("Status", "NO");

                            if (String.valueOf(hashMap.get("Key")).length() < 6)
                            {
                                Random random = new Random();
                                String s = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
                                String a = "";
                                for (int j = 0; j < 12; j++) {
                                    int i = random.nextInt(62);
                                    a = a + s.charAt(i);
                                }
                                hashMap.put("Id", a);

                                reference = reference.push();
                                reference.setValue(hashMap);
                                insertInFirebase(reference.getKey());
                            }
                            else
                            {
                                reference.addListenerForSingleValueEvent(new ValueEventListener()
                                {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                    {
                                        for (DataSnapshot ds : dataSnapshot.getChildren())
                                        {
                                            if (((HashMap<String, Object>)ds.getValue()).get("Id").equals(hashMap.get("Id")))
                                            {
                                                insertInFirebase(ds.getKey());
                                                break;
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) { }
                                });
                                reference.child(String.valueOf(hashMap.get("Key"))).setValue(hashMap);
                            }
                        }

                        private void insertInFirebase(String key)
                        {
                            System.out.println("Key : " + key);
                            arrayList.clear();
                            StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("Vehicle_Pics").child(String.valueOf(hashMap.get("Id")));
                            for (int c = 0; c < ImageList.size(); c++)
                            {
                                System.out.println("1");
                                Uri imguri = ImageList.get(c);
                                final StorageReference riversRef = storageRef.child(String.valueOf(c));
                                final DatabaseReference databaseReferences = FirebaseDatabase.getInstance().getReference().child("AddVehicleOwner").child(key).child("URL").child(String.valueOf(c));

                                riversRef.putFile(imguri)
                                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                System.out.println("2");
                                                riversRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                    @Override
                                                    public void onSuccess(Uri uri) {
                                                        System.out.println("3");
                                                        databaseReferences.setValue(String.valueOf(uri));
                                                    }
                                                });
                                            }

                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        System.out.println(e);
                                    }
                                });
                            }
                            ImageList.clear();

                            AlertDialog.Builder builder1 = new AlertDialog.Builder(VehicleDetails.this);
                            builder1.setMessage("Successfully Registered...");
                            builder1.setCancelable(false);
                            builder1.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(VehicleDetails.this, com.example.myrental.OnLogin.class);
                                    startActivity(intent);
                                }
                            });
                            builder1.show();
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

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1)
        {
            if (resultCode == RESULT_OK)
            {
                vehicle_photo.removeAllViews();
                if (data.getClipData() != null)
                {
                    int countClipData = data.getClipData().getItemCount(), currentImageSelect = 0;
                    ImageList.clear();
                    while (currentImageSelect < countClipData)
                    {
                        ImageView imageView = new ImageView(this);
                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(980,980);
                        layoutParams.setMargins(15,15,15,15);
                        imageView.setLayoutParams(layoutParams);
                        imageView.setPadding(3,3,3,3);
                        imageView.setBackgroundColor(getResources().getColor(android.R.color.black));
                        imageView.setAdjustViewBounds(true);
                        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                        imageView.setImageURI(data.getClipData().getItemAt(currentImageSelect).getUri());
                        imageView.setId(currentImageSelect);
                        vehicle_photo.addView(imageView);
                        ImageList.add(data.getClipData().getItemAt(currentImageSelect).getUri());
                        currentImageSelect++;
                    }
                }
                else if (data.getData() != null)
                {
                    ImageList.clear();

                    ImageView imageView = new ImageView(this);
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(980,980);
                    layoutParams.setMargins(15,15,15,15);
                    imageView.setLayoutParams(layoutParams);
                    imageView.setPadding(3,3,3,3);
                    imageView.setBackgroundColor(getResources().getColor(android.R.color.black));
                    imageView.setAdjustViewBounds(true);
                    imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                    imageView.setImageURI(data.getData());
                    vehicle_photo.addView(imageView);
                    ImageList.add(data.getData());
                }
            }
        }

    }
}
