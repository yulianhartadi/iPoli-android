# ![iPoli](github/logo.png)

# iPoli: AI-powered task scheduling for your life!

iPoli is a combination of Calendar, ToDo list and Habit tracking app, all in one place! It's goal is to find time for the things that matter most in your life, make you stick to good habits and free you from the burden of scheduling tasks by yourself.

# How does it look like?

# How to run it locally?

1. Clone this repo
2. Import in Android Studio
3. Create APIConstants.java file and fill it with

  ```java
  public interface APIConstants {
    String API_ENDPOINT = "http://example.com/";
    String API_KEY = "test";
  }
  ```
  
4. Create AnalyticsConstants.java file and fill it with

  ```java
  public interface AnalyticsConstants {
    String FLURRY_KEY = "123456";
  }
  ```
  
5. Run on your favorite device/emulator

# Main features so far

* Calendar + ToDo list for your daily schedule
* Overview/Agenda of the tasks for today and the next 7 days
* Smart Add - adding tasks using natural language
* Recurrent tasks/habits
* Background sync between device(s) and server

# Upcoming

* Automatic task scheduling - find the best slot (time) to start/do a task
* Flexible habit scheduling - Workout 3 times per week every Mon, Tue, Fri and Sat
* Sync with Google Calendar/Outlook (something else?)

# Libraries used

* Otto
* Realm
* PrettyTime
* Butterknife
* Dagger2
* JodaTime
* Retrolambda
* Retrofit2
* RxJava
* RxAndroid
* probably some more

# Want to help?

Hack on iPoli and send a pull request

# License

This Android app is MIT licensed (do whatever you want with it)
