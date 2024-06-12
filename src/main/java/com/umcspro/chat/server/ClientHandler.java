package com.umcspro.chat.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final Server server;
    private final PrintWriter output;
    private final Scanner input;
    private String clientName;


    public ClientHandler(Socket socket, Server server){
        try {
            this.socket = socket;
            this.server = server;
            output = new PrintWriter(socket.getOutputStream(),true);
            input = new Scanner(socket.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        try {
            boolean connected = isClinetConnected();

            if (!connected) {
                return;
            }

            join();
            comunicate();
            disconnect();

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private boolean isClinetConnected() throws IOException {
        if (input.hasNextLine()) {
            clientName = input.nextLine();

        if (server.isClientNameTaken(clientName)) {
            output.println("User already exists");
            socket.close();
            return false;
            }
        return true;
        }
        return false;
    }

    private void join() {
        server.addClient(clientName, this);
        server.printUsers();
        server.broadcast("/broadcast [SERVER]: " + clientName + " joined");
        server.broadcast("/online " + server.getClientNames());
    }

    private void comunicate() throws IOException {
        String clientMessage;
        boolean connected = true;

        while (connected) {
            if (input.hasNextLine()) {
                clientMessage = input.nextLine();
                connected = parseClientMessage(clientMessage);
            } else {
                connected = false;
            }
        }
    }

    public boolean parseClientMessage(String clientMessage) {
        String command = getCommand(clientMessage);

        switch (command) {
            case "/w":
                parsePrivateMessage(clientMessage);
                break;
            case "/online":
                this.send("/online " + server.getClientNames());
                break;
            default:
                server.broadcast("/broadcast [" + clientName + "]: " + clientMessage);
                break;
        }

        return !clientMessage.equalsIgnoreCase("bye");
    }

    private String getCommand(String clientMessage) {
        if (clientMessage.startsWith("/w")) {
            return "/w";
        } else if (clientMessage.equals("/online")) {
            return "/online";
        } else {
            return "default";
        }
    }

    private void parsePrivateMessage(String clientMessage) {
        String[] message = clientMessage.split(" ", 3);
        if (message.length >= 3) {
            String rName = message[1];
            String messageToSend = "/broadcast " + message[2];
            server.broadcastPrivate(clientName, rName, messageToSend);
        }
    }

    private void disconnect() throws IOException {
        server.removeClient(clientName);
        socket.close();
        server.printUsers();
        server.broadcast("/broadcast [SERVER]: " + clientName + " left");
        server.broadcast("/online " + server.getClientNames());
    }

    public void send(String message){
        if(output != null){
            output.println(message);
        }
    }
}
