# CLI Calculator

This is a command-line utility that acts as a calculator. You can enter standard math expressions and this program will evaluate them and print their result to the console. You can also pass in the expression as a command-line argument.

## Installation

Clone the repo, and run the following in the root of it:
```zsh
lein uberjar
```
This will build a jar in the ```target/uberjar``` that you can then run with:
```zsh
java -jar calculator-0.1.0-SNAPSHOT-standalone.jar
```

## Usage
To run in REPL mode, run
```zsh
java -jar calculator-0.1.0-SNAPSHOT-standalone.jar
```
This will continuously take input from ```stdin``` until quit with ```^C``` or by typing ```quit```, and evaluate each expression and print the result on the next line.

You can also pass in an expression as a command-line argument, e.g.
```zsh
java -jar calculator-0.1.0-SHAPSHOT-standalone.jar "3+(4abs(5))"
``` 

The following operations are supported: +, -, *, /, % (mod), and ^ (power)

The following functions are supported: sqrt, cbrt, log, ln, sin, cos, tan, arcsin, arccos, arctan, exp, abs, floor, ceil, round

The following constants are supported: pi, e

## License

Copyright © 2024 Caleb Rice

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
