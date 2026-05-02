package HospitalOpdQueueSys;

public class PatientService {

    private ReceptionistService receptionistService;

    public PatientService(ReceptionistService receptionistService) {
        this.receptionistService = receptionistService;
    }

    // ── 1. VIEW MY TOKEN ─────────────────────────────────
    public void viewMyToken(String patientId) {
        Patient p = receptionistService.findPatient(patientId);
        if (p == null) {
            System.out.println("  ERROR: Patient not found."); return;
        }
        System.out.println("\n  ══════════════════════════════════════");
        System.out.println("           MY TOKEN STATUS");
        System.out.println("  ══════════════════════════════════════");
        System.out.println("  Name     : " + p.getName());
        System.out.println("  Token #  : " + p.getTokenNumber());
        System.out.println("  Status   : " + p.getStatus());
        System.out.println("  Doctor   : " + (p.getAssignedDoctorId() == null ? "Not assigned" : p.getAssignedDoctorId()));
        int wait = receptionistService.getQueueEngine().getEstimatedWaitTime(patientId);
        if (wait >= 0)
            System.out.println("  Est.Wait : ~" + wait + " mins");
        System.out.println("  ══════════════════════════════════════");
    }

    // ── 2. VIEW MY PRESCRIPTIONS ─────────────────────────
    public void viewMyPrescriptions(String patientId) {
        Patient p = receptionistService.findPatient(patientId);
        if (p == null) {
            System.out.println("  ERROR: Patient not found."); return;
        }
        System.out.println("\n  ══════════════════════════════════════");
        System.out.println("        MY PRESCRIPTION HISTORY");
        System.out.println("  ══════════════════════════════════════");
        if (p.getPrescriptions().isEmpty())
            System.out.println("  No prescriptions found.");
        for (Prescription rx : p.getPrescriptions())
            System.out.println(rx);
        System.out.println("  ══════════════════════════════════════");
    }

    // ── 3. VIEW WAIT TIME ────────────────────────────────
    public void viewEstimatedWaitTime(String patientId) {
        int pos  = receptionistService.getQueueEngine().getPosition(patientId);
        int wait = receptionistService.getQueueEngine().getEstimatedWaitTime(patientId);
        if (pos == -1)
            System.out.println("  You are not in the queue.");
        else {
            System.out.println("  Your position  : " + pos);
            System.out.println("  Estimated wait : ~" + wait + " mins");
        }
    }

    // ── 4. CANCEL TOKEN ──────────────────────────────────
    public void cancelToken(String patientId) {
        Patient p = receptionistService.findPatient(patientId);
        if (p == null) {
            System.out.println("  ERROR: Patient not found."); return;
        }
        if (p.getStatus() == Patient.Status.IN_CONSULTATION) {
            System.out.println("  ERROR: Cannot cancel during consultation."); return;
        }
        if (!receptionistService.getQueueEngine().isPatientInQueue(patientId)) {
            System.out.println("  No active token to cancel."); return;
        }

        receptionistService.getQueueEngine().removePatient(patientId);

        if (p.getAssignedDoctorId() != null) {
            Doctor d = receptionistService.findDoctor(p.getAssignedDoctorId());
            if (d != null) d.releasePatient(patientId);
        }

        for (Token t : receptionistService.getAllTokens()) {
            if (t.getPatientId().equals(patientId)
                    && t.getStatus() == Token.TokenStatus.ACTIVE) {
                t.setStatus(Token.TokenStatus.CANCELLED);
                break;
            }
        }

        p.setStatus(Patient.Status.DONE);
        p.setAssignedDoctorId(null);
        System.out.println("  Token cancelled for: " + p.getName());
    }
}