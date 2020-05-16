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

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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
    EditText Duration, Deposit, StartDate;
    Spinner MOP;
    CheckBox Refundable;
    ImageView add, add_photos;
    Button button;
    DatabaseReference reference;
    ArrayList<String> arrayList = new ArrayList<>();
    ArrayList<Uri> ImageList = new ArrayList<>();
    int count = 0;
    int currentImageSelect = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agreement_details);

        linearLayout = findViewById(R.id.Clause);
        Photos = findViewById(R.id.Photos);
        Duration = findViewById(R.id.duration);
        Deposit = findViewById(R.id.despositAmount);
        MOP = findViewById(R.id.mode);
        Refundable = findViewById(R.id.RefundableDeposit);
        StartDate = findViewById(R.id.startDate);
        add = findViewById(R.id.add);
        add_photos = findViewById(R.id.add_photos);
        button = findViewById(R.id.submit);

        reference = FirebaseDatabase.getInstance().getReference().child("AddResidenceOwner");
        //final HashMap<String, Object> hashMap = (HashMap<String, Object>) getIntent().getSerializableExtra("list");
        final HashMap<String, Object> hashMap = new HashMap<>();

        ArrayAdapter<String> mop = new ArrayAdapter<>(AgreementDetails.this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.mop));
        mop.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        MOP.setAdapter(mop);

        if (hashMap.containsKey("Key"))
        {
            Duration.setText("" + hashMap.get("Duration"));
            Deposit.setText("" + hashMap.get("Deposit"));
            MOP.setSelection(mop.getPosition("" + hashMap.get("Duration")));
            StartDate.setText("" + hashMap.get("StartDate"));


            ArrayList<String> clauses = (ArrayList<String>) hashMap.get("Clause");
            for (int i = 0; i < clauses.size(); i++) {
                CheckBox checkBox = new CheckBox(AgreementDetails.this);
                checkBox.setId(count);
                checkBox.setText(clauses.get(i));
                checkBox.setChecked(true);
                linearLayout.addView(checkBox);
                count++;
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
                final String duration = Duration.getText().toString();
                final String deposit = Deposit.getText().toString();
                final String mop = MOP.getSelectedItem().toString();
                final String start = StartDate.getText().toString();
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

                if (duration.isEmpty() || deposit.isEmpty() || mop.isEmpty() || start.isEmpty() || ImageList.isEmpty())
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
                    AlertDialog.Builder builder = new AlertDialog.Builder(AgreementDetails.this);
                    builder.setMessage("Save & Continue ?");
                    builder.setCancelable(true);
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SessionManager sessionManager = new SessionManager(getApplicationContext());
                            hashMap.put("AddedBy", sessionManager.getEmail());
                            hashMap.put("Duration", Integer.valueOf(duration));
                            hashMap.put("Deposit", Integer.valueOf(deposit));
                            hashMap.put("MOP", mop);
                            hashMap.put("StartDate", start);
                            hashMap.put("Clause", arrayList);
                            if (hashMap.get("Status") == null)
                                hashMap.put("Status", "NO");
                            if (hashMap.get("Refundable") == null)
                                hashMap.put("Refundable", refundable);

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

                                reference.push().setValue(hashMap);
                            }
                            else
                            {
                                reference.child(String.valueOf(hashMap.get("Key"))).setValue(hashMap);
                            }
                            StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("Residence_Pics").child(String.valueOf(hashMap.get("Id")));
                            for (int c = 0; c < ImageList.size(); c++) {
                                Uri imguri = ImageList.get(c);
                                final StorageReference riversRef = storageRef.child(String.valueOf(c));

                                riversRef.putFile(imguri)
                                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                            }

                                        });
                            }
                            ImageList.clear();

                            Intent intent = new Intent(AgreementDetails.this, com.example.myrental.MyResidence.class);
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

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
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
