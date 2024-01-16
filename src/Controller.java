import java.util.Scanner;

public class Controller {
    private final Query query;
    private final Scanner scanner;
    private StringBuilder userInputBuilder;
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
                if(!activeStatement){
                    System.out.print("Start a new query or type 'help.' for help.\n>>> ");
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
                    userInputBuilder = new StringBuilder();
                }
                else if (userInput.toLowerCase().contains("exit.")) {
                    System.out.println("Exiting terminal.");
                    break;
                }
                else if (userInput.equals("help.")) {
                    System.out.println("""
                            Type 'finish.' to send query or 'tableName finish.' to print a saved table
                            'help.' for help,
                            'clear.' to clear query,
                            'print.' to print the current query,
                            'last' to print last table,
                            'tables.' to print all the name of the tables,
                            'saveLast[name]. to save the last query. Replace 'name' in command with new name for table
                            or 'exit.' to quit.""");
                }
                else if (userInput.equals("clear.")) {
                    userInputBuilder = new StringBuilder();
                    System.out.println("Cleared query.");
                    System.out.print(">>> ");
                }
                else if (userInput.equals("print.")) {
                    System.out.println(userInputBuilder);
                    System.out.print(">>> ");
                }
                else if (userInput.equals("last.")) {
                    query.printLastTable();
                    System.out.print(">>> ");
                } else if (userInput.equals("tables.")) {
                    query.printTables();
                    System.out.print(">>> ");
                }
                else if (userInput.matches("^saveLast\\[[A-Za-z][A-Za-z1-9]+].$")) {
                    String name = userInput.substring(userInput.indexOf("[") + 1, userInput.indexOf("]"));
                    if (query.saveTable(name)) System.out.println("Saved last table as " + name + ".");
                    else System.out.println("Failed to save table.");
                    System.out.print(">>> ");
                }
                else {
                    userInputBuilder.append(userInput).append("\n");
                }
            }

            scanner.close();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
