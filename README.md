# Companyons

Interactively develop Java/[Vaadin](https://vaadin.com) web
[components](https://webcomponents.org) applications with embedded
CPython in dynamic Notebook style

## Requirements

* Java 17+
* [Gradle](https://gradle.org) 7.3+
* Python 3 **( < 3.10 for now!** – _Will later be auto-installable via Gradle …)_
* Latest [Jep](https://github.com/ninia/jep) installed into your Python environment
  _(Will also later be handled by Gradle …)_

## Getting started

Open `gradle.properties` and provide the full path to your preferred
Python executable:

```properties
pythonExe = /path/to/your/bin/python
```

Or, on Windows, respectively:

```properties
pythonExe = C:\\path\\to\\your\\python.exe
```

Then, run Companyons' backend with:

```shell
> gradle bootRun
```

Which will automatically download all Java dependencies, build the
project, and finally open its frontend in your default web browser …

