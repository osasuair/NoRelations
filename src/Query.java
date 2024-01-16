import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.TreeSet;

public class Query {
    private static final ArrayList<Character> BI_QUERY_OPERATORS = new ArrayList<>(Arrays.asList('∪', '∩', '-', '⨝'));
    private final HashMap<String, Table> tableHashMap;

    private Table lastTable = null;

    public Query() {
        tableHashMap = new HashMap<>();
    }

    /**
     * Parses a query and prints the result
     *
     * @param query the query to be parsed
     */
    public void parseQuery(String query) {
        String exactQuery = parseNamedTables(query.replaceAll("[ +]", " ").replaceAll("\n+", "\n"));
        Table table = queryHelper(exactQuery);
        if(table == null) return;
        lastTable = table;
        table.printTable();
    }

    /**
     * Recursive method to parse the query and returns the result
     *
     * @param query the query to be parsed
     * @return the result of the query
     */
    private Table queryHelper(String query) {
        if ((query = query.trim()).isEmpty()) return null;

        int queryOpIndex = -1;
        char[] queryArr = query.toCharArray();
        String[] args;

        // check if query is a table
        if (isTable(query)) {
            return getTable(query);
        }

        // check if outermost query is a projection
        if (query.startsWith("π") || query.startsWith("project ")) {
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

        // check if the query is a selection
        if (query.startsWith("σ") || query.startsWith("select ")) {
            args = query.split(" ", 3);
            if (args.length != 3) {
                System.out.println("Invalid Query");
                return null;
            }

            Table table = queryHelper(args[2]);
            assert table != null;
            return table.select(args[1]);
        }

        // find query operator
        // if query is wrapped in parentheses, call queryHelper on the query inside the parentheses
        // o.w. call queryHelper on left Operand
        if (queryArr[0] == '(') {
            int bracketCount = 1;
            for (queryOpIndex = 1; queryOpIndex < queryArr.length; ++queryOpIndex) {
                if (query.charAt(queryOpIndex) == '(') {
                    bracketCount++;
                } else if (query.charAt(queryOpIndex) == ')') {
                    bracketCount--;
                }
                if (bracketCount == 0) {
                    break;
                }
            }

            // check if query is wrapped in parentheses
            if (bracketCount == 0 && queryOpIndex == queryArr.length - 1)
                return queryHelper(query.substring(1, query.length() - 1));
            else if (bracketCount == 0) {
                queryOpIndex++;  // There are brackets surrounding the left operand so
                //    skip past the closing bracket to find the operator
            } else {
                System.out.println("Invalid Query - Parentheses Mismatch");
                return null;
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
            return null;
        }
        Table leftOperand = queryHelper(query.substring(0, queryOpIndex));

        // handle binary query operators
        if (queryArr[queryOpIndex] == '⨝') {
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

        } else {
            // get right operand
            char queryOperator = queryArr[queryOpIndex];
            query = query.substring(queryOpIndex + 1);
            Table rightOperand = queryHelper(query);
            assert leftOperand != null;
            assert rightOperand != null;
            return leftOperand.setOperation(rightOperand, queryOperator);
        }
    }

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

    private boolean isTable(String tableStr) {
        if(tableStr.isEmpty()) return false;
        if(!tableStr.contains("={") &&tableStr.contains("{") && !tableStr.startsWith("{")) return false;
        if (tableStr.startsWith("(") && tableStr.endsWith(")")) tableStr = tableStr.substring(1, tableStr.length() - 1);
        if (tableHashMap.containsKey(tableStr)) return true;
        tableStr = tableStr.replaceAll("[\\p{Ps}\\p{Pe} ]", "").trim();
        ArrayList<String> rows = new ArrayList<>(Arrays.asList(tableStr.split("\n")));
        return Table.isTable(rows);
    }

    public boolean saveTable(String name){
        if(lastTable == null) {
            System.err.println("No table to save");
            return false;
        }
        if(tableHashMap.containsKey(name)) {
            System.err.println("Table already exists");
            return false;
        }
        tableHashMap.put(name, lastTable);
        return true;
    }

    public void printLastTable(){
        if(lastTable == null) {
            System.out.println("No table to print");
            return;
        }
        lastTable.printTable();
    }

    public void printTables(){
        System.out.println("Tables:");
        for(String key : tableHashMap.keySet()){
            System.out.println(key);
        }
        System.out.println();
    }

    public Table getTable(String tableStr) {
        if (tableStr.startsWith("(") && tableStr.endsWith(")")) tableStr = tableStr.substring(1, tableStr.length() - 1);
        if (tableHashMap.containsKey(tableStr)) return tableHashMap.get(tableStr);
        tableStr = tableStr.replaceAll("[\\p{Ps}\\p{Pe} ]", "").trim();
        ArrayList<String> rows = new ArrayList<>(Arrays.asList(tableStr.split("\n")));
        return new Table(rows);
    }
}
