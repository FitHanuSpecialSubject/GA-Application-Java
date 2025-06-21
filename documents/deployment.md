## Docker compose for BE FE deployment. 

### Environment

- Operating system:
  - Windows Server (run via WSL2 container)
  - Debian 
  - Ubuntu (RECOMMENDED)

- System requirement:
  - 2GB of RAM
  - 2+ cores CPU (x86_64 architecture)

### Step 1: install docker via this script
```bash
sudo bash docker_setup.sh
```

### Step 2: update server location & endpoint for frontend 
```bash
nano nginx/.env
# Update VITE_BACKEND_PROTOCOL to the correct protocol
# Update VITE_BACKEND_URL to the correct IP or URL
```


### Step 2: Build and start docker
```bash
docker compose build
docker compose up -d
```

###### Update 2025-6-21 by [Tiến Thành](https://github.com/suyttthideptrai)

