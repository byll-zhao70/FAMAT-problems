# FAMAT-problems
Repository of FAMAT problem screenshots obtained through Apache PDFBox

## Source Code
The /src folder has two files: PDFLocationList.java, and BetterTextPosition.java. PDFLocationList.java is what scrapes the test pdf files for questions, done so by detecting a new question (sees if there is a question number on the left side of the page, or on the middle of the page for a two-column test). BetterTextPosition.java is an improvement of the TextPosition class in the Apache PDFBox library, where BetterTextPosition is able to store information of words in a PDF, whereas TextPosition stores information of only singular characters. Since the FAMAT tests are formatted in numerous ways, the question detection system isn't 100% successful, and some tests in this repository have incorrect screenshots. If you've found an error, please fill out this Google form to report it: https://forms.gle/VeMLkvnpvDCMzP1ZA.

## Tests
The tests are organized in folders corresponding to their division (Algebra II, Alpha, etc.). Within these folders, the tests are numbered according to the test list index found in the FAMAT_Test_List.csv file. Each test folder contains png screenshots of the 30 questions in the individual tests. 

Each question can be accessed at `https://raw.githubusercontent.com/byll-zhao70/FAMAT-problems/main/tests/[division]/[topic]/[TestID]/question_[number].png`. Divisions are `algebra1`, `algebra2`, `geometry`, `theta`, `alpha`, `mu`, and `statistics`. Individual tests have `individual` as their topic. 
