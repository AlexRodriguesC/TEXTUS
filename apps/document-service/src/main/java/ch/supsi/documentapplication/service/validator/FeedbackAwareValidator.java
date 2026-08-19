package ch.supsi.documentapplication.service.validator;

/**
 * A validator that, in addition to validating actual output against
 * expected constraints, can produce structured feedback describing
 * exactly how the actual value diverges from what was expected.
 * This feedback is intended to be fed back into a correction prompt.
 *
 * @param <A> the type of the actual value being validated
 * @param <E> the type of the expected value used as the validation reference
 * @param <F> the type of feedback describing the divergence
 */
public interface FeedbackAwareValidator<A, E, F> extends Validator<A, E> {

    /**
     * Computes structured feedback describing how the actual value
     * diverges from the expected value, without throwing.
     *
     * @param actual   the value produced (e.g. by an AI model)
     * @param expected the reference value or constraint
     * @return feedback describing the divergence; implementations should
     * make it possible to check whether the actual value is valid
     * (e.g. via an {@code isValid()} method on the feedback type)
     */
    F extractFeedback(A actual, E expected);
}
