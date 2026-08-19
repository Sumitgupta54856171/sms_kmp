# Implementation Plan - Attendance System Enhancements

Implement "In-Time" recording for student attendance and restructure the `AttendanceScreen` for a more premium experience in both the KMP app and the React frontend.

## User Review Required

> [!IMPORTANT]
> The "In-Time" field will be added to the attendance records. When a student is marked as "Present", the current system time will be automatically recorded as their "In-Time" unless manually overridden.

## Proposed Changes

### Shared Module (KMP)

#### [MODIFY] [AttendanceModels.kt](file:///home/ubuntu/AndroidStudioProjects/SchoolManagement/shared/src/commonMain/kotlin/com/example/schoolmanagement/api/models/AttendanceModels.kt)
- Add `inTime: String?` to `AttendanceRecord` and `AttendancePayload`.

#### [MODIFY] [AttendanceViewModel.kt](file:///home/ubuntu/AndroidStudioProjects/SchoolManagement/shared/src/commonMain/kotlin/com/example/schoolmanagement/presentation/attendance/AttendanceViewModel.kt)
- Fix `_selectedClass` initialization.
- Update `AttendanceStudentRow` to include `inTime`.
- Update `updateStatus` to automatically set `inTime` when status is "present".
- Update `loadAttendanceData` to correctly map `inTime` from the repository.

#### [MODIFY] [AttendanceScreen.kt](file:///home/ubuntu/AndroidStudioProjects/SchoolManagement/shared/src/commonMain/kotlin/com/example/schoolmanagement/presentation/attendance/AttendanceScreen.kt)
- Restructure the UI with a more premium design:
    - Improved header with better iconography.
    - Enhanced stats cards with gradients or better styling.
    - Updated `AttendanceRow` to display `inTime` next to the status.
    - Add a way to manually edit the time if needed (or just show it).

---

### React Frontend (sms_frontend)

#### [MODIFY] [attendance.ts](file:///home/ubuntu/AndroidStudioProjects/SchoolManagement/sms_frontend/src/api/attendance.ts)
- Add `inTime?: string` to `AttendanceRecord`, `AttendancePayload`, and `AttendanceRecordWithGender`.
- Update mappers to handle `inTime`.
- Fix the default status bug (remove `?? "absent"` default).

#### [MODIFY] [Attendence.tsx](file:///home/ubuntu/AndroidStudioProjects/SchoolManagement/sms_frontend/src/components/Attendence.tsx)
- Update `StudentRow` interface to include `inTime`.
- Update `handleStatusChange` to set `inTime` automatically when marking "present".
- Update the table to show an "In Time" column.
- Restructure the UI to match the premium theme.

## Verification Plan

### Automated Tests
- Build the shared module to ensure no compilation errors in KMP.
- Run `npm run build` in `sms_frontend` to check for TypeScript errors.

### Manual Verification
- Deploy the KMP app and verify that marking a student as "Present" shows the current time.
- Open the React dashboard and verify the new "In Time" column in the Attendance component.
- Verify that saving attendance correctly sends the `inTime` in the payload.
