package task;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    public static void main(String[] args) throws IOException {

        serverSocketConnection();

    }

    //new serverSocket and server is running
    public static void serverSocketConnection(){
        ConsoleHelper.writeMessage("Enter server port:");
        int port = ConsoleHelper.readInt();

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            ConsoleHelper.writeMessage("Chat server is running.");
            while (true) {
                // Wait for an incoming connection and start a separate thread when it arrives
                Socket socket = serverSocket.accept();
                new Handler(socket).start();
            }
        } catch (Exception e) {
            ConsoleHelper.writeMessage("An error occurred while starting or running the server.");
        }
    }

    //task.client name, and connection with the task.client, Concurrent - thread-safe
    private static Map<String, Connection> connectionMap = new ConcurrentHashMap<>();

    public static void sendBroadcastMessage(Message message){
        // Send the message over all connections
        for (Connection connection : connectionMap.values()) {
            try {
                connection.send(message);
            } catch (IOException e) {   //informs about which connection didnt get a message
                ConsoleHelper.writeMessage("Unable to send message to " + connection.getRemoteSocketAddress());
            }
        }
    }

    //can only access static fields and its more independent
    private static class Handler extends Thread{  //Without an instance of the outer class, an object of the inner class simply could not exist

        private Socket socket;
        public Handler(Socket socket){
            this.socket = socket;
        }

        //server's main message-processing loop
        private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException{
            while (true){
               Message message =  connection.receive();
               if(message.getType() == MessageType.TEXT){
                   sendBroadcastMessage(new Message(MessageType.TEXT, userName + ": " + message.getData()));
               }else {
                   ConsoleHelper.writeMessage("task.Message received from " + socket.getRemoteSocketAddress() + ". The message type does not match the protocol.");
               }
            }
        }

        //sending information to the task.client (new participant) about
        //the other clients (chat participants)
       private void notifyUsers(Connection connection, String userName) throws IOException{
            for(String user : connectionMap.keySet()){
                Message message = new Message(MessageType.USER_ADDED, user);
                if(!userName.equals(user)) {
                    connection.send(message);
                }
            }
       }

        //adds new connections to map
        private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException {
            while (true) {
                connection.send(new Message(MessageType.NAME_REQUEST));

                Message message = connection.receive();
                if (message.getType() != MessageType.USER_NAME) {
                    ConsoleHelper.writeMessage("task.Message received from " + socket.getRemoteSocketAddress() + ". The message type does not match the protocol.");
                    continue;
                }

                String userName = message.getData();

                if (userName.isEmpty()) {
                    ConsoleHelper.writeMessage("There was an attempt to connect to the server using an empty name from " + socket.getRemoteSocketAddress());
                    continue;
                }

                if (connectionMap.containsKey(userName)) {
                    ConsoleHelper.writeMessage("There was an attempt to connect to the server using a name that is already being used from " + socket.getRemoteSocketAddress());
                    continue;
                }
                connectionMap.put(userName, connection);

                connection.send(new Message(MessageType.NAME_ACCEPTED));
                return userName;
            }
        }

        @Override
        public void run() {
            ConsoleHelper.writeMessage("A new connection with " + socket.getRemoteSocketAddress() + " has been established");

            String userName = null;

            try (Connection connection = new Connection(socket)) {
                userName = serverHandshake(connection);

                // Inform all chat participants that a new participant has joined the chat
                sendBroadcastMessage(new Message(MessageType.USER_ADDED, userName));

                // Inform the new participant about the existing participants
                notifyUsers(connection, userName);

                // Process participants' messages
                serverMainLoop(connection, userName);

            } catch (IOException | ClassNotFoundException e) {
                ConsoleHelper.writeMessage("Error while communicating with " + socket.getRemoteSocketAddress());
            }

            //after user disconnects server main loop will be interrupted, so user name!= 0
            if (userName != null) {
                connectionMap.remove(userName);
                sendBroadcastMessage(new Message(MessageType.USER_REMOVED, userName));
            }

            ConsoleHelper.writeMessage("The connection with " + socket.getRemoteSocketAddress() + " is closed.");
        }

    }
}
