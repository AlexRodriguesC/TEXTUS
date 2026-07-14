package ch.supsi.documentapplication.service.validator;

import ch.supsi.documentapplication.exception.AIOutputMappingFailedException;

/**
 * Validates an actual value against an expected value, typically used to
 * verify that AI-generated output conforms to expected structural or
 * semantic constraints.
 *
 * @param <A> the type of the actual value being validated
 * @param <E> the type of the expected value used as the validation reference
 */
public interface Validator<A, E> {

    /**
     * Validates the given actual value against the expected value.
     *
     * @param actual   the value produced (e.g. by an AI model) to validate
     * @param expected the reference value or constraint the actual value must satisfy
     * @throws AIOutputMappingFailedException if the actual value does not satisfy
     *                                         the expected value or constraint
     */
    void validate(A actual, E expected) throws AIOutputMappingFailedException;
}
