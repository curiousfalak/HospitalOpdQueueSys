package HospitalOpdQueueSys;

import java.util.ArrayList;
import java.util.List;

public class Doctor {

    private String doctorId;
    private String name;
    private String specialization;
    private int currentLoad;
    private int maxLoad;
    private boolean isAvailable;
    private String roomNumber;
    private List<String> assignedPatientIds;

    public Doctor(String doctorId, String name,
                  String specialization, String roomNumber) {
        this.doctorId           = doctorId;
        this.name               = name;
        this.specialization     = specialization;
        this.roomNumber         = roomNumber;
        this.maxLoad            = 20;
        this.currentLoad        = 0;
        this.isAvailable        = true;
        this.assignedPatientIds = new ArrayList<>();
    }

    public boolean canTakePatient() {
        return isAvailable && currentLoad < maxLoad;
    }

    public void assignPatient(String patientId) {
        assignedPatientIds.add(patientId);
        currentLoad++;
        if (currentLoad >= maxLoad) isAvailable = false;
    }

    public void releasePatient(String patientId) {
        assignedPatientIds.remove(patientId);
        currentLoad--;
        if (currentLoad < maxLoad) isAvailable = true;
    }

    public String getDoctorId()                 { return doctorId; }
    public String getName()                     { return name; }
    public String getSpecialization()           { return specialization; }
    public int getCurrentLoad()                 { return currentLoad; }
    public int getMaxLoad()                     { return maxLoad; }
    public boolean isAvailable()                { return isAvailable; }
    public void setAvailable(boolean a)         { this.isAvailable = a; }
    public String getRoomNumber()               { return roomNumber; }
    public List<String> getAssignedPatientIds() { return assignedPatientIds; }

    @Override
    public String toString() {
        return String.format(
                "ID:%-6s | Dr.%-20s | %-15s | Room:%-4s | Load:%2d/%-2d | %s",
                doctorId, name, specialization, roomNumber,
                currentLoad, maxLoad,
                isAvailable ? "AVAILABLE" : "FULL"
        );
    }
}