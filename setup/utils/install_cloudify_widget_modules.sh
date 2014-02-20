CURRENT_DIRECTORY=`pwd`

cd "$(dirname "$0")"

echo "before calling to load_sysconfig.sh"
source load_sysconfig.sh

echo "before calling to checkout_project.sh"
${WIDGET_HOME}/setup/utils/checkout_project.sh $MODULES_GIT_LOCATION $MODULES_GIT_BRANCH $MODULES_HOME
CURRENT_DIR=`pwd`
cd ${MODULES_HOME}/bin
echo "before maven-install-custom.sh"
./maven-install-custom.sh
cd ..
echo "before calling to mvn install"
mvn install
echo "after calling to mvn install"

cd $CURRENT_DIR




