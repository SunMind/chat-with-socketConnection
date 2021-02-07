package task.client;

import task.ConsoleHelper;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class BotClient extends Client {

    public static void main(String[] args) {
        new BotClient().run();
    }

    @Override
    protected Client.SocketThread getSocketThread() {
        return new BotSocketThread();
    }

    @Override
    protected boolean shouldSendTextFromConsole() {
        // The bot must not send the text entered from the console
        return false;
    }

    @Override
    protected String getUserName() {
        return "date_bot_" + (int) (Math.random() * 100);
    }

    //inner class
    public class BotSocketThread extends SocketThread{

        @Override
        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            sendTextMessage("Hello, there. I'm a bot. I understand the following commands: date, day, month, year, time, hour, minutes, seconds.");
            super.clientMainLoop();
        }

        @Override
        protected void processIncomingMessage(String message) {
            // Display the text of the message on the console
            ConsoleHelper.writeMessage(message);

            // Separate the sender from the message text
            String userNameDelimiter = ": ";
            String[] split = message.split(userNameDelimiter);
            if (split.length != 2) return;

            String messageWithoutUserName = split[1];

            // Prepare a format string for sending the date as per the request
            String format = null;
            switch (messageWithoutUserName) {
                case "date":
                    format = "d.MM.YYYY";
                    break;
                case "day":
                    format = "d";
                    break;
                case "month":
                    format = "MMMM";
                    break;
                case "year":
                    format = "YYYY";
                    break;
                case "time":
                    format = "H:mm:ss";
                    break;
                case "hour":
                    format = "H";
                    break;
                case "minutes":
                    format = "m";
                    break;
                case "seconds":
                    format = "s";
                    break;
            }

            if (format != null) {
                String answer = new SimpleDateFormat(format).format(Calendar.getInstance().getTime());
                BotClient.this.sendTextMessage("Information for " + split[0] + ": " + answer);
            }
        }
    }
}
