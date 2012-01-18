package org.motechproject.ghana.national.service;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.motechproject.ghana.national.domain.*;
import org.motechproject.ghana.national.domain.mobilemidwife.MobileMidwifeEnrollment;
import org.motechproject.ghana.national.repository.AllEncounters;
import org.motechproject.ghana.national.vo.ANCVO;
import org.motechproject.mrs.model.*;
import org.unitils.reflectionassert.ReflectionComparatorMode;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;

public class ANCServiceTest {
    private ANCService ancService;

    @Mock
    StaffService mockStaffService;
    @Mock
    PatientService mockPatientService;
    @Mock
    FacilityService mockFacilityService;
    @Mock
    AllEncounters mockAllEncounters;
    @Mock
    MobileMidwifeService mockMockMidwifeService;

    @Before
    public void setUp() {
        initMocks(this);
        ancService = new ANCService();
        setField(ancService, "staffService", mockStaffService);
        setField(ancService, "patientService", mockPatientService);
        setField(ancService, "facilityService", mockFacilityService);
        setField(ancService, "allEncounters", mockAllEncounters);
        setField(ancService, "mobileMidwifeService", mockMockMidwifeService);
    }

    @Test
    public void shouldEnrollANC() throws Exception {
        Date registrationDate = new Date(2012, 3, 1);
        final ANCVO ancvo = createTestANCVO("3", new Date(2011, 12, 9), "4", new Date(2011, 7, 5), RegistrationToday.IN_PAST, registrationDate);
        final Date observationDate = new Date();
        MRSUser mockMRSUser = mock(MRSUser.class);
        Patient mockPatient = mock(Patient.class);
        MRSPatient mockMRSPatient = mock(MRSPatient.class);
        Facility mockFacility = mock(Facility.class);
        MRSFacility mockMRSFacility = mock(MRSFacility.class);
        MRSPerson mockMRSPerson = mock(MRSPerson.class);

        when(mockStaffService.getUserByEmailIdOrMotechId(ancvo.getStaffId())).thenReturn(mockMRSUser);
        when(mockPatientService.getPatientByMotechId(ancvo.getMotechPatientId())).thenReturn(mockPatient);
        when(mockPatient.getMrsPatient()).thenReturn(mockMRSPatient);
        when(mockFacilityService.getFacility(ancvo.getFacilityId())).thenReturn(mockFacility);
        when(mockFacility.mrsFacility()).thenReturn(mockMRSFacility);
        when(mockMRSUser.getPerson()).thenReturn(mockMRSPerson);

        ancService.enroll(ancvo);

        ArgumentCaptor<MRSEncounter> captor = ArgumentCaptor.forClass(MRSEncounter.class);
        verify(mockAllEncounters).save(captor.capture());
        MRSEncounter actualEncounter = captor.getValue();
        assertEquals(mockMRSPatient, actualEncounter.getPatient());
        assertEquals(mockMRSUser, actualEncounter.getCreator());
        assertEquals(mockMRSFacility, actualEncounter.getFacility());
        assertEquals(mockMRSPerson, actualEncounter.getProvider());
        assertEquals(8, actualEncounter.getObservations().size());
        assertEquals(registrationDate, actualEncounter.getDate());
        final HashSet<MRSObservation> expectedObservations = new HashSet<MRSObservation>() {{
            add(new MRSObservation<Integer>(observationDate, Constants.CONCEPT_GRAVIDA, ancvo.getGravida()));
            add(new MRSObservation<Double>(observationDate, Constants.CONCEPT_HEIGHT, ancvo.getHeight()));
            add(new MRSObservation<Integer>(observationDate, Constants.CONCEPT_PARITY, ancvo.getParity()));
            add(new MRSObservation<Date>(observationDate, Constants.CONCEPT_EDD, ancvo.getEstimatedDateOfDelivery()));
            add(new MRSObservation<Boolean>(observationDate, Constants.CONCEPT_CONFINEMENT_CONFIRMED, ancvo.getDeliveryDateConfirmed()));
            add(new MRSObservation<String>(observationDate, Constants.CONCEPT_ANC_REG_NUM, ancvo.getSerialNumber()));
            add(new MRSObservation<Integer>(ancvo.getLastIPTDate(), Constants.CONCEPT_IPT, Integer.valueOf(ancvo.getLastIPT())));
            add(new MRSObservation<Integer>(ancvo.getLastTTDate(), Constants.CONCEPT_TT, Integer.valueOf(ancvo.getLastTT())));
        }};

        assertReflectionEquals(actualEncounter.getObservations(), expectedObservations, ReflectionComparatorMode.LENIENT_DATES, ReflectionComparatorMode.LENIENT_ORDER);
    }

    @Test
    public void shouldNotAddObsIfValueNotGiven() {
        Date registrationDate = new Date(2012, 1, 1);
        final ANCVO ancvo = createTestANCVO(null, null, null, null, RegistrationToday.IN_PAST_IN_OTHER_FACILITY, registrationDate);
        final Date observationDate = new Date();
        MRSUser mockMRSUser = mock(MRSUser.class);
        Patient mockPatient = mock(Patient.class);
        MRSPatient mockMRSPatient = mock(MRSPatient.class);
        Facility mockFacility = mock(Facility.class);
        MRSFacility mockMRSFacility = mock(MRSFacility.class);
        MRSPerson mockMRSPerson = mock(MRSPerson.class);

        when(mockStaffService.getUserByEmailIdOrMotechId(ancvo.getStaffId())).thenReturn(mockMRSUser);
        when(mockPatientService.getPatientByMotechId(ancvo.getMotechPatientId())).thenReturn(mockPatient);
        when(mockPatient.getMrsPatient()).thenReturn(mockMRSPatient);
        when(mockFacilityService.getFacility(ancvo.getFacilityId())).thenReturn(mockFacility);
        when(mockFacility.mrsFacility()).thenReturn(mockMRSFacility);
        when(mockMRSUser.getPerson()).thenReturn(mockMRSPerson);

        ancService.enroll(ancvo);

        ArgumentCaptor<MRSEncounter> captor = ArgumentCaptor.forClass(MRSEncounter.class);
        verify(mockAllEncounters).save(captor.capture());
        MRSEncounter actualEncounter = captor.getValue();
        assertEquals(mockMRSPatient, actualEncounter.getPatient());
        assertEquals(mockMRSUser, actualEncounter.getCreator());
        assertEquals(mockMRSFacility, actualEncounter.getFacility());
        assertEquals(mockMRSPerson, actualEncounter.getProvider());
        assertEquals(6, actualEncounter.getObservations().size());
        assertEquals(registrationDate, actualEncounter.getDate());
        final HashSet<MRSObservation> expectedObservations = new HashSet<MRSObservation>() {{
            add(new MRSObservation<Integer>(observationDate, Constants.CONCEPT_GRAVIDA, ancvo.getGravida()));
            add(new MRSObservation<Double>(observationDate, Constants.CONCEPT_HEIGHT, ancvo.getHeight()));
            add(new MRSObservation<Integer>(observationDate, Constants.CONCEPT_PARITY, ancvo.getParity()));
            add(new MRSObservation<Date>(observationDate, Constants.CONCEPT_EDD, ancvo.getEstimatedDateOfDelivery()));
            add(new MRSObservation<Boolean>(observationDate, Constants.CONCEPT_CONFINEMENT_CONFIRMED, ancvo.getDeliveryDateConfirmed()));
            add(new MRSObservation<String>(observationDate, Constants.CONCEPT_ANC_REG_NUM, ancvo.getSerialNumber()));
        }};

        assertReflectionEquals(actualEncounter.getObservations(), expectedObservations, ReflectionComparatorMode.LENIENT_DATES, ReflectionComparatorMode.LENIENT_ORDER);

    }

    @Test
    public void shouldSetRegistrationDateAsCurrentDateIfRegistrationToday() {
        final ANCVO ancvo = createTestANCVO(null, null, null, null, RegistrationToday.TODAY, new Date(2012, 1, 1));
        final Date observationDate = new Date();
        MRSUser mockMRSUser = mock(MRSUser.class);
        Patient mockPatient = mock(Patient.class);
        MRSPatient mockMRSPatient = mock(MRSPatient.class);
        Facility mockFacility = mock(Facility.class);
        MRSFacility mockMRSFacility = mock(MRSFacility.class);
        MRSPerson mockMRSPerson = mock(MRSPerson.class);

        when(mockStaffService.getUserByEmailIdOrMotechId(ancvo.getStaffId())).thenReturn(mockMRSUser);
        when(mockPatientService.getPatientByMotechId(ancvo.getMotechPatientId())).thenReturn(mockPatient);
        when(mockPatient.getMrsPatient()).thenReturn(mockMRSPatient);
        when(mockFacilityService.getFacility(ancvo.getFacilityId())).thenReturn(mockFacility);
        when(mockFacility.mrsFacility()).thenReturn(mockMRSFacility);
        when(mockMRSUser.getPerson()).thenReturn(mockMRSPerson);

        ancService.enroll(ancvo);

        ArgumentCaptor<MRSEncounter> captor = ArgumentCaptor.forClass(MRSEncounter.class);
        verify(mockAllEncounters).save(captor.capture());
        MRSEncounter actualEncounter = captor.getValue();
        assertEquals(mockMRSPatient, actualEncounter.getPatient());
        assertEquals(mockMRSUser, actualEncounter.getCreator());
        assertEquals(mockMRSFacility, actualEncounter.getFacility());
        assertEquals(mockMRSPerson, actualEncounter.getProvider());
        assertEquals(6, actualEncounter.getObservations().size());
        final HashSet<MRSObservation> expectedObservations = new HashSet<MRSObservation>() {{
            add(new MRSObservation<Integer>(observationDate, Constants.CONCEPT_GRAVIDA, ancvo.getGravida()));
            add(new MRSObservation<Double>(observationDate, Constants.CONCEPT_HEIGHT, ancvo.getHeight()));
            add(new MRSObservation<Integer>(observationDate, Constants.CONCEPT_PARITY, ancvo.getParity()));
            add(new MRSObservation<Date>(observationDate, Constants.CONCEPT_EDD, ancvo.getEstimatedDateOfDelivery()));
            add(new MRSObservation<Boolean>(observationDate, Constants.CONCEPT_CONFINEMENT_CONFIRMED, ancvo.getDeliveryDateConfirmed()));
            add(new MRSObservation<String>(observationDate, Constants.CONCEPT_ANC_REG_NUM, ancvo.getSerialNumber()));
        }};

        assertReflectionEquals(actualEncounter.getObservations(), expectedObservations, ReflectionComparatorMode.LENIENT_DATES, ReflectionComparatorMode.LENIENT_ORDER);
    }

    @Test
    public void shouldEnrollANCWithMobileMidwife() {

        ancService = spy(ancService);
        MRSEncounter mrsEncounter = mock(MRSEncounter.class);
        doReturn(mrsEncounter).when(ancService).enroll(Matchers.<ANCVO>any());

        MobileMidwifeEnrollment mobileMidwifeEnrollment = new MobileMidwifeEnrollment();
        MRSEncounter actualEncounter = ancService.enrollWithMobileMidwife(mock(ANCVO.class), mobileMidwifeEnrollment);

        assertSame(mrsEncounter, actualEncounter);
        verify(mockMockMidwifeService).createOrUpdateEnrollment(mobileMidwifeEnrollment);
    }

    private ANCVO createTestANCVO(String ipt, Date iptDate, String tt, Date ttDate, RegistrationToday registrationToday, Date registrationDate) {
        return new ANCVO("12345", "3434343", "1213343", registrationDate, registrationToday, "2321322", new Date(),
                12.34, 12, 34, true, true, new ArrayList<ANCCareHistory>(), ipt, tt, iptDate, ttDate);
    }

}
