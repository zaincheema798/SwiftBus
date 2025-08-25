package com.android.swiftbus;

import static com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL;

import android.content.Intent;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;

import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.Executors;

public class Login extends AppCompatActivity {

    private static final String TAG = "Login";

    private Button login;
    private EditText email, password;
    private FirebaseAuth auth;
    private Button googleSignInButton;
    private CredentialManager credentialManager;
    private CancellationSignal cancellationSignal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize views
        login = findViewById(R.id.login);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        googleSignInButton = findViewById(R.id.google);

        // Initialize Firebase Auth and Credential Manager
        auth = FirebaseAuth.getInstance();
        credentialManager = CredentialManager.create(this);

        // Set click listeners
        googleSignInButton.setOnClickListener(v -> signInWithGoogle());
        login.setOnClickListener(v -> signInWithEmail());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cancel any ongoing credential operations
        if (cancellationSignal != null && !cancellationSignal.isCanceled()) {
            cancellationSignal.cancel();
        }
    }

    @Override
    public void onBackPressed() {
        // Cancel credential operation if in progress
        if (cancellationSignal != null && !cancellationSignal.isCanceled()) {
            cancellationSignal.cancel();
        }
        super.onBackPressed();
    }

    private void signInWithEmail() {
        String emailText = email.getText().toString().trim();
        String passwordText = password.getText().toString().trim();

        if (emailText.isEmpty() || passwordText.isEmpty()) {
            Toast.makeText(Login.this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.signInWithEmailAndPassword(emailText, passwordText)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        navigateToMainActivity();
                    } else {
                        String errorMessage = task.getException() != null ?
                                task.getException().getMessage() : "Authentication failed";
                        Toast.makeText(Login.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void signInWithGoogle() {


        // Create new cancellation signal
        cancellationSignal = new CancellationSignal();

        // Create Google ID option
        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(getString(R.string.default_web_client_id))
                .build();

        // Create the Credential Manager request
        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        // Launch Credential Manager
        credentialManager.getCredentialAsync(
                this,
                request,
                cancellationSignal,
                Executors.newSingleThreadExecutor(),
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse result) {
                        // Check if operation was cancelled
                        if (cancellationSignal.isCanceled()) {
                            return;
                        }
                        handleSignIn(result.getCredential());
                    }

                    @Override
                    public void onError(@NonNull GetCredentialException exception) {
                        // Check if operation was cancelled
                        if (cancellationSignal.isCanceled()) {
                            Log.d(TAG, "Google Sign-In cancelled by user");
                            return;
                        }

                        Log.e(TAG, "Google Sign-In failed: " + exception.getMessage());
                        runOnUiThread(() -> {
                            // Check if activity is still valid before showing toast
                            if (!isFinishing() && !isDestroyed()) {
                                Toast.makeText(Login.this, "Google Sign-In failed", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
        );
    }

    private void handleSignIn(Credential credential) {
        // Check if operation was cancelled
        if (cancellationSignal != null && cancellationSignal.isCanceled()) {
            return;
        }

        // Check if credential is of type Google ID
        if (credential instanceof CustomCredential customCredential
                && credential.getType().equals(TYPE_GOOGLE_ID_TOKEN_CREDENTIAL)) {
            try {
                // Create Google ID Token
                Bundle credentialData = customCredential.getData();
                GoogleIdTokenCredential googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credentialData);

                // Sign in to Firebase using the token
                firebaseAuthWithGoogle(googleIdTokenCredential.getIdToken());
            } catch (Exception e) {
                Log.e(TAG, "Error creating Google ID Token: " + e.getMessage());
                runOnUiThread(() -> {
                    if (!isFinishing() && !isDestroyed()) {
                        Toast.makeText(Login.this, "Authentication error", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } else {
            Log.w(TAG, "Credential is not of type Google ID!");
            runOnUiThread(() -> {
                if (!isFinishing() && !isDestroyed()) {
                    Toast.makeText(Login.this, "Invalid credential type", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success
                        Log.d(TAG, "signInWithCredential:success");
                        if(task.getResult().getAdditionalUserInfo().isNewUser()) {
                            FirebaseUser user = auth.getCurrentUser();
                            String nameText = user.getDisplayName();
                            String emailText = user.getEmail();

                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            db.collection("Users")
                                    .document(auth.getCurrentUser().getUid())
                                    .set(new UserProfile(null, nameText, null, emailText, null, null, null, null, null, 0, 0, 0));

                        }
                        navigateToMainActivity();
                    } else {
                        // Sign in failed
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        String errorMessage = task.getException() != null ?
                                task.getException().getMessage() : "Firebase authentication failed";
                        Toast.makeText(Login.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void navigateToMainActivity() {
        startActivity(new Intent(this, Main.class));
        finish();
    }
}