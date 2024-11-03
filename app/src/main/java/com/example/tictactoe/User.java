package com.example.tictactoe;

public class User {
    private String username;
    private int wins;
    private int losses;
    private int draws;

    public User() {
        username = "";
        wins = 0;
        losses = 0;
        draws = 0;
    }

    public User(String username) {
        this.username = username;
        wins = 0;
        losses = 0;
        draws = 0;
    }

    public String getUsername() {
        return username;
    }

    public int getDraws() {
        return draws;
    }

    public int getLosses() {
        return losses;
    }

    public int getWins() {
        return wins;
    }

    public void setDraws(int draws) {
        this.draws = draws;
    }

    public void setLosses(int losses) {
        this.losses = losses;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }
}
