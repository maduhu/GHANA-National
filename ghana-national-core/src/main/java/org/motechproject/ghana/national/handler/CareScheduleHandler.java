package org.motechproject.ghana.national.handler;

import org.joda.time.LocalDate;
import org.motechproject.ghana.national.domain.Facility;
import org.motechproject.ghana.national.domain.Patient;
import org.motechproject.ghana.national.domain.SMS;
import org.motechproject.ghana.national.repository.AllFacilities;
import org.motechproject.ghana.national.repository.AllPatients;
import org.motechproject.ghana.national.service.TextMessageService;
import org.motechproject.ghana.national.vo.Pregnancy;
import org.motechproject.openmrs.advice.ApiSession;
import org.motechproject.openmrs.advice.LoginAsAdmin;
import org.motechproject.scheduletracking.api.domain.WindowName;
import org.motechproject.scheduletracking.api.events.MilestoneEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;

import static org.motechproject.ghana.national.configuration.TextMessageTemplateVariables.*;

@Component
public class CareScheduleHandler {
    public static final String PREGNANCY_ALERT_SMS_KEY = "PREGNANCY_ALERT_SMS_KEY";
    private AllPatients allPatients;
    private TextMessageService textMessageService;
    private AllFacilities allFacilities;

    public CareScheduleHandler() {
    }

    @Autowired
    public CareScheduleHandler(AllPatients allPatients, TextMessageService textMessageService, AllFacilities allFacilities) {
        this.allPatients = allPatients;
        this.textMessageService = textMessageService;
        this.allFacilities = allFacilities;
    }

    @LoginAsAdmin
    @ApiSession
    public void handlePregnancyAlert(final MilestoneEvent milestoneEvent) {
        String externalId = milestoneEvent.getExternalId();
        LocalDate conceptionDate = milestoneEvent.getReferenceDate();

        final Patient patient = allPatients.patientByOpenmrsId(externalId);
        Pregnancy pregnancy = Pregnancy.basedOnConceptionDate(conceptionDate);
        final LocalDate dateOfDelivery = pregnancy.dateOfDelivery();
        final String motechId = patient.getMrsPatient().getMotechId();
        Facility facility = allFacilities.getFacility(patient.getMrsPatient().getFacility().getId());

        SMS sms = textMessageService.getSMS(PREGNANCY_ALERT_SMS_KEY, new HashMap<String, String>() {{
            put(MOTECH_ID, motechId);
            put(EDD, dateOfDelivery.toString());
            put(WINDOW, getWindowName(milestoneEvent.getWindowName()));
            put(FIRST_NAME, patient.getFirstName());
            put(LAST_NAME, patient.getLastName());
            put(CARE_SERVICE, milestoneEvent.getScheduleName());
        }});
        textMessageService.sendSMS(facility, sms);
    }

    private String getWindowName(String windowName) {
        return windowName.equals(WindowName.earliest.name()) ? "Upcoming" : (windowName.equals(WindowName.due.name()) ? "Due" : "Overdue");
    }
}
