package edu.bu.jgram.server.assessment;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents the grade mapping.
 * A+=97, A=95, A-=93 etc.
 */
public class GradeMapping {

    private Map<String, Integer> mLimits;

    public GradeMapping() {
        mLimits = new LinkedHashMap<>();
    }

    /**
     * Set mapped value for specified grade i.e. A=97.
     *
     * @param pGradeLetter grade letter
     * @param pGradeLimit   grade mapped value
     *
     * @throws InvalidValueException Throws if pGradeLetter is empty.
     *
     */
    public void setGrade(String pGradeLetter, int pGradeLimit) throws InvalidValueException {
        if (pGradeLetter == null || pGradeLetter.trim().length() == 0) {
            throw new InvalidValueException("Empty grade is not permissible");
        }
        mLimits.put(pGradeLetter, pGradeLimit);
    }

    /**
     * Retrieve mapped value for specified grade i.e. if specified grade is A, it will return 97.
     *
     * @param pGradeLetter grade letter
     *
     * @throws InvalidValueException throws if grade mapping not set for specified pGradeLetter
     *
     * @return {@link Integer}
     */
    public int getGrade(String pGradeLetter) throws InvalidValueException {
        boolean containsGrade = mLimits.containsKey(pGradeLetter);
        if (!containsGrade) {
            throw new InvalidValueException(String.format("Missing grade %s mapping", pGradeLetter));
        }

        return mLimits.get(pGradeLetter);
    }


}