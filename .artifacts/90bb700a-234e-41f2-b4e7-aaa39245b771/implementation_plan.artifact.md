# Implementation Plan - Industry-Level Modern Dashboard

This plan outlines the steps to upgrade the School Management dashboard to a high-quality, Material 3-based design with interactive charts and time-range filtering.

## User Review Required
> [!IMPORTANT]
> The dashboard will now require a time-range selection (Today, Week, Month) which will trigger network reloads for the attendance trend data.

## Proposed Changes

### [Component] Core Data & Models
#### [NEW] [DashboardTimeRange.kt](file:///home/ubuntu/AndroidStudioProjects/SchoolManagement/shared/src/commonMain/kotlin/com/example/schoolmanagement/api/models/DashboardTimeRange.kt)
- Create an enum for filtering: `TODAY`, `WEEK`, `MONTH`.

#### [MODIFY] [DashboardRepository.kt](file:///home/ubuntu/AndroidStudioProjects/SchoolManagement/shared/src/commonMain/kotlin/com/example/schoolmanagement/api/DashboardRepository.kt)
- Update `fetchDashboardData(range: DashboardTimeRange)`:
    - `TODAY`: Fetch only today's attendance.
    - `WEEK`: Fetch last 7 days.
    - `MONTH`: Fetch last 30 days.
- Ensure date calculation correctly uses `kotlinx-datetime`.

### [Component] Presentation Layer
#### [MODIFY] [DashboardViewModel.kt](file:///home/ubuntu/AndroidStudioProjects/SchoolManagement/shared/src/commonMain/kotlin/com/example/schoolmanagement/presentation/dashboard/DashboardViewModel.kt)
- Introduce `selectedRange` StateFlow.
- Add `setTimeRange(range: DashboardTimeRange)` which updates the flow and reloads data.

#### [MODIFY] [DashboardScreen.kt](file:///home/ubuntu/AndroidStudioProjects/SchoolManagement/shared/src/commonMain/kotlin/com/example/schoolmanagement/presentation/dashboard/DashboardScreen.kt)
- **Top Bar / Header**: Add `SingleChoiceSegmentedButtonRow` for time range selection.
- **Layout**: Use `VerticalGrid` or adaptive columns for a better desktop/mobile experience.
- **Theming**: Use Material 3 `ElevatedCard` with custom surface tints for a premium look.

### [Component] UI Components (Charts)
#### [MODIFY] [BarChart.kt](file:///home/ubuntu/AndroidStudioProjects/SchoolManagement/shared/src/commonMain/kotlin/com/example/schoolmanagement/presentation/dashboard/components/BarChart.kt)
- Integrate `io.github.ehsannarmani:compose-charts` for Enrollment Distribution.
#### [MODIFY] [PieChart.kt](file:///home/ubuntu/AndroidStudioProjects/SchoolManagement/shared/src/commonMain/kotlin/com/example/schoolmanagement/presentation/dashboard/components/PieChart.kt)
- Integrate `io.github.ehsannarmani:compose-charts` for Gender Breakdown (Pie/Donut).
#### [MODIFY] [AreaChart.kt](file:///home/ubuntu/AndroidStudioProjects/SchoolManagement/shared/src/commonMain/kotlin/com/example/schoolmanagement/presentation/dashboard/components/AreaChart.kt)
- Integrate `io.github.ehsannarmani:compose-charts` for Attendance Trend.
#### [MODIFY] [StatCard.kt](file:///home/ubuntu/AndroidStudioProjects/SchoolManagement/shared/src/commonMain/kotlin/com/example/schoolmanagement/presentation/dashboard/components/StatCard.kt)
- Enhance design with `FontAwesomeIcons` and better visual indicators.

## Verification Plan

### Manual Verification
- Deploy to `desktopApp` and `androidApp`.
- Verify the "Today/Week/Month" toggle updates the charts.
- Check that the Gender Breakdown pie chart accurately reflects the student data.
- Verify the "Enrollment by Class" bar chart is interactive and visually appealing.

### Automated Tests
- Run Gradle assemble task to ensure no compilation errors with the new library.
