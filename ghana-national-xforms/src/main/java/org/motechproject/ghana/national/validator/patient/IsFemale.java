package org.motechproject.ghana.national.validator.patient;

import org.motechproject.ghana.national.domain.Patient;
import org.motechproject.mobileforms.api.domain.FormBean;
import org.motechproject.mobileforms.api.domain.FormError;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.motechproject.ghana.national.domain.Constants.*;

public class IsFemale extends PatientValidator{
    public List<FormError> validate(Patient patient, List<FormBean> formsSubmitted){
        return patient.getMrsPatient().getPerson().getGender().equals(PATIENT_GENDER_MALE)?
                Arrays.asList(new FormError(MOTECH_ID_ATTRIBUTE_NAME, GENDER_ERROR_MSG)): Collections.<FormError>emptyList();
    }
}
