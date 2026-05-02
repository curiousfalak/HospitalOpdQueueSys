package HospitalOpdQueueSys;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Patient {

    public enum Status {
        WAITING,
        IN_CONSULTATION,
        DONE,
        NO_SHOW
    }

    private String patientId;
    private String name;
    private int age;
    private String phone;
    private String email;
    private int severityScore;
    private String bloodGroup;
    private Status status;
    private String assignedDoctorId;
    private int tokenNumber;
    private LocalDateTime arrivalTime;
    private List<Prescription> prescriptionHistory;

    public Patient(String patientId, String name, int age,
                   String phone, String email,
                   int severityScore, String bloodGroup) {
        this.patientId           = patientId;
        this.name                = name;
        this.age                 = age;
        this.phone               = phone;
        this.email               = email;
        this.severityScore       = severityScore;
        this.bloodGroup          = bloodGroup;
        this.status              = Status.WAITING;
        this.arrivalTime         = LocalDateTime.now();
        this.prescriptionHistory = new ArrayList<>();
        this.tokenNumber         = -1;
    }

    public void addPrescription(Prescription p) {
        prescriptionHistory.add(p);
    }

    public String getPatientId()                 { return patientId; }
    public String getName()                      { return name; }
    public int getAge()                          { return age; }
    public String getPhone()                     { return phone; }
    public String getEmail()                     { return email; }
    public int getSeverityScore()                { return severityScore; }
    public void setSeverityScore(int s)          { this.severityScore = s; }
    public String getBloodGroup()                { return bloodGroup; }
    public Status getStatus()                    { return status; }
    public void setStatus(Status status)         { this.status = status; }
    public String getAssignedDoctorId()          { return assignedDoctorId; }
    public void setAssignedDoctorId(String d)    { this.assignedDoctorId = d; }
    public int getTokenNumber()                  { return tokenNumber; }
    public void setTokenNumber(int t)            { this.tokenNumber = t; }
    public LocalDateTime getArrivalTime()        { return arrivalTime; }
    public List<Prescription> getPrescriptions() { return prescriptionHistory; }

    @Override
    public String toString() {
        return String.format(
                "Token#%-4d | %-20s | Age:%-3d | Severity:%d | Status:%-15s | Doctor:%s",
                tokenNumber, name, age, severityScore, status,
                assignedDoctorId == null ? "Not Assigned" : assignedDoctorId
        );
    }
}