package com.test.integertechtest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class ProfileDetails extends AppCompatActivity {

    TextView UserEmail,DisplayName,PhoneNumber;
    ImageView profile;
    Button logout;
    GoogleSignInClient mGoogleSignInClient;
    Bundle bundle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_details);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
       bundle = getIntent().getExtras();
       String email = bundle.getString("Email");
       String name = bundle.getString("Name");
       String photourl = bundle.getString("Image");
       UserEmail =  findViewById(R.id.email);
       DisplayName = findViewById(R.id.username);
       PhoneNumber = findViewById(R.id.phonenumber);
       profile = findViewById(R.id.profile);
       logout = findViewById(R.id.logout);
       UserEmail.setText(email);
       DisplayName.setText(name);

       Glide.with(ProfileDetails.this)
            .load(photourl)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(profile);

       logout.setOnClickListener(view -> {
        LoginManager.getInstance().logOut();
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, task -> Toast.makeText(ProfileDetails.this,"successfully logged out from the app",Toast.LENGTH_SHORT).show());
            Intent intent = new Intent(ProfileDetails.this,MainActivity.class);
            startActivity(intent);
            finish();
        });

    }

    @Override
    public void onBackPressed() {
       // super.onBackPressed();
        moveTaskToBack(true);
    }
}