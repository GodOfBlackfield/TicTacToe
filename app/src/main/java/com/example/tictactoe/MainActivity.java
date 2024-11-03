package com.example.tictactoe;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private NavController ctrl;
    private static final String TAG = "MainActivity";
    public static User host;
    public static GameViewModel gameViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        gameViewModel = new ViewModelProvider(this).get(GameViewModel.class);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_logout) {
            Log.d(TAG, "logout clicked");
            NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
            NavController navController = navHostFragment.getNavController();
            if (navController.getCurrentDestination().getId() == R.id.gameFragment) {
                AlertDialog alertDialog = new AlertDialog.Builder(navHostFragment.requireContext())
                        .setTitle("Warning")
                        .setMessage("You cannot logout in the middle of a game")
                        .setNegativeButton("OK", null)
                        .show();
            }
            else if (navController.getCurrentDestination().getId() == R.id.dashboardFragment) {
                FirebaseAuth.getInstance().signOut();
                NavDirections action = DashboardFragmentDirections.actionNeedAuth();
                navController.navigate(action);
            }
            // TODO handle log out
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
