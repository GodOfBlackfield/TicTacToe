package com.example.tictactoe;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.UUID;

public class LoginFragment extends Fragment {
    private FirebaseAuth mAuth;
    private static final String TAG = "LoginStatus";
    private DatabaseReference dbr;
    private EditText email;
    private EditText password;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        dbr = FirebaseDatabase.getInstance().getReference("Accounts");
        // TODO if a user is logged in, go to Dashboard
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && !MainActivity.gameViewModel.isUserStatusChecked()) {
            NavDirections action = LoginFragmentDirections.actionLoginSuccessful();
            Navigation.findNavController(requireActivity(),R.id.loginFragment).navigate(action);
        }
        if (!MainActivity.gameViewModel.isUserStatusChecked()) {
            MainActivity.gameViewModel.setUserStatusChecked();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        email = view.findViewById(R.id.edit_email);
        password = view.findViewById(R.id.edit_password);
        updateUI();
        view.findViewById(R.id.btn_log_in)
                .setOnClickListener(v -> {
                    // TODO implement sign in logic
                    String e = email.getText().toString();
                    String p = password.getText().toString();
                    MainActivity.gameViewModel.setEmail(e);
                    MainActivity.gameViewModel.setPassword(p);
                    if (p.length() < 6) {
                        Toast.makeText(requireContext(),"Very weak password!",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String userName = e.substring(0, e.indexOf('@'));
                    Boolean isWrongUsername = userName.contains(".") || userName.contains("#") || userName.contains("$") || userName.contains("[") || userName.contains("]");
                    if (isWrongUsername) {
                        AlertDialog alertDialog = new AlertDialog.Builder(requireContext())
                                .setTitle("Warning")
                                .setMessage("Email contains ., #, $, [, ]")
                                .setNegativeButton("OK", null)
                                .show();
                    } else {
                        mAuth.fetchSignInMethodsForEmail(e).addOnCompleteListener(t -> {
                            boolean b = t.getResult().getSignInMethods().isEmpty();
                            if (b) {
                                mAuth.createUserWithEmailAndPassword(e,p).addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Log.d(TAG,"accountCreationAndSignInWithEmail:success");
                                        Toast.makeText(requireContext(),"Account Created!",Toast.LENGTH_SHORT).show();
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        MainActivity.host = new User(userName);
                                        try {
                                            dbr.child(userName).setValue(MainActivity.host);
                                        } catch (Exception ex) {
                                            Log.d("Error",ex.getMessage());
                                        }
                                        NavDirections action = LoginFragmentDirections.actionLoginSuccessful();
                                        Navigation.findNavController(v).navigate(action);
                                    } else {
                                        AlertDialog alertDialog = new AlertDialog.Builder(requireContext())
                                                .setTitle("Error")
                                                .setMessage(task.getException().getMessage())
                                                .setNegativeButton("OK", null)
                                                .show();
                                    }
                                });
                            } else {
                                mAuth.signInWithEmailAndPassword(e,p).addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Log.d(TAG,"signInWithEmail:success");
                                        Toast.makeText(requireContext(),"Signed In!",Toast.LENGTH_SHORT).show();
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        dbr.child(userName).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                MainActivity.host = snapshot.getValue(User.class);
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                                Log.d("Error",error.getMessage());
                                            }
                                        });
                                        NavDirections action = LoginFragmentDirections.actionLoginSuccessful();
                                        Navigation.findNavController(v).navigate(action);
                                    } else {
                                        AlertDialog alertDialog = new AlertDialog.Builder(requireContext())
                                                .setTitle("Error")
                                                .setMessage(task.getException().getMessage())
                                                .setNegativeButton("OK", null)
                                                .show();
                                    }
                                });
                            }
                        });
                    }
                });
        return view;
    }

    public void updateUI() {
        email.setText(MainActivity.gameViewModel.getEmail());
        password.setText(MainActivity.gameViewModel.getPassword());
    }

    // No options menu in login fragment.
}