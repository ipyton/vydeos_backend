# 🎬 Vydeo.xyz

A full-stack movie community platform where users can browse, post, and discuss films, as well as interact with each other. This project features a modern web architecture with an emphasis on performance, scalability, and user experience.

## 🚀 Tech Stack

### Frontend

- **React + Material UI** – Component-based responsive UI  
- **React Router** – Client-side routing  
- **Axios** – HTTP requests to backend APIs  
- **Tailwind CSS (partial)** – Utility-first styling for rapid UI development  

### Backend

- **Flask (Python)** – RESTful API for user auth, movie data, comments, etc.  
- **Spring Boot (Java)** – Microservices for modular architecture  
- **Redis** – Caching, rate limiting, and hot content tracking  
- **Kafka** – Event streaming and asynchronous logging  
- **Cassandra** – Distributed storage for activity streams  
- **MySQL (AWS RDS)** – Relational database for core data (users, movies, comments)  

### DevOps & Infrastructure

- **Nginx** – Reverse proxy and static file serving  
- **MinIO** – Object storage for media (avatars, posters)  
- **GitHub Actions** – CI/CD automation  
- **Cloudflare Pages / Workers** – CDN-based frontend deployment and edge logic  
- **Docker** – Environment standardization for development and deployment  

## ✨ Features

- 🔐 **User authentication** (JWT-based)  
- 📽️ **Movie detail pages** with cast, description, ratings, and reviews  
- 💬 **Commenting, likes, and reporting** system  
- 👤 **User profiles** (posts, liked content, followers/following)  
- 🔍 **Movie search** with tag and genre filtering  
- 📈 **Activity feed & recommendation system** (Kafka-based prototype)  
- 🛠️ **Admin panel** (partially implemented)  

## 📁 Project Structure

movie-community/
├── frontend/ # React frontend
├── backend-flask/ # Flask backend services
├── backend-spring/ # Spring Boot microservices
├── nginx/ # Nginx config files
├── scripts/ # Deployment and helper scripts
└── README.md

## 🧪 Getting Started

### 1. Clone the repo

```bash
git clone https://github.com/your-username/movie-community.git
cd movie-community
2. Run backend services via Docker
bash
Copy
Edit
docker-compose up --build
3. Run frontend locally
bash
Copy
Edit
cd frontend
npm install
npm start
Note: You need Docker, Node.js, and Python installed for local development.

📦 Data & APIs
Uses mock data in development mode (see mock/ folder)

Production mode supports external APIs (e.g. TMDb) for movie metadata

Kafka is used for logging user interactions and powering feed algorithms

✅ Roadmap
 Real-time notification system via WebSockets or Kafka

 Service discovery using Consul or Eureka

 Add monitoring and logging (Prometheus, Sentry)

 Complete the admin dashboard and content moderation tools

 Expand recommendation system with collaborative filtering or embeddings


🧑‍💻 Author
Zhiheng Chen
