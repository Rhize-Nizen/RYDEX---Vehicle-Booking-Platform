<div align="center">

# ⚡ RYDEX
### Autonomous Geospatial Mobility & Driver Dispatch Platform
**Engineered for the Bhilai–Durg–Raipur Transit Corridor (Chhattisgarh Grid)**

[![Java](https://img.shields.io/badge/Java-17%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![JavaScript](https://img.shields.io/badge/JavaScript-ES6+-F7DF1E?style=for-the-badge&logo=javascript&logoColor=black)](https://developer.mozilla.org/en-US/docs/Web/JavaScript)
[![Leaflet](https://img.shields.io/badge/Leaflet-1.9.4-199900?style=for-the-badge&logo=leaflet&logoColor=white)](https://leafletjs.com/)
[![License: MIT](https://img.shields.io/badge/License-MIT-6366F1?style=for-the-badge)](LICENSE)
[![Architecture: Zero Dependency](https://img.shields.io/badge/Dependencies-Zero%20External%20Jars-06B6D4?style=for-the-badge)](#technology-stack)

<p align="center">
  <a href="#key-features">Key Features</a> •
  <a href="#system-architecture">System Architecture</a> •
  <a href="#dsa-implementation">DSA Concepts</a> •
  <a href="#fleet-matrix">Fleet Matrix</a> •
  <a href="#installation--execution">Getting Started</a> •
  <a href="#api-specifications">API Specs</a> •
  <a href="#team-roles">Team Roles</a>
</p>

---

</div>

## 📌 Executive Summary

**Rydex** is a lightweight, high-throughput vehicle booking and dispatch platform tailored for regional transit corridors across Chhattisgarh. Conventional multinational ride-hailing services suffer from heavy client-side resource overhead, proprietary API key dependencies, and arbitrary surge multipliers in tier-2 urban corridors. 

Rydex resolves this by pairing a **zero-dependency, native multithreaded Java HTTP backend** with a **Cyber-Minimalist OLED single-page interface**. The platform enforces strict regional geofencing, calculates exact road driving turn distances via OSRM, matches the closest CG-07 registered captain using Euclidean spatial minimization, animates live vehicle dispatch, and issues dynamic UPI QR payment terminals alongside itemized GST invoices.

---

## 🚀 Key Features

* **Cyber-Minimalist OLED Design System:** Built on a true pitch-black canvas (`#000000` to `#0A0A0F`) with ambient neon radial lighting, glassmorphic translucent Bento Grid cards (`backdrop-filter: blur`), and high-contrast geometric typography.
* **Two-Stage Single-Page Architecture (SPA):**
  * **Stage 1 (Landing & Fleet Initialization):** Hero title (`top 1/3`), center-aligned Unified Platform Architecture Bento Overview (`middle 1/3`), and interactive 4-tier vehicle selector (`bottom 1/3`).
  * **Stage 2 (Map & Command Deck):** Seamless transition into a `3/4` Google Satellite Hybrid viewport paired with a `1/4` bottom location console (Pickup on left, live telemetry in center, Destination on right).
* **Strict Regional Geofencing (Axis-Aligned Bounding Box):** Hard-locks map panning, zooming, marker placement, and autocomplete searches strictly within the state of Chhattisgarh (`17.75° N - 24.15° N`, `80.20° E - 84.45° E`). Out-of-bounds queries are automatically rejected.
* **Live Address Autocomplete & Pinning:** Real-time localized address suggestions powered by the Photon Geocoding API, paired with manual crosshair map-clicking and draggable markers.
* **Exact Road Pathfinding & Zero-Discrepancy Fares:** Leverages Project OSRM to calculate genuine street network geometry and vehicular turn distances. Synchronizes mileage between frontend cards and backend calculations to eliminate fare discrepancies down to the decimal point.
* **Live Animated Driver Dispatch (Ola / Rapido Style):** On booking confirmation, spawns the assigned captain's pulsing vehicle marker on the satellite map and animates their real-time approach along a dashed amber route toward the pickup point with an active ETA countdown.
* **Interactive UPI QR Terminal & Digital GST Invoice:** Generates dynamic, scannable UPI QR codes (`upi://pay`) pre-filled with the exact metered amount, an itemized CGST/SGST tax breakdown, a payment simulator, and one-click PDF/print export.
* **Advance Ride Scheduling & Cancellation Workflow:** Commuters can reserve future transit slots or cancel active dispatch runs with cancellation reason telemetry.
* **Local Telemetry Ledger:** Tracks and caches chronological trip records in browser `localStorage` for offline recall and rebooking.

---

## 🏛️ System Architecture

```
+---------------------------------------------------------------------------------------+
|                                    CLIENT TIER                                        |
|   HTML5 • Vanilla ES6+ JS • Leaflet.js • Google Satellite Hybrid • Cyber-OLED CSS3    |
+-------------------------------------------+-------------------------------------------+
                                            |  JSON over REST (Asynchronous Fetch API)
                                            v
+---------------------------------------------------------------------------------------+
|                               SERVER TIER (PORT 8081)                                 |
|      Native Java HttpServer (com.sun.net.httpserver) • Multithreaded Request Worker   |
|                                                                                       |
|   +-------------------------------+               +-------------------------------+   |
|   |       RydexServer.java        |               |      VehicleManager.java      |   |
|   | - Static Asset Streamer       | <-----------> | - Haversine Distance Engine   |   |
|   | - Custom JSON Parser/Lexer    |               | - Dynamic 4-Tier Pricing Model|   |
|   | - REST API Endpoints & CORS   |               | - Nearest-Driver Allocator    |   |
|   +-------------------------------+               | - 4-Digit OTP Generator       |   |
|                                                   +-------------------------------+   |
|                                                                   |                   |
|           +-------------------------------------------------------+                   |
|           v                                                                           |
|   +-------------------------------------------------------------------------------+   |
|   |                             IN-MEMORY STORAGE                                 |   |
|   | - ConcurrentHashMap<String, Booking> (Thread-Safe O(1) Session Store)         |   |
|   | - ArrayList<Driver> (Active CG-07 Fleet Repository)                           |   |
|   +-------------------------------------------------------------------------------+   |
+-------------------------------------------+-------------------------------------------+
                                            |
                    +-----------------------+-----------------------+
                    |                                               |
                    v                                               v
    +-------------------------------+               +-------------------------------+
    |       Project OSRM API        |               |      Photon Geocoder API      |
    | (Turn-by-Turn Driving Route,  |               |  (Elasticsearch-Powered OSM   |
    |  Distance, Transit ETA)       |               |   Autocomplete Bounded to CG) |
    +-------------------------------+               +-------------------------------+
```

---

## 📊 Fleet Specification & Pricing Matrix

All rides are metered using a transparent pricing formula with a **0.0% surge policy**:

$$\text{Fare} = \left[ \text{Base Fare} + (\text{Distance}_{\text{km}} \times \text{Rate}_{\text{km}}) + (\text{Duration}_{\text{min}} \times \text{Rate}_{\text{min}}) \right] \times 1.05 \text{ (5% GST)}$$

| Vehicle Tier | Glyph | Base Fare | Per-KM Rate | Per-Min Rate | Capacity | Use Case Profile |
|:---|:---:|:---:|:---:|:---:|:---:|:---|
| **Rydex Bike** | 🏍️ | ₹20.00 | ₹7.50 | ₹1.00 | 1 Rider | Rapid transit slicing through Supela & GE Road congestion |
| **CG Auto** | 🛺 | ₹30.00 | ₹11.00 | ₹1.50 | 3 Riders | Traditional 3-wheeler with standardized digital metering |
| **Car 4 Seater** | 🚗 | ₹50.00 | ₹14.00 | ₹2.00 | 4 Riders | Compact hatchback & sedan AC fleet for comfortable urban travel |
| **7 Seater SUV** | 🚐 | ₹80.00 | ₹20.00 | ₹3.00 | 7 Riders | Premium Ertiga & Innova Prime fleet for group & airport drops |

---

## 🧠 Data Structures & Algorithms (DSA) Implementation

Rydex emphasizes fundamental computer science concepts within its domain logic:

### 1. Dynamic Arrays & Waypoint Traversal
* **Fleet Repository (`ArrayList<Driver>`):** Holds contiguous memory blocks of active drivers. Adding drivers operates in amortized $O(1)$ time, optimizing CPU cache locality during fleet iteration.
* **Coordinate Matrices (`double[][]`):** Receives and iterates through sequential polyline coordinates from OSRM to render routes and step the moving driver marker in $O(W)$ time (where $W$ is total waypoints).
* **LIFO Telemetry Queue:** Browser history applies JavaScript's `.unshift()` to enforce Last-In, First-Out insertion capped at 25 items ($O(1)$ amortized).

### 2. Hashing & Thread Safety
* **Concurrent Hash Maps (`ConcurrentHashMap<String, Booking>`):** Used in `VehicleManager.java` for live booking management. Provides average $O(1)$ lookup and insertion. Employs bucket-level locking and Compare-And-Swap (CAS) instructions to eliminate race conditions without locking the entire table.
* **Associative Parsing (`HashMap<String, String>`):** Powers the zero-dependency JSON parser, mapping HTTP request keys to string tokens in $O(1)$ time.

### 3. Spatial Optimization & Nearest-Neighbor Search
* **Nearest-Captain Linear Scan:** When a ride is booked, the algorithm iterates over the active driver fleet in $O(N)$ time, filtering drivers matching the requested `VehicleType` and minimizing Euclidean coordinate distance:

$$\min_{d \in \text{Fleet}} \left\{ \sqrt{(\text{lat}_d - \text{lat}_p)^2 + (\text{lng}_d - \text{lng}_p)^2} \;\middle|\; d.\text{type} = \text{TargetType} \right\}$$

### 4. Mathematical Modeling & Spatial Geometry
* **Haversine Great-Circle Formula:** Evaluates spherical curvature distance between coordinate pairs as an offline fallback model:

$$a = \sin^2\left(\frac{\Delta \phi}{2}\right) + \cos(\phi_1)\cos(\phi_2)\sin^2\left(\frac{\Delta \lambda}{2}\right), \quad d = 2R \cdot \operatorname{atan2}\left(\sqrt{a}, \sqrt{1-a}\right)$$

* **Axis-Aligned Bounding Box (AABB) Geofencing:** Executes constant-time ($O(1)$) spatial clipping:

$$\text{isInsideCG}(\text{lat}, \text{lng}) = (17.75 \le \text{lat} \le 24.15) \land (80.20 \le \text{lng} \le 84.45)$$

### Complexity Analysis Summary

| Algorithm / Routine | Primary Structure | Time Complexity | Auxiliary Space |
|:---|:---|:---:|:---:|
| **Driver Selection** | `ArrayList<Driver>` scan | $O(N)$ | $O(1)$ |
| **Session Registry** | `ConcurrentHashMap<String, Booking>` | $O(1)$ avg / $O(\log B)$ worst | $O(B)$ |
| **Haversine Distance** | Trigonometric formula | $O(1)$ | $O(1)$ |
| **Geofence Bounding Check**| AABB conditional clipping | $O(1)$ | $O(1)$ |
| **Custom JSON Parsing** | String tokenization | $O(L)$ | $O(K)$ |
| **Driver Polyline Animation**| Coordinate matrix iteration | $O(W)$ | $O(W)$ |

---

## 🛠️ Technology Stack Breakdown

| Layer | Technology | Purpose & Justification |
|:---|:---|:---|
| **Backend Runtime** | **Java (JDK 17+)** | Robust, statically typed, platform-independent server core. |
| **HTTP Engine** | **`com.sun.net.httpserver`** | Native multithreaded HTTP server avoiding heavy frameworks like Spring Boot. |
| **Client UI** | **HTML5 / CSS3 / ES6+** | Native browser tech adhering to pure OLED Cyber-Minimalist design principles. |
| **GIS Engine** | **Leaflet.js (v1.9.4)** | Lightweight (~42 KB) open-source canvas for rendering interactive vector overlays. |
| **Map Imagery** | **Google Satellite Hybrid** | High-resolution aerial terrain combined with street networks and landmarks. |
| **Routing Engine** | **Project OSRM** | Computes street-level turning paths, travel durations, and road distances. |
| **Geocoding** | **Photon (by Komoot)** | Substring autocomplete bounded to the Chhattisgarh bounding box. |
| **Payment Protocol** | **QRServer (UPI Deep Link)** | Generates dynamic QR codes compliant with the `upi://pay` URI standard. |

---

## 📂 Project Structure

```text
RYDEX/
├── VehicleManager.java   # Domain model, fleet store, pricing engine, Haversine & OTP logic
├── RydexServer.java      # Multithreaded Java HTTP server, REST endpoints, CORS & JSON parser
├── index.html            # Single-page client: Cyber-OLED UI, Leaflet map, modals, & ledger
└── README.md             # Project documentation and technical specifications
```

---

## 💻 Installation and Execution

### Prerequisites
* **Java Development Kit (JDK 17 or higher)** installed and verified:
  ```bash
  java -version
  javac -version
  ```
* A modern web browser (Brave, Chrome, Firefox, or Edge) with JavaScript enabled.

### Step-by-Step Setup

1. **Clone the Repository:**
   ```bash
   git clone https://github.com/your-username/rydex-mobility.git
   cd rydex-mobility
   ```

2. **Compile the Java Backend:**
   Compile the source files using the Java compiler:
   ```bash
   javac VehicleManager.java RydexServer.java
   ```

3. **Start the Rydex Server:**
   Launch the HTTP server:
   ```bash
   java RydexServer
   ```
   *Terminal Output:*
   ```text
   =================================================
    Rydex Server (Bhilai-Durg) running on port: 8081
    Open your browser: http://localhost:8081
   =================================================
   ```

4. **Access the Web Client:**
   Open your browser and navigate to:
   ```text
   http://localhost:8081
   ```

---

## 🔌 API Specifications

The server communicates via lightweight JSON over HTTP:

### 1. Compute Fares
* **Endpoint:** `POST /api/fares`
* **Request Payload:**
  ```json
  { "distance": 8.75 }
  ```
* **Response Payload (200 OK):**
  ```json
  {
    "BIKE": { "label": "Rydex Bike", "fare": 112.9, "etaMinutes": 22 },
    "AUTO": { "label": "CG Auto", "fare": 167.0, "etaMinutes": 22 },
    "CAR_4_SEATER": { "label": "Car 4 Seater", "fare": 227.1, "etaMinutes": 22 },
    "CAR_7_SEATER": { "label": "7 Seater", "fare": 336.7, "etaMinutes": 22 }
  }
  ```

### 2. Confirm Booking
* **Endpoint:** `POST /api/book`
* **Request Payload:**
  ```json
  {
    "type": "AUTO",
    "pickup": "Supela Chowk, Bhilai",
    "dropoff": "Durg Railway Station",
    "pickupLat": 21.2032,
    "pickupLng": 81.3394,
    "dropLat": 21.1904,
    "dropLng": 81.2849,
    "distance": 8.75
  }
  ```
* **Response Payload (200 OK):**
  ```json
  {
    "bookingId": "RYD-F91A2C3B",
    "status": "CONFIRMED",
    "vehicle": "CG Auto",
    "fare": 167.00,
    "otp": "3312",
    "distance": 8.75,
    "driver": {
      "name": "Vikram Patel",
      "phone": "+91 99931 77882",
      "plate": "CG 07 AT 9021",
      "rating": 4.7,
      "lat": 21.1904,
      "lng": 81.2849
    }
  }
  ```

---

## 👥 Team Roles & Responsibilities

| Member | Role Title | Core Responsibilities & Modules |
|:---|:---|:---|
| **Student 1** | **Backend & Server Engineer** | `RydexServer.java`: HTTP server lifecycle, multithreading, REST handlers, and dependency-free JSON parsing. |
| **Student 2** | **DSA & Core Logic Lead** | `VehicleManager.java`: Nearest-driver linear search, Haversine distance model, `ConcurrentHashMap` session safety. |
| **Student 3** | **UI/UX & Frontend Architect** | `index.html` (CSS): Cyber-Minimalist OLED design system, Bento Grid layout, two-stage viewport budgeting. |
| **Student 4** | **GIS & Geospatial Engineer** | `index.html` (GIS): Leaflet.js engine, Google Satellite Hybrid tiles, OSRM road routing, and state geofencing. |
| **Student 5** | **Features & Telematics Lead** | `index.html` (JS): Live driver approach animation, dynamic UPI QR generation, GST billing, and `localStorage` ledger. |

---

## 🔮 Future Scope

* **Hexagonal Spatial Indexing (Uber H3 / QuadTree):** Upgrade driver lookup from an $O(N)$ linear scan to an $O(1)$ hierarchical spatial index as the fleet scales to thousands of active vehicles.
* **Persistent PostGIS Storage:** Replace in-memory data structures with a PostgreSQL database equipped with PostGIS extensions (`ST_DWithin`) for spatial querying.
* **Bidirectional WebSockets:** Replace client-side animation loops with continuous GPS telemetry streamed directly from a dedicated Captain Mobile App.
* **Live Payment Webhooks:** Connect Razorpay or Cashfree webhooks to settle UPI transactions automatically on the backend.

---

## 📄 License

This project is licensed under the **MIT License** — see the [LICENSE](LICENSE) file for details. Developed for academic and educational engineering demonstrations.