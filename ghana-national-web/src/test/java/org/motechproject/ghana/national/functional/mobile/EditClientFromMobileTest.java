package org.motechproject.ghana.national.functional.mobile;

import junit.framework.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.functional.framework.XformHttpClient;
import org.motechproject.ghana.national.functional.Generator.FacilityGenerator;
import org.motechproject.ghana.national.functional.Generator.PatientGenerator;
import org.motechproject.ghana.national.functional.Generator.StaffGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.motechproject.functional.framework.XformHttpClient.XFormParser;
import static org.testng.AssertJUnit.assertEquals;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:/applicationContext-functional-tests.xml"})
public class EditClientFromMobileTest {

    @Autowired
    StaffGenerator staffGenerator;
    @Autowired
    FacilityGenerator facilityGenerator;
    @Autowired
    PatientGenerator patientGenerator;

    @Test
    public void shouldCheckForMandatoryFields() throws Exception {

        final XformHttpClient.XformResponse xformResponse = setupEditClientFormAndUpload(new HashMap<String, String>() {{
        }});

        final List<XformHttpClient.Error> errors = xformResponse.getErrors();
        assertEquals(errors.size(), 1);
        final Map<String, List<String>> errorsMap = errors.iterator().next().getErrors();

        assertThat(errorsMap.get("staffId"), hasItem("is mandatory"));
        assertThat(errorsMap.get("updatePatientFacilityId"), hasItem("is mandatory"));
        assertThat(errorsMap.get("motechId"), hasItem("is mandatory"));
        assertThat(errorsMap.get("date"), hasItem("is mandatory"));
    }

    @Test
    public void shouldGiveErrorIfIdsAreNotFound() throws Exception {
        final XformHttpClient.XformResponse xformResponse = setupEditClientFormAndUpload(new HashMap<String, String>() {{
            put("facilityId", "testFacilityId");
            put("motechId", "testMotechId");
            put("staffId", "testStaffId");
            put("motherMotechId", "testMotherMotechId");
        }});

        final List<XformHttpClient.Error> errors = xformResponse.getErrors();
        assertEquals(errors.size(), 1);
        final Map<String, List<String>> errorsMap = errors.iterator().next().getErrors();

        assertThat(errorsMap.get("facilityId"), hasItem("not found"));
        assertThat(errorsMap.get("staffId"), hasItem("not found"));
        assertThat(errorsMap.get("motechId"), hasItem("not found"));

    }

    @Test
    public void shouldNotGiveErrorForFirstNameIfGiven() throws Exception {

        final XformHttpClient.XformResponse xformResponse = setupEditClientFormAndUpload(new HashMap<String, String>() {{
            put("firstName", "Joe");
        }});

        final List<XformHttpClient.Error> errors = xformResponse.getErrors();
        assertEquals(errors.size(), 1);
        final Map<String, List<String>> errorsMap = errors.iterator().next().getErrors();
        assertNull(errorsMap.get("firstName"));
    }

    @Test
    @Ignore
    public void shouldUpdatePatientIfNoErrorsAreFound() throws Exception {
        final String staffId = staffGenerator.createStaffAndReturnStaffId();
        final String facilityId = facilityGenerator.createFacilityAndReturnFacilityId();
        final String patientId = patientGenerator.createPatientAndReturnPatientId(facilityId);
        final String date = "2011-01-01";
        final XformHttpClient.XformResponse xformResponse = setupEditClientFormAndUpload(new HashMap<String, String>() {{
            put("facilityId", facilityId);
            put("motechId", patientId);
            put("staffId", staffId);
            put("date", date);
        }});

        final List<XformHttpClient.Error> errors = xformResponse.getErrors();
        Assert.assertEquals(errors.size(), 0);
    }

    private XformHttpClient.XformResponse setupEditClientFormAndUpload(Map<String, String> data) throws Exception {
        return XformHttpClient.execute("http://localhost:8080/ghana-national-web/formupload",
                "NurseDataEntry", XFormParser.parse("mobile-midwife-template.xml", data));
    }
}
