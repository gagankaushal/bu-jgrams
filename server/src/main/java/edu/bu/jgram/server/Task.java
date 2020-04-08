package edu.bu.jgram.server;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.List;

import edu.bu.jgram.server.assessment.*;
import edu.bu.jgram.server.security.JWT;

public final class Task {

    private static final Logger LOGGER = Logger.getInstance();

    private static final String VALID_STATUS = "VALID";
    private static final String INVALID_STATUS = "IN-VALID";
    private static final String UNDERMINED_STATUS = "UNDETERMINED";
    private static final String TAMPERED_STATUS = "TAMPERED";

    public static void evaluationTask(String secret, String documentStorePath) throws IllegalArgumentException {
        //PreCondition 1: Read all the *.docx file from the provided directory.
        //PreCondition 2: Grade each document.

        File folder = new File(documentStorePath);
        if(!folder.isDirectory()) {
            throw new IllegalArgumentException(String.format("%s is not a directory. Must provide a absolute path to directory", documentStorePath));
        }

        FileFilter docxFilter = pathname -> {
            // We only want to process *.docx files, everything else can be skipped.
            if (pathname.isFile() && pathname.getName().matches(".*\\.docx")) {
                return true;
            }

            return false;
        };

        File[] documentList = folder.listFiles(docxFilter);
        if(documentList.length == 0) {
            throw new IllegalArgumentException(String.format("%s contains 0 documents to process.", documentStorePath));
        }

        for (File document : documentList)
        {
            gradeDocument(document, secret);
        }

        LOGGER.info("Document with SUCCESS status are appended with graded result ");
    }

    public static void tamperTestTask(String secret, String documentStorePath) {
        File folder = new File(documentStorePath);
        if(!folder.isDirectory()) {
            throw new IllegalArgumentException(String.format("%s is not a directory. Must provide a absolute path to directory", documentStorePath));
        }

        FileFilter docxFilter = pathname -> {
            // We only want to process *.docx files, everything else can be skipped.
            if (pathname.isFile() && pathname.getName().matches(".*\\.docx")) {
                return true;
            }

            return false;
        };

        File[] documentList = folder.listFiles(docxFilter);
        if(documentList.length == 0) {
            throw new IllegalArgumentException(String.format("%s contains 0 documents to process.", documentStorePath));
        }

        for (File document : documentList)
        {
            tamperTestDocument(document, secret);
        }

    }

    public static void newDocumentTestTask(String documentStorePath) {
        //PreCondition 1: Read all the *.docx file from the provided directory.
        //PreCondition 2: Test each document.
        File folder = new File(documentStorePath);
        if(!folder.isDirectory()) {
            throw new IllegalArgumentException(String.format("%s is not a directory. Must provide a absolute path to directory", documentStorePath));
        }

        FileFilter docxFilter = pathname -> {
            // We only want to process *.docx files, everything else can be skipped.
            if (pathname.isFile() && pathname.getName().matches(".*\\.docx")) {
                return true;
            }

            return false;
        };

        File[] documentList = folder.listFiles(docxFilter);
        if(documentList.length == 0) {
            throw new IllegalArgumentException(String.format("%s contains 0 documents to process.", documentStorePath));
        }

        for (File document : documentList)
        {
            newDocumentTest(document);
        }

    }

    /**
     * Main flow of activities on a document for grading. This typically includes
     * indexing the document, retrieving the checkpoints, performing just-in-time evaluation
     * and appending the final grade.
     *
     * @param pDocument document for grading
     */
    private static void gradeDocument(File pDocument, String secret) {
        try {
            Document assignmentDocument = new Document(pDocument.getAbsolutePath(),
                    1, 10, 1, 100);

            // Index contents of assignment document
            assignmentDocument.index();

            // Search and get all checkpoints
            List<Checkpoint> checkpointList = assignmentDocument.getCheckpoint();

            // Evaluate the final grades
            Evaluator assignmentEvaluator = new JustInTimeEvaluator(checkpointList);
            Result result = assignmentEvaluator.evaluate();
            assignmentDocument.appendResult(result, secret);
            LOGGER.info(String.format("Document [%s] : %s ", pDocument.getName(), "SUCCESS"));

        } catch (IOException ioe) {
            LOGGER.info(String.format("Document [%s] : %s ", pDocument.getName(), "FAILURE"));
            LOGGER.fatal("Exception occur", ioe);
        } catch (InvalidGrammarException ige) {
            LOGGER.info(String.format("Document [%s] : %s ", pDocument.getName(), "FAILURE"));
            LOGGER.fatal("Fix the grammar and try again.", ige);
        } catch (InvalidValueException ive) {
            LOGGER.info(String.format("Document [%s] : %s ", pDocument.getName(), "FAILURE"));
            LOGGER.fatal("Fix the value and try again.", ive);
        }
        System.out.print("\n");
    }

    /**
     * Main flow of activities on a document for tamper test.
     *
     * @param pDocument document for tamper test
     */
    private static void tamperTestDocument(File pDocument, String pSecret) {

        String checkpointsTamperedStatus = VALID_STATUS;
        String resultTableTamperedStatus = VALID_STATUS;

        Document assignmentDocument = new Document(pDocument.getAbsolutePath(),
                1, 10, 1, 100);

        Result calculatedResult = null;
        Result signedResult = null;

        try {
            // Calculate Result based on checkpoint(s)
            assignmentDocument.index();
            List<Checkpoint> checkpointList = assignmentDocument.getCheckpoint();
            Evaluator assignmentEvaluator = new JustInTimeEvaluator(checkpointList);
            calculatedResult = assignmentEvaluator.evaluate();

        } catch (IOException ioe) {
            checkpointsTamperedStatus = UNDERMINED_STATUS;
            //LOGGER.fatal("Exception occur", ioe);
        } catch (InvalidGrammarException ige) {
            checkpointsTamperedStatus = TAMPERED_STATUS;
            //LOGGER.fatal("Tampered the checkpoint.", ige);
        } catch (InvalidValueException ive) {
            checkpointsTamperedStatus = TAMPERED_STATUS;
            //LOGGER.fatal("Tampered the checkpoint.", ive);
        }


        try {
            // TODO: Retrieve previously Printed Result

            // Retrieve Hashed Result
            String hashedToken = assignmentDocument.getHashString();
            JWT jwt = new JWT(pSecret);
            signedResult = jwt.decodeJWT(hashedToken);

            // Verify if the checkpoint(s) are Tampered
            if (!signedResult.equals(calculatedResult)) {
                checkpointsTamperedStatus = TAMPERED_STATUS;
            }
            LOGGER.info(String.format("Document [%s] : Checkpoint(s) : %s  |  Result Table : %s  |  Hashed Token : %s",
                    pDocument.getName(), checkpointsTamperedStatus, resultTableTamperedStatus, VALID_STATUS));
        } catch (SecurityException se) {
            LOGGER.info(String.format("Document [%s] : Checkpoint(s) : %s  |  Result Table : %s  |  Hashed Token : %s",
                    pDocument.getName(), UNDERMINED_STATUS, UNDERMINED_STATUS, UNDERMINED_STATUS));
            LOGGER.error("Hash Token was tampered or you have input incorrect secret", se);
        }

        if(signedResult != null) {
            if(!checkpointsTamperedStatus.equals(VALID_STATUS) || !resultTableTamperedStatus.equals(VALID_STATUS)) {
                printResult(signedResult);
            }
        }

        System.out.print("\n");
    }

    private static void newDocumentTest(File pDocument) {
        try {
            Document assignmentDocument = new Document(pDocument.getAbsolutePath(),
                    1, 10, 1, 100);

            // Index contents of assignment document
            assignmentDocument.index();

            // Search and get all checkpoints
            List<Checkpoint> checkpointList = assignmentDocument.getCheckpoint();

            if (!checkpointList.isEmpty()) {
                LOGGER.warn(String.format("Document [%s] : %s ", pDocument.getName(), INVALID_STATUS));
                LOGGER.warn(String.format("Document contains %d checkpoint(s). make sure they are NOT graded", checkpointList.size()));
            } else {
                LOGGER.info(String.format("Document [%s] : %s ", pDocument.getName(), VALID_STATUS));
            }

        } catch (Exception ioe) {
            LOGGER.warn(String.format("Document [%s] : %s ", pDocument.getName(), UNDERMINED_STATUS));
            LOGGER.fatal("Exception occur.", ioe);
        }
        System.out.print("\n");
    }

    private static void printResult(Result pResult) {
        System.out.println("Signed Result For Cross reference");
        System.out.println("--------------------------------------------------------------");
        System.out.format("|%3s|%7s|%7s|%40s|\n", "C#", "Weight", "grade", "Feedback");
        System.out.println("--------------------------------------------------------------");
        for (int checkpointID : pResult.getCheckpointMap().keySet()) {
            Checkpoint checkpoint = pResult.getCheckpointMap().get(checkpointID);
            System.out.format("|%3d|%7d|%7d|%40s|\n", checkpointID, checkpoint.getWeight(), checkpoint.getGrade(), checkpoint.getFeedback());
        }
        System.out.println("--------------------------------------------------------------");
        System.out.format("|%3s|%7s|%7s|%40s|\n", "", "Σ", String.format("%.2f",pResult.getOverallGrade()), "");
        System.out.println("--------------------------------------------------------------");
    }

}


