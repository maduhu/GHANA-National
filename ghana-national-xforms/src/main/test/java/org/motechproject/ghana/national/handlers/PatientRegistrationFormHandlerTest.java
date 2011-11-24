package org.motechproject.ghana.national.handlers;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.motechproject.ghana.national.bean.RegisterClientForm;
import org.motechproject.ghana.national.domain.Patient;
import org.motechproject.ghana.national.domain.PatientAttributes;
import org.motechproject.ghana.national.domain.PatientType;
import org.motechproject.ghana.national.domain.RegistrationType;
import org.motechproject.ghana.national.exception.ParentNotFoundException;
import org.motechproject.ghana.national.exception.PatientIdIncorrectFormatException;
import org.motechproject.ghana.national.exception.PatientIdNotUniqueException;
import org.motechproject.ghana.national.service.PatientService;
import org.motechproject.model.MotechEvent;
import org.motechproject.mrs.model.Attribute;
import org.motechproject.openmrs.advice.LoginAsAdmin;
import org.motechproject.server.event.annotations.MotechListener;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.HashMap;

import static ch.lambdaj.Lambda.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.MockitoAnnotations.initMocks;

public class PatientRegistrationFormHandlerTest {

    @Mock
    PatientService mockPatientService;

    PatientRegistrationFormHandler patientRegistrationFormHandler;


    @Before
    public void setUp() {
        initMocks(this);
        patientRegistrationFormHandler = new PatientRegistrationFormHandler();
        ReflectionTestUtils.setField(patientRegistrationFormHandler, "patientService", mockPatientService);
    }

    @Test
    public void shouldHandleRegisterClientEventAndInvokeService() throws ParentNotFoundException, PatientIdIncorrectFormatException, PatientIdNotUniqueException {
        HashMap<String, Object> parameters = new HashMap<String, Object>();

        RegisterClientForm registerClientForm = new RegisterClientForm();
        String address = "Address";
        Date dateofBirth = new Date(10, 10, 2011);
        String district = "District";
        Boolean isBirthDateEstimated = true;
        String facilityId = "FacilityID";
        String firstName = "FirstName";
        Boolean insured = true;
        String lastName = "LastName";
        String middleName = "MiddleName";
        String motechId = "motechId";
        Date nhisExpDate = new Date(10, 10, 2022);
        String nhisNumber = "NhisNumber";
        String parentId = "ParentId";
        String preferredName = "PreferredName";
        String region = "Region";
        RegistrationType registrationMode = RegistrationType.USE_PREPRINTED_ID;
        String sex = "M";
        String subDistrict = "SubDistrict";
        String phoneNumber = "0123456789";
        PatientType patientType = PatientType.CHILD_UNDER_FIVE;

        registerClientForm.setAddress(address);
        registerClientForm.setDateOfBirth(dateofBirth);
        registerClientForm.setDistrict(district);
        registerClientForm.setEstimatedBirthDate(isBirthDateEstimated);
        registerClientForm.setFacilityId(facilityId);
        registerClientForm.setFirstName(firstName);
        registerClientForm.setInsured(insured);
        registerClientForm.setLastName(lastName);
        registerClientForm.setMiddleName(middleName);
        registerClientForm.setMotechId(motechId);
        registerClientForm.setNhisExpires(nhisExpDate);
        registerClientForm.setNhis(nhisNumber);
        registerClientForm.setMotherMotechId(parentId);
        registerClientForm.setPrefferedName(preferredName);
        registerClientForm.setRegion(region);
        registerClientForm.setRegistrationMode(registrationMode);
        registerClientForm.setSex(sex);
        registerClientForm.setSubDistrict(subDistrict);
        registerClientForm.setRegistrantType(patientType);
        registerClientForm.setPhoneNumber(phoneNumber);
        parameters.put(PatientRegistrationFormHandler.FORM_BEAN, registerClientForm);
        MotechEvent event = new MotechEvent("subject", parameters);

        ArgumentCaptor<Patient> patientArgumentCaptor = ArgumentCaptor.forClass(Patient.class);
        ArgumentCaptor<PatientType> patientTypeArgumentCaptor = ArgumentCaptor.forClass(PatientType.class);
        ArgumentCaptor<String> parentIdCaptor = ArgumentCaptor.forClass(String.class);
        doNothing().when(mockPatientService).registerPatient(patientArgumentCaptor.capture(), patientTypeArgumentCaptor.capture(), parentIdCaptor.capture());

        patientRegistrationFormHandler.handleFormEvent(event);

        Patient savedPatient = patientArgumentCaptor.getValue();

        assertThat(savedPatient.mrsPatient().getAddress(), is(equalTo(address)));
        assertThat(savedPatient.mrsPatient().getDateOfBirth(), is(equalTo(dateofBirth)));
        assertThat(savedPatient.mrsPatient().getFacility().getId(), is(equalTo(facilityId)));
        assertThat(savedPatient.mrsPatient().getFacility().getCountyDistrict(), is(equalTo(null)));
        assertThat(savedPatient.mrsPatient().getFacility().getRegion(), is(equalTo(null)));
        assertThat(savedPatient.mrsPatient().getFacility().getStateProvince(), is(equalTo(null)));
        assertThat(savedPatient.mrsPatient().getBirthDateEstimated(), is(equalTo(isBirthDateEstimated)));
        assertThat(savedPatient.mrsPatient().getFirstName(), is(equalTo(firstName)));
        assertThat(savedPatient.mrsPatient().getLastName(), is(equalTo(lastName)));
        assertThat(savedPatient.mrsPatient().getMiddleName(), is(equalTo(middleName)));
        assertThat(savedPatient.mrsPatient().getId(), is(equalTo(motechId)));
        assertThat(savedPatient.mrsPatient().getPreferredName(), is(equalTo(preferredName)));
        assertThat(savedPatient.mrsPatient().getGender(), is(equalTo(sex)));
        assertThat(((Attribute) selectUnique(savedPatient.mrsPatient().getAttributes(), having(on(Attribute.class).name(),
                equalTo(PatientAttributes.NHIS_EXPIRY_DATE.getAttribute())))).value(), is(equalTo(nhisExpDate.toString())));
        assertThat(((Attribute) selectUnique(savedPatient.mrsPatient().getAttributes(), having(on(Attribute.class).name(),
                equalTo(PatientAttributes.NHIS_NUMBER.getAttribute())))).value(), is(equalTo(nhisNumber)));
        assertThat(((Attribute) selectUnique(savedPatient.mrsPatient().getAttributes(), having(on(Attribute.class).name(),
                equalTo(PatientAttributes.INSURED.getAttribute())))).value(), is(equalTo(insured.toString())));
        assertThat(((Attribute) selectUnique(savedPatient.mrsPatient().getAttributes(), having(on(Attribute.class).name(),
                equalTo(PatientAttributes.PHONE_NUMBER.getAttribute())))).value(), is(equalTo(phoneNumber)));

        assertThat(parentIdCaptor.getValue(), is(equalTo(registerClientForm.getMotherMotechId())));
        assertThat(patientTypeArgumentCaptor.getValue(), is(equalTo(patientType)));

    }

    @Test
    public void shouldBeRegisteredAsAListenerForRegisterPatientEvent() throws NoSuchMethodException {
        String[] registeredEventSubject = patientRegistrationFormHandler.getClass().getMethod("handleFormEvent", new Class[]{MotechEvent.class}).getAnnotation(MotechListener.class).subjects();
        assertThat(registeredEventSubject, is(equalTo(new String[]{"form.validation.successful.NurseDataEntry.registerPatient-jf"})));
    }

    @Test
    public void shouldRunAsAdminUser() throws NoSuchMethodException {
        assertThat(patientRegistrationFormHandler.getClass().getMethod("handleFormEvent", new Class[]{MotechEvent.class}).getAnnotation(LoginAsAdmin.class), is(not(equalTo(null))));
    }

}
