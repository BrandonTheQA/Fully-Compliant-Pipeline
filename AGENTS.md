# Agent Commit Guidelines

- Always allow git pre-commit hooks to run; never bypass them with flags such as `--no-verify`.
- Execute the full test suite before committing. If any test fails, fix it before attempting to commit again.
- Follow repository contribution standards and document any deviations encountered during development.
- Never wait too long on a command to return. Always use `gtimeout` with all commands to prevent hanging operations. Set appropriate timeout values based on the expected duration of the command.
- Before performing any `git commit` or `git push`, ensure a JIRA ticket number (e.g. `SCRUM-123`) has been explicitly provided for the work. If none has been provided, ask the user for the JIRA number and reference it in the commit message and/or description before proceeding.

## Commit Message Format

The repository enforces a specific commit message format via pre-commit hooks. All commit messages MUST follow this format:

```
SCRUM-{number} | {Committer Name} | {Description}
```

**Example:**
```
SCRUM-2 | Brandon Garlock | Update UI version from 0.0.2 to 2.0.0
```

**Format Requirements:**
- Start with the JIRA ticket number (e.g., `SCRUM-123`)
- Followed by a pipe separator `|`
- Then the committer's full name (use `git config user.name` to get the correct name)
- Another pipe separator `|`
- Finally, a descriptive message about the changes

**Getting the Committer Name:**
Before committing, retrieve the committer name using:
```bash
git config user.name
```

**Note:** The pre-commit hook will reject commits that don't follow this exact format. The commit message title (first line) must match this pattern.
