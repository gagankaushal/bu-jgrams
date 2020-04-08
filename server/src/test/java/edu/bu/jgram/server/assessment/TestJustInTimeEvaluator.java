package edu.bu.jgram.server.assessment;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


/**
 * Tests correctness of JustInTimeEvaluator
 */
public class TestJustInTimeEvaluator {
    @Test
    public void testEvaluation() {
        ClassLoader classLoader = this.getClass().getClassLoader();
        File resourceFile = new File(classLoader.getResource("sample/valid.docx").getFile());
        Document validDocument = new Document(resourceFile.getAbsolutePath(),
                1, 10, 1, 100);

        try {
            // Index contents of assignment document
            validDocument.index();

            // Search and get all checkpoints
            List<Checkpoint> checkpointList = validDocument.getCheckpoint();

            // Evaluate the final grades
            Evaluator assignmentEvaluator = new JustInTimeEvaluator(checkpointList);
            Result result = assignmentEvaluator.evaluate();

            assertEquals(93.68421173095703, result.getOverallGrade());

        } catch (IOException ioe) {
            Assertions.fail("IOException thrown");
        } catch (InvalidValueException ive) {
            Assertions.fail("InvalidValueException thrown");
        } catch (InvalidGrammarException ige) {
            Assertions.fail("InvalidGrammarException thrown");
        }
    }

}
