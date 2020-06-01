# Wallet Client Application

Minimal [Spring Boot](http://projects.spring.io/spring-boot/)

## Requirements

For building and running the application you need:

- [JDK 11](https://www.oracle.com/java/technologies/javase-jdk11-downloads.html)
- [Gradle](https://gradle.org)

## Introduction
This guide walks you through the process of creating an application that accesses relational JPA data through gRPC calls.

## About the application

This is a wallet client application that can be used to make multithread calls to the waller server application that will 
keep the track of a users monetary balance in the system. Storing separate balance for different currencies.
There are three main features: `Deposit` `Withdraw` `Balance`

# Requirements
* The client will emulate users depositing, withdrawing and checking balance of theirs funds.

There are three pre-defined parameters that should be informed to run the calls in a multithread way:
1. users (number of concurrent users emulated)
2. concurrent_threads_per_user (number of concurrent requests a user will make)
3. rounds_per_thread (number of rounds each thread is executing)

## Programming Languages

This project is authored in Java.

## Dependencies

* 	[Flyway](https://flywaydb.org/) - Version control for database
* 	[Git](https://git-scm.com/) - Free and Open-Source distributed version control system 
* 	[Lombok](https://projectlombok.org/) - Never write another getter or equals method again, with one annotation your class has a fully featured builder, 
Automate your logging variables, and much more
* 	[Spring Boot](https://spring.io/projects/spring-boot) - Framework to ease the bootstrapping and development of new Spring Applications

## Running the application locally (pre-requirements)

Make sure that the wallet server is running according to its needs.

## Running the client application locally

There are several ways to run a Spring Boot application on your local machine. One way is to execute the `main` method in the 
`com.galvao.wallet.WalletApplication` class from your IDE.

Alternatively you can use the [Spring Boot Gradle plugin](https://docs.spring.io/spring-boot/docs/current/gradle-plugin/reference/html/) like so:

```shell
./gradlew bootRun
```

* compiles Java classes to the /target directory
* copies all resources to the /target directory
* starts an embedded Apache Tomcat server

* Important: the parameters for multi-thread run must be informed as:
users=1,concurrent_threads_per_user=1,rounds_per_thread=1

## Folder structure + important files

```bash
.
├── README.md                                   # Important! Read before changing configuration
├── build.gradle
├── settings.gradle
└── src
    ├── main
    │   ├── java                                # gRPC service
    │   ├── proto                               # contains the protocol buffer file
    │   └── resources
    │       └── application.yml                 # Common application configuration
    └── test
        ├── java                                # Sample Testcases
        └── resources
```

## Testing the application

The wallet client will emulate a number of users concurrently using the wallet. The wallet client connects to the wallet server over gRPC.
The client emulating users doing rounds (a sequence of events). Whenever a round is needed it is picked at random from the following list of available rounds:

`Round A`

• Deposit 100 USD

• Withdraw 200 USD

• Deposit 100 EUR

• Get Balance

• Withdraw 100 USD

• Get Balance

• Withdraw 100 USD

`Round B`

• Withdraw 100 GBP

• Deposit 300 GPB

• Withdraw 100 GBP

• Withdraw 100 GBP

• Withdraw 100 GBP

`Round C`

• Get Balance

• Deposit 100 USD

• Deposit 100 USD

• Withdraw 100 USD

• Depsoit 100 USD

• Get Balance

• Withdraw 200 USD

• Get Balance
