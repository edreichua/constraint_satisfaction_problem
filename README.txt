README for CSP

Author: Edrei Chua
Created on: 02/17/2016

*********** DIRECTORY STRUCTURE ***********

There are a few important files in this directory:

Report (directory for report)
    csp.pdf (detailed documentation of the code)
    csp.tex (tex file)
src (directory for source code)
    > CircuitBoard.java
    > ConstraintSatisfactionProblem.java
    > Queens.java
    > Sudoku.java
README.txt
sudoku_short
sudoku_test


*********** HOW TO START THE DEFAULT PROGRAM ***********

To start the program, compile all the .java files.

The default setup will implement the MRV heuristic with FC inference.

To run the default N Queen problem, run Queens.java. The default program solve for 20 Queens.

To run the default Sudoku problem, run Sudoku.java. The default program solve for the hard board.

To run the default Circuit Board problem, run CircuitBoard.java. The default program solve for the hard board.

*********** ADDITIONAL FUNCTIONALITY ***********

To change the default setup, toggle the boolean constants MRV, LCV, FC and MAC3 on line 13 - 15 of
ConstraintSatisfactionProblem.java.

To change the default program setup for N Queen, Sudoku and Circuit Board, change the lines in the
main function of the corresponding java files according to the comments.