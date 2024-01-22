import java.util.Scanner;
import com.google.common.base.Optional;
import org.apache.logging.log4j.*;

public class Controller {
    private static final Logger logger = LogManager.getLogger(Controller.class);
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
                    finishQuery();
                    printPrompt("");
                } else if (userInput.toLowerCase().contains("exit.")) {
                    printPrompt("Exiting terminal.");
                    break;
                } else if (userInput.equals("help.")) {
                    printHelp();
                } else if (userInput.equals("clear.")) {
                    userInputBuilder.setLength(0);
                    printPrompt("Cleared query.");
                } else if (userInput.equals("print.")) {
                    printPrompt(userInputBuilder.toString());
                } else if (userInput.equals("last.")) {
                    Optional<Table> lastTable = query.getLastTable();
                    if(lastTable.isPresent()) printTable(lastTable.get());
                    else printPrompt("No last table.");
                } else if (userInput.equals("lastAsRelation.")) {
                    Optional<Table> lastTable = query.getLastTable();
                    if(lastTable.isPresent()) printPrompt(lastTable.get().toString());
                    else printPrompt("No last table.");
                } else if (userInput.equals("tables.")) {
                    printPrompt(query.tablesToString());
                } else if (isSaveLastCommand(userInput)) {
                    handleSaveLastCommand(userInput);
                } else if (isExportCommand(userInput)) {
                    handleExportCommand(userInput);
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

    /**
     * Handles export command.
     * if user input is exportLast. then export last table as table.csv
     * if user input is exportLast[name]. then export last table as name.csv
     *
     * @param userInput user input
     */
    private void handleExportCommand(String userInput) {
        String name;
        if(userInput.equals("exportLast.")) name = "table";
        else name = userInput.substring(userInput.indexOf("[") + 1, userInput.indexOf("]"));

        Optional<Table> lastTable = query.getLastTable();
        if(lastTable.isPresent()) {
            if (!ExportToCSV.exportToCSV(lastTable.get(), name)) {
                logger.error("Failed to export last table.");
                return;
            }
            printPrompt("Exported last table as " + name + ".csv");
        }
        else {
            printPrompt("No last table.");
        }
    }

    /**
     * Checks if user input is export command.
     *
     * @param userInput user input
     * @return true if user input is export command
     */
    private boolean isExportCommand(String userInput) {
        return userInput.startsWith("exportLast[") && userInput.endsWith("].") || userInput.equals("exportLast.");
    }

    /**
     * Parses query and prints table.
     */
    private void finishQuery() {
        userInputBuilder.append(userInput, 0, userInput.indexOf("finish."));
        try {
            Optional<Table> result = query.parseQuery(userInputBuilder.toString());
            if(result.isPresent()) printTable(result.get());
            else System.out.println("No query.");
        } catch (Exception e) {
            logger.error("Error: " + e.getMessage());
            logger.error("Query failed.");
        }
        activeStatement = false;
        userInputBuilder.setLength(0);
    }

    /**
     * Prints prompt.
     *
     * @param message message to print
     */
    private void printPrompt(String message) {
        if (!message.isEmpty()) {
            System.out.println(message);
        }
        System.out.print(">>> ");
    }

    /**
     * Prints help.
     */
    private void printHelp() {
        System.out.println("""
                Type 'finish.' to send query or 'tableName finish.' to print a saved table
                'help.' for help,
                'clear.' to clear query,
                'print.' to print the current query,
                'last.' to print last table,
                'lastAsRelation.' to print last table as relation,
                'tables.' to print all the name of the tables,
                'saveLast[name].' to save the last query. Replace 'name' in command with new name for table
                'exportLast[name].' to export the last query as a csv. Replace 'name' in command with new name for table
                'exportLast.' to export the last query as a csv with default name table.csv
                or 'exit.' to quit.""");
    }

    private boolean isSaveLastCommand(String userInput) {
        return userInput.startsWith("saveLast[") && userInput.endsWith("].");
    }

    private void handleSaveLastCommand(String userInput) {
        String name = userInput.substring(userInput.indexOf("[") + 1, userInput.indexOf("]"));
        if (query.saveTable(name)) System.out.println("Saved last table as " + name + ".");
        else logger.error("Failed to save table.");
        printPrompt("");
    }

    private void printTable(Table table) {
        table.printTable();
    }
}
