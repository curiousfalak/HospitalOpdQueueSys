package HospitalOpdQueueSys;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class PriorityQueueEngine {

    private PriorityQueue<Patient> queue;

    public PriorityQueueEngine() {
        this.queue = new PriorityQueue<>((p1, p2) -> {
            if (p2.getSeverityScore() != p1.getSeverityScore()) {
                return p2.getSeverityScore() - p1.getSeverityScore();
            }
            return p1.getArrivalTime().compareTo(p2.getArrivalTime());
        });
    }

    public void addPatient(Patient p) {
        queue.offer(p);
    }

    public Patient getNextPatient() {
        return queue.poll();
    }

    public Patient peekNextPatient() {
        return queue.peek();
    }

    public boolean isPatientInQueue(String patientId) {
        for (Patient p : queue) {
            if (p.getPatientId().equals(patientId)) return true;
        }
        return false;
    }

    public void markNoShow(Patient p) {
        queue.remove(p);
        p.setSeverityScore(1);
        p.setStatus(Patient.Status.NO_SHOW);
        queue.offer(p);
        System.out.println("  Patient " + p.getName()
                + " marked NO-SHOW and moved to back of queue.");
    }

    public boolean removePatient(String patientId) {
        return queue.removeIf(p -> p.getPatientId().equals(patientId));
    }

    public int getPosition(String patientId) {
        List<Patient> snapshot = getSortedSnapshot();
        for (int i = 0; i < snapshot.size(); i++) {
            if (snapshot.get(i).getPatientId().equals(patientId))
                return i + 1;
        }
        return -1;
    }

    public int getEstimatedWaitTime(String patientId) {
        int pos = getPosition(patientId);
        return pos == -1 ? -1 : (pos - 1) * 5;
    }

    public void displayQueue() {
        if (queue.isEmpty()) {
            System.out.println("  Queue is empty."); return;
        }
        List<Patient> snapshot = getSortedSnapshot();
        int pos = 1;
        for (Patient p : snapshot) {
            System.out.println("  " + pos + ". " + p);
            pos++;
        }
    }

    private List<Patient> getSortedSnapshot() {
        List<Patient> snapshot = new ArrayList<>(queue);
        snapshot.sort((p1, p2) -> {
            if (p2.getSeverityScore() != p1.getSeverityScore())
                return p2.getSeverityScore() - p1.getSeverityScore();
            return p1.getArrivalTime().compareTo(p2.getArrivalTime());
        });
        return snapshot;
    }

    public int getSize()     { return queue.size(); }
    public boolean isEmpty() { return queue.isEmpty(); }
}