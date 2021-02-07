package task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

//helper class for reading or writing to the console.
public class ConsoleHelper {

    private static BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    public static void writeMessage(String message){
        System.out.println(message);
    }

    public static String readString(){
        String input = null;
        while (input == null){
            try {
                input = reader.readLine();
            } catch (IOException e) {
                writeMessage("An error occurred while trying to enter text. Try again.");
            }
        }
            return input;
    }

    public static int readInt(){
        while (true){
        try {
        return Integer.parseInt(readString().trim());
        }catch (NumberFormatException numberFormatException){
           writeMessage("An error while trying to enter a number. Try again.");
        }
        }
    }
}
