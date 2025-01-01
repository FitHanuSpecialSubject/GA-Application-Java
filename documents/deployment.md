## Docker compose for BE FE deployment. 

#### Currently under development, might not be running as expected.
#### Tested with Ubuntu 24.04. gi gi day LTS


### Step 1: install docker via this script
```bash
sudo bash docker_setup.sh
```

### Step 2: Setup deployment system's public IP for Web Application
```bash
nano /nginx/.env
# Then change REACT_APP_BACKEND_URL to the correct public ipv4 of the server
# Nginx setup ignores the REACT_APP_BACKEND_PORT so leave it as 80
# Save file and continue the next step
```

### Step 3: Build and start docker
```bash
docker compose build
docker compose up -d
```

###### Update 2024-12-23 by [Tiến Thành](https://github.com/suyttthideptrai)

