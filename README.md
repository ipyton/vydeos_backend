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
- **Redis** – Caching, rate limiting, sequence number generation,and hot content tracking
- **Kafka** – Event streaming and asynchronous logging  
- **Node.js** – User routing and message endpoint scale up. 
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
git clone  -b release https://github.com/ipyton/vydeos_backend.git
cd vydeos_backend
```
2. Run backend services via Docker
```bash
docker-compose -f vydeos_backend/blog_backend/scripts/docker-compose/middleware up -d
```

3. Create the database schema and Kafka topics that needed. The schema is in vydeos_backend/blog_backend/scripts/DBSchema/ which is grouped by the domain. Kafka topics is automatically created, and you need to restart the application after creation. Make sure you allow automaitically topic creation in topic.
4. Run the Movie Service, if you need movie searching service.
```bash
git clone -b release https://github.com/ipyton/MovieService.git
cd vydeos_backend
# create a venv for yourself before next steps
pip install -f requirements.txt
python app.py
```
5. Run the user registration service, it is critical for the realtime messaging function.
```bash
git clone -b release https://github.com/ipyton/user_registration_center.git
cd user_registration_center
```
Note: You need to follow the readme in this project to start it up correctly
6. Configure the nginx. This is a gateway for a single deployment. The configuration file is vydeos_backend/blog_backend/scripts/nginx/nginx.conf
8. Run frontend locally
```
cd frontend
npm install
npm start
```
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
