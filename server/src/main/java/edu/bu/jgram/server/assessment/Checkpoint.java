package edu.bu.jgram.server.assessment;

/**
 * Represents the checkpoint object. Grammar for checkpoint in document is
 * CHECKPOINT( WEIGHT=7, GRADE=?, FEEDBACK=[])
 */
public class Checkpoint {
    private int mWeight;
    private int mGrade;
    private String mFeedback;

    public Checkpoint(int pWeight, int pGrade, String pFeedback) {
        mWeight = pWeight;
        mGrade = pGrade;
        mFeedback = pFeedback;
    }

    /**
     * Retrieve weight from the checkpoint.
     *
     * @return {@link Integer}
     */
    public int getWeight() {
        return mWeight;
    }

    /**
     * Retrieve grade letter from the checkpoint.
     *
     * @return {@link Integer}
     */
    public int getGrade() {
        return mGrade;
    }

    /**
     * Retrieve feedback from the checkpoint.
     *
     * @return {@link String}
     */
    public String getFeedback() {
        return mFeedback;
    }

    /**
     * String representation for checkpoint.
     *
     * @return {@link String}
     */
    public String toString() {
        return String.format("Weight:%d , Grade:%d, Feedback:%s", mWeight, mGrade, mFeedback);
    }

    @Override
    public boolean equals(Object obj)
    {
        // If the object is compared with itself then return true
        if (obj == this) {
            return true;
        }

        /* Check if o is an instance of Checkpoint or not
          "null instanceof [type]" also returns false */
        if (!(obj instanceof Checkpoint)) {
            return false;
        }

        // typecast obj to Checkpoint so that we can compare data members
        Checkpoint checkpoint = (Checkpoint) obj;

        if (this.mGrade != checkpoint.mGrade) {
            return false;
        }

        if (this.mWeight != checkpoint.mWeight) {
            return false;
        }

        if (!this.mFeedback.equals(checkpoint.mFeedback)) {
            return false;
        }

        return true;
    }
}