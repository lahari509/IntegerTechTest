package com.test.integertechtest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    LoginButton  loginButton;
     GoogleSignInClient mGoogleSignInClient;
     private static final int RC_SIGN_IN = 1;
     public static String TAG = MainActivity.class.getName();
    CallbackManager callbackManager;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        findViewById(R.id.sign_in_button).setOnClickListener(MainActivity.this);
        callbackManager = CallbackManager.Factory.create();
        loginButton =   findViewById(R.id.login_button);
        loginButton.setPermissions("public_profile", "email");
        try {
            @SuppressLint("PackageManagerGetSignatures") PackageInfo info = getPackageManager().getPackageInfo(
                    "com.test.integertechtest",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException | NoSuchAlgorithmException e) {
            e.printStackTrace();

        }

        // Callback registration
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                getUserProfile(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onError(FacebookException exception) {
            }
        });
    }

    private void getUserProfile(AccessToken currentAccessToken) {
        GraphRequest request = GraphRequest.newMeRequest(
                currentAccessToken, (object, response) -> {
                    try {
                        String id = object.getString("id");
                        try {
                            URL profile_pic = new URL("https://graph.facebook.com/" + id + "/picture?width=50&height=50");
                            Log.i("profile_pic", profile_pic + "");
                            String f_name = object.getString("first_name");
                            String l_name = object.getString("last_name");
                            String name = f_name + " " + l_name;
                            String email = object.getString("email");
                            String image = profile_pic.toString();
                            Log.d("data", email + name + image);
                            updateUI(name,email,image);
                        } catch (MalformedURLException e) {
                           Log.e(TAG,"pic res"+e.getMessage()) ;
                        }


                    } catch (JSONException e) {

                        e.printStackTrace();

                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "first_name,last_name,email,id,picture");
        request.setParameters(parameters);
        request.executeAsync();
    }

    @Override
    public void onClick(View view) {
            if (view.getId() == R.id.sign_in_button) {
                signIn();
            }

    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                handleSignInResult(task);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
            }

        }
    }


    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            assert account != null;
            String emailId =   account.getEmail();
            Uri photoUrl = account.getPhotoUrl();
            String displayName = account.getDisplayName();
            Log.d(TAG,"emailId"+emailId);
            Log.d(TAG,"photourl"+photoUrl);
            updateUI(displayName, emailId, Objects.requireNonNull(photoUrl).toString());
        } catch (ApiException e) {
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        GoogleSignInAccount authCurrentUser = GoogleSignIn.getLastSignedInAccount(this);
        if(authCurrentUser ==null){
            Log.d(TAG,"no login");
        }
        else{
             updateUI(authCurrentUser.getDisplayName(), authCurrentUser.getEmail(), Objects.requireNonNull(authCurrentUser.getPhotoUrl()).toString());
        }
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        boolean isLoggedIn = accessToken != null && !accessToken.isExpired();
        if(isLoggedIn){
            getUserProfile(accessToken);
        }
        else{
            Log.d(TAG,"no login");
        }

    }
    public void updateUI(String name, String email, String image){

        Log.d(TAG,"updateui intent"+name+ "\n"+email+"\n"+image);
        Intent intent = new Intent(MainActivity.this,ProfileDetails.class);
        intent.putExtra("Email",email);
        intent.putExtra("Name",name);
        intent.putExtra("Image",image);
        startActivity(intent);

    }
}