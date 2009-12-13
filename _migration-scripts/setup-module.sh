#!/bin/sh
USAGE="Usage: setup-module.sh [-hvf] [-m <module-root> [-n <module-name>] [-P <parent-name>]] [-p <pom-template>]"

# we want at least one parameter (it may be a flag or an argument)
if [ $# -le 1 ]; then
	echo $USAGE >&2
	exit 1
fi

# parse command line arguments
OPTIND=1
while getopts 'hvfm:n:P:p:' OPT; do
    case "$OPT" in
	h)  echo $USAGE
            exit 0
            ;;
	v)  VERBOSE="-v"
            ;;
	f)  FORCE_RM_FLAG="-f"
            ;;
	m)  MODULE_ROOT=$OPTARG
            ;;
	n)  MODULE_ID=$OPTARG
            ;;
	P)  PARENT_ID=$OPTARG
            ;;
        p)  POM_TEMPLATE=$OPTARG
            ;;
	?)  # all other characters - error
            echo $USAGE >&2
            exit 1
            ;;
    esac
done
shift `expr $OPTIND - 1`

# access additional parameters through $@ or $* as usual or using this loop:
# for PARAM; do
#    echo $PARAM
# done


if [ -z "$MODULE_ROOT" ] ; then
    echo "No module root specified" >&2
    echo $USAGE >&2
    exit 1
fi

if [ ! -e "$MODULE_ROOT" ] ; then
    mkdir -p $VERBOSE $MODULE_ROOT
fi

if [ -z "$POM_TEMPLATE" ] ; then
    echo "No pom.xml template specified"
else
    sed -e "s/@module.id@/$MODULE_ID/g" -e "s/@parent.id@/$PARENT_ID/g" < $POM_TEMPLATE > $MODULE_ROOT/pom.xml
fi


