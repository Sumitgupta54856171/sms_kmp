# Walkthrough - Academic Session Management

I have integrated Academic Session management into the application. Users can now view and switch between school sessions, and the application automatically handles the session context in all API calls.

## Key Implementation Details

### 1. Networking & Headers
- **[KtorClient.kt](file:///home/ubuntu/AndroidStudioProjects/SchoolManagement/shared/src/commonMain/kotlin/com/example/schoolmanagement/api/KtorClient.kt)**: Updated to automatically include the `X-Session-Id` header in every request if a session ID is stored.
- **[AuthRepository.kt](file:///home/ubuntu/AndroidStudioProjects/SchoolManagement/shared/src/commonMain/kotlin/com/example/schoolmanagement/auth/AuthRepository.kt)**: Now captures the initial `sessionId` from the login response and saves it for subsequent use.

### 2. Session Logic & Persistence
- **[TokenManager.kt](file:///home/ubuntu/AndroidStudioProjects/SchoolManagement/shared/src/commonMain/kotlin/com/example/schoolmanagement/auth/TokenManager.kt)**: Added persistent storage for the `current_session_id`.
- **[SessionViewModel.kt](file:///home/ubuntu/AndroidStudioProjects/SchoolManagement/shared/src/commonMain/kotlin/com/example/schoolmanagement/presentation/session/SessionViewModel.kt)**:
    - Fetches available sessions from the backend.
    - Manages the switching logic.
    - Triggers a data refresh (e.g., reloading the Dashboard) whenever the session is changed.

### 3. UI: Session Switcher
- **[SessionSwitcher.kt](file:///home/ubuntu/AndroidStudioProjects/SchoolManagement/shared/src/commonMain/kotlin/com/example/schoolmanagement/presentation/components/SessionSwitcher.kt)**: A professional dropdown component placed in the Sidebar header.
- **[Sidebar.kt](file:///home/ubuntu/AndroidStudioProjects/SchoolManagement/shared/src/commonMain/kotlin/com/example/schoolmanagement/presentation/components/Sidebar.kt)**: Now displays the active session right below the school name.

## Verification

- **Build Status**: `SUCCESS` (:androidApp:assembleDebug)
- **Header Injection**: Verified that `X-Session-Id` is correctly injected into the Ktor client configuration.
- **Session Switching**: Switching a session updates the local storage and triggers the `onSessionChanged` callback to refresh UI data.

## How to Test
1. Run the app and log in.
2. Open the Sidebar.
3. You will see the "Active Session" section below the logo.
4. Click on it to open the dropdown and select a different academic session.
5. Notice that the Dashboard data refreshes automatically to reflect the new session.
