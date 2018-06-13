package com.newsapp.rohit.singlesignon2.activity;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.newsapp.rohit.singlesignon2.R;
import com.newsapp.rohit.singlesignon2.utlis.Constants;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences mSharedPreferences;

    String key = "";

    private TextView mTextView;
    private ImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = (TextView)findViewById(R.id.textView);
        mImageView = (ImageView)findViewById(R.id.imageView);

        try {
            Context context =  createPackageContext
                    (Constants.PACKAGE,MODE_PRIVATE);
            mSharedPreferences = context.getSharedPreferences(Constants.PREFERENCE_NAME,context.MODE_PRIVATE);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if (!checkPermissionForExtertalStorage()){
            try {
                requestPermissionForExtertalStorage();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public boolean checkPermissionForExtertalStorage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int result = this.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
            return result == PackageManager.PERMISSION_GRANTED;
        }
        return false;
    }

    public void requestPermissionForExtertalStorage() throws Exception {
        try {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]
                    {Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    protected void onResume() {
        readFile();
        super.onResume();
    }

    private void readFile() {
        File root = new File(Environment.getExternalStorageDirectory(), "SSO");

        //Get the text file
        File file = new File(root,"credentials.txt");

        //Read text from file
        StringBuilder text = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();
        }
        catch (IOException e) {
            Log.e("IOException" , e.toString());
        }


        try {
            key = mSharedPreferences.getString(Constants.KEY,"");

            String value = decryptText(String.valueOf(text),key);

            if (value.equals("null")) {
                mImageView.setImageResource(R.drawable.wrong);
                mTextView.setText("You need to Log in first from SSO1 app !");
            }else {
                mImageView.setImageResource(R.drawable.check);
                mTextView.setText("You are successfully logged in ! Your Username and Password are mentioned below\n\n" + value);
            }

        }catch (NullPointerException e) {

            mImageView.setImageResource(R.drawable.wrong);
            mTextView.setText("You need to Log in first from SSO1 app !");
        }

    }

    private String decryptText(String data, String pass) {
        SecretKeySpec key = generateKey(pass);

        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE,key);
            byte[] decodedValue = Base64.decode(data,Base64.DEFAULT);
            byte[] decValue = cipher.doFinal(decodedValue);
            String decryptedValue = new String(decValue);

            return decryptedValue;

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }

        return null;
    }

    private SecretKeySpec generateKey(String password) {
        try {
            final MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = password.getBytes("UTF-8");
            messageDigest.update(bytes,0,bytes.length);
            byte[] key = messageDigest.digest();
            return new SecretKeySpec(key,"AES");

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            Log.e("NoSuchAlgorithm" , e.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            Log.e("EncodingException", e.toString());
        }

        return null;
    }
}
