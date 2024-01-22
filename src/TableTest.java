import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class TableTest {

    @Test
    void testSelect() {
        ArrayList<String> rows = new ArrayList<>(Arrays.asList("Name, Age, Height", "'John', 25, 6.0", "'Jane', 23, 5.5", "'Jack', 27, 5.9"));
        Table table = new Table(rows);
        Table selectedTable = table.select("Age>25");
        assertEquals(1, selectedTable.getTable().size());
    }

    @Test
    void testProjection() {
        ArrayList<String> rows = new ArrayList<>(Arrays.asList("Name, Age, Height", "'John', 25, 6.0", "'Jane', 23, 5.5", "'Jack', 27, 5.9"));
        Table table = new Table(rows);
        Table projectedTable = table.projection(new TreeSet<>(Arrays.asList("Name", "Age")));
        assertEquals(new HashSet<>(Arrays.asList("Name", "Age")), projectedTable.getColumns());
    }

    @Test
    void testJoin() {
        ArrayList<String> rows1 = new ArrayList<>(Arrays.asList("Name, Age", "'John', 25", "'Jane', 23", "'Jack', 27"));
        ArrayList<String> rows2 = new ArrayList<>(Arrays.asList("Name, Height", "'John', 6.0", "'Jane', 5.5", "'Jack', 5.9"));
        Table table1 = new Table(rows1);
        Table table2 = new Table(rows2);
        Table joinedTable = table1.join(table2, "Name=Name");
        assertEquals(3, joinedTable.getTable().size());
    }

    @Test
    void testSetOperations() {
        ArrayList<String> rows1 = new ArrayList<>(Arrays.asList("Name, Age", "'John', 25", "'Jane', 23", "'Jack', 27"));
        ArrayList<String> rows2 = new ArrayList<>(Arrays.asList("Name, Age", "'John', 25", "'Jane', 23"));
        Table table1 = new Table(rows1);
        Table table2 = new Table(rows2);
        Table intersectedTable = table1.setOperation(table2, '∩');
        assertEquals(2, intersectedTable.getTable().size());
    }

    @Test
    void testUnion() {
        ArrayList<String> rows1 = new ArrayList<>(Arrays.asList("Name, Age", "'John', 25", "'Jane', 23"));
        ArrayList<String> rows2 = new ArrayList<>(Arrays.asList("Name, Age", "'Jack', 27"));
        Table table1 = new Table(rows1);
        Table table2 = new Table(rows2);
        Table unionTable = table1.setOperation(table2, '∪');
        assertEquals(3, unionTable.getTable().size());
    }

    @Test
    void testDifference() {
        ArrayList<String> rows1 = new ArrayList<>(Arrays.asList("Name, Age", "'John', 25", "'Jane', 23", "'Jack', 27"));
        ArrayList<String> rows2 = new ArrayList<>(Arrays.asList("Name, Age", "'John', 25", "'Jane', 23"));
        Table table1 = new Table(rows1);
        Table table2 = new Table(rows2);
        Table differenceTable = table1.setOperation(table2, '-');
        assertEquals(1, differenceTable.getTable().size());
    }

}