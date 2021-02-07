package task;

//responsible for the types of messages sent between a task.client and the server.
public enum MessageType {
     NAME_REQUEST, // – A name request.
     USER_NAME, //– A username.
     NAME_ACCEPTED, //– The name is accepted.
     TEXT, //– A text message.
     USER_ADDED, //– The user was added.
     USER_REMOVED; //– The user was deleted.
}
