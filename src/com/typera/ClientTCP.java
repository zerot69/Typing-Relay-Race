/*
Typing Relay Race (Typera)
Author: github.com/zerot69 & github.com/baorhieu078
Using TCP Server to make a Typing Race game between 4 players (divided into 2 teams)
 */

package com.typera;

import java.io.*;
import java.net.*;
import java.util.Scanner;


public class ClientTCP
{
    final static int ServerPort = 6969;

    public static void main(String[] args) throws IOException {

        Scanner scn = new Scanner(System.in);

        // Getting IP
        String ip = InetAddress.getLocalHost().getHostAddress();

        // Establish the connection
        Socket socket = new Socket(ip, ServerPort);

        System.out.println("Connected to server!");
        System.out.println("Connection info: " + socket);
        System.out.println("INFO: Enter 'x' to exit");
        System.out.println("\n_______________________________________");
        System.out.println("Welcome to Typing Relay Race! - Group 8");
        Game.mainMenu();

        // Obtaining input and out streams
        DataInputStream dis = new DataInputStream(socket.getInputStream());
        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

        // sendMessage thread
        Thread sendMessage = new Thread(() -> {
            while (true) {

                // Read the message to deliver.
                String msg = scn.nextLine();
                try {
                    // Write on the output stream
                    dos.writeUTF(msg);
                } catch (IOException e) {
                    System.out.println("\nSend message to server failed because of disconnection to server, please reconnect to server.");
                    System.exit(1);
                }
            }
        });

        // readMessage thread
        Thread readMessage = new Thread(() -> {

            while (true) {
                try {
                    // Read the message sent to this client
                    String msg = dis.readUTF();
                    System.out.println(msg);
                    if (msg.contains("Return to main menu!") || msg.startsWith("Thank you for register")) Game.mainMenu();
                    if (msg.contains("Welcome back,") || msg.contains("---")) Game.gameMenu();
                    if (msg.contains("Playing")) Game.countdown();
                    if (msg.contains("Disconnected")) socket.close();
                } catch (IOException e) {
                    System.out.println("\nSocket closed!");
                    System.exit(1);
                }
            }
        });

        sendMessage.start();
        readMessage.start();

    }
}