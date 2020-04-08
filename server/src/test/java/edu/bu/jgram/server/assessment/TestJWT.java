package edu.bu.jgram.server.assessment;

import edu.bu.jgram.server.security.JWT;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;


/**
 * Tests correctness of JWT based tampering-proof logic
 */
public class TestJWT {
    @Test
    public void testNonTamperedJWT() {
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

            // Create hash code for the result
            JWT jwt = new JWT("test");
            String hashString = jwt.create("1","1","1", result);

            // Decode Hash Code
            Result claimedResult = jwt.decodeJWT(hashString);

            // Validate decoded result with original result
            if (!result.equals(claimedResult)) {
                Assertions.fail("Actual result and hashed result must be same");
            }

        } catch (IOException ioe) {
            Assertions.fail("IOException thrown");
        } catch (InvalidValueException ive) {
            Assertions.fail("InvalidValueException thrown");
        } catch (InvalidGrammarException ige) {
            Assertions.fail("InvalidGrammarException thrown");
        }
    }

    @Test
    public void testTamperedJWT() {
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

            // Tampered Hash code
            JWT jwt = new JWT("test");
            String hashString = "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiIxIiwiaWF0IjoxNTYxMDgzNTM3LCJzdWIiOiIxIiwiaXNzIjoiMSIsIjEtR3JhZGUiOjkwLCIxLVdlaWdodCI6NywiMS1GZWVkYmFjayI6IiIsIjItR3JhZGUiOjk3LCIyLVdlaWdodCI6NSwiMi1GZWVkYmFjayI6ImdyZWF0IGNsYXJpdHkiLCIzLUdyYWRlIjo5NSwiMy1XZWlnaHQiOjcsIjMtRmVlZGJhY2siOiJVc2UgZ2VuZXJpY3MiLCJUb3RhbENoZWNrcG9pbnQiOjMsIk92ZXJhbGxHcmFkZSI6OTMuNjg0MjF9.e6eJ4OPCYL3WlvSxjyt1x9mDTHcMcdnQstp840hB2U";

            // Decode Hash Code
            assertThrows(SecurityException.class, () -> {
                jwt.decodeJWT(hashString);
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
    public void testTamperedResult() {
        ClassLoader classLoader = this.getClass().getClassLoader();
        File resourceFile = new File(classLoader.getResource("sample/feedback-with-comma.docx").getFile());
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

            // Hard coded hash representing different result (Simulating tampered result)
            JWT jwt = new JWT("test");
            String hashString = "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiIxIiwiaWF0IjoxNTYxMDgzODk2LCJzdWIiOiIxIiwiaXNzIjoiMSIsIjEtR3JhZGUiOjkwLCIxLVdlaWdodCI6NywiMS1GZWVkYmFjayI6IiIsIjItR3JhZGUiOjk3LCIyLVdlaWdodCI6NSwiMi1GZWVkYmFjayI6ImdyZWF0IGNsYXJpdHkiLCIzLUdyYWRlIjo5NSwiMy1XZWlnaHQiOjcsIjMtRmVlZGJhY2siOiJVc2UgZ2VuZXJpY3MiLCJUb3RhbENoZWNrcG9pbnQiOjMsIk92ZXJhbGxHcmFkZSI6OTMuNjg0MjF9.E6x328rW0E5gHveyUhmeCCIZnJrPaGAhowwNkfigp5Q";

            // Decode Hash Code
            Result claimedResult = jwt.decodeJWT(hashString);

            // Validate decoded result with original result
            if (result.equals(claimedResult)) {
                Assertions.fail("Actual result and hashed result must not be same");
            }

        } catch (IOException ioe) {
            Assertions.fail("IOException thrown");
        } catch (InvalidValueException ive) {
            Assertions.fail("InvalidValueException thrown");
        } catch (InvalidGrammarException ige) {
            Assertions.fail("InvalidGrammarException thrown");
        }
    }
}
