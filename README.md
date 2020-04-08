# JGRAM

Just in Time Grading Messages

## Introduction

JGRAM is a just in time grading proposed by Dr. Prof. Eric J Braude of the Department of Computer Science, Boston University Metropolitan College. The idea is to use technology-assisted teaching.

Metropolitan College’s advantage over MOOC-based MS degree competitors is the significant human feedback we provide to our students. For example, in Prof. Braude’s Machine Learning Class, students have to submit their project submissions using a template. This allows the graders to gradeMapping as many as 37 different projects (this semester), each in three phases, with them commenting on everyone. The Word format allows us to navigate submissions, insert comments, and compute grades using an embedded spreadsheet. This is being adopted by some of the faculty at MET college. 

## Concept 

An area where this can be improved is to better connect the comments/feedback to the grades: for example, to give a gradeMapping specifically for the degree to which a neural net architecture is well-annotated. To this end, we are working on a grading system and calling it JGRAMs (Just-in-time Grading Messages). 

The instructor is able to easily insert a criterion and weight for a gradeMapping at any location in the template. Weights are locally scaled to 10 as a reference. This has the dual purpose of evaluating and educating students about what constitutes good work. The grades themselves are inserted at relevant locations in the template and are close to the student's work on this. They are automatically weighted and summed. There is more on this below and an attachment that illustrates it.

In other words, the instructor drops these "just in time" gradeMapping checkpoints at will. The student knows pretty precisely what they are responsible for, and we should get improved outcomes. Instructors can add, remove, and edit JGRAMs at will. When we see a common problem in submissions, we'd probably insert a JGRAM to try to forestall it in future.

## Personas
- Designer
- Student
- Grader

## Assignment workflow
* Step 1: Designer creates the assignment document. **Document State = New-Assignment**
* Step 2: Uploads the assignment document to blackboard.
* Step 3: Students downloads the **New-Assignment**.
* Step 4: Student uploads the completed assignment document to Blackboard. **Document State = Completed-Assignment**
* Step 5: Grader downloads all the **Completed-Assignment**
* Step 6: Grader adds comment (with grade details) at various location in **Completed-Assignment**. **Document State = Graded-Assignment**
* Step 7: Grader generate the overall grades. 
* Step 8: Grader uploads the **Graded-Assignment** to Blackboard.
* Step 9: Students can download and view the **Graded-Assignment** from Blackboard.
* Step 10: Grader can download the **Graded-Assignment** to Blackboard for modification or cross verification.


## Requirements (Milestone and completion)

| Milestone |    Status   |
|:---------:|:-----------:|
|    M-1    |   Complete  |
|    M-2    |   Complete  |
|    M-3    |   Complete  |
|    M-4    |   Complete  |
|    M-5    |   Complete  |
|    M-6    | In-Progress |

* [x] ( **M-1** ) JGRAM app shall allow professor(s) and facilitator(s) to easily upload a assignment documents and calculate overall grade based on just-in-time weighted grades.
* [x] ( **M-1** ) JGRAM app shall alert professor(s) and facilitator(s) for invalid JGRAM(s).
* [x] ( **M-2** ) JGRAM app shall display descriptive overall grade. 
* [x] ( **M-2** ) JGRAM app shall accept JGRAM(s) grades in numeric or alphabetic form i.e. 97 or A+
* [x] ( **M-3** ) JGRAM app shall append descriptive overall grade at the end of the assigment document.
* [x] ( **M-4** ) JGRAM app shall prompt professor(s) and facilitator(s) to input directory path which contains 1 or more assignment documents.
* [x] ( **M-4** ) JGRAM app shall evaluate child comments as well i.e. reply to a comment which could contain grades.
* [x] ( **M-5** ) JGRAM app shall provide a a way for professor(s) and facilitator(s) to identify any
                  tampering with JGrams..

## Development TODOs
* [x] ( **M-1** ) Intuitive build process (gradle wrapper)
* [x] ( **M-1** ) Fetch hardcoded gradeMapping mapping, until we integrate to retrieve it from document
* [x] ( **M-2** ) Must perform following validation 
    * Each checkpoint, must not contain empty gradeMapping
    * Each checkpoint, must have valid gradeMapping i.e. must be a value from the gradeMapping letter list (A, B, C, D etc).
    * Each checkpoint, must not contain empty weight
    * Each checkpoint, must have valid weight i.e. >=1 and <=10
* [x] ( **M-2** ) Spaces must be trimmed from the fetched checkpoint details. 
* [x] ( **M-1** ) Calculate the final grading using 
* [x] ( **M-2** ) Initialize the JGRAMs gradeMapping to 0. Also grades can be letter or value ranging from 0-100

    $`Grades = [A, B, A, A, A, A, A, A, A, B]`$ in 10 JGRAMs (taking A=95, B=85) and the weights are, respectively, 
    
    $`Weights = [4, 5, 6, 7, 8, 9, 10, 1, 1, 1]`$ then the student's gradeMapping would be 
    
    $`=\frac{[95*4 + 85*5 + 95*67 + 95*8 + 95*9,  + 95*10 + 95*1 + 95*1 + 85*1]}{ [4+5+6+7+8+9+10+1+1+1]}`$
* [x] ( **M-2** ) Add basic logging
* [x] Adding documentation for non-trivial methods
* [x] Use cases

## FAQ 
###_What is Checkpoint?_ 

It is Just-in-time grade. Grader can add new checkpoint by simply inserting a new comment anywhere in the **Completed-Assignment**.

###_How to specify checkpoint?_

Insert a comment with below grammar.

```text
CHECKPOINT( WEIGHT=7, GRADE=97, FEEDBACK=Excellent technique and good clarity)
```

###_What is Grade Mapping?_

It is a mapping between grade letter and numeric score. 

###_How to specify Grade Mapping grammar?_

Insert a comment with below grammar.

```text
GRADEMAPPING( A+=97, A=95, A-=93, B+=87, B=85, B-=83, C=77, F=67)
```

## Designer JGRAM workflow

* Step 1: Designer executed the JGRAM app with option 1 (New Document Test). This will test the newly created document for any comments with already graded checkpoints, and alert the designer to clear them out before manually submitting it to Blackboard.

**Example**: Demonstrates newly designed document with comments containing graded JGram (Not clean)

```text 
2019/06/22 21:51:51 : INFO : Welcome to Application JGRAM
 
---------------------------------[ INPUT ]-------------------------------------
 
Select Task :
​ 1 : New Document Test
​ 2 : Evaluate Grade
​ 3 : Tamper Test
​​ (Example 1): 1
Enter secret (Example mysecret): secret
2019/06/22 21:52:00 : INFO : Save this secret somewhere safe, you will require it during tamper test
Enter absolute path to directory containing assignment document(s) (Example /sample/assignments): /Users/edu/sandbox/sample
 
-------------------------------------------------------------------------------
 
2019/06/22 21:52:11 : WARN : Document [assignment.docx] : IN-VALID
2019/06/22 21:52:11 : WARN : Document contains 3 checkpoint(s). make sure they are NOT graded
 
2019/06/22 21:52:11 : INFO : Goodbye...
```

## Grader JGRAM workflow 1
* Step 1: Grader manually download all the  **Completed-Assignment** to a directory.
* Step 2: Grader executes the JGRAM app with option 1 (New Document Test). This will test the **Completed-Assignment** document for any comments with already graded checkpoints (possibly tampered by student), and alert the grader to clear them out before manually grading it.
* Step 3: Grader manually adds comments with JGrams, and save the document(s).
* Step 4: Grader executes JGRAM app with Option 2 (Generate Grade). This append the document with the overall grade and signed hash (for tamper-proof).
* Step 5: Grader manually submits the **Graded-Assignment** to Blackboard. 

**Example** Demonstrate grade generation for **Graded-Assignment** 

```text
2019/06/26 22:37:59 : INFO : Welcome to Application JGRAM

---------------------------------[ INPUT ]-------------------------------------

Select Task : 
	 1 : New Document Test 
	 2 : Generate Grade
	 3 : Tamper Test
		 (Example 1): 2
Enter secret (Example mysecret): secret
2019/06/26 22:38:09 : INFO : Save this secret somewhere safe, you will require it during tamper test
Enter absolute path to directory containing assignment document(s) (Example /sample/assignments): /Users/edu/sandbox/sample

-------------------------------------------------------------------------------

2019/06/26 22:38:15 : INFO : Document [Student1-valid.docx] : SUCCESS 

2019/06/26 22:38:15 : INFO : Document [Student2-pre-validated.docx] : SUCCESS 

2019/06/26 22:38:15 : INFO : Document [Student3-no-grade-mapping.docx] : SUCCESS 

2019/06/26 22:38:15 : INFO : Document [Student4-missing-checkpoint-weight-key.docx] : FAILURE 
2019/06/26 22:38:15 : FATAL : Fix the grammar and try again.

edu.bu.jgram.InvalidGrammarException: Checkpoint 2 - Invalid checkpoint grammar. Missing weight
2019/06/26 22:38:15 : INFO : Document [Student5-missing-checkpoint-grade-value.docx] : FAILURE 
2019/06/26 22:38:15 : FATAL : Fix the value and try again.
edu.bu.jgram.InvalidValueException: Checkpoint 2 - has invalid grade. GradeMapping must be between 1-100

2019/06/26 22:38:15 : INFO : Document [Student6-invalid-checkpoint-grade-beyond-range.docx] : FAILURE 
2019/06/26 22:38:15 : FATAL : Fix the value and try again.

edu.bu.jgram.InvalidValueException: Checkpoint 1 - has invalid grade. GradeMapping must be between 1-100
2019/06/26 22:38:15 : INFO : Document with SUCCESS status are appended with graded result 
2019/06/26 22:38:15 : INFO : Goodbye...
```


## Grader JGRAM workflow 2
* Step 1: Grader manually downloads already **Graded-Assignment** from blackboard.
* Step 2: Grader executes the JGRAM app with option 3 (Tamper Test). This will test the **Graded-Assignment** and make sure the comments with JGrams are not tampered. Also, provides a full Tamper report for grader to manually fix.

**Example**: Demonstrates tamper test for (1) non-tampered document and (2) tampered document.

```text
2019/06/22 21:38:30 : INFO : Welcome to Application JGRAM
 
---------------------------------[ INPUT ]-------------------------------------
 
Select Task :
​ 1 : New Document Test
​ 2 : Evaluate Grade
​ 3 : Tamper Test
​​ (Example 1): 3
Enter secret (Example mysecret): secret
2019/06/22 21:40:03 : INFO : Save this secret somewhere safe, you will require it during tamper test
Enter absolute path to directory containing assignment document(s) (Example /sample/assignments): /Users/edu/sandbox/sample
 
-------------------------------------------------------------------------------
 
2019/06/22 21:40:14 : INFO : Document [Student1.docx] : Checkpoint(s) : VALID  |  Result Table : VALID  |  Hashed Token : VALID
 
2019/06/22 21:40:15 : INFO : Document [Student2-tampered-comment.docx] : Checkpoint(s) : TAMPERED  |  Result Table : VALID  |  Hashed Token : VALID
Signed Result For Cross reference
--------------------------------------------------------------
| C#| Weight|  grade|                                Feedback|
--------------------------------------------------------------
|  1|      7|     90|                                        |
|  2|      5|     97|                           great clarity|
|  3|      7|     95|     Use generics, but overall good work|
--------------------------------------------------------------
|   |      Σ|  93.68|                                        |
--------------------------------------------------------------
 
2019/06/22 21:40:15 : INFO : Document [Student3.docx] : Checkpoint(s) : VALID  |  Result Table : VALID  |  Hashed Token : VALID
 
2019/06/22 21:40:15 : INFO : Document [Student4-tampered-result.docx] : Checkpoint(s) : VALID  |  Result Table : VALID  |  Hashed Token : VALID
 
2019/06/22 21:40:15 : INFO : Goodbye...
```

## Refer README-Development for developer notes.
