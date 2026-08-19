# Walkthrough - Student Management Screen

I have successfully implemented the **Student Management Screen**, fully integrated with your backend and consistent with your web design.

## Features Implemented

### 1. Responsive Student List
- **[StudentScreen.kt](file:///home/ubuntu/AndroidStudioProjects/SchoolManagement/shared/src/commonMain/kotlin/com/example/schoolmanagement/presentation/students/StudentScreen.kt)**:
    - **Desktop**: A professional data table showing Student Name, Scholar Number, Father's Name, and Status.
    - **Mobile**: Automatically switches to an optimized card-based layout for better readability on smaller screens.
    - **Search**: Integrated a real-time search bar that filters by name or scholar number.

### 2. Data Integration (Live)
- **[StudentRepository.kt](file:///home/ubuntu/AndroidStudioProjects/SchoolManagement/shared/src/commonMain/kotlin/com/example/schoolmanagement/api/StudentRepository.kt)**: Fetches the live student list from `/api/v1/students/studentlist`.
- **[StudentViewModel.kt](file:///home/ubuntu/AndroidStudioProjects/SchoolManagement/shared/src/commonMain/kotlin/com/example/schoolmanagement/presentation/students/StudentViewModel.kt)**: Manages the student list state and handles the filtering logic.

### 3. Application Integration
- Updated **[App.kt](file:///home/ubuntu/AndroidStudioProjects/SchoolManagement/shared/src/commonMain/kotlin/com/example/schoolmanagement/App.kt)** to include the new repository and view model, and mapped the `"students"` route to the new screen.

## Verification

- **Build Status**: `SUCCESS` (:androidApp:assembleDebug)
- **Search Logic**: Verified that typing in the search bar correctly filters the students.
- **Responsiveness**: Verified that the UI switches between Table (Wide) and Cards (Narrow) correctly.

## How to Test
1. Run the app on Android or Desktop.
2. Login and click on "Students" in the Sidebar.
3. Observe the live list of students.
4. Try searching for a specific student name in the search bar.
