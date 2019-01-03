# HeuristicsSolver
This repository contains the Java implementation of the greedy algorithm, local search and GRASP for the AMMM course project (UPC) by Unai Perez Mendizabal (unai.perez.mendizabal) and Ferran Torres Morales (ferran.torres.morales).

To compile it, Java 8 JDK and Apache Maven are needed. In Ubuntu, you can install the dependencies and compile the project with the following commands:

```bash
sudo apt install -y maven openjdk-8-jdk
git clone https://github.com/unaipme/HeuristicsSolver
cd HeuristicsSolver
mvn clean package
```
Then, to run the solver, there are two options: You can run it with the Greedy Algorithm + Local Search or using the GRASP meta-heuristics. For the first option, run the solver with the following command:

```bash
java -jar target/heuristics.jar <data file> greedy
```

For GRASP:

```bash
java -jar target/heuristics.jar <data file> grasp <alpha> <iterations>
```

- `<data file>` must be the path to a CPLEX-format .dat data file with values for all the input data needed by the program. The instances used for the project can be found in the `data/` directory. More can be created with [the generator](https://github.com/unaipme/InstanceGenerator).
- `<alpha>` must be a float number between `0.0` and `1.0`. The alpha value is used to generate the RCLs for the GRASP meta-heuristics. `0.0` would generate a purely greedy construction, whereas `1.0` would generate a completely random one.
- `<iterations>` must be an integer number representing the number of iterations wanted for the GRASP meta-heuristics.
