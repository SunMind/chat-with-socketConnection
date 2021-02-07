package task;


import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketAddress;

//representing the connection between a task.client and the server
public class Connection implements Closeable {
  private final Socket socket;
  private final ObjectOutputStream out ;
  private final ObjectInputStream in ;

    public Connection(Socket socket) throws IOException {
            this.socket = socket;
            this.out = new ObjectOutputStream(socket.getOutputStream());
            this.in = new ObjectInputStream(socket.getInputStream());
    }

    public void send(Message message) throws IOException{
        synchronized (out){
            out.writeObject(message);
            out.flush();
        }

    }

    public Message receive() throws IOException, ClassNotFoundException{
        Message message = null;
        synchronized (in){
            message = (Message) in.readObject();
        }
        return message;
    }

    public SocketAddress getRemoteSocketAddress(){
        return socket.getRemoteSocketAddress();
    }

    @Override
    public void close() throws IOException {
        in.close();
        out.close();
        socket.close();
    }
}
