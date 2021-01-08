/*
Typing Relay Race (Typera)
Author: github.com/zerot69 & github.com/baorhieu078
Using TCP Server to make a Typing Race game between 4 players (divided into 2 teams)
 */

package com.typera;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Game {

    public static void mainMenu(){

        //Menu options
        System.out.println();
        System.out.println("Menu:");
        System.out.println("1)  Login");
        System.out.println("2)  Register");
        System.out.println("x)  Quit");
        System.out.print("Input your option (1,2 or x): ");
    }

    public static void gameMenu() {

        //Menu options
        System.out.println("\nGame Menu:");
        System.out.println("1)  Multiplayer");
        System.out.println("2)  Single-player");
        System.out.println("x)  Quit");
        System.out.print("Input your option (1,2 or x): ");

    }

    public static void countdown() {

        //Countdown from 3
        int i,j;
        System.out.println("\nREADY!");
        for (i=3; i>0; i--){
            System.out.print(i);
            for (j=0; j<3;j++){
                System.out.print(".");
                try {
                    TimeUnit.MILLISECONDS.sleep(333);
                } catch (InterruptedException e){
                    e.printStackTrace();
                }

            }
        }
        System.out.println("\nGO!");

    }

    public static String randomize() {

        //Generate word_list
        String path = "..//Project1//wordlist.txt";
        String contents = null;
        try {
            contents = new String(Files.readAllBytes(Paths.get(path)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert contents != null;

        String[] word_list = contents.split("\\r?\\n");

        //Print out 'max_char' characters (including spaces)
        Random rand = new Random();
        int max_char = 80;
        StringBuilder word_list_rand = new StringBuilder();
        while (word_list_rand.toString().trim().length() < max_char) {
            String word = word_list[rand.nextInt(word_list.length)];
            word_list_rand.append(word).append(" ");
        }

        return word_list_rand.toString().trim();

    }

    public static double accuracy(String word_list_rand,String typed) {

        //Create arrays
        String[] typed_list = typed.trim().split("\\s+",-1);
        String[] given_list = word_list_rand.trim().split("\\s+", -1);

        //Accuracy
        int corrected_char = 0;
        int total_char = word_list_rand.length();
        int i_max = given_list.length - Math.abs(given_list.length - typed_list.length);
        for (int i=0; i< i_max; i++){
            int j_max = given_list[i].length() - Math.abs(given_list[i].length() - typed_list[i].length());
            for (int j=0; j< j_max; j++){
                if (given_list[i].charAt(j) == typed_list[i].charAt(j)){
                    corrected_char++;
                }
            }
        }
        corrected_char = corrected_char + typed_list.length - 1; // Adding space into corrected_char

        return ((double) (corrected_char)) / ((double) total_char) * 100;

    }

}
