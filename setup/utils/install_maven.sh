MAVEN_OUTPUT=/tmp/maven.tar.gz

wget http://mirror.cc.columbia.edu/pub/software/apache/maven/maven-3/3.0.5/binaries/apache-maven-3.0.5-bin.tar.gz   -O $MAVEN_OUTPUT
tar xzf $MAVEN_OUTPUT -C /usr/local
echo "before sudo ln"
 sudo ln -s /usr/local/apache-maven-3.0.5 /usr/local/maven
echo "before ln"
ln -Tfs /usr/local/maven/bin/mvn /usr/bin/mvn
rm -f $MAVEN_OUTPUT