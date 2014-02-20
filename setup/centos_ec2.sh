
execute(){
    echo "executing $1"

    ${WIDGET_HOME}/setup/utils/$1
}

echo "clone cloudify-modules"

echo "before install java"
execute install_java.sh

echo "before install maven"
execute install_maven.sh

echo "before install cloudify_widget_modules"
execute install_cloudify_widget_modules.sh

echo "before install mysql"
execute install_mysql.sh

execute install_nginx.sh

execute install_ruby.sh

execute install_cloudify.sh

execute install_play.sh



execute install_node.sh

execute install_monit.sh

execute create_schema.sh

cd ${WIDGET_HOME}/setup/utils
execute upgrade.sh