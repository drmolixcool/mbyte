## MByte Extension's Projects

### Store creation task manager

#### Description

This project is responsible for managing the asynchronous tasks related to store creation. When a user requests the creation of a new store, this component handles the task in the background, ensuring that the store is set up correctly without blocking the user's main workflow.
A Task Manager is implemented to queue and process store creation requests efficiently. This allows for better resource management and improves the overall user experience by offloading time-consuming operations to a dedicated service.
In the GUI, users can initiate store creation requests, which are then processed by this task manager. Users can also monitor the status of their store creation tasks, receiving notifications upon completion or if any issues arise during the process.

--- 

### Multi-Store Support

#### Description

This project extends the existing MByte application to support multiple stores per user. Each user can now create and manage several stores, allowing for better organization and separation of data.
The multi-store functionality is integrated into the user interface, providing users with an intuitive way to switch between their different stores. Users can create new stores, rename them, and delete them as needed.

### Store configuration management

#### Description

This project focuses on the management of store configurations within the MByte application. It provides users with the ability to customize various settings related to their stores, such as storage limits or maximum file numbers.
The configuration management interface allows users to easily modify their store settings through a user-friendly GUI. 

### Failover and High Availability

#### Description

This project aims to enhance the reliability and availability of the MByte application by implementing failover mechanisms and high availability configurations.
By deploying redundant instances of critical components, the system can continue to operate seamlessly in the event of hardware failures or other disruptions.
Load balancing techniques are employed to distribute traffic evenly across multiple instances, ensuring optimal performance and responsiveness.

### Vault integration for secret management

#### Description

### Multiple Server Deployment for stores

#### 

### Full Text Search Integration

#### Description

This project integrates full-text search capabilities into the MByte application, allowing users to perform advanced searches across their stored files and documents.
By leveraging search engines like Apache Lucene or Solr, users can quickly locate files based on keywords, phrases, or other criteria.
The search functionality is seamlessly integrated into the user interface, providing an intuitive way for users to access and utilize the search features.

### File Sharing

#### Description

This project introduces file sharing features to the MByte application, enabling users to share files and folders with others securely.
Users can generate shareable links and send them to collaborators, allowing for easy access to specific files or directories.
Access controls and permissions can be configured to ensure that shared content is only accessible to authorized users.

### Activity Logging and Auditing

#### Description

This project implements activity logging and auditing features within the MByte application. It tracks user actions and system events, providing a comprehensive audit trail for security and compliance purposes.
Administrators can review logs to monitor user activity, identify potential security issues, and ensure adherence to organizational policies.
The logging system is designed to be efficient and scalable, capable of handling large volumes of data without impacting system performance.

### File history (versioning)

### Thumbnail generation and preview

### FTP Protocol integration

### Distributed search engine

### WebHook management

### Integrity check

### Quotas management

### Trash integration

### Upload progress and resume download (HTTP Range Request)

### Tagging / Automated tagging

### External storage integration (S3, WebDAV, etc.) with ciphering

### Discord Bot integration for chat backup (text and files) and notifications
