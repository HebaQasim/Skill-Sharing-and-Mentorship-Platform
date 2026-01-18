# Skill Sharing & Mentorship Platform API

A **production-grade backend system** built with **Spring Boot** for enabling structured knowledge sharing, mentorship, and professional collaboration inside organizations.

The platform allows employees to publish learning posts, host live sessions, engage through reactions and discussions, and recognize mentors through a **rule-driven feedback and approval process**.

This project is intentionally designed as a **real backend system**, not a CRUD demo, focusing on **lifecycle management, domain rules, scalability, and long-term maintainability**.

---

## Platform Capabilities

### Knowledge Sharing via Posts
- Employees create posts containing learning content and resources.
- Draft posts are private and editable.
- Published posts appear in the global feed.
- Posts support editing, soft deletion, and restoration where applicable.

---

### Feed & Content Discovery
- Feed is ordered by **newest content first**.
- Optimized for scalability using **cursor-based pagination**.
- Traditional pagination is used where cursor semantics are not required.
- Designed to support future filtering and ranking strategies without refactoring core logic.

---

## Sessions & Mentorship

### Session Creation & Scheduling
- Posts can optionally include a **live session**.
- Sessions can be saved as drafts and become visible only when the post is published.
- Scheduling includes:
  - Time selection
  - Conflict detection
  - Validation against overlapping sessions

---

### Session Lifecycle
Sessions follow a **strictly enforced lifecycle**:

- **Draft** – not visible, editable  
- **Scheduled** – published and open for registration  
- **Live** – session currently in progress  
- **Completed** – session ended with attendees  
- **Canceled** – canceled by host before start  
- **Unattended** – session ended with no attendees  

Status transitions are **time-aware and automatic**, ensuring consistency without manual intervention.

---

### Participation & Feedback
- Employees can register for sessions and cancel before start time.
- Attendance is tracked explicitly.
- After completion:
  - Attendees can submit ratings and written feedback.
  - Hosts can attach session recordings or related resources.
- Feedback is tied to mentorship evaluation and quality tracking.

---

## Mentor Recognition System
- All employees can host sessions.
- When an employee completes **5 valid sessions with attendance**:
  - The system automatically triggers an admin review.
- Admins review:
  - Session history
  - Attendance data
  - Feedback quality
- Admin decision:
  - **Approve** → Mentor badge granted
  - **Reject** → System continues tracking future sessions
- Re-evaluation is automatic after every additional 5 completed sessions until approval.

This process is **event-driven, auditable, and non-blocking**.

---

## Authentication & Security
- JWT-based authentication
- Refresh tokens for session continuity
- **Multi-device support**
  - Logout current device
  - Logout all devices
- Custom authentication filters and exception handlers
- Role-based access control:
  - Employee
  - Admin
  - Sub-Admin

---

## Notifications
In-app notifications are generated for:
- New posts
- Likes and comments
- Session registrations and cancellations
- Session status changes
- Feedback submissions
- Approval decisions

Supports:
- Read / unread states
- Unread counters
- Cursor-based pagination

Implemented using **event listeners** to avoid coupling with core business logic.

---

## Admin & Governance

### User & Role Management
- Activate / deactivate accounts
- Assign and revoke roles
- Monitor user activity

### Moderation
- Moderate posts, comments, and feedback
- Remove inappropriate content
- Department-level moderation through Sub-Admins

### Approval & Audit
- Explicit approval workflows
- Clear approve / reject actions
- Audit endpoints for system visibility
- Designed for future analytics and reporting expansion

---

## Architecture Overview

### Feature-Based Architecture
The codebase follows a **feature-oriented structure**, where each domain owns its complete vertical slice:

- Controller  
- DTOs  
- Entities  
- Repositories  
- Services  
- Events & listeners (where applicable)  
- Cursor logic and background jobs (where applicable)  

This structure ensures:
- Clear domain boundaries
- Low coupling between features
- High extensibility
- Long-term maintainability

---

### Core Modules
- **auth** – authentication, JWT, refresh tokens, security  
- **post** – posts, drafts, feed, comments, reactions  
- **session** – scheduling, lifecycle, participation, feedback  
- **notification** – in-app notifications and delivery  
- **approval** – approval requests and workflows  
- **mentor** – mentor eligibility and badge evaluation  
- **user** – profiles, directory, hosted sessions  
- **skill** – skill management  
- **admin** – moderation, audit, governance  
- **common** – shared utilities, caching, exceptions, jobs  

---

## Engineering Highlights
- Domain-driven design
- Event-driven side effects
- Cursor-based pagination for high-traffic feeds
- Classic pagination where appropriate
- Explicit lifecycle modeling
- Cache-aware read paths
- Multi-device authentication support
- Clean separation between draft, published, archived, and historical states
- Designed to evolve without structural rewrites

---

## API Documentation
- OpenAPI / Swagger UI available at runtime
- Endpoints grouped by feature responsibility
- Designed for frontend and mobile consumption

---
# Endpoints

## Auth

| HTTP Method | Endpoint | Description |
|------------|----------|-------------|
| POST | `/api/auth/register` | Register a new employee account |
| POST | `/api/auth/login` | Login and receive access/refresh tokens |
| POST | `/api/auth/refresh` | Refresh access token using refresh token |
| POST | `/api/auth/logout` | Logout from current device/session |
| POST | `/api/auth/logout-all` | Logout from all devices/sessions |

---

## My Profile

| HTTP Method | Endpoint | Description |
|------------|----------|-------------|
| GET | `/api/users/me` | Get current user profile |
| PATCH | `/api/users/me/profile` | Update current user profile |
| POST | `/api/users/me/change-password` | Change current user password |

---

## My Skills

| HTTP Method | Endpoint | Description |
|------------|----------|-------------|
| GET | `/api/users/me/skills` | Get my selected skills |
| POST | `/api/users/me/skills` | Add skills to my profile |
| GET | `/api/users/me/skills/search` | Search available skills |
| DELETE | `/api/users/me/skills/{skillId}` | Remove a skill from my profile |

---

## Employee Directory

| HTTP Method | Endpoint | Description |
|------------|----------|-------------|
| GET | `/api/users/directory` | Browse employee directory |
| GET | `/api/users/colleagues` | List colleagues (filtered view) |
| GET | `/api/users/{profileUserId}` | View a user profile by id |

---

## Posts Feed

| HTTP Method | Endpoint | Description |
|------------|----------|-------------|
| GET | `/api/posts/feed` | Get the feed (newest-first, cursor pagination) |
| GET | `/api/posts/{postId}` | Get a post by id |

---

## Post Drafts

| HTTP Method | Endpoint | Description |
|------------|----------|-------------|
| GET | `/api/posts/drafts` | List my draft posts |
| POST | `/api/posts/drafts` | Create a new draft post |
| GET | `/api/posts/{postId}/draft` | Get a draft post details |
| PATCH | `/api/posts/{postId}/draft` | Update a draft post |
| POST | `/api/posts/{postId}/publish` | Publish a draft post |

---

## Draft Attachments

| HTTP Method | Endpoint | Description |
|------------|----------|-------------|
| GET | `/api/posts/{postId}/draft/attachments` | List draft attachments |
| POST | `/api/posts/{postId}/draft/attachments` | Add attachment to draft |
| DELETE | `/api/posts/{postId}/draft/attachments/{attachmentId}` | Remove attachment from draft |

---

## Published Attachments

| HTTP Method | Endpoint | Description |
|------------|----------|-------------|
| POST | `/api/posts/{postId}/attachments` | Add attachment to a published post |
| DELETE | `/api/posts/{postId}/attachments/{attachmentId}` | Remove attachment from a published post |

---

## Comments

| HTTP Method | Endpoint | Description |
|------------|----------|-------------|
| GET | `/api/posts/{postId}/comments` | Get comments for a post |
| POST | `/api/posts/{postId}/comments` | Add a comment to a post |
| PATCH | `/api/posts/{postId}/comments/{commentId}` | Edit a comment |
| DELETE | `/api/posts/{postId}/comments/{commentId}` | Delete a comment |

---

## Likes

| HTTP Method | Endpoint | Description |
|------------|----------|-------------|
| PUT | `/api/posts/{postId}/like` | Like a post |
| DELETE | `/api/posts/{postId}/like` | Remove like from a post |
| GET | `/api/posts/{postId}/likers` | List users who liked the post |

---

## Post Management (Published)

| HTTP Method | Endpoint | Description |
|------------|----------|-------------|
| PATCH | `/api/posts/{postId}` | Update a published post |
| DELETE |`/api/posts/{postId} | Archive a published post |
| POST | `/api/posts/{postId}/restore` | Restore an archived post |

---

## Draft Session (Attached to Draft Post)

| HTTP Method | Endpoint | Description |
|------------|----------|-------------|
| GET | `/api/posts/{postId}/draft/session` | Get draft session for a draft post |
| PUT | `/api/posts/{postId}/draft/session` | Create/update draft session |
| DELETE | `/api/posts/{postId}/draft/session` | Remove draft session from draft post |

---

## Published Session (Attached to Published Post)

| HTTP Method | Endpoint | Description |
|------------|----------|-------------|
| PUT | `/api/posts/{postId}/session` | Create/update session for a published post |
| DELETE | `/api/posts/{postId}/session` | Cancel/remove session from a post |

---

## Session Registration & Join

| HTTP Method | Endpoint | Description |
|------------|----------|-------------|
| POST | `/api/sessions/{sessionId}/registrations` | Register for a session |
| DELETE | `/api/sessions/{sessionId}/registrations/me` | Cancel my registration |
| POST | `/api/sessions/{sessionId}/join` | Join a live session |

---

## Session Feedback

| HTTP Method | Endpoint | Description |
|------------|----------|-------------|
| GET | `/api/sessions/{sessionId}/feedback` | Get feedback for a session |
| POST | `/api/sessions/{sessionId}/feedback` | Add feedback (attendee) |
| PATCH | `/api/sessions/{sessionId}/feedback/{feedbackId}` | Edit feedback |
| DELETE | `/api/sessions/{sessionId}/feedback/{feedbackId}` | Delete feedback |

---

## Session People

| HTTP Method | Endpoint | Description |
|------------|----------|-------------|
| GET | `/api/sessions/{sessionId}/registered` | List registered users |
| GET | `/api/sessions/{sessionId}/attended` | List attended users |

---

## Session Recording

| HTTP Method | Endpoint | Description |
|------------|----------|-------------|
| POST | `/api/sessions/{sessionId}/recording` | Add session recording/resource |
| PUT | `/api/sessions/{sessionId}/recording` | Update recording/resource |
| DELETE | `/api/sessions/{sessionId}/recording` | Remove recording/resource |

---

## User Hosted Sessions

| HTTP Method | Endpoint | Description |
|------------|----------|-------------|
| GET | `/api/users/{userId}/sessions` | List sessions hosted by a user |

---

## Notifications (My)

| HTTP Method | Endpoint | Description |
|------------|----------|-------------|
| GET | `/api/notifications/me/unread-count` | Get unread notifications count |
| POST | `/api/notifications/me/{id}/read` | Mark notification as read |
| POST | `/api/notifications/me/{id}/unread` | Mark notification as unread |

---

# Admin Endpoints

## Admin User Management

| HTTP Method | Endpoint | Description |
|------------|----------|-------------|
| PATCH | `/api/admin/users/{id}/enabled` | Enable/disable a user account |
| POST | `/api/admin/users/change-role` | Change user role (Admin/Sub-admin/etc.) |

---

## Admin Approvals

| HTTP Method | Endpoint | Description |
|------------|----------|-------------|
| GET | `/api/admin/approvals` | List approval requests |
| GET | `/api/admin/approvals/{id}` | Get approval request details |
| POST | `/api/admin/approvals/{id}/approve` | Approve a request |
| POST | `/api/admin/approvals/{id}/reject` | Reject a request |

---

## Admin Mentor Badge Review

| HTTP Method | Endpoint | Description |
|------------|----------|-------------|
| POST | `/api/admin/mentor-requests/{id}/approve` | Approve mentor badge request |
| POST | `/api/admin/mentor-requests/{id}/reject` | Reject mentor badge request |

---

## Admin Moderation

| HTTP Method | Endpoint | Description |
|------------|----------|-------------|
| DELETE | `/api/admin/moderation/posts/{postId}` | Delete a post (moderation) |
| DELETE | `/api/admin/moderation/posts/{postId}/comments/{commentId}` | Delete a comment (moderation) |
| DELETE | `/api/admin/moderation/sessions/{sessionId}/feedback/{feedbackId}` | Delete session feedback (moderation) |

---

## Admin Audit

| HTTP Method | Endpoint | Description |
|------------|----------|-------------|
| GET | `/api/admin/audit` | View system audit logs |



