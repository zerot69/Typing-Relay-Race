/*
Typing Relay Race (Typera)
Author: github.com/zerot69 & github.com/baorhieu078
Using TCP Server to make a Typing Race game between 4 players (divided into 2 teams)
 */

package com.typera;

public class User {
    protected String password;
    protected String name;
    public User(String password, String name) {
        this.password = password;
        this.name = name;
    }
}