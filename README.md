# Gringo Wrapper
A wrapper for [gringo](http://potassco.sourceforge.net/) that takes a logic program as input and outputs the grounded program without any optimizations. For example, calling gringo with the logic program
```
a.
b :- a.
```
leads to the grounded output ``a. b.`` Calling gringo-wrapper with the same logic program does not perform any optimizations.

## Table of contents
 - [Usage](#usage)
 - [Building](#building)
 - [How it works](#how-it-works)

## Usage
Download the latest [gringo-wrapper release](https://github.com/gaste/gringo-wrapper/releases/latest) (or [build](#building) it yourself) and unpack it. Use the scripts provided in the `bin/` directory to start the gringo wrapper:
```
gringo-wrapper [filename]
```
 - If you specify a filename, gringo-wrapper will read the contents of that file and print the grounded program on the standard output.
 - If you do not specify a filename, use the standard input to specify the logic program to ground.

## Building
This project is managed using [Apache Maven](https://maven.apache.org/).

### Instructions

```
git clone https://github.com/gaste/gringo-wrapper
cd gringo-wrapper
mvn package
```

This will create a `.zip` and `.tar.gz` file containing the Java archive as well as start scripts for Windows and Unix in the `target/` directory.

## How it works
The gringo-wrapper replaces each fact ``f`` of the logic program with the rule ``f :- _l``, where ``_l`` is a new literal. Adding the rule ``_l | -_l`` ensures that gringo cannot do any optimization, since there are no facts in the logic program. After this modification, the gringo wrapper uses gringo to ground the modified logic program. Then it replaces each grounded rule ``f :- _l`` with the fact ``f.`` and removes the artificial literal ``_l``.
