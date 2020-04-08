package edu.bu.jgram.server.assessment;

import java.util.List;
import java.util.logging.Logger;

/**
 * Represents a Just-in-time grade evaluator. Given a set of checkpoints, it provides a way to evaluate the grade
 */
public class JustInTimeEvaluator
        implements Evaluator {

    private static final Logger LOGGER = Logger.getLogger(JustInTimeEvaluator.class.getName());
    private final List<Checkpoint> mCheckpointList;

    public JustInTimeEvaluator(List<Checkpoint> pCheckpointList) {
        mCheckpointList = pCheckpointList;
    }

    /**
     * Evaluate overall grade based on multiple checkpoint containing weight and grade.
     * <p>
     * $`Grades = [A, B, A, A, A, A, A, A, A, B]`$ in 10 JGRAMs (taking A=95, B=85) and the weights are, respectively,
     * $`Weights = [4, 5, 6, 7, 8, 9, 10, 1, 1, 1]`$ then the student's grade would be
     * $`=\frac{[95*4 + 85*5 + 95*67 + 95*8 + 95*9,  + 95*10 + 95*1 + 95*1 + 85*1]}{ [4+5+6+7+8+9+10+1+1+1]}`$
     *
     * @return {@link Result}
     */
    public Result evaluate() {

        Result result = new Result();

        // No point evaluating 0 checkpoints aka JGRAMs
        if (mCheckpointList.isEmpty()) {
            LOGGER.warning("There are no checkpoint aka JGRAM defined");
            return result;
        }

        float overallResult = 0;
        int overallWeight = 0;

        for (Checkpoint checkpoint : mCheckpointList) {
            overallResult = overallResult + checkpoint.getGrade() * checkpoint.getWeight();
            overallWeight = overallWeight + checkpoint.getWeight();
            result.addCheckpoint(checkpoint);
        }

        float overall = overallResult / overallWeight;

        result.setOverallGrade(overall);

        return result;
    }

}