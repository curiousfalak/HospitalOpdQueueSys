# 🏥 Hospital OPD Queue Management System

> A console-based Java application that simulates a real-world **Outpatient Department (OPD) queue** using core Data Structures and Object-Oriented Programming principles.

---

## 📌 Problem Statement

Hospitals face a critical challenge in managing outpatient departments efficiently. Without a structured system:
- Patients queue **chaotically**, leading to long wait times.
- Doctors have **no visibility** into who is next or how urgent cases are.
- **Emergency patients** get no priority over regular patients.
- There is **no audit trail** of who was served, when, and by whom.

This system solves these problems by implementing a **priority-aware, queue-based scheduling engine** that handles patient registration, token generation, doctor assignment, emergency escalation, and service history — all through a clean, menu-driven console interface.

---

## ✨ Features

### For Receptionist / Admin:
| Feature | Description |
|---|---|
| Register Patient | Add a new patient with ID, name, age, and case type (GENERAL / EMERGENCY) |
| Issue Token | Assign a queue token based on priority |
| Call Next Patient | Dequeue and send the highest-priority patient to an available doctor |
| View Queue | Display all waiting patients in priority order |
| View History | See the last N patients served (stack-based) |
| Cancel Token | Remove a specific patient from the queue |

### For Patients:
| Feature | Description |
|---|---|
| Check Position | See current position in the queue |
| Check Wait Time | Estimate wait time based on average service time |
| View Token | Display issued token and assigned doctor |

---

## 🧠 OOP Design

The system is built on a clean object-oriented hierarchy:

```
User (abstract)
 ├── Patient
 │    ├── GeneralPatient
 │    └── EmergencyPatient
 └── Doctor
```

### Key OOP Principles Applied:

| Principle | Where Applied |
|---|---|
| **Abstraction** | `User.java` defines abstract `getRole()` and `getDetails()` — all user types must implement these |
| **Inheritance** | `Patient` extends `User`; `EmergencyPatient` and `GeneralPatient` extend `Patient` |
| **Polymorphism** | `QueueService.callNext()` handles any `Patient` type uniformly; priority is resolved at runtime |
| **Encapsulation** | All entity fields are `private` with getter/setter access; business logic stays in service classes |
| **Single Responsibility** | `TokenService`, `QueueService`, `FineService`, `DoctorService` each own one concern |

---

## ❤️ Core Heart Algorithm

The central algorithm lives in **`QueueService.java`** — specifically the `callNextPatient()` method. Every patient visit passes through this decision engine.

### Patient Enqueue Flow

```
registerPatient(name, age, caseType)
        │
        ▼
  caseType == EMERGENCY?  ──YES──▶  priority = 1 (highest)
        │ NO
        ▼
  age >= 60?              ──YES──▶  priority = 2 (senior citizen)
        │ NO
        ▼
  priority = 3 (general)
        │
        ▼
  TokenService.issueToken(patient)
  PriorityQueue.enqueue(patient, priority)
        │
        ▼
  Return token ✅
```

### Call Next Patient Flow

```
callNextPatient()
        │
        ▼
  Queue empty?  ──YES──▶  "No patients waiting"
        │ NO
        ▼
  patient = priorityQueue.dequeue()   ← highest priority first
        │
        ▼
  DoctorService.getAvailableDoctor()
        │
        ▼
  Doctor available?  ──NO──▶  patient re-enqueued, throw NoDoctorAvailableException
        │ YES
        ▼
  Create Visit record
  doctor.markBusy()
  patient.setStatus("IN_CONSULTATION")
        │
        ▼
  historyStack.push(visit)            ← O(1) push for audit trail
        │
        ▼
  Return visit details ✅
```

### Why These Choices?
- **Priority Queue** — Emergency and senior patients always skip ahead. A min-heap gives O(log n) insertion and O(log n) dequeue, ensuring the most critical case is always served first regardless of arrival order.
- The **priority integer** (1, 2, 3) is computed once at registration and never changes, keeping dequeue logic simple and auditable.
- Pushing to `historyStack` at the moment of dequeue — not at discharge — ensures the record exists even if the app crashes during consultation.

---

## 🗂️ DSA Concepts Used

| Data Structure | Class | Purpose |
|---|---|---|
| **Priority Queue (Min-Heap)** | `OPDQueue` | O(log n) enqueue/dequeue — serves emergency and senior patients first |
| **Stack (LIFO)** | `VisitHistoryStack` | Tracks last-served patients; O(1) push/pop for undo and audit |
| **HashMap** | `PatientRegistry` | O(1) average-case patient lookup, update, and removal by ID |
| **Linked List** | `DoctorPool` | Dynamic doctor roster; O(1) add/remove at head for fast availability toggling |

---

## 📁 Project Structure

```
├── Main.java                    # Entry point — interactive console menu
├── QueueService.java            # Core logic: enqueue, dequeue, call next
├── TokenService.java            # Generates and manages queue tokens
├── DoctorService.java           # Doctor availability and assignment
├── FineService.java             # (Extension) No-show fine calculation
├── OPDQueue.java                # Priority queue backed by min-heap
├── VisitHistoryStack.java       # LIFO stack for visit audit trail
├── PatientRegistry.java         # HashMap-based patient store
├── DoctorPool.java              # Linked list of available doctors
├── Visit.java                   # Visit entity (patient, doctor, timestamps)
├── Patient.java                 # Patient entity (extends User)
├── EmergencyPatient.java        # Emergency subtype (priority = 1)
├── GeneralPatient.java          # General subtype (priority = 3)
├── Doctor.java                  # Doctor entity (extends User)
├── User.java                    # Abstract base class for all users
├── Constants.java               # App-wide constants and custom exceptions
├── QueueServiceTest.java        # JUnit tests for QueueService
├── OPDQueueTest.java            # JUnit tests for OPDQueue (priority ordering)
├── VisitHistoryStackTest.java   # JUnit tests for VisitHistoryStack
└── TokenServiceTest.java        # JUnit tests for TokenService
```

---

## 🧪 Test Cases

Tests are written using **JUnit 5** and run directly inside IntelliJ IDEA (right-click the test file → Run).

### `QueueServiceTest`

| Test | What it verifies |
|---|---|
| `happyPath_patientServed()` | A registered patient is dequeued and assigned a doctor; visit record is created with status `IN_CONSULTATION` |
| `emptyQueue_throwsException()` | Calling `callNextPatient()` on an empty queue throws `EmptyQueueException` |
| `noDoctorAvailable_requeuesPatient()` | If no doctor is free, the patient is re-enqueued and `NoDoctorAvailableException` is thrown |
| `emergencyPriority_servedFirst()` | An EMERGENCY patient registered after a GENERAL patient is still dequeued first |

### `OPDQueueTest`

| Test | What it verifies |
|---|---|
| `priorityOrder_emergencyFirst()` | EMERGENCY (p=1) → SENIOR (p=2) → GENERAL (p=3) dequeue order regardless of insertion sequence |
| `sameP_fifoOrder()` | Two GENERAL patients dequeue in FIFO order (arrival time used as tiebreaker) |
| `dequeueEmpty_throwsException()` | Dequeuing from an empty queue throws `RuntimeException` |
| `peekDoesNotRemove()` | `peek()` returns the highest priority patient but does not remove them; size stays constant |

### `VisitHistoryStackTest`

| Test | What it verifies |
|---|---|
| `lifoOrder_isCorrect()` | Push Visit A, B, C → pop returns C, B, A in that order (LIFO) |
| `peek_doesNotRemove()` | `peek()` returns the top visit but size stays the same |
| `overflow_throwsException()` | Pushing beyond capacity throws `RuntimeException` (Stack Overflow) |
| `underflow_throwsException()` | Popping from an empty stack throws `RuntimeException` (Stack Underflow) |

### `TokenServiceTest`

| Test | What it verifies |
|---|---|
| `tokenIncrementsSequentially()` | Tokens are issued as T001, T002, T003 in registration order |
| `duplicatePatient_throwsException()` | Registering the same patient ID twice throws `DuplicatePatientException` |
| `cancelToken_removesFromRegistry()` | Cancelling a token removes the patient from the `PatientRegistry`; subsequent lookup returns null |

---

## 📐 Assumptions

- Patients are categorised as `GENERAL` or `EMERGENCY` at registration; type cannot change after issuance.
- `EMERGENCY` patients always have priority over `SENIOR_CITIZEN` patients, who always have priority over `GENERAL` patients.
- Within the same priority tier, patients are served in **FIFO** (arrival) order.
- Each doctor can serve **one patient at a time**; they are marked `BUSY` on assignment and `AVAILABLE` on discharge.
- The system pre-loads **3 doctors** (D001, D002, D003) at startup.
- A maximum of **50 patients** can be in the queue at any given time (`MAX_QUEUE_SIZE = 50`).
- Visit history retains the **last 100 visits** in the stack (`MAX_HISTORY_SIZE = 100`).
- No-show fines are charged at **₹50 per missed appointment** if a patient cancels after being called.

---

## ⚙️ Installation

To run this application, you need **Java 17+** and **IntelliJ IDEA** installed.

### Clone the Repository
```bash
git clone https://github.com/<your-username>/HospitalOPDQueueSystem.git
```

### Open in IntelliJ IDEA
1. Open IntelliJ IDEA → **File → Open** → select the project folder.
2. IntelliJ will auto-detect the project structure.
3. Ensure **JUnit 5** is added as a dependency (IntelliJ will prompt you when you open any test file).

### Run the Application
1. Open `Main.java`.
2. Click the green **Run** button, or press **Shift + F10**.

### Run Tests
- Open any test file (e.g., `QueueServiceTest.java`).
- Right-click → **Run 'QueueServiceTest'**.
- Or right-click the project root → **Run All Tests** to execute the full suite.

---

## 💻 Usage Example

```
1.Register Patient  2.Call Next  3.View Queue  4.View History  5.Cancel Token  0.Exit
Choice: 1
Patient ID  : P001
Name        : Ravi Kumar
Age         : 68
Type        : GENERAL
Token issued: T001  [Priority: SENIOR_CITIZEN]

Choice: 1
Patient ID  : P002
Name        : Anjali Mehta
Age         : 34
Type        : EMERGENCY
Token issued: T002  [Priority: EMERGENCY]

Choice: 2
Calling next patient...
→ Anjali Mehta (T002) assigned to Dr. Sharma [Room 3]
  Emergency patient served first ✓

Choice: 2
Calling next patient...
→ Ravi Kumar (T001) assigned to Dr. Patel [Room 1]
  Senior citizen served next ✓
```
