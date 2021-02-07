package task.client;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ClientGuiModel {
    private final Set<String> allUserNames = new HashSet<>();  //store a list of all the chat participants
    private String newMessage; //store the new message received by the task.client

    public void addUser(String newUserName){
        allUserNames.add(newUserName);
    }
    public void deleteUser(String userName){
        allUserNames.remove(userName);
    }

    public Set<String> getAllUserNames() {
        return Collections.unmodifiableSet(allUserNames);//prevents modification of the returned Set
    }

    public String getNewMessage() {
        return newMessage;
    }

    public void setNewMessage(String newMessage) {
        this.newMessage = newMessage;
    }
}
