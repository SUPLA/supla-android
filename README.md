# supla-android

> Part of **SUPLA** — an open smart home platform that brings together hardware manufacturers, the community, and users.  
> Learn more at https://www.supla.org

`supla-android` is the **official Android mobile application** for the SUPLA platform.
It is a client application that communicates with SUPLA server (using native API) and SUPLA Cloud using the public REST API.

---

## What is this repository?

This repository contains the source code of the SUPLA Android application distributed to end users.

The application is responsible for:

* user authentication,
* displaying devices, channels and their states,
* sending control commands to SUPLA server,
* receiving notifications,
* client-side UI and UX.

Automation logic and device communication are handled server-side.

---

## SUPLA architecture overview

SUPLA consists of multiple components that together form a complete smart home platform, including device firmware, server-side services, cloud applications, and client applications.

`supla-android` is a **client application** in this architecture:

* it communicates with SUPLA server and SUPLA Cloud,
* it does not communicate directly with devices,
* it does not contain server-side automation or device connectivity logic.

For a high-level overview of the SUPLA architecture and how individual repositories fit together, see:

👉 [https://github.com/SUPLA](https://github.com/SUPLA)

---

## Development

Development instructions, build steps and test execution are described in:

* [`Development.md`](Development.md)

---

## Contributing

Please read:

* [`CONTRIBUTING.md`](CONTRIBUTING.md)
* [`SECURITY.md`](SECURITY.md)

---

## Releases

Official application releases are distributed via Google Play.

Source code releases and tags are available on GitHub:
[https://github.com/SUPLA/supla-android/releases](https://github.com/SUPLA/supla-android/releases)

---

## License

This project is licensed under the **GPL-2.0** license.

