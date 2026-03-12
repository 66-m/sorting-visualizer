---
agent: agent
description: Create a meaningful branch and commit all pending changes, then advise on pushing.
---

Review the current state of the repository and commit all pending changes with a well-crafted message.

## Steps

1. **Inspect changes** — run `git status` and `git diff` (or `git diff --cached`) to understand exactly what has changed and why.

2. **Derive a branch name** — based on the nature of the changes, choose a concise, kebab-case branch name following the pattern `<type>/<short-description>`, e.g. `docs/modernize-readme` or `feat/add-3d-visualizations`. Use a standard type prefix: `feat`, `fix`, `docs`, `refactor`, `chore`, `test`, or `style`.

3. **Create and switch to the branch** — run:
   ```sh
   git checkout -b <branch-name>
   ```

4. **Stage all changes** — run:
   ```sh
   git add -A
   ```

5. **Write a commit message** — follow the Conventional Commits format:
   ```
   <type>(<optional scope>): <short imperative summary>

   <optional body — explain what changed and why, not how>
   ```
   Keep the subject line under 72 characters. Include a body only if the change needs context beyond what the subject conveys.

6. **Commit** — run:
   ```sh
   git commit -m "<message>"
   ```

7. **Advise on pushing** — do NOT push. Instead, tell the user the exact command to push and open a PR when they are ready:
   ```sh
   git push -u origin <branch-name>
   ```
