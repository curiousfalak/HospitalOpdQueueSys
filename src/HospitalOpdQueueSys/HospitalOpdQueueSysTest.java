package HospitalOpdQueueSys;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class HospitalOpdQueueSysTest {


/**
 * JUnit 5 Test Suite for Hospital OPD Queue System
 * Tests all major classes and business logic
 */



    private ReceptionistService receptionistService;
    private DoctorService doctorService;
    private PatientService patientService;

    // ── Setup before each test ────────────────────────────
    @BeforeEach
    void setUp() {
        receptionistService = new ReceptionistService();
        doctorService       = new DoctorService(receptionistService);
        patientService      = new PatientService(receptionistService);
    }

    // ══════════════════════════════════════════════════════
    //  1. PATIENT REGISTRATION TESTS
    // ══════════════════════════════════════════════════════

    @Test
    @Order(1)
    @DisplayName("Register patient with valid data")
    void testRegisterPatient_Valid() {
        Patient p = receptionistService.registerPatient(
                "Amit Kumar", 35, "9876543210", "amit@gmail.com", 3, "B+");
        assertNotNull(p);
        assertEquals("Amit Kumar", p.getName());
        assertEquals(35, p.getAge());
        assertEquals("P001", p.getPatientId());
        assertEquals(Patient.Status.WAITING, p.getStatus());
    }

    @Test
    @Order(2)
    @DisplayName("Register patient with invalid phone returns null")
    void testRegisterPatient_InvalidPhone() {
        Patient p = receptionistService.registerPatient(
                "Bad Phone", 25, "12345", "test@gmail.com", 2, "A+");
        assertNull(p, "Should return null for invalid phone");
    }

    @Test
    @Order(3)
    @DisplayName("Register patient with invalid email returns null")
    void testRegisterPatient_InvalidEmail() {
        Patient p = receptionistService.registerPatient(
                "Bad Email", 25, "9876543210", "notanemail", 2, "A+");
        assertNull(p, "Should return null for invalid email");
    }

    @Test
    @Order(4)
    @DisplayName("Register patient with severity out of range returns null")
    void testRegisterPatient_InvalidSeverity() {
        Patient p = receptionistService.registerPatient(
                "Bad Severity", 25, "9876543210", "test@gmail.com", 9, "A+");
        assertNull(p, "Should return null for severity > 5");
    }

    @Test
    @Order(5)
    @DisplayName("Multiple patients get sequential IDs")
    void testRegisterPatient_SequentialIds() {
        Patient p1 = receptionistService.registerPatient("Alice", 20, "9000000001", "a@gmail.com", 1, "A+");
        Patient p2 = receptionistService.registerPatient("Bob",   22, "9000000002", "b@gmail.com", 2, "B+");
        Patient p3 = receptionistService.registerPatient("Carol", 24, "9000000003", "c@gmail.com", 3, "O+");
        assertEquals("P001", p1.getPatientId());
        assertEquals("P002", p2.getPatientId());
        assertEquals("P003", p3.getPatientId());
    }

    // ══════════════════════════════════════════════════════
    //  2. TOKEN ASSIGNMENT TESTS
    // ══════════════════════════════════════════════════════

    @Test
    @Order(6)
    @DisplayName("Assign token to valid patient")
    void testAssignToken_Valid() {
        receptionistService.registerPatient("Amit", 35, "9876543210", "amit@gmail.com", 3, "B+");
        Token t = receptionistService.assignToken("P001", "General");
        assertNotNull(t);
        assertEquals(1, t.getTokenNumber());
        assertEquals(Token.TokenStatus.ACTIVE, t.getStatus());
    }

    @Test
    @Order(7)
    @DisplayName("Cannot assign duplicate token to same patient")
    void testAssignToken_Duplicate() {
        receptionistService.registerPatient("Amit", 35, "9876543210", "amit@gmail.com", 3, "B+");
        receptionistService.assignToken("P001", "General");
        Token t2 = receptionistService.assignToken("P001", "General");
        assertNull(t2, "Should not allow duplicate token");
    }

    @Test
    @Order(8)
    @DisplayName("Assign token to non-existent patient returns null")
    void testAssignToken_PatientNotFound() {
        Token t = receptionistService.assignToken("P999", "General");
        assertNull(t, "Should return null for unknown patient");
    }

    @Test
    @Order(9)
    @DisplayName("Assign token to unavailable specialization returns null")
    void testAssignToken_NoDoctor() {
        receptionistService.registerPatient("Amit", 35, "9876543210", "amit@gmail.com", 3, "B+");
        Token t = receptionistService.assignToken("P001", "Dermatology");
        assertNull(t, "Should return null when no doctor available");
    }

    @Test
    @Order(10)
    @DisplayName("Patient is added to queue after token assignment")
    void testAssignToken_AddsToQueue() {
        receptionistService.registerPatient("Amit", 35, "9876543210", "amit@gmail.com", 3, "B+");
        receptionistService.assignToken("P001", "General");
        assertTrue(receptionistService.getQueueEngine().isPatientInQueue("P001"));
    }

    // ══════════════════════════════════════════════════════
    //  3. PRIORITY QUEUE TESTS
    // ══════════════════════════════════════════════════════

    @Test
    @Order(11)
    @DisplayName("Higher severity patient gets higher priority")
    void testQueue_SeverityPriority() {
        receptionistService.registerPatient("LowSev",  30, "9000000001", "a@gmail.com", 1, "A+");
        receptionistService.registerPatient("HighSev", 30, "9000000002", "b@gmail.com", 5, "B+");
        receptionistService.assignToken("P001", "General");
        receptionistService.assignToken("P002", "General");

        // HighSev (severity 5) should be position 1
        assertEquals(1, receptionistService.getQueueEngine().getPosition("P002"));
        assertEquals(2, receptionistService.getQueueEngine().getPosition("P001"));
    }

    @Test
    @Order(12)
    @DisplayName("Queue size increases with each patient added")
    void testQueue_Size() {
        receptionistService.registerPatient("A", 20, "9000000001", "a@gmail.com", 1, "A+");
        receptionistService.registerPatient("B", 21, "9000000002", "b@gmail.com", 2, "B+");
        receptionistService.assignToken("P001", "General");
        assertEquals(1, receptionistService.getQueueEngine().getSize());
        receptionistService.assignToken("P002", "General");
        assertEquals(2, receptionistService.getQueueEngine().getSize());
    }

    @Test
    @Order(13)
    @DisplayName("Estimated wait time is 0 for first patient")
    void testQueue_WaitTimeFirstPatient() {
        receptionistService.registerPatient("Amit", 35, "9876543210", "amit@gmail.com", 3, "B+");
        receptionistService.assignToken("P001", "General");
        assertEquals(0, receptionistService.getQueueEngine().getEstimatedWaitTime("P001"));
    }

    @Test
    @Order(14)
    @DisplayName("Estimated wait time is 5 mins per position ahead")
    void testQueue_WaitTimeSecondPatient() {
        receptionistService.registerPatient("A", 20, "9000000001", "a@gmail.com", 5, "A+");
        receptionistService.registerPatient("B", 21, "9000000002", "b@gmail.com", 1, "B+");
        receptionistService.assignToken("P001", "General");
        receptionistService.assignToken("P002", "General");
        // P002 is position 2, wait = (2-1)*5 = 5 mins
        assertEquals(5, receptionistService.getQueueEngine().getEstimatedWaitTime("P002"));
    }

    @Test
    @Order(15)
    @DisplayName("Remove patient from queue works correctly")
    void testQueue_RemovePatient() {
        receptionistService.registerPatient("Amit", 35, "9876543210", "amit@gmail.com", 3, "B+");
        receptionistService.assignToken("P001", "General");
        assertTrue(receptionistService.getQueueEngine().isPatientInQueue("P001"));
        receptionistService.getQueueEngine().removePatient("P001");
        assertFalse(receptionistService.getQueueEngine().isPatientInQueue("P001"));
    }

    // ══════════════════════════════════════════════════════
    //  4. DOCTOR TESTS
    // ══════════════════════════════════════════════════════

    @Test
    @Order(16)
    @DisplayName("Doctor is available by default")
    void testDoctor_AvailableByDefault() {
        Doctor d = new Doctor("D001", "Sharma", "General", "101");
        assertTrue(d.isAvailable());
        assertTrue(d.canTakePatient());
        assertEquals(0, d.getCurrentLoad());
    }

    @Test
    @Order(17)
    @DisplayName("Doctor load increases when patient assigned")
    void testDoctor_AssignPatient() {
        Doctor d = new Doctor("D001", "Sharma", "General", "101");
        d.assignPatient("P001");
        assertEquals(1, d.getCurrentLoad());
        assertTrue(d.isAvailable()); // still available (max is 20)
    }

    @Test
    @Order(18)
    @DisplayName("Doctor load decreases when patient released")
    void testDoctor_ReleasePatient() {
        Doctor d = new Doctor("D001", "Sharma", "General", "101");
        d.assignPatient("P001");
        d.releasePatient("P001");
        assertEquals(0, d.getCurrentLoad());
        assertTrue(d.isAvailable());
    }

    @Test
    @Order(19)
    @DisplayName("Doctor becomes unavailable at max load")
    void testDoctor_MaxLoad() {
        Doctor d = new Doctor("D001", "Sharma", "General", "101");
        for (int i = 1; i <= 20; i++) {
            d.assignPatient("P" + String.format("%03d", i));
        }
        assertFalse(d.isAvailable(), "Doctor should be FULL at 20 patients");
        assertFalse(d.canTakePatient());
    }

    @Test
    @Order(20)
    @DisplayName("Find doctor by ID works correctly")
    void testDoctor_FindById() {
        Doctor d = receptionistService.findDoctor("D001");
        assertNotNull(d);
        assertEquals("Sharma", d.getName());
        assertEquals("General", d.getSpecialization());
    }

    // ══════════════════════════════════════════════════════
    //  5. CONSULTATION TESTS
    // ══════════════════════════════════════════════════════

    @Test
    @Order(21)
    @DisplayName("Start consultation changes patient status to IN_CONSULTATION")
    void testConsultation_Start() {
        receptionistService.registerPatient("Amit", 35, "9876543210", "amit@gmail.com", 3, "B+");
        receptionistService.assignToken("P001", "General");
        doctorService.startConsultation("D001");
        Patient p = receptionistService.findPatient("P001");
        assertEquals(Patient.Status.IN_CONSULTATION, p.getStatus());
    }

    @Test
    @Order(22)
    @DisplayName("Complete consultation changes patient status to DONE")
    void testConsultation_Complete() {
        receptionistService.registerPatient("Amit", 35, "9876543210", "amit@gmail.com", 3, "B+");
        receptionistService.assignToken("P001", "General");
        doctorService.startConsultation("D001");
        doctorService.completeConsultation("P001", "D001");
        Patient p = receptionistService.findPatient("P001");
        assertEquals(Patient.Status.DONE, p.getStatus());
    }

    @Test
    @Order(23)
    @DisplayName("Complete consultation removes patient from queue")
    void testConsultation_RemovesFromQueue() {
        receptionistService.registerPatient("Amit", 35, "9876543210", "amit@gmail.com", 3, "B+");
        receptionistService.assignToken("P001", "General");
        doctorService.startConsultation("D001");
        doctorService.completeConsultation("P001", "D001");
        assertFalse(receptionistService.getQueueEngine().isPatientInQueue("P001"));
    }

    @Test
    @Order(24)
    @DisplayName("Complete consultation reduces doctor load")
    void testConsultation_ReducesDoctorLoad() {
        receptionistService.registerPatient("Amit", 35, "9876543210", "amit@gmail.com", 3, "B+");
        receptionistService.assignToken("P001", "General");
        Doctor d = receptionistService.findDoctor("D001");
        int loadBefore = d.getCurrentLoad();
        doctorService.startConsultation("D001");
        doctorService.completeConsultation("P001", "D001");
        assertEquals(loadBefore - 1, d.getCurrentLoad());
    }

    // ══════════════════════════════════════════════════════
    //  6. PRESCRIPTION TESTS
    // ══════════════════════════════════════════════════════

    @Test
    @Order(25)
    @DisplayName("Write prescription during consultation succeeds")
    void testPrescription_WriteDuringConsultation() {
        receptionistService.registerPatient("Amit", 35, "9876543210", "amit@gmail.com", 3, "B+");
        receptionistService.assignToken("P001", "General");
        doctorService.startConsultation("D001");
        Prescription rx = doctorService.writePrescription(
                "P001", "D001", "Fever", "Paracetamol", "Ibuprofen");
        assertNotNull(rx);
        assertEquals("Fever", rx.getDiagnosis());
        assertEquals(2, rx.getMedicines().size());
    }

    @Test
    @Order(26)
    @DisplayName("Cannot write prescription if patient not in consultation")
    void testPrescription_NotInConsultation() {
        receptionistService.registerPatient("Amit", 35, "9876543210", "amit@gmail.com", 3, "B+");
        receptionistService.assignToken("P001", "General");
        // Do NOT start consultation
        Prescription rx = doctorService.writePrescription(
                "P001", "D001", "Fever", "Paracetamol");
        assertNull(rx, "Should not write prescription without consultation");
    }

    @Test
    @Order(27)
    @DisplayName("Prescription is added to patient history")
    void testPrescription_AddedToHistory() {
        receptionistService.registerPatient("Amit", 35, "9876543210", "amit@gmail.com", 3, "B+");
        receptionistService.assignToken("P001", "General");
        doctorService.startConsultation("D001");
        doctorService.writePrescription("P001", "D001", "Cough", "Syrup");
        Patient p = receptionistService.findPatient("P001");
        assertEquals(1, p.getPrescriptions().size());
        assertEquals("Cough", p.getPrescriptions().get(0).getDiagnosis());
    }

    @Test
    @Order(28)
    @DisplayName("Prescription is valid (not expired) when created")
    void testPrescription_NotExpiredOnCreation() {
        receptionistService.registerPatient("Amit", 35, "9876543210", "amit@gmail.com", 3, "B+");
        receptionistService.assignToken("P001", "General");
        doctorService.startConsultation("D001");
        Prescription rx = doctorService.writePrescription(
                "P001", "D001", "Fever", "Paracetamol");
        assertFalse(rx.isExpired());
        assertTrue(rx.daysUntilExpiry() > 0);
    }

    // ══════════════════════════════════════════════════════
    //  7. NO-SHOW TESTS
    // ══════════════════════════════════════════════════════

    @Test
    @Order(29)
    @DisplayName("Mark no-show reduces patient severity to 1")
    void testNoShow_ReducesSeverity() {
        receptionistService.registerPatient("Amit", 35, "9876543210", "amit@gmail.com", 5, "B+");
        receptionistService.assignToken("P001", "General");
        receptionistService.markNoShow("P001");
        Patient p = receptionistService.findPatient("P001");
        assertEquals(1, p.getSeverityScore());
        assertEquals(Patient.Status.NO_SHOW, p.getStatus());
    }

    @Test
    @Order(30)
    @DisplayName("No-show patient moves to back of queue")
    void testNoShow_MovesToBack() {
        receptionistService.registerPatient("HighSev", 30, "9000000001", "a@gmail.com", 5, "A+");
        receptionistService.registerPatient("LowSev",  30, "9000000002", "b@gmail.com", 1, "B+");
        receptionistService.assignToken("P001", "General");
        receptionistService.assignToken("P002", "General");
        // P001 was first (severity 5), mark no-show
        receptionistService.markNoShow("P001");
        // Now P002 should be position 1, P001 at back
        assertEquals(1, receptionistService.getQueueEngine().getPosition("P002"));
        assertEquals(2, receptionistService.getQueueEngine().getPosition("P001"));
    }

    // ══════════════════════════════════════════════════════
    //  8. REMOVE PATIENT TESTS
    // ══════════════════════════════════════════════════════

    @Test
    @Order(31)
    @DisplayName("Remove patient works for waiting patient")
    void testRemovePatient_Valid() {
        receptionistService.registerPatient("Amit", 35, "9876543210", "amit@gmail.com", 3, "B+");
        boolean result = receptionistService.removePatient("P001");
        assertTrue(result);
        assertNull(receptionistService.findPatient("P001"));
    }

    @Test
    @Order(32)
    @DisplayName("Cannot remove patient in consultation")
    void testRemovePatient_InConsultation() {
        receptionistService.registerPatient("Amit", 35, "9876543210", "amit@gmail.com", 3, "B+");
        receptionistService.assignToken("P001", "General");
        doctorService.startConsultation("D001");
        boolean result = receptionistService.removePatient("P001");
        assertFalse(result, "Should not remove patient in consultation");
    }

    @Test
    @Order(33)
    @DisplayName("Remove non-existent patient returns false")
    void testRemovePatient_NotFound() {
        boolean result = receptionistService.removePatient("P999");
        assertFalse(result);
    }

    // ══════════════════════════════════════════════════════
    //  9. CANCEL TOKEN TESTS
    // ══════════════════════════════════════════════════════

    @Test
    @Order(34)
    @DisplayName("Cancel token removes patient from queue")
    void testCancelToken_RemovesFromQueue() {
        receptionistService.registerPatient("Amit", 35, "9876543210", "amit@gmail.com", 3, "B+");
        receptionistService.assignToken("P001", "General");
        patientService.cancelToken("P001");
        assertFalse(receptionistService.getQueueEngine().isPatientInQueue("P001"));
    }

    @Test
    @Order(35)
    @DisplayName("Cancel token sets patient status to DONE")
    void testCancelToken_StatusDone() {
        receptionistService.registerPatient("Amit", 35, "9876543210", "amit@gmail.com", 3, "B+");
        receptionistService.assignToken("P001", "General");
        patientService.cancelToken("P001");
        Patient p = receptionistService.findPatient("P001");
        assertEquals(Patient.Status.DONE, p.getStatus());
        assertNull(p.getAssignedDoctorId());
    }

    @Test
    @Order(36)
    @DisplayName("Cannot cancel token during consultation")
    void testCancelToken_DuringConsultation() {
        receptionistService.registerPatient("Amit", 35, "9876543210", "amit@gmail.com", 3, "B+");
        receptionistService.assignToken("P001", "General");
        doctorService.startConsultation("D001");
        // Should still be IN_CONSULTATION after failed cancel
        patientService.cancelToken("P001");
        Patient p = receptionistService.findPatient("P001");
        assertEquals(Patient.Status.IN_CONSULTATION, p.getStatus());
    }

    // ══════════════════════════════════════════════════════
    //  10. TOKEN STATUS TESTS
    // ══════════════════════════════════════════════════════

    @Test
    @Order(37)
    @DisplayName("Token is ACTIVE after assignment")
    void testToken_ActiveAfterAssignment() {
        receptionistService.registerPatient("Amit", 35, "9876543210", "amit@gmail.com", 3, "B+");
        receptionistService.assignToken("P001", "General");
        Token t = receptionistService.getAllTokens().get(0);
        assertEquals(Token.TokenStatus.ACTIVE, t.getStatus());
    }

    @Test
    @Order(38)
    @DisplayName("Token is COMPLETED after consultation ends")
    void testToken_CompletedAfterConsultation() {
        receptionistService.registerPatient("Amit", 35, "9876543210", "amit@gmail.com", 3, "B+");
        receptionistService.assignToken("P001", "General");
        doctorService.startConsultation("D001");
        doctorService.completeConsultation("P001", "D001");
        Token t = receptionistService.getAllTokens().get(0);
        assertEquals(Token.TokenStatus.COMPLETED, t.getStatus());
    }

    @Test
    @Order(39)
    @DisplayName("Token is CANCELLED after patient cancels")
    void testToken_CancelledAfterCancel() {
        receptionistService.registerPatient("Amit", 35, "9876543210", "amit@gmail.com", 3, "B+");
        receptionistService.assignToken("P001", "General");
        patientService.cancelToken("P001");
        Token t = receptionistService.getAllTokens().get(0);
        assertEquals(Token.TokenStatus.CANCELLED, t.getStatus());
    }
}