# CLI Calculator

This is a command-line utility that acts as a calculator. You can enter standard math expressions and this program will evaluate them and print their result to the console. You can also pass in the expression as a command-line argument.

## Installation

Clone the repo, and run the following in its root directory:
```bash
lein uberjar
```
This will build a jar in the ```target/uberjar```. Copy or move it to the desired directory, and then run the following in that directory:
```bash
java -jar calculator-0.1.0-SNAPSHOT-standalone.jar
```

If you wish, you can also use ```jpackage``` to create a standalone executable.

## Usage
To run in REPL mode, run
```bash
java -jar calculator-0.1.0-SNAPSHOT-standalone.jar
```
This will continuously take input from ```stdin``` until quit with ```^C``` or by typing ```quit```, and evaluate each expression and print the result on the next line.

You can also pass in an expression as a command-line argument, e.g.
```bash
java -jar calculator-0.1.0-SHAPSHOT-standalone.jar "3+(4abs(5))"
``` 

The following operations are supported: +, -, *, /, % (mod), and ^ (power)

The following functions are supported: sqrt, cbrt, log, ln, sin, cos, tan, arcsin, arccos, arctan, exp, abs, floor, ceil, round

The following constants are supported: pi, e
