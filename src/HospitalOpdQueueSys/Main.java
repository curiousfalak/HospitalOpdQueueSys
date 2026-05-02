package HospitalOpdQueueSys;

import java.util.Scanner;

public class Main {

    private static Scanner sc = new Scanner(System.in);
    private static ReceptionistService receptionistService = new ReceptionistService();
    private static DoctorService doctorService = new DoctorService(receptionistService);
    private static PatientService patientService = new PatientService(receptionistService);

    public static void main(String[] args) {
        loadSampleData();
        mainMenu();
    }

    // ─── SAMPLE DATA ─────────────────────────────────────────────────

    private static void loadSampleData() {
        System.out.println("  Loading sample data...");
        receptionistService.registerPatient("Amit Kumar",  35, "9876543210", "amit@gmail.com",  3, "B+");
        receptionistService.registerPatient("Priya Singh", 28, "9123456780", "priya@gmail.com", 5, "O+");
        receptionistService.registerPatient("Rahul Verma", 45, "9012345678", "rahul@gmail.com", 2, "A+");
        receptionistService.registerPatient("Sunita Devi", 60, "8901234567", "sunita@gmail.com", 4, "AB-");
        receptionistService.assignToken("P001", "General");
        receptionistService.assignToken("P002", "Cardiology");
        receptionistService.assignToken("P003", "General");
        receptionistService.assignToken("P004", "Neurology");
        System.out.println("  Sample data loaded.\n");
    }

    // ─── MAIN MENU ───────────────────────────────────────────────────

    private static void mainMenu() {
        while (true) {
            System.out.println("\n  ╔══════════════════════════════════════════╗");
            System.out.println("  ║    HOSPITAL OPD MANAGEMENT SYSTEM        ║");
            System.out.println("  ╠══════════════════════════════════════════╣");
            System.out.println("  ║  1. Login as Receptionist                ║");
            System.out.println("  ║  2. Login as Doctor                      ║");
            System.out.println("  ║  3. Login as Patient                     ║");
            System.out.println("  ║  4. Exit                                 ║");
            System.out.println("  ╚══════════════════════════════════════════╝");
            System.out.print("  Choose: ");

            int choice = getInt();
            switch (choice) {
                case 1: receptionistMenu(); break;
                case 2: doctorLogin();      break;
                case 3: patientLogin();     break;
                case 4: System.out.println("  Goodbye!"); System.exit(0);
                default: System.out.println("  Invalid choice.");
            }
        }
    }

    // ─── RECEPTIONIST MENU ───────────────────────────────────────────

    private static void receptionistMenu() {
        while (true) {
            System.out.println("\n  ─────────────────────────────────────────");
            System.out.println("           RECEPTIONIST MENU");
            System.out.println("  ─────────────────────────────────────────");
            System.out.println("  1. Register New Patient");
            System.out.println("  2. Remove Patient");
            System.out.println("  3. Assign Token to Patient");
            System.out.println("  4. View Full Queue");
            System.out.println("  5. View All Doctors");
            System.out.println("  6. View All Patients");
            System.out.println("  7. Mark Patient as No-Show");
            System.out.println("  8. Check Prescription Expiry Alerts");
            System.out.println("  9. Logout");
            System.out.println("  ─────────────────────────────────────────");
            System.out.print("  Choose: ");

            int choice = getInt();
            switch (choice) {
                case 1:
                    System.out.print("  Name        : ");
                    String name = sc.nextLine().trim();
                    System.out.print("  Age         : ");
                    int age = getInt();
                    System.out.print("  Phone       : ");
                    String phone = sc.nextLine().trim();
                    System.out.print("  Email       : ");
                    String email = sc.nextLine().trim();
                    System.out.print("  Severity (1=routine  5=emergency): ");
                    int sev = getInt();
                    System.out.print("  Blood Group : ");
                    String bg = sc.nextLine().trim();
                    receptionistService.registerPatient(name, age, phone, email, sev, bg);
                    break;
                case 2:
                    System.out.print("  Enter Patient ID: ");
                    receptionistService.removePatient(sc.nextLine().trim());
                    break;
                case 3:
                    System.out.print("  Enter Patient ID      : ");
                    String pid2 = sc.nextLine().trim();
                    System.out.println("  Specializations       : General, Cardiology, Orthopedics, Pediatrics, Neurology");
                    System.out.print("  Enter Specialization  : ");
                    receptionistService.assignToken(pid2, sc.nextLine().trim());
                    break;
                case 4: receptionistService.viewFullQueue();   break;
                case 5: receptionistService.viewAllDoctors();  break;
                case 6: receptionistService.viewAllPatients(); break;
                case 7:
                    System.out.print("  Enter Patient ID: ");
                    receptionistService.markNoShow(sc.nextLine().trim());
                    break;
                case 8: receptionistService.checkExpiryAlerts(); break;
                case 9: return;
                default: System.out.println("  Invalid choice.");
            }
        }
    }

    // ─── DOCTOR LOGIN ────────────────────────────────────────────────

    private static void doctorLogin() {
        receptionistService.viewAllDoctors();
        System.out.print("  Enter your Doctor ID: ");
        String doctorId = sc.nextLine().trim();
        Doctor doctor = receptionistService.findDoctor(doctorId);
        if (doctor == null) {
            System.out.println("  ERROR: Doctor ID not found."); return;
        }
        System.out.println("  Welcome, Dr." + doctor.getName() + "!");
        doctorMenu(doctorId);
    }

    private static void doctorMenu(String doctorId) {
        while (true) {
            System.out.println("\n  ─────────────────────────────────────────");
            System.out.println("              DOCTOR MENU");
            System.out.println("  ─────────────────────────────────────────");
            System.out.println("  1. View My Queue");
            System.out.println("  2. Start Next Consultation");
            System.out.println("  3. Write Prescription");
            System.out.println("  4. Complete Consultation");
            System.out.println("  5. View My Schedule");
            System.out.println("  6. Logout");
            System.out.println("  ─────────────────────────────────────────");
            System.out.print("  Choose: ");

            int choice = getInt();
            switch (choice) {
                case 1: doctorService.viewMyQueue(doctorId);        break;
                case 2: doctorService.startConsultation(doctorId);  break;
                case 3:
                    System.out.print("  Patient ID  : ");
                    String pid = sc.nextLine().trim();
                    System.out.print("  Diagnosis   : ");
                    String diag = sc.nextLine().trim();
                    System.out.print("  Medicines (comma separated): ");
                    String[] meds = sc.nextLine().trim().split(",");
                    doctorService.writePrescription(pid, doctorId, diag, meds);
                    break;
                case 4:
                    System.out.print("  Patient ID: ");
                    doctorService.completeConsultation(sc.nextLine().trim(), doctorId);
                    break;
                case 5: doctorService.viewMySchedule(doctorId); break;
                case 6: return;
                default: System.out.println("  Invalid choice.");
            }
        }
    }

    // ─── PATIENT LOGIN ───────────────────────────────────────────────

    private static void patientLogin() {
        System.out.print("  Enter your Patient ID: ");
        String patientId = sc.nextLine().trim();
        Patient patient = receptionistService.findPatient(patientId);
        if (patient == null) {
            System.out.println("  ERROR: Patient ID not found."); return;
        }
        System.out.println("  Welcome, " + patient.getName() + "!");
        patientMenu(patientId);
    }

    private static void patientMenu(String patientId) {
        while (true) {
            System.out.println("\n  ─────────────────────────────────────────");
            System.out.println("              PATIENT MENU");
            System.out.println("  ─────────────────────────────────────────");
            System.out.println("  1. View My Token Status");
            System.out.println("  2. View My Prescriptions");
            System.out.println("  3. View Estimated Wait Time");
            System.out.println("  4. Cancel My Token");
            System.out.println("  5. Logout");
            System.out.println("  ─────────────────────────────────────────");
            System.out.print("  Choose: ");

            int choice = getInt();
            switch (choice) {
                case 1: patientService.viewMyToken(patientId);            break;
                case 2: patientService.viewMyPrescriptions(patientId);    break;
                case 3: patientService.viewEstimatedWaitTime(patientId);  break;
                case 4: patientService.cancelToken(patientId);            break;
                case 5: return;
                default: System.out.println("  Invalid choice.");
            }
        }
    }

    // ─── HELPER ──────────────────────────────────────────────────────

    private static int getInt() {
        while (true) {
            try {
                return Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.print("  Please enter a number: ");
            }
        }
    }
}