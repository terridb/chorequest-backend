## Endpoints

### Users

| Method | Path | Description | Status |
|---|---|---|---|
| POST | /users | Create user | planned |
| GET | /users/{id} | Get profile | planned |
| PATCH | /users/{id} | Update profile (name, avatar) | planned |
| GET | /users/{id}/households | List households the user belongs to | planned |

### Households

| Method | Path | Description | Status |
|---|---|---|---|
| POST | /households | Create household | planned |
| GET | /households/{id} | Get household details | planned |
| PATCH | /households/{id} | Update household (name, type, photo, city, zipcode) | planned |
| DELETE | /households/{id} | Delete household | planned |

### Household members

| Method | Path | Description | Status |
|---|---|---|---|
| POST | /households/{id}/members | Add a user to a household | planned |
| GET | /households/{id}/members | List members (e.g. leaderboard/points view) | planned |
| DELETE | /households/{id}/members/{memberId} | Remove a member | planned |

### Tasks

| Method | Path | Description | Status |
|---|---|---|---|
| POST | /households/{id}/tasks | Create a task in a household | planned |
| GET | /households/{id}/tasks | List tasks for a household | existing |
| GET | /tasks/{id} | Get a single task | planned |
| PATCH | /tasks/{id} | Update task (title, effort, frequency, points, etc.) | planned |
| DELETE | /tasks/{id} | Delete task | planned |

### Task assignments

| Method | Path | Description | Status |
|---|---|---|---|
| POST | /tasks/{id}/assignments | Assign a task to a household member with a due date | planned |
| GET | /tasks/{id}/assignments | Assignment history/current assignees for a task | planned |
| GET | /household-members/{id}/assignments | A member's assigned tasks | planned |
| PATCH | /assignments/{id} | Reassign / change due date | planned |
| DELETE | /assignments/{id} | Cancel an assignment | planned |

### Task completions

| Method | Path | Description | Status |
|---|---|---|---|
| POST | /assignments/{id}/completions | Mark an assignment done (awards points) | planned |
| GET | /household-members/{id}/completions | A member's completion history | planned |

Completions are not undoable — there is no delete endpoint for them.

### Leaderboard

| Method | Path | Description | Status |
|---|---|---|---|
| GET | /households/{id}/leaderboard | Members ranked by points | planned |