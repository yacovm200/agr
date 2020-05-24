package com.noorpk.yacovapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.HashMap;
import java.util.Map;

public class Signup extends AppCompatActivity {

    private MaterialEditText fulname,username, email, password, c_password;
    private RadioGroup radioGroupGender;
    private Button btn_regist;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        fulname = findViewById(R.id.fulname);
        username = findViewById(R.id.username);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        c_password = findViewById(R.id.confirmPassword);
        radioGroupGender = (RadioGroup) findViewById(R.id.radioGender);
        btn_regist = findViewById(R.id.btn_regist);

        btn_regist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String txtfulname = fulname.getText().toString();
                String txtusername = username.getText().toString();
                String txtemail = email.getText().toString();
                String txtpassword = password.getText().toString();
                String txtc_password = c_password.getText().toString();
                String txtgender = ((RadioButton) findViewById(radioGroupGender.getCheckedRadioButtonId())).getText().toString();

                if(!validate(txtfulname, txtusername, txtemail, txtpassword, txtc_password)){
                    Toast.makeText(Signup.this, "Sign up failed", Toast.LENGTH_SHORT).show();
                } else{
                    RegistNewAccount(txtfulname, txtusername, txtemail, txtpassword, txtc_password, txtgender);
                }

            }
        });
    }

    private boolean validate(String txtfulname, String txtusername, String txtemail, String txtpassword, String txtc_password) {
        boolean vaild = true;

        if(txtfulname.isEmpty() || txtfulname.length() >35){
            this.fulname.setError("please enter Vaild full name");
            vaild=false;
        }
        if(txtusername.isEmpty()){
            this.fulname.setError("please enter Vaild user name");
            vaild=false;
        }
        if(txtemail.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(txtemail).matches()){
            this.email.setError("please enter Vaild Email Address");
            vaild=false;
        }
        if(txtpassword.isEmpty()){
            this.password.setError("please enter password");
            vaild=false;
        }
        if(txtc_password.isEmpty()){
            this.c_password.setError("please enter confirm password");
            vaild=false;
        }
        if(!txtpassword.equals(txtc_password)){
            this.c_password.setError("password are not matched");
            vaild=false;
        }

        return vaild;
    }

    private void RegistNewAccount(final String fulname, final String username, final String email, final String password, String c_password, final String gender) {
        final ProgressDialog progressDialog = new ProgressDialog(Signup.this);
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(false);
        progressDialog.setTitle("Registering New Account");
        String URL_REGIST = "https://kerron.xyz/htdocs/register.php";
        StringRequest request = new StringRequest(Request.Method.POST, URL_REGIST, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (response.equals("Successfully Registered")) {
                    progressDialog.dismiss();
                    Toast.makeText(Signup.this, response, Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(Signup.this, Login.class));
                    finish();
                } else {
                    progressDialog.dismiss();
                    Toast.makeText(Signup.this, response, Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                Toast.makeText(Signup.this, error.toString(), Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> param = new HashMap<>();
                param.put("fulname", fulname);
                param.put("username", username);
                param.put("email", email);
                param.put("password", password);
                param.put("gender", gender);

                return param;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(30000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT ));
        MySingleton.getInstance(Signup.this).addToRequestQueue(request);
    }
}


