package com.example.myrental;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.util.TypedValue;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;

public class PropertyDetails extends AppCompatActivity implements View.OnClickListener {

    LinearLayout linearLayout, linearLayout1;
    TextView Appliance;
    EditText appliance, Property_typeno, Area, Surveyno, Buildingname, Flatno, Floorno, Road, Location, City, Pincode, State, Bed, Tv, Ac, Gyeser;
    Spinner Category, Property_type, Survey_Plot, Usage;
    ImageView add;
    Button button;
    int count = 0, id = 0;
    HashMap<String,Integer> hashMap_appliances = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_property_details);

        final HashMap<String,Object> hashMap = (HashMap<String, Object>) getIntent().getSerializableExtra("list");

        linearLayout = findViewById(R.id.Applicance_Furniture);
        Category = findViewById(R.id.category);
        Property_type = findViewById(R.id.propertytype);
        Property_typeno = findViewById(R.id.type);
        Area = findViewById(R.id.area);
        Survey_Plot = findViewById(R.id.surveyno);
        Surveyno = findViewById(R.id.number);
        Usage = findViewById(R.id.usage);
        Buildingname = findViewById(R.id.buildingname);
        Flatno = findViewById(R.id.flatno);
        Floorno = findViewById(R.id.floorno);
        Road = findViewById(R.id.road);
        Location = findViewById(R.id.location);
        City = findViewById(R.id.place);
        Pincode = findViewById(R.id.pincode);
        State = findViewById(R.id.state);
        Bed = findViewById(R.id.bed);
        Tv = findViewById(R.id.tv);
        Ac = findViewById(R.id.ac);
        Gyeser = findViewById(R.id.gyeser);
        add = findViewById(R.id.add);
        button = findViewById(R.id.submit);

        linearLayout1 = new LinearLayout(PropertyDetails.this);
        linearLayout.addView(linearLayout1);

        ArrayAdapter<String> category = new ArrayAdapter<String>(PropertyDetails.this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.category));
        category.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Category.setAdapter(category);

        ArrayAdapter<String> propertytype = new ArrayAdapter<String>(PropertyDetails.this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.property_type));
        propertytype.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Property_type.setAdapter(propertytype);

        ArrayAdapter<String> surveyno = new ArrayAdapter<String>(PropertyDetails.this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.survey));
        surveyno.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Survey_Plot.setAdapter(surveyno);

        ArrayAdapter<String> usage = new ArrayAdapter<String>(PropertyDetails.this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.usage));
        usage.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Usage.setAdapter(usage);

        if (hashMap.containsKey("Key"))
        {
            Category.setSelection(category.getPosition("" + hashMap.get("Category")));
            Property_type.setSelection(propertytype.getPosition("" + hashMap.get("Property_Type")));
            Property_typeno.setText("" + hashMap.get("Property_Typeno"));
            Area.setText("" + hashMap.get("Area"));
            Survey_Plot.setSelection(surveyno.getPosition("" + hashMap.get("Survey_Plot")));
            Surveyno.setText("" + hashMap.get("Surveyno"));
            Usage.setSelection(usage.getPosition("" + hashMap.get("Usage")));
            Buildingname.setText("" + hashMap.get("Buildingname"));
            Flatno.setText("" + hashMap.get("Flatno"));
            Floorno.setText("" + hashMap.get("Floorno"));
            Road.setText("" + hashMap.get("Road"));
            Location.setText("" + hashMap.get("Location"));
            City.setText("" + hashMap.get("City"));
            Pincode.setText("" + hashMap.get("Pincode"));
            State.setText("" + hashMap.get("State"));

            HashMap<String, Integer> appliances_furniture = (HashMap<String, Integer>) hashMap.get("Appliances");
            if (appliances_furniture.containsKey("Bed"))
                Bed.setText(""+appliances_furniture.get("Bed"));
            if (appliances_furniture.containsKey("TV"))
                Tv.setText(""+appliances_furniture.get("TV"));
            if (appliances_furniture.containsKey("AC"))
                Ac.setText(""+appliances_furniture.get("AC"));
            if (appliances_furniture.containsKey("Gyeser"))
                Gyeser.setText(""+appliances_furniture.get("Gyeser"));


            for (int i = 0; i < appliances_furniture.size(); i++)
            {
            count++;
            id++;
            LinearLayout ll = new LinearLayout(PropertyDetails.this);
            ll.setOrientation(LinearLayout.VERTICAL);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(222, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.weight = 1;
            params.setMargins(15, 15, 15, 0);

            TextView textView = new TextView(PropertyDetails.this);
            textView.setId(count);
            textView.setText(""+appliances_furniture.keySet().toArray()[i]);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            textView.setTextColor(Color.BLACK);
            textView.setBackgroundResource(R.drawable.textbox);
            textView.setLayoutParams(params);

            LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(222, LinearLayout.LayoutParams.WRAP_CONTENT);
            param.weight = 1;
            param.setMargins(15, 15, 15, 15);

            EditText editText = new EditText(PropertyDetails.this);
            editText.setId(count+100);
            editText.setPadding(10, 10, 10, 10);
            editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            editText.setFilters(new InputFilter[] {new InputFilter.LengthFilter(2)});
            editText.setText(""+appliances_furniture.values().toArray()[i]);
            editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            editText.setTextColor(Color.BLACK);
            editText.setBackgroundResource(R.drawable.editbox);
            editText.setLayoutParams(param);


            if (id == 5)
            {
                linearLayout1 = new LinearLayout(PropertyDetails.this);
                linearLayout1.setOrientation(LinearLayout.HORIZONTAL);
                ll.addView(textView);
                ll.addView(editText);
                linearLayout1.addView(ll);
                linearLayout.addView(linearLayout1);
                id -= 4;
            }
            else
            {
                ll.addView(textView);
                ll.addView(editText);
                linearLayout1.addView(ll);
            }

            textView.setOnClickListener(PropertyDetails.this);
        }
        }

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final EditText object = new EditText(PropertyDetails.this);
                AlertDialog.Builder builder = new AlertDialog.Builder(PropertyDetails.this);
                builder.setMessage("Enter Appliance / Furniture Name");
                builder.setCancelable(true);
                builder.setView(object);
                builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (count < 10)
                        {
                            count++;
                            id++;
                            LinearLayout ll = new LinearLayout(PropertyDetails.this);
                            ll.setOrientation(LinearLayout.VERTICAL);

                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(222, LinearLayout.LayoutParams.WRAP_CONTENT);
                            params.weight = 1;
                            params.setMargins(15, 15, 15, 0);

                            TextView textView = new TextView(PropertyDetails.this);
                            textView.setId(count);
                            textView.setText(object.getText().toString().trim());
                            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                            textView.setTextColor(Color.BLACK);
                            textView.setBackgroundResource(R.drawable.textbox);
                            textView.setLayoutParams(params);

                            LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(222, LinearLayout.LayoutParams.WRAP_CONTENT);
                            param.weight = 1;
                            param.setMargins(15, 15, 15, 15);

                            EditText editText = new EditText(PropertyDetails.this);
                            editText.setId(count+100);
                            editText.setPadding(10, 10, 10, 10);
                            editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                            editText.setFilters(new InputFilter[] {new InputFilter.LengthFilter(2)});
                            editText.setText("0");
                            editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                            editText.setTextColor(Color.BLACK);
                            editText.setBackgroundResource(R.drawable.editbox);
                            editText.setLayoutParams(param);


                            if (id == 5)
                            {
                                linearLayout1 = new LinearLayout(PropertyDetails.this);
                                linearLayout1.setOrientation(LinearLayout.HORIZONTAL);
                                ll.addView(textView);
                                ll.addView(editText);
                                linearLayout1.addView(ll);
                                linearLayout.addView(linearLayout1);
                                id -= 4;
                            }
                            else
                            {
                                ll.addView(textView);
                                ll.addView(editText);
                                linearLayout1.addView(ll);
                            }

                            textView.setOnClickListener(PropertyDetails.this);
                        }
                        else
                        {
                            Toast.makeText(PropertyDetails.this, "Limit Exceeded", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                builder.show();
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String category = Category.getSelectedItem().toString();
                final String property_type = Property_type.getSelectedItem().toString();
                final String property_typeno = Property_typeno.getText().toString();
                final String area = Area.getText().toString();
                final String survey_Plot = Survey_Plot.getSelectedItem().toString();
                final String surveyno = Surveyno.getText().toString();
                final String usage = Usage.getSelectedItem().toString();
                final String buildingname = Buildingname.getText().toString();
                final String flatno = Flatno.getText().toString();
                final String floorno = Floorno.getText().toString();
                final String road = Road.getText().toString();
                final String location = Location.getText().toString();
                final String city = City.getText().toString();
                final String pincode = Pincode.getText().toString();
                final String state = State.getText().toString();
                hashMap_appliances.clear();

                for (int i = 1; i <= count; i++)
                {
                    Appliance = findViewById(i);
                    appliance = findViewById(i+100);
                    if (!appliance.getText().toString().isEmpty() && !appliance.getText().toString().equals("0"))
                    {
                        hashMap_appliances.put(Appliance.getText().toString(), Integer.valueOf(appliance.getText().toString().trim()));
                    }
                }

                if (category.isEmpty() || property_type.isEmpty() || property_typeno.isEmpty() || area.isEmpty() || survey_Plot.isEmpty() || surveyno.isEmpty()
                        || usage.isEmpty() || buildingname.isEmpty() || flatno.isEmpty() || floorno.isEmpty() || road.isEmpty() || location.isEmpty() || city.isEmpty()
                        || pincode.isEmpty() || state.isEmpty())
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(PropertyDetails.this);
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
                    AlertDialog.Builder builder = new AlertDialog.Builder(PropertyDetails.this);
                    builder.setMessage("Save & Continue ?");
                    builder.setCancelable(true);
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            if (!Bed.getText().toString().isEmpty() && !Bed.getText().toString().equals("0"))
                                hashMap_appliances.put("Bed",Integer.valueOf(Bed.getText().toString().trim()));
                            if (!Tv.getText().toString().isEmpty() && !Tv.getText().toString().equals("0"))
                                hashMap_appliances.put("TV",Integer.valueOf(Tv.getText().toString().trim()));
                            if (!Ac.getText().toString().isEmpty() && !Ac.getText().toString().equals("0"))
                                hashMap_appliances.put("AC",Integer.valueOf(Ac.getText().toString().trim()));
                            if (!Gyeser.getText().toString().isEmpty() && !Gyeser.getText().toString().equals("0"))
                                hashMap_appliances.put("Gyeser",Integer.valueOf(Gyeser.getText().toString().trim()));

                            hashMap.put("Category", category);
                            hashMap.put("Property_Type", property_type);
                            hashMap.put("Property_Typeno", Integer.valueOf(property_typeno));
                            hashMap.put("Area", Integer.valueOf(area));
                            hashMap.put("Survey_Plot", survey_Plot);
                            hashMap.put("Surveyno", Integer.valueOf(surveyno));
                            hashMap.put("Usage", usage);
                            hashMap.put("Buildingname", buildingname);
                            hashMap.put("Flatno", flatno);
                            hashMap.put("Floorno", Integer.valueOf(floorno));
                            hashMap.put("Road", road);
                            hashMap.put("Location", location);
                            hashMap.put("City", city);
                            hashMap.put("Pincode", Integer.valueOf(pincode));
                            hashMap.put("State", state);
                            hashMap.put("Appliances", hashMap_appliances);

                            Intent intent = new Intent(PropertyDetails.this, com.example.myrental.AgreementDetails.class);
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

    @Override
    public void onClick(View v) {

            final TextView textView = findViewById(v.getId());
            final EditText editText = new EditText(PropertyDetails.this);
            AlertDialog.Builder builder = new AlertDialog.Builder(PropertyDetails.this);
            builder.setMessage("Enter Appliance / Furniture Name");
            builder.setCancelable(true);
            builder.setView(editText);
            builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    textView.setText(editText.getText().toString().trim());
                }
            });
            builder.show();
    }
}
