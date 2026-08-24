# 🏥 Hospital OPD Queue Management System

A simple **Java console application** that manages patients in a hospital OPD using **OOP concepts and Data Structures**.

The system gives priority to patients based on the seriousness of their condition, ensuring that critical patients are treated before routine cases.

---

## 📌 Problem Statement

In a normal hospital queue, patients are often treated in the order they arrive. However, this may not be suitable when a patient has a more serious condition.

This project solves this problem by using a **Priority Queue**.

* Patients are assigned a severity level from **1 to 5**
* Higher-severity patients are given higher priority
* If two patients have the same severity, the patient who arrived first is treated first

---

## ✨ Features

### 👩‍💼 Receptionist

* Register new patients
* Assign tokens
* Assign patients to doctors based on specialization
* View all patients
* View all doctors
* View the current queue
* Remove patients
* Mark patients as no-show
* Check prescription expiry alerts

### 👨‍⚕️ Doctor

* View assigned patients
* View personal queue
* Start a consultation
* Complete a consultation
* Write prescriptions
* View daily schedule

### 🧑 Patient

* View token details
* Check queue position
* View estimated waiting time
* View prescription history
* Cancel a token

---

## 🧠 How the Priority Queue Works

Every patient is given a **severity score**:

| Severity | Priority  |
| -------- | --------- |
| 5        | Emergency |
| 4        | High      |
| 3        | Medium    |
| 2        | Low       |
| 1        | Routine   |

Example:

```text
Patient A → Severity 2
Patient B → Severity 5
Patient C → Severity 4
```

The treatment order will be:

```text
1. Patient B
2. Patient C
3. Patient A
```

If two patients have the same severity, the patient who arrived earlier is treated first.

---

## 🔄 Patient Flow

```text
Register Patient
       ↓
Assign Token
       ↓
Patient Added to Queue
       ↓
Doctor Starts Consultation
       ↓
Doctor Writes Prescription
       ↓
Consultation Completed
```

---

## 🛠️ Technologies Used

* Java 17
* Object-Oriented Programming (OOP)
* PriorityQueue
* ArrayList
* Enums


---


---
