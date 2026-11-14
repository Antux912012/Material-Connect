# Security Policy & Vulnerability Disclosure (EU Cyber Resilience Act)

// SPDX-FileCopyrightText: 2026 Antonio De Gaetano <https://github.com/Antux912012>
// SPDX-License-Identifier: GPL-2.0-only OR GPL-3.0-only OR LicenseRef-KDE-Accepted-GPL

## Supported Versions

This project provides security updates for the latest release branch of Material KDE Connect.

| Version | Supported |
| ------- | --------- |
| 1.37.x  | Yes       |
| < 1.37  | No        |

## Security Architecture

1. **Transport Layer Security**: All device links enforce TLS 1.3 / TLS 1.2 mutual certificate authentication with SHA-256 certificate pinning after pairing.
2. **Protocol Framing Protection**: Incoming network packets enforce a strict byte-length ceiling in `BoundedLineReader` (`MAX_IDENTITY_PACKET_SIZE` and `MAX_PACKET_SIZE`) before JSON deserialization to prevent memory-exhaustion Denial-of-Service (DoS) attacks.
3. **Zero Silent Telemetry**: The application initiates zero outbound internet connections for analytics, crash reporting, or telemetry. All communications remain strictly on local networks (`LAN` / `Bluetooth`) with user-paired peers.

## Reporting a Vulnerability

Do not report security vulnerabilities in public GitHub issues.

1. Submit a private vulnerability report via the GitHub Security Advisory tab on the repository.
2. Include the protocol version, affected plugin name, and steps to reproduce the issue.
3. The maintainer will acknowledge receipt within 72 hours and publish a coordinated patch release.
