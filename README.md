koneksiurl: http post from android
==================================

This library is a simple tool to help http post using urlconnection
from android. (It does not support https)

Installation
------------

You can use this library in your android studio project by add
the following `build.gradle` configuration:

```maven
repositories {
    maven {
        url 'https://dl.bintray.com/panji/maven'
    }
}

dependencies {
    compile 'com.gmail.epanji:koneksiurl:1.0.0@aar'
}
```

Usage
=====

This library need an internet connections, so use this only inside
class that extends `AsyncTask`.

GET
---
 
If you want to use GET, just
add `?field_name=value&field_name2=value2` to url:

```java
public class Sample extends AsyncTask<Void,Void,Void> {
    @Override
    protected Void doInBackground(Void... params) {
        KoneksiUrl kUrl = new KoneksiUrl();

        String url = "http://somedomain/sample.php?field_name=" 
               + value1 + "&field_name2=" + value2;

        String response = kUrl.execute(url);
        return null;
    }
}
```

POST
----

Simple http POST

```html
<form action="sample.php" method="post">
    First name:<br>
    <input type="text" name="f_firstname"><br>
    Last name:<br>
    <input type="text" name="f_lastname"><br>
    ...
</form>
```

```java
public class Sample extends AsyncTask<Void,Void,Void> {
    @Override
    protected Void doInBackground(Void... params) {
        KoneksiUrl kUrl = new KoneksiUrl();
        ContentValues cv = new ContentValues();

        cv.put("f_firstname", "John");
        cv.put("f_lastname", "Doe");

        String url = "http://somedomain/sample.php";
        String response = kUrl.execute(url, cv);
        return null;
    }
}
```

POST MULTIPART
--------------

Http POST with multipart

```html
<form action="sample.php" method="post" enctype="multipart/form-data">
    First name:<br>
    <input type="text" name="f_firstname"><br>
    Last name:<br>
    <input type="text" name="f_lastname"><br>
    Picture 1:<br>
    <input type="file" name="f_picture_1"><br>
    Picture 2:<br>
    <input type="file" name="f_picture_2"><br>
    ...
</form>
```

```java
public class Sample extends AsyncTask<Void,Void,Void> {
    @Override
    protected Void doInBackground(Void... params) {
        KoneksiUrl kUrl = new KoneksiUrl();
        ContentValues cv = new ContentValues();
        HashMap<String, File> files = new HashMap<>();

        cv.put("f_firstname", "John");
        cv.put("f_lastname", "Doe");

        files.put("f_picture_1", new File("pathtofile/picture1.png"));
        files.put("f_picture_2", new File("pathtofile/picture2.png"));

        String url = "http://somedomain/sample.php";
        String response = kUrl.execute(url, cv, files);
        return null;
    }
}
```

Notes:
If you don't need second params on execute(1st, 2nd, 3rd), use null.

POST ARRAY
----------

Http POST field array

```html
<form action="sample.php" method="post">
    Hobbies:<br>
    <input type="checkbox" name="f_hobby[23]" value="Fishing"> Fishing<br>
    <input type="checkbox" name="f_hobby[40]" value="Swimming"> Swimming<br>
    <input type="checkbox" name="f_hobby[42]" value="Surfing"> Surfing<br>
    ...
</form>
```

```java
public class Sample extends AsyncTask<Void,Void,Void> {
    @Override
    protected Void doInBackground(Void... params) {
        KoneksiUrl kUrl = new KoneksiUrl();
        ContentValues cv = new ContentValues();

        cv.put("f_hobby[" + 23 + "]", "Fishing");
        cv.put("f_hobby[" + 40 + "]", "Swimming");
        cv.put("f_hobby[" + 42 + "]", "Surfing");

        String url = "http://somedomain/sample.php";
        String response = kUrl.execute(url, cv);
        return null;
    }
}
```
