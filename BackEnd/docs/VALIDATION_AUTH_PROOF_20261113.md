# Google linking proof — 13/11/2026

When the app Bearer is valid but the submitted Google proof is invalid, return
400 so clients keep the current email session. Missing/expired app credentials
and initial Google login continue returning 401. No schema/migration changes.

Validation: 27 isolated backend tests passed (Google/Groq mocked; SQLite memory).
This is a follow-up to S1-BL03-BE; the 32 published task commits stay unchanged.
