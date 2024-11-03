package com.example.tictactoe;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.Vector;

public class GameFragment extends Fragment {
    private static final String TAG = "GameFragment";
    private static final int GRID_SIZE = 9;
    private String gameType;
    private String pieceType1;
    private String pieceType2;
    private Button[] mButtons = new Button[GRID_SIZE];
    private NavController mNavController;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference lobbies;
    private DatabaseReference accounts;
    private Lobby lobby;
    private GameLogic gl;
    private int position;
    private TextView pTurn;
    private String userName;
    private User player;
    private String gameID;
    private String choice;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true); // Needed to display the action menu for this fragment
        gl = new GameLogic();
        firebaseDatabase = FirebaseDatabase.getInstance("https://tic-tac-toe-23172-default-rtdb.firebaseio.com");
        lobbies = firebaseDatabase.getReference("Lobbies");
        accounts = firebaseDatabase.getReference("Accounts");
        // Extract the argument passed with the action in a type-safe way
        GameFragmentArgs args = GameFragmentArgs.fromBundle(getArguments());
        if (args.getGameType().equals("One-Player") || args.getGameType().equals("Two-Player")) {
            Log.d(TAG, "New game type = " + args.getGameType());
            gameType = args.getGameType();
        } else {
            gameID = args.getGameType();
            gameType = args.getGameType();
        }

        // Handle the back press by adding a confirmation dialog
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Log.d(TAG, "Back pressed");

                // TODO show dialog only when the game is still in progress
                AlertDialog dialog = new AlertDialog.Builder(requireActivity())
                        .setTitle(R.string.confirm)
                        .setMessage(R.string.forfeit_game_dialog_message)
                        .setPositiveButton(R.string.yes, (d, which) -> {
                            // TODO update loss count
                            MainActivity.host.setLosses(MainActivity.host.getLosses() + 1);
                            accounts.child(MainActivity.host.getUsername()).setValue(MainActivity.host);
                            if (gameType.equals("One-Player")) {
                                nav();
                            }
                            else {
                                lobbies.child(gameID).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        Lobby lb = snapshot.getValue(Lobby.class);
                                        if (lb.permanentClose) {
                                            nav();
                                        } else if (gameType.equals("Two-Player")) {
                                            lb.pclose = 1;
                                        } else if (gameType.equals(gameID)) {
                                            lb.hclose = 1;
                                        }
                                        lb.hasLeft = true;
                                        lb.permanentClose = true;
                                        lobbies.child(gameID).setValue(lb);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Log.d("Error", error.getMessage());
                                    }
                                });
                            }
                        })
                        .setNegativeButton(R.string.cancel, (d, which) -> d.dismiss())
                        .create();
                dialog.show();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_game, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mNavController = Navigation.findNavController(view);
        pTurn = view.findViewById(R.id.playerTurn);
        mButtons[0] = view.findViewById(R.id.button0);
        mButtons[1] = view.findViewById(R.id.button1);
        mButtons[2] = view.findViewById(R.id.button2);
        mButtons[3] = view.findViewById(R.id.button3);
        mButtons[4] = view.findViewById(R.id.button4);
        mButtons[5] = view.findViewById(R.id.button5);
        mButtons[6] = view.findViewById(R.id.button6);
        mButtons[7] = view.findViewById(R.id.button7);
        mButtons[8] = view.findViewById(R.id.button8);
        if (gameType.equals("One-Player")) {
            AlertDialog dialog = new AlertDialog.Builder(requireActivity())
                    .setTitle("Piece Type")
                    .setMessage("Please choose X or O")
                    .setPositiveButton("Choose X", (d, which) -> {
                        pieceType1 = "X";
                        pieceType2 = "O";
                        gl.setCompPiece(pieceType2);
                    })
                    .setNegativeButton("Choose O", (d, which) -> {
                        pieceType1 = "O";
                        pieceType2 = "X";
                        gl.setCompPiece(pieceType2);
                    })
                    .create();
            dialog.show();
            for (int i = 0; i < mButtons.length; i++) {
                final int finalI = i;
                mButtons[i].setOnClickListener(v -> {
                    Log.d(TAG, "Button " + finalI + " clicked");
                    // TODO implement listeners
                    if (gameType.equals("One-Player")) {
                        gl.setBoard(finalI, pieceType1);
                        mButtons[finalI].setText(gl.getBoardPiece(finalI));
                        mButtons[finalI].setEnabled(false);
                        if (checkWin() == 0) {
                            NavDirections action = GameFragmentDirections.actionGameFragmentToDashboardFragment();
                            mNavController.navigate(action);
                            return;
                        }
                        try {
                            position = gl.playGame();
                            mButtons[position].setText(gl.getBoardPiece(position));
                            mButtons[position].setEnabled(false);
                        } catch (Exception e) {
                            Log.d("Error",e.getMessage());
                        }
                        if (checkWin() == 0) {
                            NavDirections action = GameFragmentDirections.actionGameFragmentToDashboardFragment();
                            mNavController.navigate(action);
                        }
                    }
                });
            }
        } else if (gameType.equals("Two-Player")) {
            AlertDialog dialog = new AlertDialog.Builder(requireActivity())
                    .setTitle("Two Player Game")
                    .setMessage("Please choose to create or join a lobby")
                    .setPositiveButton("Create", (d,which) -> {
                        twoPlayerCreate();
                    })
                    .setNegativeButton("Join", (d,which) -> {
                        nav();
                    })
                    .create();
            dialog.show();
        } else {
            lobbies.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    lobby = snapshot.getValue(Lobby.class);
                    if (lobby.gameID.equals(gameID)) {
                        pTurn.setText(lobby.host.getUsername()+"'s turn");
                        lobby.addPlayer(MainActivity.host);
                        lobby.isOpen = false;
                        lobbies.child(gameID).setValue(lobby);
                    }
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    lobby = snapshot.getValue(Lobby.class);
                    if (lobby.gameID.equals(gameID)) {
                        if (!lobby.isOpen && lobby.pclose == 0) {
                            for (int i = 0; i < 9; i++) {
                                if (!lobby.board.get(i).equals("")) {
                                    mButtons[i].setText(lobby.board.get(i));
                                    mButtons[i].setEnabled(false);
                                }
                            }
                            if (!lobby.turn) {
                                pTurn.setText(MainActivity.host.getUsername() + "'s turn");
                            }
                            if (lobby.checkhostwin()) {
                                MainActivity.host.setLosses(MainActivity.host.getLosses() + 1);
                                accounts.child(MainActivity.host.getUsername()).setValue(MainActivity.host);
                                lobby.hclose = 1;
                                lobby.pclose = 2;
                                lobby.permanentClose = true;
                                lobbies.child(lobby.gameID).setValue(lobby);
                                return;
                            } else if (lobby.checkplayerwin()) {
                                MainActivity.host.setWins(MainActivity.host.getWins() + 1);
                                accounts.child(MainActivity.host.getUsername()).setValue(MainActivity.host);
                                lobby.hclose = 2;
                                lobby.pclose = 1;
                                lobby.permanentClose = true;
                                lobbies.child(lobby.gameID).setValue(lobby);
                                return;
                            } else if (lobby.checkdraw()) {
                                MainActivity.host.setDraws(MainActivity.host.getDraws() + 1);
                                accounts.child(MainActivity.host.getUsername()).setValue(MainActivity.host);
                                lobby.hclose = 3;
                                lobby.pclose = 3;
                                lobby.permanentClose = true;
                                lobbies.child(lobby.gameID).setValue(lobby);
                                return;
                            }
                            for (int i = 0; i < 9; i++) {
                                final int pos = i;
                                mButtons[i].setOnClickListener(v -> {
                                    if (!lobby.turn) {
                                        mButtons[pos].setText("O");
                                        mButtons[pos].setEnabled(false);
                                        lobby.board.set(pos, "O");
                                        pTurn.setText(lobby.host.getUsername() + "'s turn");
                                        lobby.turn = true;
                                        lobbies.child(gameID).setValue(lobby);
                                    }
                                });
                            }
                            if (lobby.checkhostwin()) {
                                MainActivity.host.setLosses(MainActivity.host.getLosses() + 1);
                                accounts.child(MainActivity.host.getUsername()).setValue(MainActivity.host);
                                lobby.hclose = 1;
                                lobby.pclose = 2;
                                lobby.permanentClose = true;
                                lobbies.child(lobby.gameID).setValue(lobby);
                            } else if (lobby.checkplayerwin()) {
                                MainActivity.host.setWins(MainActivity.host.getWins() + 1);
                                accounts.child(MainActivity.host.getUsername()).setValue(MainActivity.host);
                                lobby.hclose = 2;
                                lobby.pclose = 1;
                                lobby.permanentClose = true;
                                lobbies.child(lobby.gameID).setValue(lobby);
                            } else if (lobby.checkdraw()) {
                                MainActivity.host.setDraws(MainActivity.host.getDraws() + 1);
                                accounts.child(MainActivity.host.getUsername()).setValue(MainActivity.host);
                                lobby.hclose = 3;
                                lobby.pclose = 3;
                                lobby.permanentClose = true;
                                lobbies.child(lobby.gameID).setValue(lobby);
                            }
                        } else if (lobby.pclose != 0) {
                            if (lobby.hasLeft) {
                                MainActivity.host.setWins(MainActivity.host.getWins() + 1);
                                accounts.child(MainActivity.host.getUsername()).setValue(MainActivity.host);
                            }
                            switch (lobby.pclose) {
                                case 1:
                                    display(0);
                                    break;
                                case 2:
                                    display(1);
                                    break;
                                case 3:
                                    display(2);
                                    break;
                                default:
                                    break;
                            }
                        }
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
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_logout, menu);
        // this action menu is handled in MainActivity
    }

    public void nav() {
        NavDirections action = GameFragmentDirections.actionGameFragmentToDashboardFragment();
        mNavController.navigate(action);
    }

    public int checkWin() {
        boolean b1 = (!mButtons[0].getText().toString().equals(""));
        boolean b2 = (!mButtons[1].getText().toString().equals(""));
        boolean b3 = (!mButtons[2].getText().toString().equals(""));
        boolean b4 = (!mButtons[3].getText().toString().equals(""));
        boolean b5 = (!mButtons[4].getText().toString().equals(""));
        boolean b6 = (!mButtons[5].getText().toString().equals(""));
        boolean b7 = (!mButtons[6].getText().toString().equals(""));
        boolean b8 = (!mButtons[7].getText().toString().equals(""));
        boolean b9 = (!mButtons[8].getText().toString().equals(""));
        boolean b = b1 && b2 && b3 && b4 && b5 && b6 && b7 && b8 && b9;
        boolean win = false,lose = false,draw = false;
        if (pieceType1.equals("X")) {
            win = gl.checkXWin();
            lose = gl.checkOWin();
        } else if (pieceType1.equals("O")){
            win = gl.checkOWin();
            lose = gl.checkXWin();
        }
        if (win) {
            MainActivity.host.setWins(MainActivity.host.getWins() + 1);
            accounts.child(MainActivity.host.getUsername()).setValue(MainActivity.host);
            AlertDialog.Builder dialog1 = new AlertDialog.Builder(requireActivity())
                    .setTitle("Congratulations!")
                    .setMessage("You have won the game!")
                    .setNeutralButton("Ok", (dialog, which) -> dialog.dismiss());
            dialog1.show();
            return 0;
        }
        else if (lose) {
            MainActivity.host.setLosses(MainActivity.host.getLosses() + 1);
            accounts.child(MainActivity.host.getUsername()).setValue(MainActivity.host);
            AlertDialog.Builder dialog1 = new AlertDialog.Builder(requireActivity())
                    .setTitle("Oh no!")
                    .setMessage("You have lost the game!")
                    .setNeutralButton("Ok", (dialog, which) -> dialog.dismiss());
            dialog1.show();
            return 0;
        }
        else if (b) {
            MainActivity.host.setDraws(MainActivity.host.getDraws() + 1);
            accounts.child(MainActivity.host.getUsername()).setValue(MainActivity.host);
            AlertDialog.Builder dialog1 = new AlertDialog.Builder(requireActivity())
                    .setTitle("Draw!")
                    .setMessage("You have drawn the game!")
                    .setNeutralButton("Ok", (dialog, which) -> dialog.dismiss());
            dialog1.show();
            return 0;
        }
        return 1;
    }

    public void display(int result) {
        switch (result) {
            case 0:
                AlertDialog d1 = new AlertDialog.Builder(requireActivity())
                        .setTitle("Congratulations!")
                        .setMessage("You have won the game!")
                        .setNeutralButton("OK", (dialog, which) -> {
                            nav();
                        })
                        .create();
                d1.show();
                break;
            case 1:
                AlertDialog d2 = new AlertDialog.Builder(requireActivity())
                        .setTitle("Oh no!")
                        .setMessage("You have lost the game!")
                        .setNeutralButton("OK", (dialog, which) -> {
                            nav();
                        })
                        .create();
                d2.show();
                break;
            case 2:
                AlertDialog d3 = new AlertDialog.Builder(requireActivity())
                        .setTitle("Draw!")
                        .setMessage("You have drawn the game!")
                        .setNeutralButton("OK", (dialog, which) -> {
                            nav();
                        })
                        .create();
                d3.show();
                break;
        }
    }

    private void twoPlayerCreate() {
        lobby = new Lobby(MainActivity.host);
        gameID = lobby.gameID;
        lobbies.child(lobby.gameID).setValue(lobby);
        ProgressDialog pd = new ProgressDialog(requireActivity());
        pd.setMessage("Getting Opponent...");
        pd.show();
        lobbies.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                lobby = snapshot.getValue(Lobby.class);
                if (lobby.gameID.equals(gameID)) {
                    if (!lobby.isOpen && lobby.hclose == 0) {
                        if (pd.isShowing()) {
                            pTurn.setText(MainActivity.host.getUsername() + "'s turn");
                            pd.dismiss();
                        }
                        for (int i = 0; i < 9; i++) {
                            if (!lobby.board.get(i).equals("")) {
                                mButtons[i].setText(lobby.board.get(i));
                                mButtons[i].setEnabled(false);
                            }
                        }
                        if (lobby.turn) {
                            pTurn.setText(MainActivity.host.getUsername() + "'s turn");
                        }
                        if (lobby.checkhostwin()) {
                            MainActivity.host.setWins(MainActivity.host.getWins() + 1);
                            accounts.child(MainActivity.host.getUsername()).setValue(MainActivity.host);
                            lobby.pclose = 2;
                            lobby.hclose = 1;
                            lobby.permanentClose = true;
                            lobbies.child(lobby.gameID).setValue(lobby);
                            return;
                        } else if (lobby.checkplayerwin()) {
                            MainActivity.host.setLosses(MainActivity.host.getLosses() + 1);
                            accounts.child(MainActivity.host.getUsername()).setValue(MainActivity.host);
                            lobby.pclose = 1;
                            lobby.hclose = 2;
                            lobby.permanentClose = true;
                            lobbies.child(lobby.gameID).setValue(lobby);
                            return;
                        } else if (lobby.checkdraw()) {
                            MainActivity.host.setDraws(MainActivity.host.getDraws() + 1);
                            accounts.child(MainActivity.host.getUsername()).setValue(MainActivity.host);
                            lobby.pclose = 3;
                            lobby.hclose = 3;
                            lobby.permanentClose = true;
                            lobbies.child(lobby.gameID).setValue(lobby);
                            return;
                        }
                        for (int i = 0; i < 9; i++) {
                            final int pos = i;
                            mButtons[i].setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (lobby.turn) {
                                        mButtons[pos].setText("X");
                                        mButtons[pos].setEnabled(false);
                                        lobby.board.set(pos, "X");
                                        pTurn.setText(lobby.player.getUsername() + "'s turn");
                                        lobby.turn = false;
                                        lobbies.child(lobby.gameID).setValue(lobby);
                                    }
                                }
                            });
                        }
                        if (lobby.checkhostwin()) {
                            MainActivity.host.setWins(MainActivity.host.getWins() + 1);
                            accounts.child(MainActivity.host.getUsername()).setValue(MainActivity.host);
                            lobby.pclose = 2;
                            lobby.hclose = 1;
                            lobby.permanentClose = true;
                            lobbies.child(lobby.gameID).setValue(lobby);
                        } else if (lobby.checkplayerwin()) {
                            MainActivity.host.setLosses(MainActivity.host.getLosses() + 1);
                            accounts.child(MainActivity.host.getUsername()).setValue(MainActivity.host);
                            lobby.pclose = 1;
                            lobby.hclose = 2;
                            lobby.permanentClose = true;
                            lobbies.child(lobby.gameID).setValue(lobby);
                        } else if (lobby.checkdraw()) {
                            MainActivity.host.setDraws(MainActivity.host.getDraws() + 1);
                            accounts.child(MainActivity.host.getUsername()).setValue(MainActivity.host);
                            lobby.pclose = 3;
                            lobby.hclose = 3;
                            lobby.permanentClose = true;
                            lobbies.child(lobby.gameID).setValue(lobby);
                        }
                    } else if (lobby.hclose != 0) {
                        if (lobby.hasLeft) {
                            MainActivity.host.setWins(MainActivity.host.getWins() + 1);
                            accounts.child(MainActivity.host.getUsername()).setValue(MainActivity.host);
                        }
                        switch (lobby.hclose) {
                            case 1:
                                display(0);
                                break;
                            case 2:
                                display(1);
                                break;
                            case 3:
                                display(2);
                                break;
                            default:
                                break;
                        }
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {}

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("Error", error.getMessage());
            }
        });
    }
}