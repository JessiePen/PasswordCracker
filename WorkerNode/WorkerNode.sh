sudo apt update
sudo apt install openjdk-8-jdk
sudo rm Worker.java
sudo rm Worker.class
sudo wget --no-check-certificate --no-cache --no-cookies https://raw.githubusercontent.com/JessiePen/PAFinal_WNode/yuhe/src/Worker.java
sudo javac Worker.java
sudo java Worker