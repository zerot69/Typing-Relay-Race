/*
Typing Relay Race (Typera)
Author: github.com/zerot69 & github.com/baorhieu078
Using TCP Server to make a Typing Race game between 4 players (divided into 2 teams)
 */

package com.typera;

import java.io.*;
import java.time.LocalTime;
import java.util.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Server class
public class ServerTCP {

    static Vector<ClientHandler> ACTIVE_CLIENTS = new Vector<>();       //Vector to store active clients
    static Vector<Multiplay> MULTIPLAYER_CLIENTS = new Vector<>();      //Vector to store multiplayer clients
    static Map<String, User> usersList = new ConcurrentHashMap<>();     //Hashmap to store usernames and passwords
    static Map<Socket, String> userTyped = new ConcurrentHashMap<>();   //Hashmap to store String users typed in multiplayer games
    static Map<Socket, String> userReady = new ConcurrentHashMap<>();   //Hashmap to store String users ready status in multiplayer games

    //Counter for clients
    static int i = 0;

    public static void main(String[] args) throws IOException {

        System.out.println("SERVER IS ONLINE!");

        //Server is listening on port 6969
        ServerSocket serverSocket = new ServerSocket(6969);
        System.out.println("Waiting for connections...");

        Socket socket;

        while (true) {

            // Accept the incoming request
            socket = serverSocket.accept();

            System.out.println("New connection: " + socket);

            //Obtain input and output streams
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

            System.out.println("Creating a new handler for this client");

            // Create a new handler object for handling this request.
            ClientHandler request = new ClientHandler(socket,"Client " + i, dis, dos);

            // Create a new Thread with this object.
            Thread t = new Thread(request);

            System.out.println("Adding this client to active client list");

            // Add this client to active clients list
            ACTIVE_CLIENTS.add(request);

            // Start the thread.
            t.start();

            // Increment i for new client.
            i++;
            System.out.println("Total client(s): " + ACTIVE_CLIENTS.size());

        }
    }
}

// ClientHandler class
class ClientHandler implements Runnable {

    private String name;
    final DataInputStream dis;
    final DataOutputStream dos;
    Socket s;
    boolean isConnected, isPlaying, returnMenu, isReady;
    String username, password;
    User user;

    //Constructor
    public ClientHandler(Socket s, String name, DataInputStream dis, DataOutputStream dos) {
        this.dis = dis;
        this.dos = dos;
        this.name = name;
        this.s = s;
        this.isConnected = true;
    }

    @Override
    public void run() {

        String requested = "";
        String requested_isLoggedIn = "";
        while (true) {
            try {
                while (requested.compareTo("x") !=0) {

                    //Get message from client
                    requested = dis.readUTF().trim();
                    System.out.println("Client (" + s.getInetAddress() + ":" + s.getPort() + ") sent: " + requested);

                    //////////////////////////
                    // Login
                    if (requested.equals("1")){

                        boolean loginSucceeded = false;

                        while (!loginSucceeded && !returnMenu){
                            dos.writeUTF("\nLogging in. You can type 'return' in the 'Username' field to return to main menu.");
                            dos.writeUTF("Username: ");
                            username = dis.readUTF().trim();
                            if (username.equals("return")) {
                                dos.writeUTF("\nReturn to main menu!");
                                returnMenu = true;
                            } else {
                                User userLogin = ServerTCP.usersList.get(username);
                                dos.writeUTF("Password: ");
                                password = dis.readUTF().trim();
                                if (userLogin != null) {
                                    if (!password.equals(userLogin.password)) {
                                        dos.writeUTF("\nWrong username or password! Please try again! Or type 'return' to return to main menu.");
                                    } else {
                                        dos.writeUTF("\nWelcome back, " + userLogin.name + "!");
                                        this.name = username;
                                        loginSucceeded = true;
                                    }
                                } else {
                                    dos.writeUTF("\nWrong username or password! Please try again! Or type 'return' to return to main menu.");
                                }
                            }
                        }
                        if (loginSucceeded){

                            while (requested_isLoggedIn.compareTo("x") !=0) {

                                //Get message from client
                                requested_isLoggedIn = dis.readUTF().trim();
                                isReady = false;
                                isPlaying = false;

                                for (int i=0; i<ServerTCP.MULTIPLAYER_CLIENTS.size(); i++){
                                    if (s == ServerTCP.MULTIPLAYER_CLIENTS.get(i).s) isPlaying = ServerTCP.MULTIPLAYER_CLIENTS.get(i).isPlaying;
                                }
                                for (int i=0; i<ServerTCP.MULTIPLAYER_CLIENTS.size(); i++){
                                    if (s == ServerTCP.MULTIPLAYER_CLIENTS.get(i).s) isReady = ServerTCP.MULTIPLAYER_CLIENTS.get(i).isReady;
                                }

                                if (isPlaying) {
                                    System.out.println("LoggedIn Client (" + username + ":" + s.getInetAddress() + ":" + s.getPort() + ") is in a game and sent: " + requested_isLoggedIn);
                                } else {
                                    System.out.println("LoggedIn Client (" + username + ":" + s.getInetAddress() + ":" + s.getPort() + ") sent: " + requested + "->" + requested_isLoggedIn);
                                }

                                ///////////////////////////////////////
                                // Play multiplayer
                                if (requested_isLoggedIn.equals("1") && !isPlaying) {

                                    dos.writeUTF("\nFinding your teammate...");

                                    // Create a new handler object for handling this request.
                                    Multiplay multiplay = new Multiplay(s,username, dis, dos);

                                    if (ServerTCP.MULTIPLAYER_CLIENTS.size() >= 4){
                                        dos.writeUTF("\nServer is full. Please wait a few minutes!");
                                    } else {
                                        // Create a new Thread with this object.
                                        Thread multi = new Thread(multiplay);
                                        System.out.println("Adding this client to multiplayer list");
                                        // Add this client to multiplayer clients list
                                        ServerTCP.MULTIPLAYER_CLIENTS.add(multiplay);
                                        // Start the thread.
                                        multi.start();
                                        for (Multiplay mp : ServerTCP.MULTIPLAYER_CLIENTS){
                                            if (ServerTCP.MULTIPLAYER_CLIENTS.size() == 1) mp.dos.writeUTF("Currently there is 1 client finding game.");
                                            else mp.dos.writeUTF("Currently there are " + ServerTCP.MULTIPLAYER_CLIENTS.size() + " clients finding game.");
                                        }
                                    }
                                }

                                ///////////////////////////////////////
                                // isPlaying == true
                                if (isPlaying && isReady){
                                    String typed = "Typed:" + requested_isLoggedIn;
                                    ServerTCP.userTyped.putIfAbsent(s,typed + ":" + (double)LocalTime.now().toNanoOfDay());
                                    dos.writeUTF("Please wait...");
                                    for (int i=0; i<ServerTCP.MULTIPLAYER_CLIENTS.size(); i++){
                                        if (s == ServerTCP.MULTIPLAYER_CLIENTS.get(i).s) ServerTCP.MULTIPLAYER_CLIENTS.get(i).isPlaying = false;
                                    }
                                }

                                ///////////////////////////////////////
                                // isReady == false
                                if (!isReady){
                                    if (requested_isLoggedIn.equals("ready")){
                                        ServerTCP.userReady.putIfAbsent(s,"ready");
                                        dos.writeUTF("Please wait for other players...");
                                    }
                                    for (int i=0; i<ServerTCP.MULTIPLAYER_CLIENTS.size(); i++){
                                        if (s == ServerTCP.MULTIPLAYER_CLIENTS.get(i).s) ServerTCP.MULTIPLAYER_CLIENTS.get(i).isReady = true;
                                    }
                                }

                                ///////////////////////////////////////
                                // Play single player
                                if (requested_isLoggedIn.equals("2") && !isPlaying) {
                                    try {

                                        dos.writeUTF("\nPlaying Single-player!");

                                        //Countdown and send randomized String
                                        TimeUnit.SECONDS.sleep(3);
                                        String sentString = Game.randomize();
                                        dos.writeUTF("\n" + sentString);

                                        //Timing
                                        double timeStart = LocalTime.now().toNanoOfDay();
                                        String receivedString = dis.readUTF().trim();
                                        double timeEnd = LocalTime.now().toNanoOfDay();
                                        double timeElapsed = (timeEnd - timeStart)/1000000000; // sec = nano-sec / 10^9

                                        //Accuracy & WPM
                                        double accuracy = Game.accuracy(sentString,receivedString);
                                        int wpm = (int) ((((double) receivedString.length() / 5) / timeElapsed) * 60 ); // wpm formula on google

                                        //Total score = WPM * Accuracy
                                        int score = wpm * ((int) accuracy);

                                        //Print result
                                        dos.writeUTF("\nElapsed time: " + Math.round(timeElapsed * 100.00) / 100.00 + " sec");
                                        dos.writeUTF("Word per minute: " + wpm + " wpm");
                                        dos.writeUTF("Your accuracy: " + Math.round(accuracy * 100.0) / 100.0 + " %");
                                        dos.writeUTF("Total score: " + score + " points");

                                        System.out.println("LoggedIn Client (" + username + ":" + s.getInetAddress() + ":" + s.getPort() + ") scored: " + score);

                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }

                                ///////////////////////////////////////
                                // Quit
                                if (requested_isLoggedIn.equals("x") && !isPlaying) {
                                    dos.writeUTF("\nBye, " + ServerTCP.usersList.get(username).name + "!");
                                    requested = "x";
                                }

                                ///////////////////////////////////////
                                // Invalid input
                                else if (!(requested_isLoggedIn.equals("1") || requested_isLoggedIn.equals("2") || requested_isLoggedIn.equals("ready") || isPlaying)) {
                                    dos.writeUTF("\nInvalid input, please try again!");
                                }
                            }
                        }
                    }

                    //////////////////////////
                    // Register
                    if (requested.equals("2")){

                        dos.writeUTF("\nRegister new account. You can type 'return' to return to main menu.");
                        returnMenu = false;
                        boolean registerSucceeded = false;
                        boolean usernameAccepted = false;
                        while (!registerSucceeded && !returnMenu) {
                            while (!usernameAccepted) {
                                dos.writeUTF("Enter new username: ");
                                username = dis.readUTF().trim();
                                Pattern specialChar = Pattern.compile("[^A-Za-z0-9_]"); //check if username contains special characters. Underscore is not included
                                Matcher matcher = specialChar.matcher(username);
                                boolean userCheck = matcher.find();
                                if (userCheck) {
                                    dos.writeUTF("Username cannot contain special characters.");
                                } else {
                                    usernameAccepted = true;
                                }
                            }
                            while (ServerTCP.usersList.containsKey(username)) {
                                dos.writeUTF("Username already exists, please choose another username. Or type 'return' to return to menu.");
                                dos.writeUTF("Enter new username: ");
                                username = dis.readUTF().trim();
                            }
                            if (username.equals("return")) {
                                dos.writeUTF("\nReturn to main menu!");
                                returnMenu = true;
                            } else {
                                dos.writeUTF("Enter your password: ");
                                password = dis.readUTF().trim();
                                Pattern space = Pattern.compile("[\\t ]"); //check if password contains space and tab characters
                                Matcher matcher2 = space.matcher(password);
                                boolean passCheck = matcher2.find();
                                if (passCheck) {
                                    dos.writeUTF("Password cannot contain space characters.");
                                } else {
                                    if (password.length() < 5) { // check if password has at least 5 char
                                        dos.writeUTF("Password must contains at least 5 characters.");
                                    } else {
                                        dos.writeUTF("Enter your name: ");
                                        name = dis.readUTF().trim().replaceAll(" +", " ");

                                        user = new User(password,name);

                                        ServerTCP.usersList.put(username, user);

                                        dos.writeUTF("Thank you for register, " + name + ". Now you can login!");
                                        registerSucceeded = true;
                                    }
                                }
                            }
                        }
                    }

                    ///////////////////////////
                    // For testing, no need to login/register
                    if (requested.equals("0")) {
                        dos.writeUTF("\nThere are no Easter Eggs here. Go away");
                        break;
                    }

                    ////////////////////////////
                    // Exit
                    if (requested.equals("x")) {
                        dos.writeUTF("\nDisconnected to server!");
                        this.isConnected = false;
                        break;
                    }

                    ////////////////////////////
                    // Invalid input
                    if (!(requested.equals("1") || requested.equals("2"))) {
                        dos.writeUTF("\nInvalid input, please try again!");
                    }
                }

                System.out.println("Client (" + username + ":" + s.getInetAddress() + ":" + s.getPort() + ") disconnected." );

                // Remove the request from the vector
                for (int i=0; i<ServerTCP.ACTIVE_CLIENTS.size(); i++){
                    Socket socket = ServerTCP.ACTIVE_CLIENTS.get(i).s;
                    if (s == socket) ServerTCP.ACTIVE_CLIENTS.remove(ServerTCP.ACTIVE_CLIENTS.get(i));
                }

                // Close socket and streams
                s.close();
                dis.close();
                dos.close();
                break;

            } catch (IOException e) {
                System.out.println("Client (" + s.getInetAddress() + ":" + s.getPort() + ") disconnected." );

                // Remove the request from the vector
                for (int i=0; i<ServerTCP.ACTIVE_CLIENTS.size(); i++){
                    Socket socket = ServerTCP.ACTIVE_CLIENTS.get(i).s;
                    if (s == socket) ServerTCP.ACTIVE_CLIENTS.remove(ServerTCP.ACTIVE_CLIENTS.get(i));
                }

                // Close socket and streams
                try {
                    s.close();
                    dis.close();
                    dos.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
                break;
            }

        }
    }
}

class Multiplay implements Runnable {

    private final String username;
    final DataInputStream dis;
    final DataOutputStream dos;
    Socket s;
    boolean isPlaying, isReady;

    //Constructor
    public Multiplay(Socket s, String username, DataInputStream dis, DataOutputStream dos) {
        this.dis = dis;
        this.dos = dos;
        this.username = username;
        this.s = s;
        this.isPlaying = false;
        this.isReady = false;
    }

    @Override
    public void run() {

        int playerMax = 4;
        boolean phaseOne = false;
        boolean phaseTwo = false;
        boolean phaseCheck = true;
        boolean readyCheck = false;
        String sentString1, sentString2;

        java.net.Socket[] socket = new Socket[4];
        double[] timeStart = new double[4];
        double[] timeEnd = new double[4];
        double[] timeElapsed = new double[4];
        double[] accuracy = new double[4];
        String[] receivedString = new String[4];
        String[] sentString = new String[4];
        int[] wpm = new int[4];
        int[] score = new int[4];

        while (ServerTCP.MULTIPLAYER_CLIENTS.size() == playerMax) {

            // Starting the game (phaseOne = false; phaseTwo = false)
            // phaseCheck is used to check whether the phase is done or not
            if (!phaseOne && !phaseTwo) {
                for (Multiplay mp : ServerTCP.MULTIPLAYER_CLIENTS.subList(0,4)){
                    try {
                        mp.dos.writeUTF("\nGame found!");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                phaseOne = true; phaseTwo = false;

                for (int i=0; i<playerMax; i++){
                    socket[i] = ServerTCP.MULTIPLAYER_CLIENTS.get(i).s;
                    if (i % 2 == 0) message.sendToOneClient("You are in team 1!", socket[i]);
                    else message.sendToOneClient("You are in team 2!", socket[i]);
                }
                for (Multiplay mp : ServerTCP.MULTIPLAYER_CLIENTS){
                    try {
                        mp.dos.writeUTF("\nWhen you're ready, type 'ready' and enter.");
                        mp.isReady = false;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            // readyCheck for the multiplayer clients
            if (ServerTCP.userReady.size() == 4){
                int sumReady=0;
                for (int i=0; i<4; i++){
                    if (ServerTCP.userReady.get(socket[i]).equals("ready")) sumReady++;
                }
                if (sumReady==4) readyCheck = true;
            }

            if (readyCheck){

                //Starting phaseOne
                if (phaseOne && phaseCheck) {

                    for (Multiplay mp1 : ServerTCP.MULTIPLAYER_CLIENTS.subList(0,2)) {
                        mp1.isPlaying = true;
                    }
                    try {
                        for (int i=0; i<playerMax; i++){
                            if (i<(playerMax/2)) message.sendToOneClient("\nPlaying Multi-player! - 1st Phase",socket[i]);
                            if (i>=(playerMax/2)) message.sendToOneClient("\nPlease wait for the 1st phase to finish!",socket[i]);
                        }

                        TimeUnit.SECONDS.sleep(3);
                        sentString1 = Game.randomize();

                        for (int i=0; i<(playerMax/2); i++){
                            message.sendToOneClient("\nGood luck have fun.\n\n" + sentString1,socket[i]);
                            timeStart[i] = LocalTime.now().toNanoOfDay();
                            sentString[i] = sentString1;
                        }

                        phaseCheck = false;

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                //Starting phaseTwo
                if (phaseTwo && phaseCheck) {

                    for (Multiplay mp2 : ServerTCP.MULTIPLAYER_CLIENTS.subList(2,4)) {
                        mp2.isPlaying = true;
                    }

                    try {
                        for (int i=0; i<playerMax; i++){
                            if (i>=(playerMax/2)) message.sendToOneClient("\nPlaying Multi-player! - 2nd Phase",socket[i]);
                            if (i<(playerMax/2)) message.sendToOneClient("\nThe 1st phase is done.\nPlease wait for the 2nd phase to finish!",socket[i]);
                        }

                        TimeUnit.SECONDS.sleep(3);
                        sentString2 = Game.randomize();

                        for (int i=(playerMax/2); i<playerMax; i++){
                            message.sendToOneClient("\nGood luck have fun.\n\n" + sentString2,socket[i]);
                            timeStart[i] = LocalTime.now().toNanoOfDay();
                            sentString[i] = sentString2;
                        }

                        phaseCheck = false;

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                // Check whether phaseOne is finished
                if (ServerTCP.userTyped.size() == playerMax/2 && !phaseTwo) {
                    phaseOne = false; phaseTwo = true; phaseCheck = true;
                }

                // Calculating scores when phaseTwo is finished
                if (ServerTCP.userTyped.size() == playerMax) {
                    phaseTwo = false;
                    for (int i=0; i<playerMax; i++){

                        String[] received = ServerTCP.userTyped.get(socket[i]).split(":");
                        receivedString[i] = received[1];
                        timeEnd[i] = Double.parseDouble(received[2]);
                        timeElapsed[i] = (timeEnd[i]-timeStart[i])/1000000000;
                        accuracy[i] = Game.accuracy(sentString[i],receivedString[i]);
                        wpm[i] = (int) ((((double) receivedString[i].length() / 5) / timeElapsed[i]) * 60 );
                        score[i] = wpm[i] * ((int) accuracy[i]);

                        message.sendToOneClient("\nElapsed time: " + Math.round(timeElapsed[i] * 100.00) / 100.00 + " sec",socket[i]);
                        message.sendToOneClient("Word per minute: " + wpm[i] + " wpm",socket[i]);
                        message.sendToOneClient("Your accuracy: " + Math.round(accuracy[i] * 100.0) / 100.0 + " %",socket[i]);
                        message.sendToOneClient("Total score: " + score[i] + " points",socket[i]);

                        System.out.println("LoggedIn Client (" + username + ":" + socket[i].getInetAddress() + ":" + socket[i].getPort() + ") scored: " + score[i]);
                    }
                    int scoreTeam1 = 0;
                    int scoreTeam2 = 0;
                    int scoreMax = 0;
                    int scoreMaxClient = 0;
                    for (int i=0; i<playerMax; i++){
                        if (i%2==0) scoreTeam1 = scoreTeam1 + score[i];
                        else scoreTeam2 = scoreTeam2 + score[i];
                        if (scoreMax < score[i]) {
                            scoreMax = score[i];
                            scoreMaxClient = i;
                        }
                    }
                    if (scoreTeam1 > scoreTeam2) {
                        for (int i=0; i<playerMax; i++){
                            if (i%2==0) message.sendToOneClient("\nGood job!\nYour team won with the total score of " + scoreTeam1 + "\nTotal score of team 2: " + scoreTeam2, socket[i]);
                            else message.sendToOneClient("\nBetter luck next time!\nYour team lose with the total score of " + scoreTeam2 + "\nTotal score of team 1: " + scoreTeam1, socket[i]);
                        }
                    } else if (scoreTeam1 < scoreTeam2) {
                        for (int i=0; i<playerMax; i++){
                            if (i%2==0) message.sendToOneClient("\nBetter luck next time!\nYour team lose with the total score of " + scoreTeam1 + "\nTotal score of team 2: " + scoreTeam2, socket[i]);
                            else message.sendToOneClient("\nGood job!\nYour team won with the total score of " + scoreTeam2 + "\nTotal score of team 1: " + scoreTeam1, socket[i]);
                        }
                    } else {
                        for (int i=0; i<playerMax; i++){
                            message.sendToOneClient("\nTwo teams are tied with the score of " + scoreTeam1, socket[i]);
                        }
                    }
                    message.sendToOneClient("\nCongratulations! You have the highest score of the session. Your score: " + scoreMax,socket[scoreMaxClient]);

                    for (Multiplay mp : ServerTCP.MULTIPLAYER_CLIENTS){
                        try {
                            mp.dos.writeUTF("\n---------------------------------------------------------------------");
                            mp.isPlaying = false;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    ServerTCP.userReady.clear();
                    ServerTCP.userTyped.clear();
                    ServerTCP.MULTIPLAYER_CLIENTS.subList(0,4).clear();
                }
            }
        }
    }
}

class message {

    // Use to send msg to the socket
    public static void sendToOneClient(String msg, Socket socket){
        try {
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            dos.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}