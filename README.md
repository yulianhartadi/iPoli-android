[![Build Status](https://travis-ci.org/iPoli/iPoli-android.svg?branch=dev)](https://travis-ci.org/iPoli/iPoli-android)

# ![iPoli](.github/logo.png)

# iPoli: AI-powered task scheduling for your life!

iPoli is a combination of Calendar, ToDo list and Habit tracking app, all in one place! It's goal is to find time for the things that matter most in your life, make you stick to good habits and free you from the burden of scheduling tasks by yourself.

# How does it look like?

<img src="./.github/screens/calendar.png" width="280px"/>
<img src="./.github/screens/edit.png" width="280px"/>
<img src="./.github/screens/growth.png" width="280px"/>

# Want to watch it in action?

[![iPoli in action](http://img.youtube.com/vi/PTUseDG5g2g/0.jpg)](http://www.youtube.com/watch?v=PTUseDG5g2g "iPoli: Smart Calendar & To Do List Android app")

# Can I just install it?

Yep, you are in luck!

<a href="https://play.google.com/store/apps/details?id=io.ipoli.android"><img alt="Get it on Google Play" src="https://play.google.com/intl/en_us/badges/images/generic/en-play-badge.png" height="80" width="250"/></a>

# Have a question or just want to get in touch?

Join the community on [Slack](https://slofile.com/slack/productivityhackers) or write us at [hi@ipoli.io](mailto:hi@ipoli.io)

# How to run it locally?

1. Clone this repo
2. Import in Android Studio
3. Create APIConstants.java file and fill it with

  ```java
  public interface APIConstants {
    String DEV_IPOLI_ENDPOINT = "http://10.0.3.2/v1/";
    String PROD_IPOLI_ENDPOINT = "http://10.0.3.2/v1/";
    String DEV_SCHEDULING_ENDPOINT = "http://10.0.3.2/v1/";
    String PROD_SCHEDULING_ENDPOINT = "http://10.0.3.2/v1/";
    String API_KEY = "test";
  }
  ```
4. Create AnalyticsConstants.java file and fill it with

  ```java
  public interface AnalyticsConstants {
    String PROD_FLURRY_KEY = "123456";
    String DEV_FLURRY_KEY = "42";
  }
  ```
5. Run on your favorite device/emulator

# Main features so far

* Calendar + ToDo list for your daily schedule
* Overview/Agenda of the tasks for today and the next 7 days
* Smart Add - adding tasks using natural language
* Recurrent tasks/habits
* Background sync between device(s) and server
* Flexible habit scheduling - Workout 3 times per week every Mon, Tue, Fri and Sat
* Sync with Google Calendar/Outlook (something else?)

# Upcoming

* Automatic task scheduling - find the best slot (time) to start/do a task

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
