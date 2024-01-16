class QueryTest {

    @org.junit.jupiter.api.Test
    void parseQuery() {
        Query query = new Query();
        String queryStr = "table1 = {Name, Age, Height\n'John', 25, 6.0\n'Jane', 23, 5.5\n'Jack', 27, 5.9}\n table2 = {Name, Age, Height\n'John', 25, 6.0\n'Jane', 23, 5.5\n'Jak', 27, 5.9}\ntable3 = {Name, Ages, Heights\n'John', 25, 6.0\n'Jane', 23, 5.5\n'Jack', 27, 5.9}\n";
        query.parseQuery(queryStr);
        queryStr = "π Name, Age (σ Age>25 (table1 ∪ table2))";
        query.parseQuery(queryStr);
        queryStr = "π Name, Age (σ Age>25 (table1 ∩ table2))";
        query.parseQuery(queryStr);

        // Add more tables
        queryStr = """
                table4 = {Name, Age, Height
                'John', 25, 6.0
                'Jane', 23, 5.5
                'Jack', 27, 5.9}
                table5 = {Name, Ages, Heights
                'John', 25, 6.0
                'Jane', 23, 5.5
                'Jak', 27, 5.9}
                table6 = {Name, Ages, Heights
                'John', 25, 6.0
                'Jane', 23, 5.5
                'Jack', 27, 5.9}
                """;
        query.parseQuery(queryStr);
        query.parseQuery("""
                Student = {

                id, name, email

                1, 'Alex', 'alex@carleton.ca'

                2, 'John', 'john@carleton.ca'

                3, 'Mo', 'mo@carleton.ca'

                }

                Course = {

                name, hours

                'Math', 3

                'Pythics', 2

                'Network', 3

                }

                takes = {

                sid, cname

                1, 'Math'

                1, 'Pythics'

                1, 'Network'

                2, 'Network'

                3, 'Math'

                }

                (Student) ⨝ Student.id=takes.sid (takes)""");
    }
}