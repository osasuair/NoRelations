# NoRelations - Relational Algebra Compiler

This project is a Relational Algebra Compiler written in Java. It is designed to parse and execute queries based on the principles of relational algebra, a fundamental aspect of database systems.

## Functionality

The compiler supports a variety of operations, including:

- Projection (`π`)
- Selection (`σ`)
- Union (`∪`)
- Intersection (`∩`)
- Difference (`-`)
- Join (`⨝`)

These operations can be applied to tables, which are stored for easy retrieval. The compiler also supports the creation of new tables from existing ones and the saving of tables for later use.

## How to Run

First, Download the project as a zip and unzip the file or clone the repository

To run the program, you will need to use the Java Runtime Environment (JRE). If you do not have it installed, you can download it from the [official Oracle website](https://www.oracle.com/java/technologies/javase-jre8-downloads.html).

Now that Java is installed correctly, head over to the folder containing the project,
you can run the program using the following command in your terminal in the main folder:

```bash
java -jar .\NoRelations.jar
```

Please note that the program uses a command-line interface. After starting the program, you will be prompted to enter your queries. The program will then parse and execute these queries, printing the results to the console.

## Command Line Interface

The program uses a command-line interface for interaction. When you run the program, you will be greeted with a prompt where you can enter your queries. Here's an example of what it might look like:

```bash
Enter a query and type 'finish.' to send a query, 'help.' for help or commands, or 'exit.' to quit.
>>>
```
You can then enter your query. For example, if you want to perform a union operation between two tables table1 and table2, you would enter:
```bash
>>> table1={col1, col2
1, 2
3, 4
}
table2={col1, col2
2, 3
4, 5
}
table1 ∪ table2 finish.
```
And the program would return:
```bash
+------+------+
| col1 | col2 |
+------+------+
| 1    | 2    |
| 2    | 3    |
| 3    | 4    |
| 4    | 5    |
+------+------+
```
For more detailed information on how to use the program and its various features, please refer to the help in the command line, source code and comments therein.
