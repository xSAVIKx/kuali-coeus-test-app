/*
 * Copyright (c) 2014. Boston University
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND either express or
 * implied.
 *
 * See the License for the specific language governing permissions and limitations under the License.
 */
package edu.ku.kuali.kra.award.sapintegration;

import java.util.*;

/**
 * <p>A container for the results of validation of an award hierarchy transmission to
 * SAP.  The ValidationResults will indicate not only success or failure, but
 * also what validation errors were encountered (if any).
 * <p/>
 * <p>Validation errors are associated with the id of the award which triggered the error.
 * Each award undergoing validation could potentially produce zero or more errors.  Global
 * errors are supported through the use of a method for adding global validation errors
 * to the results.
 *
 * @author Eric Westfall (ewestfal@gmail.com)
 */
public final class ValidationResults {

    private final Map<Long, List<ValidationError>> validationErrorMap;

    public ValidationResults() {
        this.validationErrorMap = new HashMap<Long, List<ValidationError>>();
    }

    /**
     * Adds the given validation error to the error map for the specified award.
     *
     * @param awardId         the id of the award which triggered the error
     * @param validationError the ValidationError to add, this method will throw an
     *                        IllegalArgumentException if this value is null
     */
    public void addAwardValidationError(Long awardId, ValidationError validationError) {
        if (validationError == null) {
            throw new IllegalArgumentException("validationError should not be null");
        }
        List<ValidationError> validationErrors = establishErrorList(awardId);
        validationErrors.add(validationError);
    }

    /**
     * Adds a global validation error to the error map.
     *
     * @param validationError the ValidationError to add, this method will throw an
     *                        IllegalArgumentException if this value is null
     */
    public void addGlobalValidationError(ValidationError validationError) {
        addAwardValidationError(null, validationError);
    }

    /**
     * Returns an immutable List of global validation errors.
     *
     * @return an immutable List of global validation errors, or null if there are no
     * global validation errors.
     */
    public List<ValidationError> getGlobalValidationErrors() {
        return getAwardValidationError(null);
    }

    /**
     * Returns an immutable List of validation errors for the given award id.
     *
     * @param awardId the id of the award to locate validation errors for
     * @return an immutable List of validation errors for the award, or null if the
     * award did not trigger any validation errors
     */
    public List<ValidationError> getAwardValidationError(Long awardId) {
        List<ValidationError> validationErrors = validationErrorMap.get(awardId);
        if (validationErrors == null) {
            return null;
        }
        return Collections.unmodifiableList(validationErrors);
    }

    /**
     * Returns a Set of award ids that are in error.
     *
     * @return a Set containing award ids for awards that triggered validation errors
     */
    public Set<Long> getAwardsInError() {
        return validationErrorMap.keySet();
    }

    /**
     * Determines if the results are empty or not.  A result is empty if it has no validation errors.
     *
     * @return true if the validation results are empty, false otherwise
     */
    public boolean isEmpty() {
        return validationErrorMap.isEmpty();
    }

    /**
     * Returns a boolean indicating whether or not these ValidationResults represent
     * a successful validation.  It is preferred to use this method over attempting to
     * count validation errors using the other methods available on this class.
     *
     * @return true if the validation was successful, false otherwise
     */
    public boolean calculateSuccess() {
        for (Long awardId : validationErrorMap.keySet()) {
            List<ValidationError> validationErrors = validationErrorMap.get(awardId);
            if (!validationErrors.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Constructs and returns the ValidationError list for the given award id.
     * If the list already exists in the {@link #validationErrorMap} it will
     * return the existing list.
     *
     * @param awardId the id of the award to create the validation error list for.  If
     *                this value is null then it represents the "global" error list.
     * @return a List of ValidationError.  This method should never return null, it will
     * either return an empty list or a list of all ValidationErrors recorded so far
     * against a particular award (or at the global level).
     */
    private List<ValidationError> establishErrorList(Long awardId) {
        if (!validationErrorMap.containsKey(awardId)) {
            validationErrorMap.put(awardId, new ArrayList<ValidationError>());
        }
        return validationErrorMap.get(awardId);
    }

}
