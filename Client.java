package com.company;

import java.net.*;
import java.io.*;
import java.util.*;


public class Client  {
    private ObjectInputStream sInput;
    private ObjectOutputStream sOutput;
    private Socket socket;
    private String server, username;
    private int port;

    Client(String server, int port, String username) {
        this.server = server;
        this.port = port;
        this.username = username;
    }


    public boolean start() {

        try {
            socket = new Socket(server, port);
        }
        catch(Exception ec) {
            display("Error connectiong to server:" + ec);
            return false;
        }

        display("what is your name?");
        Scanner s = new Scanner(System.in);
        username = s.nextLine();


        try
        {
            sInput  = new ObjectInputStream(socket.getInputStream());
            sOutput = new ObjectOutputStream(socket.getOutputStream());

        }
        catch (IOException eIO) {
            display("Exception creating new Input/output Streams: " + eIO);
            return false;
        }

        new ListenFromServer().start();

        try
        {
            sOutput.writeObject(username);
        }
        catch (IOException eIO) {
            display("Exception doing login : " + eIO);
            disconnect();
            return false;
        }
        return true;
    }


    private void display(String msg) {
        System.out.println(msg);
    }


    void sendMessage(ChatMessage msg) {
        try {
            sOutput.writeObject(msg);
        }
        catch(IOException e) {
            display("Exception writing to server: " + e);
        }
    }


    private void disconnect() {
        try {
            if(sInput != null) sInput.close();
        }
        catch(Exception e) {}
        try {
            if(sOutput != null) sOutput.close();
        }
        catch(Exception e) {}
        try{
            if(socket != null) socket.close();
        }
        catch(Exception e) {}
    }

    public static void main(String[] args) {
        int portNumber = 1500;
        String serverAddress = "localhost";
        String userName = "Anonymous";

        Client client = new Client(serverAddress, portNumber, userName);
        if(!client.start())
            return;

        Scanner scan = new Scanner(System.in);
        while(true) {
            System.out.print("> ");
            String msg = scan.nextLine();
            if(msg.equalsIgnoreCase("close")) {
                client.sendMessage(new ChatMessage(ChatMessage.close, ""));
                break;
            }
            else {
                client.sendMessage(new ChatMessage(ChatMessage.MESSAGE, msg));
            }
        }
        client.disconnect();
    }

    class ListenFromServer extends Thread {

        public void run() {
            while(true) {
                try {
                    String msg = (String) sInput.readObject();

                    System.out.println(msg);
                    System.out.print("> ");
                }
                catch(IOException e) {
                    display("com.company.Server has close the connection: " + e);
                    break;
                }
                catch(ClassNotFoundException e2) {
                }
            }
        }
    }
}
