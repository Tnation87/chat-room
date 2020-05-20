package com.company;

import java.io.*;
import java.net.*;
import java.util.*;


public class Server {
    private static int uniqueId;
    private ArrayList<ClientThread> al;
    private int port;
    private boolean keepGoing;



    public Server(int port) {
        this.port = port;
        al = new ArrayList<ClientThread>();
    }

    public void start() {
        keepGoing = true;
        try
        {
            ServerSocket serverSocket = new ServerSocket(port);
            while(keepGoing)
            {

                Socket socket = serverSocket.accept();
                if(!keepGoing)
                    break;
                ClientThread t = new ClientThread(socket);
                al.add(t);
                t.start();
            }

            try {
                serverSocket.close();
                for(int i = 0; i < al.size(); ++i) {
                    ClientThread tc = al.get(i);
                    try {
                        tc.sInput.close();
                        tc.sOutput.close();
                        tc.socket.close();
                    }
                    catch(IOException ioE) {
                    }
                }
            }
            catch(Exception e) {
                display("Exception closing the server and clients: " + e);
            }
        }
        catch (IOException e) {
            String msg =" Exception on new ServerSocket: " + e + "\n";
            display(msg);
        }
    }

    protected void stop() {
        keepGoing = false;
    }

    private void display(String msg) {
        System.out.println(msg);

    }

    private synchronized void broadcast(String message) {

        String messageLf = message + "\n";
        System.out.print(messageLf);

        for(int i = al.size(); --i >= 0;) {
            ClientThread ct = al.get(i);
            if(!ct.writeMsg(messageLf)) {
                al.remove(i);
                display("Disconnected com.company.Client " + ct.username + " removed from list.");
            }
        }
    }

    synchronized void remove(int id) {
        for(int i = 0; i < al.size(); ++i) {
            ClientThread ct = al.get(i);
            if(ct.id == id) {
                al.remove(i);
                return;
            }
        }
    }


    public static void main(String[] args) {
        int portNumber = 1500;
        Server server = new Server(portNumber);
        server.start();
    }


    class ClientThread extends Thread {
        Socket socket;
        ObjectInputStream sInput;
        ObjectOutputStream sOutput;
        int id;
        String username;
        ChatMessage cm;

        ClientThread(Socket socket) {

            id = ++uniqueId;
            this.socket = socket;

            try
            {
                sOutput = new ObjectOutputStream(socket.getOutputStream());
                sInput  = new ObjectInputStream(socket.getInputStream());
                username = (String) sInput.readObject();
                writeMsg("welcome " + username + "\n");
            }
            catch (IOException e) {
                display("Exception creating new Input/output Streams: " + e);
                return;
            }

            catch (ClassNotFoundException e) {
            }
        }

        public void run() {
            boolean keepGoing = true;
            while(keepGoing) {
                try {
                    cm = (ChatMessage) sInput.readObject();
                }
                catch (IOException e) {
                    display(username + " Exception reading Streams: " + e);
                    break;
                }
                catch(ClassNotFoundException e2) {
                    break;
                }
                String message = cm.getMessage();

                switch(cm.getType()) {

                    case ChatMessage.MESSAGE:
                        broadcast(username + ": " + message);
                        break;
                    case ChatMessage.close:
                        keepGoing = false;
                        break;
                }
            }
            remove(id);
            close();
        }

        private void close() {
            try {
                if(sOutput != null) sOutput.close();
            }
            catch(Exception e) {}
            try {
                if(sInput != null) sInput.close();
            }
            catch(Exception e) {};
            try {
                if(socket != null) socket.close();
            }
            catch (Exception e) {}
        }


        private boolean writeMsg(String msg) {
            if(!socket.isConnected()) {
                close();
                return false;
            }
            try {
                sOutput.writeObject(msg);
            }
            catch(IOException e) {
                display("Error sending message to " + username);
                display(e.toString());
            }
            return true;
        }
    }
}

