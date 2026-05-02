package HospitalOpdQueueSys;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class DoctorService {

    private ReceptionistService receptionistService;

    public DoctorService(ReceptionistService receptionistService) {
        this.receptionistService = receptionistService;
    }

    // ── 1. VIEW MY QUEUE ─────────────────────────────────
    public void viewMyQueue(String doctorId) {
        Doctor doctor = receptionistService.findDoctor(doctorId);
        if (doctor == null) {
            System.out.println("  ERROR: Doctor not found."); return;
        }
        System.out.println("\n  ══════════════════════════════════════");
        System.out.println("    DR." + doctor.getName().toUpperCase() + " — QUEUE");
        System.out.println("  ══════════════════════════════════════");

        List<Patient> mine = new ArrayList<>();
        for (Patient p : receptionistService.getAllPatients()) {
            if (doctorId.equals(p.getAssignedDoctorId())
                    && (p.getStatus() == Patient.Status.WAITING
                    || p.getStatus() == Patient.Status.IN_CONSULTATION))
                mine.add(p);
        }

        if (mine.isEmpty()) {
            System.out.println("  No patients in your queue.");
        } else {
            mine.sort((p1, p2) -> {
                if (p2.getSeverityScore() != p1.getSeverityScore())
                    return p2.getSeverityScore() - p1.getSeverityScore();
                return p1.getArrivalTime().compareTo(p2.getArrivalTime());
            });
            int pos = 1;
            for (Patient p : mine)
                System.out.println("  " + pos++ + ". " + p);
        }
        System.out.println("  ══════════════════════════════════════");
    }

    // ── 2. START CONSULTATION ────────────────────────────
    public Patient startConsultation(String doctorId) {
        Doctor doctor = receptionistService.findDoctor(doctorId);
        if (doctor == null) {
            System.out.println("  ERROR: Doctor not found."); return null;
        }

        List<Patient> waiting = new ArrayList<>();
        for (Patient p : receptionistService.getAllPatients()) {
            if (doctorId.equals(p.getAssignedDoctorId())
                    && p.getStatus() == Patient.Status.WAITING)
                waiting.add(p);
        }

        if (waiting.isEmpty()) {
            System.out.println("  No patients waiting."); return null;
        }

        waiting.sort((p1, p2) -> {
            if (p2.getSeverityScore() != p1.getSeverityScore())
                return p2.getSeverityScore() - p1.getSeverityScore();
            return p1.getArrivalTime().compareTo(p2.getArrivalTime());
        });

        Patient next = waiting.get(0);
        next.setStatus(Patient.Status.IN_CONSULTATION);
        System.out.println("  Consultation started:");
        System.out.println("  Patient : " + next.getName() + " (Token #" + next.getTokenNumber() + ")");
        System.out.println("  Severity: " + next.getSeverityScore());
        return next;
    }

    // ── 3. WRITE PRESCRIPTION ────────────────────────────
    public Prescription writePrescription(String patientId,
                                          String doctorId, String diagnosis, String... medicines) {
        Patient patient = receptionistService.findPatient(patientId);
        Doctor  doctor  = receptionistService.findDoctor(doctorId);

        if (patient == null) {
            System.out.println("  ERROR: Patient not found."); return null;
        }
        if (doctor == null) {
            System.out.println("  ERROR: Doctor not found."); return null;
        }
        if (patient.getStatus() != Patient.Status.IN_CONSULTATION) {
            System.out.println("  ERROR: Patient not in consultation."); return null;
        }

        String rxId = "RX" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        Prescription rx = new Prescription(rxId, patientId, doctorId, diagnosis, Arrays.asList(medicines));
        patient.addPrescription(rx);
        System.out.println("  Prescription written:" + rx);
        return rx;
    }

    // ── 4. COMPLETE CONSULTATION ─────────────────────────
    public void completeConsultation(String patientId, String doctorId) {
        Patient patient = receptionistService.findPatient(patientId);
        Doctor  doctor  = receptionistService.findDoctor(doctorId);

        if (patient == null) {
            System.out.println("  ERROR: Patient not found."); return;
        }
        if (doctor == null) {
            System.out.println("  ERROR: Doctor not found."); return;
        }
        if (patient.getStatus() != Patient.Status.IN_CONSULTATION) {
            System.out.println("  ERROR: Not in consultation."); return;
        }

        patient.setStatus(Patient.Status.DONE);
        doctor.releasePatient(patientId);
        receptionistService.getQueueEngine().removePatient(patientId);

        for (Token t : receptionistService.getAllTokens()) {
            if (t.getPatientId().equals(patientId)
                    && t.getStatus() == Token.TokenStatus.ACTIVE) {
                t.setStatus(Token.TokenStatus.COMPLETED);
                break;
            }
        }
        System.out.println("  Consultation complete for: " + patient.getName());
    }

    // ── 5. VIEW SCHEDULE ─────────────────────────────────
    public void viewMySchedule(String doctorId) {
        Doctor doctor = receptionistService.findDoctor(doctorId);
        if (doctor == null) {
            System.out.println("  ERROR: Doctor not found."); return;
        }
        System.out.println("\n  ══════════════════════════════════════");
        System.out.println("    DR." + doctor.getName() + " — SCHEDULE");
        System.out.println("  ══════════════════════════════════════");
        System.out.println("  Specialization : " + doctor.getSpecialization());
        System.out.println("  Room           : " + doctor.getRoomNumber());
        System.out.println("  Load           : " + doctor.getCurrentLoad() + "/" + doctor.getMaxLoad());

        boolean any = false;
        for (Patient p : receptionistService.getAllPatients()) {
            if (doctorId.equals(p.getAssignedDoctorId())) {
                System.out.println("  " + p); any = true;
            }
        }
        if (!any) System.out.println("  No patients today.");
        System.out.println("  ══════════════════════════════════════");
    }
}