package edu.bu.jgram.server.assessment;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the Overall grade result object.
 */
public class Result {

    private float mOverallGrade;
    private Map<Integer, Checkpoint> mCheckpointMap;
    private int mCheckpointIDCount = 1;

    public Result() {
        mCheckpointMap = new HashMap<>();
    }

    /**
     * Retrieve overall grade.
     *
     * @return {@link Float}
     */
    public float getOverallGrade() {
        return mOverallGrade;
    }

    /**
     * Set overall grade.
     *
     * @param mOverallGrade overall grade
     */
    public void setOverallGrade(float mOverallGrade) {
        this.mOverallGrade = mOverallGrade;
    }

    public synchronized void addCheckpoint(Checkpoint checkpoint) {
        mCheckpointMap.put(mCheckpointIDCount, checkpoint);
        mCheckpointIDCount++;
    }

    public Map<Integer, Checkpoint> getCheckpointMap() {
        return mCheckpointMap;
    }

    @Override
    public boolean equals(Object obj)
    {
        // If the object is compared with itself then return true
        if (obj == this) {
            return true;
        }

        /* Check if o is an instance of Result or not
          "null instanceof [type]" also returns false */
        if (!(obj instanceof Result)) {
            return false;
        }

        // typecast obj to Result so that we can compare data members
        Result result = (Result) obj;

        if (Float.compare(this.mOverallGrade, result.getOverallGrade()) != 0) {
            return false;
        }

        if (this.mCheckpointMap.size() != result.getCheckpointMap().size()) {
            return false;
        }

        if(!this.mCheckpointMap.equals(result.getCheckpointMap())) {
            return false;
        }

        return true;
    }
}