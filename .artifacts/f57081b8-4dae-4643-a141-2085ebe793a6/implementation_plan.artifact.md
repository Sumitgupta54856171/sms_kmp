# Implementation Plan - Teacher Form & Class Student List

This plan outlines the steps to implement the Teacher Add/Edit form and the Class Student List screen in the KMP project, matching the features and logic of the `sms_frontend` (React) implementation.

## User Review Required

> [!IMPORTANT]
> The backend seems to have some field name inconsistencies (e.g., `rolleNo` vs `rollNo`). I will add compatibility fields to the models.

## Proposed Changes

### [Models & API]

#### [MODIFY] [TeacherModels.kt](file:///home/ubuntu/AndroidStudioProjects/SchoolManagement/shared/src/commonMain/kotlin/com/example/schoolmanagement/api/models/TeacherModels.kt)
- Add `password` field to `TeacherData` for account creation.

#### [MODIFY] [StudentModels.kt](file:///home/ubuntu/AndroidStudioProjects/SchoolManagement/shared/src/commonMain/kotlin/com/example/schoolmanagement/api/models/StudentModels.kt)
- Add `rolleNo` and other potential backend field aliases to `StudentListItem`.

#### [MODIFY] [StudentRepository.kt](file:///home/ubuntu/AndroidStudioProjects/SchoolManagement/shared/src/commonMain/kotlin/com/example/schoolmanagement/api/StudentRepository.kt)
- Implement `updateBulkRollNo` API.

---

### [Teacher Feature]

#### [MODIFY] [TeacherScreen.kt](file:///home/ubuntu/AndroidStudioProjects/SchoolManagement/shared/src/commonMain/kotlin/com/example/schoolmanagement/presentation/teachers/TeacherScreen.kt)
- Implement the `TeacherAddForm` dialog with all fields from the React implementation.
- Wire up the "Add Teacher" and "Edit Teacher" (if applicable) logic.

#### [MODIFY] [TeacherViewModel.kt](file:///home/ubuntu/AndroidStudioProjects/SchoolManagement/shared/src/commonMain/kotlin/com/example/schoolmanagement/presentation/teachers/TeacherViewModel.kt)
- Add `updateTeacher` method.
- Handle state for the add/edit form.

---

### [Class & Student Feature]

#### [NEW] [ClassStudentListScreen.kt](file:///home/ubuntu/AndroidStudioProjects/SchoolManagement/shared/src/commonMain/kotlin/com/example/schoolmanagement/presentation/academics/ClassStudentListScreen.kt)
- Create a new screen to display students of a specific class.
- Features:
    - Display class name and student count.
    - Table with Class No, Roll No, Student Name, and Scholar No.
    - Editable Roll Number field.
    - Bulk save functionality for roll numbers.

#### [MODIFY] [App.kt](file:///home/ubuntu/AndroidStudioProjects/SchoolManagement/shared/src/commonMain/kotlin/com/example/schoolmanagement/App.kt)
- Add a new route `class/{classNo}` for the student list.
- Update `ClasspageScreen` to navigate to `class/{classNo}`.

## Verification Plan

### Automated Tests
- Build the project to ensure no compilation errors.

### Manual Verification
- Deploy to Android/Desktop.
- Navigate to "Teachers" and test the "Add Teacher" form.
- Navigate to "Classes", click a class (e.g., "Grade 1"), and verify that the student list loads.
- Try editing a roll number and saving.
