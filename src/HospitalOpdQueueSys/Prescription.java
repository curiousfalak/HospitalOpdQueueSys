package HospitalOpdQueueSys;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class Prescription {

    private String prescriptionId;
    private String patientId;
    private String doctorId;
    private String diagnosis;
    private List<String> medicines;
    private LocalDate issuedDate;
    private LocalDate expiryDate;

    public Prescription(String prescriptionId, String patientId,
                        String doctorId, String diagnosis,
                        List<String> medicines) {
        this.prescriptionId = prescriptionId;
        this.patientId      = patientId;
        this.doctorId       = doctorId;
        this.diagnosis      = diagnosis;
        this.medicines      = medicines;
        this.issuedDate     = LocalDate.now();
        this.expiryDate     = issuedDate.plusDays(30);
    }

    public boolean isExpired() {
        return LocalDate.now().isAfter(expiryDate);
    }

    public long daysUntilExpiry() {
        return ChronoUnit.DAYS.between(LocalDate.now(), expiryDate);
    }

    public String getPrescriptionId()  { return prescriptionId; }
    public String getPatientId()       { return patientId; }
    public String getDoctorId()        { return doctorId; }
    public String getDiagnosis()       { return diagnosis; }
    public List<String> getMedicines() { return medicines; }
    public LocalDate getIssuedDate()   { return issuedDate; }
    public LocalDate getExpiryDate()   { return expiryDate; }

    @Override
    public String toString() {
        return String.format(
                "\n  Prescription ID : %s"  +
                        "\n  Diagnosis       : %s"  +
                        "\n  Medicines       : %s"  +
                        "\n  Issued          : %s"  +
                        "\n  Expires         : %s"  +
                        "\n  Status          : %s",
                prescriptionId, diagnosis, medicines,
                issuedDate, expiryDate,
                isExpired()
                        ? "EXPIRED"
                        : daysUntilExpiry() <= 3
                        ? "EXPIRING SOON (" + daysUntilExpiry() + " days)"
                        : "VALID (" + daysUntilExpiry() + " days left)"
        );
    }
}