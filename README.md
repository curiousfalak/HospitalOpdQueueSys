# 🏥 Hospital OPD Queue Management System

> A console-based Java application that simulates a real-world **Outpatient Department (OPD)** using a **priority-driven queue engine**, role-based service architecture, and a full prescription lifecycle — built entirely on core DSA and OOP principles.

---

## 📌 Problem Statement

Hospitals face a critical challenge managing outpatient departments without a structured system:

- Patients queue **chaotically** — no mechanism to prioritise critical cases over routine visits.
- **High-severity patients** wait behind routine check-ups simply because they arrived later.
- Doctors have **no visibility** into who is next or how urgent their cases are.
- There is **no audit trail** of tokens, prescriptions, or patient status transitions.
- Receptionist, doctor, and patient roles are **entangled** — no separation of concern.

This system solves each of these by implementing a **severity-score-driven priority queue**, role-separated service layers, a complete token lifecycle, and a prescription engine with expiry tracking.

---

## ✨ Features

### Receptionist
| Feature | Description |
|---|---|
| Register Patient | Add a new patient with name, age, phone, email, severity (1–5), and blood group — with full input validation |
| Remove Patient | Remove a patient unless they are currently in consultation |
| Assign Token | Issue a numbered token and route the patient to the best available doctor for the requested specialization |
| View Full Queue | Display all waiting patients sorted by severity, then arrival time |
| View All Doctors | Show all doctors with specialization, room, and current load |
| View All Patients | List every registered patient with token and status |
| Mark No-Show | Demote a patient's severity to 1 and push them to the back of the queue |
| Prescription Expiry Alerts | Flag all expired or soon-to-expire prescriptions across all patients |

### Doctor
| Feature | Description |
|---|---|
| View My Queue | See patients assigned to this doctor, ordered by severity then arrival |
| Start Consultation | Pull the highest-priority waiting patient into `IN_CONSULTATION` |
| Write Prescription | Attach a prescription (diagnosis + medicines) to a patient currently in consultation |
| Complete Consultation | Set patient to `DONE`, release doctor load, remove from queue, mark token `COMPLETED` |
| View My Schedule | See all patients assigned today with room and load summary |

### Patient
| Feature | Description |
|---|---|
| View My Token | See current token number, status, assigned doctor, and estimated wait |
| View My Prescriptions | Full prescription history with expiry status |
| View Estimated Wait Time | Queue position and `(position - 1) × 5` minute estimate |
| Cancel Token | Cancel a waiting token; blocked if currently in consultation |

---

## 🧠 OOP Design

### Class Architecture

```
ReceptionistService               ← orchestrates all shared state
 ├── PriorityQueueEngine           ← core DSA engine
 ├── List<Patient>  allPatients    ← patient registry
 ├── List<Doctor>   allDoctors     ← doctor pool (6 pre-loaded)
 └── List<Token>    allTokens      ← token ledger

DoctorService(receptionistService)    ← doctor-role operations
PatientService(receptionistService)   ← patient-role operations

Entities
 ├── Patient      (Status enum: WAITING → IN_CONSULTATION → DONE / NO_SHOW)
 ├── Doctor       (load tracking: currentLoad / maxLoad = 20)
 ├── Token        (TokenStatus enum: ACTIVE → COMPLETED / CANCELLED / EXPIRED)
 └── Prescription (issuedDate + 30-day expiry, isExpired(), daysUntilExpiry())
```

### OOP Principles Applied

| Principle | Where Applied |
|---|---|
| **Encapsulation** | All entity fields (`Patient`, `Doctor`, `Token`, `Prescription`) are `private` with controlled getter/setter access. Business rules — load limits, status transitions, expiry logic — stay inside the owning class |
| **Abstraction** | `PriorityQueueEngine` hides all heap internals behind `addPatient()`, `getNextPatient()`, `getPosition()`, `getEstimatedWaitTime()`. Callers never touch the comparator or internal sort |
| **Single Responsibility** | `ReceptionistService` owns registration and token assignment. `DoctorService` owns the consultation lifecycle. `PatientService` owns patient-facing reads and self-service actions. Each class has exactly one reason to change |
| **Dependency Injection** | `DoctorService` and `PatientService` receive `ReceptionistService` via constructor — they share state without owning it, keeping the system to a single source of truth |
| **Enum-based State Machine** | `Patient.Status` (`WAITING → IN_CONSULTATION → DONE / NO_SHOW`) and `Token.TokenStatus` (`ACTIVE → COMPLETED / CANCELLED`) make illegal state transitions compile-time visible |

---

## ❤️ Core Heart Algorithm

The central engine lives in **`PriorityQueueEngine.java`** — a `PriorityQueue` backed by a custom two-key comparator that every patient booking passes through. Three flows drive the entire system.

### Flow 1 — Token Assignment (`ReceptionistService.assignToken`)

```
assignToken(patientId, specialization)
        │
        ▼
  Patient exists?           ──NO──▶  return null  ("Patient not found")
        │ YES
        ▼
  Already in queue?         ──YES──▶  return null  ("Already has active token")
        │ NO
        ▼
  Available doctor for      ──NO──▶  return null  ("No doctor for specialization")
  this specialization?
        │ YES
        ▼
  Create Token (ACTIVE, tokenCounter++)
  patient.tokenNumber    = tokenCounter
  patient.assignedDoctor = doctor.getDoctorId()
  patient.status         = WAITING
  doctor.assignPatient(patientId)    →  currentLoad++
  queueEngine.addPatient(patient)    →  O(log n) heap insert
        │
        ▼
  Return token ✅
```

### Flow 2 — Priority Queue Comparator (the decision rule)

```java
// PriorityQueueEngine constructor:
new PriorityQueue<>((p1, p2) -> {
    if (p2.getSeverityScore() != p1.getSeverityScore())
        return p2.getSeverityScore() - p1.getSeverityScore(); // higher severity first
    return p1.getArrivalTime().compareTo(p2.getArrivalTime()); // FIFO tiebreaker
});
```

**Severity 5 (emergency) is always served before severity 1 (routine).
Within the same severity, the patient who arrived first is served first.**

### Flow 3 — Consultation Lifecycle (`DoctorService`)

```
startConsultation(doctorId)
        │
        ▼
  Filter: assignedDoctor == doctorId AND status == WAITING
  Sort:   severity DESC → arrivalTime ASC
        │
        ▼
  next = first in sorted list
  next.status = IN_CONSULTATION  ✅

  ─────────────────────────────────────────────────────────

completeConsultation(patientId, doctorId)
        │
        ▼
  patient.status == IN_CONSULTATION?  ──NO──▶  error ("Not in consultation")
        │ YES
        ▼
  patient.status          = DONE
  doctor.releasePatient()             →  currentLoad--
  queueEngine.removePatient(patientId)
  token.status            = COMPLETED
        │
        ▼
  Done ✅

  ─────────────────────────────────────────────────────────

markNoShow(patientId)   [ReceptionistService]
        │
        ▼
  queue.remove(patient)
  patient.severityScore = 1           ← demoted to lowest priority
  patient.status        = NO_SHOW
  queue.offer(patient)                ← re-inserted; now sits at back
        │
        ▼
  Patient served last ✅
```

### Why This Design?

**`PriorityQueue` with custom comparator** — A heap reordered by the two-key comparator gives O(log n) insert and O(log n) poll. This is the only structure that guarantees the most critical patient is always at the head, regardless of insertion order.

**Severity as a mutable integer (1–5)** — Rather than a fixed type hierarchy, severity is a mutable field. This is deliberate: the no-show feature *changes* a patient's priority at runtime (severity → 1) by removing them, mutating the score, and re-inserting. A fixed subclass could never support this without extra flags.

**`getSortedSnapshot()` for position queries** — Raw `PriorityQueue` iteration returns elements in an unspecified order. For position and wait-time calculations, a fresh `ArrayList` snapshot is built and sorted. This keeps `getPosition()` accurate without breaking the heap.

**Shared `ReceptionistService` reference** — `DoctorService` and `PatientService` hold a reference to the same instance. All reads and writes go through one object, eliminating out-of-sync state bugs across three service layers.

**`(position - 1) × 5` wait estimate** — Simple, predictable formula: position 1 = 0 mins, position 2 = 5 mins, position n = (n-1) × 5 mins.

---

## 🗂️ DSA Concepts Used

| Data Structure | Class | Complexity | Purpose |
|---|---|---|---|
| **Priority Queue (Max-Heap by severity)** | `PriorityQueueEngine` | O(log n) insert / O(log n) poll | Always surfaces the highest-severity patient; FIFO within the same severity tier |
| **ArrayList** | `ReceptionistService` (`allPatients`, `allDoctors`, `allTokens`) | O(1) add / O(n) search | Dynamic patient, doctor, and token registries — traversed for lookups, display, and expiry scans |
| **Sorted Snapshot** | `PriorityQueueEngine.getSortedSnapshot()` | O(n log n) | Correct ordered view of the queue for position and wait-time queries, since raw heap iteration is unordered |

---

## 📁 Project Structure

```
HospitalOpdQueueSys/
├── Main.java                        # Entry point — 3-role console menu (Receptionist / Doctor / Patient)
├── ReceptionistService.java         # Core orchestrator: registration, token assignment, queue ops, expiry alerts
├── DoctorService.java               # Doctor workflow: view queue, start/complete consultation, prescriptions
├── PatientService.java              # Patient self-service: token status, prescriptions, wait time, cancel
├── PriorityQueueEngine.java         # DSA engine: PriorityQueue with severity + arrival-time comparator
├── Patient.java                     # Entity: patientId, severityScore, Status enum, prescription history
├── Doctor.java                      # Entity: doctorId, specialization, load tracking (currentLoad / maxLoad)
├── Token.java                       # Entity: tokenNumber, TokenStatus enum (ACTIVE / COMPLETED / CANCELLED)
├── Prescription.java                # Entity: diagnosis, medicines, 30-day expiry, isExpired(), daysUntilExpiry()
└── HospitalOpdQueueSysTest.java     # JUnit 5 test suite — 39 ordered tests across 10 categories
```

---

## 🧪 Test Cases

All 39 tests use **JUnit 5** with `@TestMethodOrder(MethodOrderer.OrderAnnotation.class)`.
Run in IntelliJ: right-click `HospitalOpdQueueSysTest.java` → **Run**.

### 1 · Patient Registration (`@Order 1–5`)

| Test | What it verifies |
|---|---|
| `testRegisterPatient_Valid()` | Valid patient is created with correct name, age, ID `P001`, and initial status `WAITING` |
| `testRegisterPatient_InvalidPhone()` | A phone number shorter than 10 digits returns `null` |
| `testRegisterPatient_InvalidEmail()` | A malformed email (no `@` or `.`) returns `null` |
| `testRegisterPatient_InvalidSeverity()` | Severity outside 1–5 (e.g. `9`) returns `null` |
| `testRegisterPatient_SequentialIds()` | Three patients registered in order receive `P001`, `P002`, `P003` |

### 2 · Token Assignment (`@Order 6–10`)

| Test | What it verifies |
|---|---|
| `testAssignToken_Valid()` | Token is created with number `1` and status `ACTIVE` |
| `testAssignToken_Duplicate()` | Assigning a second token to the same patient returns `null` |
| `testAssignToken_PatientNotFound()` | Unknown patient ID `P999` returns `null` |
| `testAssignToken_NoDoctor()` | Requesting `"Dermatology"` (no doctor exists) returns `null` |
| `testAssignToken_AddsToQueue()` | After assignment, `isPatientInQueue("P001")` returns `true` |

### 3 · Priority Queue (`@Order 11–15`)

| Test | What it verifies |
|---|---|
| `testQueue_SeverityPriority()` | Severity-5 patient is position 1; severity-1 patient is position 2, regardless of insertion order |
| `testQueue_Size()` | Queue size increments correctly: 0 → 1 → 2 as patients are added |
| `testQueue_WaitTimeFirstPatient()` | Patient at position 1 has estimated wait of `0` minutes |
| `testQueue_WaitTimeSecondPatient()` | Patient at position 2 has estimated wait of `5` minutes — formula `(2-1) × 5` |
| `testQueue_RemovePatient()` | `removePatient("P001")` causes `isPatientInQueue` to return `false` |

### 4 · Doctor (`@Order 16–20`)

| Test | What it verifies |
|---|---|
| `testDoctor_AvailableByDefault()` | New doctor starts with `isAvailable=true`, `canTakePatient=true`, `currentLoad=0` |
| `testDoctor_AssignPatient()` | After `assignPatient("P001")`, `currentLoad` is `1` and doctor is still available (max is 20) |
| `testDoctor_ReleasePatient()` | After `releasePatient("P001")`, `currentLoad` returns to `0` |
| `testDoctor_MaxLoad()` | Assigning 20 patients sets `isAvailable=false` and `canTakePatient=false` |
| `testDoctor_FindById()` | `findDoctor("D001")` returns Dr. Sharma with specialization `"General"` |

### 5 · Consultation (`@Order 21–24`)

| Test | What it verifies |
|---|---|
| `testConsultation_Start()` | `startConsultation("D001")` transitions patient status to `IN_CONSULTATION` |
| `testConsultation_Complete()` | `completeConsultation` transitions patient status to `DONE` |
| `testConsultation_RemovesFromQueue()` | After completion, `isPatientInQueue` returns `false` |
| `testConsultation_ReducesDoctorLoad()` | Doctor's `currentLoad` decrements by exactly 1 after completion |

### 6 · Prescription (`@Order 25–28`)

| Test | What it verifies |
|---|---|
| `testPrescription_WriteDuringConsultation()` | Prescription created with diagnosis `"Fever"` and 2 medicines (`Paracetamol`, `Ibuprofen`) |
| `testPrescription_NotInConsultation()` | Calling `writePrescription` before `startConsultation` returns `null` |
| `testPrescription_AddedToHistory()` | `patient.getPrescriptions().size()` is `1`; diagnosis field matches |
| `testPrescription_NotExpiredOnCreation()` | New prescription: `isExpired()=false`, `daysUntilExpiry()>0` |

### 7 · No-Show (`@Order 29–30`)

| Test | What it verifies |
|---|---|
| `testNoShow_ReducesSeverity()` | Marking a severity-5 patient no-show sets `severityScore=1` and `status=NO_SHOW` |
| `testNoShow_MovesToBack()` | After no-show, the lower-severity patient moves to position 1; no-show patient drops to position 2 |

### 8 · Remove Patient (`@Order 31–33`)

| Test | What it verifies |
|---|---|
| `testRemovePatient_Valid()` | Returns `true`; `findPatient("P001")` returns `null` afterwards |
| `testRemovePatient_InConsultation()` | Returns `false` — cannot remove a patient mid-consultation |
| `testRemovePatient_NotFound()` | Returns `false` for unknown ID `P999` |

### 9 · Cancel Token (`@Order 34–36`)

| Test | What it verifies |
|---|---|
| `testCancelToken_RemovesFromQueue()` | `isPatientInQueue` returns `false` after cancellation |
| `testCancelToken_StatusDone()` | Patient status becomes `DONE`; `assignedDoctorId` is cleared to `null` |
| `testCancelToken_DuringConsultation()` | Cancel is blocked; patient remains `IN_CONSULTATION` |

### 10 · Token Status (`@Order 37–39`)

| Test | What it verifies |
|---|---|
| `testToken_ActiveAfterAssignment()` | Token status is `ACTIVE` immediately after `assignToken` |
| `testToken_CompletedAfterConsultation()` | Token status becomes `COMPLETED` after `completeConsultation` |
| `testToken_CancelledAfterCancel()` | Token status becomes `CANCELLED` after `patientService.cancelToken` |

---

## 📐 Assumptions

- Severity is an integer from **1 (routine) to 5 (emergency)**; it is mutable — no-show resets it to `1` at runtime.
- Each doctor has a `maxLoad` of **20 patients**; once reached, `canTakePatient()` returns `false`.
- Wait time formula: `(queuePosition - 1) × 5 minutes` — derived from a sorted snapshot, not raw heap order.
- The system pre-loads **6 doctors** at startup across 5 specializations: General (×2), Cardiology, Orthopedics, Pediatrics, Neurology.
- Patient IDs are auto-generated sequentially: `P001`, `P002`, … — not entered manually.
- Token numbers are auto-incremented integers starting at `1`.
- A patient cannot hold more than **one active token** at a time.
- Prescriptions are valid for **30 days** from the issue date; `daysUntilExpiry() ≤ 3` triggers a WARNING in the expiry alert scan.
- A patient `IN_CONSULTATION` cannot be removed or have their token cancelled.

---

## ⚙️ Installation

Requires **Java 17+** and **IntelliJ IDEA**.

```bash
git clone https://github.com/<your-username>/HospitalOpdQueueSystem.git
```

1. Open IntelliJ IDEA → **File → Open** → select the project folder.
2. IntelliJ will detect the `HospitalOpdQueueSys` package structure automatically.
3. When you open `HospitalOpdQueueSysTest.java`, IntelliJ will prompt you to add **JUnit 5** — accept it.

**Run the app:** Open `Main.java` → click the green Run button or press `Shift + F10`.  
**Run tests:** Right-click `HospitalOpdQueueSysTest.java` → **Run 'HospitalOpdQueueSysTest'**.

---

## 💻 Usage Example

```
  ╔══════════════════════════════════════════╗
  ║    HOSPITAL OPD MANAGEMENT SYSTEM        ║
  ╠══════════════════════════════════════════╣
  ║  1. Login as Receptionist                ║
  ║  2. Login as Doctor                      ║
  ║  3. Login as Patient                     ║
  ║  4. Exit                                 ║
  ╚══════════════════════════════════════════╝
  Choose: 1

  Name        : Anjali Mehta
  Age         : 34
  Phone       : 9876543210
  Email       : anjali@gmail.com
  Severity (1=routine  5=emergency): 5
  Blood Group : O+
  SUCCESS: Registered Anjali Mehta → ID: P005

  Enter Patient ID      : P005
  Enter Specialization  : Cardiology
  SUCCESS: Token #5 → Anjali Mehta → Dr.Mehta Room 102

  ══════════════════════════════════════
           CURRENT OPD QUEUE
  ══════════════════════════════════════
  Waiting: 2
  1. Token#5  | Anjali Mehta   | Age:34 | Severity:5 | WAITING | Doctor:D002
  2. Token#2  | Priya Singh    | Age:28 | Severity:5 | WAITING | Doctor:D002
  ══════════════════════════════════════
```

---

## 🤝 Contributing

Contributions are welcome! Please fork the repository and submit a pull request with your changes.

1. Fork the repository
2. Create your feature branch: `git checkout -b feature/your-feature-name`
3. Commit your changes: `git commit -m "Add your feature"`
4. Push to the branch: `git push origin feature/your-feature-name`
5. Open a Pull Request

---


You can find the GitHub repository for this project **[here](https://github.com/curiousfalak/HospitalOpdQueueSys)**.

Feel free to reach out if you have any questions or need any help!
