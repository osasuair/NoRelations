import java.util.Scanner;

public class Controller {
    private final Query query;
    private final Scanner scanner;
    private final StringBuilder userInputBuilder;
    private String userInput;
    private boolean activeStatement;

    public Controller() {
        query = new Query();
        scanner = new Scanner(System.in);
        userInputBuilder = new StringBuilder();
        userInput = "";
        activeStatement = false;
    }

    public void start() {
        try {
            System.out.println("Enter a query and type 'finish.' to send query, 'help.' for help or commands, or 'exit.' to quit.");

            while (true) {
                if (!activeStatement) {
                    printPrompt("Start a new query or type 'help.' for help.");
                }
                userInput = scanner.nextLine();
                activeStatement = true;
                if (userInput.contains("finish.")) {
                    userInputBuilder.append(userInput, 0, userInput.indexOf("finish."));
                    try {
                        query.parseQuery(userInputBuilder.toString());
                    } catch (Exception e) {
                        System.out.println("Error: " + e.getMessage());
                        System.out.println("Query failed.");
                    }
                    activeStatement = false;
                    userInputBuilder.setLength(0);
                } else if (userInput.toLowerCase().contains("exit.")) {
                    System.out.println("Exiting terminal.");
                    break;
                } else if (userInput.equals("help.")) {
                    printHelp();
                } else if (userInput.equals("clear.")) {
                    userInputBuilder.setLength(0);
                    printPrompt("Cleared query.");
                } else if (userInput.equals("print.")) {
                    System.out.println(userInputBuilder);
                    printPrompt("");
                } else if (userInput.equals("last.")) {
                    query.printLastTable();
                    printPrompt("");
                } else if (userInput.equals("tables.")) {
                    query.printTables();
                    printPrompt("");
                } else if (isSaveLastCommand(userInput)) {
                    handleSaveLastCommand(userInput);
                } else {
                    userInputBuilder.append(userInput).append("\n");
                }
            }

            scanner.close();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void printPrompt(String message) {
        if (!message.isEmpty()) {
            System.out.println(message);
        }
        System.out.print(">>> ");
    }

    private void printHelp() {
        System.out.println("""
                Type 'finish.' to send query or 'tableName finish.' to print a saved table
                'help.' for help,
                'clear.' to clear query,
                'print.' to print the current query,
                'last.' to print last table,
                'tables.' to print all the name of the tables,
                'saveLast[name]. to save the last query. Replace 'name' in command with new name for table
                or 'exit.' to quit.""");
    }

    private boolean isSaveLastCommand(String userInput) {
        return userInput.startsWith("saveLast[") && userInput.endsWith("].");
    }

    private void handleSaveLastCommand(String userInput) {
        String name = userInput.substring(userInput.indexOf("[") + 1, userInput.indexOf("]"));
        if (query.saveTable(name)) System.out.println("Saved last table as " + name + ".");
        else System.out.println("Failed to save table.");
        printPrompt("");
    }
}
