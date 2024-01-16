import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Query query = new Query();
        StringBuilder userInputBuilder = new StringBuilder();
        String userInput;
        boolean activeStatement = false;
        System.out.println("Enter a query and type 'finish.' to send query, 'help.' for help or 'exit.' to quit.");

        while (true) {
            if(!activeStatement){
                System.out.print("Start a new query or type 'help.' for help.\n>>> ");
            } else {
                System.out.print(">>> ");
            }
            userInput = scanner.nextLine();
            activeStatement = true;
            if (userInput.contains("finish.")) {
                userInputBuilder.append(userInput, 0, userInput.indexOf("finish."));
                query.parseQuery(userInputBuilder.toString());
                activeStatement = false;
                userInputBuilder = new StringBuilder();
            }
            else if (userInput.toLowerCase().contains("exit.")) {
                System.out.println("Exiting terminal.");
                break;
            }
            else if (userInput.equals("help.")) {
                System.out.println("Type 'finish.' to send query,\n'help.' for help,\n'clear.' to clear query,\n'print.' to print query\nor 'exit.' to quit.");
            }
            else if (userInput.equals("clear.")) {
                userInputBuilder = new StringBuilder();
            }
            else if (userInput.equals("print.")) {
                System.out.println(userInputBuilder);
            }
            else {
                userInputBuilder.append(userInput).append("\n");
            }
        }

        scanner.close();
    }
}