package com.example.firebasealss;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    TextView fullName,email,phone,tvVerifyEmail;
    FirebaseAuth fAuth;
    Button btnResendVerificationLink, btnCamera;
    SwipeRefreshLayout swipeToRefresh;
    FirebaseFirestore fStore;
    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        phone = findViewById(R.id.profilePhone);
        fullName = findViewById(R.id.profileName);
        email    = findViewById(R.id.profileEmail);
        btnResendVerificationLink = findViewById(R.id.btnResendVerificationLink);
        tvVerifyEmail = findViewById(R.id.tvVerifyEmail);
        swipeToRefresh = findViewById(R.id.swipeToRefresh);
        btnCamera = findViewById(R.id.btnCamera);
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        userId = fAuth.getCurrentUser().getUid();

        /*//if user attempts to goto MainActivity without authentication, transition to Login activity and finish
        if (fAuth.getCurrentUser() == null){
            finish();
            startActivity(new Intent(this, Login.class));
        }*/

        //Swipe down gesture to refresh activity
        swipeToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                //Finish and create activity again
                finish();
                startActivity(getIntent());

                //Disable swipeToRefresh progressBar if it is active
                if (swipeToRefresh.isRefreshing()){
                    swipeToRefresh.setRefreshing(false);
                }
            }
        });

        try {
            //Fetch data from FireStore Collection Document
            DocumentReference documentReference = fStore.collection("users").document(userId);
            documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {

                    //This check is very very very important else app will crash when user logout
                    if (documentSnapshot != null){
                        phone.setText(documentSnapshot.getString("phone"));
                        email.setText(documentSnapshot.getString("email"));
                        fullName.setText(documentSnapshot.getString("fullName"));
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }


        //firebase authentication instance
        fAuth = FirebaseAuth.getInstance();

        //Set OnClickListener for button to be clicked
        btnResendVerificationLink.setOnClickListener(this);
        btnCamera.setOnClickListener(this);


        if (!fAuth.getCurrentUser().isEmailVerified()){
            btnResendVerificationLink.setVisibility(View.VISIBLE);
            tvVerifyEmail.setVisibility(View.VISIBLE);
        }
    }

    public void logout(View view) {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(MainActivity.this, Login.class));
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnResendVerificationLink:
                FirebaseUser firebaseUser = fAuth.getCurrentUser();
                firebaseUser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(MainActivity.this, "Verification link has been sent to your email", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("TAG", "onFailure: Email not sent"+ e.getMessage());
                    }
                });
                break;
            case R.id.btnCamera:
                startActivity(new Intent(this, CameraActivity.class));
                break;
            default:
                break;
        }

    }
}
