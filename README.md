# PokePad

**A Professional Full-Stack Pokémon TCG Collection Manager** 

*Developed by Calvin Weng & Brian Yu*

PokePad is a web application designed for Pokémon TCG enthusiasts to archive, track, and organize their collections. 

## 🚀 Features

* **Set Archive:** View 180+ high-resolution cards from the latest sets, such as Prismatic Evolutions.
* **Synchronization:** Automated data pipeline that fetches and persists card metadata from the external Pokemon TCG REST API.
* **Binder Management:** Create and manage digital collections/binders for your physical cards.
* **Wishlist Tracking:** Track cards you need to complete your master sets (In Progress).
* **Multi-Region Support:** Built-in capability to handle both English and Japanese card sets.

## 🛠️ Tech Stack

### **Backend**
* **Language:** Java 17 (LTS).
* **Framework:** Spring Boot 3.
* **Data Access:** Spring Data JPA + Hibernate.
* **Database:** PostgreSQL.
* **Security:** JWT-based Authentication (In Progress).

### **Frontend**
* **Library:** React 18 (Vite).
* **State Management:** React Hooks (useState, useEffect).
* **Performance:** Native browser lazy-loading for image-heavy grids.

---

## 🚦 Getting Started

### **Prerequisites**
* **Java 17 or 21 (LTS)**
* **Node.js 18+**
* **PostgreSQL** (Ensure a database named `pokepad` is created)

### **Setup & Installation**

1.  **Clone the repository:**
    ```bash
    git clone [https://github.com/your-username/pokepad.git](https://github.com/your-username/pokepad.git)
    cd pokepad
    ```

2.  **Configure Environment Variables:**
    Create a `.env` file in the `/server` directory:
    ```env
    DB_USERNAME=your_username
    DB_PASSWORD=your_password
    POKEMON_API_KEY=your_key_here
    ```

3.  **Run the Backend:**
    ```bash
    cd server
    ./mvnw spring-boot:run
    ```
    *The app will automatically seed the database with Prismatic Evolutions data on the first run.*

4.  **Run the Frontend:**
    ```bash
    cd client
    npm install
    npm run dev
    ```

---

## 🛣️ Roadmap
- [x] Full-stack connection (Spring Boot to React)
- [x] Automated Card Seeding from REST API
- [x] Lazy-loaded Archive Grid
- [x] Functional Local MVP
- [ ] Deployment
- [ ] User Authentication (Spring Security + JWT)
- [ ] Multi-Binder "Collection" Logic
- [ ] Global Search & Filtering
