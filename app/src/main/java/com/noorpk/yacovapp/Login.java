package com.noorpk.yacovapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Login extends AppCompatActivity {
    private MaterialEditText email, password;
    private Button login, register ;
    private CheckBox LoginState;
    private SharedPreferences sharedPreferences;



    CallbackManager callbackManager;
    private static final String EMAIL = "email";
    LoginButton loginButton;
    GoogleSignInClient mGoogleSignInClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        //GoogleSignInOptions gso = new GoogleSignInOptions();

        mGoogleSignInClient = GoogleSignIn.getClient(Login.this, gso);

        SignInButton signInButton = findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, 89);
            }
        });

        callbackManager = CallbackManager.Factory.create();
        loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions(Arrays.asList(EMAIL));

        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        boolean isLoggedIn = accessToken != null && !accessToken.isExpired();
        if (isLoggedIn){
            LoginManager.getInstance().logOut();
            //Toast.makeText(this, "User is already logged in", Toast.LENGTH_SHORT).show();
/*            GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                @Override
                public void onCompleted(JSONObject object, GraphResponse response) {
                    Log.d("jkjk", object.toString());

                }
            }).executeAsync();*/
        }

        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

                GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        //uplad_data to facebook API
                        try {
                            facebook_api(object.getString("name"), object.getString("id"));

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                }).executeAsync();



                /*
                *
                *                                     String id = me.optString("id");
                                    String email=me.optString("email");
                                    String name= me.optString("name");
                                    String gender= me.optString("gender");
                                    String verified= me.optBoolean("is_verified") + "";*/

                //Log.d("result", loginResult)
                //Toast.makeText(Login.this, "Success", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancel() {
                Toast.makeText(Login.this, "Cancel", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(Login.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Add code to print out the key hash
/*        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.noorpk.yacovapp",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }*/



        sharedPreferences = getSharedPreferences("UserInfo" , Context.MODE_PRIVATE);

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        LoginState = findViewById(R.id.checkbox);
        login = findViewById(R.id.login);
        register = findViewById(R.id.register);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Login.this, Signup.class));
                finish();
            }
        });
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String txtemail = email.getText().toString();
                String txtpassword = password.getText().toString();

                if(!validateMailAndPassword(txtemail, txtpassword)){
                    Toast.makeText(Login.this, "Login failed", Toast.LENGTH_SHORT).show();
                } else{
                    login(txtemail, txtpassword);
                }
            }
        });

        String loginStatus = sharedPreferences.getString(getResources().getString(R.string.prefLoginState) , "");
        if(loginStatus.equals("loggedin")){
            startActivity(new Intent(Login.this, MainActivity.class));
        }

    }

    private boolean validateMailAndPassword(String txtemail, String txtpassword) {
        boolean vaild = true;
        if(txtemail.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(txtemail).matches()){
            this.email.setError("please enter Vaild Email Address");
            vaild=false;
        }
        if(txtpassword.isEmpty()){
            this.password.setError("please enter password");
            vaild=false;
        }
        return vaild;
    }

    private void login (final String email, final String password){
        final ProgressDialog progressDialog = new ProgressDialog(Login.this);
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(false);
        progressDialog.setTitle("Registering New Account");
        String URL_REGIST = "https://kerron.xyz/htdocs/login.php";
        StringRequest request = new StringRequest(Request.Method.POST, URL_REGIST, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if(response.equals("Login success")){
                    progressDialog.dismiss();
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    if(LoginState.isChecked()){
                        editor.putString(getResources().getString(R.string.prefLoginState),"loggedin" );
                    }else {
                        editor.putString(getResources().getString(R.string.prefLoginState),"loggedout" );
                    }
                    editor.apply();

                    SharedPreferences preferences = getSharedPreferences("user", MODE_PRIVATE);
                    SharedPreferences.Editor edit = preferences.edit();
                    edit.putString("id", ((EditText) findViewById(R.id.email)).getText().toString() );
                    edit.apply();

                    startActivity(new Intent(Login.this, MainActivity.class));

                } else {
                    progressDialog.dismiss();
                    Toast.makeText(Login.this, response, Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                Toast.makeText(Login.this, error.toString(), Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> param = new HashMap<>();
                param.put("email", email);
                param.put("password", password);

                return param;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(30000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT ));
        MySingleton.getInstance(Login.this).addToRequestQueue(request);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode!=89){
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }else {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account!=null){
                google_api(account.getDisplayName(),account.getId(), account.getEmail());
            }
            // Signed in successfully, show authenticated UI.
            //updateUI(account);
        } catch (ApiException e) {
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            //Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            //updateUI(null);
        }
    }

    void facebook_api(final String name, final String id){

        final ProgressDialog dialog = new ProgressDialog(Login.this);
        dialog.setMessage("Loading...");
        dialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, "https://kerron.xyz/htdocs/facebook.php", new Response.Listener<String>() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onResponse(String result) {
                Toast.makeText(Login.this, ""+result, Toast.LENGTH_SHORT).show();
                Log.d("result", result);
                dialog.dismiss();
                if (result.equals("0")){
                    Toast.makeText(Login.this, "Some error occur", Toast.LENGTH_SHORT).show();
                }else{
                    dialog.dismiss();

                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    if(LoginState.isChecked()){
                        editor.putString(getResources().getString(R.string.prefLoginState),"loggedin" );
                    }else {
                        editor.putString(getResources().getString(R.string.prefLoginState),"loggedout" );
                    }
                    editor.apply();

                    SharedPreferences preferences = getSharedPreferences("user", MODE_PRIVATE);
                    SharedPreferences.Editor edit = preferences.edit();
                    edit.putString("id", result);
                    edit.apply();
                    startActivity(new Intent(Login.this, MainActivity.class));
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                dialog.dismiss();
                Toast.makeText(Login.this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("id", id);
                params.put("name", name);
                return params;
            }
        };

        stringRequest.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 30000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 30000;
            }

            @Override
            public void retry(VolleyError volleyError) throws VolleyError {

            }
        });
        MySingleton.getInstance(Login.this).addToRequestQueue(stringRequest);
    }



    void google_api(final String name, final String id, final String email){

        final ProgressDialog dialog = new ProgressDialog(Login.this);
        dialog.setMessage("Loading...");
        dialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, "https://kerron.xyz/htdocs/googleLogin.php", new Response.Listener<String>() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onResponse(String result) {
                Toast.makeText(Login.this, ""+result, Toast.LENGTH_SHORT).show();
                Log.d("result", result);
                dialog.dismiss();
                if (result.equals("0")){
                    Toast.makeText(Login.this, "Some error occur", Toast.LENGTH_SHORT).show();
                }else{

                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    if(LoginState.isChecked()){
                        editor.putString(getResources().getString(R.string.prefLoginState),"loggedin" );
                    }else {
                        editor.putString(getResources().getString(R.string.prefLoginState),"loggedout" );
                    }
                    editor.apply();

                    SharedPreferences preferences = getSharedPreferences("user", MODE_PRIVATE);
                    SharedPreferences.Editor edit = preferences.edit();
                    edit.putString("id", result);
                    edit.apply();
                    startActivity(new Intent(Login.this, MainActivity.class));
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                dialog.dismiss();
                Toast.makeText(Login.this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("id", id);
                params.put("name", name);
                params.put("email", email);
                return params;
            }
        };

        stringRequest.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 30000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 30000;
            }

            @Override
            public void retry(VolleyError volleyError) throws VolleyError {

            }
        });
        MySingleton.getInstance(Login.this).addToRequestQueue(stringRequest);
    }
}









