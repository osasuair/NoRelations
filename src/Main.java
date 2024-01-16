import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Query query = new Query();
        StringBuilder userInputBuilder = new StringBuilder();
        String userInput;
        boolean activeStatement = false;

        while (true) {
            if(!activeStatement){
                System.out.println("Enter a query and type 'finish.' to send query, help, or 'exit,' to quit: ");
            }
            userInput = scanner.nextLine();
            if (!activeStatement && userInput.toLowerCase().contains("exit.")) {
                System.out.println("Exiting terminal.");
                break;
            }
            if (!activeStatement && userInput.contains("finish.")) {
                userInput = userInput.substring(0, userInput.indexOf("finish."));
                userInputBuilder.append(userInput);
                query.parseQuery(userInputBuilder.toString());
            }

            userInputBuilder.append(userInput);
            if (!activeStatement) {
                userInput = userInputBuilder.toString();
                query.parseQuery(userInput);
                System.out.println("Query executed successfully.\n");
                userInputBuilder.setLength(0);
            }
        }

        scanner.close();
    }
}