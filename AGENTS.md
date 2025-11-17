# Agent Commit Guidelines

- Always allow git pre-commit hooks to run; never bypass them with flags such as `--no-verify`.
- Execute the full test suite before committing. If any test fails, fix it before attempting to commit again.
- Follow repository contribution standards and document any deviations encountered during development.
- Never wait too long on a command to return. Always use `gtimeout` with all commands to prevent hanging operations. Set appropriate timeout values based on the expected duration of the command.
- Before performing any `git commit` or `git push`, ensure a JIRA ticket number (e.g. `SCRUM-123`) has been explicitly provided for the work. If none has been provided, ask the user for the JIRA number and reference it in the commit message and/or description before proceeding.
