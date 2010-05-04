#!/bin/sh
USAGE="Usage: register-providers.sh [-hvt] <module-root> <providers-list> <spi-provider-class-name>"

# we want at least one parameter (it may be a flag or an argument)
if [ $# -le 2 ]; then
	echo $USAGE >&2
	exit 1
fi

# parse command line arguments
ms_verbose=
ms_forceRmFlag=
target="main"

OPTIND=1
while getopts 'hvt' OPT; do
    case "$OPT" in
	h)  echo $USAGE
            exit 0
            ;;
	v)  ms_verbose="-v"
            ;;
	t)  target="test"
            ;;
	?)  # all other characters - error
            echo $USAGE >&2
            exit 1
            ;;
    esac
done
shift `expr $OPTIND - 1`

moduleRoot="$1"
providers="$2"
spiClassName="$3"

if [ ! -n "$moduleRoot" ] || [ ! -n "$providers" ] && [ ! -n "$spiClassName" ] ; then
    echo $USAGE >&2
    exit 1
fi

continueChoice () {
    printf "$* Continue? [Y/n]"

    read -n 1 -rs choice # reading single character
    echo

    if [ "$choice" = "n" ] || [ "$choice" = "N" ] ; then
        echo "Stopping script..."
        exit 0;
    fi
}


message () {
    if [ -n "$ms_verbose" ] ; then
        echo "$*"
    fi
}

appendProviders() {
    if [ $# -le 1 ]; then
        echo "USAGE: appendProviders <list> <file>" >&2
        return 1;
    fi

    list="$1"
    to="$2"

    message "Appending providers: $list\nto: $to\n"

    for p in `echo "$list" | tr "\:" " "`
    do
        message "  - $p"

        echo "$p" >> $to
    done
}

targetDir="$moduleRoot/src/$target/resources/META-INF/services"
if [ ! -e $targetDir ] ; then
    mkdir -p $ms_verbose $targetDir
fi
appendProviders "$providers" "$targetDir/$spiClassName"
