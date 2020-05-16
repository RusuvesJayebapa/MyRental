package com.example.myrental;


import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        sharedPreferences = context.getSharedPreferences("Appkey", 0);
        editor = sharedPreferences.edit();
        editor.apply();
    }

    public void setLogin(boolean login)
    {
        editor.putBoolean("KEY_LOGIN",login);
        editor.commit();
    }

    public boolean getLogin()
    {
        return sharedPreferences.getBoolean("KEY_LOGIN", false);
    }

    public void setFirstname(String firstname)
    {
        editor.putString("KEY_FIRSTNAME",firstname);
        editor.commit();
    }

    public String getFirstname()
    {
        return sharedPreferences.getString("KEY_FIRSTNAME","");
    }

    public void setMiddlename(String middlename)
    {
        editor.putString("KEY_MIDDLENAME",middlename);
        editor.commit();
    }

    public String getMiddlename()
    {
        return sharedPreferences.getString("KEY_MIDDLENAME","");
    }

    public void setLastname(String lastname)
    {
        editor.putString("KEY_LASTNAME",lastname);
        editor.commit();
    }

    public String getLastname()
    {
        return sharedPreferences.getString("KEY_LASTNAME","");
    }

    public void setEmail(String email)
    {
        editor.putString("KEY_USEREMAIL",email);
        editor.commit();
    }

    public String getEmail()
    {
        return sharedPreferences.getString("KEY_USEREMAIL","");
    }

    public void setDOB(String dob)
    {
        editor.putString("KEY_USERDOB",dob);
        editor.commit();
    }

    public String getDOB()
    {
        return sharedPreferences.getString("KEY_USERDOB","");
    }

    public void setPhone(Long phone)
    {
        editor.putLong("KEY_USERPHONE",phone);
        editor.commit();
    }

    public Long getPhone()
    {
        return sharedPreferences.getLong("KEY_USERPHONE",0);
    }

    public void setBuildingname(String buildingname)
    {
        editor.putString("KEY_BUILDINGNAME",buildingname);
        editor.commit();
    }

    public String getBuildingname()
    {
        return sharedPreferences.getString("KEY_BUILDINGNAME","");
    }

    public void setFlatno(String phone)
    {
        editor.putString("KEY_FLATNO",phone);
        editor.commit();
    }

    public String getFlatno()
    {
        return sharedPreferences.getString("KEY_FLATNO","");
    }

    public void setFloorno(Integer floorno)
    {
        editor.putInt("KEY_FLOORNO",floorno);
        editor.commit();
    }

    public Integer getFloorno() { return sharedPreferences.getInt("KEY_FLOORNO",0); }

    public void setRoad(String road)
    {
        editor.putString("KEY_ROAD",road);
        editor.commit();
    }

    public String getRoad()
    {
        return sharedPreferences.getString("KEY_ROAD","");
    }

    public void setLocation(String location)
    {
        editor.putString("KEY_LOCATION",location);
        editor.commit();
    }

    public String getLocation()
    {
        return sharedPreferences.getString("KEY_LOCATION","");
    }

    public void setCity(String city)
    {
        editor.putString("KEY_CITY",city);
        editor.commit();
    }

    public String getCity()
    {
        return sharedPreferences.getString("KEY_CITY","");
    }

    public void setPincode(Integer pincode)
    {
        editor.putInt("KEY_PINCODE",pincode);
        editor.commit();
    }

    public Integer getPincode() { return sharedPreferences.getInt("KEY_PINCODE",0); }

    public void setState(String state)
    {
        editor.putString("KEY_STATE",state);
        editor.commit();
    }

    public String getState()
    {
        return sharedPreferences.getString("KEY_STATE","");
    }

    public void setDistrict(String district)
    {
        editor.putString("KEY_DISTRICT",district);
        editor.commit();
    }

    public String getDistrict()
    {
        return sharedPreferences.getString("KEY_DISTRICT", "");
    }
}
