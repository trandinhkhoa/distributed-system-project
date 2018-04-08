if [ $# -ne 3 ] ; then
    echo "Usage: script.sh <host file> <hash> <rabbitMQ queue IP>";
    echo -e "\tThis script launch the servers provided by the hostfile and give them";
    echo -e "\tthe hash to compute.";
    echo -e "\t<host file> The host file to read from";
    echo -e "\t<hash> The hash to compute";
    echo -e "\t<rabbitMQ queue IP> The IP of the loadbalancer";
    exit 0;
fi

server_path=/$(pwd)/
username=$(whoami)
hash=$2
rabbitmq_ip=$3

while IFS='' read -r line || [[ -n "$line" ]]; do
        echo "Text read from file: $line";
        ssh $username@$line "cd $server_path; pwd ; ant server -Dhash=\"$hash\"" &
    done < "$1"
