package edu.bu.jgram.server.assessment;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


/**
 * Tests correctness of Document
 */
public class TestDocument {

    @Test
    public void testValidDocument() {
        ClassLoader classLoader = this.getClass().getClassLoader();
        File resourceFile = new File(classLoader.getResource("sample/valid.docx").getFile());
        Document validDocument = new Document(resourceFile.getAbsolutePath(),
                1, 10, 1, 100);

        try {
            // Index contents of assignment document
            validDocument.index();

            // Search and get all checkpoints
            List<Checkpoint> checkpointList = validDocument.getCheckpoint();
            assertEquals(3, checkpointList.size());

        } catch (IOException ioe) {
            Assertions.fail("IOException thrown");
        } catch (InvalidValueException ive) {
            Assertions.fail("InvalidValueException thrown");
        } catch (InvalidGrammarException ige) {
            Assertions.fail("InvalidGrammarException thrown");
        }
    }

    @Test
    public void testMissingCheckpointGradeValueDocument() {
        ClassLoader classLoader = this.getClass().getClassLoader();
        File resourceFile = new File(classLoader.getResource("sample/missing-checkpoint-grade-value.docx").getFile());
        Document validDocument = new Document(resourceFile.getAbsolutePath(),
                1, 10, 1, 100);

        assertThrows(InvalidValueException.class, () -> {
            validDocument.index();
        });
    }

    @Test
    public void testMissingCheckpointWeightKeyDocument() {
        ClassLoader classLoader = this.getClass().getClassLoader();
        File resourceFile = new File(classLoader.getResource("sample/missing-checkpoint-weight-key.docx").getFile());
        Document validDocument = new Document(resourceFile.getAbsolutePath(),
                1, 10, 1, 100);

        assertThrows(InvalidGrammarException.class, () -> {
            validDocument.index();
        });
    }

    @Test
    public void testMissingGradeMappingDocument() {
        ClassLoader classLoader = this.getClass().getClassLoader();
        File resourceFile = new File(classLoader.getResource("sample/no-grade-mapping.docx").getFile());
        Document validDocument = new Document(resourceFile.getAbsolutePath(),
                1, 10, 1, 100);

        try {
            // Index contents of assignment document.
            // Even though no grade mapping was specified system should use default grade
            validDocument.index();

            // Search and get all checkpoints
            List<Checkpoint> checkpointList = validDocument.getCheckpoint();
            assertEquals(3, checkpointList.size());

        } catch (IOException ioe) {
            Assertions.fail("IOException thrown");
        } catch (InvalidValueException ive) {
            Assertions.fail("InvalidValueException thrown");
        } catch (InvalidGrammarException ige) {
            Assertions.fail("InvalidGrammarException thrown");
        }
    }

    @Test
    public void testInvalidCheckpointGradeValueBeyondRangeDocument() {
        ClassLoader classLoader = this.getClass().getClassLoader();
        File resourceFile = new File(classLoader.getResource("sample/invalid-checkpoint-grade-beyond-range.docx").getFile());
        Document validDocument = new Document(resourceFile.getAbsolutePath(),
                1, 10, 1, 100);

        assertThrows(InvalidValueException.class, () -> {
            validDocument.index();
        });
    }

    @Test
    public void testNonExistingDocument() {
        Document validDocument = new Document("/sample/dummy.docx",
                1, 10, 1, 100);

        assertThrows(FileNotFoundException.class, () -> {
            validDocument.index();
        });
    }

    @Test
    public void testUpdatingPreValidatedDocument() {
        ClassLoader classLoader = this.getClass().getClassLoader();
        File resourceFile = new File(classLoader.getResource("sample/pre-validated.docx").getFile());
        Document preValidatedDocument = new Document(resourceFile.getAbsolutePath(),
                1, 10, 1, 100);

        try {
            // Index contents of assignment document
            preValidatedDocument.index();

            // Search and get all checkpoints
            List<Checkpoint> checkpointList = preValidatedDocument.getCheckpoint();

            // Evaluate the final grades
            Evaluator assignmentEvaluator = new JustInTimeEvaluator(checkpointList);
            Result result = assignmentEvaluator.evaluate();
            assertEquals(93.68421173095703, result.getOverallGrade());

            assertThrows(IOException.class, () -> {
                preValidatedDocument.appendResult(result, "");
            });

        } catch (IOException ioe) {
            Assertions.fail("IOException thrown");
        } catch (InvalidValueException ive) {
            Assertions.fail("InvalidValueException thrown");
        } catch (InvalidGrammarException ige) {
            Assertions.fail("InvalidGrammarException thrown");
        }
    }

    @Test
    public void testChildCommentDocument() {
        ClassLoader classLoader = this.getClass().getClassLoader();
        File resourceFile = new File(classLoader.getResource("sample/child-comments.docx").getFile());
        Document validDocument = new Document(resourceFile.getAbsolutePath(),
                1, 10, 1, 100);

        try {
            // Index contents of assignment document
            validDocument.index();

            // Search and get all checkpoints
            List<Checkpoint> checkpointList = validDocument.getCheckpoint();
            assertEquals(3, checkpointList.size());

        } catch (IOException ioe) {
            Assertions.fail("IOException thrown");
        } catch (InvalidValueException ive) {
            Assertions.fail("InvalidValueException thrown");
        } catch (InvalidGrammarException ige) {
            Assertions.fail("InvalidGrammarException thrown");
        }
    }

    @Test
    public void testFeedbackWithCommaDocument() {
        ClassLoader classLoader = this.getClass().getClassLoader();
        File resourceFile = new File(classLoader.getResource("sample/feedback-with-comma.docx").getFile());
        Document validDocument = new Document(resourceFile.getAbsolutePath(),
                1, 10, 1, 100);

        try {
            // Index contents of assignment document
            validDocument.index();

            // Search and get all checkpoints
            List<Checkpoint> checkpointList = validDocument.getCheckpoint();
            assertEquals(3, checkpointList.size());

            assertEquals("Use generics, but overall good work" , checkpointList.get(2).getFeedback());

        } catch (IOException ioe) {
            Assertions.fail("IOException thrown");
        } catch (InvalidValueException ive) {
            Assertions.fail("InvalidValueException thrown");
        } catch (InvalidGrammarException ige) {
            Assertions.fail("InvalidGrammarException thrown");
        }
    }
}
