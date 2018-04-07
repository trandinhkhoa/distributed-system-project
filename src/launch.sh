if [ $# -ne 1 ] ; then
    echo "This script require the path of an hostfile as argument.";
    exit 0;
fi

server_path=/$(pwd)/
username=$(whoami)

while IFS='' read -r line || [[ -n "$line" ]]; do
        echo "Text read from file: $line";
        ssh $username@$line "cd $server_path; pwd ; ant server" &
    done < "$1"
