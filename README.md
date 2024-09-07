## 🚗 ParkPro
Welcome to the ParkPro project! 🎉 This application is designed to help users find parking spots, check vehicle details, and access weather updates, among other features. To run this project successfully, you'll need to set up a few API keys and Firebase configurations. Follow the steps below to get started!

🛠️ Prerequisites
Before you can run the app, make sure you have the following:

📱 Android Studio installed on your machine.
🔑 A Google account to access Google APIs.
☁️ An account with OpenWeatherMap to obtain the weather API key.
🚘 An account with the DVLA for vehicle API access.
🔥 A Firebase project set up for the app's database.
🌟 Features
🅿️ Detailed Car Park Information: Access essential details like prices, entrances, and location codes to ensure a smooth parking experience.
🛣️ Parking Instructions: Step-by-step guidance to help users navigate to and within parking facilities.
⭐ Review System: Users can review and rate parking facilities, helping others make informed choices.
🚘 Vehicle Details Check: Integrates with the DVLA API to provide users with detailed vehicle information.
🔔 MOT and Road Tax Reminders: Notifies users about important vehicle maintenance dates, ensuring compliance and safety.
☀️ Live Weather Updates: Real-time weather conditions help users plan their parking and travel accordingly.
🛑 Real-Time Traffic Updates: Provides up-to-date traffic information to help users avoid delays and find the best routes.
🔐 API Keys and Configuration
To get the app up and running, you'll need to obtain the following API keys and insert them into the appropriate places in the code:

1. Google Maps API Key 🗺️
Location: res/values/strings.xml
Code:
xml
Copy code
<string name="google_maps_api_key">insert your Google Maps API key here</string>
Purpose: Displays maps within the app.
2. Google Places API Key 📍
Location: res/values/strings.xml
Code:
xml
Copy code
<string name="google_place_api_key">insert your Google Places API key here</string>
Purpose: Provides location-based services.
3. DVLA Vehicle API Key 🚗
Location: res/values/strings.xml
Code:
xml
Copy code
<string name="vehicle_api_key">insert your DVLA Vehicle API key here</string>
Purpose: Accesses the DVLA Vehicle API.
4. DVLA Vehicle API Test Key 🧪
Location: res/values/strings.xml
Code:
xml
Copy code
<string name="vehicle_api_test_key">insert your DVLA Vehicle Test API key here</string>
Purpose: Used for testing with the DVLA Vehicle API.
5. OpenWeatherMap API Key 🌦️
Location: MainActivity.java (or the appropriate file where the weather API call is made)
Code:
kotlin
Copy code
val url = "https://api.openweathermap.org/data/2.5/weather?lat=51.5072&lon=-0.1276&appid=--insert API key here--&units=metric"
Purpose: Fetches weather data.
6. Firebase Database URL 🔥
Location: Replace the existing Firebase Database URL in your code.
Code:
kotlin
Copy code
database = FirebaseDatabase.getInstance("insert your Firebase Database URL here")
Purpose: Connects your app to your Firebase Realtime Database.
🚀 Getting Started
Follow these steps to get the project running:

Clone the Repository: 📥

Clone this repository to your local machine.
Open in Android Studio: 💻

Launch the project in Android Studio.
Insert API Keys & Configurations: 🔑

Replace the placeholders with your API keys and Firebase configurations as detailed above.
Build & Run the App: 🏃‍♂️

Run the project on an emulator or physical device to see your app in action!
🔍 Additional Notes
Security Tip: 🚨 Ensure that API keys are not hardcoded into the source code if you're pushing to a public repository. Use environment variables or other secure methods to handle sensitive information.
Security Tip: 🚨 Ensure that API keys are not hardcoded into the source code if you're pushing to a public repository. Use environment variables or other secure methods to handle sensitive information.


## 🖼️ Screenshots

### 1. Landing Page
<img src="https://github.com/user-attachments/assets/90a74af7-7aca-4444-a247-87d8c14cdb72" alt="Landing Page" width="400"/>

### 2. Register Page
<img src="https://github.com/user-attachments/assets/956003b3-565d-4352-815c-fb97ad1f02ac" alt="Register Page" width="400"/>

### 3. Login Page
<img src="https://github.com/user-attachments/assets/9eeaf5a9-81a9-4f79-8a25-df526559e8fb" alt="Login Page" width="400"/>

### 4. Home Page
<img src="https://github.com/user-attachments/assets/f644fb90-54d2-4a7f-8952-e39bb448a9a1" alt="Home Page" width="400"/>

### 5. Parking Page
<img src="https://github.com/user-attachments/assets/e9254028-1b17-4305-88cb-63969296857c" alt="Parking Page" width="400"/>

### 6. Vehicle Page
<img src="https://github.com/user-attachments/assets/b052f037-91dc-4e36-aa19-f6c4b52681ab" alt="Vehicle Page" width="400"/>

### 7. Carpark Page
<img src="https://github.com/user-attachments/assets/b601c232-e1e9-48bb-97e9-084594332448" alt="Carpark Page" width="400"/>

### 8. Review Page
<img src="https://github.com/user-attachments/assets/3f666210-e5a6-46ad-a26f-cf757fdf1b00" alt="Review Page" width="400"/>


