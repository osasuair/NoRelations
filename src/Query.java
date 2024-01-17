import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.TreeSet;

public class Query {
    private static final ArrayList<Character> UNI_QUERY_OPERATORS = new ArrayList<>(Arrays.asList('π', 'σ'));
    private static final ArrayList<Character> BI_QUERY_OPERATORS = new ArrayList<>(Arrays.asList('∪', '∩', '-', '⨝'));
    private static final ArrayList<String> QUERY_OPERATORS_STR = new ArrayList<>(Arrays.asList("project", "select", "union", "intersection", "difference", "join"));
    private final HashMap<String, Table> tableHashMap;

    private Table lastTable = null;

    public Query() {
        tableHashMap = new HashMap<>();
    }

    /**
     * Finds the index of the query operator
     *
     * @param query        the query to be parsed
     * @param queryArr     the query as a char array
     * @param queryOpIndex the index of the query operator
     * @return the index of the query operator
     */
    private static int getQueryOpIndex(String query, char[] queryArr, int queryOpIndex) {
        // find query operator
        if (queryArr[0] == '(') {
            int bracketCount = 1;
            for (queryOpIndex = 1; queryOpIndex < queryArr.length; ++queryOpIndex) {
                if (queryArr[queryOpIndex] == '(') {
                    bracketCount++;
                } else if (queryArr[queryOpIndex] == ')') {
                    bracketCount--;
                }
                if (bracketCount == 0) {
                    break;
                }
            }

            if (bracketCount == 0) {
                queryOpIndex++;  // There are brackets surrounding the left operand so
                //    skip past the closing bracket to find the operator
            } else {
                System.out.println("Invalid Query - Parentheses Mismatch");
                return -1;
            }
        }

        if (queryOpIndex == -1) queryOpIndex = 0;
        for (; queryOpIndex < query.length(); ++queryOpIndex) {
            if (BI_QUERY_OPERATORS.contains(queryArr[queryOpIndex])) {
                break;  // found query operator
            }
        }
        if (queryOpIndex == query.length()) {
            System.err.println("Invalid Query - '" + query + "' is not a valid query or table");
            return -1;
        }
        return queryOpIndex;
    }

    /**
     * Parses a query and prints the result
     *
     * @param query the query to be parsed
     */
    public void parseQuery(String query) {
        String exactQuery = parseNamedTables(query.replaceAll("[ +]", " ").replaceAll("\n+", "\n"));
        if (exactQuery == null) return;
        exactQuery = replaceKeys(exactQuery);
        Table table = queryHelper(exactQuery);
        if (table == null) return;
        lastTable = table;
        table.printTable();
    }

    /**
     * Replaces the query operators keywords with their corresponding symbols
     *
     * @param exactQuery the query to be parsed
     * @return the query with the operators replaced
     */
    private String replaceKeys(String exactQuery) {
        ArrayList<Character> allOperators = new ArrayList<>(UNI_QUERY_OPERATORS);
        allOperators.addAll(BI_QUERY_OPERATORS);
        for (int i = 0; i < allOperators.size(); i++) {
            exactQuery = exactQuery.replace(QUERY_OPERATORS_STR.get(i) + " ", allOperators.get(i).toString() + " ");
        }
        return exactQuery;
    }

    /**
     * Recursive method to parse the query and returns the result
     *
     * @param query the query to be parsed
     * @return the result of the query as a table
     */
    private Table queryHelper(String query) {
        if ((query = query.trim()).isEmpty()) return null;
        while (true) {  // remove wrapping brackets
            if (query.equals(query = removeWrappingBracket(query))) break;
        }

        int queryOpIndex = -1;
        char[] queryArr = query.toCharArray();

        // check if query is a table
        if (isTable(query)) {
            return getTable(query);
        }

        // check if outermost query is a projection
        if (query.startsWith("π")) {
            return handleProjection(query);
        }

        // check if the query is a selection
        if (query.startsWith("σ")) {
            return handleSelection(query);
        }

        queryOpIndex = getQueryOpIndex(query, queryArr, queryOpIndex);
        if (queryOpIndex == -1) return null;

        Table leftOperand = queryHelper(query.substring(0, queryOpIndex));
        // handle binary query operators
        if (queryArr[queryOpIndex] == '⨝') {
            return handleJoin(query, queryArr, queryOpIndex, leftOperand);

        } else {
            assert leftOperand != null;
            return handleSetOperation(query, queryArr, queryOpIndex, leftOperand);
        }
    }

    /**
     * Removes wrapping brackets from a query
     *
     * @param query the query to be parsed
     * @return the query without wrapping brackets
     */
    private String removeWrappingBracket(String query) {
        if (!(query.startsWith("(") && query.endsWith(")")))
            return query;

        int i, bracketCount = 1;
        for (i = 1; i < query.length(); ++i) {
            if (query.charAt(i) == '(') {
                bracketCount++;
            } else if (query.charAt(i) == ')') {
                bracketCount--;
            }
            if (bracketCount == 0) {
                break;
            }
        }
        // check if query is wrapped in parentheses
        if (bracketCount == 0 && i == query.length() - 1)
            return query.substring(1, query.length() - 1);
        return query;
    }

    /**
     * Handles the set operations - union, intersection, and difference
     *
     * @param query        the query to be parsed
     * @param queryArr     the query as a char array
     * @param queryOpIndex the index of the query operator
     * @param leftOperand  the left operand of the query
     * @return the result of the set operation
     */
    private Table handleSetOperation(String query, char[] queryArr, int queryOpIndex, Table leftOperand) {
        // get right operand
        char queryOperator = queryArr[queryOpIndex];
        query = query.substring(queryOpIndex + 1);
        Table rightOperand = queryHelper(query);
        assert leftOperand != null;
        assert rightOperand != null;
        return leftOperand.setOperation(rightOperand, queryOperator);
    }

    /**
     * Handles the join operation
     *
     * @param query        the query to be parsed
     * @param queryArr     the query as a char array
     * @param queryOpIndex the index of the query operator
     * @param leftOperand  the left operand of the query
     * @return the result of the join operation
     */
    private Table handleJoin(String query, char[] queryArr, int queryOpIndex, Table leftOperand) {
        // if it doesn't, find operator and walk to end of right operand (either first space or bracket)
        int rightOperandIndex;

        for (rightOperandIndex = queryOpIndex + 1; rightOperandIndex < queryArr.length; ++rightOperandIndex) {
            if (Table.OPERATORS.contains("" + queryArr[rightOperandIndex])) {
                break;
            }
        }
        if (rightOperandIndex == queryArr.length) {
            System.err.println("Invalid Query - No Operator Found when Expected");
            return null;
        }
        if (queryArr[++rightOperandIndex] != '(') {
            while (rightOperandIndex < queryArr.length && queryArr[rightOperandIndex] != ' ' && queryArr[rightOperandIndex] != '(') {
                rightOperandIndex++;
            }
        }
        if (rightOperandIndex == queryArr.length) {
            System.err.println("Invalid Query - No Right Operand Found when Expected");
            return null;
        }

        Table rightOperand = queryHelper(query.substring(rightOperandIndex));
        assert leftOperand != null;
        assert rightOperand != null;
        return leftOperand.join(rightOperand, query.substring(queryOpIndex + 1, rightOperandIndex).trim());
    }

    /**
     * Handles the selection operation
     *
     * @param query the query to be parsed
     * @return the result of the selection operation
     */
    private Table handleSelection(String query) {
        String[] args;
        args = query.split(" ", 3);
        if (args.length != 3) {
            System.out.println("Invalid Query");
            return null;
        }

        Table table = queryHelper(args[2]);
        assert table != null;
        return table.select(args[1]);
    }

    /**
     * Handles the projection operation
     *
     * @param query the query to be parsed
     * @return the result of the projection operation
     */
    private Table handleProjection(String query) {
        String[] args;
        query = query.replace(", ", ",");
        args = query.split(" ", 3);

        if (args.length != 3) {
            System.out.println("Invalid Query");
            return null;
        }

        Table table = queryHelper(args[2]);
        assert table != null;
        return table.projection(new TreeSet<>(Arrays.asList(args[1].split(","))));
    }

    /**
     * Parses the named tables in the query and adds them to the tableHashMap
     *
     * @param query the query to be parsed
     * @return the query without the named tables
     */
    public String parseNamedTables(String query) {

        boolean namedTableExists = true;
        while (namedTableExists && !query.isEmpty()) {
            String[] splitQuery = query.split("}", 2);
            splitQuery[0] = splitQuery[0].replace(" ", "").trim();

            if (splitQuery[0].contains("={")) {
                String[] table = splitQuery[0].split("=\\{");   // Split the table in the query into name and rows
                tableHashMap.put(table[0], stringToTable(table[1]));
                query = splitQuery[1].trim();
            } else {
                namedTableExists = false;
            }
        }
        return query.trim();
    }

    /**
     * Converts a string to a table
     *
     * @param tableStr the string to be converted
     * @return the table
     */
    private Table stringToTable(String tableStr) {
        tableStr = tableStr.replaceAll("[\\p{Ps}\\p{Pe} ]", "").trim();
        Table table;

        if (!tableHashMap.containsKey(tableStr)) {
            ArrayList<String> rows = new ArrayList<>(Arrays.asList(tableStr.split("\n")));
            table = new Table(rows);
        } else {
            table = tableHashMap.get(tableStr);
        }
        if (table == null) {
            System.out.println("Invalid Table");
            return null;
        }
        return table;
    }

    /**
     * Checks if a string is a table
     *
     * @param tableStr the string to be checked
     * @return true if the string is a table, false otherwise
     */
    private boolean isTable(String tableStr) {
        if (tableStr.isEmpty()) return false;
        if (!tableStr.contains("={") && tableStr.contains("{") && !tableStr.startsWith("{")) return false;
        if (tableStr.startsWith("(") && tableStr.endsWith(")")) tableStr = tableStr.substring(1, tableStr.length() - 1);
        if (tableHashMap.containsKey(tableStr)) return true;
        tableStr = tableStr.replaceAll("[\\p{Ps}\\p{Pe} ]", "").trim();
        ArrayList<String> rows = new ArrayList<>(Arrays.asList(tableStr.split("\n")));
        return Table.isTable(rows);
    }

    /**
     * Saves the last table to the tableHashMap
     *
     * @param name the name of the table
     * @return true if the table was saved, false otherwise
     */
    public boolean saveTable(String name) {
        if (lastTable == null) {
            System.out.println("No table to save");
            return false;
        }
        if (tableHashMap.containsKey(name)) {
            System.err.println("Table name already exists");
            return false;
        }
        tableHashMap.put(name, lastTable);
        return true;
    }

    /**
     * Prints the last table
     */
    public void printLastTable() {
        if (lastTable == null) {
            System.out.println("No table to print");
            return;
        }
        lastTable.printTable();
    }

    /**
     * Prints the names of the tables in the tableHashMap
     */
    public void printTables() {
        System.out.println("Tables:");
        for (String key : tableHashMap.keySet()) {
            System.out.println(key);
        }
        System.out.println();
    }

    /**
     * Gets a table from the tableHashMap or creates a new table from a string
     *
     * @param tableStr the string to be converted to a table
     * @return the table
     */
    public Table getTable(String tableStr) {
        if (tableStr.startsWith("(") && tableStr.endsWith(")")) tableStr = tableStr.substring(1, tableStr.length() - 1);
        if (tableHashMap.containsKey(tableStr)) return tableHashMap.get(tableStr);
        tableStr = tableStr.replaceAll("[\\p{Ps}\\p{Pe} ]", "").trim();
        ArrayList<String> rows = new ArrayList<>(Arrays.asList(tableStr.split("\n")));
        return new Table(rows);
    }
}
