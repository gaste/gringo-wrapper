# Gringo Wrapper
A wrapper for [gringo](http://potassco.sourceforge.net/) that takes a logic program as input and outputs the grounded program without any optimizations. For example, calling gringo with the logic program
```
a.
b :- a.
```
leads to the grounded output ``a. b.`` Calling gringo-wrapper with the same logic program does not perform any optimizations.

Furthermore, (unless the ``--no-debug`` option is present) the gringo-wrapper adds a new unique constant ``_debug#(V1, ...)`` to the body of each non-fact rule of the logic program, where ``#`` is an integer starting from 1 to the number of non-fact rules in the logic program, and ``(V1, ...)`` is the list of all variables used in the rule (if any). In addition to adding the ``_debug`` constants to the body of the rules, one choice rule containing all debug constants is added to the program. The prefix of the constants can be specified using the ``--debug-constant`` option.

<p align="center">
<a href="https://github.com/gaste/gringo-wrapper/releases/latest"><img src="https://img.shields.io/github/release/gaste/gringo-wrapper.svg" alt="Latest Version"></img></a>
</p>

## Table of contents
 - [Usage](#usage)
 - [Building](#building)
 - [How it works](#how-it-works)

## Usage
Make sure that you have added the location of the [gringo binaries](http://sourceforge.net/projects/potassco/files/gringo/) to your `PATH` system variable.
Download the `gringo-wrapper-X.X-bin.zip` or `gringo-wrapper-X.X-bin.tar.gz` file from the [latest gringo-wrapper release](https://github.com/gaste/gringo-wrapper/releases/latest), or [build](#building) it yourself. Unpack the archive and use the scripts provided in the `bin/` directory to start the gringo wrapper:
```
gringo-wrapper [options] [files]
```
 - If you specify one or more files, gringo-wrapper will read the contents of that files and print the grounded program on the standard output.
 - If you do not specify a filename, use the standard input to specify the logic program to ground. Note that the input has to be delimited by EOF (which can be done in the console using `CTRL+D` on Unix machines and `CTRL+Z` on Windows machines).
 - Call `gringo-wrapper -h` for all options.

## Building
This project is managed using [Apache Maven](https://maven.apache.org/). To build it, clone the repository and execute the `package` goal from maven:

```
git clone https://github.com/gaste/gringo-wrapper
cd gringo-wrapper
mvn package
```

This will create a `.zip` and `.tar.gz` file containing the Java archive as well as start scripts for Windows and Unix in the `target/` directory.

## How it works
The gringo-wrapper replaces each fact ``f`` of the logic program with the rule ``f :- _l``, where ``_l`` is a new literal. Adding the rule ``_l | -_l`` ensures that gringo cannot do any optimization, since there are no facts in the logic program. After this modification, the gringo wrapper uses gringo to ground the modified logic program. Then it replaces each grounded rule ``f :- _l`` with the fact ``f.`` and removes the artificial literal ``_l``.

## Debug atom map
Unless the ``--no-debug`` option is present, the gringo-wrapper also appends a mapping of the debug constants to the rules at the end of the ground program. The entries of the debug atom map are of the following form:
```
10 _debug# #vars variables rule
```

For example, consider the following preprocessed logic program:
```
a :- b, _debug1.
pred(X,Y) :- n(X), n(Y), _debug2(X,Y).
```
The debug atom map of this program is
```
10 _debug1 0 a :- b.
10 _debug2 2 X Y pred(X,Y) :- n(X), n(Y).
```
