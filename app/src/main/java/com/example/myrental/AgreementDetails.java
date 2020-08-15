package com.example.myrental;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

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

public class AgreementDetails extends AppCompatActivity {

    LinearLayout linearLayout, Photos;
    EditText Deposit, StartDate, Rent;
    Spinner MOP, RentTime;
    CheckBox Refundable;
    ImageView add, add_photos;
    Button button;
    DatabaseReference reference, databaseReference;
    ArrayList<String> arrayList = new ArrayList<>();
    ArrayList<Uri> ImageList = new ArrayList<>();
    HashMap<String,Object> user = new HashMap<>();
    int count = 0;
    int currentImageSelect = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agreement_details);

        linearLayout = findViewById(R.id.Clause);
        Photos = findViewById(R.id.Photos);
        Deposit = findViewById(R.id.despositAmount);
        MOP = findViewById(R.id.mode);
        Refundable = findViewById(R.id.RefundableDeposit);
        StartDate = findViewById(R.id.startDate);
        Rent = findViewById(R.id.rent);
        RentTime = findViewById(R.id.time_period);
        add = findViewById(R.id.add);
        add_photos = findViewById(R.id.add_photos);
        button = findViewById(R.id.submit);


        reference = FirebaseDatabase.getInstance().getReference().child("AddResidenceOwner");
        databaseReference = FirebaseDatabase.getInstance().getReference().child("RegistrationDatabase");

        final HashMap<String, Object> hashMap = (HashMap<String, Object>) getIntent().getSerializableExtra("list");
        user = (HashMap<String, Object>) getIntent().getSerializableExtra("user");

        ArrayAdapter<String> mop = new ArrayAdapter<>(AgreementDetails.this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.mop));
        mop.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        MOP.setAdapter(mop);

        if (hashMap.containsKey("Id"))
        {
            Deposit.setText("" + hashMap.get("Deposit"));
            MOP.setSelection(mop.getPosition("" + hashMap.get("Duration")));
            Rent.setText(""+hashMap.get("Rent"));
            RentTime.setSelection((new ArrayAdapter<>(AgreementDetails.this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.time_period))).getPosition(""+hashMap.get("Rent_Time")));

            if (hashMap.containsKey("Clause"))
            {
                ArrayList<String> clauses = (ArrayList<String>) hashMap.get("Clause");
                for (int i = 0; i < clauses.size(); i++) {
                    CheckBox checkBox = new CheckBox(AgreementDetails.this);
                    checkBox.setId(count);
                    checkBox.setText(clauses.get(i));
                    checkBox.setChecked(true);
                    linearLayout.addView(checkBox);
                    count++;
                }
            }

            while (currentImageSelect < 100)
            {
                try
                {
                    currentImageSelect++;
                    final File file = File.createTempFile("image","jpeg");
                    final StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl("gs://my-rental-4ff8d.appspot.com/Residence_Pics/").child(String.valueOf(hashMap.get("Id"))).child(String.valueOf(currentImageSelect));
                    storageReference.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>()
                    {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot)
                        {
                            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

                            ImageView imageView = new ImageView(AgreementDetails.this);
                            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(980,980);
                            layoutParams.setMargins(15,15,15,15);
                            imageView.setLayoutParams(layoutParams);
                            imageView.setPadding(3,3,3,3);
                            imageView.setBackgroundColor(getResources().getColor(android.R.color.black));
                            imageView.setAdjustViewBounds(true);
                            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                            imageView.setImageBitmap(bitmap);
                            imageView.setId(currentImageSelect);
                            Photos.addView(imageView);
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

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final CheckBox checkBox = new CheckBox(AgreementDetails.this);
                checkBox.setId(count);

                AlertDialog.Builder builder = new AlertDialog.Builder(AgreementDetails.this);
                builder.setMessage("Enter The Clause");
                final EditText editText = new EditText(AgreementDetails.this);
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

        add_photos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                startActivityForResult(intent,1);
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String deposit = Deposit.getText().toString();
                final String mop = MOP.getSelectedItem().toString();
                final String rent = Rent.getText().toString();
                final String refundable;
                arrayList.clear();

                for (int i = 0; i < count; i++)
                {
                    CheckBox checkBox = findViewById(i);
                    if (checkBox.isChecked())
                    {
                        arrayList.add(checkBox.getText().toString());
                    }
                }

                if (Refundable.isChecked())
                {
                    refundable = "YES";
                }
                else
                {
                    refundable = "NO";
                }

                if (deposit.isEmpty() || mop.isEmpty() || rent.isEmpty() || ImageList.isEmpty())
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(AgreementDetails.this);
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
                    final AlertDialog.Builder builder = new AlertDialog.Builder(AgreementDetails.this);
                    builder.setMessage("Save & Continue ?");
                    builder.setCancelable(true);
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SessionManager sessionManager = new SessionManager(getApplicationContext());
                            hashMap.put("AddedBy", sessionManager.getUID());
                            hashMap.put("Deposit", deposit);
                            hashMap.put("MOP", mop);
                            hashMap.put("Rent", rent);
                            hashMap.put("Rent_Period", RentTime.getSelectedItem().toString());
                            hashMap.put("Clause", arrayList);
                            if (hashMap.get("Status") == null)
                                hashMap.put("Status", "NO");
                            if (hashMap.get("Refundable") == null)
                                hashMap.put("Refundable", refundable);


                            if (!hashMap.containsKey("Id"))
                            {
                                Random random = new Random();
                                String s = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
                                String a = "";
                                for (int j = 0; j < 12; j++)
                                {
                                    int i = random.nextInt(62);
                                    a = a + s.charAt(i);
                                }
                                hashMap.put("Id", a);

                                reference = reference.push();
                                reference.setValue(hashMap);
                                insertInFirebase(reference.getKey());
                                //databaseReference.push().setValue(user);
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
                                //databaseReference.child(String.valueOf(user.get("Key"))).setValue(user);
                            }
                        }

                        private void insertInFirebase(String key)
                        {
                            arrayList.clear();
                            StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("Residence_Pics").child(String.valueOf(hashMap.get("Id")));
                            for (int c = 0; c < ImageList.size(); c++)
                            {
                                Uri imguri = ImageList.get(c);
                                final StorageReference riversRef = storageRef.child(String.valueOf(c));
                                final DatabaseReference databaseReferences = FirebaseDatabase.getInstance().getReference().child("AddResidenceOwner").child(key).child("URL").child(String.valueOf(c));

                                riversRef.putFile(imguri)
                                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                                            {
                                                riversRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
                                                {
                                                    @Override
                                                    public void onSuccess(Uri uri)
                                                    {
                                                        databaseReferences.setValue(String.valueOf(uri));
                                                    }
                                                });
                                            }

                                        });
                            }
                            ImageList.clear();

                            AlertDialog.Builder builder1 = new AlertDialog.Builder(AgreementDetails.this);
                            builder1.setMessage("Successfully Registered...");
                            builder1.setCancelable(false);
                            builder1.setNeutralButton("OK", new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    Intent intent = new Intent(AgreementDetails.this, com.example.myrental.OnLogin.class);
                                    startActivity(intent);
                                }
                            });
                            builder1.show();
                        }
                    });
                    builder.setNeutralButton("NO", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                        }
                    });
                    builder.show();
                }
            }
        });
        findViewById(R.id.previous).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AgreementDetails.super.onBackPressed();
            }
        });
    }

    public void onBackPressed()
    {
        Intent intent = new Intent(AgreementDetails.this, com.example.myrental.PropertyDetails.class);
        startActivity(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1)
        {
            if (resultCode == RESULT_OK)
            {
                Photos.removeAllViews();
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
                        Photos.addView(imageView);
                        ImageList.add(data.getClipData().getItemAt(currentImageSelect).getUri());
                        currentImageSelect++;
                    }
                }
                else if (data.getData() != null)
                {
                    ImageList.clear();
                    final ImageView imageView = new ImageView(this);
                    imageView.setLayoutParams(new LinearLayout.LayoutParams(300,300));
                    imageView.setImageURI(data.getData());
                    Photos.addView(imageView);
                    ImageList.add(data.getData());
                }
            }
        }
    }
}
