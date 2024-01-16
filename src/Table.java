import java.util.*;

public class Table {
    public static final ArrayList<String> OPERATORS = new ArrayList<>(Arrays.asList("!=", "<=", ">=", "<", ">", "="));
    private final ArrayList<ArrayList<Object>> table ;  // Stores values in the table
    private final ArrayList<Class<?>> colType;  // Stores the type of data for each column
    private final HashMap<String, Integer> colIndex;  // Stores the index of each column

    /**
     * Constructor for Table class
     */
    private Table() {
        colType = new ArrayList<>();
        colIndex = new HashMap<>();
        table = new ArrayList<>();
    }

    public Table(ArrayList<String> rows) {
        colType = new ArrayList<>();
        colIndex = new HashMap<>();
        table = new ArrayList<>();

        buildTable(rows);
    }

    /**
     * Constructor for Table class
     *
     * @param colTypes - ArrayList of column types
     * @param colIndex - HashMap of column names and their index
     * @param rows     - ArrayList of rows
     */
    public Table(ArrayList<Class<?>> colTypes, HashMap<String, Integer> colIndex, ArrayList<ArrayList<Object>> rows) {
        this.colType = colTypes;
        this.colIndex = colIndex;
        table = rows;
    }

    public static boolean isTable(ArrayList<String> rows) {
        Table table = new Table();
        boolean buildSuccess = false;
        try {
            buildSuccess = table.buildTable(rows, true);
        } catch (Exception ignored) {
        }

        return buildSuccess;
    }

    /**
     * Returns the table with the specified name
     *
     * @param rows - ArrayList of rows
     * @param disablePrint - Boolean to disable printing of error messages
     * @return Table with the specified name
     */
    private boolean buildTable(ArrayList<String> rows, boolean disablePrint) {
        if (rows.size() < 2) {
            return false;
        }

        ArrayList<String> colName = new ArrayList<>(Arrays.asList(rows.get(0).split(",")));  // Get column names
        String[] firstValues = rows.get(1).split(",");  // Get first row of values
        updateCols(colName, firstValues);

        // Loop through each row and convert the values to their respective data types
        for (int i = 1; i < rows.size(); i++) {
            ArrayList<Object> row = new ArrayList<>();
            String[] cols = rows.get(i).split(",");

            // Verify that the number of values in the row matches the number of columns
            if (cols.length != colName.size()) {
                if(!disablePrint) System.out.println("Error: Invalid number of columns at row " + i);
                return false;
            }

            for (int j = 0; j < cols.length; j++) {
                String col = cols[j];
                try {
                    if (isString(col)) row.add(col.substring(1, col.length() - 1));
                    else if (col.contains(".")) row.add(Double.parseDouble(col));
                    else if (col.contains("true") || col.contains("false")) row.add(Boolean.parseBoolean(col));
                    else row.add(Integer.parseInt(col));
                } catch (Exception e) {
                    if(!disablePrint) System.out.println("Error: Invalid data type at row " + i + ", column " + j);
                    return false;
                }
            }
            table.add(row);
        }
        return true;
    }


    /**
     * Takes an ArrayList of rows and builds the table
     *
     * @param rows - ArrayList of rows
     */
    private void buildTable(ArrayList<String> rows) {
        buildTable(rows, false);
    }

    /**
     * Takes a row of values and converts it to the respective data types, then updates the column types and index
     *
     * @param colName - ArrayList of column names
     * @param row     - Array of values
     */
    private void updateCols(ArrayList<String> colName, String[] row) {
        for (int i = 0; i < colName.size(); i++) {
            String col = row[i];
            if (isString(col)) colType.add(String.class);
            else if (col.contains(".")) colType.add(Double.class);
            else if (col.contains("true") || col.contains("false")) colType.add(Boolean.class);
            else colType.add(Integer.class);

            colIndex.put(colName.get(i), i);
        }
    }

    /**
     * Returns a new table with the specified columns
     *
     * @param colNames - TreeSet of column names
     * @return New Table with projection applied
     */
    public Table projection(TreeSet<String> colNames) {
        ArrayList<Class<?>> newColType = new ArrayList<>();
        HashMap<String, Integer> newColIndex = new HashMap<>();
        ArrayList<ArrayList<Object>> newTable = new ArrayList<>();

        int i = 0;  // Index for new table
        // Loop through each column name and add it to the new table
        for (String colName : colNames) {
            newColIndex.put(colName, i++);
            if (!this.colIndex.containsKey(colName)) {
                System.err.println("Error: Column '" + colName + "' does not exist in the table");
                return null;
            }
            newColType.add(this.colType.get(this.colIndex.get(colName)));
        }

        // Loop through each row and add the values for the specified columns to the new table
        for (ArrayList<Object> row : table) {
            ArrayList<Object> newRow = new ArrayList<>();
            for (String colName : colNames) {
                newRow.add(row.get(colIndex.get(colName)));
            }
            newTable.add(newRow);
        }

        return new Table(newColType, newColIndex, newTable);
    }

    /**
     * Returns a new table with the specified rows based on the selection condition
     *
     * @param condition - String representing the condition to be applied
     * @return New Table with selection applied
     */
    public Table select(String condition) {
        ArrayList<ArrayList<Object>> newTable = new ArrayList<>();
        String[] parts = breakCondition(condition);
        if (parts == null) {
            System.err.println("Error: Invalid condition: "+ condition);
            return null;
        }

        // Process rows based on the selection condition
        for (ArrayList<Object> row : table) {
            // Evaluate the condition for each row
            if (evaluateCondition(row, parts)) {
                // Add matching rows to the new table
                newTable.add(row);
            }
        }

        return new Table(new ArrayList<>(colType), new HashMap<>(colIndex), newTable);
    }

    /**
     * Returns a new table with the specified set operation applied
     *
     * @param table    - Table to be used in the set operation
     * @param operator - Set operation to be applied (union ∪, intersection ∩, difference -)
     * @return New Table with set operation applied
     */
    public Table setOperation(Table table, char operator) {
        if (differentColumns(table)) {
            System.out.println("Error: Tables must have the same column names");
            return null;
        }

        TreeSet<ArrayList<Object>> set = new TreeSet<>(Comparator.comparingInt(ArrayList::hashCode));
        set.addAll(this.table);

        switch (operator) {
            case '∪' -> set.addAll(table.table);
            case '∩' -> set.retainAll(table.table);
            case '-' -> table.table.forEach(set::remove);
            default -> System.out.println("Error: Invalid set operation");
        }

        return new Table(new ArrayList<>(colType), new HashMap<>(colIndex), new ArrayList<>(set));
    }

    /**
     * Returns a new table with the specified join operation applied
     *
     * @param table     - Table to be joined
     * @param condition - String representing the condition to be applied in form "table1.column1=table2.column2"
     * @return New Table with join operation applied
     */
    public Table join(Table table, String condition) {
        if(table == this){
            table = new Table(new ArrayList<>(colType), new HashMap<>(colIndex), new ArrayList<>(this.table));
        }
        ArrayList<Class<?>> newColType = new ArrayList<>(this.colType);
        HashMap<String, Integer> newColIndex = new HashMap<>(this.colIndex);
        ArrayList<ArrayList<Object>> newTable = new ArrayList<>();
        String[] parts = breakCondition(condition);

        int removedCol = createColumnsWithCondition(table, newColIndex, newColType, parts);  // Update columns and return the index of a duplicate column if it exists

        for (ArrayList<Object> row1 : this.table) {
            for (ArrayList<Object> row2 : table.table) {
                if (evaluateCondition(row1, table, row2, parts)) {
                    newTable.add(mergeRows(row1, row2, removedCol));
                }
            }
        }
        if(newColIndex.size()!= newColType.size() || newColIndex.size() != newTable.getFirst().size()) {
            System.err.println("Error: Duplicate column names");
            return null;
        }
        return new Table(newColType, newColIndex, newTable);
    }

    /**
     * Helper method to create/fix columns for the join operation
     *
     * @param table       - Table to be joined
     * @param newColIndex - HashMap of column names and their index
     * @param newColType  - ArrayList of column types
     * @param parts       - Array of Strings representing the left operand, operator, and right operand of the condition
     * @return Array of Strings representing the new column names
     */
    private int createColumnsWithCondition(Table table, HashMap<String, Integer> newColIndex, ArrayList<Class<?>> newColType, String[] parts) {
        int i = this.colIndex.size();
        newColType.addAll(table.colType);
        String[] newColName = new String[2];  // Stores the new column names
        String[] conditionParts = new String[2];
        int removedColIndex = -1;  // Index of a removed column if it exists, otherwise -1

        // Store the column name without the table names
        conditionParts[0] = parts[0].contains(".") ? parts[0].substring(parts[0].indexOf(".") + 1) : parts[0];
        conditionParts[1] = parts[2].contains(".") ? parts[2].substring(parts[2].indexOf(".") + 1) : parts[2];
        newColName[0] = conditionParts[0];
        newColName[1] = conditionParts[1];

        if (conditionParts[0].equals(conditionParts[1])) {  // Duplicate column names
            if (parts[1].equals("=")) {  // Duplicate column must be removed
                if (!this.colIndex.containsKey(conditionParts[0])) {
                    System.err.println("Error: Column in condition does not exist in the table");
                    return -1;
                }
                newColName[1] = null;
            } else {  // Change the name of the duplicate columns
                newColName[0] = parts[0];
                newColName[1] = parts[2];
                if (parts[0].equals(parts[2])) {  // table.column format was used
                    newColName[0] += "_1";
                    newColName[1] += "_2";
                }
            }
        }

        // if the column name of the left table is changed, update the new table
        if(!newColIndex.containsKey(newColName[0])) {
            int index = newColIndex.remove(conditionParts[0]);
            newColIndex.put(newColName[0], index);
        }

        // Add columns from the right table
        for (String colName : table.getColsByIndex()) {
            if(newColName[1] != null && colName.equals(conditionParts[1]) && !(colName.equals(newColName[1]))) {  // case where column names are changed
                newColIndex.put(newColName[1], i++);
            }
            else if(!newColIndex.containsKey(colName)){  // case where column names are not changed
                newColIndex.put(colName, i++);
            } else {  // case where duplicate column was removed
                removedColIndex = table.colIndex.get(colName);
                newColType.remove(this.colType.size() +removedColIndex);
            }
        }

        return removedColIndex;
    }

    /**
     * Helper method to merge rows for the join operation
     *
     * @param row1           - ArrayList of values from the left table
     * @param row2           - ArrayList of values from the right table
     * @param duplicateIndex - Index of a removed column if it exists, otherwise -1
     * @return ArrayList of values for the new row
     */
    private ArrayList<Object> mergeRows(ArrayList<Object> row1, ArrayList<Object> row2, int duplicateIndex) {
        ArrayList<Object> newRow = new ArrayList<>(row1);
        ArrayList<Object> tempRow = new ArrayList<>(row2);
        if (duplicateIndex != -1) {
            tempRow.remove(duplicateIndex);
        }
        newRow.addAll(tempRow);
        return newRow;
    }

    /**
     * Returns an ArrayList of column names sorted by their index
     *
     * @return ArrayList of column names
     */
    private ArrayList<String> getColsByIndex() {
        ArrayList<String> cols = new ArrayList<>();
        colIndex.entrySet().stream().sorted(HashMap.Entry.comparingByValue()).forEach(entry -> cols.add(entry.getKey()));
        return cols;
    }

    /**
     * Helper method to break the condition into its parts
     *
     * @param condition - String representing the condition to be applied
     * @return Array of Strings length 3 representing the left operand, operator, and right operand
     */
    private String[] breakCondition(String condition) {
        String[] parts = new String[3];
        String op = null;
        for (String operator : OPERATORS) {
            if (condition.contains(operator)) {
                op = operator;
                break;
            }
        }
        if (op == null) {
            System.err.println("Invalid condition: " + condition);
            return null;
        }

        parts[0] = condition.substring(0, condition.indexOf(op)).trim();
        parts[1] = op;
        parts[2] = condition.substring(condition.indexOf(op) + op.length()).trim();

        return parts;
    }

    /**
     * Helper method to evaluate the selection condition for two given rows
     *
     * @param row1  - ArrayList of values from the left table
     * @param table - Table to be used in the join operation
     * @param row2  - ArrayList of values from the right table
     * @param parts - Array of Strings representing the left operand, operator, and right operand of the condition
     * @return True if the condition is satisfied, otherwise false
     */
    private boolean evaluateCondition(ArrayList<Object> row1, Table table, ArrayList<Object> row2, String[] parts) {
        if (parts == null) {
            return false;
        }
        String op = parts[1];
        // Remove the table name from the column names if it exists
        Object leftValue = getOperandObj(parts[0].trim(), this, row1);
        Object rightValue = getOperandObj(parts[2].trim(), table, row2);

        return evaluateCondition(leftValue, rightValue, op);
    }

    /**
     * Helper method to evaluate the selection condition for two given values
     *
     * @param leftValue  - Object representing the left value
     * @param rightValue - Object representing the right value
     * @param op         - String representing the operator
     * @return True if the condition is satisfied, otherwise false
     */
    private boolean evaluateCondition(Object leftValue, Object rightValue, String op) {
        int compareValues = compareValues(leftValue, rightValue);
        return switch (op) {
            case "<=" -> compareValues <= 0;
            case ">=" -> compareValues >= 0;
            case "<" -> compareValues < 0;
            case ">" -> compareValues > 0;
            case "!=" -> compareValues != 0;
            case "=" -> compareValues == 0;
            default -> {
                System.out.println("Unsupported operator: " + op);
                yield false;
            }
        };
    }

    /**
     * Helper method to evaluate the selection condition for a given row
     *
     * @param row       - ArrayList of values from the table
     * @param condition - Array of Strings representing the left operand, operator, and right operand of the condition
     * @return True if the condition is satisfied, otherwise false
     */
    private boolean evaluateCondition(ArrayList<Object> row, String[] condition) {
        return evaluateCondition(row, this, row, condition);
    }

    /**
     * Helper method to evaluate the value of an operand
     *
     * @param operand - String representing the operand
     * @param table   - Table to be used in the join operation
     * @param row     - ArrayList of values from the table
     * @return Object representing the value of the operand
     */
    private Object getOperandObj(String operand, Table table, ArrayList<Object> row) {
        if (isString(operand)) {  // Operand is a string
            return operand.substring(1, operand.length() - 1);
        } else if (operand.equalsIgnoreCase("true") || operand.equalsIgnoreCase("false")) {  // Operand is a boolean
            return Boolean.parseBoolean(operand);
        } else {
            try {  // Operand is an integer
                return Integer.parseInt(operand);
            } catch (Exception ignored) {
            }
            try {  // Operand is a double
                return Double.parseDouble(operand);
            } catch (Exception ignored) {
            }

            // o.w. Operand is a column name
            String tempOperand = operand.substring(operand.indexOf(".") + 1);
            if (!table.colIndex.containsKey(operand) && !table.colIndex.containsKey(tempOperand)) {
                System.err.println("Column '" + operand + "' does not exist in the table");
                return null;
            }
            return row.get(table.colIndex.getOrDefault(operand, table.colIndex.get(tempOperand)));  // Return the value of the column in the row
        }
    }

    /**
     * Helper method to check if the column types of two tables are the same
     * @param table - Table to be compared
     * @return True if the column types are the same, otherwise false
     */
    private boolean differentColumns(Table table) {
        return !this.colType.equals(table.colType) || !this.colIndex.equals(table.colIndex);
    }

    private boolean isString(String input) {
        return input.startsWith("'") && input.endsWith("'");
    }

    /**
     * Helper method to compare two values
     *
     * @param leftValue  - Object representing the left value
     * @param rightValue - Object representing the right value
     * @return 0 if the values are equal, -1 if the left value is less than the right value, 1 if the left value is greater than the right value
     */
    private int compareValues(Object leftValue, Object rightValue) {
        if (leftValue instanceof Comparable && rightValue instanceof Comparable) {
            @SuppressWarnings("unchecked") Comparable<Object> leftComparable = (Comparable<Object>) leftValue;
            return leftComparable.compareTo(rightValue);
        }
        throw new IllegalArgumentException("Cannot compare non-comparable values");
    }

    /**
     * Prints the table aligned by column
     */
    public void printTable() {
        // Calculate column widths based on the largest element and column name in each column
        int[] colWidths = new int[colIndex.size()];

        // Consider column names
        for (String columnName : colIndex.keySet()) {
            int columnIndex = colIndex.get(columnName);
            colWidths[columnIndex] = Math.max(colWidths[columnIndex], columnName.length());
        }

        // Consider data elements
        for (ArrayList<Object> row : table) {
            for (int i = 0; i < row.size(); i++) {
                int currentWidth = String.valueOf(row.get(i)).length();
                if (row.get(i) instanceof String) currentWidth += 2;  // Account for quotes
                colWidths[i] = Math.max(colWidths[i], currentWidth);
            }
        }

        // Print top border
        printBorder(colWidths);

        // Print header
        for (String columnName : this.getColsByIndex()) {
            System.out.printf("| %-" + colWidths[colIndex.get(columnName)] + "s ", columnName);
        }
        System.out.println("|");

        // Print middle border
        printBorder(colWidths);

        // Print rows
        for (ArrayList<Object> row : table) {
            for (int i = 0; i < row.size(); i++) {
                Object value = row.get(i);
                if(value instanceof String) value = "'" + value + "'";  // Add quotes to strings
                System.out.printf("| %-" + colWidths[i] + "s ", value);
            }
            System.out.println("|");
        }

        // Print bottom border
        printBorder(colWidths);
    }

    private void printBorder(int[] colWidths) {
        for (int width : colWidths) {
            System.out.print("+" + "-".repeat(width + 2));  // 2 accounts for padding and border
        }
        System.out.println("+");
    }

}
