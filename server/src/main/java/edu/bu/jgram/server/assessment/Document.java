package edu.bu.jgram.server.assessment;

import java.io.*;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

import edu.bu.jgram.server.security.JWT;
import org.apache.poi.xwpf.usermodel.*;


/**
 * Represents a in-memory document, to provide convenient search and save features
 */
public class Document {

    private static final String CHECKPOINT_GRAMMAR_START = "CHECKPOINT(";
    private static final String WEIGHT_GRAMMAR_KEY = "weight";
    private static final String GRADE_GRAMMAR_KEY = "grade";
    private static final String FEEDBACK_GRAMMAR_KEY = "feedback";
    private static final String GRADE_MAPPING_GRAMMAR_START = "GRADEMAPPING(";
    private static final String OVERALL_GRADE_TITLE = "JGRAM Overall Grade";

    private final String mDocumentPath;
    private final int mMinWeight;
    private final int mMaxWeight;
    private final int mMinGrade;
    private final int mMaxGrade;

    private List<Checkpoint> mCheckpointList;
    private GradeMapping mGradeMapping;
    private XWPFTable mOverallGradeTable;

    public Document(String pDocumentPath, int pMinWeight, int pMaxWeight, int pMinGrade, int pMaxGrade) {
        mDocumentPath = pDocumentPath;
        mMinWeight = pMinWeight;
        mMaxWeight = pMaxWeight;
        mMinGrade = pMinGrade;
        mMaxGrade = pMaxGrade;
    }

    /**
     * Retrieve all available checkpoint(s).
     *
     * <p>Note: If the are no checkpoint in the document, it will return an empty list;
     * however null means document hasn't been indexed.</p>
     *
     * @return {@link List}
     */
    public List<Checkpoint> getCheckpoint() throws IllegalStateException {
        return mCheckpointList;
    }

    /**
     * Read the content of the word document,and stores the entire content in-memory.
     *
     * <p>TODO: In future we can index just the required content from the document</p>
     *
     * @throws IOException Throws if file not found or issue reading document
     * @throws InvalidGrammarException Throws if JGRAM (aka Checkpoint) or grade mapping grammar is invalid. eg CHECKPOINT( GRADE=95, FEEDBACK=[]) is missing WEIGHT
     * @throws InvalidValueException Throws if JGRAM (aka Checkpoint) attribute value is invalid. eg CHECKPOINT( WEIGHT=A, GRADE=95, FEEDBACK=[]) has invalid WEIGHT value
     */
    public void index() throws IOException, InvalidGrammarException, InvalidValueException {

        //GOAL1: Read a MS word document.
        //GOAL2: Extract all the comments.
        //GOAL3: Filter out the comments not containing checkpoint or grade mapping
        //GOAL4: Notify user with details of non-compliant checkpoints
        //GOAL4: Persist the (1) extracted checkpoint and (2) grade mapping for easy access

        // Read the word document
        File file = new File(mDocumentPath);
        if (!file.exists()) {
            throw new FileNotFoundException(String.format("File %s not found", mDocumentPath));
        }

        FileInputStream fileInputStream = new FileInputStream(file.getAbsolutePath());
        XWPFDocument documentContent = null;

        try {
            //How1 : Read the MS Word document using Apache POI library
            documentContent = new XWPFDocument(fileInputStream);

            mOverallGradeTable = getOverallResultTable(documentContent);

            //How2 : Extract all the comments from already read document content
            List<XWPFComment> commentList = Arrays.asList(documentContent.getComments());

            // Set the default grade mapping, this will be overridden if the document contains custom grade mapping.
            mGradeMapping = defaultGradeMapping();

            mCheckpointList = new ArrayList<>();

            // GOAL: What will happen if a checkpoint had bad grammar? Is there a way to convey user which checkpoint had issues?
            // How: Keep a counter of checkpoints (since we always traverse the document from top to bottom), we can get the exact
            //      checkpoint order number, which can be tagged logged with error message.
            int commentCounter = 0;

            //How3 : Iterate through all the comments and filter out NOT (checkpoint or grade mapping).
            //       We define non checkpoint, based on grammar i.e. any comment not starting with CHECKPOINT( will be ignored
            //       We define non grade mapping based on grammar i.e. any comment not starting with GRADEMAPPING( will be ignored

            for (final XWPFComment comment : commentList) {
                String commentText = comment.getText();

                if (isCheckpoint(commentText)) {
                    // If the comment is of type checkpoint, go on with further checkpoint scrutiny.

                    commentCounter++;

                    //How3 : Comment considered for checkpoint will go through further validation to comply
                    //       with checkpoint grammar. Not compliant checkpoint will throw exceptions.
                    Checkpoint checkpoint = extractCheckpoint(commentText, commentCounter);

                    //How4 : Non-compliant checkpoint values wil throw exception. Non-compliant checkpoint values are
                    //       (1) grade < minGrade or grade > maxGrade eg. 0-100
                    //       (2) weight < minWeight or weight > maxWeight eg. 1-10
                    validateCheckpoint(checkpoint, commentCounter);

                    //how4: At this point, it has gone through all the scrutiny and passed with flying colors.
                    //      It's safe to persist the checkpoint for further evaluation.
                    mCheckpointList.add(checkpoint);


                } else if (isGradeMapping(commentText)) {
                    // If the comment is of type grade mapping, go on with further grade mapping scrutiny.

                    commentCounter++;

                    //How3 : Comment considered for grade mapping will go through further validation to comply
                    //       with grade mapping grammar. Not compliant checkpoint will throw exceptions.
                    mGradeMapping = extractGradeMapping(commentText);

                }
            }
        } finally {
            // Defer section, for clean exit
            if (documentContent != null)
                documentContent.close();

            fileInputStream.close();
        }
    }

    /**
     * Appends final evaluated result at the end of the document.
     *
     * <p>Note: Grammar for the result in the document, is yet to be determined</p>
     *
     * @param pResult final evaluated result
     */
    public void appendResult(Result pResult, String secret) throws IOException {

        // Read the word document
        File file = new File(mDocumentPath);
        if (!file.exists()) {
            throw new FileNotFoundException(String.format("File %s not found", mDocumentPath));
        }

        FileInputStream fileInputStream = new FileInputStream(file.getAbsolutePath());
        XWPFDocument documentContent = null;

        try {
            documentContent = new XWPFDocument(fileInputStream);

            // Create JGRAM overall grade paragraph
            if (!isParagraphExists(documentContent, OVERALL_GRADE_TITLE)) {
                // create paragraph
                createParagraph(documentContent, OVERALL_GRADE_TITLE);
            }

            // if overall grade table already exists, then delete table.
            if (mOverallGradeTable == null) {
                //create table
                createOverallResultTable(documentContent, pResult, secret);
            } else {
                //TODO: There is a bug in POI library, restricting us from automatically deleting or updating the existing table.
                // For now we will let user know to manually cleanup
                throw new IOException("Must not contain old results. Please manually delete JGRAM Overall Grade section first(including table)");
            }

            FileOutputStream fileOutputStream = new FileOutputStream(mDocumentPath);
            documentContent.write(fileOutputStream);

        } finally {
            // Defer section
            if (documentContent != null)
                documentContent.close();

            fileInputStream.close();
        }
    }

    public String getHashString() {
        int lastRowIndex = mOverallGradeTable.getRows().size() -1;
        String hashString = mOverallGradeTable.getRow(lastRowIndex).getCell(3).getText();

        return hashString;
    }

    /**
     * Retrieve grade mapping.
     *
     * <p>Note: If the are no grade mapping in the document, it will return an empty list;
     * however null means document hasn't been indexed.</p>
     *
     * @return {@link GradeMapping}
     */
    private GradeMapping getGradeMapping() {
        return mGradeMapping;
    }

    /**
     * Validate whether a given string is numeric or not.
     *
     * @param strNum String to be validated
     * @return {@link Boolean}
     */
    private static boolean isNumeric(String strNum) {
        try {
            int d = Integer.parseInt(strNum);
        } catch (NumberFormatException | NullPointerException nfe) {
            return false;
        }
        return true;
    }

    /**
     * Extract checkpoint meta data from a given string. Also, validate the grammar, and value
     *
     * @param pComment String with potential checkpoint meta data
     * @param pCommentOrderNumber value denotes checkpoint order in the document.
     * @return {@link Boolean}
     *
     */
    private Checkpoint extractCheckpoint(String pComment, int pCommentOrderNumber) throws InvalidGrammarException, InvalidValueException {
        //GOAL1: Extract meta data i.e. weight, grade, feedback from given string.
        //GOAL2: Notify user with details of non-compliant checkpoints grammar or value
        //GOAL3: Construct a Checkpoint object with extracted meta data.

        //how1 : String manipulations to extract checkpoint metadata from expected grammar i.e. CHECKPOINT( WEIGHT=7, GRADE=97, FEEDBACK=[foo bar])
        int startIndex = pComment.indexOf("(") + 1; //Start index is inclusive
        int endIndex = pComment.indexOf(")"); // End index is exclusive

        //how1 : Below substring will provide value like -  WEIGHT=7, GRADE=97, FEEDBACK=[foo bar]
        String checkpointValue = pComment.substring(startIndex, endIndex);
        checkpointValue = checkpointValue.trim();

        //how2 : Empty string means, incorrect grammar specified for this checkpoint. Something like CHECKPOINT()
        if (checkpointValue.length() == 0) {
            throw new InvalidGrammarException(String.format("Checkpoint %d - Invalid checkpoint grammar", pCommentOrderNumber));
        }

        //Extract weight and grade
        boolean weightSpecified = false;
        boolean gradeSpecified = false;
        boolean feedbackSpecified = false;

        int weight = 0;
        int grade = 0;
        String feedback = "";

        //how1 : Handle the case where feedback value contains "," eg. CHECKPOINT( WEIGHT=7, GRADE=97, FEEDBACK=[foo,bar]).
        //       Extract the feedback first and then process other meta data
        int feedbackStartIndex = checkpointValue.indexOf("[") + 1; //Start index is inclusive of [, so +1 to exclude [
        int feedbackEndIndex = checkpointValue.indexOf("]"); // End index is exclusive
        feedback = checkpointValue.substring(feedbackStartIndex, feedbackEndIndex);
        feedback = feedback.trim();

        //how1: We need the feedback string excluded from remaining checkpoint string, since it's already processed.
        checkpointValue = checkpointValue.substring(0, feedbackStartIndex - 1 );

        List<String> checkpointProperties = Arrays.asList(checkpointValue.split(","));
        if (checkpointProperties.isEmpty()) {
            throw new InvalidGrammarException(String.format("Checkpoint %d - Invalid checkpoint grammar", pCommentOrderNumber));
        }


        for (final String property : checkpointProperties) {
            if (!property.contains("=")) {
                throw new InvalidGrammarException(String.format("Checkpoint %d - Invalid checkpoint grammar", pCommentOrderNumber));
            }

            List<String> propertyParts = Arrays.asList(property.split("="));
            String key = propertyParts.get(0).trim().toLowerCase();
            //Default to already extracted feedback string, but overwrite with actual value if key is not FEEDBACK
            //This is to avoid if/else, and keeping the logic readable.
            String value = feedback;

            if(!key.equals(FEEDBACK_GRAMMAR_KEY)) {
                value = propertyParts.get(1).trim();
            }

            switch (key) {
                case WEIGHT_GRAMMAR_KEY:
                    if (value.length() > 0 && isNumeric(value)) {
                        weight = Integer.parseInt(value);
                    }
                    weightSpecified = true;
                    break;
                case GRADE_GRAMMAR_KEY:
                    if (value.length() > 0) {
                        // Professor can specify grade letter or grade value. This will handle both scenario
                        grade = isNumeric(value) ? Integer.parseInt(value) : mGradeMapping.getGrade(value.toUpperCase());
                    }
                    gradeSpecified = true;
                    break;
                case FEEDBACK_GRAMMAR_KEY:
                    //no-op, we have already extracted feedback at the very beginning.
                    feedbackSpecified = true;
                    break;
                default:
                    break;
            }
        }

        if (!weightSpecified) {
            throw new InvalidGrammarException(String.format("Checkpoint %d - Invalid checkpoint grammar. Missing weight", pCommentOrderNumber));
        }
        if (!gradeSpecified) {
            throw new InvalidGrammarException(String.format("Checkpoint %d - Invalid checkpoint grammar. Missing grade", pCommentOrderNumber));
        }
        if (!feedbackSpecified) {
            throw new InvalidGrammarException(String.format("Checkpoint %d - Invalid checkpoint grammar. Missing feedback", pCommentOrderNumber));
        }

        //how4: assemble the processed value to construct checkpoint object.
        Checkpoint checkpoint = new Checkpoint(weight, grade, feedback);
        return checkpoint;
    }

    /**
     * Validate whether a given checkpoint meets our value criteria.
     *
     * @param checkpoint checkpoint under validation
     * @param commentOrderNumber value denotes checkpoint order in the document.
     * @return {@link Boolean}
     */
    private void validateCheckpoint(Checkpoint checkpoint, int commentOrderNumber) throws InvalidValueException {
        if (checkpoint.getWeight() > mMaxWeight || checkpoint.getWeight() < mMinWeight) {
            String err = String.format("Checkpoint %d - has invalid weight. Weight must be between %d-%d", commentOrderNumber, mMinWeight, mMaxWeight);
            throw new InvalidValueException(err);
        }

        if (checkpoint.getGrade() > mMaxGrade || checkpoint.getGrade() < mMinGrade) {
            String err = String.format("Checkpoint %d - has invalid grade. GradeMapping must be between %d-%d", commentOrderNumber, mMinGrade, mMaxGrade);
            throw new InvalidValueException(err);
        }
    }

    /**
     * Constructs a default hardcoded grade mapping.
     *
     * @return {@link GradeMapping}
     */
    private GradeMapping defaultGradeMapping() throws InvalidValueException {
        //GOAL1: What will happen if there was no grade mapping specified in the document. It might be better to have
        //       a default mapping.
        GradeMapping gradeMapping = new GradeMapping();
        gradeMapping.setGrade("A+", 97);
        gradeMapping.setGrade("A", 95);
        gradeMapping.setGrade("A-", 93);
        gradeMapping.setGrade("B+", 87);
        gradeMapping.setGrade("B", 85);
        gradeMapping.setGrade("B-", 83);
        gradeMapping.setGrade("C+", 77);
        gradeMapping.setGrade("C", 75);
        gradeMapping.setGrade("C-", 73);
        gradeMapping.setGrade("F", 67);

        return gradeMapping;
    }

    /**
     * Validate whether a given string meets the checkpoint grammar.
     *
     * @param pComment String to be validated
     * @return {@link Boolean}
     */
    private boolean isCheckpoint(String pComment) {
        if (pComment.startsWith(CHECKPOINT_GRAMMAR_START)) {
            return true;
        }

        return false;
    }

    /**
     * Validate whether a given string meets the grade mapping grammar.
     *
     * @param pComment String to be validated
     * @return {@link Boolean}
     */
    private boolean isGradeMapping(String pComment) {
        if (pComment.startsWith(GRADE_MAPPING_GRAMMAR_START)) {
            return true;
        }

        return false;
    }

    /**
     * Extract grade mapping meta data from a given string. Also, validate the grammar, and value
     *
     * @param pComment String with potential grade mapping meta data
     * @return {@link Boolean}
     *
     */
    private GradeMapping extractGradeMapping(String pComment) throws InvalidGrammarException, InvalidValueException {
        GradeMapping gradeMapping = new GradeMapping();

        //GradeMapping Mapping grammar is - GRADEMAPPING( A+=97, A=95, A-=93, B+=87, B=85, B-=83, C=77, F=67)
        int startIndex = pComment.indexOf("(") + 1; //Start index is inclusive
        int endIndex = pComment.indexOf(")"); // End index is exclusive

        // Value will look like  A+=97, A=95, A-=93, B+=87, B=85, B-=83, C=77, F=67
        String gradeMappingValue = pComment.substring(startIndex, endIndex);
        gradeMappingValue = gradeMappingValue.trim();

        // Incorrect grammar specified for this gradeMapping mapping
        if (gradeMappingValue.length() == 0) {
            throw new InvalidGrammarException("Invalid gradeMapping mapping grammar");
        }

        List<String> gradeMappingProperties = Arrays.asList(gradeMappingValue.split(","));
        if (gradeMappingProperties.isEmpty()) {
            throw new InvalidGrammarException("Invalid gradeMapping mapping grammar");
        }

        for (final String property : gradeMappingProperties) {
            if (!property.contains("=")) {
                throw new InvalidGrammarException("Invalid gradeMapping mapping grammar");
            }

            List<String> propertyParts = Arrays.asList(property.split("="));
            String key = propertyParts.get(0).trim().toUpperCase();
            String value = propertyParts.get(1).trim();

            if (key.length() > 0 && value.length() > 0 && isNumeric(value)) {
                int gradeNumber = Integer.parseInt(value);
                gradeMapping.setGrade(key, gradeNumber);
            }
        }

        return gradeMapping;
    }

    /**
     * Append paragraph to MS Word document.
     *
     * @param pDocumentContent Document where the content will be appended
     * @param pText text to be appended.
     * @return {@link Boolean}
     *
     */
    private void createParagraph(XWPFDocument pDocumentContent, String pText) {
        XWPFParagraph paragraph = pDocumentContent.createParagraph();
        XWPFRun pRun= paragraph.createRun();
        pRun.setBold(true);
        pRun.setFontSize(16);
        pRun.setText(pText);
    }

    /**
     * Verify if a paragraph contains specified text exists in provided MS Word document.
     *
     * @param pDocumentContent Document where the content will be verified
     * @param pText text to be verified.
     * @return {@link Boolean}
     *
     */
    private boolean isParagraphExists(XWPFDocument pDocumentContent, String pText) {
        List<XWPFParagraph> paragraphList = pDocumentContent.getParagraphs();
        if(paragraphList.isEmpty()) {
            return false;
        }

        for (int i = 1; i < paragraphList.size(); i++) {
            if (paragraphList.get(i).getText().contains(pText) ) {
                return true;
            }
        }

        return false;
    }

    /**
     * Append overall result table to the specified MS Word document.
     *
     * @param pDocumentContent Document where the content will be appended
     * @param pResult result to be appended
     *
     */
    private void createOverallResultTable(XWPFDocument pDocumentContent, Result pResult, String secret) {

        // Create hash token for tamper-proof
        JWT jwt = new JWT(secret);
        String hashString = jwt.create("1","BU-MET","JGram", pResult);

        //create table
        XWPFTable table = pDocumentContent.createTable();

        //create first row
        XWPFTableRow tableHeaderRow = table.getRow(0);
        tableHeaderRow.getCell(0).setText("C#");
        tableHeaderRow.addNewTableCell().setText("Weight");
        tableHeaderRow.addNewTableCell().setText("Grade");
        tableHeaderRow.addNewTableCell().setText("Feedback");

        //Format Row
        tableHeaderRow.getCell(0).setColor("c0c0c0");
        tableHeaderRow.getCell(1).setColor("c0c0c0");
        tableHeaderRow.getCell(2).setColor("c0c0c0");
        tableHeaderRow.getCell(3).setColor("c0c0c0");

        for (int checkpointID : pResult.getCheckpointMap().keySet())
        {
            Checkpoint checkpoint = pResult.getCheckpointMap().get(checkpointID);

            //create checkpoint row
            XWPFTableRow tableCheckpointRow = table.createRow();
            tableCheckpointRow.getCell(0).setText("" + checkpointID);
            tableCheckpointRow.getCell(1).setText("" + checkpoint.getWeight());
            tableCheckpointRow.getCell(2).setText("" + checkpoint.getGrade());
            tableCheckpointRow.getCell(3).setText("" + checkpoint.getFeedback());

        }

        //create overall result row
        XWPFTableRow tableOverallRow = table.createRow();
        tableOverallRow.getCell(0).setText("");
        tableOverallRow.getCell(1).setText("Σ");
        tableOverallRow.getCell(2).setText(String.format("%.2f",pResult.getOverallGrade()));
        tableOverallRow.getCell(3).setText(hashString);

        //Format row
        tableOverallRow.getCell(2).setColor("8fbc8f");
    }

    /**
     * Retrieve the overall result table from the specified MS Word document.
     *
     * @param pDocumentContent Document where the table will be read from
     *
     */
    private XWPFTable getOverallResultTable(XWPFDocument pDocumentContent) {

        List<XWPFTable> tables = pDocumentContent.getTables();
        if (tables.isEmpty()) {
            return null;
        }

        // We only care about the last table, as we always append the JGRAM overall result at the bottom of the document
        XWPFTable lastTable = tables.get(tables.size() - 1);
        List<XWPFTableRow> rows = lastTable.getRows();
        if (rows.isEmpty() || rows.get(0).getTableCells().isEmpty()) {
            return null;
        }

        String firstCellText = rows.get(0).getCell(0).getText();
        if (!firstCellText.contains("C#")) {
            return null;
        }

        return lastTable;
    }
}