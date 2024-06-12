package com.umcspro.chat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Server {
    private final Map<String, ClientHandler> clientHandlers = new HashMap<>();

    public void start(int port) {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Server started on port " + port);
            while (true) {
                 Socket socket = serverSocket.accept();
                 ClientHandler clientHandler = new ClientHandler(socket, this);
                 new Thread(clientHandler).start();

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public void addClient(String userName, ClientHandler clientHandler) {
        clientHandlers.put(userName, clientHandler);
    }

    public void broadcast(String message) {
        System.out.println("send: " + message);
        for(ClientHandler client : clientHandlers.values()){
                client.send(message);
        }
    }

    public void removeClient(String userName) {
        ClientHandler userThread = clientHandlers.remove(userName);
        if (userThread != null) {
            System.out.println("The user " + userName + " quitted");
        }
    }

    public void printUsers(){
        System.out.println("Users connected: " + clientHandlers.keySet());
    }

    public String getClientNames(){
        return clientHandlers.keySet().toString();
    }

    public void broadcastPrivate(String sender,String receiver, String message){
        ClientHandler reciverHandler = clientHandlers.get(receiver);
        if ( reciverHandler!=null)
            reciverHandler.send("/broadcast [ _private_ " + sender + "]: " + message);
        else{
            clientHandlers.get(sender).send("/broadcast [SERVER]: User Offline");
        }
    }

    public boolean isClientNameTaken(String clientName) {
        return clientHandlers.containsKey(clientName);
    }
}
