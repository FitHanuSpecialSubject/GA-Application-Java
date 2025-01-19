# **1\. Introduction**

### **Abstract**

This application is developed to assist in calculating and finding optimal solutions in Game Theory and Stable Matching Theory problems. Aiming to address the complex issues in these problems, the application applies advanced optimization algorithms such as Genetic Algorithms, which help estimate and achieve accurate results, thereby providing effective solutions for users. The technologies used in the application include: the Java programming language, the Spring Boot Framework (used to build and deploy the backend of the application, supporting the Model-View-Controller architecture), and the MOEA Framework (a library that supports evolutionary optimization algorithms, particularly for multi-objective optimization problems).

The primary goal of this manual is to provide a detailed, easy-to-understand guide for users to effectively utilize the application and make the most of its features. This guide will walk users through the process of installing the application on various operating systems, the system requirements necessary for the application to function, how to create and validate the data forms required for the problems, detailed installation steps, running the application via command-line tools or IDEs, and analyzing the output results. Additionally, the manual offers optimization tips, ways to customize output results to meet user requirements, and troubleshooting common issues during the usage of the application. This ensures that users can maximize the application's performance and achieve highly accurate output results.

# **2\. Table of Contents**
1. [Introduction](#1-introduction)  
2. [Table of Contents](#2-table-of-contents)  
3. [Module Specifications](#3-module-specifications)  
   - [SMT (Stable Matching Theory)](#31-stable-matching-theory)  
   - [GT (Game Theory)](#32-gt-game-theory)  
4. [Local Installation](#4-local-installation)  
   - [System Requirements](#41-system-requirements)  
   - [Installation Steps](#42-installation-steps-) 
   - [Troubleshooting](#43-troubleshooting)  
5. [Data Form Creation](#5-data-form-creation)  
6. [Solve](#6-solve)  
   - [Overview](#61-overview)  
   - [ Step-by-step Execution](#62-step-by-step-excution) 
   - [Optimization Tips](#63-optimization-tips)  
7. [Get Result Insight](#7-get-result-insight)
   - [Analyzing Results](#71-analyzing-results)  
   - [Customization](#72-customization) 
   - [Common Issues](#73-common-issues)    
8. [Deployment](#8-deployment)
   - [Supported Environments](#81-supported-environments)  
   - [Deployment Steps](#82-deployment-steps) 
   - [Best Practices](#83-best-pratices)      
9. [Contributors](#9-contributors)  
10. [End](#10-end)

# **3\. Module Specifications**

### **3.1 SMT (Stable Matching Theory)**

### **3.2 GT (Game Theory)**

# **4\. Local Installation**

### **4.1 System Requirements**

#### **Hardware Prerequisites**

- CPU: Minimum 2 GHz (multi-core recommended).
- RAM: At least 8GB.
- Disk Space: Minimum 10GB of free storage.

#### **Software Dependencies**

- Java Development Kit (JDK) 17\.
- Maven 3.8.3 or later.
- Git for source code cloning.

### **4.2 Installation Steps**

1. Clone the repository using Git:  
   git clone https://github.com/FitHanuSpecialSubject/GA-Application-Java.git  
   cd GA-Application-Java

2. Build the application:  
   a, Using Maven Wrapper:

- Linux: bash ./mvnw clean install
- Windows: mvnw.cmd clean install

b,Using System Maven: mvn clean install

### **4.3 Troubleshooting**

- **Issue**: Missing dependencies.
  - **Solution**: Run **mvn clean install** to refresh dependencies.
- **Issue**: Incorrect Java version.
  - **Solution**: Verify Java 17 installation and set it as the default.

# **5\. Data Form Creation**

### **Purpose of Data Forms**

Data forms allow users to define and structure the input data required for problem-solving.

### **Instructions for Creating and Validating Forms**

1. Navigate to the data form creation interface.
2. Add fields based on the problem type (e.g., participants, preferences).
3. Validate the form for completeness and consistency.

### **Examples and Best Practices**

- **Stable Matching Example**: Create a form with columns for participants and their ranked preferences.
- Use consistent naming conventions for clarity.

# **6\. Solve**

### **6.1 Overview**

#### **How the Solving Process Works**

- The solver processes the input data and applies algorithms to compute results.
- Supports multiple methodologies such as Genetic Algorithms and Brute Force.

### **6.2 Step-by-Step Execution**

1. Prepare input data using the data form creation tool.
2. Configure the solver with desired parameters (e.g., algorithm type).
3. Execute the solving process via the application or command line.

### **6.3 Optimization Tips**

- Use smaller datasets for initial testing.
- Ensure input data is complete and correctly formatted.

# **7\. Get Result Insight**

### **7.1 Analyzing Results**

- View results in dashboards with charts, tables, and other visual aids.
- Filter data to focus on specific insights.

### **7.2 Customization**

- Adjust result views by applying filters or customizing visual elements.
- Export results in formats like CSV or PDF for further analysis.

### **7.3 Common Issues**

- **Issue**: Missing results.
  - **Solution**: Verify that the solving process completed successfully.
- **Issue**: Misinterpreted data.
  - **Solution**: Refer to the documentation for guidance on result formats.

# **8\. Deployment**

### **8.1 Supported Environments**

- **Local**: Single machine deployment for development or testing.
- **Server**: Deploy to dedicated or virtualized servers.
- **Cloud**: Deploy to platforms like AWS, Azure, or Google Cloud.

### **8.2 Deployment Steps**

1. Build the application using Maven:  
   **`mvn clean package`**
2. Deploy the `.jar` file to the desired environment.
3. Configure environment-specific settings, such as database connections.

### **8.3 Best Practices**

- Secure sensitive information using environment variables or encrypted files.
- Regularly monitor application logs for performance and errors.

# **9\. Contributors**

### **List of Contributors**

- **John Doe**: Backend Developer
- **Jane Smith**: Frontend Developer
- **Alex Nguyen**: Documentation Specialist

### **Acknowledgments**

- **MOEA Framework**: Core solving engine.
- **Spring Framework**: Backend infrastructure

# **10\. End**

### **Final Notes and Disclaimers**

- Ensure the application is used within its intended scope.
- Future updates may change certain functionalities.
