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
 - [Performance analysis](#performance-analysis)

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

## Performance analysis
We have assessed the performance of the gringo-wrapper by comparing it with the OUROBOROS [1,2] debugger.
We used the same instances and encodings of the second ASP competition that where used in the evaluation of [2].
Both the program encodings as well as the OUROBOROS translator can be found [here](http://www.sealion.at/index.php/develop) inside the project `org.mmdasp.sealion.ouroboros`.
For the OUROBOROS translator, we used the code of the `org.mmdasp.sealion.ouroboros.evaluation.EvaluationEA` class.
The encodings of the instances where taken from the `org.mmdasp.sealion.ouroboros.evaluation.programs` package.

All benchmarks where performed on a machine equipped with an Intel Core i7-3667U CPU, 8GB of RAM as well as a 256 GB Toshiba THNSNF256GMCS solid-state drive.
The grounders gringo, gringo-wrapper, and OUROBOROS where executed using the following commands, respectively:
```
gringo program.lp instance.in > grounded.txt
gringo-wrapper -nw program.lp instance.in > grounded.txt
java -jar ouroboros-translator.jar program.lp | gringo instance.in > grounded.txt
```

The results of the benchmark are presented below.
Each row contains the identifier of the instance, the number of non-grounded rules (#ng), the number of grounded rules by gringo 4.4 (#g g), gringo-wrapper (#g g-w), and OUROBOROS (#g o) as well as the time in seconds required for the grounding (t).
Furthermore, we report the increase of the size of the grounded program (inc) for the gringo-wrapper and OUROBOROS, compared to gringo.

| Program        | Instance | #ng  |   #g g |    t | #g g-w |     t | inc |      #g o |     t |      inc |
|----------------|----------|-----:|-------:|-----:|-------:|------:|----:|----------:|------:|---------:|
| Graph Coloring | 1-125    | 1672 |   6145 | 0.22 |   8031 |  0.63 | 1.3 |     19020 |  0.95 |      3.1 |
| Graph Coloring | 11-130   | 1757 |   6455 | 0.21 |   8416 |  0.68 | 1.3 |     19845 |  1.10 |      3.1 |
| Graph Coloring | 21-135   | 1986 |   7269 | 0.24 |   9305 |  0.73 | 1.3 |     21174 |  1.04 |      2.9 |
| Graph Coloring | 30-135   | 1794 |   6597 | 0.25 |   8633 |  0.64 | 1.3 |     20502 |  1.02 |      3.1 |
| Graph Coloring | 31-140   | 2039 |   7467 | 0.22 |   9578 |  0.67 | 1.3 |     21887 |  1.03 |      2.9 |
| Graph Coloring | 40-140   | 2219 |   8097 | 0.32 |  10208 |  0.68 | 1.3 |     22517 |  1.03 |      2.8 |
| Graph Coloring | 41-145   | 2262 |   8260 | 0.25 |  10446 |  0.68 | 1.3 |     23195 |  1.04 |      2.8 |
| Graph Coloring | 51-120   | 2405 |   8773 | 0.36 |  11034 |  0.76 | 1.3 |     24223 |  1.05 |      2.8 |
| Hanoi          | 09-28    |  104 |  31748 | 0.40 |  94166 |  1.61 | 3.0 |   1739800 |  8.09 |     54.8 |
| Hanoi          | 11-30    |  106 |  34056 | 0.33 | 100942 |  1.58 | 3.0 |   1864222 |  9.50 |     54.7 |
| Hanoi          | 15-34    |  110 |  38672 | 0.38 | 114524 |  2.11 | 3.0 |   2112986 |  9.43 |     54.6 |
| Hanoi          | 16-40    |  100 |  27137 | 0.35 |  80615 |  1.40 | 3.0 |   1491281 |  7.04 |     55.0 |
| Hanoi          | 22-60    |  102 |  28311 | 0.29 |  84644 |  1.43 | 3.0 |   1678483 |  7.80 |     59.3 |
| Hanoi          | 38-80    |  106 |  34044 | 0.23 | 100942 |  1.68 | 3.0 |   1864250 |  8.53 |     54.8 |
| Hanoi          | 41-100   |  104 |  31738 | 0.39 |  94166 |  1.52 | 3.0 |   1739830 | 13.24 |     54.8 |
| Hanoi          | 47-120   |   99 |  25968 | 0.19 |  77227 |  1.49 | 3.0 |   1429695 |  6.90 |     55.1 |
| Knights Tour   | 01-8     |   21 |   1384 | 0.34 |   3413 |  1.14 | 2.5 |  12985716 | 59.44 |   9382.7 |
| Knights Tour   | 03-12    |   22 |   3356 | 0.13 |   8652 |  0.60 | 2.6 | >72244034 |  >300 | >21526.8 |
| Knights Tour   | 05-16    |   21 |   6192 | 0.16 |  16285 |  0.64 | 2.6 | >69494641 |  >300 | >11223.3 |
| Knights Tour   | 06-20    |   21 |   9892 | 0.16 |  26321 |  0.88 | 2.7 | >62785993 |  >300 |  >6347.1 |
| Knights Tour   | 07-30    |   21 |  22922 | 0.40 |  61911 |  1.13 | 2.7 | >59166564 |  >300 |  >2581.2 |
| Knights Tour   | 08-40    |   21 |  41352 | 0.44 | 112501 |  1.27 | 2.7 | >54944042 |  >300 |  >1328.7 |
| Knights Tour   | 09-46    |   21 |  55002 | 0.53 | 150055 |  1.58 | 2.7 | >56443633 |  >300 |  >1026.2 |
| Knights Tour   | 10-50    |   22 |  65182 | 0.86 | 178094 |  2.15 | 2.7 | >62402315 |  >300 |   >957.4 |
| Partner Units  | 176-24   |   68 |  12563 | 0.22 |  14218 |  1.03 | 1.1 |    102023 |  1.47 |      8.1 |
| Partner Units  | 23-30    |  117 |  39231 | 0.29 |  42106 |  1.20 | 1.1 |    276645 |  2.11 |      7.1 |
| Partner Units  | 29-40    |  108 |  59979 | 0.34 |  64413 |  1.67 | 1.1 |    629639 |  3.35 |     10.5 |
| Partner Units  | 207-58   |  136 | 158564 | 0.61 | 168289 |  3.07 | 1.1 |   2726182 | 11.94 |     17.2 |
| Partner Units  | 204-67   |  141 | 218808 | 0.78 | 231083 |  5.30 | 1.1 |   4280282 | 17.79 |     19.6 |
| Partner Units  | 175-75   |  290 | 682015 | 2.10 | 699472 | 16.03 | 1.0 |   8604415 | 40.60 |     12.6 |
| Partner Units  | 52-100   |  254 | 952363 | 2.68 | 979603 | 16.61 | 1.0 |  20125857 | 90.10 |     21.1 |
| Partner Units  | 115-100  |  254 | 952369 | 2.86 | 979759 | 16.07 | 1.0 |  20317011 | 94.26 |     21.3 |

----------

## References
  [1]: Oetsch, J., Pührer, J., Tompits, H.: Catching the Ouroboros: On Debugging Non-ground Answer-Set Programs. TPLP 10(4-6), 2010 (2010) <br/>
  [2]: Polleres, A., Frühstück, M., Schenner, G., Friedrich, G.: Debugging Non-ground ASP Programs with Choice Rules, Cardinality and Weight Constraints. In: LPNMR, pp. 452–464 (2013)
