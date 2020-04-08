
package edu.bu.jgram.server.assessment;

/**
 * Represents a grade evaluator. Given a set of checkpoints, it provides a way to evaluate the grade
 */
public interface Evaluator {
    Result evaluate();
}