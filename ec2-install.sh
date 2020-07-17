echo "JAVA INSTALL"

sudo apt-get update
sudo apt install openjdk-13-jre-headless

echo "SBT INSTALL"

echo "deb https://dl.bintray.com/sbt/debian /" | sudo tee -a /etc/apt/sources.list.d/sbt.list
curl -sL "https://keyserver.ubuntu.com/pks/lookup?op=get&search=0x2EE0EA64E40A89B84B2DF73499E82A75642AC823" | sudo apt-key add
sudo apt-get update
sudo apt-get install sbt

echo "GIT CLONE"
git clone https://github.com/maxbundscherer/scala-akka-prime/