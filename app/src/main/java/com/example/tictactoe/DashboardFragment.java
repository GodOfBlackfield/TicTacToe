package com.example.tictactoe;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Collections;
import java.util.List;
import java.util.Vector;

public class DashboardFragment extends Fragment {

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private static final String TAG = "DashboardFragment";
    private DatabaseReference lobbies;
    private DatabaseReference accounts;
    private NavController mNavController;
    private Vector<Lobby> lobbyList = new Vector<>();
    private List<Lobby> lobbyList1;
    private ProgressDialog progressDialog;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public DashboardFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        lobbies = FirebaseDatabase.getInstance().getReference("Lobbies");
        accounts = FirebaseDatabase.getInstance().getReference("Accounts");
        setHasOptionsMenu(true); // Needed to display the action menu for this fragment
    }

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        OpenGamesAdapter adapter = new OpenGamesAdapter(requireContext());
        lobbies.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Lobby lb = dataSnapshot.getValue(Lobby.class);
                    if (!lb.permanentClose || lb.isOpen) {
                        lobbyList.add(dataSnapshot.getValue(Lobby.class));
                    }
                }
                lobbyList1 = Collections.list(lobbyList.elements());
                adapter.setAccounts(lobbyList1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("Error",error.getMessage());
            }
        });
        RecyclerView recyclerView = view.findViewById(R.id.list);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mNavController = Navigation.findNavController(view);
        TextView placeholder = view.findViewById(R.id.txt_score);
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            NavDirections action = DashboardFragmentDirections.actionNeedAuth();
            mNavController.navigate(action);
            return;
        } else {
            progressDialog = new ProgressDialog(requireActivity());
            progressDialog.setMessage("Loading...");
            progressDialog.show();
        }
        String e = mUser.getEmail();
        Log.d("email",e);
        String userName = e.substring(0,e.indexOf('@'));
        accounts.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.getKey().equals(userName)) {
                    MainActivity.host = snapshot.getValue(User.class);
                    placeholder.setText(MainActivity.host.getUsername()+"\n"+"Wins:"+String.valueOf(MainActivity.host.getWins())+"|Losses:"+String.valueOf(MainActivity.host.getLosses())+"|Draws:"+String.valueOf(MainActivity.host.getDraws()));
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.getKey().equals(MainActivity.host.getUsername())) {
                    MainActivity.host = snapshot.getValue(User.class);
                    placeholder.setText(MainActivity.host.getUsername()+"\n"+"Wins:"+String.valueOf(MainActivity.host.getWins())+"|Losses:"+String.valueOf(MainActivity.host.getLosses())+"|Draws:"+String.valueOf(MainActivity.host.getDraws()));
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {}

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("Error",error.getMessage());
            }
        });

        // TODO if a user is not logged in, go to LoginFragment
        // Show a dialog when the user clicks the "new game" button
        view.findViewById(R.id.fab_new_game).setOnClickListener(v -> {

            // A listener for the positive and negative buttons of the dialog
            DialogInterface.OnClickListener listener = (dialog, which) -> {
                String gameType = "No type";
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    gameType = getString(R.string.two_player);
                } else if (which == DialogInterface.BUTTON_NEGATIVE) {
                    gameType = getString(R.string.one_player);
                }
                Log.d(TAG, "New Game: " + gameType);

                // Passing the game type as a parameter to the action
                // extract it in GameFragment in a type safe way
                NavDirections action = DashboardFragmentDirections.actionGame(gameType);
                mNavController.navigate(action);
            };

            // create the dialog
            AlertDialog dialog = new AlertDialog.Builder(requireActivity())
                    .setTitle(R.string.new_game)
                    .setMessage(R.string.new_game_dialog_message)
                    .setPositiveButton(R.string.two_player, listener)
                    .setNegativeButton(R.string.one_player, listener)
                    .setNeutralButton(R.string.cancel, (d, which) -> d.dismiss())
                    .create();
            dialog.show();
        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_logout, menu);
        // this action menu is handled in MainActivity
    }
}