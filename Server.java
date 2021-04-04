package com.javarush.task.task30.task3008;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private static Map<String,Connection> connectionMap = new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(ConsoleHelper.readInt());
        ConsoleHelper.writeMessage("The server is running");
        try {
            while (true) {
                Socket socket = serverSocket.accept();
                Handler user = new Handler(socket);
                user.start();
            }
        }catch (Exception ex){
            serverSocket.close();
            System.out.println(ex.getMessage());
        }
    }

    public static void sendBroadcastMessage(Message message){
        for (Map.Entry<String,Connection> entry :
                connectionMap.entrySet()) {
            try {
                entry.getValue().send(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    
    private static class Handler extends Thread{
        private Socket socket;

        @Override
        public void run() {
            super.run();
            ConsoleHelper.writeMessage(String.format("A connection to the remote address gas been established %s",
                    socket.getRemoteSocketAddress()));

            Connection current = null;
            String userName = null;
            try {
                current = new Connection(socket);
                userName = serverHandshake(current);
                sendBroadcastMessage(new Message(MessageType.USER_ADDED, userName));
                notifyUsers(current,userName);
                serverMainLoop(current,userName);
            } catch (IOException | ClassNotFoundException e) {
                ConsoleHelper.writeMessage("An error occurred while communicating with the remote server");
                }finally {
                if (userName != null){
                    connectionMap.remove(userName);
                    sendBroadcastMessage(new Message(MessageType.USER_REMOVED,userName));
                }
                ConsoleHelper.writeMessage("Connection lost");
            }

            }



        public Handler(Socket socket) {
            this.socket = socket;
        }

        private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException{
            while(true) {
                connection.send(new Message(MessageType.NAME_REQUEST));
                Message user = connection.receive();
                if(user.getType().equals(MessageType.USER_NAME) && user.getData() != "" && !connectionMap.containsKey(user.getData())){
                    connectionMap.put(user.getData(),connection);
                    connection.send(new Message(MessageType.NAME_ACCEPTED));
                    return user.getData();
                }
            }
        }

        private void notifyUsers(Connection connection, String userName) throws IOException{
            for (Map.Entry<String, Connection> entry :
                    connectionMap.entrySet()) {
                if (!entry.getKey().equals(userName))
                    connection.send(new Message(MessageType.USER_ADDED, entry.getKey()));
            }
        }
        private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException{
            while (true) {
                Message message = connection.receive();
                if (message.getType() == MessageType.TEXT) {
                    Message send = new Message(MessageType.TEXT, userName + ": " + message.getData());
                    sendBroadcastMessage(send);
                } else {
                    ConsoleHelper.writeMessage("incorrect message format!");
                }
            }
        }

    }
}
