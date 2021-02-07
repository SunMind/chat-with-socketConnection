package task.client;

import task.*;
import java.io.IOException;
import java.net.Socket;

public class Client {

    protected Connection connection;
    private volatile boolean clientConnected ; // every thread using the clientConnected field will be working with the current value, not a cached value

    protected String getServerAddress(){
        ConsoleHelper.writeMessage("Enter IP address");
        return ConsoleHelper.readString();  //IP address if the task.client and server are running on different machines or 'localhost' if the task.client and server are running on the same machine
    }

    protected int getServerPort(){
        ConsoleHelper.writeMessage("Enter port");
        return ConsoleHelper.readInt();
    }

    protected String getUserName(){
        ConsoleHelper.writeMessage("Enter user name");
        return ConsoleHelper.readString();
    }

    protected boolean shouldSendTextFromConsole(){
        return true;  //because always sends text from the console
    }

    protected SocketThread getSocketThread(){
        return new SocketThread();
    }

    protected void sendTextMessage(String text) {
        try {
            connection.send(new Message(MessageType.TEXT, text));
        } catch (IOException e) {
            ConsoleHelper.writeMessage("Unable to send message");
            clientConnected = false;
        }
    }

    public void run() {
        SocketThread socketThread = getSocketThread();
        socketThread.setDaemon(true);  //helper thread is automatically interrupted when the program exits.
        socketThread.start();

        try {
            synchronized (this) {  //object level synchronization
                wait();  //waits until it will be notified by socketTgread after establishing the connection
            }
        } catch (InterruptedException exception) {
            exception.printStackTrace();
            notify();
            return; //exit the program
        }

        if (clientConnected) {
            System.out.println("task.Connection established. To exit, enter 'exit'.");
        } else if (!clientConnected) {
            System.out.println("An error occurred while working with the task.client.");
        }

        // Until the exit command is entered, read messages from the console and send them to the server
        while (clientConnected) {
            String text = ConsoleHelper.readString();
            if (text.equalsIgnoreCase("exit"))
                break;

            if (shouldSendTextFromConsole())
                sendTextMessage(text);

        }
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }


    //establishes the socket connection and
    //reads server messages
    //non-static is more dependent class,
    //can be inherited
    public class SocketThread extends Thread{

         public void run(){
             try(Socket socket = new Socket(getServerAddress(), getServerPort())) {
                 connection = new Connection(socket);
                 clientHandshake();
                 clientMainLoop();
             } catch (IOException | ClassNotFoundException ioException) {
                 ioException.printStackTrace();
                 notifyConnectionStatusChanged(false);
             }
         }

        //This method will represent the task.client to the server.
        protected void clientHandshake() throws IOException, ClassNotFoundException{
            while (true){
                Message messageReceived = connection.receive();
                if(messageReceived.getType() == MessageType.NAME_REQUEST){
                    connection.send(new Message(MessageType.USER_NAME, getUserName()));
                }
                else if(messageReceived.getType() == MessageType.NAME_ACCEPTED){
                    notifyConnectionStatusChanged(true);
                    return;
                }
                else throw new IOException("Unexpected task.MessageType");
            }
        }

        // the server's main message-processing loop
        protected void clientMainLoop() throws IOException, ClassNotFoundException{
            while (true){
            Message message = connection.receive();
            if(message.getType() ==MessageType.TEXT){
                processIncomingMessage(message.getData());
            }else if(message.getType()==MessageType.USER_ADDED){
                informAboutAddingNewUser(message.getData());
            }else if(message.getType()==MessageType.USER_REMOVED){
                informAboutDeletingNewUser(message.getData());
            }else {
               throw new IOException("Unexpected " + message.getType());
            }
            }

        }

      protected void processIncomingMessage(String message){
          ConsoleHelper.writeMessage(message);
      }
      protected void informAboutAddingNewUser(String userName){
          ConsoleHelper.writeMessage(userName + " has joined the chat");
      }

      protected void informAboutDeletingNewUser(String userName){
          ConsoleHelper.writeMessage(userName + " has left the chat");
      }

      protected void notifyConnectionStatusChanged(boolean clientConnected){
          Client.this.clientConnected = clientConnected;

              synchronized (Client.this){
                  Client.this.notify();  //wake the main thread
              }

      }

    }
}
