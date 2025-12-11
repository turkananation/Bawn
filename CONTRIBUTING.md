# 🤝 Contributing to Bawn

Thank you for your interest in contributing to **Bawn**. We are building the world's fastest Android digital fortress, and we need precise, high-quality engineering to maintain our "Zero Latency" standard.

This guide outlines the standard operating procedures (SOPs) for contributing to this repository. Please read it carefully before opening a Pull Request.

---

## 🌿 Branching Strategy

We follow a strict **Gitflow Workflow** to ensure stability while maintaining high development velocity.

### Core Branches
* **`main`**: The "Production" branch. Code here is stable, tested, and ready for deployment. **Direct commits are prohibited.**
* **`develop`**: The "Integration" branch. This is the default target for all Feature and Bugfix Pull Requests. It contains the latest development changes for the next release.

### Temporary Branches
All contributions must be done in a dedicated branch. We categorize branches by their purpose:

| Branch Type | Source | Merge Target | Purpose | Naming Convention |
| :--- | :--- | :--- | :--- | :--- |
| **Feature** | `develop` | `develop` | New features from the roadmap. | `feature/<ID>-<description-slug>` |
| **Bugfix** | `develop` | `develop` | Fixing bugs in the `develop` branch. | `bugfix/<ID>-<description-slug>` |
| **Release** | `develop` | `main` & `develop` | Preparing a new version release. | `release/v<version-number>` |
| **Hotfix** | `main` | `main` & `develop` | Critical production patches. | `hotfix/<ID>-<description-slug>` |

---

## 🏷️ Naming Conventions

We use strict naming conventions to link code changes directly to our **Engineering Roadmap** and **Issue Tracker**.

### 1. Feature Branches
Use the **Feature ID** from the [Engineering Roadmap](ENGINEERING_ROADMAP.md) or the GitHub Issue number.
* **Syntax:** `feature/<ID>-<short-description>`
* **Examples:**
    * `feature/12-uninstall-protection` (Implements Roadmap Item #12)
    * `feature/14-intruder-selfie` (Implements Roadmap Item #14)
    * `feature/settings-ui-refactor` (General improvements without a specific ID)

### 2. Bugfix Branches
Use the Issue number if available.
* **Syntax:** `bugfix/<issue-ID>-<short-description>` or `fix/...`
* **Examples:**
    * `bugfix/45-overlay-flicker`
    * `fix/crash-on-rotation`

### 3. Release & Hotfix Branches
* **Release:** `release/v1.2.0`
* **Hotfix:** `hotfix/security-patch-v1.1.1`

---

## 🛠️ Development Workflow

1.  **Select a Task:** Pick an item from the [Engineering Roadmap](README.md) or an open Issue.
2.  **Create a Branch:**
    ```bash
    git checkout develop
    git pull origin develop
    git checkout -b feature/12-uninstall-protection
    ```
3.  **Code:** Implement your changes. adhere to the **Visual Protocol** and **Security Critical Path** defined in the README.
4.  **Test:** Ensure the app builds and all unit tests pass.
5.  **Commit:** Use descriptive, imperative commit messages.
    * ✅ *Good:* "Add DeviceAdminReceiver for uninstall protection"
    * ❌ *Bad:* "Update code" or "Fixed stuff"

---

## 🚀 Pull Request Process

1.  **Push your branch** to the repository.
2.  **Open a Pull Request** against the **`develop`** branch.
3.  **Fill out the PR Template:** Clearly describe *what* you changed and *why*.
4.  **Link Issues:** Use magic words like "Closes #12" to auto-close roadmap items.
5.  **Review:** Wait for a code review from a maintainer. Be ready to make requested changes.
6.  **Squash & Merge:** Once approved, your PR will be squashed and merged into `develop`.

### ⚠️ Important Rules
* **Never** merge into `main` directly.
* **Never** commit secrets (API keys, keystore passwords).
* **Always** rebase on `develop` before opening your PR to avoid conflicts.

---

## 🎨 Style & Standards

* **Kotlin:** Follow the official [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html).
* **Compose:** Use `CamelCase` for Composable functions.
* **Theme:** Strictly use the colors defined in `LocalNeonColors`. Do not hardcode hex values in UI components.
* **Security:** Any change touching `UserSecurityDao` or `RestrictionHelper` requires a secondary security review.

---

> **Note:** By contributing to Bawn, you agree that your code will be licensed under the MIT License used by the project.