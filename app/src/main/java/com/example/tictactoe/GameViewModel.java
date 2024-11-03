package com.example.tictactoe;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;

public class GameViewModel extends ViewModel {
    private String email;
    private String password;
    private ArrayList<String> board2;
    private String[] board1;
    private String turn;
    private boolean userStatusChecked;
    private boolean gameStarted;
    private boolean isSet;

    public GameViewModel() {
        email = "";
        password = "";
        board2 = new ArrayList<>(9);
        board1 = new String[9];
        for (int i = 0; i < 9; i++) {
            board1[i] = "";
        }
        userStatusChecked = false;
        gameStarted = false;
        for (int i = 0; i < 9; i++) {
            board2.add(i,"");
        }
        turn = "";
        isSet = false;
    }

    public String getEmail() {
        return email;
    }

    public ArrayList<String> getBoard2() {
        return board2;
    }

    public String getPassword() {
        return password;
    }

    public String getTurn() {
        return turn;
    }

    public boolean isSet() {
        return isSet;
    }

    public boolean isUserStatusChecked() {
        return userStatusChecked;
    }

    public String[] getBoard1() {
        return board1;
    }

    public boolean isGameStarted() {
        return gameStarted;
    }

    public void setBoard2(ArrayList<String> board2) {
        this.board2 = board2;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setTurn(String turn) {
        this.turn = turn;
    }

    public void setUserStatusChecked() {
        userStatusChecked = true;
    }

    public void setGameStarted() {
        gameStarted = true;
    }

    public void setBoard1(String[] board1) {
        this.board1 = board1;
    }

    public void setSet() {
        isSet = true;
    }
}
