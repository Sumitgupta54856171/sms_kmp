# Implementation Plan - "Same-to-Same" UI/API Restructuring

This plan outlines a complete restructuring of the app's key modules to strictly mirror the `sms_frontend` (React) implementation in terms of UI design, component layout, and API interaction logic.

## User Review Required

> [!IMPORTANT]
> This is a major overhaul of the UI components. I will be moving away from generic Material 3 layouts towards the specific customized "Premium" look of your React frontend (Slate-900 colors, specific card shadows, customized badges, and multi-step forms).

## Proposed Changes

### 1. Attendance & Summary (Group 4)
- **[MODIFY] [AttendanceScreen.kt](file:///home/ubuntu/AndroidStudioProjects/SchoolManagement/shared/src/commonMain/kotlin/com/example/schoolmanagement/presentation/attendance/AttendanceScreen.kt)**
    - Recreate the header with "Edit/Done" toggle.
    - Implement 4 distinct stats cards (Present, Absent, Holiday, Total roster).
    - Restructure the table: Initials avatar (Indigo-100), Roll No sorting, Scholar No.
    - Update status buttons: Green (P), Red (A), Purple (H) with specific border/bg states.
    - Bottom summary bar with "Save attendance" button.
- **[MODIFY] [AttendanceSummaryScreen.kt](file:///home/ubuntu/AndroidStudioProjects/SchoolManagement/shared/src/commonMain/kotlin/com/example/schoolmanagement/presentation/attendance/AttendanceSummaryScreen.kt)**
    - Implement the "Reporting Period" header with a gradient glow.
    - Add gender-wise breakdown bars (Boys/Girls average).
    - Class-wise expandable cards with detailed gender stats and progress bars.

### 2. Teacher Management (Group 1)
- **[MODIFY] [TeacherScreen.kt](file:///home/ubuntu/AndroidStudioProjects/SchoolManagement/shared/src/commonMain/kotlin/com/example/schoolmanagement/presentation/teachers/TeacherScreen.kt)**
    - Switch from a list to a **Grid of Cards** (Adaptive columns).
    - Cards will feature: Large circular avatars, "Specialization" subtitle, Role/Status badges, and a "Quick Actions" footer (Mail, Phone, Edit, Role, Timetable icons).
    - Implement the "Change Role" floating modal.

### 3. Timetable & Classes (Group 2)
- **[MODIFY] [TimetableScreen.kt](file:///home/ubuntu/AndroidStudioProjects/SchoolManagement/shared/src/commonMain/kotlin/com/example/schoolmanagement/presentation/academics/TimetableScreen.kt)**
    - Implement a clean Tabbed interface: "Grade View", "Teacher View", "My Timetable", "Class Teacher".
    - Grade view: Scrollable horizontal grade selector, period cards with "Period X" badges.
- **[MODIFY] [ClasspageScreen.kt](file:///home/ubuntu/AndroidStudioProjects/SchoolManagement/shared/src/commonMain/kotlin/com/example/schoolmanagement/presentation/academics/ClasspageScreen.kt)**
    - Grid of 16.dp padded cards with Teal-900 icons.

### 4. Examination & Grade Entry (Group 3)
- **[MODIFY] [GradeScreen.kt](file:///home/ubuntu/AndroidStudioProjects/SchoolManagement/shared/src/commonMain/kotlin/com/example/schoolmanagement/presentation/assessment/GradeScreen.kt)**
    - Implement the **5-Step Filter Process**: 1. Teacher -> 2. Type (Test/Exam) -> 3. Name -> 4. Class -> 5. Subject.
    - Data-driven filtering: Only show classes/subjects assigned to the selected teacher/exam.
    - Grade table: Marks input with "Max Marks" suffix, "Saved" (Green) or "New" (Amber) status badges.

### 5. Fee Profile & Operations (Group 5)
- **[MODIFY] [FeeProfileScreen.kt](file:///home/ubuntu/AndroidStudioProjects/SchoolManagement/shared/src/commonMain/kotlin/com/example/schoolmanagement/presentation/fees/FeeProfileScreen.kt)**
    - **Summary:** Strictly 6 boxes: Annual Fee, Total Amount (Fee + Due), Total Paid, Discount, Balance Amount, Paid %.
    - **Calculations:** Total Amount = Annual + Previous Dues; Balance = Total - Paid - Discount.
    - **History:** Session-wise history table with columns: Session, Annual Fee, Total Paid, Due, Payments.
- **[MODIFY] [TCScreen.kt](file:///home/ubuntu/AndroidStudioProjects/SchoolManagement/shared/src/commonMain/kotlin/com/example/schoolmanagement/presentation/operations/TCScreen.kt)**
    - Implement the full form with 20+ fields (SSSMID, Aadhaar, APAAR ID, Bank Details, etc.).
    - Add a "Preview" mode that mimics an A5 printed document.

### 6. Critical Fix: Data Isolation
- **[MODIFY] All ViewModels**
    - Audit all data fetching logic to ensure that whenever a student/class is selected, previous state is explicitly cleared.
    - Fix the "All students show paid fees" issue by ensuring the `StudentListItem` correctly identifies payment status *per student* based on the specific API response from `/api/v1/fee/student/{id}/fee`.

## Verification Plan

### Manual Verification
- Compare every screen side-by-side with the `sms_frontend` running in a browser.
- Verify that clicking "Pay Fees" for Student A does NOT show Student B's data.
- Verify the 6-box summary calculations in the Fee Profile.
- Verify the 5-step filtering in Grade Entry.
