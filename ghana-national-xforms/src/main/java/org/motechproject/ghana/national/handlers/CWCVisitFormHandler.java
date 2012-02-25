package org.motechproject.ghana.national.handlers;

import org.motechproject.ghana.national.bean.CWCVisitForm;
import org.motechproject.ghana.national.domain.Constants;
import org.motechproject.ghana.national.domain.Facility;
import org.motechproject.ghana.national.domain.Patient;
import org.motechproject.ghana.national.service.ChildVisitService;
import org.motechproject.ghana.national.service.FacilityService;
import org.motechproject.ghana.national.service.PatientService;
import org.motechproject.ghana.national.service.StaffService;
import org.motechproject.ghana.national.vo.CWCVisit;
import org.motechproject.mobileforms.api.callbacks.FormPublishHandler;
import org.motechproject.model.MotechEvent;
import org.motechproject.mrs.model.MRSUser;
import org.motechproject.openmrs.advice.ApiSession;
import org.motechproject.openmrs.advice.LoginAsAdmin;
import org.motechproject.server.event.annotations.MotechListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CWCVisitFormHandler implements FormPublishHandler {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    ChildVisitService childVisitService;

    @Autowired
    FacilityService facilityService;
    @Autowired
    private PatientService patientService;
    @Autowired
    private StaffService staffService;

    @Override
    @MotechListener(subjects = "form.validation.successful.NurseDataEntry.cwcVisit")
    @LoginAsAdmin
    @ApiSession
    public void handleFormEvent(MotechEvent event) {
        try {
            CWCVisitForm form = (CWCVisitForm) event.getParameters().get(Constants.FORM_BEAN);
            childVisitService.save(cwcVisitFor(form));
        } catch (Exception e) {
            log.error("Encountered error while saving CWC visit details", e);
        }
    }

    private CWCVisit cwcVisitFor(CWCVisitForm form) {
        Facility facility = facilityService.getFacilityByMotechId(form.getFacilityId());
        Patient patient = patientService.getPatientByMotechId(form.getMotechId());
        MRSUser staff = staffService.getUserByEmailIdOrMotechId(form.getStaffId());

        return new CWCVisit().staff(staff).facility(facility).patient(patient)
                .date(form.getDate()).serialNumber(form.getSerialNumber()).weight(form.getWeight())
                .height(form.getHeight()).muac(form.getMuac()).maleInvolved(form.getMaleInvolved())
                .iptidose(form.getIptidose()).pentadose(form.getPentadose()).opvdose(form.getOpvdose())
                .cwcLocation(form.getCwcLocation()).comments(form.getComments()).house(form.getHouse())
                .community(form.getCommunity()).immunizations(form.immunizations());
    }
}