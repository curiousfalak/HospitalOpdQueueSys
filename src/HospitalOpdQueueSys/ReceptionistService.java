package HospitalOpdQueueSys;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ReceptionistService {

    private List<Patient> allPatients;
    private List<Doctor> allDoctors;
    private List<Token> allTokens;
    private PriorityQueueEngine queueEngine;
    private int tokenCounter;

    public ReceptionistService() {
        this.allPatients  = new ArrayList<>();
        this.allDoctors   = new ArrayList<>();
        this.allTokens    = new ArrayList<>();
        this.queueEngine  = new PriorityQueueEngine();
        this.tokenCounter = 1;
        loadDefaultDoctors();
    }

    private void loadDefaultDoctors() {
        allDoctors.add(new Doctor("D001", "Sharma",     "General",     "101"));
        allDoctors.add(new Doctor("D002", "Mehta",      "Cardiology",  "102"));
        allDoctors.add(new Doctor("D003", "Kapoor",     "Orthopedics", "103"));
        allDoctors.add(new Doctor("D004", "Verma",      "Pediatrics",  "104"));
        allDoctors.add(new Doctor("D005", "Iyer",       "Neurology",   "105"));
        allDoctors.add(new Doctor("D006", "Chatterjee", "General",     "106"));
    }

    private boolean isValidPhone(String phone) {
        return phone != null && phone.matches("\\d{10}");
    }

    private boolean isValidEmail(String email) {
        if (email == null) return false;
        int at  = email.indexOf('@');
        int dot = email.lastIndexOf('.');
        return at > 0 && dot > at + 1 && (email.length() - dot - 1) >= 2;
    }

    private boolean isValidSeverity(int s) {
        return s >= 1 && s <= 5;
    }

    // ── 1. REGISTER PATIENT ──────────────────────────────
    public Patient registerPatient(String name, int age,
                                   String phone, String email,
                                   int severityScore, String bloodGroup) {
        if (!isValidPhone(phone)) {
            System.out.println("  ERROR: Phone must be 10 digits."); return null;
        }
        if (!isValidEmail(email)) {
            System.out.println("  ERROR: Invalid email."); return null;
        }
        if (!isValidSeverity(severityScore)) {
            System.out.println("  ERROR: Severity must be 1-5."); return null;
        }
        String id = "P" + String.format("%03d", allPatients.size() + 1);
        Patient p = new Patient(id, name, age, phone, email, severityScore, bloodGroup);
        allPatients.add(p);
        System.out.println("  SUCCESS: Registered " + name + " → ID: " + id);
        return p;
    }

    // ── 2. REMOVE PATIENT ────────────────────────────────
    public boolean removePatient(String patientId) {
        Patient p = findPatient(patientId);
        if (p == null) {
            System.out.println("  ERROR: Patient not found."); return false;
        }
        if (p.getStatus() == Patient.Status.IN_CONSULTATION) {
            System.out.println("  ERROR: Cannot remove — in consultation."); return false;
        }
        queueEngine.removePatient(patientId);
        allPatients.remove(p);
        System.out.println("  SUCCESS: Patient removed.");
        return true;
    }

    // ── 3. ASSIGN TOKEN ──────────────────────────────────
    public Token assignToken(String patientId, String specialization) {
        Patient patient = findPatient(patientId);
        if (patient == null) {
            System.out.println("  ERROR: Patient not found."); return null;
        }
        if (queueEngine.isPatientInQueue(patientId)) {
            System.out.println("  ERROR: Patient already has active token."); return null;
        }
        Doctor doctor = findAvailableDoctor(specialization);
        if (doctor == null) {
            System.out.println("  ERROR: No available doctor for " + specialization + "."); return null;
        }
        String tokenId = "T" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        Token token = new Token(tokenId, patientId, doctor.getDoctorId(), tokenCounter++);
        patient.setTokenNumber(token.getTokenNumber());
        patient.setAssignedDoctorId(doctor.getDoctorId());
        patient.setStatus(Patient.Status.WAITING);
        doctor.assignPatient(patientId);
        queueEngine.addPatient(patient);
        allTokens.add(token);
        System.out.println("  SUCCESS: Token #" + token.getTokenNumber()
                + " → " + patient.getName()
                + " → Dr." + doctor.getName()
                + " Room " + doctor.getRoomNumber());
        return token;
    }

    // ── 4. VIEW FULL QUEUE ───────────────────────────────
    public void viewFullQueue() {
        System.out.println("\n  ══════════════════════════════════════");
        System.out.println("           CURRENT OPD QUEUE");
        System.out.println("  ══════════════════════════════════════");
        System.out.println("  Waiting: " + queueEngine.getSize());
        queueEngine.displayQueue();
        System.out.println("  ══════════════════════════════════════");
    }

    // ── 5. VIEW ALL DOCTORS ──────────────────────────────
    public void viewAllDoctors() {
        System.out.println("\n  ══════════════════════════════════════");
        System.out.println("             ALL DOCTORS");
        System.out.println("  ══════════════════════════════════════");
        for (Doctor d : allDoctors)
            System.out.println("  " + d);
        System.out.println("  ══════════════════════════════════════");
    }

    // ── 6. VIEW ALL PATIENTS ─────────────────────────────
    public void viewAllPatients() {
        System.out.println("\n  ══════════════════════════════════════");
        System.out.println("             ALL PATIENTS");
        System.out.println("  ══════════════════════════════════════");
        if (allPatients.isEmpty())
            System.out.println("  No patients registered.");
        for (Patient p : allPatients)
            System.out.println("  " + p);
        System.out.println("  ══════════════════════════════════════");
    }

    // ── 7. MARK NO-SHOW ──────────────────────────────────
    public void markNoShow(String patientId) {
        Patient p = findPatient(patientId);
        if (p == null) {
            System.out.println("  ERROR: Patient not found."); return;
        }
        if (!queueEngine.isPatientInQueue(patientId)) {
            System.out.println("  ERROR: Patient not in queue."); return;
        }
        queueEngine.markNoShow(p);
    }

    // ── 8. EXPIRY ALERTS ─────────────────────────────────
    public void checkExpiryAlerts() {
        System.out.println("\n  ══════════════════════════════════════");
        System.out.println("        PRESCRIPTION EXPIRY ALERTS");
        System.out.println("  ══════════════════════════════════════");
        boolean any = false;
        for (Patient p : allPatients) {
            for (Prescription rx : p.getPrescriptions()) {
                if (rx.isExpired()) {
                    System.out.println("  [EXPIRED] " + p.getName() + " | " + rx.getPrescriptionId());
                    any = true;
                } else if (rx.daysUntilExpiry() <= 3) {
                    System.out.println("  [WARNING] " + p.getName()
                            + " expires in " + rx.daysUntilExpiry() + " day(s)");
                    any = true;
                }
            }
        }
        if (!any) System.out.println("  All prescriptions valid.");
        System.out.println("  ══════════════════════════════════════");
    }

    // ── Helpers ──────────────────────────────────────────
    public Patient findPatient(String patientId) {
        for (Patient p : allPatients)
            if (p.getPatientId().equalsIgnoreCase(patientId)) return p;
        return null;
    }

    public Doctor findDoctor(String doctorId) {
        for (Doctor d : allDoctors)
            if (d.getDoctorId().equalsIgnoreCase(doctorId)) return d;
        return null;
    }

    private Doctor findAvailableDoctor(String specialization) {
        for (Doctor d : allDoctors)
            if (d.getSpecialization().equalsIgnoreCase(specialization) && d.canTakePatient())
                return d;
        return null;
    }

    public PriorityQueueEngine getQueueEngine() { return queueEngine; }
    public List<Patient> getAllPatients()        { return allPatients; }
    public List<Doctor> getAllDoctors()          { return allDoctors; }
    public List<Token> getAllTokens()            { return allTokens; }
}