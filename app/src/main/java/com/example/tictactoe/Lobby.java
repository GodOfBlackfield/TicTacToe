package com.example.tictactoe;

import java.util.ArrayList;
import java.util.UUID;

public class Lobby {
    public User host;
    public User player;
    public String gameID;
    public ArrayList<String> board;
    public boolean isOpen;
    public boolean turn;
    public int pclose;
    public int hclose;
    public boolean permanentClose;
    public boolean hasLeft;

    public Lobby() {}

    public Lobby(User host) {
        this.host = host;
        board = new ArrayList<>(9);
        for (int i = 0; i < 9; i++) {
            board.add(i,"");
        }
        isOpen = false;
        turn = true;
        player = null;
        permanentClose = false;
        hasLeft = false;
        pclose = 0;
        hclose = 0;
        gameID = UUID.randomUUID().toString();
    }

    public void addPlayer(User user) {
        player = user;
    }

    public boolean checkhostwin() {
        boolean b1 = (board.get(0).equals("X") && board.get(1).equals("X") && board.get(2).equals("X"));
        boolean b2 = (board.get(3).equals("X") && board.get(4).equals("X") && board.get(5).equals("X"));
        boolean b3 = (board.get(6).equals("X") && board.get(7).equals("X") && board.get(8).equals("X"));
        boolean b4 = (board.get(0).equals("X") && board.get(3).equals("X") && board.get(6).equals("X"));
        boolean b5 = (board.get(1).equals("X") && board.get(4).equals("X") && board.get(7).equals("X"));
        boolean b6 = (board.get(2).equals("X") && board.get(5).equals("X") && board.get(8).equals("X"));
        boolean b7 = (board.get(0).equals("X") && board.get(4).equals("X") && board.get(8).equals("X"));
        boolean b8 = (board.get(2).equals("X") && board.get(4).equals("X") && board.get(6).equals("X"));
        boolean b = (b1 || b2 || b3 || b4 || b5 || b6 || b7 || b8);
        return b;
    }

    public boolean checkplayerwin() {
        boolean b1 = (board.get(0).equals("O") && board.get(1).equals("O") && board.get(2).equals("O"));
        boolean b2 = (board.get(3).equals("O") && board.get(4).equals("O") && board.get(5).equals("O"));
        boolean b3 = (board.get(6).equals("O") && board.get(7).equals("O") && board.get(8).equals("O"));
        boolean b4 = (board.get(0).equals("O") && board.get(3).equals("O") && board.get(6).equals("O"));
        boolean b5 = (board.get(1).equals("O") && board.get(4).equals("O") && board.get(7).equals("O"));
        boolean b6 = (board.get(2).equals("O") && board.get(5).equals("O") && board.get(8).equals("O"));
        boolean b7 = (board.get(0).equals("O") && board.get(4).equals("O") && board.get(8).equals("O"));
        boolean b8 = (board.get(2).equals("O") && board.get(4).equals("O") && board.get(6).equals("O"));
        boolean b = (b1 || b2 || b3 || b4 || b5 || b6 || b7 || b8);
        return b;
    }

    public boolean checkdraw() {
        boolean b1 = !board.get(0).equals("");
        boolean b2 = !board.get(1).equals("");
        boolean b3 = !board.get(2).equals("");
        boolean b4 = !board.get(3).equals("");
        boolean b5 = !board.get(4).equals("");
        boolean b6 = !board.get(5).equals("");
        boolean b7 = !board.get(6).equals("");
        boolean b8 = !board.get(7).equals("");
        boolean b9 = !board.get(8).equals("");
        boolean b0 = (b1 && b2 && b3 && b4 && b5 && b6 && b7 && b8 && b9);
        boolean b = (b0 && !checkhostwin() && !checkplayerwin());
        return b;
    }
}
