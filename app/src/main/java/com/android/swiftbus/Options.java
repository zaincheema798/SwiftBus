package com.android.swiftbus;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class Options extends AppCompatActivity {

    Button login;
    Button signup;
    Button google;
    FirebaseAuth auth;
    SignInClient oneTapClient;

    // Launcher to handle the sign-in intent result.
    private final ActivityResultLauncher<IntentSenderRequest> signInLauncher =
            registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    try {
                        SignInCredential credential = oneTapClient.getSignInCredentialFromIntent(result.getData());
                        String idToken = credential.getGoogleIdToken();
                        if (idToken != null) {
                            //handle request to firebase
                            firebaseAuthWithGoogle(idToken);
                        } else {
                            Toast.makeText(this, "No ID token received", Toast.LENGTH_SHORT).show();
                        }
                    } catch (ApiException e) {
                        Toast.makeText(this, "Sign in failed", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Sign in cancelled", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);

        // Initialize Firebase Auth and One Tap client
        auth = FirebaseAuth.getInstance();
        oneTapClient = Identity.getSignInClient(this);

        login = findViewById(R.id.Login);
        signup = findViewById(R.id.Signup);
        google = findViewById(R.id.google);

        google.setOnClickListener(v -> signInWithGoogle());

        signup.setOnClickListener(v -> {
            Intent intent = new Intent(Options.this, Signup.class);
            startActivity(intent);
        });

        login.setOnClickListener(v -> {
            Intent intent = new Intent(Options.this, Login.class);
            startActivity(intent);
        });
    }

    private void signInWithGoogle() {
        BeginSignInRequest signInRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(
                        BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                                .setSupported(true)
                                .setServerClientId(getString(R.string.default_web_client_id))
                                .setFilterByAuthorizedAccounts(false)
                                .build())
                .build();

        oneTapClient.beginSignIn(signInRequest)
                .addOnSuccessListener(result -> {
                    IntentSenderRequest request = new IntentSenderRequest.Builder(
                            result.getPendingIntent().getIntentSender()).build();
                    signInLauncher.launch(request);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Sign in error, please try again", Toast.LENGTH_SHORT).show());
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        startActivity(new Intent(this, Main.class));
                        finish();
                    } else {
                        Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
