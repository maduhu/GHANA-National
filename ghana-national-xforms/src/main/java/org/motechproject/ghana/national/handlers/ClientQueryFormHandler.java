package org.motechproject.ghana.national.handlers;

import org.motechproject.appointments.api.service.contract.VisitResponse;
import org.motechproject.ghana.national.bean.ClientQueryForm;
import org.motechproject.ghana.national.domain.ClientQueryType;
import org.motechproject.ghana.national.domain.Patient;
import org.motechproject.ghana.national.domain.SMSTemplate;
import org.motechproject.ghana.national.domain.sms.UpcomingCareSMS;
import org.motechproject.ghana.national.exception.XFormHandlerException;
import org.motechproject.ghana.national.messagegateway.domain.MessageDispatcher;
import org.motechproject.ghana.national.domain.SMSPayload;
import org.motechproject.ghana.national.repository.AllAppointments;
import org.motechproject.ghana.national.repository.AllCareSchedules;
import org.motechproject.ghana.national.repository.SMSGateway;
import org.motechproject.ghana.national.service.PatientService;
import org.motechproject.ghana.national.service.PregnancyService;
import org.motechproject.mrs.model.MRSPatient;
import org.motechproject.openmrs.advice.ApiSession;
import org.motechproject.openmrs.advice.LoginAsAdmin;
import org.motechproject.scheduletracking.api.service.EnrollmentRecord;
import org.motechproject.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.motechproject.ghana.national.domain.Constants.NO_MATCHING_RECORDS_FOUND;

@Component
public class ClientQueryFormHandler{

    public static final String PREGNANT_CLIENT_QUERY_RESPONSE_SMS_KEY = "PREGNANT_CLIENT_QUERY_RESPONSE_SMS_KEY";
    public static final String NON_PREGNANT_CLIENT_QUERY_RESPONSE_SMS_KEY = "NON_PREGNANT_CLIENT_QUERY_RESPONSE_SMS_KEY";
    public static final String FIND_CLIENT_RESPONSE_SMS_KEY = "FIND_CLIENT_RESPONSE_SMS_KEY";

    @Autowired
    private PatientService patientService;
    @Autowired
    private PregnancyService pregnancyService;
    @Autowired
    private SMSGateway smsGateway;
    @Autowired
    private AllCareSchedules allCareSchedules;
    @Autowired
    private AllAppointments allAppointments;
    Logger log = LoggerFactory.getLogger(this.getClass());

    @LoginAsAdmin
    @ApiSession
    public void handleFormEvent(ClientQueryForm clientQueryForm) {
        try {
            if (clientQueryForm.getQueryType().equals(ClientQueryType.CLIENT_DETAILS.toString())) {
                getPatientDetails(clientQueryForm);
            } else if (clientQueryForm.getQueryType().equals(ClientQueryType.FIND_CLIENT_ID.toString())) {
                searchPatient(clientQueryForm);
            } else if (clientQueryForm.getQueryType().equals(ClientQueryType.UPCOMING_CARE.toString())) {
                queryUpcomingCareAndVisits(clientQueryForm);
            }
        } catch (Exception e) {
            log.warn("Encountered error while processing client query form", e);
            throw new XFormHandlerException("Encountered error while processing client query form", e);
        }
    }

    private void queryUpcomingCareAndVisits(ClientQueryForm clientQueryForm) {
        Patient patient = patientService.getPatientByMotechId(clientQueryForm.getMotechId());
        List<EnrollmentRecord> upcomingEnrollments = allCareSchedules.upcomingCareForCurrentWeek(patient.getMRSPatientId());
        List<VisitResponse> upcomingAppointments = allAppointments.upcomingAppointmentsForCurrentWeek(patient.getMotechId());
        UpcomingCareSMS upcomingCareSMS = new UpcomingCareSMS(smsGateway, patient, upcomingEnrollments, upcomingAppointments);
        smsGateway.dispatchSMS(clientQueryForm.getSender(), upcomingCareSMS.smsText());
    }

    private void searchPatient(ClientQueryForm clientQueryForm) {
        List<MRSPatient> patients = patientService.getPatients(clientQueryForm.getFirstName(), clientQueryForm.getLastName(), clientQueryForm.getPhoneNumber(), clientQueryForm.getDateOfBirth(), clientQueryForm.getNhis());
        String message = (patients.size() == 0) ? NO_MATCHING_RECORDS_FOUND : createMessage(patients);
        smsGateway.dispatchSMS(clientQueryForm.getSender(), message);
    }

    private void getPatientDetails(ClientQueryForm clientQueryForm) {
        Patient patient = patientService.getPatientByMotechId(clientQueryForm.getMotechId());
        Date pregnancyEDD = pregnancyService.activePregnancyEDD(patient.getMotechId());
        Map<String, String> smsTemplateValues = new SMSTemplate().fillPatientDetails(patient)
                .fillFacilityDetails(patient).fillEDD(DateUtil.newDate(pregnancyEDD)).getRuntimeVariables();

        smsGateway.dispatchSMS(getTemplateKey(pregnancyEDD), smsTemplateValues, clientQueryForm.getSender());
    }

    private String getTemplateKey(Date pregnancyEDD) {
        return (null != pregnancyEDD) ? PREGNANT_CLIENT_QUERY_RESPONSE_SMS_KEY : NON_PREGNANT_CLIENT_QUERY_RESPONSE_SMS_KEY;
    }

    private String createMessage(List<MRSPatient> patients) {
        String messageKey = FIND_CLIENT_RESPONSE_SMS_KEY;
        StringBuilder message = new StringBuilder();
        for (final MRSPatient mrsPatient : patients) {
            Patient patient = new Patient(mrsPatient);
            Map<String, String> smsTemplateValues = new SMSTemplate().fillPatientDetails(patient).fillFacilityDetails(patient).getRuntimeVariables();
            message.append(SMSPayload.fill(smsGateway.getSMSTemplate(messageKey), smsTemplateValues)).append(MessageDispatcher.SMS_SEPARATOR);
        }
        return message.toString();
    }
}
