# apps
This repository is a collection of small applications I made over time for random purposes.

Given below is a list of the different apps and their what they're about.

# Bluetooth Group Chat

## Introduction
I made this app in college when my primary phone was N72. It lets a small group of people communicate via Bluetooth on a group chat. It's an app based on J2ME. I tested this on N72, and another old-style Nokia phone.

## Architecture
It follows a star topology/network with the server at the center of the star. Multiple clients (upto a max of 10) connect to the server. All communications are routed through server.
Steps:
* Someone starts the app in the "Server" mode and waits for other people to connect.
* Other people start the app in the "Client" mode and connect to the server using their Bluetooth device name.
* If server wants to send a message, it loops through all the clients and pushes the message.
* If a client wants to send a message, it sends it to the server, when then pushes the message to the rest of the clients.

## Uses
I developed this primarily as a learning experience and didn't use much practically. Everyone soon moved onto smartphones. One intended use was to text and cheat in exams where cellular network was poor or jammers were used. Since Bluetooth network can be established locally without cellular network.

