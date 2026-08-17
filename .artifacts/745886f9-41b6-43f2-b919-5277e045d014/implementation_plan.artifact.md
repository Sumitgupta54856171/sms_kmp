# Implementation Plan - Dashboard Data Fix & Mobile Scroller

Fix missing data on the Dashboard and resolve the scrolling issue on mobile devices.

## Proposed Changes

### Data Layer (API Integration)

#### [MODIFY] [DashboardRepository.kt](file:///home/ubuntu/AndroidStudioProjects/SchoolManagement/shared/src/commonMain/kotlin/com/example/schoolmanagement/api/DashboardRepository.kt)
- **Robust Parsing**: Update to handle both object-wrapped (`{ "body": [...] }`) and raw-array (`[...]`) responses for all endpoints.
- **Staff Data**: Call `/api/v1/teachers/all` to get the real faculty count.
- **Attendance Data**: Call `/api/v1/attendance/date/{today}` to calculate today's attendance percentage.
- **Fee Collection**: Call `/api/v1/fee/invoice/history` for both today and session-wide collection stats.
- **Enrollment**: Ensure the class-wise enrollment chart uses real data from `/api/v1/dashoard/get/enrollment/class`.

---

### UI Layer (Responsive & Mobile)

#### [MODIFY] [DashboardScreen.kt](file:///home/ubuntu/AndroidStudioProjects/SchoolManagement/shared/src/commonMain/kotlin/com/example/schoolmanagement/presentation/dashboard/DashboardScreen.kt)
- **Fix Mobile Scroller**:
    - Remove `Modifier.fillMaxSize()` from the scrollable `Column` inside `BoxWithConstraints`.
    - Ensure the parent container provides proper height constraints.
- **Optimization**:
    - Reduce padding on mobile for a better fit.
    - Ensure charts scale down correctly for narrow screens.

#### [MODIFY] [MainScaffold.kt](file:///home/ubuntu/AndroidStudioProjects/SchoolManagement/shared/src/commonMain/kotlin/com/example/schoolmanagement/presentation/MainScaffold.kt)
- Ensure the `content()` area correctly fills the available space in the `Scaffold` to allow child components to scroll.

## Verification Plan

### Manual Verification
- **Desktop**: Confirm all stats (Students, Teachers, Attendance, Fees) show non-zero/real values.
- **Mobile (Android Emulator)**:
    - Verify that the page is scrollable.
    - Confirm data is displayed (not just loading/empty).
- **Session Switching**: Switch session and verify that "Total Collection" and "Enrollments" update accordingly.
