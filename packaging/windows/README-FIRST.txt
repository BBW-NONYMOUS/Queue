Queue-Clearance for Windows

Run Queue-Clearance.exe (or install the EXE/MSI first).
On first launch the application creates a safe configuration template at:
  %LOCALAPPDATA%/Queue-Clearance/app.properties
Logs, reports and other relative output paths are beside this configuration.

Exit the application and have campus IT set the host name, database credentials
and verified TLS trust. The template has no database password and backup is off.
Keep the configuration outside the installation; upgrades must preserve it.
No database password, truststore private key or backup secret is bundled here.

The installer does not create or upgrade MySQL. A DBA must apply V001 through
V008 and install the restricted database account separately before campus use.

Keep manifest.json with the release and verify SHA-256 hashes before deployment.
Test login/logout, students, queue calls, alerts, PDF/Excel, native printing and
TV full screen on the actual workstation. This release requires Windows and
bundles Java 21; a separate Java installation is not required on client PCs.
